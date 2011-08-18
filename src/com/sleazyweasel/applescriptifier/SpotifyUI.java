package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
import de.felixbruns.jotify.media.Playlist;
import layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class SpotifyUI implements MuseControllerFrame {

    private final AtomicBoolean executionLock = new AtomicBoolean(false);

    private final Widgets widgets = new Widgets();
    private final Models models = new Models();

    private final NativeSpotifySupport spotifySupport;
    private final JMenuBar mainMenuBar;
    private final JMenuItem spotifyMenuItem;

    public SpotifyUI(NativeSpotifySupport spotifySupport, JMenuBar mainMenuBar, JMenuItem spotifyMenuItem, MuseControllerPreferences preferences) {
        this.spotifySupport = spotifySupport;
        this.mainMenuBar = mainMenuBar;
        this.spotifyMenuItem = spotifyMenuItem;
        initUserInterface();
        preferences.setSpotifyAsStreamer();
    }

    private void initUserInterface() {
        initWidgetsAndModels();
        initLayout();
    }

    private void initWidgetsAndModels() {
        initMenuBar();
        initPlaylistComboBox();
        initWindow();
    }

    private void initPlaylistComboBox() {
        models.playlistComboBoxModel = new PlaylistComboBoxModel(spotifySupport);
        widgets.playlistComboBox = new JComboBox(models.playlistComboBoxModel);
        widgets.playlistComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (acquireLock()) {
                    releaseLock();
                }
            }
        });
        widgets.playlistComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    Playlist playlist = (Playlist) value;
                    setText(playlist.getName());
                } else {
                    setText("Choose a playlist");
                }
                return component;
            }
        });

    }

    private void initWindow() {
        widgets.window = new JFrame("Spotify");
        widgets.window.setContentPane(new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
//                ((TableLayout) getLayout()).drawGrid(this, g);
            }
        });
    }

    private void initMenuBar() {
    }

    private void initLayout() {
        widgets.window.getContentPane().setLayout(new TableLayout(new double[][]{
                {15, 130, 15, 300, 15},
                {15, 30, 15, 130, 15}}

        ));
        widgets.window.getContentPane().add(widgets.playlistComboBox, "1, 1, 3, 1, L");

        widgets.window.pack();
        widgets.window.setResizable(false);
    }

    public JFrame getWindow() {
        return widgets.window;
    }

    public void close() {
//        spotifySupport.close();
//        mainMenuBar.remove(widgets.menu);
        spotifyMenuItem.setEnabled(true);
        widgets.window.dispose();
    }

    private static class Widgets {
        private JFrame window;
        private JComboBox playlistComboBox;
    }

    private static class Models {
        private PlaylistComboBoxModel playlistComboBoxModel;
    }

    private boolean acquireLock() {
        if (executionLock.get()) {
            return false;
        }
        executionLock.getAndSet(true);
        return true;
    }

    private void releaseLock() {
        executionLock.set(false);
    }
}
