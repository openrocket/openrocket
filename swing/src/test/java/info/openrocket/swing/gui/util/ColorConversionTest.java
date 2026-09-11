package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;

import org.junit.jupiter.api.Test;

class ColorConversionTest {

	@Test
	void compositeColorReturnsOpaqueForegroundUnchanged() {
		Color foreground = new Color(12, 34, 56);

		assertEquals(foreground, ColorConversion.compositeColor(foreground, Color.WHITE));
	}

	@Test
	void compositeColorReturnsBackgroundForTransparentForeground() {
		Color background = new Color(12, 34, 56);

		assertEquals(background, ColorConversion.compositeColor(new Color(200, 150, 100, 0), background));
	}

	@Test
	void compositeColorBlendsTranslucentForegroundIntoOpaqueBackground() {
		Color foreground = new Color(80, 45, 10, 60);
		Color background = new Color(73, 76, 79);

		assertEquals(new Color(75, 69, 63), ColorConversion.compositeColor(foreground, background));
	}
}
