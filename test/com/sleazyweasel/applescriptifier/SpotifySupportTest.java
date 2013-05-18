package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpotifySupportTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void testGetStatus_notRunning() throws Exception {
        AppleScriptTemplate appleScriptTemplate = mock(AppleScriptTemplate.class);
        when(appleScriptTemplate.isRunning(Application.SPOTIFY())).thenReturn(false);

        SpotifySupport testClass = new SpotifySupport(appleScriptTemplate);
        Map<String, Object> status = testClass.getStatus();
        assertNotNull(status);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testGetStatus_running() throws Exception {
        //setup
        AppleScriptTemplate appleScriptTemplate = mock(AppleScriptTemplate.class);
        when(appleScriptTemplate.isRunning(Application.SPOTIFY())).thenReturn(true);
        List<Object> playerInfo = new ArrayList<Object>();
        playerInfo.add("playing");
        playerInfo.add(64);
        playerInfo.add(3.5);
        when(appleScriptTemplate.execute(Application.SPOTIFY(), "[get player state as string, get sound volume, get player position]")).thenReturn(playerInfo);
        when(appleScriptTemplate.execute(Application.SPOTIFY(), "get [name, artist, album, duration, spotify url] of current track")).thenReturn(Arrays.asList("New Song", "The Who", "Who Are You", 3.333, "lemons"));

        SpotifySupport testClass = new SpotifySupport(appleScriptTemplate);

        //when
        Map<String, Object> status = testClass.getStatus();

        //then
        assertNotNull(status);
        Map<String, Object> playerState = (Map<String, Object>) status.get("playerState");
        assertNotNull(playerState);
        assertEquals("YES", playerState.get("playing"));
        assertEquals(64, playerState.get("volume"));
        assertEquals(3.5, playerState.get("playerPosition"));

        Map<String, Object> currentTrack = (Map<String, Object>) status.get("currentTrack");
        assertEquals("New Song", currentTrack.get("title"));
        assertEquals("Who Are You", currentTrack.get("album"));
        assertEquals("The Who", currentTrack.get("artist"));
        assertEquals("lemons", currentTrack.get("spotifyUrl"));
        assertEquals(3.333, currentTrack.get("duration"));

    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testGetStatus_noTrack() throws Exception {
        //setup
        AppleScriptTemplate appleScriptTemplate = mock(AppleScriptTemplate.class);
        when(appleScriptTemplate.isRunning(Application.SPOTIFY())).thenReturn(true);
        List<Object> playerInfo = new ArrayList<Object>();
        playerInfo.add("stopped");
        playerInfo.add(64);
        playerInfo.add(3.5);
        when(appleScriptTemplate.execute(Application.SPOTIFY(), "[get player state as string, get sound volume, get player position]")).thenReturn(playerInfo);
        when(appleScriptTemplate.execute(Application.SPOTIFY(), "get [name, artist, album, duration, spotify url] of current track")).thenThrow(new AppleScriptException(new Exception()));

        SpotifySupport testClass = new SpotifySupport(appleScriptTemplate);

        //when
        Map<String, Object> status = testClass.getStatus();

        //then
        assertNotNull(status);

        assertNotNull(status);
        Map<String, Object> playerState = (Map<String, Object>) status.get("playerState");
        assertNotNull(playerState);
        assertEquals("NO", playerState.get("playing"));
        assertEquals(64, playerState.get("volume"));
        assertEquals(3.5, playerState.get("playerPosition"));

        Map<String, Object> currentTrack = (Map<String, Object>) status.get("currentTrack");
        assertEquals("", currentTrack.get("title"));
        assertEquals("", currentTrack.get("album"));
        assertEquals("", currentTrack.get("artist"));
        assertEquals("", currentTrack.get("spotifyUrl"));
        assertEquals("", currentTrack.get("duration"));
    }

}
