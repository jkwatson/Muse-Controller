package com.sleazyweasel.applescriptifier;

import java.io.IOException;

public class Main2 {

    public static void main(String[] args) throws InterruptedException, IOException {
        ScriptEngineAppleScriptTemplate template = new ScriptEngineAppleScriptTemplate();

        String result = template.execute(Application.PIANOBAR, "tell current terminal", "get name of current session", "end tell");
        System.out.println("result = " + result);

        System.exit(0);

        try {
            template.execute(Application.PIANOBAR, "tell current terminal", "select session \"Pianobar\"", "end tell");
        } catch (Exception e) {
            template.execute(Application.PIANOBAR, "tell current terminal", "launch session \"Pianobar\"", "end tell");
            waitForPianobarStartup(template);
            template.executeKeyStroke(Application.PIANOBAR, "\n");
        }

        template.executeKeyStrokeWithCommandKey(Application.PIANOBAR, "k");
        template.executeKeyStroke(Application.PIANOBAR, "i");
        String results = getCurrentScreenContents(template);

        String[] lines = results.split("\n");
        for (String line : lines) {
            System.out.println(line);
        }
    }

    private static void waitForPianobarStartup(AppleScriptTemplate appleScriptTemplate) {
        int tries = 0;
        while (tries < 10) {
            String contents = getCurrentScreenContents(appleScriptTemplate);
            if (contents.contains("Select station:")) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
            tries ++;
        }
    }


    private static String getCurrentScreenContents(AppleScriptTemplate template) {
        return template.execute(Application.PIANOBAR,
                    "tell current terminal",
                    "tell current session",
                    "get contents",
                    "end tell",
                    "end tell");
    }


//    	tell current terminal
//		try
//			select session "Pianobar"
//		on error
//			launch session "Pianobar"
//		end try
//	end tell
}
