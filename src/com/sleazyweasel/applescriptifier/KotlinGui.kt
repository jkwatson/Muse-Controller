package com.sleazyweasel.musecontroller;

import com.sleazyweasel.applescriptifier.MuseControllerFrame
import com.sleazyweasel.applescriptifier.MuseControllerMain
import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import javax.swing.JMenuBar
import com.apple.eawt.PreferencesHandler
import com.apple.eawt.AppEvent.PreferencesEvent
import com.sleazyweasel.applescriptifier.PreferencesGui
import javax.swing.JMenuItem
import com.sleazyweasel.applescriptifier.MusicPlayer
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import com.sleazyweasel.applescriptifier.PandoraUI
import com.sleazyweasel.applescriptifier.BadPandoraPasswordException
import com.sleazyweasel.applescriptifier.PandoraPasswordUI
import com.sleazyweasel.applescriptifier.NativeSpotifySupportImpl
import javax.swing.SwingUtilities
import javax.swing.JMenu
import javax.swing.JFrame
import com.sleazyweasel.applescriptifier.NativeSpotifySupport
import com.sleazyweasel.applescriptifier.*

class KotlinGui :MuseControllerMain {
    private var activeFrame: MuseControllerFrame? = null;
    override fun setActiveFrame(museControllerFrame: MuseControllerFrame?) {
        if (museControllerFrame != activeFrame) {
            activeFrame?.close()
        }
        activeFrame = museControllerFrame;
    }

    fun startupGui(musicPlayer: MusicPlayer, preferences: MuseControllerPreferences) {
        val spotifySupport = NativeSpotifySupportImpl()

        SwingUtilities.invokeLater(object: Runnable{
            override fun run() {
                val menubar = JMenuBar()
                val pandoraMenuItem = createPandoraMenuItem(preferences, musicPlayer, menubar)
                val spotifyMenuItem = createSpotifyMenuItem(preferences, spotifySupport, menubar)

                val menu = JMenu("Music")
                menu.add(pandoraMenuItem)
                menu.add(spotifyMenuItem)
                menubar.add(menu)

                setUpMacApplicationState(preferences, menubar)

                if (preferences.isPianoBarEnabled() && preferences.wasPandoraTheLastStreamerOpen()) {
                    startupPandora(pandoraMenuItem, musicPlayer, preferences, menubar)
                }
                else if (preferences.isSpotifyEnabled() && preferences.wasSpotifyTheLastStreamerOpen()) {
                    startupSpotify(spotifyMenuItem, spotifySupport, preferences, menubar)
                }
                else {
                    JFrame().pack()
                }
            }
        })
    }

    private fun setUpMacApplicationState(preferences: MuseControllerPreferences, menubar: JMenuBar) {
        val macApp = com.apple.eawt.Application.getApplication();
        macApp?.setPreferencesHandler(object: PreferencesHandler {
            override fun handlePreferences(p0: PreferencesEvent?) {
                val preferencesGui = PreferencesGui(preferences)
                val preferencesFrame = preferencesGui.getWindow()
                preferencesFrame?.setLocationRelativeTo(null)
                preferencesFrame?.setVisible(true)
            }
        })
        macApp?.setDefaultMenuBar(menubar)
    }

    private  fun createPandoraMenuItem(preferences: MuseControllerPreferences, musicPlayer: MusicPlayer, menubar: JMenuBar) : JMenuItem {
        val pandoraMenuItem = JMenuItem("Pandora")
        pandoraMenuItem.setEnabled(true)
        pandoraMenuItem.addActionListener(object: ActionListener{

            override fun actionPerformed(e: ActionEvent?) {
                startupPandora(pandoraMenuItem, musicPlayer, preferences, menubar)
            }
        });
        return pandoraMenuItem
    }

    private fun createSpotifyMenuItem(preferences: MuseControllerPreferences, spotifySupport: NativeSpotifySupport, menubar: JMenuBar) : JMenuItem {
        val spotifyMenuItem = JMenuItem("Spotify")
        spotifyMenuItem.setEnabled(true)
        spotifyMenuItem.addActionListener(object:ActionListener {

            override fun actionPerformed(e: ActionEvent?) {
                startupSpotify(spotifyMenuItem, spotifySupport, preferences, menubar)
            }
        })
        return spotifyMenuItem
    }

    private fun startupSpotify(spotifyMenuItem: JMenuItem, spotifySupport: NativeSpotifySupport, preferences: MuseControllerPreferences, mainMenuBar: JMenuBar) {
        if (spotifySupport.isSpotifyAuthorized()) {
            setActiveFrame(null)
            val spotifyUI = SpotifyUI(spotifySupport, mainMenuBar, spotifyMenuItem, preferences);
            val window = spotifyUI.getWindow()
            setActiveFrame(spotifyUI)
            spotifyMenuItem.setEnabled(false)
            window?.setVisible(true)
            //todo convert spotify UI to kotlin
        }
        else {
            promptForSpotifyPassword(spotifySupport, preferences, mainMenuBar, spotifyMenuItem)
        }
    }

    private fun promptForSpotifyPassword(spotifySupport: NativeSpotifySupport, preferences: MuseControllerPreferences, mainMenuBar: JMenuBar, spotifyMenuItem: JMenuItem) {
        val spotifyPasswordUI = SpotifyPasswordUI(spotifySupport, preferences, mainMenuBar, spotifyMenuItem, this);
        spotifyMenuItem.setEnabled(false)
        val window = spotifyPasswordUI.getWindow()
        setActiveFrame(spotifyPasswordUI)
        window?.setLocationRelativeTo(null)
        window?.setVisible(true)
    }

    private fun startupPandora(pandoraMenuItem: JMenuItem, musicPlayer: MusicPlayer, preferences: MuseControllerPreferences, menubar: JMenuBar) {
        setActiveFrame(null)
        if (musicPlayer.isConfigured()) {
            try {
                val pandoraUi = PandoraUI(musicPlayer, menubar, pandoraMenuItem, preferences)
                pandoraUi.initialize()
                val window = pandoraUi.getWindow()
                setActiveFrame(pandoraUi)
                pandoraMenuItem.setEnabled(false)
                window?.setVisible(true)
            }
            catch(e:BadPandoraPasswordException) {
                promptForPandoraPassword(musicPlayer, preferences, menubar, pandoraMenuItem)
            }
        }
        else {
            promptForPandoraPassword(musicPlayer, preferences, menubar, pandoraMenuItem)
        }
    }

    fun promptForPandoraPassword(musicPlayer: MusicPlayer, preferences: MuseControllerPreferences, mainMenuBar: JMenuBar, pandoraMenuItem: JMenuItem) {
        val pandoraPasswordUi = PandoraPasswordUI(musicPlayer, preferences, mainMenuBar, pandoraMenuItem, this)
        val window = pandoraPasswordUi.getWindow()
        setActiveFrame(pandoraPasswordUi)
        window?.setLocationRelativeTo(null)
        window?.setVisible(true)
    }
}

