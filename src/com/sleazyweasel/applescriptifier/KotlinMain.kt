package com.sleazyweasel.musecontroller

import javax.swing.JOptionPane
import javax.swing.JMenuBar
import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import java.util.prefs.Preferences
import com.sleazyweasel.applescriptifier.*
import com.sleazyweasel.sparkle.SparkleActivator
import org.eclipse.jetty.server.Server
import com.apple.dnssd.DNSSD
import java.net.InetAddress
import com.apple.dnssd.RegisterListener
import com.apple.dnssd.DNSSDService
import com.apple.dnssd.DNSSDRegistration
import org.eclipse.jetty.servlet.ServletContextHandler
import com.sleazyweasel.applescriptifier.AirfoilServlet
import org.eclipse.jetty.servlet.ServletHolder
import com.sleazyweasel.applescriptifier.PandoraBoyServlet

fun main(args : Array<String>) {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    println("environment = ${System.getenv()}")
    val foo = JMenuBar()
    KotlinMain().start()

}

fun addUncaughtExceptionHandler() {
    Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler())
}

class KotlinMain() {
    private val PORT : Int = 23233
    private val preferences : MuseControllerPreferences = MuseControllerPreferences(Preferences.userNodeForPackage(Class.forName("com.sleazyweasel.applescriptifier.Application")));
    private val musicPlayer : MusicPlayer = JavaPandoraPlayer(preferences)

    fun start() {
        println("start");
        val main = KotlinGui()
        activateSparkle()
        main.startupGui(musicPlayer, preferences)
        if (preferences.isMuseControlEnabled()) {
            startupWebServer(musicPlayer)
        }
    }

    fun startupWebServer(musicPlayer: MusicPlayer) {
        val server = Server(PORT)
        DNSSD.register(0, 0, InetAddress.getLocalHost()?.getHostName(), "_asrunner._udp", "local.", null, PORT, null, RegisterListener())

        val context = ServletContextHandler(ServletContextHandler.SESSIONS)
        context.setContextPath("/")
        server.setHandler(context)

        val airfoilServlet = AirfoilServlet(musicPlayer)
        val musicPlayerServlet = MusicPlayerServlet(musicPlayer)
        context.addServlet(ServletHolder(airfoilServlet), "/airfoil/*")
        context.addServlet(ServletHolder(PandoraBoyServlet()), "/pandoraboy/*")
        context.addServlet(ServletHolder(PulsarServlet()), "/pulsar/*");
        context.addServlet(ServletHolder(RdioServlet()), "/rdio/*");
        context.addServlet(ServletHolder(musicPlayerServlet), "/pianobar/*")
        context.addServlet(ServletHolder(musicPlayerServlet), "/musicplayer/*")
        context.addServlet(ServletHolder(SpotifyServlet()), "/spotify/*")
        context.addServlet(ServletHolder(ControlServlet(musicPlayer)), "/control/*")

        Thread(ServerThread(server)).start()
    }

    class ServerThread(server: Server) : Runnable {
        val server = server
        override fun run() {
            server.start()
        }
    }

    fun activateSparkle() {
        try {
            SparkleActivator().start()
        }
        catch (e:Throwable) {
            e.printStackTrace()
        }
    }
}

class RegisterListener : com.apple.dnssd.RegisterListener{

    override fun operationFailed(p0: DNSSDService?, p1: Int) {
        throw RuntimeException("Failed to register Muse Controller service with local DNS.")
    }

    override fun serviceRegistered(p0: DNSSDRegistration?, p1: Int, p2: String?, p3: String?, p4: String?) {

    }
}

class UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        e?.printStackTrace()
        JOptionPane.showMessageDialog(null, e?.getMessage(), "Error", JOptionPane.ERROR_MESSAGE)
    }
}