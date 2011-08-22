package com.sleazyweasel.applescriptifier

import javax.swing.JOptionPane
import preferences.MuseControllerPreferences
import java.util.prefs.Preferences
import com.sleazyweasel.sparkle.SparkleActivator

object ScalaMain {

  def main(args: Array[String]) {
    System.setProperty("apple.laf.useScreenMenuBar", "true")

    println("environment = " + System.getenv)

    addUncaughtExceptionHandler()

    new ScalaMain().start()
  }

  private def addUncaughtExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
      def uncaughtException(thread: Thread, e: Throwable) {
        e.printStackTrace()
        JOptionPane.showMessageDialog(null, e.getMessage, "Error", JOptionPane.ERROR_MESSAGE)
      }
    })
  }
}

class ScalaMain {
  private val preferences: MuseControllerPreferences = new MuseControllerPreferences(Preferences.userNodeForPackage(classOf[ScalaMain]))
  private val pianobarSupport: NativePianobarSupport = new NativePianobarSupport

  private def start() {
    val main = new Main()

    if (preferences.isMuseControlEnabled) {
      main.startupWebServer(pianobarSupport)
    }

    main.startupGui(pianobarSupport, preferences)
    activateSparkle()
  }

  private def activateSparkle() {
    try {
      new SparkleActivator().start()
    }
    catch {
      case e: Throwable => {
        e.printStackTrace()
      }
    }
  }

}