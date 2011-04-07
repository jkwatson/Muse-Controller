package com.sleazyweasel.applescriptifier;

import com.apple.dnssd.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class Main2 {

    public static void main(String[] args) throws DNSSDException, InterruptedException {
        final AtomicBoolean connected = new AtomicBoolean(false);
        DNSSDService service = DNSSD.browse("_asrunner._udp", new BrowseListener() {

            public void serviceFound(DNSSDService dnssdService, int i, int i1, String s, String s1, String s2) {
                System.out.println("Main.serviceFound");
                System.out.println("dnssdService = " + dnssdService);
                System.out.println("i = " + i);
                System.out.println("i1 = " + i1);
                System.out.println("s = " + s);
                System.out.println("s1 = " + s1);
                System.out.println("s2 = " + s2);
                try {
                    DNSSD.resolve(i, i1, s, s1, s2, new ResolveListener() {
                        public void serviceResolved(DNSSDService dnssdService, int i, int i1, String name, String hostName, int port, TXTRecord txtRecord) {
                            System.out.println("Main2.serviceResolved");
                            System.out.println("name = " + name);
                            System.out.println("hostName = " + hostName);
                            System.out.println("port = " + port);
                            connected.set(true);
                            dnssdService.stop();
                        }

                        public void operationFailed(DNSSDService dnssdService, int i) {
                            //To change body of implemented methods use File | Settings | File Templates.
                        }
                    });
                } catch (DNSSDException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            public void serviceLost(DNSSDService dnssdService, int i, int i1, String s, String s1, String s2) {
                System.out.println("Main2.serviceLost");
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void operationFailed(DNSSDService dnssdService, int i) {
                //To change body of implemented methods use File | Settings | File Templates.
                System.out.println("Main2.operationFailed");
            }
        });

        Thread.sleep(10000);
        if (!connected.get()) {
            service.stop();
            System.out.println("not connected in 10s");
        }

    }
}
