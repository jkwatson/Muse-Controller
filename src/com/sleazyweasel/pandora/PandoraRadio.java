package com.sleazyweasel.pandora;

import com.sleazyweasel.applescriptifier.BadPandoraPasswordException;

import java.util.List;

public interface PandoraRadio {
    void connect(String user, String password) throws BadPandoraPasswordException;

    void sync();

    void disconnect();

    List<Station> getStations();

    Station getStationById(long sid);

    void rate(Song song, boolean rating);

    boolean isAlive();

    Song[] getPlaylist(Station station, String format);
}
