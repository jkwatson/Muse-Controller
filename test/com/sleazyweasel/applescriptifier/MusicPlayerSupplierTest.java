package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import static org.mockito.Mockito.*;

public class MusicPlayerSupplierTest {

    @Test
    public void testListeners() throws Exception {

        MusicPlayer rdioPlayer = mock(MusicPlayer.class);
        MusicPlayer pandoraPlayer = mock(MusicPlayer.class);
        MusicPlayer.MusicPlayerStateChangeListener listener = mock(MusicPlayer.MusicPlayerStateChangeListener.class);

        MusicPlayerSupplier testClass = new MusicPlayerSupplier();
        testClass.addMusicPlayer(Application.RDIO(), rdioPlayer);

        testClass.setCurrentApplication(Application.RDIO());
        verify(rdioPlayer, never()).addListener(any(MusicPlayer.MusicPlayerStateChangeListener.class));
        verify(pandoraPlayer, never()).addListener(any(MusicPlayer.MusicPlayerStateChangeListener.class));

        testClass.addListener(listener);
        verify(rdioPlayer).addListener(listener);

        verify(rdioPlayer).removeListener(listener);
        verify(pandoraPlayer).addListener(listener);

        testClass.setCurrentApplication(Application.RDIO());
        verify(pandoraPlayer).removeListener(listener);
        verify(rdioPlayer, times(2)).addListener(listener);

        testClass.removeListener(listener);
        verify(rdioPlayer, times(2)).removeListener(listener);
    }

}
