package com.sleazyweasel.applescriptifier;

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
import layout.TableLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static layout.TableLayoutConstants.FILL;
import static layout.TableLayoutConstants.PREFERRED;

public class PandoraUI implements MuseControllerFrame {
    private static final Logger logger = Logger.getLogger(PandoraUI.class.getName());
    private final AtomicBoolean executionLock = new AtomicBoolean(false);

    private final Widgets widgets = new Widgets();
    private final Models models = new Models();

    private final MusicPlayer musicPlayer;
    private final JMenuBar mainMenuBar;
    private final JMenuItem pandoraMenuItem;
    private final MuseControllerPreferences preferences;

    public PandoraUI(MusicPlayer musicPlayer, JMenuBar mainMenuBar, JMenuItem pandoraMenuItem, MuseControllerPreferences preferences) {
        this.musicPlayer = musicPlayer;
        this.mainMenuBar = mainMenuBar;
        this.pandoraMenuItem = pandoraMenuItem;
        this.preferences = preferences;
        initUserInterface();
        preferences.setPandoraAsStreamer();
    }

    private void initUserInterface() {
        initWidgetsAndModels();
        initLayout();
    }

    private void initWidgetsAndModels() {
        initMenuBar();
        initWindow();
        initStationComboBox();
        initStationNameLabel();
        initArtistLabel();
        initAlbumLabel();
        initSongLabel();
        initTimeLabel();
        initDurationLabel();
        initTimeProgressIndicator();
        initPlayPauseButton();
        initNextButton();
        initThumbsUpButton();
        initThumbsDownButton();
        initSleepButton();
        initVolumeUpButton();
        initVolumeDownButton();
        initVolumeSlider();
        initImageLabel();
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

    private void initMenuBar() {
        widgets.menu = new JMenu("Control");
        widgets.menu.setMnemonic('c');
        widgets.menu.add(initPlayPauseMenuItem());
        widgets.menu.add(initNextMenuItem());
        widgets.menu.addSeparator();
        widgets.menu.add(initThumbsUpMenuItem());
        widgets.menu.add(initThumbsDownMenuItem());
        widgets.menu.add(initSleepMenuItem());
        widgets.menu.addSeparator();
        widgets.menu.add(initVolumeUpMenuItem());
        widgets.menu.add(initVolumeDownMenuItem());
        widgets.menu.addSeparator();
        widgets.menu.add(initRestartPandoraMenuItem());

        mainMenuBar.add(widgets.menu);
    }

    private JMenuItem initVolumeDownMenuItem() {
        JMenuItem menuItem = new JMenuItem("Volume Down");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.volumeDown();
            }
        });
        menuItem.setEnabled(false);
        widgets.volumeDownMenuItem = menuItem;
        return menuItem;
    }

    private JMenuItem initVolumeUpMenuItem() {
        JMenuItem menuItem = new JMenuItem("Volume Up");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.volumeUp();
            }
        });
        menuItem.setEnabled(false);
        widgets.volumeUpMenuItem = menuItem;
        return menuItem;
    }

    private JMenuItem initThumbsDownMenuItem() {
        JMenuItem menuItem = new JMenuItem("Thumbs Down");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.thumbsDown();
            }
        });
        menuItem.setEnabled(false);
        widgets.thumbsDownMenuItem = menuItem;
        return menuItem;
    }

    private JMenuItem initThumbsUpMenuItem() {
        JMenuItem menuItem = new JMenuItem("Thumbs Up");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.thumbsUp();
            }
        });
        menuItem.setEnabled(false);
        widgets.thumbsUpMenuItem = menuItem;
        return menuItem;
    }

    private JMenuItem initSleepMenuItem() {
        JMenuItem menuItem = new JMenuItem("Mark as Tired");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.sleep();
            }
        });
        menuItem.setEnabled(false);
        widgets.sleepMenuItem = menuItem;
        return menuItem;
    }

    private JMenuItem initNextMenuItem() {
        JMenuItem menuItem = new JMenuItem("Next");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.next();
            }
        });
        menuItem.setEnabled(false);
        widgets.nextMenuItem = menuItem;
        return menuItem;
    }

    private JMenuItem initPlayPauseMenuItem() {
        JMenuItem menuItem = new JMenuItem("Play/Pause");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.playPause();
            }
        });
        menuItem.setEnabled(false);
        widgets.playPauseMenuItem = menuItem;
        return menuItem;
    }

    private JMenuItem initRestartPandoraMenuItem() {
        JMenuItem restartMenuItem = new JMenuItem("Restart Pandora");
        restartMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.bounce();
            }
        });
        return restartMenuItem;
    }

    private void initStationComboBox() {
        models.stationComboBoxModel = new StationComboBoxModel(musicPlayer);
        widgets.stationComboBox = new JComboBox(models.stationComboBoxModel);
        widgets.stationComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (acquireLock()) {
                    musicPlayer.askToChooseStation();
                    StationChoice selectedItem = models.stationComboBoxModel.getSelectedStation();
                    if (selectedItem != null) {
                        musicPlayer.selectStation(selectedItem.key());
                    }
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
                    setText(stationChoice.stationName());
                } else {
                    setText("Choose a station");
                }
                return component;
            }
        });
        models.stationComboBoxModel.setSelectedItem(null);
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

    private void initDurationLabel() {
        widgets.durationLabel = new JLabel();
        widgets.durationLabel.setFont(Font.decode("Lucida Grande-Bold-12"));
        widgets.durationLabel.setForeground(new Color(102, 102, 102));
    }

    private void initTimeProgressIndicator() {
        widgets.timeProgressBar = new JProgressBar(0, 10000);
    }

    private void initPlayPauseButton() {
        widgets.playPauseButton = new JButton(getIcon("playpause.png"));
        setButtonDefaults(widgets.playPauseButton);
        widgets.playPauseButton.setEnabled(false);
        widgets.playPauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.playPause();
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
                musicPlayer.next();
            }
        });
    }

    private void initThumbsUpButton() {
        widgets.thumbsUpButton = new JButton(getIcon("ThumbsUp.png"));
        setButtonDefaults(widgets.thumbsUpButton);
        widgets.thumbsUpButton.setEnabled(false);
        widgets.thumbsUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.thumbsUp();
            }
        });
    }

    private void initThumbsDownButton() {
        widgets.thumbsDownButton = new JButton(getIcon("ThumbsDown.png"));
        setButtonDefaults(widgets.thumbsDownButton);
        widgets.thumbsDownButton.setEnabled(false);
        widgets.thumbsDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.thumbsDown();
            }
        });
    }

    private void initSleepButton() {
        widgets.sleepButton = new JButton(getIcon("Sleep.png"));
        setButtonDefaults(widgets.sleepButton);
        widgets.sleepButton.setToolTipText("Mark Song as Tired");
        widgets.sleepButton.setEnabled(false);
        widgets.sleepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.sleep();
            }
        });
    }

    private void initVolumeUpButton() {
        widgets.volumeUpButton = new JButton(getIcon("volume_up.png"));
        setButtonDefaults(widgets.volumeUpButton);
        widgets.volumeUpButton.setEnabled(false);
        widgets.volumeUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.volumeUp();
            }
        });
    }

    private void initVolumeDownButton() {
        widgets.volumeDownButton = new JButton(getIcon("volume_down.png"));
        setButtonDefaults(widgets.volumeDownButton);
        widgets.volumeDownButton.setEnabled(false);
        widgets.volumeDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                musicPlayer.volumeDown();
            }
        });
    }

    private void initVolumeSlider() {
        widgets.volumeSlider = new JSlider();
        widgets.volumeSlider.setEnabled(false);
        widgets.volumeSlider.setMinimum(0);
        widgets.volumeSlider.setMaximum(100);
        widgets.volumeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                musicPlayer.setVolume(getSliderValue());
            }
        });
    }

    private double getSliderValue() {
        return widgets.volumeSlider.getValue() / 100d;
    }

    private void initImageLabel() {
        widgets.imageLabel = new JLabel();
//        widgets.imageLabel.setBorder(BorderFactory.createEtchedBorder());
    }

    private void initInfoLabel() {
        widgets.infoLabel = new JLabel();
        ImageIcon icon = getIcon("info.png");
        widgets.infoLabel.setIcon(icon);
        widgets.infoLabel.setVisible(false);
        widgets.infoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String detailUrl = musicPlayer.getState().getDetailUrl();
                if (detailUrl != null && detailUrl.length() > 0) {
                    try {
                        open(new URI(detailUrl));
                    } catch (URISyntaxException e1) {
                        logger.log(Level.WARNING, "Exception caught:", e1);
                        //do nothing if it doesn't parse?
                    }
                }
            }
        });
    }

    private void initLayout() {
        JPanel leftButtonPanel = new JPanel(new TableLayout(new double[][]{
                {FILL, PREFERRED, PREFERRED, PREFERRED, PREFERRED, PREFERRED, FILL, PREFERRED, PREFERRED}, // horizontal
                {FILL, PREFERRED} // vertical
        }));
        leftButtonPanel.add(widgets.playPauseButton, "1,1");
        leftButtonPanel.add(widgets.nextButton, "2,1");
        leftButtonPanel.add(widgets.thumbsUpButton, "3,1");
        leftButtonPanel.add(widgets.thumbsDownButton, "4,1");
        leftButtonPanel.add(widgets.sleepButton, "5,1");

        int gap = -3;
        JPanel infoPanel = new JPanel(new TableLayout(new double[][]{
                {35, 25, FILL, 15, FILL, 10, 35}, // horizontal
                {20, gap, 20, gap, 20, 8} // vertical
        }));
        infoPanel.add(widgets.artistLabel, "0, 0, 6, 0, L, t");
        infoPanel.add(widgets.infoLabel, "6, 0, R, c");
        infoPanel.add(widgets.songLabel, "0, 2, 6, 2, L, t");
        infoPanel.add(widgets.albumLabel, "0, 4, 6, 4, L, t");

        widgets.window.getContentPane().setLayout(new TableLayout(new double[][]{
                {15, 150, 15, 35, 5, FILL, 5, 35, 15}, // horizontal
                {15, 30, 10, 70, 1, 12, 5, 32, 2, 32, 15}}  // vertical
        ));
        widgets.window.getContentPane().add(widgets.stationComboBox, "1, 1, 7, 1, L");
        widgets.window.getContentPane().add(infoPanel, "3, 3, 7, 3");
        widgets.window.getContentPane().add(widgets.imageLabel, "1, 3, 1, 9, L, c");


        JPanel progressPanel = new JPanel(new TableLayout(new double[][]{
                {FILL}, //horizontal
                {4, 4, 4}  //vertical
        }));
        progressPanel.add(widgets.timeProgressBar, "0, 1");

        widgets.window.getContentPane().add(widgets.timeLabel, "3, 5, c, c");
        widgets.window.getContentPane().add(progressPanel, "5, 5");
        widgets.window.getContentPane().add(widgets.durationLabel, "7, 5, c, c");
        widgets.window.getContentPane().add(leftButtonPanel, "3, 7, 7, 7");
        widgets.window.getContentPane().add(widgets.volumeDownButton, "3, 9");
        widgets.window.getContentPane().add(widgets.volumeSlider, "5, 9");
        widgets.window.getContentPane().add(widgets.volumeUpButton, "7, 9");

        widgets.window.pack();
        widgets.window.setResizable(false);
    }

    public JFrame getWindow() {
        return widgets.window;
    }

    public void initialize() {
        musicPlayer.addListener(new MusicPlayerStateChangeListener());
        musicPlayer.activate();
        musicPlayer.initializeFromSavedUserState(preferences);
        initWidgetStateFromPlayer();
    }

    private void initWidgetStateFromPlayer() {
        double volume = musicPlayer.getState().getVolume();
        widgets.volumeSlider.setValue((int) (volume * 100));
    }

    public void close() {
        musicPlayer.close();
        mainMenuBar.remove(widgets.menu);
        pandoraMenuItem.setEnabled(true);
        widgets.window.dispose();
    }

    private static class Widgets {
        private JComboBox stationComboBox;
        private JFrame window;
        private JButton playPauseButton;
        private JButton nextButton;
        private JButton thumbsUpButton;
        private JButton thumbsDownButton;
        private JButton sleepButton;
        private JButton volumeUpButton;
        private JButton volumeDownButton;
        private JSlider volumeSlider;
        private JLabel stationNameLabel;
        private JLabel artistLabel;
        private JLabel albumLabel;
        private JLabel songLabel;
        private JLabel imageLabel;
        private JLabel timeLabel;
        private JLabel durationLabel;
        private JLabel infoLabel;
        private JMenuItem volumeDownMenuItem;
        private JMenuItem volumeUpMenuItem;
        private JMenuItem thumbsDownMenuItem;
        private JMenuItem thumbsUpMenuItem;
        private JMenuItem sleepMenuItem;
        private JMenuItem nextMenuItem;
        private JMenuItem playPauseMenuItem;
        private JMenu menu;
        private JProgressBar timeProgressBar;
    }

    private static class Models {
        private StationComboBoxModel stationComboBoxModel;
    }

    private class MusicPlayerStateChangeListener implements MusicPlayer.MusicPlayerStateChangeListener {

        public void stateChanged(final MusicPlayer player, MusicPlayerState state) {
            if (acquireLock()) {
                widgets.playPauseButton.setEnabled(!state.isInputRequested());
                widgets.nextButton.setEnabled(trackCanBeModified(state));
                widgets.thumbsDownButton.setEnabled(trackCanBeModified(state));
                widgets.volumeUpButton.setEnabled(!state.isInputRequested());
                widgets.volumeDownButton.setEnabled(!state.isInputRequested());
                widgets.volumeDownMenuItem.setEnabled(!state.isInputRequested());
                widgets.volumeUpMenuItem.setEnabled(!state.isInputRequested());
                widgets.volumeSlider.setEnabled(!state.isInputRequested());
                widgets.volumeSlider.setValue((int) (state.getVolume() * 100));
                widgets.thumbsDownMenuItem.setEnabled(trackCanBeModified(state));
                widgets.thumbsUpMenuItem.setEnabled(trackCanBeModified(state));
                widgets.sleepMenuItem.setEnabled(trackCanBeModified(state));
                widgets.sleepButton.setEnabled(trackCanBeModified(state));
                widgets.nextMenuItem.setEnabled(trackCanBeModified(state));
                widgets.playPauseMenuItem.setEnabled(!state.isInputRequested());

                widgets.stationNameLabel.setText(state.getStation());
                widgets.artistLabel.setText(state.getArtist());
                widgets.albumLabel.setText(state.getAlbum());
                widgets.songLabel.setText(state.getTitle());
                widgets.timeLabel.setText(formatTime(state.getCurrentTime()));
                if (state.getCurrentTime() == 0 || state.getDuration() == 0) {
                    widgets.timeProgressBar.setValue(0);
                } else {
                    widgets.timeProgressBar.setValue((int) (((float) state.getCurrentTime() / (float) state.getDuration()) * 10000f));
                }
                widgets.durationLabel.setText(formatTime(state.getDuration()));
                models.stationComboBoxModel.refreshContents();
                models.stationComboBoxModel.setSelectedItem(state.getCurrentStation());

                widgets.thumbsUpButton.setEnabled(trackCanBeModified(state) && !state.isCurrentSongIsLoved());

                if (state.isPlaying()) {
                    widgets.playPauseButton.setIcon(getIcon("pause.png"));
                } else {
                    widgets.playPauseButton.setIcon(getIcon("play.png"));
                }

                if (state.getDetailUrl() != null && state.getDetailUrl().length() > 0) {
                    widgets.infoLabel.setVisible(true);
                } else {
                    widgets.infoLabel.setVisible(false);
                }

                String albumArtUrl = state.getAlbumArtUrl();
                if (widgets.imageLabel.getName() == null || !widgets.imageLabel.getName().equals(albumArtUrl)) {
                    widgets.imageLabel.setName(albumArtUrl);

                    if (albumArtUrl.startsWith("http")) {
                        setAlbumImage(albumArtUrl);
                    } else {
                        widgets.imageLabel.setIcon(null);
                    }
                }


                releaseLock();
            }
        }

        private boolean trackCanBeModified(MusicPlayerState state) {
            return !state.isInputRequested() && state.getTitle() != null && !state.getTitle().isEmpty();
        }
    }

    private String formatTime(int currentTime) {
        if (currentTime == 0) {
            return "";
        }
        int minutes = currentTime / 60;
        int seconds = currentTime % 60;
        String secondsString = String.valueOf(seconds);
        if (secondsString.length() == 1) {
            secondsString = "0" + secondsString;
        }
        return minutes + ":" + secondsString;
    }

    private void setAlbumImage(final String albumArtUrl) {
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                ImageIcon icon = null;
                try {
                    URL imageUrl = new URL(albumArtUrl);
                    icon = new ImageIcon(imageUrl);
                    Image scaledImage = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    icon.setImage(scaledImage);
                } catch (MalformedURLException e) {
                    logger.log(Level.WARNING, "Exception caught.", e);
                }
                return icon;
            }

            @Override
            protected void done() {
                try {
                    widgets.imageLabel.setIcon((Icon) get());
                } catch (InterruptedException e) {
                    //make sure that anyone else gets notified that something should be done.
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    logger.log(Level.WARNING, "Exception caught.", e);
                }
            }
        }.execute();
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
