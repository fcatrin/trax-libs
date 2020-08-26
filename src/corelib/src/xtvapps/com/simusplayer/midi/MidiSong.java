package xtvapps.com.simusplayer.midi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fts.core.Log;

public class MidiSong {
	private static final String LOGTAG = MidiSong.class.getSimpleName();
	
	List<MidiTrack> tracks;
	boolean useSmpteTiming;
	long tempo;
	long ppq;

	public static MidiSong load(SimpleStream is) throws IOException {
		long id = (MidiUtils.read32le(is));
		if (id == MidiUtils.makeId("MThd")) {
			return loadSmf(is);
		} else if (id == MidiUtils.makeId("RIFF")) {
			return loadRiff(is);
		}
		throw new InvalidFormatException();
	}

	private static MidiSong loadRiff(SimpleStream is) throws IOException {
		/* skip file length */
		MidiUtils.read32le(is);

		/* check file type ("RMID" = RIFF MIDI) */
		long id = MidiUtils.read32le(is);
		if (id != MidiUtils.makeId("RMID")) {
			throw new IOException("Invalid format");
		}

		do {
			id = MidiUtils.read32le(is);
			long len = MidiUtils.read32le(is);
			
			if (id == MidiUtils.makeId("data")) break;
			is.skip((int)len+1);
		} while(true);
		
		id = MidiUtils.read32le(is);
		if (id != MidiUtils.makeId("MThd")) {
			throw new IOException("Invalid format");
		}
		
		return loadSmf(is);
	}

	private static MidiSong loadSmf(SimpleStream is) throws IOException {
		long headerLen = MidiUtils.read32le(is);
		if (headerLen < 6) {
			throw new InvalidFormatException();
		}

		long type = MidiUtils.readInt(is, 2);
		if (type != 0 && type != 1) {
			String msg = String.format("Type %d format is not supported", type);
			throw new InvalidFormatException(msg);
		}

		long nTracks = MidiUtils.readInt(is, 2);
		if (nTracks < 1 || nTracks > 1000) {
			String msg = String.format("Invalid number of tracks: %d", nTracks);
			throw new InvalidFormatException(msg);
		}
		
		long timeDivision = MidiUtils.readInt(is, 2);
		if (timeDivision < 0) {
			String msg = String.format("Invalid time division: %d", timeDivision);
			throw new InvalidFormatException(msg);
		}

		MidiSong song = new MidiSong();
		song.useSmpteTiming = (timeDivision & 0x8000) != 0;
		
		if (!song.useSmpteTiming) {
			/* time_division is ticks per quarter */
			song.tempo = 500000; /* default: 120 bpm */
			song.ppq   = timeDivision;
			Log.d(LOGTAG, String.format("smpt_timing off ppq %d", song.ppq));
		} else {
			/* upper byte is negative frames per second */
			int fps = (int)(0x80 - ((timeDivision >> 8) & 0x7f));
			/* lower byte is ticks per frame */
			timeDivision &= 0xff;
			/* now pretend that we have quarter-note based timing */
			switch (fps) {
			case 24:
				song.tempo = 500000;
				song.ppq   = 12 * timeDivision;
				break;
			case 25:
				song.tempo = 400000;
				song.ppq   = 10 * timeDivision;
				break;
			case 29: /* 30 drop-frame */
				song.tempo = 100000000;
				song.ppq   = 2997 * timeDivision;
				break;
			case 30:
				song.tempo = 500000;
				song.ppq   = 15 * timeDivision;
				break;
			default:
				Log.e(LOGTAG, String.format("Invalid number of SMPTE frames per second (%d)", fps));
			}
			Log.d(LOGTAG, String.format("smpt_timing on ppq %d tempo %d division %d", song.ppq, song.tempo, fps));
		}
		
		song.tracks = new ArrayList<MidiTrack>();
		long len = 0;
		for(int i = 0; i < nTracks; i++) {
			do {
				long id = MidiUtils.read32le(is);
				len = MidiUtils.readInt(is, 4);
				if (len < 0 || len >= 0x10000000) {
					String msg = String.format("invalid chunk length %d", len);
					throw new InvalidFormatException(msg);
				}

				if (id == MidiUtils.makeId("MTrk")) {
					break;
				}
				is.skip((int)len);
			} while (true);
			
			song.tracks.add(readTrack(is, song, i, len));
		}
	}
}
