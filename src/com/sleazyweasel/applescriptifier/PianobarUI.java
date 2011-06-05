package com.sleazyweasel.applescriptifier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PianobarUI {

    private final Widgets widgets = new Widgets();
    private final Models models = new Models();

    private final NativePianobarSupport pianobarSupport;

    public PianobarUI(NativePianobarSupport pianobarSupport) {
        this.pianobarSupport = pianobarSupport;
        initUserInterface();
    }

    private void initUserInterface() {
        System.out.println("PianobarUI.initUserInterface");
        initWidgetsAndModels();
        initLayout();
    }

    private void initWidgetsAndModels() {
        System.out.println("PianobarUI.initWidgetsAndModels");
        initWindow();
        initStationNameLabel();
        initArtistLabel();
        initAlbumLabel();
        initSongLabel();
        initPlayPauseButton();
        initNextButton();
        initThumbsUpButton();
        initThumbsDownButton();
    }

    private void initWindow() {
        System.out.println("PianobarUI.initWindow");
        widgets.window = new JFrame();
    }

    private void initStationNameLabel() {
        System.out.println("PianobarUI.initStationNameLabel");
        widgets.stationNameLabel = new JLabel();
    }

    private void initArtistLabel() {
        System.out.println("PianobarUI.initArtistLabel");
        widgets.artistLabel = new JLabel();
    }

    private void initAlbumLabel() {
        System.out.println("PianobarUI.initAlbumLabel");
        widgets.albumLabel = new JLabel();
    }

    private void initSongLabel() {
        System.out.println("PianobarUI.initSongLabel");
        widgets.songLabel = new JLabel();
    }

    private void initPlayPauseButton() {
        System.out.println("PianobarUI.initPlayPauseButton");
        widgets.playPauseButton = new JButton("|>");
        widgets.playPauseButton.setEnabled(false);
        widgets.playPauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.playPause();
            }
        });
    }

    private void initNextButton() {
        System.out.println("PianobarUI.initNextButton");
        widgets.nextButton = new JButton(">>");
        widgets.nextButton.setEnabled(false);
        widgets.nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.next();
            }
        });
    }

    private void initThumbsUpButton() {
        System.out.println("PianobarUI.initThumbsUpButton");
        widgets.thumbsUpButton = new JButton("+");
        widgets.thumbsUpButton.setEnabled(false);
        widgets.thumbsUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.thumbsUp();
            }
        });
    }

    private void initThumbsDownButton() {
        System.out.println("PianobarUI.initThumbsDownButton");
        widgets.thumbsDownButton = new JButton("-");
        widgets.thumbsDownButton.setEnabled(false);
        widgets.thumbsDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.thumbsDown();
            }
        });
    }

    private void initLayout() {
        System.out.println("PianobarUI.initLayout");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(widgets.playPauseButton);
        buttonPanel.add(widgets.nextButton);
        buttonPanel.add(widgets.thumbsUpButton);
        buttonPanel.add(widgets.thumbsDownButton);

        widgets.window.getContentPane().setLayout(new GridLayout(0, 2, 5, 5));
        widgets.window.getContentPane().add(new JLabel("Station:"));
        widgets.window.getContentPane().add(widgets.stationNameLabel);
        widgets.window.getContentPane().add(new JLabel("Artist:"));
        widgets.window.getContentPane().add(widgets.artistLabel);
        widgets.window.getContentPane().add(new JLabel("Album:"));
        widgets.window.getContentPane().add(widgets.albumLabel);
        widgets.window.getContentPane().add(new JLabel("Song:"));
        widgets.window.getContentPane().add(widgets.songLabel);
        widgets.window.getContentPane().add(buttonPanel);

        widgets.window.pack();
    }

    public JFrame getWindow() {
        System.out.println("PianobarUI.getWindow");
        return widgets.window;
    }

    public void initialize() {
        System.out.println("PianobarUI.initialize");
        pianobarSupport.addListener(new NativePianobarSupport.PianobarStateChangeListener() {
            public void stateChanged(NativePianobarSupport pianobarSupport, PianobarState state) {
                widgets.stationNameLabel.setText(state.getStation());
                widgets.artistLabel.setText(state.getArtist());
                widgets.albumLabel.setText(state.getAlbum());
                widgets.songLabel.setText(state.getTitle());
                widgets.playPauseButton.setEnabled(!state.isInputRequested());
                widgets.nextButton.setEnabled(!state.isInputRequested());
                widgets.thumbsDownButton.setEnabled(!state.isInputRequested());
                widgets.thumbsUpButton.setEnabled(!state.isInputRequested());
            }
        });
    }

    private static class Widgets {
        private JFrame window;
        private JButton playPauseButton;
        private JButton nextButton;
        private JButton thumbsUpButton;
        private JButton thumbsDownButton;
        private JLabel stationNameLabel;
        private JLabel artistLabel;
        private JLabel albumLabel;
        private JLabel songLabel;
    }

    private static class Models {

    }
}
