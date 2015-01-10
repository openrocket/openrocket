package net.sf.openrocket.gui.configdialog;


import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class RingComponentConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public RingComponentConfig(OpenRocketDocument d, RocketComponent component) {
		super(d, component);
	}
	
	
	protected JPanel generalTab(String outer, String inner, String thickness, String length) {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		DoubleModel m;
		JSpinner spin;
		DoubleModel od = null;
		
		
		//// Outer diameter
		if (outer != null) {
			panel.add(new JLabel(outer));
			
			//// OuterRadius
			od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);
			// Diameter = 2*Radius
			
			spin = new JSpinner(od.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx");
			
			panel.add(new UnitSelector(od), "growx");
			panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");
			
			if (od.isAutomaticAvailable()) {
				JCheckBox check = new JCheckBox(od.getAutomaticAction());
				//// Automatic
				check.setText(trans.get("ringcompcfg.Automatic"));
				panel.add(check, "skip, span 2, wrap");
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
				panel.add(check, "skip, span 2, wrap");
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
			panel.add(spin, "growx");
			
			panel.add(new UnitSelector(m), "growx");
			panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 100lp, wrap");
		}
		
		
		////  Position
		
		//// Position relative to:
		panel.add(new JLabel(trans.get("ringcompcfg.Positionrelativeto")));
		
		JComboBox combo = new JComboBox(
				new EnumModel<RocketComponent.Position>(component, "RelativePosition",
						new RocketComponent.Position[] {
								RocketComponent.Position.TOP,
								RocketComponent.Position.MIDDLE,
								RocketComponent.Position.BOTTOM,
								RocketComponent.Position.ABSOLUTE
						}));
		panel.add(combo, "spanx 3, growx, wrap");
		
		//// plus
		panel.add(new JLabel(trans.get("ringcompcfg.plus")), "right");
		
		//// PositionValue
		m = new DoubleModel(component, "PositionValue", UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap");
		
		
		//// Material
		JPanel sub = materialPanel( Material.Type.BULK);
		
		if (component instanceof EngineBlock) {
			final DescriptionArea desc = new DescriptionArea(6);
			//// <html>An <b>engine block</b> stops the motor from moving forwards in the motor mount tube.<br><br>In order to add a motor, create a <b>body tube</b> or <b>inner tube</b> and mark it as a motor mount in the <em>Motor</em> tab.
			desc.setText(trans.get("ringcompcfg.EngineBlock.desc"));
			sub.add(desc, "width 1px, growx, wrap");
		}
		panel.add(sub, "cell 4 0, gapleft paragraph, aligny 0%, spany");
		
		return panel;
	}
	
	
}
