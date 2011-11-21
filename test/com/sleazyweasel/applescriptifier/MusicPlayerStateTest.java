package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class MusicPlayerStateTest {

    @Test
    public void testTimeInTrack() {
        MusicPlayerState testClass = new MusicPlayerState(false, null, null, null, null, null, null, null, "-02:19/03:11", false, null);

        int duration = testClass.getDuration();
        assertEquals(191, duration);
    }
}
