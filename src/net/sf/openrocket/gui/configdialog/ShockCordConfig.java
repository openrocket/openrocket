package net.sf.openrocket.gui.configdialog;


import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.BasicSlider;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.UnitSelector;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;

public class ShockCordConfig extends RocketComponentConfig {


	public ShockCordConfig(RocketComponent component) {
		super(component);
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]",""));
		JLabel label;
		DoubleModel m;
		JSpinner spin;
		String tip;
		
		
		//////  Left side
		
		// Cord length
		label = new JLabel("Shock cord length");
		panel.add(label);
		
		m = new DoubleModel(component,"CordLength",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 1, 10)),"w 100lp, wrap");
		

		// Material
		materialPanel(panel, Material.Type.LINE, "Shock cord material:", null);
		

		
		/////  Right side
		JPanel panel2 = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]",""));
		panel.add(panel2, "cell 4 0, gapleft paragraph, aligny 0%, spany");
		
		
		////  Position
		
		panel2.add(new JLabel("Position relative to:"));

		JComboBox combo = new JComboBox(
				new EnumModel<RocketComponent.Position>(component, "RelativePosition",
						new RocketComponent.Position[] {
						RocketComponent.Position.TOP,
						RocketComponent.Position.MIDDLE,
						RocketComponent.Position.BOTTOM,
						RocketComponent.Position.ABSOLUTE
				}));
		panel2.add(combo,"spanx, growx, wrap");
		
		panel2.add(new JLabel("plus"),"right");

		m = new DoubleModel(component,"PositionValue",UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel2.add(spin,"growx");
		
		panel2.add(new UnitSelector(m),"growx");
		panel2.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap");


		////  Spatial length
		panel2.add(new JLabel("Packed length:"));
		
		m = new DoubleModel(component,"Length",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel2.add(spin,"growx");
		
		panel2.add(new UnitSelector(m),"growx");
		panel2.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)),"w 100lp, wrap");
		
		
		//// Tube diameter
		panel2.add(new JLabel("Packed diameter:"));

		DoubleModel od  = new DoubleModel(component,"Radius",2,UnitGroup.UNITS_LENGTH,0);
		// Diameter = 2*Radius

		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel2.add(spin,"growx");
		
		panel2.add(new UnitSelector(od),"growx");
		panel2.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)),"w 100lp, wrap");

		
		
		
		tabbedPane.insertTab("General", null, panel, "General properties", 0);
//		tabbedPane.insertTab("Radial position", null, positionTab(), 
//				"Radial position configuration", 1);
		tabbedPane.setSelectedIndex(0);
	}
	
	
}
