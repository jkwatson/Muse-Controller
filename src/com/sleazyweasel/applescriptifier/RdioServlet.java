package com.sleazyweasel.applescriptifier;

import com.google.gson.Gson;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RdioServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(RdioServlet.class.getName());
    private final AppleScriptTemplate appleScriptTemplate = new AppleScriptTemplateFactory().getActiveTemplate();
    private final RdioSupport rdioSupport = new RdioSupport(appleScriptTemplate);

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
        } else if (pathInfo.startsWith("/playpause")) {
            rdioSupport.playPause();
            appendStatus(response);
        } else if (pathInfo.startsWith("/next")) {
            rdioSupport.next();
            appendStatus(response);
        } else if (pathInfo.startsWith("/previous")) {
            rdioSupport.previous();
            appendStatus(response);
        } else if (pathInfo.startsWith("/artwork")) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                Thread.interrupted();
                return;
            }
            appleScriptTemplate.execute(Application.RDIO(), imageCommand);
            BufferedImage image = ImageIO.read(new File("/tmp/rdio.tiff"));
            response.setContentType("image/png");
            ImageIO.write(image, "PNG", response.getOutputStream());
            response.setStatus(HttpServletResponse.SC_OK);
        } else if (pathInfo.startsWith("/setVolume")) {
            String value = req.getParameter("value");
            Integer volume = Integer.valueOf(value);
            rdioSupport.setVolume(volume);
            appendStatus(response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void appendStatus(HttpServletResponse response) throws IOException {

        Map<String, Object> currentTrack = new HashMap<String, Object>();
        Map<String, Object> playerState = new HashMap<String, Object>();
        try {
            List<List<Object>> data = appleScriptTemplate.execute(Application.RDIO(), "[get [name, artist, album, duration, rdio url] of current track, get [player position, sound volume]]");
            List<Object> trackData = data.get(0);
            currentTrack.put("title", trackData.get(0));
            currentTrack.put("artist", trackData.get(1));
            currentTrack.put("album", trackData.get(2));
            currentTrack.put("duration", trackData.get(3));
            currentTrack.put("rdioUrl", trackData.get(4));

            List<Object> playerData = data.get(1);
            Double position = (Double) playerData.get(0);
            playerState.put("playerPosition", position.isNaN() || position.isInfinite() ? 0 : position);
            playerState.put("volume", playerData.get(1));

        } catch (Exception e) {
            logger.log(Level.WARNING, "Exception caught.", e);
            ;
            List<List<Object>> data = appleScriptTemplate.execute(Application.RDIO(), "[get [player position, sound volume]]");
            currentTrack.put("title", "");
            currentTrack.put("artist", "");
            currentTrack.put("album", "");
            currentTrack.put("duration", "");
            currentTrack.put("rdioUrl", "");
            List<Object> playerData = data.get(0);
//            Double position = (Double) playerData.get(0);
            playerState.put("playerPosition", 0);
            playerState.put("volume", playerData.get(1));
        }
        try {
            String running = appleScriptTemplate.execute(Application.RDIO(), "get player state as string");
            playerState.put("playing", "playing".equalsIgnoreCase(running) ? "YES" : "NO");
        } catch (Exception e) {
            playerState.put("playing", "NO");
        }

        Map<String, Object> status = new HashMap<String, Object>();
        status.put("currentTrack", currentTrack);
        status.put("playerState", playerState);
        status.put("version", ControlServlet.CURRENT_VERSION);
        response.getWriter().append(new Gson().toJson(status));

        response.setStatus(HttpServletResponse.SC_OK);
    }

    //this has got to be the *hardest* possible way to accomplish this.
    private static final String[] imageCommand = {
            "set imagedata to artwork of current track",
            "set the_file to \"/tmp/rdio.tiff\"",
            "try",
            "	open for access the_file with write permission",
            "	set eof of the_file to 0",
            "	write (imagedata) to the_file starting at eof",
            "	close access the_file",
            "on error",
            "	try",
            "		close access the_file",
            "	end try",
            "end try"
    };

}
