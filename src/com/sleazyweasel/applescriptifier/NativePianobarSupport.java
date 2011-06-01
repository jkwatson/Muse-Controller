package com.sleazyweasel.applescriptifier;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NativePianobarSupport implements ApplicationSupport {

    private Process pianobar;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Thread readingThread;
    private LineBuffer data = new LineBuffer(20000);

    public NativePianobarSupport() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    pianobar.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                    //what to do here???
                }
            }
        }));
    }

    public void playPause() {
        activatePianoBar();
        sendKeyStroke('p');
    }

    public void next() {
        activatePianoBar();
        sendKeyStroke('n');
    }

    public void previous() {
        //no previous support in pandora
    }

    public void thumbsUp() {
        activatePianoBar();
        sendKeyStroke('+');
    }

    public void thumbsDown() {
        activatePianoBar();
        sendKeyStroke('-');
    }

    public synchronized void activatePianoBar() {
        if (pianobar == null) {
            try {
                pianobar = Runtime.getRuntime().exec("/opt/local/bin/pianobar");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            inputStream = pianobar.getInputStream();
            outputStream = pianobar.getOutputStream();
            readingThread = new Thread(new Runnable() {
                public void run() {
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    int character;
                    try {
                        boolean inPrefixGunk = false;
                        int prefixCount = 0;
                        boolean inTimeInfo = false;

                        while ((character = reader.read()) != -1) {
//                            System.out.println(character);
                            if (character == 27) {
                                inPrefixGunk = true;
                                continue;
                            }
                            if (inPrefixGunk) {
                                if (prefixCount < 2) {
                                    prefixCount++;
                                } else {
                                    inPrefixGunk = false;
                                    prefixCount = 0;
                                }
                                continue;
                            }
                            if (data.lastCharacterWasNewLine() && character == '#') {
                                inTimeInfo = true;
                                continue;
                            }
                            if (inTimeInfo) {
                                if (character == 13) {
                                    inTimeInfo = false;
                                }
                                continue;
                            }

//                            System.out.print(Character.valueOf((char) character));
                            data.add((char) character);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("io exception while reading", e);
                    }
                }
            });
            readingThread.start();
            waitForPianobarStartup();
        }
    }

    private void waitForPianobarStartup() {
        int tries = 0;
        while (tries < 5) {
            String currentScreenContents = getCurrentScreenContents();
            String[] lines = currentScreenContents.split("\\n");
            System.out.println("lines = " + Arrays.toString(lines));
            if (lines.length > 1) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //nothing to do..this is fine.
            }
            tries++;
        }
    }

    public String getCurrentScreenContents() {
        String contents = data.getContents();
        String[] lines = contents.split("\\n");
        StringBuilder results = new StringBuilder();
        for (String line : lines) {
            results.append(line);
            results.append("\n");
        }
        return results.toString();
    }

    public void sendKeyStroke(char key) {
        activatePianoBar();
        try {
            data.add(key);
            outputStream.write(key);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("keystroke failed", e);
        }
    }

    public void sendTextCommand(String command) {
        activatePianoBar();
        try {
            byte[] bytes = command.getBytes();
            for (byte aByte : bytes) {
                data.add((char) aByte);
                data.add('\n');
                //this bit is a hack, since it appears that pianobar has no visible reaction at all to empty input.
                // this puts a little feedback in the system, so the servlet knows data was entered.
                data.add('-');
            }
            outputStream.write(bytes);
            outputStream.write('\n');
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("text command failed", e);
        }
    }

    public List<String> getDataFromFile() {
        List<String> contents = new ArrayList<String>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("/tmp/pianobar_data")));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                contents.add(line);
            }

        } catch (FileNotFoundException e) {
            //well, then, we just return an empty list, don't we?
        } catch (IOException e) {
            //not sure what to do here yet.
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    //nothing to see here, move along.
                }
            }
        }
        return contents;
    }
}
