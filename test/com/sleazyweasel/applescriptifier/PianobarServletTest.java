package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class PianobarServletTest {

    @Test
    public void testStation() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String station = testClass.extractStation(Arrays.asList("foo=blah", "stationName=Afrobeat Radio"));
        assertEquals("Afrobeat Radio", station);
    }

    @Test
    public void testAlbum() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String album = testClass.extractAlbum(Arrays.asList("foo=blah", "album=Orgone"));
        assertEquals("Orgone", album);
    }

    @Test
    public void testArtist() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String artist = testClass.extractArtist(Arrays.asList("foo=blah", "artist=Orgone"));
        assertEquals("Orgone", artist);
    }

    @Test
    public void testTitle() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String title = testClass.extractTitle(Arrays.asList("foo=blah", "title=Soul Strut"));
        assertEquals("Soul Strut", title);
    }

    @Test
    public void testHeart_false() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String heart = testClass.extractHeart(Arrays.asList("foo=blah", "rating=0"));
        assertEquals("NO", heart);
    }

    @Test
    public void testHeart_true() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        String heart = testClass.extractHeart(Arrays.asList("foo=blah", "rating=1"));
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
                "[?] Select station: \n-");
        assertEquals("NO", inputRequested);
    }

    @Test
    public void testGetStationList() {
        NativePianobarServlet testClass = new NativePianobarServlet(null);
        List<String> input = Arrays.asList(
                "detailUrl=",
                "stationCount=30",
                "stationName=foober",
                "station0=Afrobeat Radio",
                "station1=Antonio Vivaldi Radio",
                "station2=BjÌürk Radio",
                "station3=Burning Spear Radio",
                "station4=CrÌ´cker Radio",
                "station5=Delta Blues",
                "station6=Disco Radio",
                "station7=Everlast Radio",
                "station8=Harvest Moon Radio",
                "station9=House Radio",
                "station10=Interpol Radio",
                "station11=Jem Radio",
                "station12=jkwatson's QuickMix",
                "station13=Jonathan Coulton Radio",
                "station14=Linger Radio",
                "station15=Love Stinks Radio",
                "station16=Lump Radio",
                "station17=Miles Davis Radio",
                "station18=My Head's In Mississippi Radio",
                "station19=New Orleans/Classic Jazz",
                "station20=Pink Martini Radio",
                "station21=Rush Radio",
                "station22=Santana Radio",
                "station23=Son Volt Radio",
                "station24=The Firm Radio",
                "station25=Total Eclipse Of The Heart Radio",
                "station26=Trip Shakespeare Radio",
                "station27=Weezer Radio",
                "station28=Wilco Radio",
                "station29=XTC Radio"
        );

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
