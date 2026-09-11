package info.openrocket.core.document;

import info.openrocket.core.preferences.DocumentPreferences;
import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenRocketDocumentPreferencesTest extends BaseTestCase {

	@Test
	void changingSavedViewPreferenceMarksDocumentChanged() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		DocumentPreferences preferences = document.getDocumentPreferences();
		AtomicInteger changeEvents = new AtomicInteger();
		document.addDocumentChangeListener(event -> {
			assertSame(preferences, event.getSource());
			changeEvents.incrementAndGet();
		});

		assertTrue(document.isSaved());
		preferences.putColor(DocumentPreferences.PREF_3D_BACKGROUND_COLOR, Color.BLUE);

		assertFalse(document.isSaved());
		assertEquals(1, changeEvents.get());
	}

	@Test
	void unchangedOrMissingPreferenceDoesNotMarkDocumentChanged() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		DocumentPreferences preferences = document.getDocumentPreferences();
		preferences.putBoolean(DocumentPreferences.PREF_3D_SHADOWS_ENABLED, true);
		document.setSaved(true);
		AtomicInteger changeEvents = new AtomicInteger();
		document.addDocumentChangeListener(event -> changeEvents.incrementAndGet());

		preferences.putBoolean(DocumentPreferences.PREF_3D_SHADOWS_ENABLED, true);
		preferences.removePreference("missing.preference");

		assertTrue(document.isSaved());
		assertEquals(0, changeEvents.get());
	}

	@Test
	void removingSavedViewPreferenceMarksDocumentChanged() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		DocumentPreferences preferences = document.getDocumentPreferences();
		preferences.putBoolean(DocumentPreferences.PREF_3D_SHADOWS_ENABLED, true);
		document.setSaved(true);

		preferences.removePreference(DocumentPreferences.PREF_3D_SHADOWS_ENABLED);

		assertFalse(document.isSaved());
	}
}
