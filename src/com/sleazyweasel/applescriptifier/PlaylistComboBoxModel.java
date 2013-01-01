package com.sleazyweasel.applescriptifier;

import de.felixbruns.jotify.media.Playlist;

import javax.swing.*;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

public class PlaylistComboBoxModel extends DefaultComboBoxModel {
    private static final Logger logger = Logger.getLogger(PlaylistComboBoxModel.class.getName());
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
        logger.info("selectedItem = " + selectedItem);
        removeAllElements();
        for (Playlist playlist : playlists) {
            addElement(playlist);
        }
        setSelectedItem(selectedItem);
    }
}
