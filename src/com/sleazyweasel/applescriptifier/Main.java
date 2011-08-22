package com.sleazyweasel.applescriptifier;

import com.apple.dnssd.DNSSDException;
import com.apple.dnssd.DNSSDRegistration;
import com.apple.dnssd.DNSSDService;
import com.apple.dnssd.RegisterListener;
import com.apple.eawt.AppEvent;
import com.apple.eawt.PreferencesHandler;
import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.apple.dnssd.DNSSD.register;

public class Main {

    private static final int PORT = 23233;

    private MuseControllerFrame activeFrame;

    public void setActiveFrame(MuseControllerFrame frame) {
        if (activeFrame != null && frame != activeFrame) {
            activeFrame.close();
        }
        activeFrame = frame;
    }

    public void startupWebServer(NativePianobarSupport pianobarSupport) throws UnknownHostException, DNSSDException {
        register(0, 0, InetAddress.getLocalHost().getHostName(), "_asrunner._udp", "local.", null, PORT, null, new RegisterListener() {
            public void serviceRegistered(DNSSDRegistration dnssdRegistration, int port, String s, String s1, String s2) {
            }

            public void operationFailed(DNSSDService dnssdService, int i) {
                throw new RuntimeException("Failed to register Muse Controller service with local DNS.");
            }
        });

        final Server server = new Server(PORT);

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

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    server.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        new Thread(runnable).start();
    }

    public void startupGui(final NativePianobarSupport pianobarSupport, final MuseControllerPreferences preferences) {
        final NativeSpotifySupport spotifySupport = new NativeSpotifySupportImpl();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                com.apple.eawt.Application macApp = com.apple.eawt.Application.getApplication();
                macApp.setPreferencesHandler(new PreferencesHandler() {
                    public void handlePreferences(AppEvent.PreferencesEvent preferencesEvent) {
                        PreferencesGui preferencesGui = new PreferencesGui(preferences);
                        JFrame preferencesFrame = preferencesGui.getWindow();
                        preferencesFrame.setLocationRelativeTo(null);
                        preferencesFrame.setVisible(true);
                    }
                });

                boolean enablePianoBar = preferences.isPianoBarEnabled();

                final JMenuBar menubar = new JMenuBar();
                JMenu menu = new JMenu("Music");
                final JMenuItem pandoraMenuItem = new JMenuItem("Pandora");
                menu.add(pandoraMenuItem);
                pandoraMenuItem.setEnabled(enablePianoBar);
                pandoraMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        startupPandora(pandoraMenuItem, pianobarSupport, preferences, menubar);
                    }
                });

                final JMenuItem spotifyMenuItem = new JMenuItem("Spotify");
                menu.add(spotifyMenuItem);
                spotifyMenuItem.setEnabled(true);
                spotifyMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        startupSpotify(spotifyMenuItem, spotifySupport, preferences, menubar);
                    }
                });

                menubar.add(menu);
                macApp.setDefaultMenuBar(menubar);

                if (enablePianoBar && preferences.wasPandoraTheLastStreamerOpen()) {
                    startupPandora(pandoraMenuItem, pianobarSupport, preferences, menubar);
                } else if (preferences.isSpotifyEnabled() && preferences.wasSpotifyTheLastStreamerOpen()) {
                    startupSpotify(spotifyMenuItem, spotifySupport, preferences, menubar);
                } else {
                    new JFrame().pack();
                }
            }
        });
    }

    private void startupSpotify(JMenuItem spotifyMenuItem, NativeSpotifySupport spotifySupport, MuseControllerPreferences preferences, JMenuBar mainMenuBar) {
        if (spotifySupport.isSpotifyAuthorized()) {
            setActiveFrame(null);
            SpotifyUI spotifyUI = new SpotifyUI(spotifySupport, mainMenuBar, spotifyMenuItem, preferences);
            JFrame window = spotifyUI.getWindow();
            setActiveFrame(spotifyUI);
            spotifyMenuItem.setEnabled(false);
            window.setVisible(true);
        } else {
            promptForSpotifyPassword(spotifySupport, preferences, mainMenuBar, spotifyMenuItem);
        }
    }

    private void promptForSpotifyPassword(NativeSpotifySupport spotifySupport, MuseControllerPreferences preferences, JMenuBar mainMenuBar, JMenuItem spotifyMenuItem) {
        SpotifyPasswordUI spotifyPasswordUI = new SpotifyPasswordUI(spotifySupport, preferences, mainMenuBar, spotifyMenuItem, this);
        spotifyMenuItem.setEnabled(false);
        JFrame window = spotifyPasswordUI.getWindow();
        setActiveFrame(spotifyPasswordUI);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    private void startupPandora(final JMenuItem pandoraMenuItem, NativePianobarSupport pianobarSupport, MuseControllerPreferences preferences, final JMenuBar mainMenuBar) {
        setActiveFrame(null);
        if (NativePianobarSupport.isPianoBarConfigured()) {
            PianobarUI pianobarUI = new PianobarUI(pianobarSupport, mainMenuBar, pandoraMenuItem, preferences);
            try {
                pianobarUI.initialize();
                JFrame window = pianobarUI.getWindow();
                setActiveFrame(pianobarUI);
                pandoraMenuItem.setEnabled(false);
                window.setVisible(true);
            } catch (BadPandoraPasswordException e) {
                promptForPandoraPassword(pianobarSupport, preferences, mainMenuBar, pandoraMenuItem);
            }
        } else {
            promptForPandoraPassword(pianobarSupport, preferences, mainMenuBar, pandoraMenuItem);
        }
    }

    private void promptForPandoraPassword(NativePianobarSupport pianobarSupport, MuseControllerPreferences preferences, JMenuBar mainMenuBar, JMenuItem pandoraMenuItem) {
        PandoraPasswordUI pandoraPasswordUI = new PandoraPasswordUI(pianobarSupport, preferences, mainMenuBar, pandoraMenuItem, this);

        JFrame window = pandoraPasswordUI.getWindow();
        setActiveFrame(pandoraPasswordUI);
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

}
