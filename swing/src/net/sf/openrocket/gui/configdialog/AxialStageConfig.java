package net.sf.openrocket.gui.configdialog;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

public class AxialStageConfig extends RocketComponentConfig {
	private static final long serialVersionUID = -944969957186522471L;
	private static final Translator trans = Application.getTranslator();
	
	public AxialStageConfig(OpenRocketDocument document, RocketComponent component) {
		super(document, component);
		
		// Stage separation config (for non-first stage)
		if (component.getStageNumber() > 0) {
			JPanel tab = separationTab((AxialStage) component);
			tabbedPane.insertTab(trans.get("StageConfig.tab.Separation"), null, tab,
					trans.get("StageConfig.tab.Separation.ttip"), 1);
		}
	 	
		System.err.println(" building Stage Config Dialogue for: "+component.getName()+" type: "+component.getComponentName());
	 	// only stages which are actually off-centerline will get the dialog here:
		if( ! component.isCenterline()){
			tabbedPane.insertTab( trans.get("RocketCompCfg.tab.Parallel"), null, parallelTab( (AxialStage) component ), trans.get("RocketCompCfg.tab.ParallelComment"), 2);
		}
	}
	
	private JPanel parallelTab( final AxialStage stage ){
		JPanel motherPanel = new JPanel( new MigLayout("fill"));
	
		// set radial distance
		JLabel radiusLabel = new JLabel(trans.get("StageConfig.parallel.radius"));  
		motherPanel.add( radiusLabel , "align left");
		DoubleModel radiusModel = new DoubleModel( stage, "RadialOffset", UnitGroup.UNITS_LENGTH, 0);
		//radiusModel.setCurrentUnit( UnitGroup.UNITS_LENGTH.getUnit("cm"));
		JSpinner radiusSpinner = new JSpinner( radiusModel.getSpinnerModel());
		radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner ));
		motherPanel.add(radiusSpinner , "growx 1, align right");
		UnitSelector radiusUnitSelector = new UnitSelector(radiusModel);
		motherPanel.add(radiusUnitSelector, "growx 1, wrap");
		
		// set location angle around the primary stage
		JLabel angleLabel = new JLabel(trans.get("StageConfig.parallel.angle"));
		motherPanel.add( angleLabel, "align left");
		DoubleModel angleModel = new DoubleModel( stage, "AngularOffset", 1.0, UnitGroup.UNITS_ANGLE, 0.0, Math.PI*2);
		angleModel.setCurrentUnit( UnitGroup.UNITS_ANGLE.getUnit("rad"));
		JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
		angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
		motherPanel.add(angleSpinner, "growx 1");
		UnitSelector angleUnitSelector = new UnitSelector(angleModel);
		motherPanel.add( angleUnitSelector, "growx 1, wrap");
		
		// set multiplicity
		JLabel countLabel = new JLabel(trans.get("StageConfig.parallel.count"));
		motherPanel.add( countLabel, "align left");
		
		IntegerModel countModel = new IntegerModel( stage, "InstanceCount", 2);
		JSpinner countSpinner = new JSpinner(countModel.getSpinnerModel());
		countSpinner.setEditor(new SpinnerEditor(countSpinner));
		motherPanel.add(countSpinner, "growx 1, wrap");
		
		// setPositions relative to parent component
		JLabel positionLabel = new JLabel(trans.get("LaunchLugCfg.lbl.Posrelativeto"));
		motherPanel.add( positionLabel);
		
		//	EnumModel(ChangeSource source, String valueName, Enum<T>[] values) {
		ComboBoxModel<RocketComponent.Position> relativePositionMethodModel = new EnumModel<RocketComponent.Position>(component, "RelativePositionMethod",
				new RocketComponent.Position[] {
						RocketComponent.Position.TOP,
						RocketComponent.Position.MIDDLE,
						RocketComponent.Position.BOTTOM,
						RocketComponent.Position.ABSOLUTE
				});
		JComboBox<?> positionMethodCombo = new JComboBox<RocketComponent.Position>( relativePositionMethodModel );
		motherPanel.add(positionMethodCombo, "spanx 2, growx, wrap");
		
		// relative offset labels
		JLabel positionPlusLabel = new JLabel(trans.get("StageConfig.parallel.offset"));
		motherPanel.add( positionPlusLabel );
		DoubleModel axialOffsetModel = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
		axialOffsetModel.setCurrentUnit(UnitGroup.UNITS_LENGTH.getUnit("cm"));
		JSpinner axPosSpin= new JSpinner( axialOffsetModel.getSpinnerModel());
		axPosSpin.setEditor(new SpinnerEditor(axPosSpin));
		motherPanel.add(axPosSpin, "growx");
		UnitSelector axialOffsetUnitSelector = new UnitSelector(axialOffsetModel);
		motherPanel.add(axialOffsetUnitSelector, "growx 1, wrap");
		
		// For DEBUG purposes
		//System.err.println(stage.getRocket().toDebugTree());
		
		return motherPanel;
	}
	
	private JPanel separationTab(AxialStage stage) {
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Select separation event
		panel.add(new StyledLabel(trans.get("StageConfig.separation.lbl.title") + " " + CommonStrings.dagger, Style.BOLD), "spanx, wrap rel");
		
		StageSeparationConfiguration config = stage.getStageSeparationConfiguration().getDefault();
		JComboBox combo = new JComboBox(new EnumModel<StageSeparationConfiguration.SeparationEvent>(config, "SeparationEvent"));
		panel.add(combo, "");
		
		// ... and delay
		panel.add(new JLabel(trans.get("StageConfig.separation.lbl.plus")), "");
		
		DoubleModel dm = new DoubleModel(config, "SeparationDelay", 0);
		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "width 45");
		
		//// seconds
		panel.add(new JLabel(trans.get("StageConfig.separation.lbl.seconds")), "wrap unrel");
		
		panel.add(new StyledLabel(CommonStrings.override_description, -1), "spanx, wrap para");
		
		return panel;
	}

	
}
