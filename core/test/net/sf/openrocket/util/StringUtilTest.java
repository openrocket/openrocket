package net.sf.openrocket.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A class that tests
 * {@link net.sf.openrocket.util.StringUtil}.
 */
public class StringUtilTest {
    @Test
    public void testStrings() {
        assertTrue(StringUtil.isEmpty(""));
        assertTrue(StringUtil.isEmpty(new StringBuilder().toString())); // ""
        assertTrue(StringUtil.isEmpty(" "));
        assertTrue(StringUtil.isEmpty("  "));
        assertTrue(StringUtil.isEmpty("       "));
        assertTrue(StringUtil.isEmpty(null));

        assertFalse(StringUtil.isEmpty("A"));
        assertFalse(StringUtil.isEmpty("         .        "));
    }
}
