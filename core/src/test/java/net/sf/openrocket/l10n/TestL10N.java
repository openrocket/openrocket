package net.sf.openrocket.l10n;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.util.Chars;

import org.junit.Test;

public class TestL10N {
	
	@Test
	public void testNormalize() {
		assertEquals("hello", L10N.normalize("hello"));
		assertEquals("hello", L10N.normalize("Hello"));
		assertEquals("hello", L10N.normalize(" \t Hello \n "));
		assertEquals("hello", L10N.normalize("H\u00eall\u00d6"));
		assertEquals("hello_world", L10N.normalize("Hello World!"));
		assertEquals("hello_world", L10N.normalize("?  Hello\nWorld  !"));
		assertEquals("hello_123", L10N.normalize("Hello  123!"));
		assertEquals("hello_123", L10N.normalize("Hello/123?"));
		
		assertEquals("plywood_birch", L10N.normalize("Plywood (birch)"));
		assertEquals("styrofoam_blue_foam_xps", L10N.normalize("Styrofoam \"Blue foam\" (XPS)"));
		assertEquals("tubular_nylon_11_mm_7_16_in", L10N.normalize("Tubular nylon (11 mm, 7/16 in)"));
		
		assertEquals("m2", L10N.normalize("m" + Chars.SQUARED));
		assertEquals("a_b", L10N.normalize("a" + Chars.NBSP + "b"));
		assertEquals("1_2a", L10N.normalize(Chars.FRAC12 + "A"));
	}
}
