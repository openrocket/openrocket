package net.sf.openrocket.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * A class that tests
 * {@link StringUtils}.
 */
public class StringUtilTest {
    @Test
    public void testStrings() {
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty(new StringBuilder().toString())); // ""
        assertTrue(StringUtils.isEmpty(" "));
        assertTrue(StringUtils.isEmpty("  "));
        assertTrue(StringUtils.isEmpty("       "));
        assertTrue(StringUtils.isEmpty(null));

        assertFalse(StringUtils.isEmpty("A"));
        assertFalse(StringUtils.isEmpty("         .        "));
    }

    @Test
    public void testConvertToDouble() {
        assertEquals(0.2, StringUtils.convertToDouble(".2"), MathUtil.EPSILON);
        assertEquals(0.2, StringUtils.convertToDouble(",2"), MathUtil.EPSILON);
        assertEquals(1, StringUtils.convertToDouble("1,"), MathUtil.EPSILON);
        assertEquals(2, StringUtils.convertToDouble("2."), MathUtil.EPSILON);
        assertEquals(1, StringUtils.convertToDouble("1"), MathUtil.EPSILON);
        assertEquals(1.52, StringUtils.convertToDouble("1.52"), MathUtil.EPSILON);
        assertEquals(1.52, StringUtils.convertToDouble("1,52"), MathUtil.EPSILON);
        assertEquals(1.5, StringUtils.convertToDouble("1.500"), MathUtil.EPSILON);
        assertEquals(1.5, StringUtils.convertToDouble("1,500"), MathUtil.EPSILON);
        assertEquals(1500.61, StringUtils.convertToDouble("1.500,61"), MathUtil.EPSILON);
        assertEquals(1500.61, StringUtils.convertToDouble("1,500.61"), MathUtil.EPSILON);
        assertEquals(1500.2, StringUtils.convertToDouble("1,500,200"), MathUtil.EPSILON);
        assertEquals(1500.2, StringUtils.convertToDouble("1.500.200"), MathUtil.EPSILON);
        assertEquals(1500200.23, StringUtils.convertToDouble("1500200.23"), MathUtil.EPSILON);
        assertEquals(1500200.23, StringUtils.convertToDouble("1500200,23"), MathUtil.EPSILON);
        assertEquals(1500200.23, StringUtils.convertToDouble("1,500,200.23"), MathUtil.EPSILON);
        assertEquals(1500200.23, StringUtils.convertToDouble("1.500.200,23"), MathUtil.EPSILON);
    }
}
