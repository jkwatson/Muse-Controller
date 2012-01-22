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

public class RdioServlet extends HttpServlet {

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
            appleScriptTemplate.execute(Application.RDIO, imageCommand);
            BufferedImage image = ImageIO.read(new File("/tmp/rdio.tiff"));
            response.setContentType("image/png");
            ImageIO.write(image, "PNG", response.getOutputStream());
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void appendStatus(HttpServletResponse response) throws IOException {
        Map<String, Object> status = new HashMap<String, Object>();

        Map<String, Object> currentTrack = new HashMap<String, Object>();
        Map<String, Object> playerState = new HashMap<String, Object>();
        try {
            List<List<String>> data = appleScriptTemplate.execute(Application.RDIO, "[get [name, artist, album, duration, rdio url] of current track, get [player position, player state as string, sound volume]]");
            List<String> trackData = data.get(0);
            currentTrack.put("title", trackData.get(0));
            currentTrack.put("artist", trackData.get(1));
            currentTrack.put("album", trackData.get(2));
            currentTrack.put("duration", trackData.get(3));
            currentTrack.put("rdioUrl", trackData.get(4));

            List<String> playerData = data.get(1);
            playerState.put("position", playerData.get(0));
            playerState.put("playing", playerData.get(1).equalsIgnoreCase("playing") ? "YES" : "NO");
            playerState.put("volume", playerData.get(2));

        } catch (Exception e) {
            e.printStackTrace();
            currentTrack.put("title", "");
            currentTrack.put("artist", "");
            currentTrack.put("album", "");
            currentTrack.put("duration", "");
            currentTrack.put("rdioUrl", "");
            playerState.put("position", "");
            playerState.put("playing", "NO");
            playerState.put("volume", "");
        }

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
