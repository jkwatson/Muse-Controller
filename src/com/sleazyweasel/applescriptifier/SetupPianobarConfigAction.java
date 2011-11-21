package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

class SetupPianobarConfigAction implements ActionListener {

    private final MusicPlayer pianobarSupport;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JFrame parent;
    private final MuseControllerPreferences preferences;
    private final JMenuBar mainMenuBar;
    private final JMenuItem pandoraMenuItem;
    private final MuseControllerMain main;

    public SetupPianobarConfigAction(JFrame parent, MusicPlayer pianobarSupport, JTextField usernameField, JPasswordField passwordField, MuseControllerPreferences preferences, JMenuBar mainMenuBar, JMenuItem pandoraMenuItem, MuseControllerMain main) {
        this.parent = parent;
        this.pianobarSupport = pianobarSupport;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.preferences = preferences;
        this.mainMenuBar = mainMenuBar;
        this.pandoraMenuItem = pandoraMenuItem;
        this.main = main;
    }

    public void actionPerformed(ActionEvent event) {
        try {
            pianobarSupport.saveConfig(usernameField.getText(), passwordField.getPassword());
            parent.dispose();
            PandoraUI pandoraUI = new PandoraUI(pianobarSupport, mainMenuBar, pandoraMenuItem, preferences);
            pandoraUI.initialize();
            main.setActiveFrame(pandoraUI);
            pandoraUI.getWindow().setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Failed to Configure Pianobar", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (BadPandoraPasswordException e) {
            PandoraPasswordUI pandoraPasswordUI = new PandoraPasswordUI(pianobarSupport, preferences, mainMenuBar, pandoraMenuItem, main);

            JFrame window = pandoraPasswordUI.getWindow();
            window.setLocationRelativeTo(null);
            main.setActiveFrame(pandoraPasswordUI);
            window.setVisible(true);
        }

    }
}
