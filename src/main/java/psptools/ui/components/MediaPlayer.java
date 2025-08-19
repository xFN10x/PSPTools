package psptools.ui.components;

import psptools.ui.interfaces.VideoPlayingListiener;

public class MediaPlayer {

    private volatile Thread playThread;
    private boolean playing = true;

    public MediaPlayer(String file) {
        
    }

    public MediaPlayer(String file, VideoPlayingListiener listiener) { // video
        
    }

    public void stop() {
    }
}