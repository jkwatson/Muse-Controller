package com.sleazyweasel.applescriptifier;

import de.felixbruns.jotify.JotifyPool;
import de.felixbruns.jotify.exceptions.AuthenticationException;
import de.felixbruns.jotify.exceptions.ConnectionException;

import java.io.*;

public class NativeSpotifySupport {

    private static final String ALIAS = "com.sleazyweasel.spotify";
    private JotifyPool jotifyPool;

    private synchronized JotifyPool getJotifyPool() {
        if (jotifyPool == null) {
            jotifyPool = new JotifyPool(2);
        }
        return jotifyPool;
    }

    public boolean isSpotifyAuthorized() {
        JotifyPool pool = getJotifyPool();

        File configFile = getConfigFile();
        if (!configFile.exists()) {
            return false;
        }
        BufferedReader reader = null;
        try {
            pool.close();
            reader = new BufferedReader(new FileReader(configFile));
            String userLine = reader.readLine();
            String passwordLine = reader.readLine();
            String userName = userLine.substring(5);
            String password = passwordLine.substring(9);
            pool.login(userName, password);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    //todo return different stuff, depending on no connection vs. bad username/password
    //todo figure out better exception handling here.
    public boolean authorize(String username, char[] password) {
        try {
            getJotifyPool().login(username, new String(password));
            saveSpotifyConfig(username, password);
        } catch (ConnectionException e) {
            e.printStackTrace();
            return false;
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void saveSpotifyConfig(String username, char[] password) throws IOException {
        File spotifyConfigDirectory = getSpotifyConfigDirectory();
        if (!spotifyConfigDirectory.exists()) {
            spotifyConfigDirectory.mkdirs();
            Runtime.getRuntime().exec(new String[]{"chmod", "700", spotifyConfigDirectory.getAbsolutePath()});
        }
        File configFile = getConfigFile();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(configFile))));
        writer.write("user=" + username);
        writer.newLine();
        writer.write("password=" + new String(password));
        writer.newLine();
        writer.close();
    }

    private File getConfigFile() {
        return new File(getSpotifyConfigDirectory(), "config");
    }

    private static File getSpotifyConfigDirectory() {
        String userHome = System.getProperty("user.home");
        return new File(userHome + "/.config/spotify");
    }


}
