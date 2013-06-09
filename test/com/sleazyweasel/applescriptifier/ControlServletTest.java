package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ControlServletTest {
    @Test
    public void testDoGet_noException() throws Exception {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(httpServletRequest.getPathInfo()).thenReturn("/foobar");

        ControlServlet testClass = new ControlServlet(mock(MusicPlayer.class));

        testClass.doGet(httpServletRequest, response);
    }
}
