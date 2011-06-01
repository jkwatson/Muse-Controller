package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativePianobarServlet extends HttpServlet {
    private final NativePianobarSupport pianobarSupport;

    public NativePianobarServlet(NativePianobarSupport pianobarSupport) {
        this.pianobarSupport = pianobarSupport;
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
        if (pathInfo.startsWith("/status")) {
        } else if (pathInfo.startsWith("/playpause")) {
            pianobarSupport.playPause();
        } else if (pathInfo.startsWith("/thumbsup")) {
            pianobarSupport.thumbsUp();
            pianobarSupport.sendKeyStroke('i');
        } else if (pathInfo.startsWith("/thumbsdown")) {
            pianobarSupport.thumbsDown();
        } else if (pathInfo.startsWith("/next")) {
            pianobarSupport.next();
        } else if (pathInfo.startsWith("/keyStroke")) {
            pianobarSupport.sendKeyStroke(req.getParameter("key").charAt(0));
            sleep();
        } else if (pathInfo.startsWith("/textEntry")) {
            String text = req.getParameter("text");
            //trim the user data to protect against buffer overflows in pianobar.
            if (text.length() > 100) {
                text = text.substring(0, 100);
            }
            if (text != null && text.length() != 0) {
                if (text.length() == 1 && !Character.isDigit(text.charAt(0))) {
                    pianobarSupport.sendKeyStroke(text.charAt(0));
                } else {
                    sendTextCommand(text);
                }
            } else {
                sendTextCommand("\n");
            }
            sleep();
        } else if (pathInfo.startsWith("/albumArt")) {
            String url = "http://url";
            Map<String, String> responseData = new HashMap<String, String>(1);

            Map<String, Object> currentData = new HashMap<String, Object>();
            populateResponseDataFromFile(currentData);

            String title = (String) currentData.get("title");
            if (title != null && title.length() > 0) {
                url = getAlbumArtUrl();
            }

            responseData.put("albumArtUrl", url);
            response.getWriter().append(new Gson().toJson(responseData));
            return;
        }

        appendStatus(response);
    }

    private String getAlbumArtUrl() {
        //todo consider doing something along the lines below to try to dig for other images.
//        valueFromDataFile = valueFromDataFile.replace("130W", "500W");
//        valueFromDataFile = valueFromDataFile.replace("130H", "434H");
        return getValueFromDataFile("coverArt=");
    }

    private String getValueFromDataFile(String key) {
        List<String> dataFromFile = pianobarSupport.getDataFromFile();
        return getValueFromDataFile(key, dataFromFile);
    }

    private String getValueFromDataFile(String key, List<String> dataFromFile) {
        for (String line : dataFromFile) {
            if (line.startsWith(key)) {
                return line.substring(line.indexOf(key) + key.length());
            }
        }
        return "";
    }

    private void appendStatus(HttpServletResponse response) throws IOException {
        appendStatus(response, new HashMap<String, Object>());
    }

    private void appendStatus(HttpServletResponse response, Map<String, Object> responseData) throws IOException {
        populateResponseDataFromFile(responseData);
        response.getWriter().append(new Gson().toJson(responseData));
    }

    private void populateResponseDataFromFile(Map<String, Object> responseData) {
        pianobarSupport.activatePianoBar();

        String currentScreenContents = pianobarSupport.getCurrentScreenContents();
        String[] lines = currentScreenContents.split("\n");
        boolean inputRequested = "YES".equals(extractInputRequested(currentScreenContents));

        responseData.put("screen", currentScreenContents);

        List<String> pianobarData = pianobarSupport.getDataFromFile();

        responseData.put("station", extractStation(pianobarData));
        responseData.put("artist", extractArtist(pianobarData));
        responseData.put("album", extractAlbum(pianobarData));
        responseData.put("title", extractTitle(pianobarData));
        responseData.put("heart", extractHeart(pianobarData));

        if (inputRequested) {
            String lastLine = lines[lines.length - 1];
            if (lastLine.startsWith("[?] Select station:")) {
                Map<Integer, String> stations = parseStationList(pianobarData);
                responseData.put("stations", stations);
                responseData.put("inputType", "stationSelection");
            }
        }
        responseData.put("inputRequested", inputRequested ? "YES" : "NO");
    }

    Map<Integer, String> parseStationList(List<String> pianobarData) {
        Map<Integer, String> stations = new HashMap<Integer, String>();

        for (String dataLine : pianobarData) {
            if (dataLine.startsWith("station") && !dataLine.startsWith("stationCount") && !dataLine.startsWith("stationName")) {
                String stationNumber = dataLine.substring(dataLine.indexOf("station") + 7, dataLine.indexOf("="));
                String stationName = dataLine.substring(dataLine.indexOf("=") + 1);
                stations.put(Integer.valueOf(stationNumber), stationName);
            }
        }
        return stations;
    }


    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted");
        }
    }

    String extractStation(List<String> data) {
        return getValueFromDataFile("stationName=", data);
    }

    String extractTitle(List<String> data) {
        return getValueFromDataFile("title=", data);
    }

    String extractAlbum(List<String> data) {
        return getValueFromDataFile("album=", data);
    }

    String extractArtist(List<String> data) {
        return getValueFromDataFile("artist=", data);
    }

    String extractHeart(List<String> data) {
        String rating = getValueFromDataFile("rating=", data);
        return rating.equals("1") ? "YES" : "NO";
    }

    String extractInputRequested(String screenContents) {
        String[] lines = screenContents.split("\\n");
        String lastLine = lines[lines.length - 1];
        return lastLine.startsWith("[?]") ? "YES" : "NO";
    }

    private void sendTextCommand(String command) {
        pianobarSupport.sendTextCommand(command);
    }

}
