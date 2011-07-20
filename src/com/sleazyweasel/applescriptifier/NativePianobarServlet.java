package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class NativePianobarServlet extends HttpServlet {
    private final NativePianobarSupport pianobarSupport;

    private AtomicReference<PianobarState> pianobarState = new AtomicReference<PianobarState>(new PianobarState(false, "", "", "", "", NativePianobarSupport.InputType.NONE, new HashMap<Integer, String>(), "", "", false, ""));

    public NativePianobarServlet(NativePianobarSupport pianobarSupport) {
        this.pianobarSupport = pianobarSupport;
    }

    public void init() {
        System.out.println("NativePianobarServlet.init");
        pianobarSupport.addListener(new NativePianobarSupport.PianobarStateChangeListener() {
            public void stateChanged(NativePianobarSupport pianobarSupport, PianobarState state) {
                pianobarState.set(state);
            }
        });
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
        if (pathInfo.startsWith("/airfoilstatusdata")) {
            PrintWriter writer = response.getWriter();
            PianobarState pianobarState = this.pianobarState.get();
            writer.println(pianobarState.getAlbumArtUrl());
            writer.println(pianobarState.getTitle());
            writer.println(pianobarState.getArtist());
            writer.println(pianobarState.getAlbum());
            writer.println(pianobarState.getDuration());
            writer.println();
            writer.flush();
            return;
        } else if (pathInfo.startsWith("/playpause")) {
            pianobarSupport.playPause();
            sleep();
        } else if (pathInfo.startsWith("/thumbsup")) {
            pianobarSupport.thumbsUp();
            sleep();
        } else if (pathInfo.startsWith("/thumbsdown")) {
            pianobarSupport.thumbsDown();
            sleep();
        } else if (pathInfo.startsWith("/next")) {
            pianobarSupport.next();
            sleep();
        } else if (pathInfo.startsWith("/keyStroke")) {
            pianobarSupport.sendKeyStroke(req.getParameter("key").charAt(0));
            sleep();
        } else if (pathInfo.startsWith("/bounce")) {
            pianobarSupport.kill();
            sleep();
        } else if (pathInfo.startsWith("/volumeUp")) {
            pianobarSupport.volumeUp();
            sleep();
        } else if (pathInfo.startsWith("/volumeDown")) {
            pianobarSupport.volumeDown();
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
        } else if (pathInfo.startsWith("/selectStation")) {
            String stationId = req.getParameter("id");
            pianobarSupport.selectStation(Integer.valueOf(stationId));
        } else if (pathInfo.startsWith("/albumArt")) {
            populateResponseDataFromFile(new HashMap<String, Object>());

            Map<String, String> responseData = new HashMap<String, String>(1);
            responseData.put("albumArtUrl", pianobarState.get().getAlbumArtUrl());
            response.getWriter().append(new Gson().toJson(responseData));
            return;
        }

        appendStatus(response);
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
        PianobarState state = pianobarState.get();

        boolean inputRequested = state.isInputRequested();

        responseData.put("station", state.getStation());
        responseData.put("artist", state.getArtist());
        responseData.put("album", state.getAlbum());
        responseData.put("title", state.getTitle());
        responseData.put("heart", state.isCurrentSongIsLoved() ? "YES" : "NO");
        responseData.put("playing", state.isPlaying() ? "YES" : "NO");

        if (inputRequested && state.getInputTypeRequested().equals(NativePianobarSupport.InputType.CHOOSE_STATION)) {
            Map<Integer, String> stations = state.getStations();
            responseData.put("stations", stations);
            responseData.put("inputType", "stationSelection");
        }
        responseData.put("inputRequested", inputRequested ? "YES" : "NO");
    }


    private void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException("interrupted");
        }
    }

    private void sendTextCommand(String command) {
        pianobarSupport.sendTextCommand(command);
    }

}
