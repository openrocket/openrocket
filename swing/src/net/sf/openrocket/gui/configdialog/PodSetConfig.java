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
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

@SuppressWarnings("serial")
public class PodSetConfig extends RocketComponentConfig {

	private static final Translator trans = Application.getTranslator();
	
	public PodSetConfig(OpenRocketDocument document, RocketComponent component) {
		super(document, component);
	
		// only stages which are actually off-centerline will get the dialog here:
		tabbedPane.insertTab( trans.get("RocketCompCfg.tab.Parallel"), null, parallelTab( (ComponentAssembly) component ), trans.get("RocketCompCfg.tab.ParallelComment"), 1);
	}
	
	private JPanel parallelTab( final ComponentAssembly assembly ){
		JPanel motherPanel = new JPanel( new MigLayout("fill"));
	
		// set radial distance
		JLabel radiusLabel = new JLabel(trans.get("StageConfig.parallel.radius"));  
		motherPanel.add( radiusLabel , "align left");
		DoubleModel radiusModel = new DoubleModel( assembly, "RadialOffset", UnitGroup.UNITS_LENGTH, 0);
		//radiusModel.setCurrentUnit( UnitGroup.UNITS_LENGTH.getUnit("cm"));
		JSpinner radiusSpinner = new JSpinner( radiusModel.getSpinnerModel());
		radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner ));
		motherPanel.add(radiusSpinner , "growx 1, align right");
		UnitSelector radiusUnitSelector = new UnitSelector(radiusModel);
		motherPanel.add(radiusUnitSelector, "growx 1, wrap");
		
		// set location angle around the primary stage
		JLabel angleLabel = new JLabel(trans.get("StageConfig.parallel.angle"));
		motherPanel.add( angleLabel, "align left");
		DoubleModel angleModel = new DoubleModel( assembly, "AngularOffset", 1.0, UnitGroup.UNITS_ANGLE, 0.0, Math.PI*2);
		angleModel.setCurrentUnit( UnitGroup.UNITS_ANGLE.getUnit("rad"));
		JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
		angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
		motherPanel.add(angleSpinner, "growx 1");
		UnitSelector angleUnitSelector = new UnitSelector(angleModel);
		motherPanel.add( angleUnitSelector, "growx 1, wrap");
		
		// set multiplicity
		JLabel countLabel = new JLabel(trans.get("StageConfig.parallel.count"));
		motherPanel.add( countLabel, "align left");
		
		IntegerModel countModel = new IntegerModel( assembly, "InstanceCount", 2);
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
		DoubleModel axialOffsetModel = new DoubleModel( assembly, "AxialOffset", UnitGroup.UNITS_LENGTH);
		axialOffsetModel.setCurrentUnit(UnitGroup.UNITS_LENGTH.getUnit("cm"));
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
