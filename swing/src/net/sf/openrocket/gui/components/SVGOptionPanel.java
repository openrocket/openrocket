package net.sf.openrocket.gui.components;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.UnitGroup;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import java.awt.Color;

public class SVGOptionPanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	private static final Preferences prefs = Application.getPreferences();

	private final ColorChooserButton colorChooser;
	private double strokeWidth = 0.1;

	public SVGOptionPanel() {
		super(new MigLayout());

		// Stroke color
		JLabel label = new JLabel(trans.get("SVGOptionPanel.lbl.strokeColor"));
		label.setToolTipText(trans.get("SVGOptionPanel.lbl.strokeColor.ttip"));
		add(label);
		colorChooser = new ColorChooserButton(prefs.getSVGStrokeColor());
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
		add(spin);
		add(new UnitSelector(dm), "growx, wrap");
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
}
