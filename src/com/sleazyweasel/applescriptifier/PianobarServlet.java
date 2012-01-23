package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PianobarServlet extends HttpServlet {
    private final AppleScriptTemplate appleScriptTemplate = new AppleScriptTemplateFactory().getActiveTemplate();
    private final PianobarSupport pianobarSupport = new PianobarSupport(appleScriptTemplate);

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
        if (pathInfo.startsWith("/status")) {
        } else if (pathInfo.startsWith("/playpause")) {
            pianobarSupport.playPause();
        } else if (pathInfo.startsWith("/thumbsup")) {
            pianobarSupport.thumbsUp();
            appleScriptTemplate.executeKeyStroke(Application.MUSECONTROLLER, "i");
        } else if (pathInfo.startsWith("/thumbsdown")) {
            pianobarSupport.thumbsDown();
        } else if (pathInfo.startsWith("/next")) {
            pianobarSupport.next();
        } else if (pathInfo.startsWith("/keyStroke")) {
            appleScriptTemplate.executeKeyStroke(Application.MUSECONTROLLER, req.getParameter("key"));
        } else if (pathInfo.startsWith("/textEntry")) {
            String text = req.getParameter("text");
            if (text != null) {
                if (text.length() == 1 && !Character.isDigit(text.charAt(0))) {
                    appleScriptTemplate.executeKeyStroke(Application.MUSECONTROLLER, text);
                } else {
                    sendTextCommand(text);
                }
            }
        }
        appendStatus(response);
    }

    private void appendStatus(HttpServletResponse response) throws IOException {
        appendStatus(response, new HashMap<String, Object>());
    }

    private void appendStatus(HttpServletResponse response, Map<String, Object> responseData) throws IOException {
        pianobarSupport.activatePianoBar();

        String currentScreenContents = pianobarSupport.getCurrentScreenContents(appleScriptTemplate);
        String[] lines = currentScreenContents.split("\n");
        boolean inputRequested = "YES".equals(extractInputRequested(currentScreenContents));

        int lastStationEntryPoint = currentScreenContents.lastIndexOf("|>  Station");
        if (lastStationEntryPoint < 0 && !inputRequested) {
            sendTextCommand("i");
            currentScreenContents = pianobarSupport.getCurrentScreenContents(appleScriptTemplate);
            lastStationEntryPoint = currentScreenContents.lastIndexOf("|>  Station");
        }
        responseData.put("screen", currentScreenContents);
        if (lastStationEntryPoint >= 0) {
            currentScreenContents = currentScreenContents.substring(lastStationEntryPoint);
        }

        if (lastStationEntryPoint < 0 || currentScreenContents.contains("No song playing.")) {
            responseData.put("artist", "");
            responseData.put("album", "");
            responseData.put("station", "");
            responseData.put("title", "");
            responseData.put("heart", "NO");
        } else {
            responseData.put("station", extractStation(currentScreenContents));
            responseData.put("artist", extractArtist(currentScreenContents));
            responseData.put("album", extractAlbum(currentScreenContents));
            responseData.put("title", extractTitle(currentScreenContents));
            responseData.put("heart", extractHeart(currentScreenContents));
        }

        if (inputRequested) {
            String lastLine = lines[lines.length - 1];
            if (lastLine.startsWith("[?] Select station:")) {
                int beginIndex = currentScreenContents.lastIndexOf(" 0)");
                Map<Integer, String> stations;
                if (beginIndex >= 0) {
                    stations = parseStationList(currentScreenContents.substring(beginIndex));
                } else {
                    stations = Collections.emptyMap();
                }
                responseData.put("stations", stations);
                responseData.put("inputType ", "stationSelection");
            }
        }

        responseData.put("inputRequested", inputRequested ? "YES" : "NO");
        response.getWriter().append(new Gson().toJson(responseData));
    }

    String extractStation(String nowPlaying) {
        int stationLocation = nowPlaying.lastIndexOf("Station \"");
        int parenLocation = nowPlaying.indexOf("(");
        if (stationLocation < 0 || parenLocation < 0) {
            return "";
        }
        return nowPlaying.substring(stationLocation + 9, parenLocation - 2);
    }

    String extractTitle(String nowPlaying) {
        int albumLocation = nowPlaying.lastIndexOf("|>  \"");
        int end = nowPlaying.lastIndexOf("\" by \"");
        if (albumLocation < 0 || end < 0) {
            return "";
        }
        return nowPlaying.substring(albumLocation + 5, end);
    }

    String extractAlbum(String nowPlaying) {
        int albumLocation = nowPlaying.lastIndexOf(" on \"");
        int end = nowPlaying.lastIndexOf("\"");
        if (albumLocation < 0 || end < 0) {
            return "";
        }
        return nowPlaying.substring(albumLocation + 5, end);
    }

    String extractArtist(String nowPlaying) {
        int albumLocation = nowPlaying.lastIndexOf(" by \"");
        int end = nowPlaying.lastIndexOf("\" on \"");
        if (albumLocation < 0 || end < 0) {
            return "";
        }
        return nowPlaying.substring(albumLocation + 5, end);
    }

    String extractHeart(String nowPlaying) {
        int heartLocation = nowPlaying.lastIndexOf(" on \"");
        if (heartLocation < 0) {
            return "NO";
        }
        String endOfSongInfo = nowPlaying.substring(heartLocation);
        int end = endOfSongInfo.lastIndexOf("<3");
        return end > 0 ? "YES" : "NO";
    }

    String extractInputRequested(String screenContents) {
        String[] lines = screenContents.split("\\n");
        String lastLine = lines[lines.length - 1];
        return lastLine.startsWith("[?]") ? "YES" : "NO";
    }

    private void sendTextCommand(String command) {
        appleScriptTemplate.execute(Application.MUSECONTROLLER, "tell current terminal", "tell current session", "write text \"" + command + "\"", "end tell", "end tell");
    }

    Map<Integer, String> parseStationList(String stationData) {
        Map<Integer, String> results = new LinkedHashMap<Integer, String>();
        String[] lines = stationData.split("\n");
        for (String line : lines) {
            if (line.startsWith("(i)") || line.startsWith("[?]")) {
                continue;
            }

            Pattern pattern = Pattern.compile("\\s+(\\d+)\\)\\s+[Q|S]?\\s+(\\w.*)$");
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                Integer key = Integer.valueOf(matcher.group(1));
                String stationName = matcher.group(2);
                results.put(key, stationName);
            }
        }
        return results;
    }
}
