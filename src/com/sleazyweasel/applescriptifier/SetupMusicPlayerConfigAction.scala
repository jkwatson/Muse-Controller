package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import javax.swing._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.IOException
import java.util.logging.Level
import java.util.logging.Logger

object SetupMusicPlayerConfigAction {
  private final val logger: Logger = Logger.getLogger(classOf[SetupMusicPlayerConfigAction].getName)
}

class SetupMusicPlayerConfigAction(val parent: JFrame, val musicPlayer: MusicPlayer, val usernameField: JTextField,
                                   val passwordField: JPasswordField, val preferences: MuseControllerPreferences, val mainMenuBar: JMenuBar, val pandoraMenuItem: JMenuItem, val main: MuseControllerMain)
  extends ActionListener {

  def actionPerformed(event: ActionEvent) {
    try {
      musicPlayer.saveConfig(usernameField.getText, passwordField.getPassword)
      parent.dispose()
      val pandoraUI: PandoraUI = new PandoraUI(musicPlayer, mainMenuBar, pandoraMenuItem, preferences)
      pandoraUI.initialize()
      main.setActiveFrame(pandoraUI)
      pandoraUI.getWindow.setVisible(true)
    }
    catch {
      case e: IOException => {
        SetupMusicPlayerConfigAction.logger.log(Level.WARNING, "Exception caught.", e)
        JOptionPane.showMessageDialog(parent, "Failed to Configure Pandora", "Error", JOptionPane.ERROR_MESSAGE)
      }
      case e: BadPandoraPasswordException => {
        val pandoraPasswordUI: PandoraPasswordUI = new PandoraPasswordUI(musicPlayer, preferences, mainMenuBar, pandoraMenuItem, main)
        val window: JFrame = pandoraPasswordUI.getWindow
        window.setLocationRelativeTo(null)
        main.setActiveFrame(pandoraPasswordUI)
        window.setVisible(true)
      }
    }
  }

}