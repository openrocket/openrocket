package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class TransitionConfig extends RocketComponentConfig {
	
	private static final Translator trans = Application.getTranslator();
	private JComboBox typeBox;
	//private JLabel description;
	
	private JLabel shapeLabel;
	private JSpinner shapeSpinner;
	private BasicSlider shapeSlider;
	private DescriptionArea description;
	

	// Prepended to the description from Transition.DESCRIPTIONS
	private static final String PREDESC = "<html>";
	
	
	public TransitionConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		DoubleModel m;
		JSpinner spin;
		JCheckBox checkbox;
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));
		


		////  Shape selection
		//// Transition shape:
		panel.add(new JLabel(trans.get("TransitionCfg.lbl.Transitionshape")));
		
		Transition.Shape selected = ((Transition) component).getType();
		Transition.Shape[] typeList = Transition.Shape.values();
		
		typeBox = new JComboBox(typeList);
		typeBox.setEditable(false);
		typeBox.setSelectedItem(selected);
		typeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Transition.Shape s = (Transition.Shape) typeBox.getSelectedItem();
				((Transition) component).setType(s);
				description.setText(PREDESC + s.getTransitionDescription());
				updateEnabled();
			}
		});
		panel.add(typeBox, "span, split 2");
		
		//// Clipped
		checkbox = new JCheckBox(new BooleanModel(component, "Clipped"));
		//// Clipped
		checkbox.setText(trans.get("TransitionCfg.checkbox.Clipped"));
		panel.add(checkbox, "wrap");
		

		////  Shape parameter:
		shapeLabel = new JLabel(trans.get("TransitionCfg.lbl.Shapeparam"));
		panel.add(shapeLabel);
		
		m = new DoubleModel(component, "ShapeParameter");
		
		shapeSpinner = new JSpinner(m.getSpinnerModel());
		shapeSpinner.setEditor(new SpinnerEditor(shapeSpinner));
		panel.add(shapeSpinner, "growx");
		
		DoubleModel min = new DoubleModel(component, "ShapeParameterMin");
		DoubleModel max = new DoubleModel(component, "ShapeParameterMax");
		shapeSlider = new BasicSlider(m.getSliderModel(min, max));
		panel.add(shapeSlider, "skip, w 100lp, wrap");
		
		updateEnabled();
		

		////  Length
		//// Transition length:
		panel.add(new JLabel(trans.get("TransitionCfg.lbl.Transitionlength")));
		
		m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.05, 0.3)), "w 100lp, wrap");
		

		//// Transition diameter 1
		//// Fore diameter:
		panel.add(new JLabel(trans.get("TransitionCfg.lbl.Forediam")));
		
		DoubleModel od = new DoubleModel(component, "ForeRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		// Diameter = 2*Radius
		
		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(od), "growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap 0px");
		
		checkbox = new JCheckBox(od.getAutomaticAction());
		//// Automatic
		checkbox.setText(trans.get("TransitionCfg.checkbox.Automatic"));
		panel.add(checkbox, "skip, span 2, wrap");
		

		//// Transition diameter 2
		//// Aft diameter:
		panel.add(new JLabel(trans.get("TransitionCfg.lbl.Aftdiam")));
		
		od = new DoubleModel(component, "AftRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		// Diameter = 2*Radius
		
		spin = new JSpinner(od.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(od), "growx");
		panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap 0px");
		
		checkbox = new JCheckBox(od.getAutomaticAction());
		//// Automatic
		checkbox.setText(trans.get("TransitionCfg.checkbox.Automatic"));
		panel.add(checkbox, "skip, span 2, wrap");
		

		////  Wall thickness:
		panel.add(new JLabel(trans.get("TransitionCfg.lbl.Wallthickness")));
		
		m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(m), "growx");
		panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 0px");
		
		//// Filled
		checkbox = new JCheckBox(new BooleanModel(component, "Filled"));
		//// Filled
		checkbox.setText(trans.get("TransitionCfg.checkbox.Filled"));
		panel.add(checkbox, "skip, span 2, wrap");
		


		////  Description
		
		JPanel panel2 = new JPanel(new MigLayout("ins 0"));
		
		description = new DescriptionArea(5);
		description.setText(PREDESC + ((Transition) component).getType().
				getTransitionDescription());
		panel2.add(description, "wmin 250lp, spanx, growx, wrap para");
		

		//// Material
		

		panel2.add(materialPanel(Material.Type.BULK), "span, wrap");
		panel.add(panel2, "cell 4 0, gapleft paragraph, aligny 0%, spany");
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("TransitionCfg.tab.General"), null, panel,
				trans.get("TransitionCfg.tab.Generalproperties"), 0);
		//// Shoulder and Shoulder properties
		tabbedPane.insertTab(trans.get("TransitionCfg.tab.Shoulder"), null, shoulderTab(),
				trans.get("TransitionCfg.tab.Shoulderproperties"), 1);
		tabbedPane.setSelectedIndex(0);
	}
	
	

	private void updateEnabled() {
		boolean e = ((Transition) component).getType().usesParameter();
		shapeLabel.setEnabled(e);
		shapeSpinner.setEnabled(e);
		shapeSlider.setEnabled(e);
	}
	
}
