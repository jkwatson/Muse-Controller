package com.sleazyweasel.applescriptifier;

import de.felixbruns.jotify.media.Playlist;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

public class PlaylistComboBoxModel extends DefaultComboBoxModel {

    private final NativeSpotifySupport spotifySupport;

    public PlaylistComboBoxModel(NativeSpotifySupport spotifySupport) {
        super(new Vector<Playlist>());
        this.spotifySupport = spotifySupport;
        setSelectedItem(null);
    }

    public Playlist getSelectedStation() {
        return (Playlist) super.getSelectedItem();
    }

    public void refreshContents() {
        List<Playlist> playlists = spotifySupport.getPlaylists();
        Playlist selectedItem = getSelectedStation();
        removeAllElements();
        for (Playlist playlist : playlists) {
            addElement(playlist);
        }
        setSelectedItem(selectedItem);
    }
}
