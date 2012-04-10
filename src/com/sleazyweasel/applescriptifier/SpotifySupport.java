package com.sleazyweasel.applescriptifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpotifySupport implements ApplicationSupport {
    private final AppleScriptTemplate appleScriptTemplate;

    public SpotifySupport(AppleScriptTemplate appleScriptTemplate) {
        this.appleScriptTemplate = appleScriptTemplate;
    }

    public void playPause() {
        appleScriptTemplate.execute(Application.SPOTIFY, "playpause");
    }

    public void next() {
        appleScriptTemplate.execute(Application.SPOTIFY, "next track");
    }

    public void previous() {
        appleScriptTemplate.execute(Application.SPOTIFY, "previous track");
    }

    public void thumbsUp() {
    }

    public void thumbsDown() {
    }

    public Map<String, Object> getStatus() {
        boolean isRunning = appleScriptTemplate.isRunning(Application.SPOTIFY);
        Map<String, Object> playerState = new HashMap<String, Object>();
        Map<String, Object> currentTrack = new HashMap<String, Object>();

        if (isRunning) {
            List results = appleScriptTemplate.execute(Application.SPOTIFY, "[get player state as string, get sound volume, get player position]");
            playerState.put("playing", "playing".equals(results.get(0)) ? "YES" : "NO");
            playerState.put("volume", results.get(1));
            playerState.put("playerPosition", results.get(2));

            try {
                List songInfo = appleScriptTemplate.execute(Application.SPOTIFY, "get [name, artist, album, duration, spotify url] of current track");
                currentTrack.put("title", songInfo.get(0));
                currentTrack.put("artist", songInfo.get(1));
                currentTrack.put("album", songInfo.get(2));
                currentTrack.put("duration", songInfo.get(3));
                currentTrack.put("spotifyUrl", songInfo.get(4));
            } catch (AppleScriptException e) {
                //this happens when there is no current track
                currentTrack.put("title", "");
                currentTrack.put("artist", "");
                currentTrack.put("album", "");
                currentTrack.put("duration", "");
                currentTrack.put("spotifyUrl", "");
            }
        }
        HashMap<String, Object> status = new HashMap<String, Object>();
        status.put("currentTrack", currentTrack);
        status.put("playerState", playerState);
        status.put("version", ControlServlet.CURRENT_VERSION);

        return status;
    }

    public void setVolume(Integer volume) {
        appleScriptTemplate.execute(Application.SPOTIFY, "set sound volume to " + volume);
    }
}
