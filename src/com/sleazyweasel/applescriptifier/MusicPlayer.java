package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

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

    void initializeFromSavedUserState(MuseControllerPreferences preferences);

    public interface MusicPlayerStateChangeListener {
        void stateChanged(MusicPlayer player, MusicPlayerState state);
    }

    public boolean isConfigured();
}
