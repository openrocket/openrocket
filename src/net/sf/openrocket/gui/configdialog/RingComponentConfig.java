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
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;

public class RingComponentConfig extends RocketComponentConfig {

	public RingComponentConfig(RocketComponent component) {
		super(component);
	}
	
	
	protected JPanel generalTab(String outer, String inner, String thickness, String length) {
		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]",""));
		DoubleModel m;
		JSpinner spin;
		DoubleModel od=null;
		
		
		//// Outer diameter
		if (outer != null) {
			panel.add(new JLabel(outer));
			
			od  = new DoubleModel(component,"OuterRadius",2,UnitGroup.UNITS_LENGTH,0);
			// Diameter = 2*Radius
			
			spin = new JSpinner(od.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin,"growx");
			
			panel.add(new UnitSelector(od),"growx");
			panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)),"w 100lp, wrap");
			
			if (od.isAutomaticAvailable()) {
				JCheckBox check = new JCheckBox(od.getAutomaticAction());
				check.setText("Automatic");
				panel.add(check,"skip, span 2, wrap");
			}
		}

		
		////  Inner diameter
		if (inner != null) {
			panel.add(new JLabel(inner));
			
			m = new DoubleModel(component,"InnerRadius",2,UnitGroup.UNITS_LENGTH,0);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin,"growx");
			
			panel.add(new UnitSelector(m),"growx");
			if (od == null)
				panel.add(new BasicSlider(m.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");
			else
				panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)),
						"w 100lp, wrap");
			
			if (m.isAutomaticAvailable()) {
				JCheckBox check = new JCheckBox(m.getAutomaticAction());
				check.setText("Automatic");
				panel.add(check,"skip, span 2, wrap");
			}
		}
		
		
		////  Wall thickness
		if (thickness != null) {
			panel.add(new JLabel(thickness));
			
			m = new DoubleModel(component,"Thickness",UnitGroup.UNITS_LENGTH,0);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin,"growx");
			
			panel.add(new UnitSelector(m),"growx");
			panel.add(new BasicSlider(m.getSliderModel(0,0.01)),"w 100lp, wrap");
		}

		
		////  Inner tube length
		if (length != null) {
			panel.add(new JLabel(length));
			
			m = new DoubleModel(component,"Length",UnitGroup.UNITS_LENGTH,0);
			
			spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin,"growx");
			
			panel.add(new UnitSelector(m),"growx");
			panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)),"w 100lp, wrap");
		}
		
		
		////  Position
		
		panel.add(new JLabel("Position relative to:"));

		JComboBox combo = new JComboBox(
				new EnumModel<RocketComponent.Position>(component, "RelativePosition",
						new RocketComponent.Position[] {
						RocketComponent.Position.TOP,
						RocketComponent.Position.MIDDLE,
						RocketComponent.Position.BOTTOM,
						RocketComponent.Position.ABSOLUTE
				}));
		panel.add(combo,"spanx 3, growx, wrap");
		
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

		
		//// Material
		panel.add(materialPanel(new JPanel(new MigLayout()), Material.Type.BULK),
				"cell 4 0, gapleft paragraph, aligny 0%, spany");
		
		return panel;
	}
	
	
	protected JPanel positionTab() {
		JPanel panel = new JPanel(new MigLayout("align 20% 20%, gap rel unrel",
				"[][65lp::][30lp::]",""));
		
		////  Radial position
		JLabel l = new JLabel("Radial distance:");
		l.setToolTipText("Distance from the rocket centerline");
		panel.add(l);
		
		DoubleModel m = new DoubleModel(component,"RadialPosition",UnitGroup.UNITS_LENGTH,0);
		
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText("Distance from the rocket centerline");
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		BasicSlider bs = new BasicSlider(m.getSliderModel(0, 0.1, 1.0));
		bs.setToolTipText("Distance from the rocket centerline");
		panel.add(bs,"w 100lp, wrap");
		
		
		//// Radial direction
		l = new JLabel("Radial direction:");
		l.setToolTipText("The radial direction from the rocket centerline");
		panel.add(l);
		
		m = new DoubleModel(component,"RadialDirection",UnitGroup.UNITS_ANGLE,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		spin.setToolTipText("The radial direction from the rocket centerline");
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		bs = new BasicSlider(m.getSliderModel(-Math.PI, Math.PI));
		bs.setToolTipText("The radial direction from the rocket centerline");
		panel.add(bs,"w 100lp, wrap");

		
		//// Reset button
		JButton button = new JButton("Reset");
		button.setToolTipText("Reset the component to the rocket centerline");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				((RingComponent) component).setRadialDirection(0.0);
				((RingComponent) component).setRadialPosition(0.0);
			}
		});
		panel.add(button,"spanx, right");
		
		
		return panel;
	}

}
