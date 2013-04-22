package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicPlayerSupplier implements MusicPlayer {
    private final Map<Application, MusicPlayer> suppliers = new HashMap<Application, MusicPlayer>();
    private final List<MusicPlayerStateChangeListener> listeners = new ArrayList<MusicPlayerStateChangeListener>();
    private Application currentApplication;

    public void addMusicPlayer(Application application, MusicPlayer musicPlayer) {
        suppliers.put(application, musicPlayer);
    }

    public void setCurrentApplication(Application currentApplication) {
        MusicPlayer oldPlayer = getCurrentMusicPlayer();
        if (oldPlayer != null) {
            for (MusicPlayerStateChangeListener listener : listeners) {
                oldPlayer.removeListener(listener);
            }
        }
        this.currentApplication = currentApplication;
        MusicPlayer newPlayer = getCurrentMusicPlayer();
        if (newPlayer != null) {
            for (MusicPlayerStateChangeListener listener : listeners) {
                newPlayer.addListener(listener);
            }
        }
    }

    @Override
    public void removeListener(MusicPlayerStateChangeListener listener) {
        listeners.remove(listener);
        MusicPlayer currentMusicPlayer = getCurrentMusicPlayer();
        if (currentMusicPlayer != null) {
            currentMusicPlayer.removeListener(listener);
        }
    }

    @Override
    public void addListener(MusicPlayer.MusicPlayerStateChangeListener listener) {
        listeners.add(listener);
        MusicPlayer currentMusicPlayer = getCurrentMusicPlayer();
        if (currentMusicPlayer != null) {
            currentMusicPlayer.addListener(listener);
        }
    }

    private MusicPlayer getCurrentMusicPlayer() {
        return suppliers.get(currentApplication);
    }

    public void volumeUp() {
        getCurrentMusicPlayer().volumeUp();
    }

    public void bounce() {
        getCurrentMusicPlayer().bounce();
    }

    public void thumbsDown() {
        getCurrentMusicPlayer().thumbsDown();
    }

    public void next() {
        getCurrentMusicPlayer().next();
    }

    public void playPause() {
        getCurrentMusicPlayer().playPause();
    }

    public boolean isConfigured() {
        return getCurrentMusicPlayer().isConfigured();
    }

    @Override
    public boolean isAuthorized() {
        return getCurrentMusicPlayer().isAuthorized();
    }

    @Override
    public void sleep() {
        getCurrentMusicPlayer().sleep();
    }

    @Override
    public void setVolume(double volume) {
        getCurrentMusicPlayer().setVolume(volume);
    }

    public boolean isPlaying() {
        return getCurrentMusicPlayer().isPlaying();
    }

    public void initializeFromSavedUserState(MuseControllerPreferences preferences) {
        getCurrentMusicPlayer().initializeFromSavedUserState(preferences);
    }

    public void activate() {
        getCurrentMusicPlayer().activate();
    }

    public void askToChooseStation() {
        getCurrentMusicPlayer().askToChooseStation();
    }

    public void close() {
        getCurrentMusicPlayer().close();
    }

    public void cancelStationSelection() {
        getCurrentMusicPlayer().cancelStationSelection();
    }

    public MusicPlayerState getState() {
        return getCurrentMusicPlayer().getState();
    }

    public void selectStation(Integer stationNumber) {
        getCurrentMusicPlayer().selectStation(stationNumber);
    }

    public void volumeDown() {
        getCurrentMusicPlayer().volumeDown();
    }

    public void saveConfig(String username, char[] password) throws IOException {
        getCurrentMusicPlayer().saveConfig(username, password);
    }

    public void thumbsUp() {
        getCurrentMusicPlayer().thumbsUp();
    }

    public void previous() {
        getCurrentMusicPlayer().previous();
    }

}
