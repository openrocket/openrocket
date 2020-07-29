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
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;


@SuppressWarnings("serial")
public class ComponentAssemblyConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public ComponentAssemblyConfig(OpenRocketDocument document, RocketComponent component) {
		super(document, component);
	
		// only stages which are actually off-centerline will get the dialog here:
		if( ParallelStage.class.isAssignableFrom( component.getClass()) || PodSet.class.isAssignableFrom( component.getClass())){
			tabbedPane.insertTab( trans.get("RocketCompCfg.tab.Assembly"), null, parallelTab( (ComponentAssembly)component ), trans.get("RocketCompCfg.tab.AssemblyComment"), 0);
			tabbedPane.setSelectedIndex(0);
		}
	}

	
	private JPanel parallelTab( final ComponentAssembly boosters ){
		JPanel motherPanel = new JPanel( new MigLayout("fill"));
		
		// radial distance method
		JLabel radiusMethodLabel = new JLabel(trans.get("RocketComponent.Position.Method.Radius.Label"));
        motherPanel.add( radiusMethodLabel, "align left");
		final EnumModel<RadiusMethod> radiusMethodModel = new EnumModel<RadiusMethod>( boosters, "RadiusMethod", RadiusMethod.choices());
		final JComboBox<RadiusMethod> radiusMethodCombo = new JComboBox<RadiusMethod>( radiusMethodModel );
		motherPanel.add( radiusMethodCombo, "align left, wrap");
		
		// set radial distance
		JLabel radiusLabel = new JLabel(trans.get("StageConfig.parallel.radius"));
		motherPanel.add( radiusLabel , "align left");
		//radiusMethodModel.addEnableComponent(radiusLabel, false);
		DoubleModel radiusModel = new DoubleModel( boosters, "RadiusOffset", UnitGroup.UNITS_LENGTH, 0);

		JSpinner radiusSpinner = new JSpinner( radiusModel.getSpinnerModel());
		radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner ));
		motherPanel.add(radiusSpinner , "growx 1, align right");
//		autoRadOffsModel.addEnableComponent(radiusSpinner, false);
		UnitSelector radiusUnitSelector = new UnitSelector(radiusModel);
		motherPanel.add(radiusUnitSelector, "growx 1, wrap");
//		autoRadOffsModel.addEnableComponent(radiusUnitSelector, false);
		
//		// set location angle around the primary stage
//		JLabel angleMethodLabel = new JLabel(trans.get("RocketComponent.Position.Method.Angle.Label"));
//		motherPanel.add( angleMethodLabel, "align left");
//		EnumModel<AngleMethod> angleMethodModel = new EnumModel<AngleMethod>( boosters, "AngleMethod", AngleMethod.choices() );
//		final JComboBox<AngleMethod> angleMethodCombo = new JComboBox<AngleMethod>( angleMethodModel );
//        motherPanel.add( angleMethodCombo, "align left, wrap");
        
		JLabel angleLabel = new JLabel(trans.get("StageConfig.parallel.angle"));
		motherPanel.add( angleLabel, "align left");
		DoubleModel angleModel = new DoubleModel( boosters, "AngleOffset", 1.0, UnitGroup.UNITS_ANGLE, 0.0, Math.PI*2);

		JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
		angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
		motherPanel.add(angleSpinner, "growx 1");
		UnitSelector angleUnitSelector = new UnitSelector(angleModel);
		motherPanel.add( angleUnitSelector, "growx 1, wrap");
		
		// set multiplicity
		JLabel countLabel = new JLabel(trans.get("StageConfig.parallel.count"));
		motherPanel.add( countLabel, "align left");
		
		IntegerModel countModel = new IntegerModel( boosters, "InstanceCount", 1);
		JSpinner countSpinner = new JSpinner(countModel.getSpinnerModel());
		countSpinner.setEditor(new SpinnerEditor(countSpinner));
		motherPanel.add(countSpinner, "growx 1, wrap");
		
		// setPositions relative to parent component
		JLabel positionLabel = new JLabel(trans.get("LaunchLugCfg.lbl.Posrelativeto"));
		motherPanel.add( positionLabel);
		
		ComboBoxModel<AxialMethod> axialPositionMethodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
		JComboBox<?> positionMethodCombo = new JComboBox<AxialMethod>( axialPositionMethodModel );
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
