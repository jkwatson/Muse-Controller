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

public class MusicPlayerServlet extends HttpServlet {
    private final MusicPlayer musicPlayer;

    private AtomicReference<MusicPlayerState> musicPlayerState = new AtomicReference<MusicPlayerState>(new MusicPlayerState(false, "", "", "", "", MusicPlayerInputType.NONE, new HashMap<Integer, String>(), "", "", false, "", 1.0));

    public MusicPlayerServlet(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    public void init() {
        musicPlayer.addListener(new MusicPlayer.MusicPlayerStateChangeListener() {
            public void stateChanged(MusicPlayer player, MusicPlayerState state) {
                musicPlayerState.set(state);
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
            MusicPlayerState musicPlayerState = this.musicPlayerState.get();
            writer.println(musicPlayerState.getAlbumArtUrl());
            writer.println(musicPlayerState.getTitle());
            writer.println(musicPlayerState.getArtist());
            writer.println(musicPlayerState.getAlbum());
            writer.println(musicPlayerState.getDuration());
            writer.println();
            writer.flush();
            return;
        } else if (pathInfo.startsWith("/playpause")) {
            musicPlayer.playPause();
            sleep();
        } else if (pathInfo.startsWith("/thumbsup")) {
            musicPlayer.thumbsUp();
            sleep();
        } else if (pathInfo.startsWith("/thumbsdown")) {
            musicPlayer.thumbsDown();
            sleep();
        } else if (pathInfo.startsWith("/next")) {
            musicPlayer.next();
            sleep();
        } else if (pathInfo.startsWith("/keyStroke")) {
            char key = req.getParameter("key").charAt(0);
            if (key == 's') {
                musicPlayer.askToChooseStation();
                sleep();
            }
        } else if (pathInfo.startsWith("/bounce")) {
            musicPlayer.bounce();
            sleep();
        } else if (pathInfo.startsWith("/volumeUp")) {
            musicPlayer.volumeUp();
            sleep();
        } else if (pathInfo.startsWith("/volumeDown")) {
            musicPlayer.volumeDown();
            sleep();
        } else if (pathInfo.startsWith("/textEntry")) {
            //todo deprecate this command altogether, as it's a giant hack.
            String text = req.getParameter("text");
            //trim the user data to protect against possible buffer overflows in pianobar.
            if (text.length() > 100) {
                text = text.substring(0, 100);
            }
            if (text != null && text.length() != 0) {
                //the only time we ever do this is from the client is to enter "" to cancel station selection.
                //so do nothing for now...?
//                if (text.length() == 1 && !Character.isDigit(text.charAt(0))) {
//                    musicPlayer.sendKeyStroke(text.charAt(0));
//                } else {
//                    sendTextCommand(text);
//                }
            } else {
                musicPlayer.cancelStationSelection();
            }
            sleep();
        } else if (pathInfo.startsWith("/selectStation")) {
            String stationId = req.getParameter("id");
            musicPlayer.selectStation(Integer.valueOf(stationId));
        } else if (pathInfo.startsWith("/albumArt")) {
            populateResponseDataFromFile(new HashMap<String, Object>());

            Map<String, String> responseData = new HashMap<String, String>(1);
            responseData.put("albumArtUrl", musicPlayerState.get().getAlbumArtUrl());
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
        musicPlayer.activate();
        MusicPlayerState state = musicPlayerState.get();

        boolean inputRequested = state.isInputRequested();
        responseData.put("station", state.getStation());
        responseData.put("artist", state.getArtist());
        responseData.put("album", state.getAlbum());
        responseData.put("title", state.getTitle());
        responseData.put("heart", state.isCurrentSongIsLoved() ? "YES" : "NO");
        responseData.put("playing", state.isPlaying() ? "YES" : "NO");
        if (inputRequested && state.getInputTypeRequested().equals(MusicPlayerInputType.CHOOSE_STATION)) {
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

//    private void sendTextCommand(String command) {
//        musicPlayer.sendTextCommand(command);
//    }

}
