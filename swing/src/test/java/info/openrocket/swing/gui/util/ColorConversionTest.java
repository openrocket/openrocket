package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.ORColor;

class ColorConversionTest {

	@Test
	void conversionToAndFromAwtColorPreservesChannels() {
		ORColor color = new ORColor(1, 2, 3, 4);
		Color awt = ColorConversion.toAwtColor(color);

		assertEquals(1, awt.getRed());
		assertEquals(2, awt.getGreen());
		assertEquals(3, awt.getBlue());
		assertEquals(4, awt.getAlpha());

		ORColor converted = ColorConversion.fromAwtColor(awt);
		assertNotSame(color, converted);
		assertEquals(color, converted);
	}

	@Test
	void nullInputsReturnNull() {
		assertNull(ColorConversion.toAwtColor(null));
		assertNull(ColorConversion.fromAwtColor((Color) null));
	}

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
