package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class NativePianobarSupportTest {

    @Test
    public void testStation() {
        NativePianobarSupport testClass = new NativePianobarSupport();
        String station = testClass.extractStation(Arrays.asList("foo=blah", "stationName=Afrobeat Radio"));
        assertEquals("Afrobeat Radio", station);
    }

    @Test
    public void testAlbum() {
        NativePianobarSupport testClass = new NativePianobarSupport();
        String album = testClass.extractAlbum(Arrays.asList("foo=blah", "album=Orgone"));
        assertEquals("Orgone", album);
    }

    @Test
    public void testArtist() {
        NativePianobarSupport testClass = new NativePianobarSupport();
        String artist = testClass.extractArtist(Arrays.asList("foo=blah", "artist=Orgone"));
        assertEquals("Orgone", artist);
    }

    @Test
    public void testTitle() {
        NativePianobarSupport testClass = new NativePianobarSupport();
        String title = testClass.extractTitle(Arrays.asList("foo=blah", "title=Soul Strut"));
        assertEquals("Soul Strut", title);
    }

    @Test
    public void testHeart_false() {
        NativePianobarSupport testClass = new NativePianobarSupport();
        String heart = testClass.extractHeart(Arrays.asList("foo=blah", "rating=0"));
        assertEquals("NO", heart);
    }

    @Test
    public void testHeart_true() {
        NativePianobarSupport testClass = new NativePianobarSupport();
        String heart = testClass.extractHeart(Arrays.asList("foo=blah", "rating=1"));
        assertEquals("YES", heart);
    }


    @Test
    public void testGetStationList() {
        NativePianobarSupport testClass = new NativePianobarSupport();
        List<String> input = Arrays.asList(
                "detailUrl=",
                "stationCount=30",
                "stationName=foober",
                "station0=Afrobeat Radio",
                "station1=Antonio Vivaldi Radio",
                "station2=Bjørk Radio",
                "station3=Burning Spear Radio",
                "station4=Cr̴cker Radio",
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
