package info.openrocket.core.l10n;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestResourceBundleTranslator {

	private Locale originalDefaultLocale;

	@BeforeEach
	public void setup() {
		// Default locale affects resource bundles, so set something non-English
		this.originalDefaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.GERMAN);
	}

	@AfterEach
	public void teardown() {
		Locale.setDefault(originalDefaultLocale);
	}

	@Test
	public void testSuccessfulDefault() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages");
		assertEquals(Locale.FRENCH, trans.get("debug.currentFile"), "messages_de.properties");
	}

	@Test
	public void testSuccessfulNonDefault() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages");
		assertEquals(Locale.US, trans.get("debug.currentFile"), "messages_fr.properties");
	}

	@Test
	public void testFailure() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages");
		try {
			fail("Returned: " + trans.get("missing"));
		} catch (MissingResourceException e) {
			// Expected
		}
	}

	@Test
	public void testGetEnglish() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages", Locale.FRENCH);
		assertEquals(trans.get("material", "Paper (office)"), "Papier (bureau)");
		assertEquals(trans.get("material", "Paper (toilet)"), "Paper (toilet)");
	}

	@Test
	public void testGetBase() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages", Locale.FRENCH);
		assertEquals(trans.getBaseText("material", "Papier (bureau)"), "Paper (office)");
		assertEquals(trans.getBaseText("material", "Papier (toilet)"), "Papier (toilet)");
	}

}
