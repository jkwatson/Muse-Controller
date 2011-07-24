package com.sleazyweasel.applescriptifier;

import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.apple.eawt.AppEvent;
import com.apple.eawt.PreferencesHandler;
import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
import com.sleazyweasel.sparkle.SparkleActivator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.swing.*;
import java.net.InetAddress;
import java.util.prefs.Preferences;

import static com.apple.dnssd.DNSSD.register;

public class Main {

    private static final int PORT = 23233;

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        final MuseControllerPreferences preferences = new MuseControllerPreferences(Preferences.userNodeForPackage(Main.class));
        final NativePianobarSupport pianobarSupport = new NativePianobarSupport();

        if (preferences.isMuseControlEnabled()) {
            startupWebServer(pianobarSupport);
        }
        addUncaughtExceptionHandler();

        startupGui(pianobarSupport, preferences);

        try {
            new SparkleActivator().start();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private static void startupWebServer(NativePianobarSupport pianobarSupport) throws Exception {
        register(0, 0, InetAddress.getLocalHost().getHostName(), "_asrunner._udp", "local.", null, PORT, null, new RegisterListener() {
            public void serviceRegistered(DNSSDRegistration dnssdRegistration, int port, String s, String s1, String s2) {
            }

            public void operationFailed(DNSSDService dnssdService, int i) {
                System.exit(-3876);
            }
        });

        Server server = new Server(PORT);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);


        AirfoilServlet airfoilServlet = new AirfoilServlet(pianobarSupport);
        context.addServlet(new ServletHolder(airfoilServlet), "/airfoil/*");
        context.addServlet(new ServletHolder(new PandoraBoyServlet()), "/pandoraboy/*");
        context.addServlet(new ServletHolder(new PulsarServlet()), "/pulsar/*");
        NativePianobarServlet nativePianobarServlet = new NativePianobarServlet(pianobarSupport);
        context.addServlet(new ServletHolder(nativePianobarServlet), "/pianobar/*");
        context.addServlet(new ServletHolder(new SpotifyServlet()), "/spotify/*");
        context.addServlet(new ServletHolder(new ControlServlet()), "/control/*");

        server.start();
    }

    private static void startupGui(final NativePianobarSupport pianobarSupport, final MuseControllerPreferences preferences) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                boolean enablePianoBar = preferences.isPianoBarEnabled();
                com.apple.eawt.Application macApp = com.apple.eawt.Application.getApplication();
                macApp.setPreferencesHandler(new PreferencesHandler() {
                    public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
                        PreferencesGui preferencesGui = new PreferencesGui(preferences);
                        JFrame preferencesFrame = preferencesGui.getWindow();
                        preferencesFrame.setLocationRelativeTo(null);
                        preferencesFrame.setVisible(true);
                    }
                });

                if (enablePianoBar) {
                    if (NativePianobarSupport.isPianoBarConfigured()) {
                        PianobarUI pianobarUI = new PianobarUI(pianobarSupport);
                        try {
                            pianobarUI.initialize();
                            JFrame window = pianobarUI.getWindow();
                            window.setVisible(true);
                        } catch (BadPandoraPasswordException e) {
                            promptForPandoraPassword(pianobarSupport, preferences);
                        }
                    } else {
                        promptForPandoraPassword(pianobarSupport, preferences);
                    }
                } else {
                    new JFrame().pack();
                }
            }
        });
    }

    private static void promptForPandoraPassword(NativePianobarSupport pianobarSupport, MuseControllerPreferences preferences) {
        PandoraPasswordUI pandoraPasswordUI = new PandoraPasswordUI(pianobarSupport, preferences);

        JFrame window = pandoraPasswordUI.getWindow();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private static void addUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }

}
