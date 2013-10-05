package com.sleazyweasel.applescriptifier;

public class MuseSupport implements ApplicationSupport {
    private final AppleScriptTemplate appleScriptTemplate;

    public MuseSupport(AppleScriptTemplate appleScriptTemplate) {
        this.appleScriptTemplate = appleScriptTemplate;
    }

    public void playPause() {
        appleScriptTemplate.execute(Application.MUSE(), "playpause");
    }

    public void next() {
        appleScriptTemplate.execute(Application.MUSE(), "next");
    }

    public void previous() {
    }

    public void thumbsUp() {
    }

    public void thumbsDown() {
    }
}
