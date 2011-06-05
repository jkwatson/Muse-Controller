package com.sleazyweasel.applescriptifier;

import ch.randelshofer.quaqua.JSheet;
import ch.randelshofer.quaqua.SheetEvent;
import ch.randelshofer.quaqua.SheetListener;
import layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class PianobarUI {

    private final Widgets widgets = new Widgets();
    private final Models models = new Models();

    private final NativePianobarSupport pianobarSupport;

    public PianobarUI(NativePianobarSupport pianobarSupport) {
        this.pianobarSupport = pianobarSupport;
        initUserInterface();
    }

    private void initUserInterface() {
        initWidgetsAndModels();
        initLayout();
    }

    private void initWidgetsAndModels() {
        initWindow();
        initStationNameLabel();
        initArtistLabel();
        initAlbumLabel();
        initSongLabel();
        initPlayPauseButton();
        initNextButton();
        initThumbsUpButton();
        initThumbsDownButton();
        initChooseStationButton();
    }

    private void initWindow() {
        widgets.window = new JFrame("Pandora");
    }

    private void initStationNameLabel() {
        widgets.stationNameLabel = new JLabel();
    }

    private void initArtistLabel() {
        widgets.artistLabel = new JLabel();
    }

    private void initAlbumLabel() {
        widgets.albumLabel = new JLabel();
    }

    private void initSongLabel() {
        widgets.songLabel = new JLabel();
    }

    private void initPlayPauseButton() {
        widgets.playPauseButton = new JButton("|>");
        setButtonDefaults(widgets.playPauseButton);
        widgets.playPauseButton.setEnabled(false);
        widgets.playPauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.playPause();
            }
        });
    }

    private void setButtonDefaults(JButton button) {
        button.putClientProperty("JComponent.sizeVariant", "small");
        button.putClientProperty("Quaqua.Button.style", "square");
    }

    private void initNextButton() {
        widgets.nextButton = new JButton(">>");
        setButtonDefaults(widgets.nextButton);
        widgets.nextButton.setEnabled(false);
        widgets.nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.next();
            }
        });
    }

    private void initThumbsUpButton() {
        widgets.thumbsUpButton = new JButton("+");
        setButtonDefaults(widgets.thumbsUpButton);
        widgets.thumbsUpButton.setEnabled(false);
        widgets.thumbsUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.thumbsUp();
            }
        });
    }

    private void initThumbsDownButton() {
        widgets.thumbsDownButton = new JButton("-");
        setButtonDefaults(widgets.thumbsDownButton);
        widgets.thumbsDownButton.setEnabled(false);
        widgets.thumbsDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.thumbsDown();
            }
        });
    }

    private void initChooseStationButton() {
        widgets.chooseStationButton = new JButton("Station");
        setButtonDefaults(widgets.chooseStationButton);
        widgets.chooseStationButton.setEnabled(false);
        widgets.chooseStationButton.addActionListener(new ChooseStationAction());
    }

    private void initLayout() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(widgets.playPauseButton);
        buttonPanel.add(widgets.nextButton);
        buttonPanel.add(widgets.thumbsUpButton);
        buttonPanel.add(widgets.thumbsDownButton);
        buttonPanel.add(widgets.chooseStationButton);

        int border = 10;
        int gap = 2;
        JPanel infoPanel = new JPanel(new TableLayout(new double[][]{
                {border, TableLayout.PREFERRED, gap, TableLayout.FILL, border},
                {border, 0.25, gap, 0.25, gap, 0.25, gap, 0.25, border}
        }));
        infoPanel.add(new JLabel("Station:"), "1, 1");
        infoPanel.add(widgets.stationNameLabel, "3, 1");
        infoPanel.add(new JLabel("Artist:"), "1, 3");
        infoPanel.add(widgets.artistLabel, "3, 3");
        infoPanel.add(new JLabel("Album:"), "1, 5");
        infoPanel.add(widgets.albumLabel, "3, 5");
        infoPanel.add(new JLabel("Song:"), "1, 7");
        infoPanel.add(widgets.songLabel, "3, 7");

        widgets.window.getContentPane().setLayout(new BorderLayout());
        widgets.window.getContentPane().add(infoPanel, BorderLayout.CENTER);
        widgets.window.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        widgets.window.pack();
    }

    public JFrame getWindow() {
        return widgets.window;
    }

    public void initialize() {
        pianobarSupport.addListener(new PianobarStateChangeListener());
        pianobarSupport.activatePianoBar();
    }

    private static class Widgets {
        private JFrame window;
        private JButton playPauseButton;
        private JButton nextButton;
        private JButton thumbsUpButton;
        private JButton thumbsDownButton;
        private JButton chooseStationButton;
        private JLabel stationNameLabel;
        private JLabel artistLabel;
        private JLabel albumLabel;
        private JLabel songLabel;
    }

    private static class Models {

    }

    private class PianobarStateChangeListener implements NativePianobarSupport.PianobarStateChangeListener {

        public void stateChanged(final NativePianobarSupport pianobarSupport, PianobarState state) {
            widgets.playPauseButton.setEnabled(!state.isInputRequested());
            widgets.nextButton.setEnabled(!state.isInputRequested());
            widgets.thumbsDownButton.setEnabled(!state.isInputRequested());
            widgets.thumbsUpButton.setEnabled(!state.isInputRequested());

            if (state.isInputRequested() && NativePianobarSupport.InputType.CHOOSE_STATION.equals(state.getInputTypeRequested())) {
                widgets.chooseStationButton.setEnabled(true);
            }
            widgets.stationNameLabel.setText(state.getStation());
            widgets.artistLabel.setText(state.getArtist());
            widgets.albumLabel.setText(state.getAlbum());
            widgets.songLabel.setText(state.getTitle());

        }
    }

    private static class StationChoice {
        private final Integer key;
        private final String stationName;

        public StationChoice(Integer key, String stationName) {
            this.key = key;
            this.stationName = stationName;
        }

        public Integer getKey() {
            return key;
        }

        public String getStationName() {
            return stationName;
        }

        @Override
        public String toString() {
            //todo I've never felt so dirty in my life, using toString as a renderer. Please make this better!
            return stationName;
        }
    }

    private class ChooseStationAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            pianobarSupport.askToChooseStation();
            PianobarState state = pianobarSupport.getState();
            Map<Integer, String> stations = state.getStations();
            java.util.List<StationChoice> choices = new ArrayList<StationChoice>();
            java.util.List<Integer> list = new ArrayList<Integer>(stations.keySet());
            Collections.sort(list);
            for (Integer integer : list) {
                choices.add(new StationChoice(integer, stations.get(integer)));
            }
            JSheet.showInputSheet(widgets.window, "Choose a station", JOptionPane.INFORMATION_MESSAGE, null, choices.toArray(), null, new SheetListener() {
                public void optionSelected(SheetEvent sheetEvent) {
                    if (sheetEvent.getInputValue() instanceof StationChoice) {
                        StationChoice inputValue = (StationChoice) sheetEvent.getInputValue();
                        pianobarSupport.selectStation(inputValue.key);
                    } else {
                        pianobarSupport.cancelStationSelection();
                    }

                    widgets.window.setEnabled(true);
                }
            });

        }
    }
}
