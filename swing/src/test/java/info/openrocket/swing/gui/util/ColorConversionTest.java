package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import info.openrocket.core.util.ORColor;

public class ColorConversionTest {

	@Test
	public void conversionToAndFromAwtColorPreservesChannels() {
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
	public void nullInputsReturnNull() {
		assertNull(ColorConversion.toAwtColor(null));
		assertNull(ColorConversion.fromAwtColor((Color) null));
	}
}
