package com.sleazyweasel.applescriptifier

import de.felixbruns.jotify.media.Playlist
import javax.swing._
import java.util.logging.Logger
import java.util

class PlaylistComboBoxModel(val spotifySupport: NativeSpotifySupport) extends DefaultComboBoxModel(new util.Vector[Playlist]) {
  private final val logger: Logger = Logger.getLogger(classOf[PlaylistComboBoxModel].getName)

  setSelectedItem(null)

  def getSelectedStation: Playlist = {
    super.getSelectedItem.asInstanceOf[Playlist]
  }

  def refreshContents() {
    val playlists: util.List[Playlist] = spotifySupport.getPlaylists
    val selectedItem: Playlist = getSelectedStation
    logger.finest("selectedItem = " + selectedItem)
    removeAllElements()
    import scala.collection.JavaConversions._
    for (playlist <- playlists) {
      addElement(playlist)
    }
    setSelectedItem(selectedItem)
  }

}