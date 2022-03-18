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
import net.sf.openrocket.gui.adaptors.TransitionShapeModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
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
	private final JCheckBox checkAutoAftRadius;
	private final JCheckBox checkAutoForeRadius;
	private DescriptionArea description;
	

	// Prepended to the description from Transition.DESCRIPTIONS
	private static final String PREDESC = "<html>";
	
	
	public TransitionConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		final JPanel panel = new JPanel(new MigLayout("gap rel unrel, fillx", "[][65lp::][30lp::]", ""));

		////  Shape selection
		//// Transition shape:
		panel.add(new JLabel(trans.get("TransitionCfg.lbl.Transitionshape")));
		
		typeBox = new JComboBox<>(new TransitionShapeModel(c));
		typeBox.setEditable(false);
		typeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Transition.Shape s = (Transition.Shape) typeBox.getSelectedItem();
				if (s != null) {
					description.setText(PREDESC + s.getTransitionDescription());
				}
				updateEnabled();
			}
		});
		panel.add(typeBox, "span 3, split 2");

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

			checkAutoForeRadius = new JCheckBox(foreRadiusModel.getAutomaticAction());
			//// Automatic
			checkAutoForeRadius.setText(trans.get("TransitionCfg.checkbox.Automatic"));
			panel.add(checkAutoForeRadius, "skip, span 2, wrap");
			updateCheckboxAutoForeRadius();
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

			checkAutoAftRadius = new JCheckBox(aftRadiusModel.getAutomaticAction());
			//// Automatic
			checkAutoAftRadius.setText(trans.get("TransitionCfg.checkbox.Automatic"));
			panel.add(checkAutoAftRadius, "skip, span 2, wrap");
			updateCheckboxAutoAftRadius();
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

	/**
	 * Sets the checkAutoAftRadius checkbox's enabled state and tooltip text, based on the state of its next component.
	 * If there is no next symmetric component or if that component already has its auto checkbox checked, the
	 * checkAutoAftRadius checkbox is disabled.
	 */
	private void updateCheckboxAutoAftRadius() {
		if (component == null || checkAutoAftRadius == null) return;

		// Disable check button if there is no component to get the diameter from
		SymmetricComponent nextComp = ((Transition) component).getNextSymmetricComponent();
		if (nextComp == null) {
			checkAutoAftRadius.setEnabled(false);
			((Transition) component).setAftRadiusAutomatic(false);
			checkAutoAftRadius.setToolTipText(trans.get("TransitionCfg.checkbox.ttip.Automatic_noReferenceComponent"));
			return;
		}
		if (!nextComp.usesPreviousCompAutomatic()) {
			checkAutoAftRadius.setEnabled(true);
			checkAutoAftRadius.setToolTipText(trans.get("TransitionCfg.checkbox.ttip.Automatic"));
		} else {
			checkAutoAftRadius.setEnabled(false);
			((Transition) component).setAftRadiusAutomatic(false);
			checkAutoAftRadius.setToolTipText(trans.get("TransitionCfg.checkbox.ttip.Automatic_alreadyAuto"));
		}
	}

	/**
	 * Sets the checkAutoForeRadius checkbox's enabled state and tooltip text, based on the state of its next component.
	 * If there is no next symmetric component or if that component already has its auto checkbox checked, the
	 * checkAutoForeRadius checkbox is disabled.
	 */
	private void updateCheckboxAutoForeRadius() {
		if (component == null || checkAutoForeRadius == null) return;

		// Disable check button if there is no component to get the diameter from
		SymmetricComponent prevComp = ((Transition) component).getPreviousSymmetricComponent();
		if (prevComp == null) {
			checkAutoForeRadius.setEnabled(false);
			((Transition) component).setForeRadiusAutomatic(false);
			checkAutoForeRadius.setToolTipText(trans.get("TransitionCfg.checkbox.ttip.Automatic_noReferenceComponent"));
			return;
		}
		if (!prevComp.usesNextCompAutomatic()) {
			checkAutoForeRadius.setEnabled(true);
			checkAutoForeRadius.setToolTipText(trans.get("TransitionCfg.checkbox.ttip.Automatic"));
		} else {
			checkAutoForeRadius.setEnabled(false);
			((Transition) component).setForeRadiusAutomatic(false);
			checkAutoForeRadius.setToolTipText(trans.get("TransitionCfg.checkbox.ttip.Automatic_alreadyAuto"));
		}
	}
	
}
