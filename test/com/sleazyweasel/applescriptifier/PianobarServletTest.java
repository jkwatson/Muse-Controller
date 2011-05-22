package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import static junit.framework.Assert.*;

public class PianobarServletTest {
    
    @Test
    public void testStation() {
        PianobarServlet testClass = new PianobarServlet();
        String station = testClass.extractStation("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("Afrobeat Radio", station);
    }

    @Test
    public void testAlbum() {
        PianobarServlet testClass = new PianobarServlet();
        String album = testClass.extractAlbum("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("Orgone", album);
    }

    @Test
    public void testArtist() {
        PianobarServlet testClass = new PianobarServlet();
        String artist = testClass.extractArtist("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("Orgone", artist);
    }

    @Test
    public void testTitle() {
        PianobarServlet testClass = new PianobarServlet();
        String title = testClass.extractTitle("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("Soul Strut", title);
    }

    @Test
    public void testHeart_false() {
        PianobarServlet testClass = new PianobarServlet();
        String heart = testClass.extractHeart("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("NO", heart);
    }

    @Test
    public void testHeart_true() {
        PianobarServlet testClass = new PianobarServlet();
        String heart = testClass.extractHeart("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\" <3");
        assertEquals("YES", heart);
    }

    @Test
    public void testInputRequested_true() {
        PianobarServlet testClass = new PianobarServlet();
        String inputRequested = testClass.extractInputRequested("a bunch of stuff then\n[?] Looking for input!");
        assertEquals("YES", inputRequested);
    }

    @Test
    public void testInputRequested_false() {
        PianobarServlet testClass = new PianobarServlet();
        String inputRequested = testClass.extractInputRequested("\"|>  Station \\\"Afrobeat Radio\\\" (467728149065328655)\\n\" +\n" +
                "                \"|>  \\\"Soul Strut\\\" by \\\"Orgone\\\" on \\\"Orgone\\\" <3\"");
        assertEquals("NO", inputRequested);
    }


}
