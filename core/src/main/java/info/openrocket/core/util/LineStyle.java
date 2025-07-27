package info.openrocket.core.util;

import java.util.Arrays;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

/**
 * An enumeration of line styles. The line styles are defined by an array of
 * floats suitable for <code>BasicStroke</code>.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public enum LineStyle {

	//// Solid
	SOLID("LineStyle.Solid", new float[] {10.0f, 0.0f}),
	//// Dashed
	DASHED("LineStyle.Dashed", new float[] {6.0f, 4.0f}),
	//// Dotted
	DOTTED("LineStyle.Dotted", new float[] {2.0f, 3.0f}),
	//// Dash-dotted
	DASHDOT("LineStyle.Dash-dotted", new float[] {8.0f, 3.0f, 2.0f, 3.0f});

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