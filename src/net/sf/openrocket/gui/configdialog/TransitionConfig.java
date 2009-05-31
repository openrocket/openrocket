package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.BasicSlider;
import net.sf.openrocket.gui.DescriptionArea;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.UnitSelector;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.unit.UnitGroup;

public class TransitionConfig extends RocketComponentConfig {

	private JComboBox typeBox;
	//private JLabel description;
	
	private JLabel shapeLabel;
	private JSpinner shapeSpinner;
	private BasicSlider shapeSlider;
	private DescriptionArea description;
	

	// Prepended to the description from Transition.DESCRIPTIONS
	private static final String PREDESC = "<html><p style=\"font-size: x-small\">";
	
	
	public TransitionConfig(RocketComponent c) {
		super(c);
		
		DoubleModel m;
		JSpinner spin;
		JCheckBox checkbox;

		JPanel panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]",""));
		


		////  Shape selection
		
		panel.add(new JLabel("Transition shape:"));

		Transition.Shape selected = ((Transition)component).getType();
		Transition.Shape[] typeList = Transition.Shape.values();
		
		typeBox = new JComboBox(typeList);
		typeBox.setEditable(false);
		typeBox.setSelectedItem(selected);
		typeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Transition.Shape s = (Transition.Shape)typeBox.getSelectedItem();
				((Transition)component).setType(s);
				description.setText(PREDESC + s.getTransitionDescription());
				updateEnabled();
			}
		});
		panel.add(typeBox,"span, split 2");


		checkbox = new JCheckBox(new BooleanModel(component,"Clipped"));
		checkbox.setText("Clipped");
		panel.add(checkbox,"wrap");
		
		
		////  Shape parameter
		shapeLabel = new JLabel("Shape parameter:");
		panel.add(shapeLabel);
		
		m = new DoubleModel(component,"ShapeParameter");
		
		shapeSpinner = new JSpinner(m.getSpinnerModel());
		shapeSpinner.setEditor(new SpinnerEditor(shapeSpinner));
		panel.add(shapeSpinner,"growx");
		
		DoubleModel min = new DoubleModel(component,"ShapeParameterMin");
		DoubleModel max = new DoubleModel(component,"ShapeParameterMax");
		shapeSlider = new BasicSlider(m.getSliderModel(min,max)); 
		panel.add(shapeSlider,"skip, w 100lp, wrap");
		
		updateEnabled();
		
		
		////  Length
		panel.add(new JLabel("Transition length:"));
		
		m = new DoubleModel(component,"Length",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.3)),"w 100lp, wrap");
		
		
		//// Transition diameter 1
		panel.add(new JLabel("Fore diameter:"));

		DoubleModel od  = new DoubleModel(component,"ForeRadius",2,UnitGroup.UNITS_LENGTH,0);
		// Diameter = 2*Radius

		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(od),"growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)),"w 100lp, wrap 0px");

		checkbox = new JCheckBox(od.getAutomaticAction());
		checkbox.setText("Automatic");
		panel.add(checkbox,"skip, span 2, wrap");
		
		
		//// Transition diameter 2
		panel.add(new JLabel("Aft diameter:"));

		od  = new DoubleModel(component,"AftRadius",2,UnitGroup.UNITS_LENGTH,0);
		// Diameter = 2*Radius

		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(od),"growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)),"w 100lp, wrap 0px");

		checkbox = new JCheckBox(od.getAutomaticAction());
		checkbox.setText("Automatic");
		panel.add(checkbox,"skip, span 2, wrap");
		
		
		////  Wall thickness
		panel.add(new JLabel("Wall thickness:"));
		
		m = new DoubleModel(component,"Thickness",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0,0.01)),"w 100lp, wrap 0px");
		
		
		checkbox = new JCheckBox(new BooleanModel(component,"Filled"));
		checkbox.setText("Filled");
		panel.add(checkbox,"skip, span 2, wrap");

		
		
		////  Description
		
		JPanel panel2 = new JPanel(new MigLayout("ins 0"));
		
		description = new DescriptionArea(5);
		description.setText(PREDESC + ((Transition)component).getType().
				getTransitionDescription());
		panel2.add(description, "wmin 250lp, spanx, growx, wrap para");
		

		//// Material
		
		
		materialPanel(panel2, Material.Type.BULK);
		panel.add(panel2, "cell 4 0, gapleft paragraph, aligny 0%, spany");
		

		tabbedPane.insertTab("General", null, panel, "General properties", 0);
		tabbedPane.insertTab("Shoulder", null, shoulderTab(), "Shoulder properties", 1);
		tabbedPane.setSelectedIndex(0);
	}
	
	
	
	
	
	private void updateEnabled() {
		boolean e = ((Transition)component).getType().usesParameter();
		shapeLabel.setEnabled(e);
		shapeSpinner.setEnabled(e);
		shapeSlider.setEnabled(e);
	}

}
