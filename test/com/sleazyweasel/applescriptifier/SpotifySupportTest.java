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
        when(appleScriptTemplate.isRunning(Application.SPOTIFY)).thenReturn(false);

        SpotifySupport testClass = new SpotifySupport(appleScriptTemplate);
        Map<String, Object> status = testClass.getStatus();
        assertNotNull(status);
        assertEquals("Spotify", status.get("app"));

        Map<String, Object> state = (Map<String, Object>) status.get("state");
        assertNotNull(state);

        assertEquals(false, state.get("running"));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testGetStatus_running() throws Exception {
        //setup
        AppleScriptTemplate appleScriptTemplate = mock(AppleScriptTemplate.class);
        when(appleScriptTemplate.isRunning(Application.SPOTIFY)).thenReturn(true);
        List<Object> playerInfo = new ArrayList<Object>();
        playerInfo.add("stopped");
        playerInfo.add(64);
        playerInfo.add(3.5);
        when(appleScriptTemplate.execute(Application.SPOTIFY, "[get player state as string, get sound volume, get player position]")).thenReturn(playerInfo);
        when(appleScriptTemplate.execute(Application.SPOTIFY, "get [name, artist, album] of current track")).thenReturn(Arrays.asList("New Song", "The Who", "Who Are You"));

        SpotifySupport testClass = new SpotifySupport(appleScriptTemplate);

        //when
        Map<String, Object> status = testClass.getStatus();

        //then
        assertNotNull(status);
        assertEquals("Spotify", status.get("app"));

        Map<String, Object> state = (Map<String, Object>) status.get("state");
        assertNotNull(state);

        assertEquals(true, state.get("running"));
        assertEquals("stopped", state.get("playerState"));
        assertEquals(64, state.get("volume"));
        assertEquals(3.5, state.get("playerPosition"));
        assertEquals("New Song", state.get("songName"));
        assertEquals("Who Are You", state.get("album"));
        assertEquals("The Who", state.get("artist"));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testGetStatus_noTrack() throws Exception {
        //setup
        AppleScriptTemplate appleScriptTemplate = mock(AppleScriptTemplate.class);
        when(appleScriptTemplate.isRunning(Application.SPOTIFY)).thenReturn(true);
        List<Object> playerInfo = new ArrayList<Object>();
        playerInfo.add("stopped");
        playerInfo.add(64);
        playerInfo.add(3.5);
        when(appleScriptTemplate.execute(Application.SPOTIFY, "[get player state as string, get sound volume, get player position]")).thenReturn(playerInfo);
        when(appleScriptTemplate.execute(Application.SPOTIFY, "get [name, artist, album] of current track")).thenThrow(new AppleScriptException(new Exception()));

        SpotifySupport testClass = new SpotifySupport(appleScriptTemplate);

        //when
        Map<String, Object> status = testClass.getStatus();

        //then
        assertNotNull(status);
        assertEquals("Spotify", status.get("app"));

        Map<String, Object> state = (Map<String, Object>) status.get("state");
        assertNotNull(state);

        assertEquals(true, state.get("running"));
        assertEquals("stopped", state.get("playerState"));
        assertEquals(64, state.get("volume"));
        assertEquals(3.5, state.get("playerPosition"));
        assertEquals("", state.get("songName"));
        assertEquals("", state.get("album"));
        assertEquals("", state.get("artist"));
    }

}
