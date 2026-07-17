# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`trax-libs` provides the core playback libraries for Trax (formerly Simple Music Player / SiMusPlayer). It is a single Gradle module (`corelib`) that wraps several C/C++ music-format emulator/synthesizer libraries behind a common Java player API, exposed via JNI. This library is consumed by sibling application projects (e.g. "TraX for Linux" — see the `onEndedCallback` API, which exists specifically for that consumer).

The `fts:core` dependency (`fts.core.*` imports) is an external library resolved from `mavenLocal()` — it is a separate project (FTS), not part of this repo. If it appears missing, it needs to be `publishToMavenLocal`'d from that project first.

## Build

This is a Gradle project (`java-library` plugin), but the JNI native layer is built with a plain `make`, invoked automatically by Gradle:

```
./gradlew build          # builds Java, and runs jniBuild (make) as a dependency of processResources
./gradlew install        # publishes corelib to mavenLocal (group "trax", artifactId "core")
```

The `jniBuild` Gradle task just runs `make` inside `corelib/src/main/jni`. To iterate on native code directly:

```
cd corelib/src/main/jni
make            # builds libtrax-corelib.<ext> into ../../../build/jni/libs/<arch>/
make clean
```

The native Makefile auto-detects Linux/architecture (`armv7l` vs `x86_64`) and picks a matching `JAVA_HOME` for JNI headers — check/adjust those paths in `corelib/src/main/jni/Makefile` if building on a new machine. It also builds `libgme` via CMake as a static-lib dependency the first time it's needed.

There is also an `Android.mk`/`Application.mk` under the same `jni` directory for Android NDK builds, parallel to the Makefile-based desktop build.

Java compatibility is fixed at 1.8 (required to build against FTS Core).

## Regenerating JNI headers

After changing the native method signatures on `ModPlayer`, `FluidPlayer`, or `GMEPlayer`, regenerate the corresponding JNI headers (see `corelib/src/main/jni/README.txt`):

```
cd corelib/src/main/jni
javah -classpath ../../../build/classes/java/main xtvapps.trax.core.ModPlayer
javah -classpath ../../../build/classes/java/main xtvapps.trax.core.FluidPlayer
javah -classpath ../../../build/classes/java/main xtvapps.trax.core.GMEPlayer
```

## Architecture

### Player hierarchy

`MediaPlayer` (`corelib/src/main/java/xtvapps/trax/core/MediaPlayer.java`) is the abstract base for all format players. It owns the generic playback state machine (play/pause/stop/seek, `timeElapsed`/`timeTotal`, a `MediaPlayerListener` callback, and an `onEndedCallback` used by the Linux app for shutdown coordination) and spins up a `MediaPlayerControllerThread` per `play()` call. Subclasses only need to implement format-specific hooks:

- `onPrepare(File)` — open the native decoder/synth and return an `AudioRenderer` that fills PCM buffers on demand.
- `onFinish()` / `onInit()` / `onRelease()` — native resource lifecycle.
- `doForward()`, `doRewind()`, `seek()`, `getWave()`, `muteChannel()` — format-specific transport/visualization.

Concrete players, each backed by a different native library via JNI:

- `ModPlayer` — tracker modules (`xmp*` natives), backed by `libs/libxmp` (libxmp-lite).
- `GMEPlayer` — console/chiptune formats (NSF, SAP, VGZ, etc., `gme*` natives), backed by `libs/game-music-emu`.
- `FluidPlayer` — MIDI via FluidSynth (`fluid*` natives) using a loaded SoundFont file, with a separate `FluidMidiThread` driving MIDI event timing.

There is also a pure-Java MIDI path independent of FluidSynth: `xtvapps.trax.midi.MidiPlayer` drives a `MidiSequencer` by walking parsed `MidiSong`/`MidiTrack`/`MidiEvent` data and dispatching events in real time against tempo/ticks — this is a separate implementation from `FluidPlayer`, not a shared code path.

### Audio pipeline

Audio flows through a fixed producer/consumer pipeline decoupled from decoding, in `xtvapps.trax.core.audio`:

1. `AudioRenderThread` repeatedly calls the current `AudioRenderer.fillBuffer()` (i.e., the native decoder) into a rotating pool of `AudioBuffer`s, applies an optional `AudioProcessor`, and marks buffers `Filled`.
2. `AudioPlayerThread` pulls `Filled` buffers via `getNextBuffer()` and writes them to the platform-specific `WaveDevice` (abstract; the concrete audio output implementation lives in the consuming app, not here), then marks buffers `Free`.

Buffer size is derived from `freq / resolution` rounded up to the next power of two (`TraXCoreUtils.findNextPowerOfTwo`). `MediaPlayer.play()` is handed both threads (owned/created by the caller) and just attaches/detaches its `AudioRenderer` to the shared `AudioRenderThread` as playback starts/stops — the threads themselves are long-lived and reused across track changes.

### Native layer

`corelib/src/main/jni/` contains the JNI glue (`modplayer.cpp`, `gmeplayer.cpp`, `fluidplayer.cpp`, `common.c`/`.h`) that bridges the Java native method declarations to the vendored C/C++ libraries under `libs/`:

- `libs/libxmp` — module/tracker format playback.
- `libs/game-music-emu` — chiptune/console sound format playback (built as a static lib via its own CMake build, linked into the final `.so`/native lib).
- `libs/timidity` — pulled in by the desktop Makefile for MIDI-related support code.

`corelib/src/main/jni/libs` is a symlink to the top-level `libs/` directory, so both the Android NDK build and the desktop Makefile build share the same vendored sources.

Note: `formats/midi/` and `libs/midiloader/` are legacy/duplicated native MIDI loader sources that are **not** wired into the current build (`libs/midiloader/Makefile` is referenced only by the top-level desktop Makefile via commented-out `include`, and `formats/midi` has its own standalone Makefile not invoked by anything) — don't assume changes there take effect unless you also update the build wiring.
