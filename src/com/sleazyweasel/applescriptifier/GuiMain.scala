package com.sleazyweasel.applescriptifier

import com.apple.eawt.PreferencesHandler
import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import javax.swing._
import com.apple.eawt.AppEvent.PreferencesEvent
import scala.swing.event.ButtonClicked
import swing._

class GuiMain extends MuseControllerMain {
  private var activeFrame: MuseControllerFrame = null

  def setActiveFrame(frame: MuseControllerFrame) {
    if (activeFrame != null && (frame ne activeFrame)) {
      activeFrame.close()
    }
    activeFrame = frame
  }

  private def setUpMacApplicationState(preferences: MuseControllerPreferences, menubar: MenuBar) {
    val macApp = com.apple.eawt.Application.getApplication
    macApp.setPreferencesHandler(new PreferencesHandler {
      def handlePreferences(preferencesEvent: PreferencesEvent) {
        val preferencesGui: PreferencesGui = new PreferencesGui(preferences)
        val preferencesFrame: JFrame = preferencesGui.getWindow
        preferencesFrame.setLocationRelativeTo(null)
        preferencesFrame.setVisible(true)
      }
    })
    macApp.setDefaultMenuBar(menubar.peer)
  }

  private def createPandoraMenuItem(preferences: MuseControllerPreferences, pandoraPlayer: MusicPlayer, menubar: MenuBar, playerSupplier: MusicPlayerSupplier): MenuItem = {
    val pandoraMenuItem = new MenuItem("Pandora")
    pandoraMenuItem.enabled = preferences.isPandoraEnabled
    pandoraMenuItem.reactions += {
      case ButtonClicked(`pandoraMenuItem`) => startupPandora(pandoraMenuItem, pandoraPlayer, preferences, menubar, playerSupplier)
    }
    pandoraMenuItem
  }

  private def createSpotifyMenuItem(preferences: MuseControllerPreferences, spotifySupport: NativeSpotifySupport, menubar: MenuBar, playerSupplier: MusicPlayerSupplier): MenuItem = {
    val spotifyMenuItem = new MenuItem("Spotify")
    spotifyMenuItem.enabled = true
    spotifyMenuItem.reactions += {
      case ButtonClicked(`spotifyMenuItem`) => startupSpotify(spotifyMenuItem, spotifySupport, preferences, menubar, playerSupplier)
    }
    spotifyMenuItem
  }

  def startupGui(playerSupplier: MusicPlayerSupplier, preferences: MuseControllerPreferences, spotifyPlayer: NativeSpotifySupport, pandoraPlayer: MusicPlayer) {

    Swing.onEDT({
      val menubar = new MenuBar

      val pandoraMenuItem = createPandoraMenuItem(preferences, pandoraPlayer, menubar, playerSupplier)
      val spotifyMenuItem = createSpotifyMenuItem(preferences, spotifyPlayer, menubar, playerSupplier)

      val menu = new Menu("Music")
      menu.contents += pandoraMenuItem
      menu.contents += spotifyMenuItem
      menubar.contents += menu

      setUpMacApplicationState(preferences, menubar)

      if (preferences.isPandoraEnabled && preferences.wasPandoraTheLastStreamerOpen) {
        startupPandora(pandoraMenuItem, pandoraPlayer, preferences, menubar, playerSupplier)
      }
      else if (preferences.isSpotifyEnabled && preferences.wasSpotifyTheLastStreamerOpen) {
        startupSpotify(spotifyMenuItem, spotifyPlayer, preferences, menubar, playerSupplier)
      }
      else {
        new Frame pack()
      }
    })
  }

  private def startupSpotify(spotifyMenuItem: MenuItem, spotifySupport: NativeSpotifySupport, preferences: MuseControllerPreferences, mainMenuBar: MenuBar, playerSupplier: MusicPlayerSupplier) {
    playerSupplier.setCurrentApplication(Application.SPOTIFY)
    if (spotifySupport.isAuthorized) {
      setActiveFrame(null)
      val spotifyUI = new SpotifyUI(spotifySupport, mainMenuBar.peer, spotifyMenuItem.peer, preferences)
      val window = spotifyUI.getWindow
      setActiveFrame(spotifyUI)
      spotifyMenuItem.enabled = false
      window.setVisible(true)
    }
    else {
      promptForSpotifyPassword(spotifySupport, preferences, mainMenuBar.peer, spotifyMenuItem.peer)
    }
  }

  private def promptForSpotifyPassword(spotifySupport: NativeSpotifySupport, preferences: MuseControllerPreferences, mainMenuBar: JMenuBar, spotifyMenuItem: JMenuItem) {
    val spotifyPasswordUI = new SpotifyPasswordUI(spotifySupport, preferences, mainMenuBar, spotifyMenuItem, this)
    spotifyMenuItem.setEnabled(false)
    val window = spotifyPasswordUI.getWindow
    setActiveFrame(spotifyPasswordUI)
    window.setLocationRelativeTo(null)
    window.setVisible(true)
  }

  private def startupPandora(pandoraMenuItem: MenuItem, musicPlayer: MusicPlayer, preferences: MuseControllerPreferences, mainMenuBar: MenuBar, playerSupplier: MusicPlayerSupplier) {
    setActiveFrame(null)
    playerSupplier.setCurrentApplication(Application.PANDORAONE)
    if (musicPlayer.isConfigured) {
      try {
        val pandoraUI = new PandoraUI(musicPlayer, mainMenuBar.peer, pandoraMenuItem.peer, preferences)
        pandoraUI.initialize()
        val window = pandoraUI.getWindow
        setActiveFrame(pandoraUI)
        pandoraMenuItem.enabled = false;
        window.setVisible(true)
      }
      catch {
        case e: BadPandoraPasswordException => {
          promptForPandoraPassword(musicPlayer, preferences, mainMenuBar, pandoraMenuItem)
        }
      }
    }
    else {
      promptForPandoraPassword(musicPlayer, preferences, mainMenuBar, pandoraMenuItem)
    }
  }

  private def promptForPandoraPassword(musicPlayer: MusicPlayer, preferences: MuseControllerPreferences, mainMenuBar: MenuBar, pandoraMenuItem: MenuItem) {
    val pandoraPasswordUI = new PandoraPasswordUI(musicPlayer, preferences, mainMenuBar.peer, pandoraMenuItem.peer, this)
    val window = pandoraPasswordUI.getWindow
    setActiveFrame(pandoraPasswordUI)
    window.setLocationRelativeTo(null)
    window.setVisible(true)
  }

}