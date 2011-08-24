package com.sleazyweasel.applescriptifier;

import de.felixbruns.jotify.media.Playlist;

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
}