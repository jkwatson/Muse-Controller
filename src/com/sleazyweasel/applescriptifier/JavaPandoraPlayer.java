package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.pandora.PandoraRadio;
import com.sleazyweasel.pandora.Song;
import com.sleazyweasel.pandora.Station;
import javazoom.jlgui.basicplayer.*;

import java.io.*;
import java.net.URL;
import java.util.*;

public class JavaPandoraPlayer implements MusicPlayer, BasicPlayerListener {
    private PandoraRadio pandoraRadio;
    private List<Station> stations;
    private Station station;
    private Song song;
    private Song[] playlist;
    private int currentSongPointer = -1;
    private BasicPlayer player = new BasicPlayer();
    private List<MusicPlayerStateChangeListener> listeners = new ArrayList<MusicPlayerStateChangeListener>();
    private int currentTime;
    private double volume = 0.7d;
    private MusicPlayerInputType currentInputType = MusicPlayerInputType.CHOOSE_STATION;

    public JavaPandoraPlayer() {
        player.addBasicPlayerListener(this);
    }

    @Override
    public void volumeUp() {
        volume = volume + 0.1d;
        if (volume > 1.0d) {
            volume = 1.0d;
        }
        applyGain();
    }

    private void applyGain() {
        try {
            player.setGain(volume);
        } catch (BasicPlayerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void volumeDown() {
        volume = volume - 0.1d;
        if (volume < 0.0d) {
            volume = 0.0d;
        }
        applyGain();
    }

    @Override
    public void close() {
        try {
            player.stop();
        } catch (BasicPlayerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bounce() {
        try {
            player.stop();
        } catch (BasicPlayerException e) {
            throw new RuntimeException("Failed to restart Pandora stream", e);
        }
        pandoraRadio = null;
        activate();
    }

    @Override
    public void activate() {
        if (pandoraRadio != null) {
            return;
        }
        pandoraRadio = new PandoraRadio();
        try {
            LoginInfo loginInfo = getLogin();
            pandoraRadio.connect(loginInfo.userName, loginInfo.password);
            notifyListeners();
        } catch (BadPandoraPasswordException b) {
            pandoraRadio = null;
            throw b;
        } catch (IOException e) {
            pandoraRadio = null;
            e.printStackTrace();
            throw new RuntimeException("Failed to log in to Pandora.", e);
        }
    }

    private void notifyListeners() {
        MusicPlayerState state = getState();
        for (MusicPlayerStateChangeListener listener : listeners) {
            try {
                listener.stateChanged(this, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private PandoraRadio getRadio() {
        if (pandoraRadio != null && pandoraRadio.isAlive()) {
            return pandoraRadio;
        }
        activate();
        return pandoraRadio;
    }


    @Override
    public MusicPlayerState getState() {
        PandoraRadio radio = getRadio();
        if (stations == null) {
            stations = radio.getStations();
        }
        Map<Integer, String> stationData = new HashMap<Integer, String>(stations.size());
        int i = 0;
        for (Station station : stations) {
//            System.out.println("station.getName() = " + station.getName());
            stationData.put(i++, station.getName());
        }

        String stationName = "";
        if (station != null) {
            stationName = station.getName();
        }
        boolean currentSongIsLoved = false;
        String title = "";
        String artist = "";
        String album = "";
        String albumArtUrl = "";
        String detailUrl = "";
        String currentTimeInTrack = "";
        if (song != null) {
            currentSongIsLoved = song.isLoved();
            title = song.getTitle();
            artist = song.getArtist();
            album = song.getAlbum();
            albumArtUrl = song.getAlbumCoverUrl();
            detailUrl = song.getAlbumDetailURL();
            //todo figure out how to get total track time.
            currentTimeInTrack = formatCurrentTime();
        }

        boolean isPlaying = isPlaying();
        return new MusicPlayerState(currentSongIsLoved, title, artist, stationName, album, currentInputType, stationData, albumArtUrl, currentTimeInTrack, isPlaying, detailUrl);
    }

    private String formatCurrentTime() {
        int minutes = currentTime / 60;
        int seconds = currentTime % 60;
        String secondsString = String.valueOf(seconds);
        if (secondsString.length() == 1) {
            secondsString = "0" + secondsString;
        }
        return minutes + ":" + secondsString;
    }

    @Override
    public void selectStation(Integer stationNumber) {
        station = stations.get(stationNumber);
        refreshPlaylist();
        next();
        currentInputType = MusicPlayerInputType.NONE;
        notifyListeners();
    }

    private void refreshPlaylist() {
        playlist = station.getPlaylist("mp3-hifi");
    }

    private void play(Song song) {
        this.song = song;
        try {
            player.open(new URL(song.getAudioUrl()));
            player.setGain(this.volume);
            player.play();
        } catch (Exception e) {
            //not sure what I can do here!?
            e.printStackTrace();
            throw new RuntimeException("Failed to play music.", e);
        }
    }

    @Override
    public void askToChooseStation() {
        currentInputType = MusicPlayerInputType.CHOOSE_STATION;
        notifyListeners();
    }

    //todo extract getLogin, saveConfig, getConfigFile, getConfigDirectory to a helper class and inject.
    private LoginInfo getLogin() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(getConfigFile()));
        String userLine = reader.readLine();
        String passwordLine = reader.readLine();
        String userName = userLine.substring(5);
        String password = passwordLine.substring(9);
        return new LoginInfo(userName, password);
    }

    public void saveConfig(String username, char[] password) throws IOException {
        File configDirectory = getConfigDirectory();
        if (!configDirectory.exists()) {
            configDirectory.mkdirs();
            Runtime.getRuntime().exec(new String[]{"chmod", "700", configDirectory.getAbsolutePath()});
        }
        File configFile = getConfigFile();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(configFile))));
        writer.write("user=" + username);
        writer.newLine();
        writer.write("password=" + new String(password));
        writer.newLine();
        writer.close();
    }

    private File getConfigFile() {
        return new File(getConfigDirectory(), "config");
    }

    private static File getConfigDirectory() {
        String userHome = System.getProperty("user.home");
        return new File(userHome + "/.config/pandora");
    }

    @Override
    public boolean isConfigured() {
        return getConfigFile().exists();
    }

    @Override
    public boolean isPlaying() {
        return player.getStatus() == BasicPlayer.PLAYING;
    }

    @Override
    public void addListener(MusicPlayerStateChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void cancelStationSelection() {
    }

    @Override
    public void playPause() {
        try {
            if (isPlaying()) {
                player.pause();
            } else {
                player.resume();
            }
        } catch (BasicPlayerException e) {
            e.printStackTrace();
            throw new RuntimeException("failed to play/pause", e);
        }
        notifyListeners();
    }

    @Override
    public void next() {
        if (station != null && playlist != null && playlist.length > 1) {
            play(nextSongToPlay());
        }
        System.out.println("playlist = " + Arrays.toString(playlist));
    }

    private Song nextSongToPlay() {
        ++currentSongPointer;
        Song songToPlay = playlist[currentSongPointer];
        if (currentSongPointer == playlist.length - 1) {
            refreshPlaylist();
            currentSongPointer = -1;
        }

        return songToPlay;
    }


    @Override
    public void previous() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void thumbsUp() {
        pandoraRadio.rate(station, song, true);
        song = new Song(song, 1);
    }

    @Override
    public void thumbsDown() {
        pandoraRadio.rate(station, song, false);
        next();
    }

    @Override
    public void opened(Object stream, Map properties) {
    }

    @Override
    public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
        Long positionInMicroseconds = (Long) properties.get("mp3.position.microseconds");
        int seconds = (int) (positionInMicroseconds / 1000000);
        boolean shouldNotify = false;
        if (seconds - currentTime >= 1) {
            shouldNotify = true;
        }
        currentTime = seconds;
        if (shouldNotify) {
            notifyListeners();
        }
    }

    @Override
    public void stateUpdated(BasicPlayerEvent event) {
        if (BasicPlayerEvent.EOM == event.getCode()) {
            next();
        }
    }

    @Override
    public void setController(BasicController controller) {
        System.out.println("JavaPandoraPlayer.setController");
    }

    private class LoginInfo {
        private final String userName;
        private final String password;

        public LoginInfo(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }
}
