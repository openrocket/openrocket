package info.openrocket.core.file.svg.export;

import java.util.Objects;

import info.openrocket.core.util.ORColor;

/**
 * Immutable container describing how SVG outlines should be rendered.
 * At the moment it only tracks stroke characteristics but it can be
 * extended later (fill colors, dash styles, etc.).
 */
public class SVGExportOptions {
	private final ORColor strokeColor;
	private final double strokeWidthMm;
	private final boolean drawCrosshair;
	private final ORColor crosshairColor;
	private final double crosshairSizeMm;
	private final boolean showLabels;
	private final ORColor labelColor;
	private final double partSpacingM;

	public SVGExportOptions(ORColor strokeColor, double strokeWidthMm) {
		this(strokeColor, strokeWidthMm, true, strokeColor, 2.0, true, strokeColor, 0.01);
	}

	public SVGExportOptions(ORColor strokeColor, double strokeWidthMm, boolean drawCrosshair) {
		this(strokeColor, strokeWidthMm, drawCrosshair, strokeColor, 2.0, true, strokeColor, 0.01);
	}

	public SVGExportOptions(ORColor strokeColor, double strokeWidthMm, boolean drawCrosshair, ORColor crosshairColor) {
		this(strokeColor, strokeWidthMm, drawCrosshair, crosshairColor, 2.0, true, strokeColor, 0.01);
	}

	public SVGExportOptions(ORColor strokeColor, double strokeWidthMm, boolean drawCrosshair, ORColor crosshairColor, double crosshairSizeMm, boolean showLabels) {
		this(strokeColor, strokeWidthMm, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, strokeColor, 0.01);
	}

	public SVGExportOptions(ORColor strokeColor, double strokeWidthMm, boolean drawCrosshair, ORColor crosshairColor, double crosshairSizeMm, boolean showLabels, ORColor labelColor) {
		this(strokeColor, strokeWidthMm, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, labelColor, 0.01);
	}

	public SVGExportOptions(ORColor strokeColor, double strokeWidthMm, boolean drawCrosshair, ORColor crosshairColor, double crosshairSizeMm, boolean showLabels, ORColor labelColor, double partSpacingM) {
		this.strokeColor = Objects.requireNonNull(strokeColor, "strokeColor");
		this.strokeWidthMm = strokeWidthMm;
		this.drawCrosshair = drawCrosshair;
		this.crosshairColor = Objects.requireNonNull(crosshairColor, "crosshairColor");
		this.crosshairSizeMm = crosshairSizeMm;
		this.showLabels = showLabels;
		this.labelColor = Objects.requireNonNull(labelColor, "labelColor");
		this.partSpacingM = partSpacingM;
	}

	public ORColor getStrokeColor() {
		return strokeColor;
	}

	public double getStrokeWidthMm() {
		return strokeWidthMm;
	}

	public boolean isDrawCrosshair() {
		return drawCrosshair;
	}

	public ORColor getCrosshairColor() {
		return crosshairColor;
	}

	public double getCrosshairSizeMm() {
		return crosshairSizeMm;
	}

	public boolean isShowLabels() {
		return showLabels;
	}

	public ORColor getLabelColor() {
		return labelColor;
	}

	public SVGExportOptions withStrokeColor(ORColor color) {
		return new SVGExportOptions(color, strokeWidthMm, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, labelColor, partSpacingM);
	}

	public SVGExportOptions withStrokeWidth(double strokeWidth) {
		return new SVGExportOptions(strokeColor, strokeWidth, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, labelColor, partSpacingM);
	}

	public SVGExportOptions withDrawCrosshair(boolean drawCrosshair) {
		return new SVGExportOptions(strokeColor, strokeWidthMm, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, labelColor, partSpacingM);
	}

	public SVGExportOptions withCrosshairColor(ORColor color) {
		return new SVGExportOptions(strokeColor, strokeWidthMm, drawCrosshair, color, crosshairSizeMm, showLabels, labelColor, partSpacingM);
	}

	public SVGExportOptions withCrosshairSize(double crosshairSizeMm) {
		return new SVGExportOptions(strokeColor, strokeWidthMm, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, labelColor, partSpacingM);
	}

	public SVGExportOptions withShowLabels(boolean showLabels) {
		return new SVGExportOptions(strokeColor, strokeWidthMm, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, labelColor, partSpacingM);
	}

	public SVGExportOptions withLabelColor(ORColor color) {
		return new SVGExportOptions(strokeColor, strokeWidthMm, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, color, partSpacingM);
	}

	public double getPartSpacingM() {
		return partSpacingM;
	}

	public SVGExportOptions withPartSpacing(double partSpacingM) {
		return new SVGExportOptions(strokeColor, strokeWidthMm, drawCrosshair, crosshairColor, crosshairSizeMm, showLabels, labelColor, partSpacingM);
	}
}
