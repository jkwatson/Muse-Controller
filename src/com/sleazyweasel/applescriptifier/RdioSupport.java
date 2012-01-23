package com.sleazyweasel.applescriptifier;

public class RdioSupport implements ApplicationSupport {
    private final AppleScriptTemplate appleScriptTemplate;

    public RdioSupport(AppleScriptTemplate appleScriptTemplate) {
        this.appleScriptTemplate = appleScriptTemplate;
    }

    public void playPause() {
        appleScriptTemplate.execute(Application.RDIO, "playpause");
    }

    public void next() {
        appleScriptTemplate.execute(Application.RDIO, "next track");
    }

    public void previous() {
        appleScriptTemplate.execute(Application.RDIO, "previous track");
    }

    public void thumbsUp() {
    }

    public void thumbsDown() {
    }

    public void setVolume(Integer volume) {
        appleScriptTemplate.execute(Application.RDIO, "set sound volume to " + volume);
    }
}
