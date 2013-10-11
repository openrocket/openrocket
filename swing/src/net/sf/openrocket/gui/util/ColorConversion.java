package net.sf.openrocket.gui.util;

public class ColorConversion {

	public static java.awt.Color toAwtColor( net.sf.openrocket.util.Color c ) {
		if ( c == null ) {
			return null;
		}
		return new java.awt.Color(c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha());
	}
	
	public static net.sf.openrocket.util.Color fromAwtColor( java.awt.Color c ) {
		if ( c == null ) {
			return null;
		}
		return new net.sf.openrocket.util.Color( c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}
}
