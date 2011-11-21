package com.sleazyweasel.pandora;/* Pandoroid Radio - open source pandora.com client for android
 * Copyright (C) 2011  Andrew Regner <andrew@aregner.com>
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import java.util.Map;

public class Song {
    private String album;
    private String artist;
    private String artistMusicId;
    private String audioUrl;
    private String fileGain;
    private String identity;
    private Integer rating;
    private String stationId;
    private String title;
    private String songDetailURL;
    private String albumDetailURL;
    private String artRadio;
    private String trackToken;

    private boolean tired;
    private String message;
    private Object startTime;
    private boolean finished;
    private long playlistTime;

    public Song(Map<String, Object> data, PandoraRadio pandoraRadio) {
        try {
            album = (String) data.get("albumTitle");
            artist = (String) data.get("artistSummary");
            artistMusicId = (String) data.get("artistMusicId");
            audioUrl = (String) data.get("audioURL"); // needs to be hacked, see below
            fileGain = (String) data.get("fileGain");
            identity = (String) data.get("identity");
            rating = (Integer) data.get("rating");
            stationId = (String) data.get("stationId");
            title = (String) data.get("songTitle");
            songDetailURL = (String) data.get("songDetailURL");
            albumDetailURL = (String) data.get("albumDetailURL");
            artRadio = (String) data.get("artRadio");
            trackToken = (String) data.get("trackToken");

            int aul = audioUrl.length();
            audioUrl = audioUrl.substring(0, aul - 48) + pandoraRadio.pandoraDecrypt(audioUrl.substring(aul - 48));

            tired = false;
            message = "";
            startTime = null;
            finished = false;
            playlistTime = System.currentTimeMillis() / 1000L;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    public Song(Song copy, Integer newRating) {
        album = copy.album;
        artist = copy.artist;
        artistMusicId = copy.artistMusicId;
        audioUrl = copy.audioUrl;
        fileGain = copy.fileGain;
        identity = copy.identity;
        rating = newRating;
        stationId = copy.stationId;
        title = copy.title;
        songDetailURL = copy.songDetailURL;
        albumDetailURL = copy.albumDetailURL;
        artRadio = copy.artRadio;
        trackToken = copy.trackToken;
        audioUrl = copy.audioUrl;
        tired = copy.tired;
        message = copy.message;
        startTime = copy.startTime;
        finished = copy.finished;
        playlistTime = copy.playlistTime;
    }

    public boolean isStillValid() {
        return ((System.currentTimeMillis() / 1000L) - playlistTime) < PandoraRadio.PLAYLIST_VALIDITY_TIME;
    }

    public String getTrackToken() {
        return trackToken;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getAlbumCoverUrl() {
        return artRadio;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public Integer getRating() {
        return rating;
    }

    public String getArtistMusicId() {
        return artistMusicId;
    }

    public String getFileGain() {
        return fileGain;
    }

    public String getIdentity() {
        return identity;
    }

    public String getStationId() {
        return stationId;
    }

    public String getSongDetailURL() {
        return songDetailURL;
    }

    public String getAlbumDetailURL() {
        return albumDetailURL;
    }

    public boolean isTired() {
        return tired;
    }

    public String getMessage() {
        return message;
    }

    public Object getStartTime() {
        return startTime;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isLoved() {
        return rating > 0;
    }

    @Override
    public String toString() {
        return title;
    }
}
