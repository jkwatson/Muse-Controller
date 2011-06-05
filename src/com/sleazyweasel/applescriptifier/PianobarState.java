package com.sleazyweasel.applescriptifier;

import java.util.Map;

public class PianobarState {
    private final boolean currentSongIsLoved;
    private final String title;
    private final String artist;
    private final String station;
    private final String album;
    private final NativePianobarSupport.InputType inputTypeRequested;
    private final Map<Integer, String> stations;
    private final String albumArtUrl;

    public PianobarState(boolean currentSongIsLoved, String title, String artist, String station, String album, NativePianobarSupport.InputType inputTypeRequested, Map<Integer, String> stations, String albumArtUrl) {
        this.currentSongIsLoved = currentSongIsLoved;
        this.title = title;
        this.artist = artist;
        this.station = station;
        this.album = album;
        this.inputTypeRequested = inputTypeRequested;
        this.stations = stations;
        this.albumArtUrl = albumArtUrl;
    }

    public boolean isCurrentSongIsLoved() {
        return currentSongIsLoved;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getStation() {
        return station;
    }

    public String getAlbum() {
        return album;
    }

    public NativePianobarSupport.InputType getInputTypeRequested() {
        return inputTypeRequested;
    }

    public boolean isInputRequested() {
        return !NativePianobarSupport.InputType.NONE.equals(getInputTypeRequested());
    }

    public Map<Integer, String> getStations() {
        return stations;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }
}
