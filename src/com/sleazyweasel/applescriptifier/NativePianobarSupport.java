package com.sleazyweasel.applescriptifier;

import java.io.*;
import java.util.*;

public class NativePianobarSupport implements ApplicationSupport {

    private Process pianobar;
    private InputStream inputStream;
    private OutputStream outputStream;
    private LineBuffer data = new LineBuffer(20000);
    private String currentTimeInTrack = "";
    private String previousCurrentTimeInTrack = "";

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
        resetCurrentTime();
    }

    public void volumeUp() {
        activatePianoBar();
        sendKeyStroke(')');
        sendKeyStroke(')');
        sendKeyStroke(')');
    }

    public void volumeDown() {
        activatePianoBar();
        sendKeyStroke('(');
        sendKeyStroke('(');
        sendKeyStroke('(');
    }

    private void resetCurrentTime() {
        currentTimeInTrack = "";
        previousCurrentTimeInTrack = "";
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

    public void close() {
        if (pianobar != null) {
            pianobar.destroy();
            pianobar = null;
        }
        data = new LineBuffer(20000);
    }

    public synchronized void bounce() {
        if (pianobar != null) {
            pianobar.destroy();
            pianobar = null;
        }
        data = new LineBuffer(20000);
        activatePianoBar();
    }

    public synchronized void activatePianoBar() {
        if (pianobar == null) {
            try {
                String libraryPath = "DYLD_LIBRARY_PATH=" + System.getProperty("user.dir") + "/Muse Controller.app/Contents/Resources/Java/";
                String home = "HOME=" + System.getProperty("user.home");

                System.out.println("libraryPath = " + libraryPath);
                Map<String, String> environment = System.getenv();
                System.out.println("environment = " + environment);
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
            String[] lines = currentScreenContents.split("\n");
            System.out.println("lines = " + Arrays.toString(lines));
            if (lines.length > 1) {
                for (String line : lines) {
                    if (line.contains("Login... Ok.")) {
                        return;
                    }
                    if (line.contains("Error: Username and/or password not correct.")) {
                        pianobar = null;
                        data = new LineBuffer(20000);
                        throw new BadPandoraPasswordException();
                    }
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
            tries++;
        }
        if (tries == 5) {
            throw new RuntimeException("Failed to start pianobar!");
        }

    }

    private void waitForStationSelectionPrompt() {
        waitForText("[?] Select station:");
    }

    private void waitForText(String textToLookFor) {
        int tries = 0;
        while (tries < 5) {
            String currentScreenContents = getCurrentScreenContents();
            String[] lines = currentScreenContents.split("\n");
            if (lines.length > 1) {
                String lastLine = lines[lines.length - 1];
                if (lastLine.startsWith(textToLookFor)) {
                    return;
                }
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                return;
            }
            tries++;
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

            if (key == 's') {
                waitForStationSelectionPrompt();
            }

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
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream("/tmp/pianobar_data"), "UTF-8"));
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

    public InputType inputTypeRequested() {
        String[] lines = getCurrentScreenContents().split("\n");
        if (lines.length < 1) {
            return InputType.NONE;
        }
        String lastLine = lines[lines.length - 1];
        return lastLine.startsWith("[?] Select station:") ? InputType.CHOOSE_STATION : InputType.NONE;
    }

    public static boolean isPianoBarConfigured() {
        File pianoBarConfigDirectory = getPianobarConfigDirectory();
        return pianoBarConfigDirectory.isDirectory();
    }

    private static File getPianobarConfigDirectory() {
        String userHome = System.getProperty("user.home");
        return new File(userHome + "/.config/pianobar");
    }

    public void addListener(PianobarStateChangeListener listener) {
        listeners.add(listener);
    }

//    private AtomicLong lastNotificationPoint = new AtomicLong(System.currentTimeMillis());

    private void notifyListeners() {
//        System.out.println("NativePianobarSupport.notifyListeners firing");
        PianobarState state = getState();
        for (PianobarStateChangeListener listener : listeners) {
            try {
                listener.stateChanged(this, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public PianobarState getState() {
        List<String> data = getDataFromFile();
        return new PianobarState(currentSongIsLoved(data), extractTitle(data), extractArtist(data), extractStation(data), extractAlbum(data), inputTypeRequested(), parseStationList(data), getAlbumArtUrl(data), currentTimeInTrack, isPlaying(), getDetailUrl(data));
    }

    private String getAlbumArtUrl(List<String> data) {
        return getValueFromDataFile("coverArt=", data);
    }

    private String getDetailUrl(List<String> data) {
        return getValueFromDataFile("detailUrl=", data);
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

    public void selectStation(Integer stationNumber) {
        if (stationNumber != null) {
            sendTextCommand(stationNumber.toString());
            waitForText("|>");
            resetCurrentTime();
        }
    }

    public void askToChooseStation() {
        if (inputTypeRequested().equals(InputType.CHOOSE_STATION)) {
            return;
        }
        sendKeyStroke('s');
    }

    public void cancelStationSelection() {
        sendTextCommand("");
    }

    public void savePianobarConfig(String username, char[] password) throws IOException {
        File pianobarConfigDirectory = getPianobarConfigDirectory();
        if (!pianobarConfigDirectory.exists()) {
            pianobarConfigDirectory.mkdirs();
        }
        File configFile = new File(pianobarConfigDirectory, "config");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(configFile))));
        writer.write("user = " + username);
        writer.newLine();
        writer.write("password = " + new String(password));
        writer.newLine();
        writer.write("event_command = " + pianobarConfigDirectory.getAbsolutePath() + "/echo.pl");
        writer.newLine();
        writer.close();

        File echoFile = new File(System.getProperty("user.dir") + "/Muse Controller.app/native/echo.pl");
        File outputFile = new File(pianobarConfigDirectory, "/echo.pl");

        copy(echoFile, outputFile);

        outputFile.setExecutable(true);
    }

    private void copy(File echoFile, File outputFile) throws IOException {
        FileReader in = new FileReader(echoFile);
        FileWriter out = new FileWriter(outputFile);
        int c;

        while ((c = in.read()) != -1)
            out.write(c);

        in.close();
        out.close();
    }

    enum InputType {
        NONE, CHOOSE_STATION
    }

    private class PianobarStandardOutReader implements Runnable {
        public void run() {
            int character;
            try {
                InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
                boolean inPrefixGunk = false;
                int prefixCount = 0;
                boolean inTimeInfo = false;
                StringBuilder timeData = new StringBuilder(8);
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
                            pushTime(timeData.toString());
                            timeData = new StringBuilder(8);
                        }
                        else {
                            timeData.append((char) character);
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

    private void pushTime(String timeData) {
        previousCurrentTimeInTrack = currentTimeInTrack;
        currentTimeInTrack = timeData;
    }

    public boolean isPlaying() {
        return !currentTimeInTrack.equals(previousCurrentTimeInTrack);
    }

    public interface PianobarStateChangeListener {
        void stateChanged(NativePianobarSupport pianobarSupport, PianobarState state);
    }
}
