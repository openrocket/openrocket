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
	void clearGraphicsPreferences() {
		preferences.getPreferences().remove(ApplicationPreferences.OPENGL_REDUCE_EFFECTS_DURING_INTERACTION);
		preferences.getPreferences().remove(ApplicationPreferences.OPENGL_ENABLE_MSAA);
	}

	@Test
	void multisamplingIsEnabledByDefaultAndCanBeDisabledIndependentlyOfQuality() {
		GraphicsQualitySettings quality = new GraphicsQualitySettings();
		quality.setQuality(GraphicsQualitySettings.RenderQuality.HIGH);

		assertTrue(quality.isMSAAEnabled());
		assertEquals(4, quality.getSceneSampleCount());

		quality.setMSAAEnabled(false);
		assertEquals(0, quality.getSceneSampleCount());
		assertTrue(quality.isFXAAEnabled(), "disabling MSAA must not also disable FXAA");

		quality.resetToDefaults();
		assertTrue(quality.isMSAAEnabled());
	}

	@Test
	void multisamplingPreferenceIsAppliedToNewConfigurations() {
		Figure3DPreferences.setMSAAEnabled(preferences, false);
		RenderingConfiguration config = new RenderingConfiguration();
		Figure3DPreferences.applyDefaults(config, preferences);

		assertFalse(config.getQuality().isMSAAEnabled());
		assertEquals(0, config.getQuality().getSceneSampleCount());
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
		RenderingConfiguration config = new RenderingConfiguration();
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

	@Test
	void changingMultisamplingNotifiesPreferenceListenersOnce() {
		AtomicInteger notifications = new AtomicInteger();
		StateChangeListener listener = event -> notifications.incrementAndGet();
		preferences.addChangeListener(listener);
		try {
			Figure3DPreferences.setMSAAEnabled(preferences, false);
			Figure3DPreferences.setMSAAEnabled(preferences, false);

			assertEquals(1, notifications.get());
		} finally {
			preferences.removeChangeListener(listener);
		}
	}
}
