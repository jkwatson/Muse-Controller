package com.sleazyweasel.applescriptifier;

public class ITunesSupport implements ApplicationSupport {
    private final AppleScriptTemplate appleScriptTemplate;

    public ITunesSupport(AppleScriptTemplate appleScriptTemplate) {
        this.appleScriptTemplate = appleScriptTemplate;
    }

    public void playPause() {
        appleScriptTemplate.execute(Application.ITUNES(), "playpause");
    }

    public void next() {
        appleScriptTemplate.execute(Application.ITUNES(), "next track");
    }

    public void previous() {
        appleScriptTemplate.execute(Application.ITUNES(), "previous track");
    }

    public void thumbsUp() {
    }

    public void thumbsDown() {
    }
}
