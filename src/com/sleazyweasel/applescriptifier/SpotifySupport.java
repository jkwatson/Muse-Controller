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
        HashMap<String, Object> status = new HashMap<String, Object>();
        boolean isRunning = appleScriptTemplate.isRunning(Application.SPOTIFY);
        status.put("app", Application.SPOTIFY.getName());
        HashMap<String, Object> state = new HashMap<String, Object>();
        status.put("state", state);
        state.put("running", isRunning);

        if (isRunning) {
            List results = appleScriptTemplate.execute(Application.SPOTIFY, "[get player state as string, get sound volume, get player position]");
            state.put("playerState", results.get(0));
            state.put("volume", results.get(1));
            state.put("playerPosition", results.get(2));

            try {
                List songInfo = appleScriptTemplate.execute(Application.SPOTIFY, "get [name, artist, album, duration, spotify url] of current track");
                state.put("title", songInfo.get(0));
                state.put("artist", songInfo.get(1));
                state.put("album", songInfo.get(2));
                state.put("duration", songInfo.get(3));
                state.put("spotifyUrl", songInfo.get(4));
            } catch (AppleScriptException e) {
                //this happens when there is no current track
                state.put("title", "");
                state.put("artist", "");
                state.put("album", "");
                state.put("duration", "");
                state.put("spotifyUrl", "");
            }
        }
        return status;
    }

    public void setVolume(Integer volume) {
        appleScriptTemplate.execute(Application.SPOTIFY, "set sound volume to " + volume);
    }
}
