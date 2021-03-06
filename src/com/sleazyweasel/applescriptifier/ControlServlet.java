package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class ControlServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(ControlServlet.class.getName());
    public static final String CURRENT_VERSION = "2.0";

    private AppleScriptTemplate appleScriptTemplate = new AppleScriptTemplateFactory().getActiveTemplate();

    private List<String> applications = new ArrayList<String>();
    private final MusicPlayer musicPlayer;

    public ControlServlet(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        updateApplications();

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
        if (pathInfo.startsWith("/apps")) {
            Map<String, Object> results = new HashMap<String, Object>();
            results.put("version", CURRENT_VERSION);
            results.put("supportedApplications", applications);
            response.getWriter().append(new Gson().toJson(results));
        }
    }

    private synchronized void updateApplications() {
        List<Application> possibleApplications = Application.values();
        for (Application application : possibleApplications) {
            if (applications.contains(application.displayName()) || !application.fullSupport()) {
                continue;
            }
            logger.info("checking: " + application);
            if (Application.MUSECONTROLLER().equals(application)) {
                if (musicPlayer.isConfigured()) {
                    logger.info("adding: " + application);
                    applications.add(Application.MUSECONTROLLER().displayName());
                }
            } else {
                if (appleScriptTemplate.applicationExists(application)) {
                    logger.info("adding: " + application);
                    applications.add(application.displayName());
                }
            }
        }
    }

}
