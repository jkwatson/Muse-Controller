package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.Application._
import scala.collection.JavaConversions._

object Application {

  val AIRFOIL = new Application("Airfoil", "Airfoil", "com.rogueamoeba.Airfoil", true, false, false, false, false, false) {}
  val PANDORABOY = new Application("PandoraBoy", "PandoraBoy", "com.frozensilicon.PandoraBoy", true, true, true, false, true, true) {}
  val PULSAR = new Application("Pulsar", "Pulsar", "com.rogueamoeba.Pulsar", true, true, true, true, false, false) {}
  val ITUNES = new Application("iTunes", "iTunes", "com.apple.iTunes", false, true, true, true, false, false) {}
  val PANDORAONE = new Application("Pandora", "Pandora", "com.pandora.desktop.FB9956FD96E03239939108614098AD95535EE674.1", false, true, true, false, true, true) {}
  val RDIO = new Application("Rdio", "Rdio", "com.rdio.desktop", true, true, true, true, false, false) {}
  val MUSECONTROLLER = new Application("Muse Controller", "Muse Controller", "com.sleazyweasel.MuseController", true, true, true, false, true, true) {}
  val SPOTIFY = new Application("Spotify", "Spotify", "com.spotify.client", true, true, true, true, false, false) {}
  val OTHER = new Application("Other", "Other", "unknown", false, false, false, false, false, false) {}

  def values = seqAsJavaList(allSupportedApplications)

  def allSupportedApplications = List(AIRFOIL, PANDORABOY, PULSAR, ITUNES, PANDORAONE, RDIO, MUSECONTROLLER, SPOTIFY)

  def forName(name: String): Application = {
    allSupportedApplications.find(a => a.name.equalsIgnoreCase(name)).getOrElse(OTHER)
  }
}

abstract sealed class Application(val name: String, val displayName: String, val identifier: String, val fullSupport: Boolean, val playPauseSupport: Boolean,
                                  val nextSupport: Boolean, val previousSupport: Boolean, val thumbsUpSupport: Boolean, val thumbsDownSupport: Boolean) {
  def getApplicationSupport(appleScriptTemplate: AppleScriptTemplate, musicPlayer: MusicPlayer): ApplicationSupport = {
    this match {
      case PANDORABOY =>
        new PandoraBoySupport(appleScriptTemplate)
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
      case _ =>
        null
    }
  }

  override def toString: String = name
}



