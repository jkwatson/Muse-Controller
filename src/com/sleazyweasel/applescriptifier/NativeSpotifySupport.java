package com.sleazyweasel.applescriptifier;

import de.felixbruns.jotify.media.Playlist;
import nl.pascaldevink.jotify.gui.listeners.PlayerListener;

import java.awt.*;
import java.util.List;

public interface NativeSpotifySupport extends MusicPlayer {
    boolean isSpotifyAuthorized();

    //todo return different stuff, depending on no connection vs. bad username/password
    //todo figure out better exception handling here.
    boolean authorize(String username, char[] password);

    List<Playlist> getPlaylists();

    void setListener(PlayerListener playbackListener);

    void play(Playlist playlist);

    void play();

    void pause();

    Image image(String imageCode);

    void setVolume(float volume);
}
