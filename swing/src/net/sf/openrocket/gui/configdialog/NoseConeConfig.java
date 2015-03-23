package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class NoseConeConfig extends RocketComponentConfig {
	
	private JComboBox typeBox;
	
	private DescriptionArea description;
	
	private JLabel shapeLabel;
	private JSpinner shapeSpinner;
	private JSlider shapeSlider;
	private static final Translator trans = Application.getTranslator();
	
	// Prepended to the description from NoseCone.DESCRIPTIONS
	private static final String PREDESC = "<html>";
	
	public NoseConeConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		DoubleModel m;
		JPanel panel = new JPanel(new MigLayout("", "[][65lp::][30lp::]"));
		
		////  Shape selection
		//// Nose cone shape:
		panel.add(new JLabel(trans.get("NoseConeCfg.lbl.Noseconeshape")));
		
		Transition.Shape selected = ((NoseCone) component).getType();
		Transition.Shape[] typeList = Transition.Shape.values();
		
		typeBox = new JComboBox(typeList);
		typeBox.setEditable(false);
		typeBox.setSelectedItem(selected);
		typeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Transition.Shape s = (Transition.Shape) typeBox.getSelectedItem();
				((NoseCone) component).setType(s);
				description.setText(PREDESC + s.getNoseConeDescription());
				updateEnabled();
			}
		});
		panel.add(typeBox, "span, wrap rel");
		



		////  Shape parameter
		////  Shape parameter:
		shapeLabel = new JLabel(trans.get("NoseConeCfg.lbl.Shapeparam"));
		panel.add(shapeLabel);
		
		m = new DoubleModel(component, "ShapeParameter");
		
		shapeSpinner = new JSpinner(m.getSpinnerModel());
		shapeSpinner.setEditor(new SpinnerEditor(shapeSpinner));
		panel.add(shapeSpinner, "growx");
		
		DoubleModel min = new DoubleModel(component, "ShapeParameterMin");
		DoubleModel max = new DoubleModel(component, "ShapeParameterMax");
		shapeSlider = new BasicSlider(m.getSliderModel(min, max));
		panel.add(shapeSlider, "skip, w 100lp, wrap para");
		
		updateEnabled();
		

		////  Length
		//// Nose cone length:
		panel.add(new JLabel(trans.get("NoseConeCfg.lbl.Noseconelength")));
		
		m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		JSpinner spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.7)), "w 100lp, wrap");
		
		////  Diameter
		//// Base diameter:
		panel.add(new JLabel(trans.get("NoseConeCfg.lbl.Basediam")));
		
		m = new DoubleModel(component, "AftRadius", 2.0, UnitGroup.UNITS_LENGTH, 0); // Diameter = 2*Radius
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap 0px");
		
		JCheckBox check = new JCheckBox(m.getAutomaticAction());
		//// Automatic
		check.setText(trans.get("NoseConeCfg.checkbox.Automatic"));
		panel.add(check, "skip, span 2, wrap");
		

		////  Wall thickness:
		panel.add(new JLabel(trans.get("NoseConeCfg.lbl.Wallthickness")));
		
		m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 0px");
		

		check = new JCheckBox(new BooleanModel(component, "Filled"));
		//// Filled
		check.setText(trans.get("NoseConeCfg.checkbox.Filled"));
		panel.add(check, "skip, span 2, wrap");
		

		panel.add(new JLabel(""), "growy");
		


		////  Description
		
		JPanel panel2 = new JPanel(new MigLayout("ins 0"));
		
		description = new DescriptionArea(5);
		description.setText(PREDESC + ((NoseCone) component).getType().getNoseConeDescription());
		panel2.add(description, "wmin 250lp, spanx, growx, wrap para");
		

		//// Material
		

		panel2.add(materialPanel( Material.Type.BULK), "span, wrap");
		panel.add(panel2, "cell 4 0, gapleft paragraph, aligny 0%, spany");
		

		//// General and General properties
		tabbedPane.insertTab(trans.get("NoseConeCfg.tab.General"), null, panel,
				trans.get("NoseConeCfg.tab.ttip.General"), 0);
		//// Shoulder and Shoulder properties
		tabbedPane.insertTab(trans.get("NoseConeCfg.tab.Shoulder"), null, shoulderTab(),
				trans.get("NoseConeCfg.tab.ttip.Shoulder"), 1);
		tabbedPane.setSelectedIndex(0);
	}
	
	
	private void updateEnabled() {
		boolean e = ((NoseCone) component).getType().usesParameter();
		shapeLabel.setEnabled(e);
		shapeSpinner.setEnabled(e);
		shapeSlider.setEnabled(e);
	}
	

}
