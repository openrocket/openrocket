package net.sf.openrocket.util;

public class Color {

	public static Color BLACK = new Color(0,0,0);
	public static Color INVISIBLE = new Color(1, 1, 1, 0);
	public static Color DARK_RED = new Color(200, 0, 0);
	
	private int red;
	private int green;
	private int blue;
	private int alpha;
	
	public Color( int red, int green, int blue ) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = 255;
	}

	public Color( int red, int green, int blue, int alpha ) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	@Override
	public String toString() {
		return "Color [r=" + red + ", g=" + green + ", b=" + blue + ", a=" + alpha + "]";
	}

	public java.awt.Color toAWTColor() {
		return new java.awt.Color(red, green, blue, alpha);
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (!(obj instanceof Color)) {
			return false;
		}
		Color c = (Color) obj;
		return c.getRed() == getRed() && c.getGreen() == getGreen() && c.getBlue() == getBlue() && c.getAlpha() == getAlpha();
	}
}
