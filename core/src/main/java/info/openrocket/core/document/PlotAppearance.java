package info.openrocket.core.document;

import java.util.Objects;

import info.openrocket.core.util.LineStyle;
import info.openrocket.core.util.ORColor;

/**
 * Per-series plot appearance overrides (color and/or line style) stored against a simulation.
 * Keyed externally by {@link info.openrocket.core.simulation.FlightDataType#getSymbol()} for stable persistence.
 */
public class PlotAppearance implements Cloneable {
	/** The built-in factory default: no color override and solid line style. */
	public static final PlotAppearance FACTORY_DEFAULT = new PlotAppearance(null, LineStyle.SOLID);

	private ORColor color;
	private LineStyle lineStyle;

	public PlotAppearance(ORColor color, LineStyle lineStyle) {
		this.color = color;
		this.lineStyle = lineStyle;
	}

	public ORColor getColor() {
		return color;
	}

	public void setColor(ORColor color) {
		this.color = color;
	}

	public LineStyle getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	public boolean isEmpty() {
		return color == null && lineStyle == null;
	}

	@Override
	public PlotAppearance clone() {
		ORColor colorCopy = color != null ? new ORColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()) : null;
		return new PlotAppearance(colorCopy, lineStyle);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PlotAppearance other)) {
			return false;
		}
		return Objects.equals(color, other.color) && lineStyle == other.lineStyle;
	}

	@Override
	public int hashCode() {
		return Objects.hash(color, lineStyle);
	}
}
