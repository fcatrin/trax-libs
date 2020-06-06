package xtvapps.simusplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import xtvapps.simusplayer.core.WaveDevice;

public class AndroidWaveDevice extends WaveDevice {
	private static final String LOGTAG = AndroidWaveDevice.class.getSimpleName();
	private AudioTrack audioTrack;

	public AndroidWaveDevice(int freq, int bufferSize) {
		super(freq, bufferSize);
	}

	@Override
	public void open() {
		int minbuffsize = AudioTrack.getMinBufferSize(
				getFreq(), AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

		int bufferSize = getBufferSize();
		Log.d(LOGTAG, "buffersize: " + bufferSize + ", minbuffersize"  + minbuffsize);
		if (minbuffsize > bufferSize) {
			bufferSize = minbuffsize;
		}
		audioTrack = new AudioTrack(
				AudioManager.STREAM_MUSIC, getFreq(), 
				AudioFormat.CHANNEL_OUT_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize,
				AudioTrack.MODE_STREAM);
		audioTrack.play();
	}

	@Override
	public void write(short[] samples, int size) {
		audioTrack.write(samples, 0, size);
	}

	@Override
	public void close() {
		audioTrack.stop();
		audioTrack.release();
	}

}
