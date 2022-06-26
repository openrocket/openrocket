package net.sf.openrocket.gui.configdialog;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;


@SuppressWarnings("serial")
public class ComponentAssemblyConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	private final RocketComponent component;
	
	public ComponentAssemblyConfig(OpenRocketDocument document, RocketComponent component, JDialog parent) {
		super(document, component, parent);
		this.component = component;
	
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
		final ComboBoxModel<RadiusMethod> radiusMethodModel = new EnumModel<RadiusMethod>( boosters, "RadiusMethod", RadiusMethod.choices());
		final JComboBox<RadiusMethod> radiusMethodCombo = new JComboBox<RadiusMethod>( radiusMethodModel );
		motherPanel.add( radiusMethodCombo, "spanx 3, growx, wrap");
		
		// set radial distance
		JLabel radiusLabel = new JLabel(trans.get("StageConfig.parallel.radius"));
		motherPanel.add( radiusLabel , "align left");
		//radiusMethodModel.addEnableComponent(radiusLabel, false);
		DoubleModel radiusModel = new DoubleModel( boosters, "RadiusOffset", UnitGroup.UNITS_LENGTH, 0);

		JSpinner radiusSpinner = new JSpinner(radiusModel.getSpinnerModel());
		radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner));
		motherPanel.add(radiusSpinner , "wmin 65lp, growx 1, align right");
//		autoRadOffsModel.addEnableComponent(radiusSpinner, false);
		UnitSelector radiusUnitSelector = new UnitSelector(radiusModel);
		motherPanel.add(radiusUnitSelector, "growx 1");
		motherPanel.add(new BasicSlider(radiusModel.getSliderModel(0, new DoubleModel(component.getParent(), "OuterRadius", 4.0, UnitGroup.UNITS_LENGTH))),
				"gapleft para, growx 2, wrap");

		radiusMethodCombo.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				radiusModel.stateChanged(new EventObject(e));
			}
		});

		// set angle
		JLabel angleLabel = new JLabel(trans.get("StageConfig.parallel.angle"));
		motherPanel.add( angleLabel, "align left");
		DoubleModel angleModel = new DoubleModel( boosters, "AngleOffset", 1.0, UnitGroup.UNITS_ANGLE, 0.0, Math.PI*2);

		JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
		angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
		motherPanel.add(angleSpinner, "wmin 65lp, growx 1");
		UnitSelector angleUnitSelector = new UnitSelector(angleModel);
		motherPanel.add( angleUnitSelector, "growx 1");
		motherPanel.add(new BasicSlider(angleModel.getSliderModel(-Math.PI, Math.PI)), "gapleft para, growx 2, wrap");

		// set multiplicity
		JLabel countLabel = new JLabel(trans.get("StageConfig.parallel.count"));
		motherPanel.add( countLabel, "align left");
		
		IntegerModel countModel = new IntegerModel( boosters, "InstanceCount", 1);
		JSpinner countSpinner = new JSpinner(countModel.getSpinnerModel());
		countSpinner.setEditor(new SpinnerEditor(countSpinner));
		motherPanel.add(countSpinner, "wmin 65lp, growx 1, wrap");
		
		// setPositions relative to parent component
		JLabel positionLabel = new JLabel(trans.get("LaunchLugCfg.lbl.Posrelativeto"));
		motherPanel.add( positionLabel);
		
		ComboBoxModel<AxialMethod> axialPositionMethodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
		JComboBox<?> positionMethodCombo = new JComboBox<AxialMethod>( axialPositionMethodModel );
		motherPanel.add(positionMethodCombo, "spanx 3, growx, wrap");
		
		// plus
		motherPanel.add(new JLabel(trans.get("StageConfig.parallel.plus")), "right");

		final DoubleModel axialOffsetModel = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
		final JSpinner axialOffsetSpinner = new JSpinner(axialOffsetModel.getSpinnerModel());
		axialOffsetSpinner.setEditor(new SpinnerEditor(axialOffsetSpinner));

		motherPanel.add(axialOffsetSpinner, "wmin 65lp, growx 1");

		positionMethodCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				axialOffsetModel.stateChanged(new EventObject(e));
			}
		});

		motherPanel.add(new UnitSelector(axialOffsetModel), "growx");
		motherPanel.add(new BasicSlider(axialOffsetModel.getSliderModel(
						new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
						new DoubleModel(component.getParent(), "Length"))),
				"gapleft para, growx 2, wrap");
		
		return motherPanel;
	}
}
