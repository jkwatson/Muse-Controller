package com.sleazyweasel.applescriptifier.preferences

import java.util.logging.Level
import java.util.logging.Logger
import java.util.prefs.BackingStoreException
import java.util.prefs.Preferences

object MuseControllerPreferences {
  private final val logger: Logger = Logger.getLogger(classOf[MuseControllerPreferences].getName)
  private final val PANDORA_VETO_KEY: String = "pianobar.veto"
  private final val SPOTIFY_ENABLE_KEY: String = "spotify.enable"
  private final val MUSECONTROL_ENABLE_KEY: String = "musecontrol.enable"
  private final val LAST_STREAMER_KEY: String = "last.streamer"
  private final val PANDORA_STREAMER_VALUE: String = "Pandora"
  private final val SPOTIFY_STREAMER_VALUE: String = "Spotify"
  private final val PREVIOUS_PANDORA_STATION_ID_KEY: String = "PREVIOUS_PANDORA_STATION_ID_KEY"
  private final val PREVIOUS_PANDORA_VOLUME_KEY: String = "PREVIOUS_PANDORA_VOLUME_KEY"
  private final val PREVIOUS_SPOTIFY_VOLUME_KEY: String = "PREVIOUS_SPOTIFY_VOLUME_KEY"
}

class MuseControllerPreferences(val preferences: Preferences) {

  def isPandoraEnabled: Boolean = {
    !preferences.getBoolean(MuseControllerPreferences.PANDORA_VETO_KEY, false)
  }

  def enablePandora(enable: Boolean) {
    preferences.putBoolean(MuseControllerPreferences.PANDORA_VETO_KEY, !enable)
  }

  def enableMuseControl(enable: Boolean) {
    preferences.putBoolean(MuseControllerPreferences.MUSECONTROL_ENABLE_KEY, enable)
  }

  def isMuseControlEnabled: Boolean = {
    preferences.getBoolean(MuseControllerPreferences.MUSECONTROL_ENABLE_KEY, true)
  }

  def save() {
    preferences.sync()
  }

  def wasPandoraTheLastStreamerOpen(): Boolean = {
    val lastStreamer: String = preferences.get(MuseControllerPreferences.LAST_STREAMER_KEY, null)
    lastStreamer == null || (lastStreamer == MuseControllerPreferences.PANDORA_STREAMER_VALUE)
  }

  def wasSpotifyTheLastStreamerOpen(): Boolean = {
    val lastStreamer: String = preferences.get(MuseControllerPreferences.LAST_STREAMER_KEY, null)
    MuseControllerPreferences.SPOTIFY_STREAMER_VALUE == lastStreamer
  }

  def setPandoraAsStreamer() {
    preferences.put(MuseControllerPreferences.LAST_STREAMER_KEY, MuseControllerPreferences.PANDORA_STREAMER_VALUE)
  }

  def enableSpotify(enable: Boolean) {
    preferences.putBoolean(MuseControllerPreferences.SPOTIFY_ENABLE_KEY, enable)
  }

  def setSpotifyAsStreamer() {
    preferences.put(MuseControllerPreferences.LAST_STREAMER_KEY, MuseControllerPreferences.SPOTIFY_STREAMER_VALUE)
  }

  def getPreviousPandoraStationId: Long = {
    if (keyExists(MuseControllerPreferences.PREVIOUS_PANDORA_STATION_ID_KEY)) {
      return preferences.getLong(MuseControllerPreferences.PREVIOUS_PANDORA_STATION_ID_KEY, 0)
    }
    -1
  }

  def setPandoraStationId(stationId: Long) {
    if (stationId == -1) {
      preferences.remove(MuseControllerPreferences.PREVIOUS_PANDORA_STATION_ID_KEY)
    }
    else {
      preferences.putLong(MuseControllerPreferences.PREVIOUS_PANDORA_STATION_ID_KEY, stationId)
    }
  }

  def getPreviousPandoraVolume: Double = {
    if (keyExists(MuseControllerPreferences.PREVIOUS_PANDORA_VOLUME_KEY)) {
      return preferences.getDouble(MuseControllerPreferences.PREVIOUS_PANDORA_VOLUME_KEY, 0)
    }
    -1d
  }

  def setPandoraVolume(volume: Double) {
    preferences.putDouble(MuseControllerPreferences.PREVIOUS_PANDORA_VOLUME_KEY, volume)
  }

  def getPreviousSpotifyVolume: Float = {
    preferences.getFloat(MuseControllerPreferences.PREVIOUS_SPOTIFY_VOLUME_KEY, 1.0f)
  }

  private def keyExists(keyToCheck: String): Boolean = {
    try {
      val keys: Array[String] = preferences.keys
      for (key <- keys) {
        if (keyToCheck == key) {
          return true
        }
      }
      false
    }
    catch {
      case e: BackingStoreException => {
        MuseControllerPreferences.logger.log(Level.WARNING, "Exception caught.", e)
        return false
      }
    }
  }

}