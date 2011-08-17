package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetupSpotifyConfigAction implements ActionListener {
    private final MuseControllerPreferences preferences;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final NativeSpotifySupport spotifySupport;
    private final JMenuItem spotifyMenuItem;
    private final JFrame parent;
    private final JMenuBar mainMenuBar;

    public SetupSpotifyConfigAction(MuseControllerPreferences preferences, JTextField usernameField, JPasswordField passwordField, NativeSpotifySupport spotifySupport, JMenuItem spotifyMenuItem, JFrame window, JMenuBar mainMenuBar) {
        this.preferences = preferences;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.spotifySupport = spotifySupport;
        this.spotifyMenuItem = spotifyMenuItem;
        this.parent = window;
        this.mainMenuBar = mainMenuBar;
    }

    public void actionPerformed(ActionEvent e) {
        boolean authorized = spotifySupport.authorize(usernameField.getText(), passwordField.getPassword());
        if (!authorized) {
            JOptionPane.showMessageDialog(usernameField, "Failed to authenticate. Try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        } else {
            parent.dispose();
            SpotifyUI spotifyUI = new SpotifyUI(spotifySupport, mainMenuBar, spotifyMenuItem, preferences);
            Main.setActiveFrame(spotifyUI);
            spotifyUI.getWindow().setVisible(true);
        }
    }
}
