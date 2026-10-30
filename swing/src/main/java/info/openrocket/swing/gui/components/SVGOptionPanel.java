package info.openrocket.swing.gui.components;

import info.openrocket.core.preferences.ApplicationPreferences;
import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import java.awt.Color;

import info.openrocket.swing.gui.util.ColorConversion;

public class SVGOptionPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private final ColorChooserButton colorChooser;
	private ColorChooserButton crosshairColorChooser;
	private JLabel crosshairColorLabel;
	private JLabel crosshairSizeLabel;
	private ColorChooserButton labelColorChooser;
	private JLabel labelColorLabel;
	private double strokeWidth = 0.1;
	private boolean drawCrosshair = true;
	private double crosshairSize = 2.0; // Default 2mm crosshair size in mm
	private boolean showLabels = true;
	private final boolean showCrosshairToggle;
	private JCheckBox crosshairCheckbox;
	private JCheckBox showLabelsCheckbox;
	private DoubleModel crosshairSizeModel;
	private JSpinner crosshairSizeSpinner;
	private UnitSelector crosshairSizeUnitSelector;
	
	// Part spacing
	private double partSpacing = 0.01; // Default 10mm spacing in meters
	private DoubleModel spacingModel;
	private JLabel spacingLabel;
	private JSpinner spacingSpinner;
	private UnitSelector spacingUnitSelector;

	public SVGOptionPanel() {
		this(false);
	}

	public SVGOptionPanel(boolean showCrosshairToggle) {
		super(new MigLayout());
		this.showCrosshairToggle = showCrosshairToggle;
		
		// Load part spacing and crosshair size from preferences
		partSpacing = prefs.getSVGPartSpacing();
		crosshairSize = prefs.getSVGCrosshairSize();
		
		// Stroke color
		JLabel label = new JLabel(trans.get("SVGOptionPanel.lbl.strokeColor"));
		label.setToolTipText(trans.get("SVGOptionPanel.lbl.strokeColor.ttip"));
		add(label);
		colorChooser = new ColorChooserButton(ColorConversion.toAwtColor(prefs.getSVGStrokeColor()));
		colorChooser.setToolTipText(trans.get("SVGOptionPanel.lbl.strokeColor.ttip"));
		add(colorChooser, "wrap");

		// Stroke width
		label = new JLabel(trans.get("SVGOptionPanel.lbl.strokeWidth"));
		label.setToolTipText(trans.get("SVGOptionPanel.lbl.strokeWidth.ttip"));
		add(label);
		DoubleModel dm = new DoubleModel(this, "StrokeWidth", UnitGroup.UNITS_STROKE_WIDTH, 0.001, 10);
		dm.setValue(prefs.getSVGStrokeWidth());
		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setToolTipText(trans.get("SVGOptionPanel.lbl.strokeWidth.ttip"));
		spin.setEditor(new SpinnerEditor(spin, 5));
		add(spin, "split 2");
		add(new UnitSelector(dm), "growx, wrap para");

		if (showCrosshairToggle) {
			crosshairCheckbox = new JCheckBox(trans.get("SVGOptionPanel.lbl.crosshair"));
			crosshairCheckbox.setToolTipText(trans.get("SVGOptionPanel.lbl.crosshair.ttip"));
			drawCrosshair = prefs.isSVGDrawCrosshair();
			crosshairCheckbox.setSelected(drawCrosshair);
			add(crosshairCheckbox, "span, wrap");

			crosshairColorLabel = new JLabel(trans.get("SVGOptionPanel.lbl.crosshairColor"));
			crosshairColorLabel.setToolTipText(trans.get("SVGOptionPanel.lbl.crosshairColor.ttip"));
			add(crosshairColorLabel);
			crosshairColorChooser = new ColorChooserButton(ColorConversion.toAwtColor(prefs.getSVGCrosshairColor()));
			crosshairColorChooser.setToolTipText(trans.get("SVGOptionPanel.lbl.crosshairColor.ttip"));
			add(crosshairColorChooser, "wrap");

			// Crosshair size (in mm, length of one full crosshair line)
			crosshairSizeLabel = new JLabel(trans.get("SVGOptionPanel.lbl.crosshairSize"));
			crosshairSizeLabel.setToolTipText(trans.get("SVGOptionPanel.lbl.crosshairSize.ttip"));
			add(crosshairSizeLabel);
			crosshairSizeModel = new DoubleModel(this, "CrosshairSize", UnitGroup.UNITS_LENGTH, 0.0001, 0.1); // 0.1mm to 100mm in meters
			crosshairSizeModel.setValue(crosshairSize / 1000.0); // Convert mm to meters for model
			crosshairSizeSpinner = new JSpinner(crosshairSizeModel.getSpinnerModel());
			crosshairSizeSpinner.setToolTipText(trans.get("SVGOptionPanel.lbl.crosshairSize.ttip"));
			crosshairSizeSpinner.setEditor(new SpinnerEditor(crosshairSizeSpinner, 5));
			crosshairSizeUnitSelector = new UnitSelector(crosshairSizeModel);
			add(crosshairSizeSpinner, "split 2");
			add(crosshairSizeUnitSelector, "growx, wrap para");

			crosshairCheckbox.addActionListener(e -> {
				drawCrosshair = crosshairCheckbox.isSelected();
				updateCrosshairInputs();
			});
			updateCrosshairInputs();
		}

		// Show labels checkbox (always shown)
		showLabelsCheckbox = new JCheckBox(trans.get("SVGOptionPanel.lbl.showLabels"));
		showLabelsCheckbox.setToolTipText(trans.get("SVGOptionPanel.lbl.showLabels.ttip"));
		showLabels = prefs.isSVGShowLabels();
		showLabelsCheckbox.setSelected(showLabels);
		add(showLabelsCheckbox, "spanx, wrap");

		// Label color
		labelColorLabel = new JLabel(trans.get("SVGOptionPanel.lbl.labelColor"));
		labelColorLabel.setToolTipText(trans.get("SVGOptionPanel.lbl.labelColor.ttip"));
		add(labelColorLabel);
		labelColorChooser = new ColorChooserButton(ColorConversion.toAwtColor(prefs.getSVGLabelColor()));
		labelColorChooser.setToolTipText(trans.get("SVGOptionPanel.lbl.labelColor.ttip"));
		add(labelColorChooser, "wrap para");

		//// Enable/disable label color controls based on checkbox
		showLabelsCheckbox.addActionListener(e -> {
			boolean enabled = showLabelsCheckbox.isSelected();
			labelColorLabel.setEnabled(enabled);
			labelColorChooser.setEnabled(enabled);
		});

		//// Initial state
		boolean labelsEnabled = showLabelsCheckbox.isSelected();
		labelColorLabel.setEnabled(labelsEnabled);
		labelColorChooser.setEnabled(labelsEnabled);

		// Part spacing
		spacingLabel = new JLabel(trans.get("SVGOptionPanel.lbl.partSpacing"));
		spacingLabel.setToolTipText(trans.get("SVGOptionPanel.lbl.partSpacing.ttip"));
		spacingModel = new DoubleModel(this, "PartSpacing", UnitGroup.UNITS_LENGTH, 0.0, 1.0);
		spacingModel.setValue(partSpacing);
		spacingSpinner = new JSpinner(spacingModel.getSpinnerModel());
		spacingSpinner.setToolTipText(trans.get("SVGOptionPanel.lbl.partSpacing.ttip"));
		spacingSpinner.setEditor(new SpinnerEditor(spacingSpinner, 5));
		spacingUnitSelector = new UnitSelector(spacingModel);
		add(spacingLabel);
		add(spacingSpinner, "split 2");
		add(spacingUnitSelector, "growx, wrap para");
	}

	public Color getStrokeColor() {
		return colorChooser.getSelectedColor();
	}

	public void setStrokeColor(Color color) {
		colorChooser.setSelectedColor(color);
	}

	public double getStrokeWidth() {
		return strokeWidth;
	}

	public void setStrokeWidth(double strokeWidth) {
		this.strokeWidth = strokeWidth;
	}

	public boolean isDrawCrosshair() {
		return showCrosshairToggle ? drawCrosshair : true;
	}

	public void setDrawCrosshair(boolean drawCrosshair) {
		if (showCrosshairToggle) {
			this.drawCrosshair = drawCrosshair;
			if (crosshairCheckbox != null) {
				crosshairCheckbox.setSelected(drawCrosshair);
				updateCrosshairInputs();
			}
		}
	}

	public Color getCrosshairColor() {
		return showCrosshairToggle && crosshairColorChooser != null ?
				crosshairColorChooser.getSelectedColor() : colorChooser.getSelectedColor();
	}

	public void setCrosshairColor(Color color) {
		if (showCrosshairToggle && crosshairColorChooser != null) {
			crosshairColorChooser.setSelectedColor(color);
		}
	}

	public double getCrosshairSize() {
		// DoubleModel expects SI units (meters), but we store in mm
		// Return in meters for DoubleModel
		return crosshairSize / 1000.0;
	}

	// Setter called by DoubleModel - receives value in meters, convert to mm and store
	public void setCrosshairSize(double size) {
		// DoubleModel passes value in SI units (meters), convert to mm
		this.crosshairSize = size * 1000.0;
	}
	
	/**
	 * Get crosshair size in mm (for use in SVGExportOptions).
	 * @return crosshair size in mm
	 */
	public double getCrosshairSizeMm() {
		return crosshairSize;
	}

	public boolean isShowLabels() {
		return showLabelsCheckbox != null ? showLabelsCheckbox.isSelected() : showLabels;
	}

	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
		if (showLabelsCheckbox != null) {
			showLabelsCheckbox.setSelected(showLabels);
			boolean enabled = showLabels;
			if (labelColorLabel != null) {
				labelColorLabel.setEnabled(enabled);
			}
			if (labelColorChooser != null) {
				labelColorChooser.setEnabled(enabled);
			}
		}
	}

	public Color getLabelColor() {
		return labelColorChooser != null ? labelColorChooser.getSelectedColor() : Color.BLACK;
	}

	public void setLabelColor(Color color) {
		if (labelColorChooser != null) {
			labelColorChooser.setSelectedColor(color);
		}
	}

	private void updateCrosshairInputs() {
		boolean enabled = drawCrosshair;
		if (crosshairColorLabel != null) {
			crosshairColorLabel.setEnabled(enabled);
		}
		if (crosshairColorChooser != null) {
			crosshairColorChooser.setEnabled(enabled);
		}
		if (crosshairSizeLabel != null) {
			crosshairSizeLabel.setEnabled(enabled);
		}
		if (crosshairSizeSpinner != null) {
			crosshairSizeSpinner.setEnabled(enabled);
		}
		if (crosshairSizeUnitSelector != null) {
			crosshairSizeUnitSelector.setEnabled(enabled);
		}
	}
	
	public double getPartSpacing() {
		return partSpacing;
	}
	
	// Setter called by DoubleModel - just set the field, don't call the model to avoid infinite loop
	public void setPartSpacing(double spacing) {
		this.partSpacing = spacing;
	}
	
	/**
	 * Store the current settings to user preferences.
	 */
	public void storePreferences() {
		prefs.setSVGPartSpacing(partSpacing);
		if (showCrosshairToggle) {
			prefs.setSVGCrosshairSize(crosshairSize);
		}
	}
}
