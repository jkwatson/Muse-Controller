package com.sleazyweasel.applescriptifier.preferences;


import com.sleazyweasel.applescriptifier.Application;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class MuseControllerPreferences {
    public static final String PIANOBAR_VETO_KEY = "pianobar.veto";
    private static final String MUSECONTROL_ENABLE_KEY = "musecontrol.enable";
    private static final String LAST_STREAMER_KEY = "last.streamer";
    private static final String PANDORA_STREAMER_VALUE = "Pandora";

    private final Preferences preferences;

    public MuseControllerPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    public boolean isPianoBarEnabled() {
        return !preferences.getBoolean(PIANOBAR_VETO_KEY, false);
    }

    public void enablePianoBar(boolean enable) {
        preferences.putBoolean(PIANOBAR_VETO_KEY, !enable);
    }

    public void enableMuseControl(boolean enable) {
        preferences.putBoolean(MUSECONTROL_ENABLE_KEY, enable);
    }

    public boolean isMuseControlEnabled() {
        return preferences.getBoolean(MUSECONTROL_ENABLE_KEY, true);
    }

    public void save() throws BackingStoreException {
        preferences.sync();
    }

    public boolean wasPandoraTheLastStreamerOpen() {
        String lastStreamer = preferences.get(LAST_STREAMER_KEY, null);
        //at the moment, Pandora is the default to start up with. In future, I should probably pop up a message on first startup of the app, to configure prefs and set this stuff up.
        return lastStreamer == null || lastStreamer.equals(PANDORA_STREAMER_VALUE);
    }

    public void setPandoraAsStreamer() {
        preferences.put(LAST_STREAMER_KEY, PANDORA_STREAMER_VALUE);
    }
}
