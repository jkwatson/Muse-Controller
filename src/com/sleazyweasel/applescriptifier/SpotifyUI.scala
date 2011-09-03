package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import layout.TableLayout
import de.felixbruns.jotify.media.{Track, Playlist}
import scala.swing.event.{ValueChanged, ButtonClicked}
import layout.TableLayoutConstants._
import nl.pascaldevink.jotify.gui.listeners.PlayerListener
import nl.pascaldevink.jotify.gui.listeners.PlayerListener.Status
import java.awt.event._
import java.io.IOException
import java.net.URI
import swing._
import java.awt.{Component, Graphics, Cursor, Desktop, Font, Image}
import javax.swing._
import java.util.concurrent.Semaphore

class SpotifyUI(spotifySupport: NativeSpotifySupport, mainMenuBar: JMenuBar, spotifyMenuItem: JMenuItem, preferences: MuseControllerPreferences) extends PlayerListener with MuseControllerFrame {
  private val executionLock = new Semaphore(1)
  private val widgets = new SpotifyUI.Widgets
  private val models = new SpotifyUI.Models
  initUserInterface()
  preferences.setSpotifyAsStreamer()
  spotifySupport setListener this

  private def initUserInterface() {
    initWidgetsAndModels()
    initLayout()
  }

  private def initWidgetsAndModels() {
    initWindow()
    initMenuBar()
    initPlaylistComboBox()
    initTrackNameLabel()
    initArtistLabel()
    initAlbumLabel()
    initInfoLabel()
    initImageLabel()
    initPlayButton()
    initNextButton()
    initPreviousButton()
    initPauseButton()
    initVolumeSlider()
    initRefreshPlaylistsButton()
  }

  private def getIcon(name: String): ImageIcon = {
    new ImageIcon(getClass.getClassLoader.getResource(name))
  }

  def initPlayButton() {
    val playButton = new Button
    playButton.icon = getIcon("play.png")
    widgets.playButton = playButton
    widgets.playButton.enabled = false
    widgets.playButton.reactions += {
      case ButtonClicked(`playButton`) => doWithLock {
        () =>
          spotifySupport play()
      }
    }
  }

  def initNextButton() {
    val nextButton = new Button
    nextButton.icon = getIcon("nextsong.png")
    widgets.nextButton = nextButton
    widgets.nextButton.enabled = false
    widgets.nextButton.reactions += {
      case ButtonClicked(`nextButton`) => doWithLock {
        () =>
          spotifySupport nextTrack()
      }
    }
  }

  def initVolumeSlider() {
    val volumeSlider = new Slider
    volumeSlider.min = 0
    volumeSlider.max = 100
    volumeSlider.value = 100
    volumeSlider.reactions += {
      case ValueChanged(`volumeSlider`) => {
        doWithLock(() => {
          val volume: Float = volumeSlider.value
          spotifySupport setVolume (volume / 100f)
          if (volume > 0 && volume < 0.5) {
            //todo modify icon next to volume slider...
          }
        })
      }
    }
    widgets.volumeSlider = volumeSlider
    widgets.volumeSlider.enabled = true
  }

  def initRefreshPlaylistsButton() {
    val refreshPlaylistsButton = new Button
    refreshPlaylistsButton.icon = getIcon("refresh.png")
    refreshPlaylistsButton.reactions += {
      case ButtonClicked(`refreshPlaylistsButton`) =>
        doWithLock(() => {
          println("refreshing combo from button")
          models.playlistComboBoxModel.refreshContents()
        })
    }
    widgets.refreshPlaylistsButton = refreshPlaylistsButton
  }

  def doWithLock(action: () => Unit) {
    if (executionLock.tryAcquire()) {
      widgets.window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR))
      try {
        action()
      }
      finally {
        widgets.window.setCursor(Cursor.getDefaultCursor)
        executionLock.release()
      }
    }
  }

  def initPreviousButton() {
    val previousButton = new Button
    previousButton.icon = getIcon("previoussong.png")
    widgets.previousButton = previousButton
    widgets.previousButton.enabled = false
    widgets.previousButton.reactions += {
      case ButtonClicked(`previousButton`) => doWithLock {
        () => spotifySupport previousTrack()
      }
    }
  }

  def initPauseButton() {
    val pauseButton = new Button
    pauseButton.icon = getIcon("pause.png")
    widgets.pauseButton = pauseButton
    widgets.pauseButton.enabled = false
    widgets.pauseButton.reactions += {
      case ButtonClicked(`pauseButton`) => doWithLock {
        () => spotifySupport pause()
      }
    }
  }

  private def initTrackNameLabel() {
    widgets.trackNameLabel = new Label("", null, Alignment.Left)
    widgets.trackNameLabel.foreground = new Color(51, 51, 51)
    widgets.trackNameLabel.font = Font.decode("Lucida Grande-Bold-14")
  }

  private def initArtistLabel() {
    widgets.artistLabel = new Label("", null, Alignment.Left)
    widgets.artistLabel.foreground = new Color(102, 102, 102)
    widgets.artistLabel.font = Font.decode("Lucida Grande-Bold-14")
  }

  private def initAlbumLabel() {
    widgets.albumLabel = new Label("", null, Alignment.Left)
    widgets.albumLabel.foreground = new Color(102, 102, 102)
    widgets.albumLabel.font = Font.decode("Lucida Grande-Bold-14")
  }

  private def initImageLabel() {
    widgets.imageLabel = new Label()
  }

  private def open(uri: URI) {
    if (Desktop.isDesktopSupported) {
      val desktop = Desktop.getDesktop
      try {
        desktop.browse(uri)
      }
      catch {
        case e: IOException => {
          //todo error handling
        }
      }
    }
    else {
      //todo error handling
    }
  }

  private def initInfoLabel() {
    widgets.infoLabel = new Label
    val icon: ImageIcon = getIcon("info.png")
    widgets.infoLabel.icon = icon
    widgets.infoLabel.visible = false
    widgets.infoLabel.peer.addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) {
        val detailUrl = widgets.infoLabel.peer.getClientProperty("URL")
        detailUrl match {
          case url: String => {
            if ((url length) > 0) {
              open(new URI(url))
            }
          }
          case _ => None
        }
      }
    })
  }

  private def initPlaylistComboBox() {
    models.playlistComboBoxModel = new PlaylistComboBoxModel(spotifySupport)

    Swing.onEDT({
      doWithLock({
        () =>
          models.playlistComboBoxModel.refreshContents()
      })
    })

    widgets.playlistComboBox = new JComboBox(models.playlistComboBoxModel)
    widgets.playlistComboBox.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        doWithLock({
          () =>
            val selected: AnyRef = models.playlistComboBoxModel.getSelectedItem
            selected match {
              case item: Playlist => spotifySupport play item
              case _ => None
            }
        })
      }
    })

    widgets.playlistComboBox.setRenderer(new DefaultListCellRenderer {
      override def getListCellRendererComponent(list: JList, value: AnyRef, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component = {
        val component: Component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        if (value != null) {
          val playlist: Playlist = value.asInstanceOf[Playlist]
          setText(playlist.getName)
        }
        else {
          setText("Choose a playlist")
        }
        component
      }
    })
  }

  private def initWindow() {
    widgets.window = new JFrame("Spotify")
    widgets.window.setContentPane(new JPanel {
      override def paint(g: Graphics) {
        super.paint(g)
        //        val layout = super.getLayout
        //        layout match {
        //          case item: TableLayout => item drawGrid(this, g)
        //          case _ => None
        //        }
      }
    })
    widgets.window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    widgets.window.addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent) {
        spotifySupport.close()
        spotifyMenuItem.setEnabled(true)
      }
    })
  }

  private def initMenuBar() {
  }

  private def initLayout() {
    val tableLayoutConfig = Array(Array(15d, 130d, 15d, 300d, 28d, 15d), Array(15d, 30d, 15d, 130d, 15d))
    widgets.window.getContentPane.setLayout(new TableLayout(tableLayoutConfig))
    widgets.window.getContentPane.add(widgets.playlistComboBox, "1, 1, 3, 1, L")
    widgets.window.getContentPane.add(widgets.refreshPlaylistsButton.peer, "4,1")

    val leftButtonPanel = new JPanel(new TableLayout(Array(Array(PREFERRED, PREFERRED, PREFERRED, PREFERRED, 2d, PREFERRED, FILL, PREFERRED), Array(FILL, PREFERRED))))
    leftButtonPanel.add(widgets.previousButton.peer, "0,1")
    leftButtonPanel.add(widgets.playButton.peer, "1,1")
    leftButtonPanel.add(widgets.pauseButton.peer, "2,1")
    leftButtonPanel.add(widgets.nextButton.peer, "3,1")
    //      leftButtonPanel.add(widgets.volumeDownButton, "5,1")
    //      leftButtonPanel.add(widgets.volumeUpButton, "6,1")
    leftButtonPanel.add(new JLabel(getIcon("volume_icon.png")), "5,1,R")
    leftButtonPanel.add(widgets.volumeSlider.peer, "6,1,L")

    val gap = -3
    val infoPanel = new JPanel(new TableLayout(Array(
      Array(130, 15, FILL, 30),
      Array(20, gap, 20, gap, 20, gap, 20, FILL, 40))))
    infoPanel.add(widgets.artistLabel.peer, "0, 0, 3, 0, L, t")
    //    infoPanel.add(widgets.infoLabel.peer, "3, 0, R, c")
    infoPanel.add(widgets.trackNameLabel.peer, "0, 2, 3, 2, L, t")
    infoPanel.add(widgets.albumLabel.peer, "0, 4, 3, 4, L, t")
    //      infoPanel.add(widgets.timeLabel, "0, 6, L, t")
    infoPanel.add(leftButtonPanel, "0, 8, 3, 8, L, b")

    widgets.window.getContentPane.add(infoPanel, "3, 3")
    widgets.window.getContentPane.add(widgets.imageLabel.peer, "1, 3, L, c")

    widgets.window.pack()
    widgets.window.setResizable(false)
  }

  def getWindow: JFrame = {
    widgets.window
  }

  def close() {
    spotifySupport.close()
    spotifyMenuItem.setEnabled(true)
    spotifySupport.setListener(null);
    widgets.window.dispose()
  }

  override def playerTrackChanged(track: Track) {
    widgets.trackNameLabel.text = track.getTitle
    widgets.albumLabel.text = track.getAlbum.getName
    widgets.artistLabel.text = track.getArtist.getName
    widgets.infoLabel.peer.putClientProperty("URL", track.getLink.asString())
    widgets.infoLabel.visible = true
    widgets.playButton.enabled = false;
    widgets.pauseButton.enabled = true;
    widgets.nextButton.enabled = true;
    widgets.previousButton.enabled = true;
    widgets.imageLabel.icon = new ImageIcon(spotifySupport.image(track.getCover).getScaledInstance(130, 130, Image.SCALE_SMOOTH));
  }

  override def playerStatusChanged(status: Status) {
    if (status == Status.PAUSE) {
      widgets.playButton.enabled = true;
      widgets.pauseButton.enabled = false;
      widgets.nextButton.enabled = true;
    }
    else {
      widgets.playButton.enabled = false;
      widgets.pauseButton.enabled = true;
      widgets.nextButton.enabled = true;
    }
  }

  override def playerPositionChanged(position: Int) {}

}


object SpotifyUI {

  private class Widgets {
    var window: JFrame = null
    var playlistComboBox: JComboBox = null
    var trackNameLabel: Label = null;
    var artistLabel: Label = null;
    var albumLabel: Label = null;
    var playButton: Button = null;
    var nextButton: Button = null;
    var previousButton: Button = null;
    var pauseButton: Button = null;
    var infoLabel: Label = null;
    var imageLabel: Label = null;
    var volumeSlider: Slider = null;
    var refreshPlaylistsButton: Button = null;
  }

  private class Models {
    var playlistComboBoxModel: PlaylistComboBoxModel = null
  }

}