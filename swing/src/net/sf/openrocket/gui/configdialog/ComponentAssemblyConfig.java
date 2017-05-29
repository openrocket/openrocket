package net.sf.openrocket.gui.configdialog;

import javax.swing.ComboBoxModel;
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
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;


public class ComponentAssemblyConfig extends RocketComponentConfig {
	private static final long serialVersionUID = -5153592258788614257L;
	private static final Translator trans = Application.getTranslator();
	
	public ComponentAssemblyConfig(OpenRocketDocument document, RocketComponent component) {
		super(document, component);
	
		// only stages which are actually off-centerline will get the dialog here:
		if( component.getClass().isAssignableFrom( ParallelStage.class) || component.getClass().isAssignableFrom( PodSet.class)){
			tabbedPane.insertTab( trans.get("RocketCompCfg.tab.Assembly"), null, parallelTab( (ComponentAssembly)component ), trans.get("RocketCompCfg.tab.AssemblyComment"), 0);
			tabbedPane.setSelectedIndex(0);
		}
	}

	
	private JPanel parallelTab( final ComponentAssembly boosters ){
		JPanel motherPanel = new JPanel( new MigLayout("fill"));
		
		// auto radial distance		
		BooleanModel autoRadOffsModel = new BooleanModel( boosters, "AutoRadialOffset");
		JCheckBox autoRadCheckBox = new JCheckBox( autoRadOffsModel );
		autoRadCheckBox.setText( trans.get("StageConfig.parallel.autoradius"));
		motherPanel.add( autoRadCheckBox, "align left, wrap");
		// set radial distance
		JLabel radiusLabel = new JLabel(trans.get("StageConfig.parallel.radius"));
		motherPanel.add( radiusLabel , "align left");
		autoRadOffsModel.addEnableComponent(radiusLabel, false);
		DoubleModel radiusModel = new DoubleModel( boosters, "RadialOffset", UnitGroup.UNITS_LENGTH, 0);

		JSpinner radiusSpinner = new JSpinner( radiusModel.getSpinnerModel());
		radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner ));
		motherPanel.add(radiusSpinner , "growx 1, align right");
		autoRadOffsModel.addEnableComponent(radiusSpinner, false);
		UnitSelector radiusUnitSelector = new UnitSelector(radiusModel);
		motherPanel.add(radiusUnitSelector, "growx 1, wrap");
		autoRadOffsModel.addEnableComponent(radiusUnitSelector, false);
		
		// set location angle around the primary stage
		JLabel angleLabel = new JLabel(trans.get("StageConfig.parallel.angle"));
		motherPanel.add( angleLabel, "align left");
		DoubleModel angleModel = new DoubleModel( boosters, "AngularOffset", 1.0, UnitGroup.UNITS_ANGLE, 0.0, Math.PI*2);

		JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
		angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
		motherPanel.add(angleSpinner, "growx 1");
		UnitSelector angleUnitSelector = new UnitSelector(angleModel);
		motherPanel.add( angleUnitSelector, "growx 1, wrap");
		
		// set multiplicity
		JLabel countLabel = new JLabel(trans.get("StageConfig.parallel.count"));
		motherPanel.add( countLabel, "align left");
		
		IntegerModel countModel = new IntegerModel( boosters, "InstanceCount", 2);
		JSpinner countSpinner = new JSpinner(countModel.getSpinnerModel());
		countSpinner.setEditor(new SpinnerEditor(countSpinner));
		motherPanel.add(countSpinner, "growx 1, wrap");
		
		// setPositions relative to parent component
		JLabel positionLabel = new JLabel(trans.get("LaunchLugCfg.lbl.Posrelativeto"));
		motherPanel.add( positionLabel);
		
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
		DoubleModel axialOffsetModel = new DoubleModel( boosters, "AxialOffset", UnitGroup.UNITS_LENGTH);

		JSpinner axPosSpin= new JSpinner( axialOffsetModel.getSpinnerModel());
		axPosSpin.setEditor(new SpinnerEditor(axPosSpin));
		motherPanel.add(axPosSpin, "growx");
		UnitSelector axialOffsetUnitSelector = new UnitSelector(axialOffsetModel);
		motherPanel.add(axialOffsetUnitSelector, "growx 1, wrap");
		
		// For DEBUG purposes
		//System.err.println(assembly.getRocket().toDebugTree());
		
		return motherPanel;
	}
}
