package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PandoraBoyServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(PandoraBoyServlet.class.getName());
    private static final String QUICK_MIX_STATION_CODE = "QuickMix";
    private final AppleScriptTemplate appleScriptTemplate = new AppleScriptTemplateFactory().getActiveTemplate();
    private final PandoraBoySupport pandoraBoySupport = new PandoraBoySupport(appleScriptTemplate);

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
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/setStation")) {
            String station = req.getParameter("station");
            setStation(station);
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/playpause")) {
            pandoraBoySupport.playPause();
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/next")) {
            pandoraBoySupport.next();
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/thumbsUp")) {
            pandoraBoySupport.thumbsUp();
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/thumbsDown")) {
            pandoraBoySupport.thumbsDown();
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/create")) {
            String stationName = req.getParameter("station");
            appleScriptTemplate.execute(Application.PANDORABOY, "create station \"" + stationName + "\"");
        }
        else if (pathInfo.startsWith("/reset")) {
            appleScriptTemplate.executeKeyStrokeWithCommandKey(Application.PANDORABOY, "r");
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void setStation(String station) {
        if (QUICK_MIX_STATION_CODE.equals(station)) {
            appleScriptTemplate.execute(Application.PANDORABOY, "set current station to (quickmix station)");
        }
        else {
            appleScriptTemplate.execute(Application.PANDORABOY, "set current station to item 1 of (every station whose name is \"" + station+ "\")");
        }
    }

    private void appendStatus(HttpServletResponse response) throws IOException {
        Map<String, Object> status = new HashMap<String, Object>();
        Object currentStation;
        try {
            currentStation = appleScriptTemplate.execute(Application.PANDORABOY, "get name of current station");
            if (((String)currentStation).contains(QUICK_MIX_STATION_CODE)) {
                currentStation = QUICK_MIX_STATION_CODE;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            currentStation = "Unable to get Station from PandoraBoy";
        }
        status.put("currentStation", currentStation);


        List<String> currentTrack;
        try {
            currentTrack = appleScriptTemplate.execute(Application.PANDORABOY, "get [name of current track, artist of current track]");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught.", e);;
            currentTrack = Arrays.asList("", "");
        }
        status.put("currentTrack", currentTrack);
        LinkedHashSet<String> stations = new LinkedHashSet<String>(appleScriptTemplate.<List<String>>execute(Application.PANDORABOY, "get name of every station"));
        stations.add(QUICK_MIX_STATION_CODE);
        status.put("stations", stations);
        String uglyStuff = appleScriptTemplate.execute(Application.PANDORABOY, "get player state");
        status.put("status", uglyStuff.contains("play") ? "playing" : "stopped");
        status.put("version", ControlServlet.CURRENT_VERSION);
        String json = new Gson().toJson(status);
        response.getWriter().append(json);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}
