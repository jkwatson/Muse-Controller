package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import javax.swing._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class SetupSpotifyConfigAction extends ActionListener {
  def this(preferences: MuseControllerPreferences, usernameField: JTextField, passwordField: JPasswordField, spotifySupport: NativeSpotifySupport, spotifyMenuItem: JMenuItem, window: JFrame, mainMenuBar: JMenuBar, main: MuseControllerMain) {
    this ()
    this.preferences = preferences
    this.usernameField = usernameField
    this.passwordField = passwordField
    this.spotifySupport = spotifySupport
    this.spotifyMenuItem = spotifyMenuItem
    this.parent = window
    this.mainMenuBar = mainMenuBar
    this.main = main
  }

  def actionPerformed(e: ActionEvent) {
    val authorized: Boolean = spotifySupport.authorize(usernameField.getText, passwordField.getPassword)
    if (!authorized) {
      JOptionPane.showMessageDialog(usernameField, "Failed to authenticate. Try again.", "Login Failed", JOptionPane.ERROR_MESSAGE)
    }
    else {
      parent.dispose()
      val spotifyUI: SpotifyUI = new SpotifyUI(spotifySupport, mainMenuBar, spotifyMenuItem, preferences)
      main.setActiveFrame(spotifyUI)
      spotifyUI.getWindow.setVisible(true)
    }
  }

  private final var preferences: MuseControllerPreferences = null
  private final var usernameField: JTextField = null
  private final var passwordField: JPasswordField = null
  private final var spotifySupport: NativeSpotifySupport = null
  private final var spotifyMenuItem: JMenuItem = null
  private final var parent: JFrame = null
  private final var mainMenuBar: JMenuBar = null
  private final var main: MuseControllerMain = null
}