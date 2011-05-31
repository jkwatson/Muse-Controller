package com.sleazyweasel.applescriptifier;

public class PianobarSupport implements ApplicationSupport {
    private final AppleScriptTemplate template;

    public PianobarSupport(AppleScriptTemplate appleScriptTemplate) {
        this.template = appleScriptTemplate;
    }

    public void playPause() {
        activatePianoBar();
        template.executeKeyStroke(Application.MUSECONTROLLER, "p");
    }

    public void next() {
        activatePianoBar();
        template.executeKeyStroke(Application.MUSECONTROLLER, "n");
    }

    public void previous() {
    }

    public void thumbsUp() {
        activatePianoBar();
        template.executeKeyStroke(Application.MUSECONTROLLER, "+");
    }

    public void thumbsDown() {
        activatePianoBar();
        template.executeKeyStroke(Application.MUSECONTROLLER, "-");
    }

    public void activatePianoBar() {
        String result = template.execute(Application.MUSECONTROLLER, "tell current terminal", "get name of current session", "end tell");
        if ("Pianobar".equals(result)) {
            return;
        }
        try {
            template.execute(Application.MUSECONTROLLER, "tell current terminal", "select session \"Pianobar\"", "end tell");
        } catch (Exception e) {
            template.execute(Application.MUSECONTROLLER, "tell current terminal", "launch session \"Pianobar\"", "end tell");
            waitForPianobarStartup();
        }
    }

    private void waitForPianobarStartup() {
        int tries = 0;
        while (tries < 10) {
            String contents = getCurrentScreenContents(template);
            if (contents.contains("Select station:")) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            tries++;
        }
    }


    public String getCurrentScreenContents(AppleScriptTemplate template) {
        String rawContents = template.execute(Application.MUSECONTROLLER,
                "tell current terminal",
                "tell current session",
                "get contents",
                "end tell",
                "end tell");
        //strip trailing newlines
        return rawContents.replaceAll("\\n+$", "");
    }


}
