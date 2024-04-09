package info.openrocket.core.l10n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import info.openrocket.core.util.Chars;

import org.junit.jupiter.api.Test;

public class TestL10N {

	@Test
	public void testNormalize() {
		assertEquals(L10N.normalize("hello"), "hello");
		assertEquals(L10N.normalize("Hello"), "hello");
		assertEquals(L10N.normalize(" \t Hello \n "), "hello");
		assertEquals(L10N.normalize("H\u00eall\u00d6"), "hello");
		assertEquals(L10N.normalize("Hello World!"), "hello_world");
		assertEquals(L10N.normalize("?  Hello\nWorld  !"), "hello_world");
		assertEquals(L10N.normalize("Hello  123!"), "hello_123");
		assertEquals(L10N.normalize("Hello/123?"), "hello_123");

		assertEquals(L10N.normalize("Plywood (birch)"), "plywood_birch");
		assertEquals(L10N.normalize("Styrofoam \"Blue foam\" (XPS)"), "styrofoam_blue_foam_xps");
		assertEquals(L10N.normalize("Tubular nylon (11 mm, 7/16 in)"), "tubular_nylon_11_mm_7_16_in");

		assertEquals(L10N.normalize("m" + Chars.SQUARED), "m2");
		assertEquals(L10N.normalize("a" + Chars.NBSP + "b"), "a_b");
		assertEquals(L10N.normalize(Chars.FRAC12 + "A"), "1_2a");
	}
}
