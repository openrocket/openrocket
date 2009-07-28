package net.sf.openrocket.gui.configdialog;


import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;

public class EllipticalFinSetConfig extends FinSetConfig {

	public EllipticalFinSetConfig(final RocketComponent component) {
		super(component);

		DoubleModel m;
		JSpinner spin;
		JComboBox combo;
		
		JPanel mainPanel = new JPanel(new MigLayout());
		
		
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]",""));
		
		////  Number of fins
		panel.add(new JLabel("Number of fins:"));
		
		IntegerModel im = new IntegerModel(component,"FinCount",1,8);
		
		spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx, wrap");
		
		
		////  Base rotation
		panel.add(new JLabel("Rotation:"));
		
		m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE,-Math.PI,Math.PI);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI,Math.PI)),"w 100lp, wrap");
		
		
		////  Root chord
		panel.add(new JLabel("Root chord:"));
		
		m  = new DoubleModel(component,"Length",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0,0.05,0.2)),"w 100lp, wrap");


		////  Height
		panel.add(new JLabel("Height:"));
		
		m = new DoubleModel(component,"Height",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0,0.05,0.2)),"w 100lp, wrap");
	
		
		////  Position
		
		panel.add(new JLabel("Position relative to:"));

		combo = new JComboBox(
				new EnumModel<RocketComponent.Position>(component, "RelativePosition",
						new RocketComponent.Position[] {
						RocketComponent.Position.TOP,
						RocketComponent.Position.MIDDLE,
						RocketComponent.Position.BOTTOM,
						RocketComponent.Position.ABSOLUTE
				}));
		panel.add(combo,"spanx, growx, wrap");
		
		panel.add(new JLabel("plus"),"right");

		m = new DoubleModel(component,"PositionValue",UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap");


		
		//// Right portion
		mainPanel.add(panel,"aligny 20%");
		
		mainPanel.add(new JSeparator(SwingConstants.VERTICAL),"growy");
		
		
		
		panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]",""));


		////  Cross section
		panel.add(new JLabel("Fin cross section:"),"span, split");
		combo = new JComboBox(
				new EnumModel<FinSet.CrossSection>(component,"CrossSection"));
		panel.add(combo,"growx, wrap unrel");
		

		////  Thickness
		panel.add(new JLabel("Thickness:"));
		
		m = new DoubleModel(component,"Thickness",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0,0.01)),"w 100lp, wrap 30lp");
		
		

		//// Material
		materialPanel(panel, Material.Type.BULK);
		
		
		
		
		
		mainPanel.add(panel,"aligny 20%");
		
		addFinSetButtons();


		tabbedPane.insertTab("General", null, mainPanel, "General properties", 0);
		tabbedPane.setSelectedIndex(0);
	}

}
