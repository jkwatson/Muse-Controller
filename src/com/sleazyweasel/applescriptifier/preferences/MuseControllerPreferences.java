package com.sleazyweasel.applescriptifier.preferences;


import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class MuseControllerPreferences {
    public static final String PIANOBAR_VETO_KEY = "pianobar.veto";
    private static final String MUSECONTROL_ENABLE_KEY = "musecontrol.enable";

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
}
