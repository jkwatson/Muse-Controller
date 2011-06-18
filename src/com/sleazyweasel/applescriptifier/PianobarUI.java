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

import static layout.TableLayoutConstants.FILL;
import static layout.TableLayoutConstants.PREFERRED;

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
        initVolumeUpButton();
        initVolumeDownButton();
        initChooseStationButton();
        initImageLabel();
        initHeartLabel();
        initInfoLabel();
    }

    private void initWindow() {
        widgets.window = new JFrame("Pandora");
        widgets.window.setContentPane(new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
//                ((TableLayout) getLayout()).drawGrid(this, g);
            }
        });
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
        widgets.stationNameLabel.setForeground(new Color(102, 102, 102));
        widgets.stationNameLabel.setFont(Font.decode("Lucida Grande-Bold-14"));
    }

    private void initArtistLabel() {
        widgets.artistLabel = new JLabel();
        widgets.artistLabel.setForeground(new Color(102, 102, 102));
        widgets.artistLabel.setFont(Font.decode("Lucida Grande-Bold-14"));
    }

    private void initAlbumLabel() {
        widgets.albumLabel = new JLabel();
        widgets.albumLabel.setForeground(new Color(102, 102, 102));
        widgets.albumLabel.setFont(Font.decode("Lucida Grande-Bold-14"));
    }

    private void initSongLabel() {
        widgets.songLabel = new JLabel();
        widgets.songLabel.setForeground(new Color(51, 51, 51));
        widgets.songLabel.setFont(Font.decode("Lucida Grande-Bold-14"));
    }

    private void initTimeLabel() {
        widgets.timeLabel = new JLabel();
        widgets.timeLabel.setFont(Font.decode("Lucida Grande-Bold-12"));
        widgets.timeLabel.setForeground(new Color(102, 102, 102));
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

    private void initVolumeUpButton() {
        widgets.volumeUpButton = new JButton(getIcon("volume_up.png"));
        setButtonDefaults(widgets.volumeUpButton);
        widgets.volumeUpButton.setEnabled(false);
        widgets.volumeUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.volumeUp();
            }
        });
    }

    private void initVolumeDownButton() {
        widgets.volumeDownButton = new JButton(getIcon("volume_down.png"));
        setButtonDefaults(widgets.volumeDownButton);
        widgets.volumeDownButton.setEnabled(false);
        widgets.volumeDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pianobarSupport.volumeDown();
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
//        widgets.imageLabel.setBorder(BorderFactory.createEtchedBorder());
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
        JPanel leftButtonPanel = new JPanel(new TableLayout(new double[][] {
                {PREFERRED,PREFERRED,PREFERRED,PREFERRED,FILL,PREFERRED,PREFERRED},{FILL, PREFERRED}
        }));
        leftButtonPanel.add(widgets.playPauseButton, "0,1");
        leftButtonPanel.add(widgets.nextButton, "1,1");
        leftButtonPanel.add(widgets.thumbsUpButton, "2,1");
        leftButtonPanel.add(widgets.thumbsDownButton, "3,1");
        leftButtonPanel.add(widgets.volumeDownButton, "5,1");
        leftButtonPanel.add(widgets.volumeUpButton, "6,1");

        int gap = -3;
        JPanel infoPanel = new JPanel(new TableLayout(new double[][]{
                {130, 15, TableLayout.FILL, 30},
                {20, gap, 20, gap, 20, gap, 20, TableLayout.FILL, 40}
        }));
        infoPanel.add(widgets.artistLabel, "0, 0, L, t");
        infoPanel.add(widgets.infoLabel, "3, 0, R, c");
        infoPanel.add(widgets.songLabel, "0, 2, 3, 2, L, t");
        infoPanel.add(widgets.albumLabel, "0, 4, 3, 4, L, t");
        infoPanel.add(widgets.timeLabel, "0, 6, L, t");
//        infoPanel.add(widgets.heartLabel, "0, 9, L, c");
        infoPanel.add(leftButtonPanel, "0, 8,3,8 L, b");

        widgets.window.getContentPane().setLayout(new TableLayout(new double[][]{
                {15, 130, 15, 300, 15},
                {15, 30, 15, 130, 15}}
        ));
        widgets.window.getContentPane().add(widgets.stationComboBox, "1, 1, 3, 1, L");
        widgets.window.getContentPane().add(infoPanel, "3, 3");
        widgets.window.getContentPane().add(widgets.imageLabel, "1, 3, L, c");

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
        private JButton volumeUpButton;
        private JButton volumeDownButton;
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
                widgets.volumeUpButton.setEnabled(!state.isInputRequested());
                widgets.volumeDownButton.setEnabled(!state.isInputRequested());

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
