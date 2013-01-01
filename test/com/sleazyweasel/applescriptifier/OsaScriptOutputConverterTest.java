package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static junit.framework.Assert.*;

public class OsaScriptOutputConverterTest {
    private static final Logger logger = Logger.getLogger(OsaScriptOutputConverterTest.class.getName());
    @Test
    public void testOutputString() {
        OsaScriptOutputConverter testClass = new OsaScriptOutputConverter();
        Object result = testClass.convert("result");
        assertEquals("result", result);
    }

    @Test
    public void testOutputStringInQuotes() {
        OsaScriptOutputConverter testClass = new OsaScriptOutputConverter();
        Object result = testClass.convert("\"some result\"");
        assertEquals("some result", result);
    }

    @Test
    public void testBrokenOldAirfoil() {
        String big = "{{|classCode|:1.09512792E+9, |uniqueID|:\"com.rogueamoeba.airfoil.LocalSpeaker\", |name|:\"Computer\", connected:0, volume:0.75}, {|classCode|:1.09512792E+9, |uniqueID|:\"0023DFF7C27C\", |name|:\"Living Room\", connected:0, volume:0.75}, {|classCode|:1.09512792E+9, |uniqueID|:\"0016CB941A4D\", |name|:\"Kitchen\", connected:0, volume:0.75}, {|classCode|:1.09512792E+9, |uniqueID|:\"5855CA04DBCA\", |name|:\"Apple TV\", connected:0, volume:0.75}, {|classCode|:1.09512792E+9, |uniqueID|:\"F0B479007666\", |name|:\"Upstairs\", connected:0, volume:0.75}}, {{|classCode|:1.094808932E+9, |uniqueID|:0, |name|:\"QuickTime Player\", |applicationFile|:\"/Applications/QuickTime Player.app\"}, {|classCode|:1.094808932E+9, |uniqueID|:1, |name|:\"iTunes\", |applicationFile|:\"/Applications/iTunes.app\"}}, {}, {|classCode|:1.094808932E+9, |uniqueID|:1, |name|:\"iTunes\", |applicationFile|:\"/Applications/iTunes.app\"}}";
        OsaScriptOutputConverter testClass = new OsaScriptOutputConverter();
        List results = testClass.convert(big);
        logger.info("results = " + results);
        Object shouldBeAList = results.get(1);
        assertTrue(shouldBeAList instanceof List);
        logger.info("shouldBeAList = " + shouldBeAList);
    }

    @Test
    public void testBiggerComplexThing() {
        String big = "{{{|name|:\"Computer\", volume:0.602272748947, uniqueID:\"com.rogueamoeba.airfoil.LocalSpeaker\", connected:0, classCode:1.09512792E+9}, {|name|:\"Living Room\", volume:0.550781011581, uniqueID:\"0023DFF7C27C\", connected:0, classCode:1.09512792E+9}, {|name|:\"Upstairs\", volume:0.552734017372, uniqueID:\"F0B479007666\", connected:0, classCode:1.09512792E+9}, {|name|:\"Apple TV\", volume:0.351561993361, uniqueID:\"5855CA04DBCA\", connected:0, classCode:1.09512792E+9}}, {{|name|:\"Pandora\", applicationFile:\"/Applications/Pandora.app\", uniqueID:0, classCode:1.094808932E+9}, {|name|:\"iTunes\", applicationFile:\"/Applications/iTunes.app\", uniqueID:1, classCode:1.094808932E+9}, {|name|:\"PandoraBoy\", applicationFile:\"/Applications/PandoraBoy.app\", uniqueID:2, classCode:1.094808932E+9}, {|name|:\"Google Chrome\", applicationFile:\"/Applications/Google Chrome.app\", uniqueID:3, classCode:1.094808932E+9}, {|name|:\"Last.fm\", applicationFile:\"/Applications/Last.fm.app\", uniqueID:4, classCode:1.094808932E+9}}}";
        OsaScriptOutputConverter testClass = new OsaScriptOutputConverter();
        List results = testClass.convert(big);
        logger.info("results = " + results);
        Object shouldBeAList = results.get(1);
        assertTrue(shouldBeAList instanceof List);
        logger.info("shouldBeAList = " + shouldBeAList);
    }

    @Test
    public void testBigComplexThing() {
        String big = "{{{|name|:\"Computer\", volume:0.602272748947, uniqueID:\"com.rogueamoeba.airfoil.LocalSpeaker\", connected:0, classCode:1.09512792E+9}, {|name|:\"Living Room\", volume:0.550781011581, uniqueID:\"0023DFF7C27C\", connected:0, classCode:1.09512792E+9}, {|name|:\"Upstairs\", volume:0.552734017372, uniqueID:\"F0B479007666\", connected:0, classCode:1.09512792E+9}, {|name|:\"Apple TV\", volume:0.351561993361, uniqueID:\"5855CA04DBCA\", connected:0, classCode:1.09512792E+9}}}";
        OsaScriptOutputConverter testClass = new OsaScriptOutputConverter();
        List results = testClass.convert(big);
        logger.info("results = " + results);
    }

    @Test
    public void testSimpleMap() {
       String output = "{|name|:\"Computer\", volume:0.602272748947, uniqueID:\"com.rogueamoeba.airfoil.LocalSpeaker\", connected:0, classCode:1.09512792E+9}";
        OsaScriptOutputConverter testClass = new OsaScriptOutputConverter();
        Map result = testClass.convert(output);
        logger.info("result = " + result);
        assertNotNull(result);
        assertEquals("Computer", result.get("name"));
        assertEquals("0.602272748947", result.get("volume"));
        assertEquals("com.rogueamoeba.airfoil.LocalSpeaker", result.get("uniqueID"));
        assertEquals("0", result.get("connected"));
        assertEquals("1.09512792E+9", result.get("classCode"));
    }

    @Test
    public void testSimpleList() {
        String output = "{\"Computer\", volume, 0.343434}";
        OsaScriptOutputConverter testClass = new OsaScriptOutputConverter();
        List result = testClass.convert(output);
        logger.info("result = " + result);
        assertNotNull(result);
        assertEquals("Computer", result.get(0));
        assertEquals("volume", result.get(1));
        assertEquals("0.343434", result.get(2));
    }

    @Test
    public void testListOfMaps() {
        String output = "{{name:\"Computer\"}, {foo:volume, thing:0.343434}}";
        OsaScriptOutputConverter testClass = new OsaScriptOutputConverter();
        List result = testClass.convert(output);
        logger.info("result = " + result);
        assertNotNull(result);
        Map first = (Map) result.get(0);
        assertEquals("Computer", first.get("name"));

        Map second = (Map) result.get(1);
        assertEquals("volume", second.get("foo"));
        assertEquals("0.343434", second.get("thing"));
    }

}
