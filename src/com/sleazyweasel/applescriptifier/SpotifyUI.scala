package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import de.felixbruns.jotify.media.Playlist
import layout.TableLayout
import javax.swing._
import java.awt._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.atomic.AtomicBoolean
import swing.Swing

class SpotifyUI(spotifySupport: NativeSpotifySupport, mainMenuBar: JMenuBar, spotifyMenuItem: JMenuItem, preferences: MuseControllerPreferences) extends MuseControllerFrame {
  private val executionLock = new AtomicBoolean(false)
  private val widgets = new SpotifyUI.Widgets
  private val models = new SpotifyUI.Models
  initUserInterface()
  preferences.setSpotifyAsStreamer()

  private def initUserInterface() {
    initWidgetsAndModels()
    initLayout()
  }

  private def initWidgetsAndModels() {
    initMenuBar()
    initPlaylistComboBox()
    initWindow()
  }

  private def initPlaylistComboBox() {
    models.playlistComboBoxModel = new PlaylistComboBoxModel(spotifySupport)

    Swing.onEDT({
      models.playlistComboBoxModel.refreshContents()
    })

    widgets.playlistComboBox = new JComboBox(models.playlistComboBoxModel)
    widgets.playlistComboBox.addActionListener(new ActionListener {
      def actionPerformed(e: ActionEvent) {
        if (acquireLock) {
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
    widgets.window.pack()
    widgets.window.setResizable(false)
  }

  def getWindow: JFrame = {
    widgets.window
  }

  def close() {
    spotifySupport.close()
    spotifyMenuItem.setEnabled(true)
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

}


object SpotifyUI {

  private class Widgets {
    var window: JFrame = null
    var playlistComboBox: JComboBox = null
  }

  private class Models {
    var playlistComboBoxModel: PlaylistComboBoxModel = null
  }

}