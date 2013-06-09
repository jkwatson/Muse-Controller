package com.sleazyweasel.applescriptifier;

public class PulsarSupport implements ApplicationSupport {
    private final AppleScriptTemplate appleScriptTemplate;

    public PulsarSupport(AppleScriptTemplate appleScriptTemplate) {
        this.appleScriptTemplate = appleScriptTemplate;
    }

    public void playPause() {
        appleScriptTemplate.execute(Application.PULSAR(), "playpause");
    }

    public void next() {
        appleScriptTemplate.execute(Application.PULSAR(), "next");
    }

    public void previous() {
        appleScriptTemplate.execute(Application.PULSAR(), "previous");
    }

    public void thumbsUp() {
    }

    public void thumbsDown() {
    }
}
