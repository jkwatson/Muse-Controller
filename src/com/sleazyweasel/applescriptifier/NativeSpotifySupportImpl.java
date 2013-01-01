package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
import de.felixbruns.jotify.JotifyPool;
import de.felixbruns.jotify.exceptions.AuthenticationException;
import de.felixbruns.jotify.exceptions.ConnectionException;
import de.felixbruns.jotify.media.Playlist;
import de.felixbruns.jotify.media.PlaylistContainer;
import de.felixbruns.jotify.media.Track;
import nl.pascaldevink.jotify.gui.JotifyApplication;
import nl.pascaldevink.jotify.gui.JotifyPlayer;
import nl.pascaldevink.jotify.gui.listeners.JotifyBroadcast;
import nl.pascaldevink.jotify.gui.listeners.PlayerListener;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NativeSpotifySupportImpl implements NativeSpotifySupport, PlayerListener {
    private static final Logger logger = Logger.getLogger(NativeSpotifySupportImpl.class.getName());

    private JotifyPool jotifyPool;
    private JotifyPlayer jotifyPlayer;
    private float volume;
    private Status playerStatus;
    private Track currentTrack;
    private int currentPlayerPosition;
    private Playlist currentPlaylist;
    private MusicPlayerInputType currentInputType = MusicPlayerInputType.NONE;
    private List<MusicPlayerStateChangeListener> listeners = new ArrayList<MusicPlayerStateChangeListener>();

    public NativeSpotifySupportImpl() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (jotifyPool != null) {
                    try {
                        jotifyPool.close();
                    } catch (ConnectionException e) {
                        logger.log(Level.WARNING, "Exception caught.", e);;
                    }
                }
            }
        });
    }

    private synchronized JotifyPlayer getJotifyPlayer() {
        if (jotifyPlayer == null) {
            try {
                jotifyPlayer = new JotifyPlayer(getJotifyPool());
                JotifyBroadcast.getInstance().addPlayerListener(this);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception caught.", e);;
                throw new RuntimeException(e);
            }
        }
        return jotifyPlayer;
    }

    private synchronized JotifyPool getJotifyPool() {
        if (jotifyPool == null) {
            jotifyPool = new JotifyPool(2);
        }
        return jotifyPool;
    }

    public boolean isSpotifyAuthorized() {
        JotifyPool pool = getJotifyPool();

        File configFile = getConfigFile();
        if (!configFile.exists()) {
            return false;
        }
        BufferedReader reader = null;
        try {
            pool.close();
            reader = new BufferedReader(new FileReader(configFile));
            String userLine = reader.readLine();
            String passwordLine = reader.readLine();
            String userName = userLine.substring(5);
            String password = passwordLine.substring(9);
            pool.login(userName, password);

        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Exception caught.", e);;
                }
            }
        }

        return true;
    }

    //todo return different stuff, depending on no connection vs. bad username/password
    //todo figure out better exception handling here.
    public boolean authorize(String username, char[] password) {
        try {
            getJotifyPool().login(username, new String(password));
            saveConfig(username, password);
        } catch (ConnectionException e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            return false;
        } catch (AuthenticationException e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            return false;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            return false;
        }
        return true;
    }

    public void saveConfig(String username, char[] password) throws IOException {
        File spotifyConfigDirectory = getSpotifyConfigDirectory();
        if (!spotifyConfigDirectory.exists()) {
            spotifyConfigDirectory.mkdirs();
            Runtime.getRuntime().exec(new String[]{"chmod", "700", spotifyConfigDirectory.getAbsolutePath()});
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
        return new File(getSpotifyConfigDirectory(), "config");
    }

    private static File getSpotifyConfigDirectory() {
        String userHome = System.getProperty("user.home");
        return new File(userHome + "/.config/spotify");
    }

    @Override
    public List<Playlist> getPlaylists() {
        //todo figure out a way to background this task, so the user can start using the app sooner.
        try {
            PlaylistContainer playlistContainer = getJotifyPool().playlistContainer();
            List<Playlist> playlists = playlistContainer.getPlaylists();
            List<Playlist> results = new ArrayList<Playlist>(playlists.size());
            for (Playlist playlist : playlists) {
                Playlist reifiedPlaylist = getJotifyPool().playlist(playlist.getId(), true);
                if (reifiedPlaylist.hasTracks()) {
                    results.add(reifiedPlaylist);
                }
            }
            return results;
        } catch (TimeoutException e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            getJotifyPool().close();
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        } finally {
            jotifyPool = null;
        }
    }

    @Override
    public void setListener(PlayerListener playbackListener) {
        if (playbackListener != null) {
            JotifyBroadcast.getInstance().addPlayerListener(playbackListener);
        } else {
            //todo add code to remove a player listener
        }
    }

    @Override
    public void play(Playlist playlist) {
        this.currentPlaylist = playlist;
        JotifyPlayer player = getJotifyPlayer();
        List<Track> tracks = playlist.getTracks();
        List<Track> browsedTracks;
        try {
            browsedTracks = getJotifyPool().browse(tracks);
        } catch (TimeoutException e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            this.jotifyPool = null;
            this.jotifyPlayer = null;
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            //todo figure out a better way to do this!
            this.jotifyPool = null;
            this.jotifyPlayer = null;
            throw new RuntimeException(e);
        }

        player.controlSelect(browsedTracks);
        player.controlPlay();
    }

    @Override
    public void next() {
        getJotifyPlayer().controlNext();
    }

    @Override
    public void play() {
        getJotifyPlayer().controlPlay();
    }

    @Override
    public void pause() {
        getJotifyPlayer().controlPause();
    }

    @Override
    public void previous() {
        getJotifyPlayer().controlPrevious();
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        getJotifyPlayer().controlVolume(volume);
    }

    @Override
    public void volumeUp() {
        setVolume(Math.min(volume + .1f, 1f));
    }

    @Override
    public void volumeDown() {
        setVolume(Math.max(0, volume - .1f));
    }

    @Override
    public void initializeFromSavedUserState(MuseControllerPreferences preferences) {
        setVolume(preferences.getPreviousSpotifyVolume());
    }

    @Override
    public void removeListener(MusicPlayerStateChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void playPause() {
        if (Status.PLAY.equals(playerStatus)) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void thumbsUp() {
        //spotify doesn't support this idea.
    }

    @Override
    public void thumbsDown() {
        //spotify doesn't support this idea.
    }

    @Override
    public Image image(String imageCode) {
        try {
            return getJotifyPool().image(imageCode);
        } catch (TimeoutException e) {
            return new ImageIcon(JotifyApplication.class.getResource("images/cover.png")).getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public URL imageUrl(String imageCode) {
        try {
            return new URL("http://o.scdn.co/image/" + imageCode);
        } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            return null;
        }
    }

    @Override
    public void bounce() {
        //no op for now...
    }

    @Override
    public void activate() {
        //no op?
    }

    @Override
    public MusicPlayerState getState() {
        String title = "";
        String artistName = "";
        String albumName = "";
        String cover = "";
        if (currentTrack != null) {
            title = currentTrack.getTitle();
            artistName = currentTrack.getArtist().getName();
            albumName = currentTrack.getAlbum().getName();
            //todo this is not a URL that anyone can use to get the image... figure out how to get an image URL from spotify, if it is even possible.
            cover = imageUrl(currentTrack.getCover()).toString();
        }
        String playlistName = "";
        if (currentPlaylist != null) {
            playlistName = currentPlaylist.getName();
        }
        return new MusicPlayerState(false, title, artistName, playlistName, albumName, currentInputType,
                buildStationMap(), cover, renderCurrentPosition(), isPlaying(), null, volume);
    }

    private String renderCurrentPosition() {
        //todo prettify.
        return String.valueOf(currentPlayerPosition);
    }

    private Map<Integer, String> buildStationMap() {
        return null;
    }

    @Override
    public void selectStation(Integer stationNumber) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void askToChooseStation() {
        currentInputType = MusicPlayerInputType.CHOOSE_STATION;
    }


    @Override
    public boolean isPlaying() {
        return Status.PLAY.equals(playerStatus);
    }

    @Override
    public void addListener(MusicPlayerStateChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void cancelStationSelection() {
        currentInputType = MusicPlayerInputType.NONE;
    }

    @Override
    public boolean isConfigured() {
        return getConfigFile().exists();
    }

    @Override
    public boolean isAuthorized() {
        return isSpotifyAuthorized();
    }

    private void notifyListeners() {
        for (MusicPlayerStateChangeListener listener : listeners) {
            listener.stateChanged(this, getState());
        }
    }

    //*************************  methods from PlayerListener
    @Override
    public void playerTrackChanged(Track track) {
        currentTrack = track;
        notifyListeners();
    }

    @Override
    public void playerStatusChanged(Status status) {
        this.playerStatus = status;
        notifyListeners();
    }

    @Override
    public void playerPositionChanged(int position) {
        currentPlayerPosition = position;
        notifyListeners();
    }
}
