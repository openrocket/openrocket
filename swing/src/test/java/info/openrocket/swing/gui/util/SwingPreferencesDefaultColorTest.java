package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

public class SwingPreferencesDefaultColorTest extends BaseTestCase {

	@Test
	public void defaultColorFollowsStoredColor() {
		SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
		ORColor original = preferences.getDefaultColor(BodyTube.class);
		try {
			preferences.setDefaultColor(BodyTube.class, new ORColor(10, 20, 30, 255));
			assertColor(10, 20, 30, 255, preferences.getDefaultColor(BodyTube.class));

			preferences.setDefaultColor(BodyTube.class, new ORColor(40, 50, 60, 255));
			assertColor(40, 50, 60, 255, preferences.getDefaultColor(BodyTube.class));
		} finally {
			preferences.setDefaultColor(BodyTube.class, original);
		}
	}

	@Test
	public void modifyingReturnedColorDoesNotChangeDefault() {
		SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
		ORColor color = preferences.getDefaultColor(BodyTube.class);
		int red = color.getRed();

		color.setRed((red + 1) % 256);

		assertEquals(red, preferences.getDefaultColor(BodyTube.class).getRed());
	}

	private static void assertColor(int red, int green, int blue, int alpha, ORColor color) {
		assertEquals(red, color.getRed());
		assertEquals(green, color.getGreen());
		assertEquals(blue, color.getBlue());
		assertEquals(alpha, color.getAlpha());
	}
}
