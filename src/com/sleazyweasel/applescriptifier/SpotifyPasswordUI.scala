package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import layout.TableLayout
import javax.swing._
import java.awt._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import layout.TableLayoutConstants._

trait SpotifyPasswordUIModule {

  class Widgets {
    var window: JFrame = null
    var passwordField: JPasswordField = null
    var usernameField: JTextField = null
    var neverShowAgainCheckbox: JCheckBox = null
    var okButton: JButton = null
    var cancelButton: JButton = null
  }

  class SpotifyPasswordUI extends MuseControllerFrame {

    private final val widgets = new Widgets

    private final var spotifySupport: NativeSpotifySupport = null
    private final var preferences: MuseControllerPreferences = null
    private final var mainMenuBar: JMenuBar = null
    private final var spotifyMenuItem: JMenuItem = null
    private final var main: MuseControllerMain = null


    def this(spotifySupport: NativeSpotifySupport, preferences: MuseControllerPreferences, mainMenuBar: JMenuBar, spotifyMenuItem: JMenuItem, main: MuseControllerMain) {
      this()
      this.spotifySupport = spotifySupport
      this.preferences = preferences
      this.mainMenuBar = mainMenuBar
      this.spotifyMenuItem = spotifyMenuItem
      this.main = main
      initUserInterface()
      initLayout()
    }

    private def initUserInterface() {
      initWindow()
      initUsernameField()
      initPasswordField()
      initNeverShowAgainCheckbox()
      initOkButton()
      initCancelButton()
    }

    private def initWindow() {
      widgets.window = new JFrame("Spotify Login")
      widgets.window.setResizable(false)
    }

    private def initUsernameField() {
      widgets.usernameField = new JTextField
    }

    private def initPasswordField() {
      widgets.passwordField = new JPasswordField
    }

    private def initNeverShowAgainCheckbox() {
      widgets.neverShowAgainCheckbox = new JCheckBox("I don't use Spotify. Don't ask again.")
    }

    private def initOkButton() {
      widgets.okButton = new JButton("OK")
      widgets.okButton.addActionListener(new SetupSpotifyConfigAction(preferences, widgets.usernameField, widgets.passwordField, spotifySupport, spotifyMenuItem, widgets.window, mainMenuBar, main))
    }

    private def initCancelButton() {
      widgets.cancelButton = new JButton("Cancel")
      widgets.cancelButton.addActionListener(new ActionListener {
        def actionPerformed(e: ActionEvent) {
          close()
          if (widgets.neverShowAgainCheckbox.isSelected) {
            preferences.enableSpotify(false)
          }
        }
      })
    }

    def getWindow: JFrame = {
      widgets.window
    }

    private def initLayout() {
      val contentPane: Container = widgets.window.getContentPane
      val config = Array(Array(4d, PREFERRED, 2d, PREFERRED, 2d, PREFERRED, 4d), Array(4d, 30d, 2d, 30d, 2d, 25d, 2d, 30d, 4d))
      contentPane.setLayout(new TableLayout(config))
      contentPane.add(new JLabel("Username:"), "1, 1")
      contentPane.add(widgets.usernameField, "3, 1, 5, 1")
      contentPane.add(new JLabel("Password:"), "1, 3")
      contentPane.add(widgets.passwordField, "3, 3, 5, 3")
      contentPane.add(widgets.neverShowAgainCheckbox, "3, 5")
      contentPane.add(widgets.cancelButton, "3, 7, r")
      contentPane.add(widgets.okButton, "5, 7, l")
      widgets.window.getRootPane.setDefaultButton(widgets.okButton)
      widgets.window.pack()
    }

    def close() {
      widgets.window.dispose()
      spotifyMenuItem.setEnabled(true)
    }

  }

}