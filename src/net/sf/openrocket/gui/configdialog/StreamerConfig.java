package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.MaterialModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.ResizeLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.MotorMount.IgnitionEvent;
import net.sf.openrocket.unit.UnitGroup;

public class StreamerConfig extends RecoveryDeviceConfig {

	public StreamerConfig(final RocketComponent component) {
		super(component);

		JPanel primary = new JPanel(new MigLayout());
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::][]",""));
		
		
		
		panel.add(new JLabel("Strip length:"));
		
		DoubleModel m = new DoubleModel(component,"StripLength",UnitGroup.UNITS_LENGTH,0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.6, 1.5)),"w 100lp, wrap");

		
		panel.add(new JLabel("Strip width:"));
		
		m = new DoubleModel(component,"StripWidth",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.2)),"w 100lp, wrap 20lp");

		
		

		panel.add(new JLabel("Strip area:"));
		
		m = new DoubleModel(component,"Area",UnitGroup.UNITS_AREA,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.04, 0.25)),"w 100lp, wrap");

		
		panel.add(new JLabel("Aspect ratio:"));
		
		m = new DoubleModel(component,"AspectRatio",UnitGroup.UNITS_NONE,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
//		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(2, 15)),"skip, w 100lp, wrap 20lp");

		
		
		panel.add(new JLabel("Material:"));
		
		JComboBox combo = new JComboBox(new MaterialModel(panel, component, 
				Material.Type.SURFACE));
		combo.setToolTipText("The component material affects the weight of the component.");
		panel.add(combo,"spanx 3, growx, wrap 20lp");

		
		
		// CD
		JLabel label = new JLabel("<html>Drag coefficient C<sub>D</sub>:");
		String tip = "<html>The drag coefficient relative to the total area of the streamer.<br>" +
				"A larger drag coefficient yields a slowed descent rate.";
		label.setToolTipText(tip);
		panel.add(label);
		
		m = new DoubleModel(component,"CD",UnitGroup.UNITS_COEFFICIENT,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setToolTipText(tip);
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		JCheckBox check = new JCheckBox(m.getAutomaticAction());
		check.setText("Automatic");
		panel.add(check,"skip, span, wrap");
		
		panel.add(new ResizeLabel("The drag coefficient is relative to the area of the streamer.",
				-2), "span, wrap");
		
		
		
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
				((MassComponent) component).setRadialDirection(0.0);
				((MassComponent) component).setRadialPosition(0.0);
			}
		});
		panel.add(button,"spanx, right");
		
		return panel;
	}
}
