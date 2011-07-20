package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import static junit.framework.Assert.*;

public class PianobarStateTest {

    @Test
    public void testTimeInTrack() {
        PianobarState testClass = new PianobarState(false, null, null, null, null, null, null, null, "-02:19/03:11", false, null);

        int duration = testClass.getDuration();
        assertEquals(191, duration);
    }
}
