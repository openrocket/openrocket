package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransparencyPolicyTest extends BaseTestCase {

	private static final float EPSILON = 1.0e-6f;

	@Test
	void finishedModeUsesAppearanceOpacity() {
		RenderingConfiguration config = configurationWithMode(DisplaySettings.RenderMode.FINISHED);
		Appearance3D appearance = appearanceWithOpacity(0.42f);

		assertEquals(0.42f,
				TransparencyPolicy.getEffectiveOpacity(new BodyTube(), appearance, config), EPSILON);
		assertEquals(0.42f,
				TransparencyPolicy.getEffectiveOpacity(new Transition(), appearance, config), EPSILON);
		assertEquals(0.42f,
				TransparencyPolicy.getEffectiveOpacity(new NoseCone(), appearance, config), EPSILON);
	}

	@Test
	void xrayModeUsesOneSharedComponentRule() {
		RenderingConfiguration config = configurationWithMode(DisplaySettings.RenderMode.XRAY);
		config.getQuality().setXrayOpacity(0.15f);
		Appearance3D appearance = appearanceWithOpacity(0.42f);

		assertEquals(0.15f,
				TransparencyPolicy.getEffectiveOpacity(new BodyTube(), appearance, config), EPSILON);
		assertEquals(0.15f,
				TransparencyPolicy.getEffectiveOpacity(new Transition(), appearance, config), EPSILON);
		assertEquals(1.0f,
				TransparencyPolicy.getEffectiveOpacity(new NoseCone(), appearance, config), EPSILON);
		assertEquals(1.0f,
				TransparencyPolicy.getEffectiveOpacity(null, appearance, config), EPSILON);
	}

	@Test
	void unfinishedModeOnlyOverridesBodyTubes() {
		RenderingConfiguration config = configurationWithMode(DisplaySettings.RenderMode.UNFINISHED);
		Appearance3D appearance = appearanceWithOpacity(0.42f);

		assertEquals(0.2f,
				TransparencyPolicy.getEffectiveOpacity(new BodyTube(), appearance, config), EPSILON);
		assertEquals(0.42f,
				TransparencyPolicy.getEffectiveOpacity(new Transition(), appearance, config), EPSILON);
		assertEquals(0.42f,
				TransparencyPolicy.getEffectiveOpacity(new NoseCone(), appearance, config), EPSILON);
	}

	@Test
	void bodyAndFigureClassificationsPreserveNoseConeException() {
		BodyTube bodyTube = new BodyTube();
		Transition transition = new Transition();
		NoseCone noseCone = new NoseCone();

		assertTrue(TransparencyPolicy.isFigureTransparentComponent(bodyTube));
		assertTrue(TransparencyPolicy.isFigureTransparentComponent(transition));
		assertFalse(TransparencyPolicy.isFigureTransparentComponent(noseCone));

		assertTrue(TransparencyPolicy.isTransparentBodyComponent(bodyTube));
		assertTrue(TransparencyPolicy.isTransparentBodyComponent(transition));
		assertTrue(TransparencyPolicy.isTransparentBodyComponent(noseCone));
	}

	@Test
	void opacityIndependentTexturesUseAnOpaqueFragmentPass() {
		RenderingConfiguration config = configurationWithMode(DisplaySettings.RenderMode.FINISHED);
		Appearance3D appearance = new Appearance3D(null, Appearance3D.RenderStyle.TEXTURED);
		appearance.setOpacity(0.42f);

		assertTrue(TransparencyPolicy.mayProduceOpaqueTextureFragments(
				new BodyTube(), appearance, config));

		appearance.setOpacityAffectsTexture(true);
		assertFalse(TransparencyPolicy.mayProduceOpaqueTextureFragments(
				new BodyTube(), appearance, config));

		appearance.setOpacityAffectsTexture(false);
		appearance.setOpacity(1.0f);
		assertFalse(TransparencyPolicy.mayProduceOpaqueTextureFragments(
				new BodyTube(), appearance, config));
	}

	private static Appearance3D appearanceWithOpacity(float opacity) {
		Appearance3D appearance = new Appearance3D();
		appearance.setOpacity(opacity);
		return appearance;
	}

	private static RenderingConfiguration configurationWithMode(DisplaySettings.RenderMode mode) {
		RenderingConfiguration config = new RenderingConfiguration();
		config.getDisplay().setMode(mode);
		return config;
	}
}
