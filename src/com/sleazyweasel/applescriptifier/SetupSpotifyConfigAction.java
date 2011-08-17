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

    public SetupSpotifyConfigAction(MuseControllerPreferences preferences, JTextField usernameField, JPasswordField passwordField, NativeSpotifySupport spotifySupport, JMenuItem spotifyMenuItem, JFrame window) {
        this.preferences = preferences;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.spotifySupport = spotifySupport;
        this.spotifyMenuItem = spotifyMenuItem;
        this.parent = window;
    }

    public void actionPerformed(ActionEvent e) {
        boolean authorized = spotifySupport.authorize(usernameField.getText(), passwordField.getPassword());
        if (!authorized) {
            JOptionPane.showMessageDialog(usernameField, "Failed to authenticate. Try again.", "Fail", JOptionPane.ERROR_MESSAGE);
        } else {
            parent.dispose();
        }
    }
}
