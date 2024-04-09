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
}
