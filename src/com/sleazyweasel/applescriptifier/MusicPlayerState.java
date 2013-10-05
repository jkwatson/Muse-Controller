package com.sleazyweasel.applescriptifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MusicPlayerState {
    private final boolean currentSongIsLoved;
    private final String title;
    private final String artist;
    private final String station;
    private final String album;
    private final MusicPlayerInputType inputTypeRequested;
    private final Map<Integer, String> stations;
    private final String albumArtUrl;
    private final String detailUrl;
    private final String currentTimeInTrack;
    private final boolean playing;
    private final double volume;

    public MusicPlayerState(boolean currentSongIsLoved, String title, String artist, String station, String album, MusicPlayerInputType inputTypeRequested, Map<Integer, String> stations, String albumArtUrl, String currentTimeInTrack, boolean isPlaying, String detailUrl, double volume) {
        this.currentSongIsLoved = currentSongIsLoved;
        this.title = title;
        this.artist = artist;
        this.station = station;
        this.album = album;
        this.inputTypeRequested = inputTypeRequested;
        this.stations = stations;
        this.albumArtUrl = albumArtUrl;
        this.detailUrl = detailUrl;
        this.volume = volume;
        currentTimeInTrack = currentTimeInTrack.trim();
        if (currentTimeInTrack.startsWith("-")) {
            currentTimeInTrack = currentTimeInTrack.substring(1);
        }
        this.currentTimeInTrack = currentTimeInTrack;
        this.playing = isPlaying;
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

    public MusicPlayerInputType getInputTypeRequested() {
        return inputTypeRequested;
    }

    public boolean isInputRequested() {
        return !MusicPlayerInputType.NONE.equals(getInputTypeRequested());
    }

    public Map<Integer, String> getStations() {
        return stations;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public String getCurrentTimeInTrack() {
        return currentTimeInTrack;
    }

    public boolean isPlaying() {
        return playing;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public List<StationChoice> getStationChoices() {
        List<StationChoice> choices = new ArrayList<StationChoice>();
        for (Map.Entry<Integer, String> stationEntry : stations.entrySet()) {
            choices.add(new StationChoice(stationEntry.getKey(), stationEntry.getValue()));
        }

//        List<Integer> list = new ArrayList<Integer>(stations.keySet());
//        Collections.sort(list);
//        for (Integer integer : list) {
//            choices.add(new StationChoice(integer, stations.get(integer)));
//        }
        return choices;
    }


    public StationChoice getCurrentStation() {
        for (Map.Entry<Integer, String> entry : stations.entrySet()) {
            if (station.equals(entry.getValue())) {
                return new StationChoice(entry.getKey(), entry.getValue());
            }
        }
        return null;
    }

    public int getCurrentTime() {
        if (currentTimeInTrack != null && currentTimeInTrack.length() > 0) {
            String[] parts = currentTimeInTrack.split("/");
            String totalTimePart = parts[0];
            return parseTime(totalTimePart);
        }
        return 0;
    }

    public int getDuration() {
        if (currentTimeInTrack != null && currentTimeInTrack.length() > 0) {
            String[] parts = currentTimeInTrack.split("/");
            if (parts.length > 1) {
                String totalTimePart = parts[1];
                return parseTime(totalTimePart);
            }
        }
        return 0;
    }

    //todo this is ridiculous...why are we storing the formatted data, instead of the ints?
    private int parseTime(String totalTimePart) {
        String[] minutesAndSeconds = totalTimePart.split(":");
        int minutes = Integer.parseInt(minutesAndSeconds[0]);
        int seconds = Integer.parseInt(minutesAndSeconds[1]);
        return minutes * 60 + seconds;
    }

    public double getVolume() {
        return volume;
    }
}
