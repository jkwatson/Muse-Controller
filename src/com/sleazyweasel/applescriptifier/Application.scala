package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.Application._
import scala.collection.JavaConversions._

object Application {

  val AIRFOIL = new Application("Airfoil", "Airfoil", "com.rogueamoeba.Airfoil", true, false, false, false, false, false)
  val PULSAR = new Application("Pulsar", "Pulsar", "com.rogueamoeba.Pulsar", true, true, true, true, false, false)
  val ITUNES = new Application("iTunes", "iTunes", "com.apple.iTunes", false, true, true, true, false, false)
  val PANDORAONE = new Application("Pandora", "Pandora", "com.pandora.desktop.FB9956FD96E03239939108614098AD95535EE674.1", false, true, true, false, true, true)
  val RDIO = new Application("Rdio", "Rdio", "com.rdio.desktop", true, true, true, true, false, false)
  val MUSECONTROLLER = new Application("Muse Controller", "Muse Controller", "com.sleazyweasel.MuseController", true, true, true, false, true, true)
  val SPOTIFY = new Application("Spotify", "Spotify", "com.spotify.client", true, true, true, true, false, false)
  val MUSE = new Application("Muse", "Muse", "com.industriousone.PandoraMusicality", false, true, true, false, false, false)
  val OTHER = new Application("Other", "Other", "unknown", false, false, false, false, false, false)

  def values = seqAsJavaList(allSupportedApplications)

  def allSupportedApplications = List(AIRFOIL, PULSAR, ITUNES, PANDORAONE, RDIO, MUSECONTROLLER, SPOTIFY, MUSE)

  def forName(name: String): Application = {
    allSupportedApplications.find(a => a.name.equalsIgnoreCase(name)).getOrElse(OTHER)
  }
}

sealed case class Application(name: String, displayName: String, identifier: String, fullSupport: Boolean, playPauseSupport: Boolean,
                                  nextSupport: Boolean, previousSupport: Boolean, thumbsUpSupport: Boolean, thumbsDownSupport: Boolean) {

  def getApplicationSupport(appleScriptTemplate: AppleScriptTemplate, musicPlayer: MusicPlayer): ApplicationSupport = {
    this match {
      case PULSAR =>
        new PulsarSupport(appleScriptTemplate)
      case ITUNES =>
        new ITunesSupport(appleScriptTemplate)
      case PANDORAONE =>
        new PandoraOneSupport(appleScriptTemplate)
      case RDIO =>
        new RdioSupport(appleScriptTemplate)
      case MUSECONTROLLER =>
        musicPlayer
      case SPOTIFY =>
        new SpotifySupport(appleScriptTemplate)
      case MUSE =>
        new MuseSupport(appleScriptTemplate)
      case _ =>
        null
    }
  }

  override def toString: String = name
}



