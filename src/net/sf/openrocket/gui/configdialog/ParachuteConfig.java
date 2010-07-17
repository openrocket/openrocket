package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.adaptors.MaterialModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.HtmlLabel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.MotorMount.IgnitionEvent;
import net.sf.openrocket.unit.UnitGroup;

public class ParachuteConfig extends RecoveryDeviceConfig {

	public ParachuteConfig(final RocketComponent component) {
		super(component);

		JPanel primary = new JPanel(new MigLayout());
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::][]",""));
		
		
		//// Canopy
		panel.add(new StyledLabel("Canopy:", Style.BOLD), "wrap unrel");
		

		panel.add(new JLabel("Diameter:"));
		
		DoubleModel m = new DoubleModel(component,"Diameter",UnitGroup.UNITS_LENGTH,0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.4, 1.5)),"w 100lp, wrap");

		
		panel.add(new JLabel("Material:"));
		
		JComboBox combo = new JComboBox(new MaterialModel(panel, component, 
				Material.Type.SURFACE));
		combo.setToolTipText("The component material affects the weight of the component.");
		panel.add(combo,"spanx 3, growx, wrap paragraph");

//		materialPanel(panel, Material.Type.SURFACE, "Material:", null);
		
		
		
		// CD
		JLabel label = new HtmlLabel("<html>Drag coefficient C<sub>D</sub>:");
		String tip = "<html>The drag coefficient relative to the total area of the parachute.<br>" +
				"A larger drag coefficient yields a slowed descent rate.  " +
				"A typical value for parachutes is 0.8.";
		label.setToolTipText(tip);
		panel.add(label);
		
		m = new DoubleModel(component,"CD",UnitGroup.UNITS_COEFFICIENT,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setToolTipText(tip);
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		JButton button = new JButton("Reset");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Parachute p = (Parachute)component;
				p.setCD(Parachute.DEFAULT_CD);
			}
		});
		panel.add(button,"spanx, wrap 30lp");

		
		
		////  Shroud lines
		panel.add(new StyledLabel("Shroud lines:", Style.BOLD), "wrap unrel");


		panel.add(new JLabel("Number of lines:"));
		IntegerModel im = new IntegerModel(component,"LineCount",0);
		
		spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx, wrap");
		
		
		panel.add(new JLabel("Line length:"));

		m = new DoubleModel(component,"LineLength",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.4, 1.5)),"w 100lp, wrap");

		
		panel.add(new JLabel("Material:"));
		
		combo = new JComboBox(new MaterialModel(panel, component, Material.Type.LINE, 
				"LineMaterial"));
		panel.add(combo,"spanx 3, growx, wrap");

		
		
		primary.add(panel, "grow, gapright 20lp");
		panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::][]",""));

		
		
		
		//// Position

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


		////  Spatial length
		panel.add(new JLabel("Packed length:"));
		
		m = new DoubleModel(component,"Length",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)),"w 100lp, wrap");
		
		
		//// Tube diameter
		panel.add(new JLabel("Packed diameter:"));

		DoubleModel od  = new DoubleModel(component,"Radius",2,UnitGroup.UNITS_LENGTH,0);
		// Diameter = 2*Radius

		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(od),"growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)),"w 100lp, wrap 30lp");
		
		
		//// Deployment

		panel.add(new JLabel("Deploys at:"),"");
		
		combo = new JComboBox(new EnumModel<IgnitionEvent>(component, "DeployEvent"));
		panel.add(combo,"spanx 3, growx, wrap");
		
		// ... and delay
		panel.add(new JLabel("plus"),"right");
		
		m = new DoubleModel(component,"DeployDelay",0);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"spanx, split");
		
		panel.add(new JLabel("seconds"),"wrap paragraph");

		// Altitude
		label = new JLabel("Altitude:");
		altitudeComponents.add(label);
		panel.add(label);
		
		m = new DoubleModel(component,"DeployAltitude",UnitGroup.UNITS_DISTANCE,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		altitudeComponents.add(spin);
		panel.add(spin,"growx");
		UnitSelector unit = new UnitSelector(m);
		altitudeComponents.add(unit);
		panel.add(unit,"growx");
		BasicSlider slider = new BasicSlider(m.getSliderModel(100, 1000));
		altitudeComponents.add(slider);
		panel.add(slider,"w 100lp, wrap");

		
		primary.add(panel, "grow");
		
		updateFields();

		tabbedPane.insertTab("General", null, primary, "General properties", 0);
		tabbedPane.insertTab("Radial position", null, positionTab(), 
				"Radial position configuration", 1);
		tabbedPane.setSelectedIndex(0);
	}
	
	
	


	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]",""));
		
		////  Radial position
		panel.add(new JLabel("Radial distance:"));
		
		DoubleModel m = new DoubleModel(component,"RadialPosition",UnitGroup.UNITS_LENGTH,0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)),"w 100lp, wrap");
		
		
		//// Radial direction
		panel.add(new JLabel("Radial direction:"));
		
		m = new DoubleModel(component,"RadialDirection",UnitGroup.UNITS_ANGLE,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)),"w 100lp, wrap");

		
		//// Reset button
		JButton button = new JButton("Reset");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((MassObject) component).setRadialDirection(0.0);
				((MassObject) component).setRadialPosition(0.0);
			}
		});
		panel.add(button,"spanx, right");
		
		return panel;
	}
}
