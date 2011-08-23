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

  private def createPandoraMenuItem(preferences: MuseControllerPreferences, pianobarSupport: NativePianobarSupport, menubar: MenuBar): MenuItem = {
    val pandoraMenuItem = new MenuItem("Pandora")
    pandoraMenuItem.enabled = preferences.isPianoBarEnabled
    pandoraMenuItem.reactions += {
      case ButtonClicked(`pandoraMenuItem`) => startupPandora(pandoraMenuItem, pianobarSupport, preferences, menubar)
    }
    pandoraMenuItem
  }

  private def createSpotifyMenuItem(preferences: MuseControllerPreferences, spotifySupport: NativeSpotifySupportImpl, menubar: MenuBar): MenuItem = {
    val spotifyMenuItem = new MenuItem("Spotify")
    spotifyMenuItem.enabled = true
    spotifyMenuItem.reactions += {
      case ButtonClicked(`spotifyMenuItem`) => startupSpotify(spotifyMenuItem, spotifySupport, preferences, menubar)
    }
    spotifyMenuItem
  }

  def startupGui(pianobarSupport: NativePianobarSupport, preferences: MuseControllerPreferences) {
    val spotifySupport = new NativeSpotifySupportImpl

    Swing.onEDT({
      val menubar = new MenuBar

      val pandoraMenuItem = createPandoraMenuItem(preferences, pianobarSupport, menubar)
      val spotifyMenuItem = createSpotifyMenuItem(preferences, spotifySupport, menubar)

      val menu = new Menu("Music")
      menu.contents += pandoraMenuItem
      menu.contents += spotifyMenuItem
      menubar.contents += menu

      setUpMacApplicationState(preferences, menubar)

      if (preferences.isPianoBarEnabled && preferences.wasPandoraTheLastStreamerOpen) {
        startupPandora(pandoraMenuItem, pianobarSupport, preferences, menubar)
      }
      else if (preferences.isSpotifyEnabled && preferences.wasSpotifyTheLastStreamerOpen) {
        startupSpotify(spotifyMenuItem, spotifySupport, preferences, menubar)
      }
      else {
        new Frame pack()
      }
    })
  }

  private def startupSpotify(spotifyMenuItem: MenuItem, spotifySupport: NativeSpotifySupport, preferences: MuseControllerPreferences, mainMenuBar: MenuBar) {
    if (spotifySupport.isSpotifyAuthorized) {
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

  private def startupPandora(pandoraMenuItem: MenuItem, pianobarSupport: NativePianobarSupport, preferences: MuseControllerPreferences, mainMenuBar: MenuBar) {
    setActiveFrame(null)
    if (NativePianobarSupport.isPianoBarConfigured) {
      val pianobarUI = new PianobarUI(pianobarSupport, mainMenuBar.peer, pandoraMenuItem.peer, preferences)
      try {
        pianobarUI.initialize()
        val window = pianobarUI.getWindow
        setActiveFrame(pianobarUI)
        pandoraMenuItem.enabled = false;
        window.setVisible(true)
      }
      catch {
        case e: BadPandoraPasswordException => {
          promptForPandoraPassword(pianobarSupport, preferences, mainMenuBar, pandoraMenuItem)
        }
      }
    }
    else {
      promptForPandoraPassword(pianobarSupport, preferences, mainMenuBar, pandoraMenuItem)
    }
  }

  private def promptForPandoraPassword(pianobarSupport: NativePianobarSupport, preferences: MuseControllerPreferences, mainMenuBar: MenuBar, pandoraMenuItem: MenuItem) {
    val pandoraPasswordUI = new PandoraPasswordUI(pianobarSupport, preferences, mainMenuBar.peer, pandoraMenuItem.peer, this)
    val window = pandoraPasswordUI.getWindow
    setActiveFrame(pandoraPasswordUI)
    window.setLocationRelativeTo(null)
    window.setVisible(true)
  }

}