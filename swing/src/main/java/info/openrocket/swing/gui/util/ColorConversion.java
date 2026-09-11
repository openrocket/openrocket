package info.openrocket.swing.gui.util;

import info.openrocket.core.util.ORColor;

import java.awt.Color;

public class ColorConversion {

	public static java.awt.Color toAwtColor( ORColor c ) {
		if ( c == null ) {
			return null;
		}
		return new java.awt.Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
	}
	
	public static ORColor fromAwtColor(java.awt.Color c ) {
		if ( c == null ) {
			return null;
		}
		return new ORColor( c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}

	public static String formatHTMLColor(Color c, String content) {
		if (c == null) {
			return null;
		}
		String hexColor = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
		return String.format("<font color=\"%s\">%s</font>", hexColor, content);
	}

	public static String toHexColor(ORColor c) {
		if (c == null) {
			return null;
		}
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		if (((r>>4) == (r&15)) &&
			((g>>4) == (g&15)) &&
			((b>>4) == (b&15))) {
			return String.format("#%01X%01X%01X", r&15, g&15, b&15);
		}
		else {
			return String.format("#%02X%02X%02X", r, g, b);
		}
	}

	public static Color brightenColor(Color color, int amount) {
		return new Color(
				Math.min(255, color.getRed() + amount),
				Math.min(255, color.getGreen() + amount),
				Math.min(255, color.getBlue() + amount));
	}

	/**
	 * Composite a possibly translucent foreground color over an opaque background color.
	 * The returned color is opaque, making it safe to use as the background of an opaque Swing component.
	 *
	 * @param foreground the color drawn over the background
	 * @param background the opaque color below the foreground
	 * @return the opaque composite color
	 */
	public static Color compositeColor(Color foreground, Color background) {
		int foregroundAlpha = foreground.getAlpha();
		int backgroundAlpha = 255 - foregroundAlpha;
		return new Color(
				compositeChannel(foreground.getRed(), background.getRed(), foregroundAlpha, backgroundAlpha),
				compositeChannel(foreground.getGreen(), background.getGreen(), foregroundAlpha, backgroundAlpha),
				compositeChannel(foreground.getBlue(), background.getBlue(), foregroundAlpha, backgroundAlpha));
	}

	/**
	 * Composite one foreground color channel over its background channel with integer rounding.
	 */
	private static int compositeChannel(int foreground, int background, int foregroundAlpha, int backgroundAlpha) {
		return (foreground * foregroundAlpha + background * backgroundAlpha + 127) / 255;
	}

	public static ORColor fromHexColor(String hexColor) {
		if (hexColor == null || hexColor.isBlank()) {
			return null;
		}
		if (hexColor.startsWith("#")) {
			hexColor = hexColor.substring(1);
		}
		hexColor = hexColor.trim();
		int red, green, blue;
		if (hexColor.matches("^[0-9A-Fa-f]{3}$")) {
			int color = Integer.parseInt(hexColor, 16);
			red = (color >> 8);
			red = (red << 4) + red;
			green = (color >> 4) & 15;
			green = (green << 4) + green;
			blue = (color & 15);
			blue = (blue << 4) + blue;
		}
		else if (hexColor.matches("^[0-9A-Fa-f]{6}$")) {
			red = Integer.parseInt(hexColor.substring(0, 2), 16);
			green = Integer.parseInt(hexColor.substring(2, 4), 16);
			blue = Integer.parseInt(hexColor.substring(4, 6), 16);
		}
		else {
			throw new IllegalArgumentException("Invalid hex color: " + hexColor);
		}
		return new ORColor(red, green, blue);
	}
}
