package net.sf.openrocket.l10n;

import static org.junit.Assert.*;

import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.Test;

public class TestResourceBundleTranslator {
	
	@Test
	public void testSuccessfulUS() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages", Locale.US);
		assertEquals("messages.properties", trans.get("debug.currentFile"));
	}
	
	@Test
	public void testSuccessfulFR() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages", Locale.FRENCH);
		assertEquals("messages_fr.properties", trans.get("debug.currentFile"));
	}
	
	@Test
	public void testFailure() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages", Locale.US);
		try {
			fail("Returned: " + trans.get("missing"));
		} catch (MissingResourceException e) {
			// Expected
		}
	}
	
}
