package net.sf.openrocket.util;

import java.util.Arrays;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

/**
 * An enumeration of line styles.  The line styles are defined by an array of
 * floats suitable for <code>BasicStroke</code>.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public enum LineStyle {
	

	//// Solid
	SOLID("LineStyle.Solid", new float[] { 10f, 0f }),
	//// Dashed
	DASHED("LineStyle.Dashed", new float[] { 6f, 4f }),
	//// Dotted
	DOTTED("LineStyle.Dotted", new float[] { 2f, 3f }),
	//// Dash-dotted
	DASHDOT("LineStyle.Dash-dotted", new float[] { 8f, 3f, 2f, 3f });
	
	private static final Translator trans = Application.getTranslator();
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
		return trans.get(name);
	}
}