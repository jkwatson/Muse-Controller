package com.sleazyweasel.applescriptifier;

import java.io.*;
import java.util.*;

public class NativePianobarSupport implements ApplicationSupport {

    private Process pianobar;
    private InputStream inputStream;
    private OutputStream outputStream;
    private LineBuffer data = new LineBuffer(20000);

    private List<PianobarStateChangeListener> listeners = new ArrayList<PianobarStateChangeListener>();

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
                    if (pianobar != null) {
                        pianobar.destroy();
                    }
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
                String libraryPath = "DYLD_LIBRARY_PATH=" + System.getProperty("user.dir") + "/Muse Controller.app/Contents/Resources/Java/";
                String home = "HOME=" + System.getProperty("user.home");

                System.out.println("libraryPath = " + libraryPath);
                pianobar = Runtime.getRuntime().exec("native/pianobar", new String[]{libraryPath, home}, new File("Muse Controller.app"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            inputStream = pianobar.getInputStream();
            outputStream = pianobar.getOutputStream();
            Thread readingThread = new Thread(new PianobarStandardOutReader());
            readingThread.start();
            waitForPianobarStartup();
            kickOffPeriodicNotifierThread();
        }
    }

    private void kickOffPeriodicNotifierThread() {
        Thread notifierThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                        notifyListeners();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        notifierThread.setDaemon(true);
        notifierThread.start();
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
        if (tries == 5) {
            throw new RuntimeException("failed to start pianobar!");
        }
    }

    public String getCurrentScreenContents() {
        return data.getContents();
    }

    public void sendKeyStroke(char key) {
        activatePianoBar();
        try {
            data.add(key);
            outputStream.write(key);
            outputStream.flush();
            checkForPossibleNotificationPoint();
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
            }
            data.add('\n');
            //this bit is a hack, since it appears that pianobar has no visible reaction at all to empty input.
            // this puts a little feedback in the system, so the servlet knows data was entered.
            data.add('-');
            outputStream.write(bytes);
            outputStream.write('\n');
            outputStream.flush();
            checkForPossibleNotificationPoint();
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

    String getValueFromDataFile(String key) {
        List<String> dataFromFile = getDataFromFile();
        return getValueFromDataFile(key, dataFromFile);
    }

    String getValueFromDataFile(String key, List<String> dataFromFile) {
        for (String line : dataFromFile) {
            if (line.startsWith(key)) {
                return line.substring(line.indexOf(key) + key.length());
            }
        }
        return "";
    }

    //todo plenty of API cleanup here.
    String extractStation(List<String> data) {
        return getValueFromDataFile("stationName=", data);
    }

    String extractTitle(List<String> data) {
        return getValueFromDataFile("title=", data);
    }

    String extractAlbum(List<String> data) {
        return getValueFromDataFile("album=", data);
    }

    String extractArtist(List<String> data) {
        return getValueFromDataFile("artist=", data);
    }

    String extractHeart(List<String> data) {
        return currentSongIsLoved(data) ? "YES" : "NO";
    }

    boolean currentSongIsLoved(List<String> data) {
        String rating = getValueFromDataFile("rating=", data);
        return rating.equals("1");
    }

    String getAlbumArtUrl() {
        //todo consider doing something along the lines below to try to dig for other images.
//        valueFromDataFile = valueFromDataFile.replace("130W", "500W");
//        valueFromDataFile = valueFromDataFile.replace("130H", "434H");
        return getValueFromDataFile("coverArt=");
    }

    public InputType inputTypeRequested() {
        String[] lines = getCurrentScreenContents().split("\n");
        if (lines.length < 1) {
            return InputType.NONE;
        }
        String lastLine = lines[lines.length - 1];
        return lastLine.startsWith("[?] Select station:") ? InputType.CHOOSE_STATION : InputType.NONE;
    }

    public static boolean isPianoBarSupportEnabled() {
        String userHome = System.getProperty("user.home");
        File pianoBarConfigDirectory = new File(userHome + "/.config/pianobar");
        return pianoBarConfigDirectory.isDirectory();
    }

    public void addListener(PianobarStateChangeListener listener) {
        listeners.add(listener);
    }

//    private AtomicLong lastNotificationPoint = new AtomicLong(System.currentTimeMillis());

    private void notifyListeners() {
//        System.out.println("NativePianobarSupport.notifyListeners firing");
        PianobarState state = getState();
        for (PianobarStateChangeListener listener : listeners) {
            listener.stateChanged(this, state);
        }
    }

    public PianobarState getState() {
        List<String> data = getDataFromFile();
        return new PianobarState(currentSongIsLoved(data), extractTitle(data), extractArtist(data), extractStation(data), extractAlbum(data), inputTypeRequested(), parseStationList(data), getAlbumArtUrl(data));
    }

    private String getAlbumArtUrl(List<String> data) {
        return getValueFromDataFile("coverArt=", data);
    }

    Map<Integer, String> parseStationList(List<String> pianobarData) {
        Map<Integer, String> stations = new HashMap<Integer, String>();

        for (String dataLine : pianobarData) {
            if (dataLine.startsWith("station") && !dataLine.startsWith("stationCount") && !dataLine.startsWith("stationName")) {
                String stationNumber = dataLine.substring(dataLine.indexOf("station") + 7, dataLine.indexOf("="));
                String stationName = dataLine.substring(dataLine.indexOf("=") + 1);
                stations.put(Integer.valueOf(stationNumber), stationName);
            }
        }
        return stations;
    }

    private void checkForPossibleNotificationPoint() {
        if (!inputTypeRequested().equals(InputType.NONE)) {
            notifyListeners();
        } else {
            String[] split = getCurrentScreenContents().split("[\n\r]");
            if (split.length > 0) {
                String lastLine = split[split.length - 1];
//                System.out.println("lastLine = " + lastLine);
                if (lastLine.startsWith("|>") && lastLine.endsWith("\"") || lastLine.endsWith("Ok.") || lastLine.equals("-")) {
                    notifyListeners();
                }
            }
        }
    }


    enum InputType {
        NONE, CHOOSE_STATION
    }

    private class PianobarStandardOutReader implements Runnable {
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
                    checkForPossibleNotificationPoint();
                }
            } catch (IOException e) {
                throw new RuntimeException("io exception while reading", e);
            }
        }
    }

    public interface PianobarStateChangeListener {
        void stateChanged(NativePianobarSupport pianobarSupport, PianobarState state);
    }
}
