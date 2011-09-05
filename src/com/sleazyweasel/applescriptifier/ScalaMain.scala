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

object ScalaMain {

  def main(args: Array[String]) {
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
        e.printStackTrace()
        JOptionPane.showMessageDialog(null, e.getMessage, "Error", JOptionPane.ERROR_MESSAGE)
      }
    })
  }
}

class ScalaMain {
  private final val PORT: Int = 23233

  private val preferences: MuseControllerPreferences = new MuseControllerPreferences(Preferences.userNodeForPackage(classOf[ScalaMain]))
  private val pianobarSupport: NativePianobarSupport = new NativePianobarSupport

  private def start() {
    println("start")
    val main = new GuiMain()
    activateSparkle()
    main.startupGui(pianobarSupport, preferences)

    if (preferences.isMuseControlEnabled) {
      startupWebServer(pianobarSupport)
    }
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

  private def startupWebServer(pianobarSupport: NativePianobarSupport) {
    register(0, 0, InetAddress.getLocalHost.getHostName, "_asrunner._udp", "local.", null, PORT, null, new RegisterListener {
      def serviceRegistered(dnssdRegistration: DNSSDRegistration, port: Int, s: String, s1: String, s2: String) {
      }

      def operationFailed(dnssdService: DNSSDService, i: Int) {
        throw new RuntimeException("Failed to register Muse Controller service with local DNS.")
      }
    })
    val server = new Server(PORT)

    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
    server.setHandler(context)

    val airfoilServlet = new AirfoilServlet(pianobarSupport)
    context.addServlet(new ServletHolder(airfoilServlet), "/airfoil/*")
    context.addServlet(new ServletHolder(new PandoraBoyServlet), "/pandoraboy/*")
    context.addServlet(new ServletHolder(new PulsarServlet), "/pulsar/*")

    val nativePianobarServlet = new NativePianobarServlet(pianobarSupport)
    context.addServlet(new ServletHolder(nativePianobarServlet), "/pianobar/*")
    context.addServlet(new ServletHolder(new SpotifyServlet), "/spotify/*")
    context.addServlet(new ServletHolder(new ControlServlet), "/control/*")

    spawn({
      server.start();
    })
  }
}