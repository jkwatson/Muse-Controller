package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
import layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;

import static layout.TableLayoutConstants.FILL;
import static layout.TableLayoutConstants.PREFERRED;

public class PreferencesGui {

    private final Widgets widgets = new Widgets();
    private final MuseControllerPreferences preferences;

    public PreferencesGui(MuseControllerPreferences preferences) {
        this.preferences = preferences;

        initWidgetsAndModels();
        initLayout();

    }

    private void initLayout() {
        double[][] columnsThenRows = {{15, FILL, PREFERRED, PREFERRED, 12}, {15, PREFERRED, PREFERRED, PREFERRED, PREFERRED, 5, PREFERRED, 15, PREFERRED, 12}};
        TableLayout tableLayout = new TableLayout(columnsThenRows);
        Container contentPane = widgets.window.getContentPane();
        contentPane.setLayout(tableLayout);

        contentPane.add(widgets.enablePandoraCheckbox, "1, 1");
        contentPane.add(widgets.enableSpotifyCheckbox, "1, 2");
        contentPane.add(widgets.enableMuseControlCheckbox, "1, 3");
        contentPane.add(widgets.bounceAirfoilOnPlayPause, "1, 4");
        JPanel labelHolder = new JPanel(new TableLayout(new double[][]{{FILL}, {FILL}}));
        labelHolder.add(new JLabel("(Application must be restarted for changes to take effect)"), "0,0,c,c");
        contentPane.add(labelHolder, "1,6,3,6");
        contentPane.add(widgets.saveButton, "2, 8");
        contentPane.add(widgets.cancelButton, "3, 8");
        widgets.window.getRootPane().setDefaultButton(widgets.saveButton);
        widgets.window.pack();
        widgets.cancelButton.requestFocus();
    }

    private void initWidgetsAndModels() {
        initWindow();
        initCancelButton();
        initEnablePandoraCheckbox();
        initEnableSpotifyCheckbox();
        initEnableMuseControlCheckbox();
        initBounceAirfoilOnPlayPauseCheckbox();
        initSaveButton();
    }


    private void initEnableMuseControlCheckbox() {
        widgets.enableMuseControlCheckbox = new JCheckBox("Enable Web Services for Muse Control", preferences.isMuseControlEnabled());
    }

    private void initCancelButton() {
        widgets.cancelButton = new JButton("Cancel");
        widgets.cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                widgets.window.dispose();
            }
        });
    }

    private void initSaveButton() {
        widgets.saveButton = new JButton("OK");
        widgets.saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                preferences.enablePandora(widgets.enablePandoraCheckbox.isSelected());
                preferences.enableSpotify(widgets.enableSpotifyCheckbox.isSelected());
                preferences.enableMuseControl(widgets.enableMuseControlCheckbox.isSelected());
                preferences.setAirfoilShouldBounceOnPlayPause(widgets.bounceAirfoilOnPlayPause.isSelected());
                try {
                    preferences.save();
                } catch (BackingStoreException e) {
                    JOptionPane.showMessageDialog(widgets.window, "Unable to Save Preferences", "Error", JOptionPane.ERROR_MESSAGE);
                }
                widgets.window.dispose();
            }
        });
    }

    private void initEnablePandoraCheckbox() {
        widgets.enablePandoraCheckbox = new JCheckBox("Enable Pandora Streaming", preferences.isPandoraEnabled());
    }

    private void initEnableSpotifyCheckbox() {
        widgets.enableSpotifyCheckbox = new JCheckBox("Enable Spotify Streaming", preferences.isSpotifyEnabled());
    }

    private void initBounceAirfoilOnPlayPauseCheckbox() {
        widgets.bounceAirfoilOnPlayPause = new JCheckBox("<html>Restart Airfoil Automatically from Muse Control <b><font color=\"red\">(EXPERIMENTAL!)</font></b>", preferences.shouldBounceAirfoilOnPlayPause());
    }

    private void initWindow() {
        widgets.window = new JFrame("Preferences");
    }

    public JFrame getWindow() {
        return widgets.window;
    }

    private static class Widgets {
        private JFrame window;
        private JCheckBox enablePandoraCheckbox;
        private JCheckBox enableSpotifyCheckbox;
        private JCheckBox enableMuseControlCheckbox;
        private JCheckBox bounceAirfoilOnPlayPause;
        private JButton saveButton;
        private JButton cancelButton;

    }
}