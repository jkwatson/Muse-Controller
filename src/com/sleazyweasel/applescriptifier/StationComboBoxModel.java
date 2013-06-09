package com.sleazyweasel.applescriptifier;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

public class StationComboBoxModel extends DefaultComboBoxModel {

    private final MusicPlayer musicPlayer;

    public StationComboBoxModel(MusicPlayer musicPlayer) {
        super(new Vector<StationChoice>(musicPlayer.getState().getStationChoices()));
        this.musicPlayer = musicPlayer;
    }

    public StationChoice getSelectedStation() {
        return (StationChoice) super.getSelectedItem();
    }

    public void refreshContents() {
        List<StationChoice> stationChoices = musicPlayer.getState().getStationChoices();
        Object selectedItem = getSelectedItem();
        for (StationChoice stationChoice : stationChoices) {
            Object existingItem = getElementAt(stationChoice.key());
            if (existingItem == null) {
                insertElementAt(stationChoice, stationChoice.key());
            } else if (!existingItem.equals(stationChoice)) {
                removeElementAt(stationChoice.key());
                insertElementAt(stationChoice, stationChoice.key());
            }
        }
        setSelectedItem(selectedItem);
    }
}
