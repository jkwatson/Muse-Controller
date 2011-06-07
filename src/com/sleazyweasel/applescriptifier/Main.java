package com.sleazyweasel.applescriptifier;

import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.sleazyweasel.sparkle.SparkleActivator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.swing.*;
import java.net.InetAddress;

import static com.apple.dnssd.DNSSD.register;

public class Main {

    private static final int PORT = 23233;

    public static void main(String[] args) throws Exception {
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

        final NativePianobarSupport pianobarSupport = new NativePianobarSupport();

        AirfoilServlet airfoilServlet = new AirfoilServlet(pianobarSupport);
        context.addServlet(new ServletHolder(airfoilServlet), "/airfoil/*");
        context.addServlet(new ServletHolder(new PandoraBoyServlet()), "/pandoraboy/*");
        context.addServlet(new ServletHolder(new PulsarServlet()), "/pulsar/*");
        NativePianobarServlet nativePianobarServlet = new NativePianobarServlet(pianobarSupport);
        context.addServlet(new ServletHolder(nativePianobarServlet), "/pianobar/*");
        context.addServlet(new ServletHolder(new ControlServlet()), "/control/*");

        server.start();

        setupQuaqua();

        addUncaughtExceptionHandler();

        startupPianobar(pianobarSupport);

        try {
            new SparkleActivator().start();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private static void startupPianobar(final NativePianobarSupport pianobarSupport) {
        final boolean pianoBarSupportEnabled = NativePianobarSupport.isPianoBarSupportEnabled();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (pianoBarSupportEnabled) {
                    PianobarUI pianobarUI = new PianobarUI(pianobarSupport);
                    try {
                        pianobarUI.initialize();
                        JFrame window = pianobarUI.getWindow();
                        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        window.setVisible(true);
                    } catch (BadPandoraPasswordException e) {
                        PandoraPasswordUI pandoraPasswordUI = new PandoraPasswordUI(pianobarSupport);

                        JFrame window = pandoraPasswordUI.getWindow();
                        window.setLocationRelativeTo(null);
                        window.setVisible(true);
                    }
                } else {
                    PandoraPasswordUI pandoraPasswordUI = new PandoraPasswordUI(pianobarSupport);

                    JFrame window = pandoraPasswordUI.getWindow();
                    window.setLocationRelativeTo(null);
                    window.setVisible(true);
//                    new JFrame().pack();
                }
            }
        });
    }

    private static void addUncaughtExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        });
    }

    private static void setupQuaqua() {
        // set system properties here that affect Quaqua
        // for example the default layout policy for tabbed
        // panes:
        System.setProperty(
                "Quaqua.tabLayoutPolicy", "wrap"
        );
        // set the Quaqua Look and Feel in the UIManager
        try {
            UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
            // set UI manager properties here that affect Quaqua
        } catch (Exception e) {
            e.printStackTrace();
            // take an appropriate action here
        }
    }
}
