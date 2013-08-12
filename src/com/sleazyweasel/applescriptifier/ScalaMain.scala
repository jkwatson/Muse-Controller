package com.sleazyweasel.applescriptifier

import preferences.MuseControllerPreferences
import java.util.prefs.Preferences
import com.sleazyweasel.sparkle.SparkleActivator
import com.apple.dnssd.DNSSD._
import java.net.InetAddress
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{ServletHolder, ServletContextHandler}
import com.apple.dnssd.{RegisterListener, DNSSDRegistration, DNSSDService}
import scala.concurrent.ops.spawn
import javax.swing.{JMenuBar, JOptionPane}
import java.util.logging.{SimpleFormatter, FileHandler, Level, Logger}
import java.io.File

object ScalaMain {
  def logger: Logger = Logger.getLogger(ScalaMain.getClass.getName)

  def main(args: Array[String]) {
    val userHomeDirectory: String = System.getProperty("user.home")
    new File(userHomeDirectory + "/Library/Logs/MuseController/").mkdirs()
    val fileHandler: FileHandler = new FileHandler("%h/Library/Logs/MuseController/MuseController.log", 1000000, 10, true)

    fileHandler.setFormatter(new SimpleFormatter)
    Logger.getLogger("com").addHandler(fileHandler)
    System.setProperty("apple.laf.useScreenMenuBar", "true")

    println("environment = " + System.getenv)

    addUncaughtExceptionHandler()
    //todo wtf? If I don't have this here, the gui hangs...
    val foo = new JMenuBar

    new ScalaMain().start()
  }

  private def addUncaughtExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
      def uncaughtException(thread: Thread, e: Throwable) {
        logger.log(Level.WARNING, "Exception caught.", e)
        JOptionPane.showMessageDialog(null, e.getMessage, "Error", JOptionPane.ERROR_MESSAGE)
      }
    })
  }
}

class ScalaMain {
  private final val PORT: Int = 23233

  def logger: Logger = Logger.getLogger(ScalaMain.getClass.getName)

  private val preferences: MuseControllerPreferences = new MuseControllerPreferences(Preferences.userNodeForPackage(classOf[ScalaMain]))

  private def start() {
    println("start")
    val main = new GuiMain()
    activateSparkle()
    val playerSupplier: MusicPlayerSupplier = new MusicPlayerSupplier
    val spotifyPlayer: NativeSpotifySupportImpl = new NativeSpotifySupportImpl
    val pandoraPlayer: JavaPandoraPlayer = new JavaPandoraPlayer(preferences)

    playerSupplier.addMusicPlayer(Application.SPOTIFY, spotifyPlayer)
    playerSupplier.addMusicPlayer(Application.PANDORAONE, pandoraPlayer)
    if (preferences.wasPandoraTheLastStreamerOpen()) {
      playerSupplier.setCurrentApplication(Application.PANDORAONE)
    }
    else if (preferences.wasSpotifyTheLastStreamerOpen()) {
      playerSupplier.setCurrentApplication(Application.SPOTIFY)
    }
    else if (preferences.isPandoraEnabled) {
      playerSupplier.setCurrentApplication(Application.PANDORAONE)
    }

    main.startupGui(playerSupplier, preferences, spotifyPlayer, pandoraPlayer)
    if (preferences.isMuseControlEnabled) {
      startupWebServer(playerSupplier, preferences)
    }
  }

  private def activateSparkle() {
    try {
      new SparkleActivator().start()
    }
    catch {
      case e: Throwable => {
        logger.log(Level.WARNING, "Exception caught.", e)
      }
    }
  }

  private def startupWebServer(player: MusicPlayerSupplier, preferences: MuseControllerPreferences) {
    //big todo: make the port dynamic...search until you find a free one.
    val server = new Server(PORT)
    register(0, 0, InetAddress.getLocalHost.getHostName, "_asrunner._udp", "local.", null, PORT, null, new RegisterListener {
      def serviceRegistered(dnssdRegistration: DNSSDRegistration, port: Int, s: String, s1: String, s2: String) {
      }

      def operationFailed(dnssdService: DNSSDService, i: Int) {
        throw new RuntimeException("Failed to register Muse Controller service with local DNS.")
      }
    })

    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
    server.setHandler(context)

    val airfoilServlet = new AirfoilServlet(player)
    context.addServlet(new ServletHolder(airfoilServlet), "/airfoil/*")
    context.addServlet(new ServletHolder(new PulsarServlet), "/pulsar/*")
    context.addServlet(new ServletHolder(new RdioServlet), "/rdio/*")

    val musicPlayerServlet = new MusicPlayerServlet(player)
    context.addServlet(new ServletHolder(musicPlayerServlet), "/pianobar/*")
    context.addServlet(new ServletHolder(musicPlayerServlet), "/pandora/*")
    context.addServlet(new ServletHolder(new SpotifyServlet), "/spotify/*")
    context.addServlet(new ServletHolder(new ControlServlet(player)), "/control/*")

    spawn({
      server.start()
    })
  }
}