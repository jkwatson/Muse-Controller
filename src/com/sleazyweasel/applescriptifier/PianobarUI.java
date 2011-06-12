package com.sleazyweasel.applescriptifier;

import ch.randelshofer.quaqua.JSheet;
import ch.randelshofer.quaqua.SheetEvent;
import ch.randelshofer.quaqua.SheetListener;
import layout.TableLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class PianobarUI {

    private final AtomicBoolean executionLock = new AtomicBoolean(false);

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
        initStationComboBox();
        initStationNameLabel();
        initArtistLabel();
        initAlbumLabel();
        initSongLabel();
        initTimeLabel();
        initPlayPauseButton();
        initNextButton();
        initThumbsUpButton();
        initThumbsDownButton();
        initChooseStationButton();
        initImageLabel();
        initHeartLabel();
        initInfoLabel();
    }

    private void initWindow() {
        widgets.window = new JFrame("Pandora");
    }

    private void initStationComboBox() {
        models.stationComboBoxModel = new StationComboBoxModel(pianobarSupport);
        widgets.stationComboBox = new JComboBox(models.stationComboBoxModel);
        widgets.stationComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (acquireLock()) {
                    pianobarSupport.askToChooseStation();
                    StationChoice selectedItem = models.stationComboBoxModel.getSelectedStation();
                    pianobarSupport.selectStation(selectedItem.getKey());
                    releaseLock();
                }
            }
        });
        widgets.stationComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    StationChoice stationChoice = (StationChoice) value;
                    setText(stationChoice.getStationName());
                } else {
                    setText("Choose a station");
                }
                return component;
            }
        });
    }

    private void initStationNameLabel() {
        widgets.stationNameLabel = new JLabel();
    }

    private void initArtistLabel() {
        widgets.artistLabel = new JLabel();
        widgets.artistLabel.setForeground(Color.LIGHT_GRAY);
    }

    private void initAlbumLabel() {
        widgets.albumLabel = new JLabel();
        widgets.albumLabel.setFont(Font.decode("Lucida Grande-Bold-14"));
        widgets.albumLabel.setForeground(Color.DARK_GRAY);
    }

    private void initSongLabel() {
        widgets.songLabel = new JLabel();
        widgets.songLabel.setFont(Font.decode("Lucida Grande-Bold-16"));
    }

    private void initTimeLabel() {
        widgets.timeLabel = new JLabel();
        widgets.timeLabel.setForeground(Color.LIGHT_GRAY);
    }

    private void initPlayPauseButton() {
        String name = "playpause.png";
        widgets.playPauseButton = new JButton(getIcon(name));
        setButtonDefaults(widgets.playPauseButton);
        widgets.playPauseButton.setEnabled(false);
        widgets.playPauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.playPause();
            }
        });
    }

    private ImageIcon getIcon(String name) {
        return new ImageIcon(getClass().getClassLoader().getResource(name));
    }

    private void setButtonDefaults(JButton button) {
        button.putClientProperty("JComponent.sizeVariant", "small");
        button.putClientProperty("Quaqua.Button.style", "push");
    }

    private void initNextButton() {
        widgets.nextButton = new JButton(getIcon("nextsong.png"));
        setButtonDefaults(widgets.nextButton);
        widgets.nextButton.setEnabled(false);
        widgets.nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.next();
            }
        });
    }

    private void initThumbsUpButton() {
        widgets.thumbsUpButton = new JButton(getIcon("ThumbsUp.png"));
        setButtonDefaults(widgets.thumbsUpButton);
        widgets.thumbsUpButton.setEnabled(false);
        widgets.thumbsUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.thumbsUp();
            }
        });
    }

    private void initThumbsDownButton() {
        widgets.thumbsDownButton = new JButton(getIcon("ThumbsDown.png"));
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

    private void initImageLabel() {
        widgets.imageLabel = new JLabel();
        widgets.imageLabel.setBorder(BorderFactory.createEtchedBorder());
    }

    private void initHeartLabel() {
        widgets.heartLabel = new JLabel();
        ImageIcon icon = getIcon("heart.png");
        widgets.heartLabel.setIcon(icon);
        widgets.heartLabel.setVisible(false);
    }

    private void initInfoLabel() {
        widgets.infoLabel = new JLabel();
        ImageIcon icon = getIcon("info.png");
        widgets.infoLabel.setIcon(icon);
        widgets.infoLabel.setVisible(false);
        widgets.infoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String detailUrl = pianobarSupport.getState().getDetailUrl();
                if (detailUrl != null && detailUrl.length() > 0) {
                    try {
                        open(new URI(detailUrl));
                    } catch (URISyntaxException e1) {
                        e1.printStackTrace();
                        //do nothing if it doesn't parse?
                    }
                }
            }
        });
    }

    private void initLayout() {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(widgets.playPauseButton);
        buttonPanel.add(widgets.nextButton);
        buttonPanel.add(widgets.thumbsUpButton);
        buttonPanel.add(widgets.thumbsDownButton);

        int border = 2;
        int gap = 2;
        JPanel infoPanel = new JPanel(new TableLayout(new double[][]{
                {25, TableLayout.FILL, border},
                {border, 30, gap, 20, gap, 25, gap, 20, gap, 35, border}
        }));
        infoPanel.add(widgets.infoLabel, "0, 1, L, c");
        infoPanel.add(widgets.songLabel, "1, 1");
        infoPanel.add(widgets.artistLabel, "1, 3");
        infoPanel.add(widgets.albumLabel, "1, 5");
        infoPanel.add(widgets.timeLabel, "1, 7, L");
        infoPanel.add(widgets.heartLabel, "0, 9, L, c");
        infoPanel.add(buttonPanel, "1, 9, L");

        widgets.window.getContentPane().setLayout(new TableLayout(new double[][]{
                {15, 130, 15, 300, 15},
                {15, 30, 15, 130, 20, 10}}
        ));
        widgets.window.getContentPane().add(widgets.stationComboBox, "1, 1, 3, 1");
        widgets.window.getContentPane().add(infoPanel, "3, 3, 3, 4");
        widgets.window.getContentPane().add(widgets.imageLabel, "1, 3");

        widgets.window.pack();
        widgets.window.setResizable(false);
    }

    public JFrame getWindow() {
        return widgets.window;
    }

    public void initialize() {
        pianobarSupport.addListener(new PianobarStateChangeListener());
        pianobarSupport.activatePianoBar();
    }

    private static class Widgets {
        private JComboBox stationComboBox;
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
        private JLabel imageLabel;
        private JLabel timeLabel;
        private JLabel heartLabel;
        private JLabel infoLabel;
    }

    private static class Models {
        private StationComboBoxModel stationComboBoxModel;
    }

    private class PianobarStateChangeListener implements NativePianobarSupport.PianobarStateChangeListener {

        public void stateChanged(final NativePianobarSupport pianobarSupport, PianobarState state) {
            if (acquireLock()) {
                widgets.playPauseButton.setEnabled(!state.isInputRequested());
                widgets.nextButton.setEnabled(!state.isInputRequested());
                widgets.thumbsDownButton.setEnabled(!state.isInputRequested());

                if (state.isInputRequested() && NativePianobarSupport.InputType.CHOOSE_STATION.equals(state.getInputTypeRequested())) {
                    widgets.chooseStationButton.setEnabled(true);
                }
                widgets.stationNameLabel.setText(state.getStation());
                widgets.artistLabel.setText(state.getArtist());
                widgets.albumLabel.setText(state.getAlbum());
                widgets.songLabel.setText(state.getTitle());
                widgets.timeLabel.setText(state.getCurrentTimeInTrack());
                models.stationComboBoxModel.refreshContents();
                models.stationComboBoxModel.setSelectedItem(state.getCurrentStation());

                widgets.thumbsUpButton.setEnabled(!state.isInputRequested() && !state.isCurrentSongIsLoved());

                if (state.isPlaying()) {
                    widgets.playPauseButton.setIcon(getIcon("pause.png"));
                } else {
                    widgets.playPauseButton.setIcon(getIcon("play.png"));
                }

                widgets.heartLabel.setVisible(state.isCurrentSongIsLoved());
                if (state.getDetailUrl() != null && state.getDetailUrl().length() > 0) {
                    widgets.infoLabel.setVisible(true);
                }
                else {
                    widgets.infoLabel.setVisible(false);
                }


                String albumArtUrl = state.getAlbumArtUrl();
                if (widgets.imageLabel.getName() == null || !widgets.imageLabel.getName().equals(albumArtUrl)) {
                    widgets.imageLabel.setName(albumArtUrl);

                    if (albumArtUrl.startsWith("http")) {
                    try {
                        URL imageUrl = new URL(albumArtUrl);
                        widgets.imageLabel.setIcon(new ImageIcon(imageUrl));
                    } catch (MalformedURLException e) {
                        //do nothing, I guess/
                    }
                    } else {
                        widgets.imageLabel.setIcon(null);
                    }
                }


                releaseLock();
            }
        }
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(uri);
            } catch (IOException e) {
                // TODO: error handling
            }
        } else {
            // TODO: error handling
        }
    }

    private class ChooseStationAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            pianobarSupport.askToChooseStation();
            PianobarState state = pianobarSupport.getState();
            java.util.List<StationChoice> stationChoices = state.getStationChoices();
            JSheet.showInputSheet(widgets.window, "Choose a station", JOptionPane.INFORMATION_MESSAGE, null, stationChoices.toArray(), null, new SheetListener() {
                public void optionSelected(SheetEvent sheetEvent) {
                    if (sheetEvent.getInputValue() instanceof StationChoice) {
                        StationChoice inputValue = (StationChoice) sheetEvent.getInputValue();
                        pianobarSupport.selectStation(inputValue.getKey());
                    } else {
                        pianobarSupport.cancelStationSelection();
                    }

                    widgets.window.setEnabled(true);
                }
            });

        }
    }

    private boolean acquireLock() {
        if (executionLock.get()) {
            return false;
        }
        executionLock.getAndSet(true);
        return true;
    }

    private void releaseLock() {
        executionLock.set(false);
    }
}