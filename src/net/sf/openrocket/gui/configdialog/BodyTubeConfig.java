package net.sf.openrocket.gui.configdialog;


import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.BasicSlider;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.UnitSelector;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;

public class BodyTubeConfig extends RocketComponentConfig {

	private MotorConfig motorConfigPane = null;

	public BodyTubeConfig(RocketComponent c) {
		super(c);
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::][]",""));
		
		////  Body tube length
		panel.add(new JLabel("Body tube length:"));
		
		DoubleModel m = new DoubleModel(component,"Length",UnitGroup.UNITS_LENGTH,0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.5, 2.0)),"w 100lp, wrap");
		
		
		//// Body tube diameter
		panel.add(new JLabel("Outer diameter:"));

		DoubleModel od  = new DoubleModel(component,"Radius",2,UnitGroup.UNITS_LENGTH,0);
		// Diameter = 2*Radius

		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(od),"growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)),"w 100lp, wrap 0px");

		JCheckBox check = new JCheckBox(od.getAutomaticAction());
		check.setText("Automatic");
		panel.add(check,"skip, span 2, wrap");
		
		
		////  Inner diameter
		panel.add(new JLabel("Inner diameter:"));

		// Diameter = 2*Radius
		m = new DoubleModel(component,"InnerRadius",2,UnitGroup.UNITS_LENGTH,0);
		

		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)),"w 100lp, wrap");

		
		////  Wall thickness
		panel.add(new JLabel("Wall thickness:"));
		
		m = new DoubleModel(component,"Thickness",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0,0.01)),"w 100lp, wrap 0px");
		
		
		check = new JCheckBox(new BooleanModel(component,"Filled"));
		check.setText("Filled");
		panel.add(check,"skip, span 2, wrap");
		
		
		//// Material
		panel.add(materialPanel(new JPanel(new MigLayout()), Material.Type.BULK),
				"cell 4 0, gapleft paragraph, aligny 0%, spany");
		

		tabbedPane.insertTab("General", null, panel, "General properties", 0);
		motorConfigPane = new MotorConfig((BodyTube)c);
		tabbedPane.insertTab("Motor", null, motorConfigPane, "Motor mount configuration", 1);
		tabbedPane.setSelectedIndex(0);
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
		if (motorConfigPane != null)
			motorConfigPane.updateFields();
	}
	
}
