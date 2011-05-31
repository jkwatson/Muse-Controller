package com.sleazyweasel.applescriptifier;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class LineBufferTest {

    @Test
    public void basics() {
        LineBuffer testClass = new LineBuffer(5);
        testClass.add('o');
        testClass.add('t');
        testClass.add('t');
        testClass.add('f');
        testClass.add('f');
        testClass.add('s');
        String contents = testClass.getContents();
        assertNotNull(contents);
        assertEquals("ttffs", contents);
    }

}
