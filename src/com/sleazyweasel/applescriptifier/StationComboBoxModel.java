package com.sleazyweasel.applescriptifier;

import javax.swing.*;
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
}
