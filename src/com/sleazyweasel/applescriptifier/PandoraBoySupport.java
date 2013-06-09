package com.sleazyweasel.applescriptifier;

public class PandoraBoySupport implements ApplicationSupport {
    private final AppleScriptTemplate appleScriptTemplate;

    public PandoraBoySupport(AppleScriptTemplate appleScriptTemplate) {
        this.appleScriptTemplate = appleScriptTemplate;
    }

    public void playPause() {
        appleScriptTemplate.execute(Application.PANDORABOY(), "playpause");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            //whatever!
        }
    }

    public void next() {
        appleScriptTemplate.execute(Application.PANDORABOY(), "next track");
    }

    public void previous() {
    }

    public void thumbsUp() {
        appleScriptTemplate.execute(Application.PANDORABOY(), "thumbs up");
    }

    public void thumbsDown() {
        appleScriptTemplate.execute(Application.PANDORABOY(), "thumbs down");
    }
}
