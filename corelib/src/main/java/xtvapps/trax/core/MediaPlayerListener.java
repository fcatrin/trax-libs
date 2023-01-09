package xtvapps.trax.core;

public interface MediaPlayerListener {
    void onStart();
    void onProgress(int progress, int duration);
    void onEnd();
}
