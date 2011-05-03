package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.imageio.ImageIO;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class PulsarServlet extends HttpServlet {

    private final AppleScriptTemplate appleScriptTemplate = new AppleScriptTemplateImpl();
    private final PulsarSupport pulsarSupport = new PulsarSupport(appleScriptTemplate);

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
        else if (pathInfo.startsWith("/playpause")) {
            pulsarSupport.playPause();
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/next")) {
            pulsarSupport.next();
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/previous")) {
            pulsarSupport.previous();
            appendStatus(response);
        }
        else if (pathInfo.startsWith("/stationlogo")) {
            Object image;
            try {
                image = appleScriptTemplate.execute(Application.PULSAR, "get raw channel logo");
            } catch (Exception e) {
                image = appleScriptTemplate.execute(Application.PULSAR, "get channel logo");
            }
            if (image instanceof BufferedImage) {
                BufferedImage thing = (BufferedImage) image;
                ImageIO.write(thing, "PNG", response.getOutputStream());
            }

            response.setStatus(HttpServletResponse.SC_OK);
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void appendStatus(HttpServletResponse response) throws IOException {
        Map<String, Object> status = new HashMap<String, Object>();
        String currentStation;
        try {
            currentStation = appleScriptTemplate.execute(Application.PULSAR, "get channel name");
            if (currentStation.contains("<NSAppleEventDescriptor")) {
                currentStation = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentStation = "Unable to get Station from Pulsar";
        }
//        System.out.println("currentStation = " + currentStation);
        status.put("currentStation", currentStation);


        List<String> currentTrack;
        try {
            currentTrack = appleScriptTemplate.execute(Application.PULSAR, "get [track title, artist]");
        } catch (Exception e) {
            e.printStackTrace();
            currentTrack = Arrays.asList("", "");
        }

        status.put("currentTrack", currentTrack);
        status.put("version", ControlServlet.CURRENT_VERSION);
        response.getWriter().append(new Gson().toJson(status));

        response.setStatus(HttpServletResponse.SC_OK);
    }

}
