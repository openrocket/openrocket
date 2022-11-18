package net.sf.openrocket.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

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

    @Test
    public void testConvertToDouble() {
        assertEquals(0.2, StringUtil.convertToDouble(".2"), MathUtil.EPSILON);
        assertEquals(0.2, StringUtil.convertToDouble(",2"), MathUtil.EPSILON);
        assertEquals(1, StringUtil.convertToDouble("1,"), MathUtil.EPSILON);
        assertEquals(2, StringUtil.convertToDouble("2."), MathUtil.EPSILON);
        assertEquals(1, StringUtil.convertToDouble("1"), MathUtil.EPSILON);
        assertEquals(1.52, StringUtil.convertToDouble("1.52"), MathUtil.EPSILON);
        assertEquals(1.52, StringUtil.convertToDouble("1,52"), MathUtil.EPSILON);
        assertEquals(1.5, StringUtil.convertToDouble("1.500"), MathUtil.EPSILON);
        assertEquals(1.5, StringUtil.convertToDouble("1,500"), MathUtil.EPSILON);
        assertEquals(1500.61, StringUtil.convertToDouble("1.500,61"), MathUtil.EPSILON);
        assertEquals(1500.61, StringUtil.convertToDouble("1,500.61"), MathUtil.EPSILON);
        assertEquals(1500.2, StringUtil.convertToDouble("1,500,200"), MathUtil.EPSILON);
        assertEquals(1500.2, StringUtil.convertToDouble("1.500.200"), MathUtil.EPSILON);
        assertEquals(1500200.23, StringUtil.convertToDouble("1500200.23"), MathUtil.EPSILON);
        assertEquals(1500200.23, StringUtil.convertToDouble("1500200,23"), MathUtil.EPSILON);
        assertEquals(1500200.23, StringUtil.convertToDouble("1,500,200.23"), MathUtil.EPSILON);
        assertEquals(1500200.23, StringUtil.convertToDouble("1.500.200,23"), MathUtil.EPSILON);
    }
}
