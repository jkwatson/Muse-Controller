package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MuseServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(MuseServlet.class.getName());
    private final AppleScriptTemplate appleScriptTemplate = new AppleScriptTemplateFactory().getActiveTemplate();
    private final MuseSupport museSupport = new MuseSupport(appleScriptTemplate);

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
        String pathInfo = req.getPathInfo();
        if (pathInfo.startsWith("/status")) {
            appendStatus(response);
        } else if (pathInfo.startsWith("/playpause")) {
            museSupport.playPause();
            appendStatus(response);
        } else if (pathInfo.startsWith("/next")) {
            museSupport.next();
            appendStatus(response);
        } else if (pathInfo.startsWith("/coverArt")) {
            Object image = appleScriptTemplate.execute(Application.MUSE(), "get cover art");
            response.setContentType("image/png");
            if (image instanceof BufferedImage) {
                BufferedImage thing = (BufferedImage) image;
                ImageIO.write(thing, "PNG", response.getOutputStream());
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void appendStatus(HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> status = new HashMap<String, Object>();

        List<String> currentTrack;
        try {
            currentTrack = appleScriptTemplate.execute(Application.MUSE(), "get [track title, artist, album]");
        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught.", e);
            currentTrack = Arrays.asList("", "", "");
        }

        status.put("currentTrack", currentTrack);
        status.put("version", ControlServlet.CURRENT_VERSION);
        response.getWriter().append(new Gson().toJson(status));

        response.setStatus(HttpServletResponse.SC_OK);
    }

}
