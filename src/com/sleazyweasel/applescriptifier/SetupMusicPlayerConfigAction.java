package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class SetupMusicPlayerConfigAction implements ActionListener {
    private static final Logger logger = Logger.getLogger(SetupMusicPlayerConfigAction.class.getName());
    private final MusicPlayer musicPlayer;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JFrame parent;
    private final MuseControllerPreferences preferences;
    private final JMenuBar mainMenuBar;
    private final JMenuItem pandoraMenuItem;
    private final MuseControllerMain main;

    public SetupMusicPlayerConfigAction(JFrame parent, MusicPlayer musicPlayer, JTextField usernameField, JPasswordField passwordField, MuseControllerPreferences preferences, JMenuBar mainMenuBar, JMenuItem pandoraMenuItem, MuseControllerMain main) {
        this.parent = parent;
        this.musicPlayer = musicPlayer;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.preferences = preferences;
        this.mainMenuBar = mainMenuBar;
        this.pandoraMenuItem = pandoraMenuItem;
        this.main = main;
    }

    public void actionPerformed(ActionEvent event) {
        try {
            musicPlayer.saveConfig(usernameField.getText(), passwordField.getPassword());
            parent.dispose();
            PandoraUI pandoraUI = new PandoraUI(musicPlayer, mainMenuBar, pandoraMenuItem, preferences);
            pandoraUI.initialize();
            main.setActiveFrame(pandoraUI);
            pandoraUI.getWindow().setVisible(true);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception caught.", e);
            JOptionPane.showMessageDialog(parent, "Failed to Configure Pandora", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (BadPandoraPasswordException e) {
            PandoraPasswordUI pandoraPasswordUI = new PandoraPasswordUI(musicPlayer, preferences, mainMenuBar, pandoraMenuItem, main);

            JFrame window = pandoraPasswordUI.getWindow();
            window.setLocationRelativeTo(null);
            main.setActiveFrame(pandoraPasswordUI);
            window.setVisible(true);
        }

    }
}
