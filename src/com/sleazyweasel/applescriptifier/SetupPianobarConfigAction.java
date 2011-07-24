package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

class SetupPianobarConfigAction implements ActionListener {

    private final NativePianobarSupport pianobarSupport;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JFrame parent;
    private final MuseControllerPreferences preferences;

    public SetupPianobarConfigAction(JFrame parent, NativePianobarSupport pianobarSupport, JTextField usernameField, JPasswordField passwordField, MuseControllerPreferences preferences) {
        this.parent = parent;
        this.pianobarSupport = pianobarSupport;
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.preferences = preferences;
    }

    public void actionPerformed(ActionEvent event) {
        try {
            pianobarSupport.savePianobarConfig(usernameField.getText(), passwordField.getPassword());
            parent.dispose();
            PianobarUI pianobarUI = new PianobarUI(pianobarSupport);
            pianobarUI.initialize();
            pianobarUI.getWindow().setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Failed to Configure Pianobar", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (BadPandoraPasswordException e) {
            PandoraPasswordUI pandoraPasswordUI = new PandoraPasswordUI(pianobarSupport, preferences);

            JFrame window = pandoraPasswordUI.getWindow();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        }

    }
}
