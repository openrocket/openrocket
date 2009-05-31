package net.sf.openrocket.util;

import java.util.Arrays;

/**
 * An enumeration of line styles.  The line styles are defined by an array of
 * floats suitable for <code>BasicStroke</code>.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public enum LineStyle {
	SOLID("Solid",new float[] { 10f, 0f }),
	DASHED("Dashed",new float[] { 6f, 4f }),
	DOTTED("Dotted",new float[] { 2f, 3f }),
	DASHDOT("Dash-dotted",new float[] { 8f, 3f, 2f, 3f})
	;
	
	private final String name;
	private final float[] dashes;
	LineStyle(String name, float[] dashes) {
		this.name = name;
		this.dashes = dashes;
	}
	public float[] getDashes() {
		return Arrays.copyOf(dashes, dashes.length);
	}
	@Override
	public String toString() {
		return name;
	}
}