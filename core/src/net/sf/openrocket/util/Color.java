package net.sf.openrocket.util;

public class Color {

	public static Color BLACK = new Color(255,255,255);
	
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
	
}
