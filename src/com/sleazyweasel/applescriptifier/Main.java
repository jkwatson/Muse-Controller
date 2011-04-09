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

        context.addServlet(new ServletHolder(new AirfoilServlet()), "/airfoil/*");
        context.addServlet(new ServletHolder(new PandoraBoyServlet()), "/pandoraboy/*");
        context.addServlet(new ServletHolder(new PulsarServlet()), "/pulsar/*");
        context.addServlet(new ServletHolder(new ControlServlet()), "/control/*");

        server.start();

        new JFrame().pack();

        try {
            new SparkleActivator().start();
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        server.join();

    }
}
