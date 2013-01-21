package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;
import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Collator;
import java.util.*;

public class AirfoilServlet extends HttpServlet {
    private static final String CURRENT_SOURCE_KEY = "currentSource";
    private static final String NAME_KEY = "name";
    private static final String STATE_KEY = "state";
    private AppleScriptTemplate appleScriptTemplate = new AppleScriptTemplateFactory().getActiveTemplate();
    private final MusicPlayer musicPlayer;
    private final MuseControllerPreferences preferences;

    public AirfoilServlet(MusicPlayer musicPlayer, MuseControllerPreferences preferences) {
        this.musicPlayer = musicPlayer;
        this.preferences = preferences;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        // Set to expire far in the past.
        response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
        // Set standard HTTP/1.1 no-cache headers.
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        // Set standard HTTP/1.0 no-cache header.
        response.setHeader("Pragma", "no-cache");
        response.setContentType("application/json; charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo.equals("/status")) {
            response.setStatus(HttpServletResponse.SC_OK);
            appendStatus(response);
        } else if (pathInfo.startsWith("/updateSpeaker")) {
            String speakerId = req.getParameter("id");
            String conn = req.getParameter("conn");
            String volume = req.getParameter("volume");
            if (conn != null) {
                boolean connected = "1".equals(conn);
                updateSpeakerStatus(response, speakerId, connected);
            } else if (volume != null) {
                float vol = Float.parseFloat(volume);
                updateSpeakerVolume(response, speakerId, vol);
            }
        } else if (pathInfo.startsWith("/startApp")) {
            startApplication(response);
        } else if (pathInfo.startsWith("/selectApplicationAudioSource")) {
            String sourceId = req.getParameter("id");
            selectApplicationAudioSource(sourceId, response);
        } else if (pathInfo.startsWith("/playpause")) {
            Map<String, Object> runningStatus = getRunningStatus();
            ApplicationSupport applicationSupport = getCurrentApplicationSupport(runningStatus, musicPlayer);
            if (applicationSupport != null) {
                if (preferences.shouldBounceAirfoilOnPlayPause()) {
                    bounceAirfoil();
                }
                applicationSupport.playPause();
            }
            appendRunningStatus(response, runningStatus);

        } else if (pathInfo.startsWith("/next")) {
            Map<String, Object> runningStatus = getRunningStatus();
            ApplicationSupport applicationSupport = getCurrentApplicationSupport(runningStatus, musicPlayer);
            if (applicationSupport != null) {
                applicationSupport.next();
            }
            appendRunningStatus(response, runningStatus);

        } else if (pathInfo.startsWith("/previous")) {
            Map<String, Object> runningStatus = getRunningStatus();
            ApplicationSupport applicationSupport = getCurrentApplicationSupport(runningStatus, musicPlayer);
            if (applicationSupport != null) {
                applicationSupport.previous();
            }
            appendRunningStatus(response, runningStatus);
        } else if (pathInfo.startsWith("/thumbsup")) {
            Map<String, Object> runningStatus = getRunningStatus();
            ApplicationSupport applicationSupport = getCurrentApplicationSupport(runningStatus, musicPlayer);
            if (applicationSupport != null) {
                applicationSupport.thumbsUp();
            }
            appendRunningStatus(response, runningStatus);

        } else if (pathInfo.startsWith("/thumbsdown")) {
            Map<String, Object> runningStatus = getRunningStatus();
            ApplicationSupport applicationSupport = getCurrentApplicationSupport(runningStatus, musicPlayer);
            if (applicationSupport != null) {
                applicationSupport.thumbsDown();
            }
            appendRunningStatus(response, runningStatus);
        } else if (pathInfo.startsWith("/bounce")) {
            bounceAirfoil();
            appendRunningStatus(response, getRunningStatus());
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void bounceAirfoil() {
        appleScriptTemplate.execute(Application.AIRFOIL, "disconnect from every speaker");
        appleScriptTemplate.execute(Application.AIRFOIL, "quit");
        String[] script = {
                "delay 1",
                "activate application \"Airfoil\""
        };
        appleScriptTemplate.executeBare(script);
    }

    private ApplicationSupport getCurrentApplicationSupport(Map<String, Object> runningStatus, MusicPlayer musicPlayer) {
        Map<String, Object> state = (Map<String, Object>) runningStatus.get(STATE_KEY);
        Map<String, Object> currentSource = (Map<String, Object>) state.get(CURRENT_SOURCE_KEY);
        String sourceName = (String) currentSource.get(NAME_KEY);
        Application currentApplication = Application.forName(sourceName);
        return currentApplication.getApplicationSupport(appleScriptTemplate, musicPlayer);
    }

    private void selectApplicationAudioSource(String sourceId, HttpServletResponse response) throws IOException {
        appleScriptTemplate.execute(Application.AIRFOIL,
                "try",
                "   set current audio source to item 1 of (every application source whose id is " + sourceId + ")",
                "on error",
                "   set current audio source to item 1 of (every system source whose id is " + sourceId + ")",
                "end try");
        appendRunningStatus(response, getRunningStatus());
    }

    private void appendRunningStatus(HttpServletResponse response, Map<String, Object> runningStatus) throws IOException {
        response.getWriter().append(new Gson().toJson(runningStatus));
    }

    private void startApplication(HttpServletResponse response) throws IOException {
        appleScriptTemplate.startApplication(Application.AIRFOIL);
        appendRunningStatus(response, getRunningStatus());
    }

    private void updateSpeakerVolume(HttpServletResponse response, String speakerId, float vol) throws IOException {
        String setScript = "set the volume of (every speaker whose id is \"" + speakerId + "\") to " + vol + "";
        appleScriptTemplate.execute(Application.AIRFOIL, setScript);
        appendRunningStatus(response, getRunningStatus());
    }

    private void updateSpeakerStatus(HttpServletResponse response, String speakerId, boolean connect) throws IOException {
        if (connect) {
            appleScriptTemplate.execute(Application.AIRFOIL, "connect to item 1 of (every speaker whose id is \"" + speakerId + "\")");
        } else {
            appleScriptTemplate.execute(Application.AIRFOIL, "disconnect from item 1 of (every speaker whose id is \"" + speakerId + "\")");
        }
        appendRunningStatus(response, getRunningStatus());
    }

    private void appendStatus(HttpServletResponse response) throws IOException {
        boolean isRunning = appleScriptTemplate.isRunning(Application.AIRFOIL);

        if (!isRunning) {
            response.getWriter().append("{\"app\":\"").append(Application.AIRFOIL.getName()).append("\",\"state\":{\"running\":false}}");
        } else {
            appendRunningStatus(response, getRunningStatus());
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private Map<String, Object> getRunningStatus() throws IOException {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("app", Application.AIRFOIL.getName());
        Map<String, Object> stateMap = new HashMap<String, Object>();
        stateMap.put("running", true);

        //todo this blows up if there is no "current audio source"...
        List data = appleScriptTemplate.execute(Application.AIRFOIL, "[get properties of every speaker, get properties of every application source, get properties of every system source, get properties of current audio source]");

        stateMap.put("speakers", data.get(0));
        List<Map<String, Object>> sources = (List<Map<String, Object>>) data.get(1);
        sources.addAll((List) data.get(2));
        stateMap.put("appSources", sources);
        Collections.sort(sources, new Comparator<Map<String, Object>>() {
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Collator collator = Collator.getInstance();
                String name1 = (String) o1.get(NAME_KEY);
                String name2 = (String) o2.get(NAME_KEY);
                return collator.compare(name1, name2);
            }
        });
        Map<String, Object> currentSource = (Map<String, Object>) data.get(3);
        String sourceName = (String) currentSource.get(NAME_KEY);
        Application currentApplication = Application.forName(sourceName);
        currentSource.put("playPause", currentApplication.hasPlayPauseSupport());
        currentSource.put("previous", currentApplication.hasPreviousSupport());
        currentSource.put("next", currentApplication.hasNextSupport());
        currentSource.put("thumbsUp", currentApplication.hasThumbsUpSupport());
        currentSource.put("thumbsDown", currentApplication.hasThumbsDownSupport());

        stateMap.put(CURRENT_SOURCE_KEY, currentSource);
        stateMap.put("version", ControlServlet.CURRENT_VERSION);
        output.put(STATE_KEY, stateMap);

        return output;
    }

}
