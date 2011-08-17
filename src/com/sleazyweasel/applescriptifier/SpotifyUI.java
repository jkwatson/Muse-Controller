package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

import javax.swing.*;
import java.awt.*;
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
        initWindow();
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

    private ImageIcon getIcon(String name) {
        return new ImageIcon(getClass().getClassLoader().getResource(name));
    }

    private void initLayout() {
        widgets.window.setPreferredSize(new Dimension(600, 400));
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
    }

    private static class Models {
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
