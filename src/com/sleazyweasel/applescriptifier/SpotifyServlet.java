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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        } else if (pathInfo.startsWith("/previous")) {
            spotifySupport.previous();
        } else if (pathInfo.startsWith("/next")) {
            spotifySupport.next();
        } else if (pathInfo.startsWith("/artwork")) {
            File tempFile = File.createTempFile("spotify", ".tiff");
            tempFile.deleteOnExit();
            appleScriptTemplate.execute(Application.SPOTIFY, getImageCommand(tempFile));
            BufferedImage image = ImageIO.read(tempFile);
            response.setContentType("image/png");
            ImageIO.write(image, "PNG", response.getOutputStream());
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        } else if (pathInfo.startsWith("/setVolume")) {
            String value = req.getParameter("value");
            Integer volume = Integer.valueOf(value);
            spotifySupport.setVolume(volume);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        appendStatus(response);
    }

    private void appendStatus(HttpServletResponse response) throws IOException {
        response.getWriter().append(new Gson().toJson(spotifySupport.getStatus()));
    }

    private String[] getImageCommand(File tempFile) {
        List<String> commands = new ArrayList<String>();
        commands.add(imageCommandPart1);
        commands.add(String.format(midPart, tempFile.getAbsolutePath()));
        commands.addAll(Arrays.asList(imageCommandPart2));
        return commands.toArray(new String[commands.size()]);
    }

    private static final String midPart = "set the_file to \"%s\"";
    private static final String imageCommandPart1 = "set imagedata to artwork of current track";
    private static final String[] imageCommandPart2 = {
            "try",
            "	open for access the_file with write permission",
            "	set eof of the_file to 0",
            "	write (imagedata) to the_file starting at eof",
            "	close access the_file",
            "on error",
            "	try",
            "		close access the_file",
            "	end try",
            "end try"};
}
