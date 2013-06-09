package com.sleazyweasel.applescriptifier

import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences
import layout.TableLayout
import javax.swing._
import java.awt._
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.prefs.BackingStoreException
import layout.TableLayoutConstants.FILL
import layout.TableLayoutConstants.PREFERRED

object PreferencesGui {

  private class Widgets {
    var window: JFrame = null
    var enablePandoraCheckbox: JCheckBox = null
    var enableMuseControlCheckbox: JCheckBox = null
    var saveButton: JButton = null
    var cancelButton: JButton = null
  }

}

class PreferencesGui(val preferences: MuseControllerPreferences) {
  private final val widgets: PreferencesGui.Widgets = new PreferencesGui.Widgets

  initWidgetsAndModels()
  initLayout()

  private def initLayout() {
    val columnsThenRows: Array[Array[Double]] = Array(Array(15, FILL, PREFERRED, PREFERRED, 12), Array(15, PREFERRED, PREFERRED, 5, PREFERRED, 15, PREFERRED, 12))
    val tableLayout: TableLayout = new TableLayout(columnsThenRows)
    val contentPane: Container = widgets.window.getContentPane
    contentPane.setLayout(tableLayout)
    contentPane.add(widgets.enablePandoraCheckbox, "1, 1")
    contentPane.add(widgets.enableMuseControlCheckbox, "1, 2")
    val labelHolder: JPanel = new JPanel(new TableLayout(Array[Array[Double]](Array(FILL), Array(FILL))))
    labelHolder.add(new JLabel("(Application must be restarted for changes to take effect)"), "0,0,c,c")
    contentPane.add(labelHolder, "1,4,3,4")
    contentPane.add(widgets.saveButton, "2, 6")
    contentPane.add(widgets.cancelButton, "3, 6")
    widgets.window.getRootPane.setDefaultButton(widgets.saveButton)
    widgets.window.pack()
    widgets.cancelButton.requestFocus()
  }

  private def initWidgetsAndModels() {
    initWindow()
    initCancelButton()
    initEnablePandoraCheckbox()
    initEnableMuseControlCheckbox()
    initSaveButton()
  }

  private def initEnableMuseControlCheckbox() {
    widgets.enableMuseControlCheckbox = new JCheckBox("Enable Web Services for Muse Control", preferences.isMuseControlEnabled)
  }

  private def initCancelButton() {
    widgets.cancelButton = new JButton("Cancel")
    widgets.cancelButton.addActionListener(new ActionListener {
      def actionPerformed(actionEvent: ActionEvent) {
        widgets.window.dispose()
      }
    })
  }

  private def initSaveButton() {
    widgets.saveButton = new JButton("OK")
    widgets.saveButton.addActionListener(new ActionListener {
      def actionPerformed(actionEvent: ActionEvent) {
        preferences.enablePandora(widgets.enablePandoraCheckbox.isSelected)
        preferences.enableMuseControl(widgets.enableMuseControlCheckbox.isSelected)
        try {
          preferences.save()
        }
        catch {
          case e: BackingStoreException => {
            JOptionPane.showMessageDialog(widgets.window, "Unable to Save Preferences", "Error", JOptionPane.ERROR_MESSAGE)
          }
        }
        widgets.window.dispose()
      }
    })
  }

  private def initEnablePandoraCheckbox() {
    widgets.enablePandoraCheckbox = new JCheckBox("Enable Pandora Streaming", preferences.isPandoraEnabled)
  }

  private def initWindow() {
    widgets.window = new JFrame("Preferences")
  }

  def getWindow: JFrame = {
    widgets.window
  }

}