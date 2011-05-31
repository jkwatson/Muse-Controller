package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class PianobarServletTest {

    @Test
    public void testStation() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String station = testClass.extractStation("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("Afrobeat Radio", station);
    }

    @Test
    public void testAlbum() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String album = testClass.extractAlbum("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("Orgone", album);
    }

    @Test
    public void testArtist() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String artist = testClass.extractArtist("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("Orgone", artist);
    }

    @Test
    public void testTitle() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String title = testClass.extractTitle("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("Soul Strut", title);
    }

    @Test
    public void testHeart_false() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String heart = testClass.extractHeart("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\"");
        assertEquals("NO", heart);
    }

    @Test
    public void testHeart_true() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String heart = testClass.extractHeart("|>  Station \"Afrobeat Radio\" (467728149065328655)\n" +
                "|>  \"Soul Strut\" by \"Orgone\" on \"Orgone\" <3");
        assertEquals("YES", heart);
    }

    @Test
    public void testInputRequested_true() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String inputRequested = testClass.extractInputRequested("a bunch of stuff then\n[?] Looking for input!");
        assertEquals("YES", inputRequested);
    }

    @Test
    public void testInputRequested_false() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String inputRequested = testClass.extractInputRequested("\"|>  Station \\\"Afrobeat Radio\\\" (467728149065328655)\\n\" +\n" +
                "                \"|>  \\\"Soul Strut\\\" by \\\"Orgone\\\" on \\\"Orgone\\\" <3\"");
        assertEquals("NO", inputRequested);
    }

    @Test
    public void testInputRequested_postStationSelectionCanceled() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String inputRequested = testClass.extractInputRequested("\t24)     The Firm Radio\n" +
                "\t25)     Total Eclipse Of The Heart Radio\n" +
                "\t26)     Trip Shakespeare Radio\n" +
                "\t27)     Weezer Radio\n" +
                "\t28)     Wilco Radio\n" +
                "\t29)     XTC Radio\n" +
                "[?] Select station: \n");
        assertEquals("NO", inputRequested);
    }

    @Test
    public void testGetStationList() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String input = "(i) Get stations... Ok.\n" +
                "         0)     Afrobeat Radio\n" +
                "         1)     Antonio Vivaldi Radio\n" +
                "         2)     BjÌürk Radio\n" +
                "         3)     Burning Spear Radio\n" +
                "         4)     CrÌ´cker Radio\n" +
                "         5)   S Delta Blues\n" +
                "         6)     Disco Radio\n" +
                "         7)     Everlast Radio\n" +
                "         8)     Harvest Moon Radio\n" +
                "         9)     House Radio\n" +
                "        10)     Interpol Radio\n" +
                "        11)     Jem Radio\n" +
                "        12)  Q  jkwatson's QuickMix\n" +
                "        13)     Jonathan Coulton Radio\n" +
                "        14)     Linger Radio\n" +
                "        15)   S Love Stinks Radio\n" +
                "        16)     Lump Radio\n" +
                "        17)     Miles Davis Radio\n" +
                "        18)     My Head's In Mississippi Radio\n" +
                "        19)     New Orleans/Classic Jazz\n" +
                "        20)     Pink Martini Radio\n" +
                "        21)     Rush Radio\n" +
                "        22)     Santana Radio\n" +
                "        23)     Son Volt Radio\n" +
                "        24)     The Firm Radio\n" +
                "        25)     Total Eclipse Of The Heart Radio\n" +
                "        26)     Trip Shakespeare Radio\n" +
                "        27)     Weezer Radio\n" +
                "        28)     Wilco Radio\n" +
                "        29)     XTC Radio\n" +
                "[?] Select station: 2";

        Map<Integer, String> stationList = testClass.parseStationList(input);
        assertNotNull(stationList);
        assertEquals(30, stationList.size());
        assertEquals("Afrobeat Radio", stationList.get(0));
        assertEquals("Delta Blues", stationList.get(5));
        assertEquals("jkwatson's QuickMix", stationList.get(12));
        assertEquals("Love Stinks Radio", stationList.get(15));
        assertEquals("XTC Radio", stationList.get(29));
    }


}
