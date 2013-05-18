package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

public class ApplicationTest {

    @Test
    public void testValues() throws Exception {
        new Application("", "", "", true, true, true, true, true, true) {
        };
        List<Application> values = Application.values();
        assertNotNull(values);
        assertFalse(values.isEmpty());
        assertNotNull(values.get(0));
    }

}
