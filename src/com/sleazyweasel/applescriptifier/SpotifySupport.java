package com.sleazyweasel.applescriptifier;

public class SpotifySupport implements ApplicationSupport {
    private final AppleScriptTemplate appleScriptTemplate;

    public SpotifySupport(AppleScriptTemplate appleScriptTemplate) {
        this.appleScriptTemplate = appleScriptTemplate;
    }

    public void playPause() {
        appleScriptTemplate.execute(Application.SPOTIFY, "playpause");
    }

    public void next() {
        appleScriptTemplate.execute(Application.SPOTIFY, "next track");
    }

    public void previous() {
        appleScriptTemplate.execute(Application.SPOTIFY, "previous track");
    }

    public void thumbsUp() {
    }

    public void thumbsDown() {
    }
}
