package info.openrocket.core.util;

import java.util.Map;
import java.util.Objects;

public class ORColor {

	public static final ORColor BLACK = new ORColor(0, 0, 0);
	public static ORColor INVISIBLE = new ORColor(1, 1, 1, 0);
	public static ORColor DARK_RED = new ORColor(200, 0, 0);

	private int red;
	private int green;
	private int blue;
	private int alpha;

	public ORColor(int red, int green, int blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = 255;
	}

	public ORColor(int red, int green, int blue, int alpha) {
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
		return "ORColor [r=" + red + ", g=" + green + ", b=" + blue + ", a=" + alpha + "]";
	}

	public java.awt.Color toAWTColor() {
		return new java.awt.Color(red, green, blue, alpha);
	}

	public static ORColor fromAWTColor(java.awt.Color AWTColor) {
		return new ORColor(AWTColor.getRed(), AWTColor.getGreen(), AWTColor.getBlue(), AWTColor.getAlpha());
	}

	/**
	 * Parse an ORColor from XML attributes containing "red", "green", "blue", and optionally "alpha" keys.
	 * Returns {@code null} if any required attribute is missing or if any value is not a valid integer in [0, 255].
	 *
	 * @param attributes the XML attribute map
	 * @return the parsed color, or {@code null} if the attributes are incomplete or invalid
	 */
	public static ORColor fromXMLAttributes(Map<String, String> attributes) {
		String redStr = attributes.get("red");
		String greenStr = attributes.get("green");
		String blueStr = attributes.get("blue");
		if (redStr == null || greenStr == null || blueStr == null) {
			return null;
		}

		int r, g, b;
		try {
			r = Integer.parseInt(redStr);
			g = Integer.parseInt(greenStr);
			b = Integer.parseInt(blueStr);
		} catch (NumberFormatException e) {
			return null;
		}

		int a = 255;
		String alphaStr = attributes.get("alpha");
		if (alphaStr != null) {
			try {
				a = Integer.parseInt(alphaStr);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		if (r < 0 || g < 0 || b < 0 || a < 0 || r > 255 || g > 255 || b > 255 || a > 255) {
			return null;
		}

		return new ORColor(r, g, b, a);
	}

	/**
	 * Produce the XML attribute fragment for this color: {@code red="R" green="G" blue="B" alpha="A"}.
	 *
	 * @return the XML attribute string (without leading/trailing spaces)
	 */
	public String toXMLAttributes() {
		return "red=\"" + red + "\" green=\"" + green + "\" blue=\"" + blue + "\" alpha=\"" + alpha + "\"";
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (!(obj instanceof ORColor)) {
			return false;
		}
		ORColor c = (ORColor) obj;
		return c.getRed() == getRed() && c.getGreen() == getGreen() && c.getBlue() == getBlue()
				&& c.getAlpha() == getAlpha();
	}

	@Override
	public int hashCode() {
		return Objects.hash(red, green, blue, alpha);
	}
}
