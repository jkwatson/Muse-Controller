package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SpotifyServlet extends HttpServlet {

    private final AppleScriptTemplate appleScriptTemplate = new AppleScriptTemplateFactory().getActiveTemplate();
    private final SpotifySupport spotifySupport = new SpotifySupport(appleScriptTemplate);

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

        if (pathInfo.startsWith("/playpause")) {
            spotifySupport.playPause();
        }
        else if (pathInfo.startsWith("/previous")) {
            spotifySupport.previous();
        }
        else if (pathInfo.startsWith("/next")) {
            spotifySupport.next();
        }

        response.setStatus(HttpServletResponse.SC_OK);
        appendStatus(response);
    }


    private void appendStatus(HttpServletResponse response) throws IOException {
        response.getWriter().append(new Gson().toJson(spotifySupport.getStatus()));
    }
}
