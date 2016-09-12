package net.sf.openrocket.gui.configdialog;


import javax.swing.BoundedRangeModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

@SuppressWarnings("serial")
public class BodyTubeConfig extends RocketComponentConfig {

	
	private static final Translator trans = Application.getTranslator();

	public BodyTubeConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);

		JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));

		////  Body tube length
		panel.add(new JLabel(trans.get("BodyTubecfg.lbl.Bodytubelength")));

		final DoubleModel maxLengthModel = new DoubleModel(2.0);
		final DoubleModel lengthModel = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		final JSpinner lengthSpinner = new JSpinner(lengthModel.getSpinnerModel());
		lengthSpinner.setEditor(new SpinnerEditor(lengthSpinner));
		panel.add(lengthSpinner, "growx");

		final UnitSelector lengthUnits = new UnitSelector(lengthModel);
		panel.add( lengthUnits, "growx");
		
		final BoundedRangeModel lengthSliderModel = lengthModel.getSliderModel(0, 0.5, maxLengthModel);
		final BasicSlider lengthSlider = new BasicSlider( lengthSliderModel );
		
		panel.add( lengthSlider, "w 100lp, wrap");
		
		
		//// Body tube diameter
		panel.add(new JLabel(trans.get("BodyTubecfg.lbl.Outerdiameter")));
		
		final DoubleModel outerRadiusModel = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		final JSpinner orSpinner = new JSpinner(outerRadiusModel.getSpinnerModel());
		orSpinner.setEditor(new SpinnerEditor(orSpinner));
		panel.add(orSpinner, "growx");

		final UnitSelector orUnits = new UnitSelector(outerRadiusModel);
		panel.add( orUnits, "growx");
		final BasicSlider orSlider = new BasicSlider(outerRadiusModel.getSliderModel(0, 0.04, 0.2));
		panel.add( orSlider, "w 100lp, wrap 0px");

		final JCheckBox check = new JCheckBox(outerRadiusModel.getAutomaticAction());
		//// Automatic ?
		check.setText(trans.get("BodyTubecfg.checkbox.Automatic"));
		panel.add( check, "skip, span 2, wrap");
	

		////  Inner diameter (Diameter = 2*Radius
        panel.add(new JLabel(trans.get("BodyTubecfg.lbl.Innerdiameter")));
		
		final DoubleModel innerRadiusModel = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
		final JSpinner irSpinner = new JSpinner(innerRadiusModel.getSpinnerModel());
		irSpinner.setEditor(new SpinnerEditor(irSpinner));
		panel.add(irSpinner, "growx");

		final UnitSelector irUnits = new UnitSelector(innerRadiusModel);
		panel.add( irUnits, "growx");
		final BasicSlider irSlider = new BasicSlider(innerRadiusModel.getSliderModel(new DoubleModel(0), outerRadiusModel));
		panel.add( irSlider, "w 100lp, wrap");
		
		
		////  Wall thickness
		panel.add(new JLabel(trans.get("BodyTubecfg.lbl.Wallthickness")));
		
		final DoubleModel thicknessModel = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
		innerRadiusModel.addChangeListener( thicknessModel);
		thicknessModel.addChangeListener( innerRadiusModel);
		
		final JSpinner thicknessSpinner = new JSpinner(thicknessModel.getSpinnerModel());
		thicknessSpinner.setEditor(new SpinnerEditor(thicknessSpinner));
		panel.add( thicknessSpinner, "growx");

		final UnitSelector thicknessUnits = new UnitSelector(thicknessModel);
		panel.add( thicknessUnits );
		final BasicSlider thicknessSlider = new BasicSlider(thicknessModel.getSliderModel(0, 0.01));
		panel.add( thicknessSlider, "w 100lp, wrap 0px");
		
		
		//// Filled
		final BooleanModel filledModel = new BooleanModel(component, "Filled");
		final JCheckBox filledCheckBox = new JCheckBox( filledModel );
		filledCheckBox.setText(trans.get("BodyTubecfg.checkbox.Filled"));
		panel.add( filledCheckBox, "skip, span 2, wrap");
		// add all the inner radius, thickness components to this enabler
		filledModel.addEnableComponent( irSpinner, false);
		filledModel.addEnableComponent( irUnits, false);
		filledModel.addEnableComponent( irSlider, false );
		filledModel.addEnableComponent( thicknessSpinner, false );
		filledModel.addEnableComponent( thicknessUnits, false );
		filledModel.addEnableComponent( thicknessSlider, false );

		//// Material
		panel.add(materialPanel(Material.Type.BULK),
				"cell 4 0, gapleft paragraph, aligny 0%, spany");

		//// General and General properties
		tabbedPane.insertTab(trans.get("BodyTubecfg.tab.General"), null, panel,
				trans.get("BodyTubecfg.tab.Generalproperties"), 0);

		tabbedPane.setSelectedIndex(0);

		MotorConfig motorConfig = new MotorConfig((MotorMount)c);

		tabbedPane.insertTab(trans.get("BodyTubecfg.tab.Motor"), null, motorConfig,
				trans.get("BodyTubecfg.tab.Motormountconf"), 1);


	}

	@Override
	public void updateFields() {
		super.updateFields();
	}

}
