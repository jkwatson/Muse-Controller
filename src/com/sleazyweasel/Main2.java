package com.sleazyweasel;

import com.sleazyweasel.pandora.PandoraRadio;
import com.sleazyweasel.pandora.Song;
import com.sleazyweasel.pandora.Station;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class Main2 {

    public static void main(String[] args) throws Exception {
        PandoraRadio pandoraRadio = new PandoraRadio();
        pandoraRadio.connect("jkwatson@gmail.com", "landheart");
        List<Station> stations = pandoraRadio.getStations();
        for (Station station : stations) {
            System.out.println("station = " + station.getName());
        }

        Station station = stations.get(2);
        Song[] playlist = station.getPlaylist("mp3-hifi");
        Song song = playlist[0];
        URL url = new URL(song.getAudioUrl());
        HttpURLConnection conn = ((HttpURLConnection) url.openConnection());
        conn.setRequestMethod("GET");
        setRequestHeaders(conn);
        conn.connect();
        InputStream is = conn.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);
        final Player player = new Player(bis);
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    player.play();
                } catch (JavaLayerException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t = new Thread(runnable);
        t.setDaemon(false);
        t.start();
        t.join();

    }

    private static void setRequestHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
        conn.setRequestProperty("Accept", "*/*");
    }

}
