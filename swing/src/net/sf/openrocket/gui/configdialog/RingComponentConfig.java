package net.sf.openrocket.gui.configdialog;


import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

@SuppressWarnings("serial")
public class RingComponentConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public RingComponentConfig(OpenRocketDocument d, RocketComponent component, JDialog parent) {
		super(d, component, parent);
	}
	

	protected JPanel generalTab(String outer, String inner, String thickness, String length) {
		JPanel primary = new JPanel(new MigLayout());

		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));
		DoubleModel m;
		JSpinner spin;
		DoubleModel od = null;

		//// Attributes ----

		//// Outer diameter
		if (outer != null) {
			panel.add(new JLabel(outer));
			
			//// OuterRadius
			od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);

			spin = new JSpinner(od.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());
			
			panel.add(new UnitSelector(od), "growx");
			panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");
			
			if (od.isAutomaticAvailable()) {
				JCheckBox check = new JCheckBox(od.getAutomaticAction());
				//// Automatic
				check.setText(trans.get("ringcompcfg.Automatic"));
				check.setToolTipText(trans.get("ringcompcfg.AutomaticOuter.ttip"));
				panel.add(check, "skip, spanx 2, wrap");
				order.add(check);
			}
		}
		
		
		////  Inner diameter
		if (inner != null) {
			panel.add(new JLabel(inner));
			
			//// InnerRadius
			m = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());
			
			panel.add(new UnitSelector(m), "growx");
			if (od == null)
				panel.add(new BasicSlider(m.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");
			else
				panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)),
						"w 100lp, wrap");
			
			if (m.isAutomaticAvailable()) {
				JCheckBox check = new JCheckBox(m.getAutomaticAction());
				//// Automatic
				check.setText(trans.get("ringcompcfg.Automatic"));
				check.setToolTipText(trans.get("ringcompcfg.AutomaticInner.ttip"));
				panel.add(check, "skip, span 2, wrap");
				order.add(check);
			}
		}
		
		
		////  Wall thickness
		if (thickness != null) {
			panel.add(new JLabel(thickness));
			
			//// Thickness
			m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());
			
			panel.add(new UnitSelector(m), "growx");
			panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap");
		}
		
		
		////  Inner tube length
		if (length != null) {
			panel.add(new JLabel(length));
			
			//// Length
			m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			if (component instanceof ThicknessRingComponent) {
				focusElement = spin;
			}
			panel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());
			
			panel.add(new UnitSelector(m), "growx");
			panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 100lp, wrap");
		}

		primary.add(panel, "grow, gapright 40lp");

		// Right side panel
		JPanel rightSide = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));
		primary.add(rightSide, "cell 4 0, aligny 0, spany");

		//// Position
		rightSide.add(new PlacementPanel(component, order), "span, grow");

		//// Material
		MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
		rightSide.add(materialPanel, "span, grow, wrap");

		return primary;
	}
	
	
}
