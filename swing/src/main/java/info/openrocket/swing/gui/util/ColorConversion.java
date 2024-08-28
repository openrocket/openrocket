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
		return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()).toUpperCase();
	}

	public static ORColor fromHexColor(String hexColor) {
		if (hexColor == null || hexColor.isBlank()) {
			return null;
		}
		if (hexColor.startsWith("#")) {
			hexColor = hexColor.substring(1);
		}
		hexColor = hexColor.trim();
		if (hexColor.length() != 6 || hexColor.matches("^#[0-9A-Fa-f]{6}$")) {
			throw new IllegalArgumentException("Invalid hex color: " + hexColor);
		}
		int red = Integer.parseInt(hexColor.substring(0, 2), 16);
		int green = Integer.parseInt(hexColor.substring(2, 4), 16);
		int blue = Integer.parseInt(hexColor.substring(4, 6), 16);
		return new ORColor(red, green, blue);
	}
}
