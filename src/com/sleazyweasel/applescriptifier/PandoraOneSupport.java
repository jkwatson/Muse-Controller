package com.sleazyweasel.applescriptifier;

public class PandoraOneSupport implements ApplicationSupport {
    private final AppleScriptTemplate appleScriptTemplate;

    public PandoraOneSupport(AppleScriptTemplate appleScriptTemplate) {
        this.appleScriptTemplate = appleScriptTemplate;
    }

    public void playPause() {
        appleScriptTemplate.executeKeyCode(Application.PANDORAONE(), AppleScriptTemplate.SPACE);
    }

    public void next() {
        appleScriptTemplate.executeKeyCode(Application.PANDORAONE(), AppleScriptTemplate.RIGHT_ARROW);
    }

    public void previous() {
    }

    public void thumbsUp() {
        appleScriptTemplate.executeKeyStroke(Application.PANDORAONE(), AppleScriptTemplate.PLUS);
    }

    public void thumbsDown() {
        appleScriptTemplate.executeKeyStroke(Application.PANDORAONE(), AppleScriptTemplate.MINUS);
    }
}
