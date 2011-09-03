package com.sleazyweasel.applescriptifier;

import de.felixbruns.jotify.media.Playlist;
import nl.pascaldevink.jotify.gui.listeners.PlayerListener;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public interface NativeSpotifySupport {
    boolean isSpotifyAuthorized();

    //todo return different stuff, depending on no connection vs. bad username/password
    //todo figure out better exception handling here.
    boolean authorize(String username, char[] password);

    void saveSpotifyConfig(String username, char[] password) throws IOException;

    List<Playlist> getPlaylists();

    void close();

    void setListener(PlayerListener playbackListener);

    void play(Playlist playlist);

    void nextTrack();

    void play();

    void pause();

    Image image(String imageCode);

    void previousTrack();

    void setVolume(float volume);
}
