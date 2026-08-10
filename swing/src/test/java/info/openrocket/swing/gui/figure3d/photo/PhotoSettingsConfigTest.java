package info.openrocket.swing.gui.figure3d.photo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.util.TestRockets;
import info.openrocket.swing.util.BaseTestCase;

class PhotoSettingsConfigTest extends BaseTestCase {
	@Test
	void effectsTabGroupsSharedExhaustScaleAheadOfIndividualEffects() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
			PhotoSettingsConfig config = new PhotoSettingsConfig(new PhotoSettings(), document);

			JPanel effectsPanel = effectsPanel(config);
			assertEquals(2, effectsPanel.getComponentCount());

			JPanel particleEffectsPanel = assertInstanceOf(JPanel.class, effectsPanel.getComponent(0));
			JPanel motionBlurPanel = assertInstanceOf(JPanel.class, effectsPanel.getComponent(1));
			assertNull(particleEffectsPanel.getBorder());
			assertTrue(borderTitle(motionBlurPanel).contains("PhotoSettingsConfig.lbl.motionBlur"));

			JPanel smokePanel = titledPanel(particleEffectsPanel, "PhotoSettingsConfig.lbl.smoke");
			JPanel flamePanel = titledPanel(particleEffectsPanel, "PhotoSettingsConfig.lbl.flame");
			JPanel sparksPanel = titledPanel(particleEffectsPanel, "PhotoSettingsConfig.lbl.sparks");
			int exhaustScaleIndex = labelIndex(particleEffectsPanel,
					"PhotoSettingsConfig.lbl.exhaustScale");
			assertTrue(exhaustScaleIndex < componentIndex(particleEffectsPanel, smokePanel));
			assertTrue(exhaustScaleIndex < componentIndex(particleEffectsPanel, flamePanel));
			assertTrue(exhaustScaleIndex < componentIndex(particleEffectsPanel, sparksPanel));
		});
	}

	@Test
	void exhaustScaleIsEnabledOnlyWhileAtLeastOneParticleEffectIsEnabled() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			PhotoSettings settings = new PhotoSettings();
			settings.setSmoke(false);
			settings.setFlame(false);
			settings.setSparks(false);
			OpenRocketDocument document = OpenRocketDocumentFactory.createDocumentFromRocket(
					TestRockets.makeEstesAlphaIII());
			document.getRocket().setSelectedConfiguration(TestRockets.TEST_FCID_0);
			PhotoSettingsConfig config = new PhotoSettingsConfig(settings, document);
			JPanel particleEffectsPanel = assertInstanceOf(JPanel.class,
					effectsPanel(config).getComponent(0));

			assertExhaustScaleEnabled(particleEffectsPanel, false);
			settings.setSmoke(true);
			assertExhaustScaleEnabled(particleEffectsPanel, true);
			settings.setSmoke(false);
			assertExhaustScaleEnabled(particleEffectsPanel, false);
			settings.setFlame(true);
			assertExhaustScaleEnabled(particleEffectsPanel, true);
			settings.setSparks(true);
			settings.setFlame(false);
			assertExhaustScaleEnabled(particleEffectsPanel, true);
			settings.setSparks(false);
			assertExhaustScaleEnabled(particleEffectsPanel, false);
		});
	}

	private static JPanel effectsPanel(PhotoSettingsConfig config) {
		JScrollPane effectsScrollPane = assertInstanceOf(JScrollPane.class,
				config.getComponentAt(config.getTabCount() - 1));
		return assertInstanceOf(JPanel.class, effectsScrollPane.getViewport().getView());
	}

	private static void assertExhaustScaleEnabled(JPanel particleEffectsPanel, boolean expected) {
		int exhaustScaleIndex = labelIndex(particleEffectsPanel,
				"PhotoSettingsConfig.lbl.exhaustScale");
		assertEquals(expected, particleEffectsPanel.getComponent(exhaustScaleIndex).isEnabled());
		assertEquals(expected, particleEffectsPanel.getComponent(exhaustScaleIndex + 1).isEnabled());
		assertEquals(expected, particleEffectsPanel.getComponent(exhaustScaleIndex + 2).isEnabled());
	}

	private static String borderTitle(JPanel panel) {
		return assertInstanceOf(TitledBorder.class, panel.getBorder()).getTitle();
	}

	private static int labelIndex(JPanel panel, String translationKey) {
		Component[] components = panel.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JLabel label && label.getText().contains(translationKey)) {
				return i;
			}
		}
		throw new AssertionError("No label found for translation key: " + translationKey);
	}

	private static JPanel titledPanel(JPanel parent, String translationKey) {
		for (Component component : parent.getComponents()) {
			if (component instanceof JPanel panel
					&& panel.getBorder() instanceof TitledBorder border
					&& border.getTitle().contains(translationKey)) {
				return panel;
			}
		}
		throw new AssertionError("No titled panel found for translation key: " + translationKey);
	}

	private static int componentIndex(JPanel parent, Component target) {
		Component[] components = parent.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] == target) {
				return i;
			}
		}
		throw new AssertionError("Component is not a direct child of the expected panel");
	}
}
