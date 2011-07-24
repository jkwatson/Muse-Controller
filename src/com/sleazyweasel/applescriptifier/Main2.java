package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

public class Main2 {

    public static void main(String[] args) throws InterruptedException, IOException {
        final AtomicBoolean stop = new AtomicBoolean(false);
        Process process = Runtime.getRuntime().exec("/opt/local/bin/pianobar");
        final InputStream inputStream = process.getInputStream();
        OutputStream outputStream = process.getOutputStream();
        Thread t = new Thread(new Runnable() {
            public void run() {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        if (stop.get()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException("blah!", e);
                }
            }
        });
        t.start();
        Reader reader = new InputStreamReader(System.in);
        int character;
        while ((character = System.in.read()) != -1) {
            outputStream.write(character);
            outputStream.flush();
        }

        System.out.println("done");

        process.waitFor();
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
            tries++;
        }
    }


    private static String getCurrentScreenContents(AppleScriptTemplate template) {
        return template.execute(Application.MUSECONTROLLER,
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
