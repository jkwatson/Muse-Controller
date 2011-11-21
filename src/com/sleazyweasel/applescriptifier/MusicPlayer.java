package com.sleazyweasel.applescriptifier;

import java.io.IOException;

public interface MusicPlayer extends ApplicationSupport {

    void volumeUp();

    void volumeDown();

    void close();

    void bounce();

    void activate();

    MusicPlayerState getState();

    void selectStation(Integer stationNumber);

    void askToChooseStation();

    void saveConfig(String username, char[] password) throws IOException;

    boolean isPlaying();

    void addListener(MusicPlayerStateChangeListener listener);

    void cancelStationSelection();

    public interface MusicPlayerStateChangeListener {
        void stateChanged(MusicPlayer pianobarSupport, MusicPlayerState state);
    }

    public boolean isConfigured();
}
