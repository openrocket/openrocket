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
		assertEquals("messages_de.properties", trans.get("debug.currentFile"));
	}

	@Test
	public void testSuccessfulNonDefault() {
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

	@Test
	public void testGetEnglish() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages", Locale.FRENCH);
		assertEquals("Papier (bureau)", trans.get("material", "Paper (office)"));
		assertEquals("Paper (toilet)", trans.get("material", "Paper (toilet)"));
	}

	@Test
	public void testGetBase() {
		ResourceBundleTranslator trans = new ResourceBundleTranslator("l10n.messages", Locale.FRENCH);
		assertEquals("Paper (office)", trans.getBaseText("material", "Papier (bureau)"));
		assertEquals("Papier (toilet)", trans.getBaseText("material", "Papier (toilet)"));
	}

}
