package net.sf.openrocket.gui.configdialog;

import java.awt.Component;
import java.awt.Container;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.OutsideComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ChangeSource;

public class StageConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();

	
	public StageConfig(OpenRocketDocument document, RocketComponent component) {
		super(document, component);
		
		// Stage separation config (for non-first stage)
		if (component.getStageNumber() > 0) {
			JPanel tab = separationTab((Stage) component);
			tabbedPane.insertTab(trans.get("tab.Separation"), null, tab,
					trans.get("tab.Separation.ttip"), 1);
		}
	 	
	 	// all stage instances should qualify here...
		if( component instanceof OutsideComponent ){
			tabbedPane.insertTab( trans.get("RocketCompCfg.tab.Parallel"), null, parallelTab( (Stage) component ), trans.get("RocketCompCfg.tab.ParallelComment"), 2);
		}
	}
	
	private JPanel parallelTab( final Stage stage ){
		// enable parallel staging
		JPanel motherPanel = new JPanel( new MigLayout("fill"));
		BooleanModel parallelEnabledModel = new BooleanModel( component, "Outside");
		parallelEnabledModel.setValue( stage.getOutside());
		JCheckBox parallelEnabled = new JCheckBox( parallelEnabledModel);
		parallelEnabled.setText(trans.get("RocketCompCfg.outside.stage"));
		motherPanel.add(parallelEnabled, "wrap");

		motherPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "spanx 3, growx, wrap");

		// set radial distance
		JLabel radiusLabel = new JLabel(trans.get("RocketCompCfg.outside.radius"));  
		motherPanel.add( radiusLabel , "align left");
		parallelEnabledModel.addEnableComponent( radiusLabel, true);
		DoubleModel radiusModel = new DoubleModel( stage, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
		//radiusModel.setCurrentUnit( UnitGroup.UNITS_LENGTH.getUnit("cm"));
		JSpinner radiusSpinner = new JSpinner( radiusModel.getSpinnerModel());
		radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner ));
		motherPanel.add(radiusSpinner , "growx 1, align right");
		parallelEnabledModel.addEnableComponent( radiusSpinner, true);
		UnitSelector radiusUnitSelector = new UnitSelector(radiusModel);
		motherPanel.add(radiusUnitSelector, "growx 1, wrap");
		parallelEnabledModel.addEnableComponent( radiusUnitSelector , true);
		
		// set location angle around the primary stage
		JLabel angleLabel = new JLabel(trans.get("RocketCompCfg.outside.angle"));
		motherPanel.add( angleLabel, "align left");
		parallelEnabledModel.addEnableComponent( angleLabel, true);
		DoubleModel angleModel = new DoubleModel( stage, "AngularPosition", 1.0, UnitGroup.UNITS_ANGLE, 0.0, Math.PI*2);
		angleModel.setCurrentUnit( UnitGroup.UNITS_ANGLE.getUnit("rad"));
		JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
		angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
		motherPanel.add(angleSpinner, "growx 1");
		parallelEnabledModel.addEnableComponent( angleSpinner, true);
		UnitSelector angleUnitSelector = new UnitSelector(angleModel);
		motherPanel.add( angleUnitSelector, "growx 1, wrap");
		parallelEnabledModel.addEnableComponent( angleUnitSelector , true);

		// set multiplicity
		JLabel countLabel = new JLabel(trans.get("RocketCompCfg.outside.count"));
		motherPanel.add( countLabel, "align left");
		parallelEnabledModel.addEnableComponent( countLabel, true);
		
		IntegerModel countModel = new IntegerModel( stage, "Count", 1 );
		JSpinner countSpinner = new JSpinner(countModel.getSpinnerModel());
		countSpinner.setEditor(new SpinnerEditor(countSpinner));
		motherPanel.add(countSpinner, "growx 1, wrap");
		parallelEnabledModel.addEnableComponent( countSpinner, true);
		
		// setPositions relative to parent component
		JLabel positionLabel = new JLabel(trans.get("LaunchLugCfg.lbl.Posrelativeto"));
		motherPanel.add( positionLabel);
		parallelEnabledModel.addEnableComponent( positionLabel, true);
		
		//	EnumModel(ChangeSource source, String valueName, Enum<T>[] values) {
		ComboBoxModel<RocketComponent.Position> posRelModel = new EnumModel<RocketComponent.Position>(component, "RelativePositionMethod",
				new RocketComponent.Position[] {
						RocketComponent.Position.TOP,
						RocketComponent.Position.MIDDLE,
						RocketComponent.Position.BOTTOM,
						RocketComponent.Position.ABSOLUTE
				});
		JComboBox<?> positionMethodCombo = new JComboBox<RocketComponent.Position>( posRelModel );
		motherPanel.add(positionMethodCombo, "spanx 2, growx, wrap");
		parallelEnabledModel.addEnableComponent( positionMethodCombo, true);
		
		// setPositions relative to parent component
		JLabel relativeStageLabel = new JLabel(trans.get("RocketCompCfg.outside.componentname"));
		motherPanel.add( relativeStageLabel);
		parallelEnabledModel.addEnableComponent( relativeStageLabel, true);
		// may need to implement a new ComponentComboModel or something
		IntegerModel relToStageModel = new IntegerModel( stage, "RelativeToStage",0);
		List<RocketComponent> stageList = stage.getParent().getChildren(); 
		RocketComponent[] forCombo = new RocketComponent[stageList.size()];
		forCombo = stageList.toArray(forCombo);
		DefaultComboBoxModel<RocketComponent> relativeStageComboModel = new DefaultComboBoxModel<RocketComponent>( forCombo );
		ComboBoxModel<RocketComponent> relativeStageCombo = relativeStageComboModel;
		JComboBox<?> relToCombo = new JComboBox<RocketComponent>( relativeStageCombo );
		motherPanel.add( relToCombo , "growx, wrap");
		parallelEnabledModel.addEnableComponent( relToCombo, true );
		
		// plus
		JLabel positionPlusLabel = new JLabel(trans.get("LaunchLugCfg.lbl.plus"));
		motherPanel.add( positionPlusLabel );
		parallelEnabledModel.addEnableComponent( positionPlusLabel, true );
		
		DoubleModel axialPositionModel = new DoubleModel(component, "AxialPosition", UnitGroup.UNITS_LENGTH);
		JSpinner axPosSpin= new JSpinner( axialPositionModel.getSpinnerModel());
		axPosSpin.setEditor(new SpinnerEditor(axPosSpin));
		motherPanel.add(axPosSpin, "growx");
		parallelEnabledModel.addEnableComponent( axPosSpin, true );
		
		return motherPanel;
	}
	
	private JPanel separationTab(Stage stage) {
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Select separation event
		panel.add(new StyledLabel(trans.get("separation.lbl.title") + " " + CommonStrings.dagger, Style.BOLD), "spanx, wrap rel");
		
		StageSeparationConfiguration config = stage.getStageSeparationConfiguration().getDefault();
		JComboBox combo = new JComboBox(new EnumModel<StageSeparationConfiguration.SeparationEvent>(config, "SeparationEvent"));
		panel.add(combo, "");
		
		// ... and delay
		panel.add(new JLabel(trans.get("separation.lbl.plus")), "");
		
		DoubleModel dm = new DoubleModel(config, "SeparationDelay", 0);
		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "width 45");
		
		//// seconds
		panel.add(new JLabel(trans.get("separation.lbl.seconds")), "wrap unrel");
		
		panel.add(new StyledLabel(CommonStrings.override_description, -1), "spanx, wrap para");
		
		return panel;
	}

	
}
