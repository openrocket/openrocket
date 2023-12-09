package net.sf.openrocket.gui.util;

import net.sf.openrocket.util.ORColor;

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
}
