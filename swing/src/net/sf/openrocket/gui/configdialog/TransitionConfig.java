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
	private static final long serialVersionUID = -1851275950604625741L;
	
	private static final Translator trans = Application.getTranslator();
	private JComboBox<Transition.Shape> typeBox;
	//private JLabel description;
	
	private JLabel shapeLabel;
	private JSpinner shapeSpinner;
	private BasicSlider shapeSlider;
	private DescriptionArea description;
	

	// Prepended to the description from Transition.DESCRIPTIONS
	private static final String PREDESC = "<html>";
	
	
	public TransitionConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		final JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

		////  Shape selection
		//// Transition shape:
		panel.add(new JLabel(trans.get("TransitionCfg.lbl.Transitionshape")));
		
		Transition.Shape selected = ((Transition) component).getType();
		Transition.Shape[] typeList = Transition.Shape.values();
		
		typeBox = new JComboBox<Transition.Shape>(typeList);
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

		{//// Clipped
			final JCheckBox checkbox = new JCheckBox(new BooleanModel(component, "Clipped"));
			checkbox.setText(trans.get("TransitionCfg.checkbox.Clipped"));
			panel.add(checkbox, "wrap");

			////  Shape parameter:
			this.shapeLabel = new JLabel(trans.get("TransitionCfg.lbl.Shapeparam"));
			panel.add(shapeLabel);
		}

		{
			final DoubleModel shapeModel = new DoubleModel(component, "ShapeParameter");

			this.shapeSpinner = new JSpinner(shapeModel.getSpinnerModel());
			shapeSpinner.setEditor(new SpinnerEditor(shapeSpinner));
			panel.add(shapeSpinner, "growx");

			DoubleModel min = new DoubleModel(component, "ShapeParameterMin");
			DoubleModel max = new DoubleModel(component, "ShapeParameterMax");
			this.shapeSlider = new BasicSlider(shapeModel.getSliderModel(min, max));
			panel.add(shapeSlider, "skip, w 100lp, wrap");

			updateEnabled();
		}

		{/// Transition length:

			panel.add(new JLabel(trans.get("TransitionCfg.lbl.Transitionlength")));

			final DoubleModel lengthModel = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);

			final JSpinner lengthSpinner = new JSpinner(lengthModel.getSpinnerModel());
			lengthSpinner.setEditor(new SpinnerEditor(lengthSpinner));
			panel.add(lengthSpinner, "growx");

			panel.add(new UnitSelector(lengthModel), "growx");
			panel.add(new BasicSlider(lengthModel.getSliderModel(0, 0.05, 0.3)), "w 100lp, wrap");
		}

		{ /// Fore diameter:
			panel.add(new JLabel(trans.get("TransitionCfg.lbl.Forediam")));

	         // Diameter = 2*Radius
			final DoubleModel foreRadiusModel = new DoubleModel(component, "ForeRadius", 2, UnitGroup.UNITS_LENGTH, 0);

			final JSpinner foreRadiusSpinner = new JSpinner(foreRadiusModel.getSpinnerModel());
			foreRadiusSpinner.setEditor(new SpinnerEditor(foreRadiusSpinner));
			panel.add(foreRadiusSpinner, "growx");

			panel.add(new UnitSelector(foreRadiusModel), "growx");
			panel.add(new BasicSlider(foreRadiusModel.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap 0px");

			final JCheckBox checkbox = new JCheckBox(foreRadiusModel.getAutomaticAction());
			//// Automatic
			checkbox.setText(trans.get("TransitionCfg.checkbox.Automatic"));
			panel.add(checkbox, "skip, span 2, wrap");
		}

		{	//// Aft diameter:
			panel.add(new JLabel(trans.get("TransitionCfg.lbl.Aftdiam")));

            // Diameter = 2*Radius
			final DoubleModel aftRadiusModel = new DoubleModel(component, "AftRadius", 2, UnitGroup.UNITS_LENGTH, 0);

			final JSpinner aftRadiusSpinner = new JSpinner(aftRadiusModel .getSpinnerModel());
			aftRadiusSpinner.setEditor(new SpinnerEditor(aftRadiusSpinner));
			panel.add(aftRadiusSpinner, "growx");

			panel.add(new UnitSelector(aftRadiusModel), "growx");
			panel.add(new BasicSlider(aftRadiusModel.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap 0px");

			final JCheckBox aftRadiusCheckbox = new JCheckBox(aftRadiusModel.getAutomaticAction());
			//// Automatic
			aftRadiusCheckbox.setText(trans.get("TransitionCfg.checkbox.Automatic"));
			panel.add(aftRadiusCheckbox, "skip, span 2, wrap");
		}

		{ ///  Wall thickness:
			panel.add(new JLabel(trans.get("TransitionCfg.lbl.Wallthickness")));

			final DoubleModel thicknessModel = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);

			final JSpinner thicknessSpinner = new JSpinner(thicknessModel.getSpinnerModel());
			thicknessSpinner.setEditor(new SpinnerEditor(thicknessSpinner));
			panel.add(thicknessSpinner, "growx");

			panel.add(new UnitSelector(thicknessModel), "growx");
			panel.add(new BasicSlider(thicknessModel.getSliderModel(0, 0.01)), "w 100lp, wrap 0px");

			//// Filled
			final JCheckBox thicknessCheckbox = new JCheckBox(new BooleanModel(component, "Filled"));
			//// Filled
			thicknessCheckbox.setText(trans.get("TransitionCfg.checkbox.Filled"));
			panel.add(thicknessCheckbox, "skip, span 2, wrap");
		}

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
