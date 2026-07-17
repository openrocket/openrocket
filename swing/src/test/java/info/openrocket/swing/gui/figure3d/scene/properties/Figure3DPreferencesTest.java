package info.openrocket.swing.gui.figure3d.scene.properties;

import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.ServicesForTesting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Figure3DPreferencesTest {
	private final ApplicationPreferences preferences = new ServicesForTesting.PreferencesForTesting();

	@BeforeEach
	@AfterEach
	void clearInteractionEffectPreference() {
		preferences.getPreferences().remove(ApplicationPreferences.OPENGL_REDUCE_EFFECTS_DURING_INTERACTION);
	}

	@Test
	void interactionEffectReductionIsDisabledByDefaultAndAfterReset() {
		GraphicsQualitySettings quality = new GraphicsQualitySettings();

		assertFalse(quality.shouldReduceEffectsDuringInteraction());
		quality.setReduceEffectsDuringInteraction(true);
		quality.resetToDefaults();
		assertFalse(quality.shouldReduceEffectsDuringInteraction());
	}

	@Test
	void interactionEffectReductionPreferenceIsAppliedToNewConfigurations() {
		assertFalse(Figure3DPreferences.shouldReduceEffectsDuringInteraction(preferences));

		Figure3DPreferences.setReduceEffectsDuringInteraction(preferences, true);
		RenderingConfiguration config = RenderingConfiguration.builder().build();
		Figure3DPreferences.applyDefaults(config, preferences);

		assertTrue(config.getQuality().shouldReduceEffectsDuringInteraction());
	}

	@Test
	void changingInteractionEffectReductionNotifiesPreferenceListenersOnce() {
		AtomicInteger notifications = new AtomicInteger();
		StateChangeListener listener = event -> notifications.incrementAndGet();
		preferences.addChangeListener(listener);
		try {
			Figure3DPreferences.setReduceEffectsDuringInteraction(preferences, true);
			Figure3DPreferences.setReduceEffectsDuringInteraction(preferences, true);

			assertEquals(1, notifications.get());
		} finally {
			preferences.removeChangeListener(listener);
		}
	}
}
