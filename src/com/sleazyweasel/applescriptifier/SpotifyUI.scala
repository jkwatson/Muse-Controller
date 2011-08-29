package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import layout.TableLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.atomic.AtomicBoolean
import de.felixbruns.jotify.media.{Track, Playlist}
import javax.swing._
import layout.TableLayoutConstants._
import swing.{Button, Label, Swing}
import swing.event.ButtonClicked
import java.awt.{Font, Color, Graphics, Component}
import nl.pascaldevink.jotify.gui.listeners.PlayerListener
import nl.pascaldevink.jotify.gui.listeners.PlayerListener.Status

class SpotifyUI(spotifySupport: NativeSpotifySupport, mainMenuBar: JMenuBar, spotifyMenuItem: JMenuItem, preferences: MuseControllerPreferences) extends PlayerListener with MuseControllerFrame {
  private val executionLock = new AtomicBoolean(false)
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
    initMenuBar()
    initPlaylistComboBox()
    initTrackNameLabel()
    initPlayButton()
    initPauseButton()
    initWindow()
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
      case ButtonClicked(`playButton`) => spotifySupport play()
    }
  }

  def initPauseButton() {
    val pauseButton = new Button
    pauseButton.icon = getIcon("pause.png")
    widgets.pauseButton = pauseButton
    widgets.pauseButton.enabled = false
    widgets.pauseButton.reactions += {
      case ButtonClicked(`pauseButton`) => spotifySupport pause()
    }
  }

  private def initTrackNameLabel() {
    widgets.trackNameLabel = new Label();
    widgets.trackNameLabel.foreground = new Color(51, 51, 51)
    widgets.trackNameLabel.font = Font.decode("Lucida Grande-Bold-14")
  }

  private def initPlaylistComboBox() {
    models.playlistComboBoxModel = new PlaylistComboBoxModel(spotifySupport)

    Swing.onEDT({
      if (acquireLock) {
        models.playlistComboBoxModel.refreshContents()
        releaseLock();
      }
    })

    widgets.playlistComboBox = new JComboBox(models.playlistComboBoxModel)
    widgets.playlistComboBox.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        if (acquireLock) {
          val selected: AnyRef = models.playlistComboBoxModel.getSelectedItem
          selected match {
            case item: Playlist => spotifySupport play item
            case _ => None
          }
          releaseLock()
        }
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
    val tableLayoutConfig = Array(Array(15d, 130d, 15d, 300d, 15d), Array(15d, 30d, 15d, 130d, 15d))
    widgets.window.getContentPane.setLayout(new TableLayout(tableLayoutConfig))
    widgets.window.getContentPane.add(widgets.playlistComboBox, "1, 1, 3, 1, L")

    val leftButtonPanel: JPanel = new JPanel(new TableLayout(Array(Array(PREFERRED, PREFERRED, PREFERRED, PREFERRED, FILL, PREFERRED, PREFERRED), Array(FILL, PREFERRED))))
    leftButtonPanel.add(widgets.playButton.peer, "0,1")
    leftButtonPanel.add(widgets.pauseButton.peer, "1,1")
    //      leftButtonPanel.add(widgets.nextButton, "2,1")
    //      leftButtonPanel.add(widgets.thumbsDownButton, "3,1")
    //      leftButtonPanel.add(widgets.volumeDownButton, "5,1")
    //      leftButtonPanel.add(widgets.volumeUpButton, "6,1")

    val gap = -3
    val infoPanel: JPanel = new JPanel(new TableLayout(Array(Array(130, 15, FILL, 30), Array(20, gap, 20, gap, 20, gap, 20, FILL, 40))))
    //      infoPanel.add(widgets.artistLabel, "0, 0, L, t")
    //      infoPanel.add(widgets.infoLabel, "3, 0, R, c")
    infoPanel.add(widgets.trackNameLabel.peer, "0, 2, 3, 2, L, t")
    //      infoPanel.add(widgets.albumLabel, "0, 4, 3, 4, L, t")
    //      infoPanel.add(widgets.timeLabel, "0, 6, L, t")
    infoPanel.add(leftButtonPanel, "0, 8,3,8 L, b")

    widgets.window.getContentPane.add(infoPanel, "3, 3")
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

  private def acquireLock: Boolean = {
    if (executionLock.get) {
      return false
    }
    executionLock.getAndSet(true)
    true
  }

  private def releaseLock() {
    executionLock.set(false)
  }

  override def playerTrackChanged(track: Track) {
    widgets.trackNameLabel.text = track.getTitle
    widgets.playButton.enabled = false;
    widgets.pauseButton.enabled = true;
  }

  override def playerStatusChanged(status: Status) {
    if (status == Status.PAUSE) {
      widgets.playButton.enabled = true;
      widgets.pauseButton.enabled = false;
    }
    else {
      widgets.playButton.enabled = false;
      widgets.pauseButton.enabled = true;
    }
  }

  override def playerPositionChanged(position: Int) {}

}


object SpotifyUI {

  private class Widgets {
    var window: JFrame = null
    var playlistComboBox: JComboBox = null
    var trackNameLabel: Label = null;
    var playButton: Button = null;
    var pauseButton: Button = null;
  }

  private class Models {
    var playlistComboBoxModel: PlaylistComboBoxModel = null
  }

}