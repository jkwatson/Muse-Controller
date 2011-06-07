package com.sleazyweasel.applescriptifier;

import layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PandoraPasswordUI {
    private final NativePianobarSupport pianobarSupport;

    private final Widgets widgets = new Widgets();

    public PandoraPasswordUI(NativePianobarSupport pianobarSupport) {
        this.pianobarSupport = pianobarSupport;
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
        widgets.window = new JFrame("Pandora Login");
        widgets.window.setResizable(false);
    }

    private void initUsernameField() {
        widgets.usernameField = new JTextField();
    }

    private void initPasswordField() {
        widgets.passwordField = new JPasswordField();
    }

    private void initNeverShowAgainCheckbox() {
        widgets.neverShowAgainCheckbox = new JCheckBox("Never ask again");
    }

    private void initOkButton() {
        widgets.okButton = new JButton("OK");
        widgets.okButton.addActionListener(new SetupPianobarConfigAction(widgets.window, pianobarSupport, widgets.usernameField, widgets.passwordField));
    }

    private void initCancelButton() {
        widgets.cancelButton = new JButton("Cancel");
        widgets.cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                widgets.window.dispose();
            }
        });
    }

    public JFrame getWindow() {
        return widgets.window;
    }

    private void initLayout() {
        Container contentPane = widgets.window.getContentPane();
        contentPane.setLayout(new TableLayout(new double[][]{
                {2, TableLayout.PREFERRED, 2, TableLayout.FILL, 2, TableLayout.PREFERRED, 2},
                {2, 30, 2, 30, 2, 30, 2, 30, 2}
        }));
        contentPane.add(new JLabel("Username:"), "1, 1");
        contentPane.add(widgets.usernameField, "3, 1, 5, 1");
        contentPane.add(new JLabel("Password:"), "1, 3");
        contentPane.add(widgets.passwordField, "3, 3, 5, 3");
        contentPane.add(widgets.neverShowAgainCheckbox, "3, 5");
        contentPane.add(widgets.cancelButton, "3, 7");
        contentPane.add(widgets.okButton, "5, 7");

        widgets.window.pack();

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
