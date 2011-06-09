package com.sleazyweasel.applescriptifier;

import javax.swing.*;
import java.util.List;
import java.util.Vector;

public class StationComboBoxModel extends DefaultComboBoxModel {

    private final NativePianobarSupport pianobarSupport;

    public StationComboBoxModel(NativePianobarSupport pianobarSupport) {
        super(new Vector<StationChoice>(pianobarSupport.getState().getStationChoices()));
        this.pianobarSupport = pianobarSupport;
    }

    public StationChoice getSelectedStation() {
        return (StationChoice) super.getSelectedItem();
    }

    public void refreshContents() {
        List<StationChoice> stationChoices = pianobarSupport.getState().getStationChoices();
        Object selectedItem = getSelectedItem();
        for (StationChoice stationChoice : stationChoices) {
            Object existingItem = getElementAt(stationChoice.getKey());
            if (!existingItem.equals(stationChoice)) {
                removeElementAt(stationChoice.getKey());
                insertElementAt(stationChoice, stationChoice.getKey());
            }
        }
        setSelectedItem(selectedItem);
    }
}
