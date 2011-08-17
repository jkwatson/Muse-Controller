package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
import layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SpotifyPasswordUI implements MuseControllerFrame {

    private final NativeSpotifySupport spotifySupport;
    private final MuseControllerPreferences preferences;

    private final Widgets widgets = new Widgets();
    private final JMenuBar mainMenuBar;
    private final JMenuItem spotifyMenuItem;

    public SpotifyPasswordUI(NativeSpotifySupport spotifySupport, MuseControllerPreferences preferences, JMenuBar mainMenuBar, JMenuItem spotifyMenuItem) {
        this.spotifySupport = spotifySupport;
        this.preferences = preferences;
        this.mainMenuBar = mainMenuBar;
        this.spotifyMenuItem = spotifyMenuItem;
        initUserInterface();
        initLayout();
    }

    private void initUserInterface() {
        initWindow();
        initUsernameField();
        initPasswordField();
        initNeverShowAgainCheckbox();
        initOkButton();
        initCancelButton();
    }

    private void initWindow() {
        widgets.window = new JFrame("Spotify Login");
        widgets.window.setResizable(false);
    }

    private void initUsernameField() {
        widgets.usernameField = new JTextField();
    }

    private void initPasswordField() {
        widgets.passwordField = new JPasswordField();
    }

    private void initNeverShowAgainCheckbox() {
        widgets.neverShowAgainCheckbox = new JCheckBox("I don't use Spotify. Don't ask again.");
    }

    private void initOkButton() {
        widgets.okButton = new JButton("OK");
        widgets.okButton.addActionListener(new SetupSpotifyConfigAction(preferences, widgets.usernameField, widgets.passwordField, spotifySupport, spotifyMenuItem, widgets.window, mainMenuBar));
    }

    private void initCancelButton() {
        widgets.cancelButton = new JButton("Cancel");
        widgets.cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
                if (widgets.neverShowAgainCheckbox.isSelected()) {
                    preferences.enableSpotify(false);
                }
            }
        });
    }

    public JFrame getWindow() {
        return widgets.window;
    }

    private void initLayout() {
        Container contentPane = widgets.window.getContentPane();
        contentPane.setLayout(new TableLayout(new double[][]{
                {4, TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 2, TableLayout.PREFERRED, 4},
                {4, 30, 2, 30, 2, 25, 2, 30, 4}
        }));
        contentPane.add(new JLabel("Username:"), "1, 1");
        contentPane.add(widgets.usernameField, "3, 1, 5, 1");
        contentPane.add(new JLabel("Password:"), "1, 3");
        contentPane.add(widgets.passwordField, "3, 3, 5, 3");
        contentPane.add(widgets.neverShowAgainCheckbox, "3, 5");
        contentPane.add(widgets.cancelButton, "3, 7, r");
        contentPane.add(widgets.okButton, "5, 7, l");
        widgets.window.getRootPane().setDefaultButton(widgets.okButton);

        widgets.window.pack();

    }

    public void close() {
        widgets.window.dispose();
        spotifyMenuItem.setEnabled(true);
    }

    private static class Widgets {
        private JFrame window;
        private JPasswordField passwordField;
        private JTextField usernameField;
        private JCheckBox neverShowAgainCheckbox;
        private JButton okButton;
        private JButton cancelButton;
    }

}
