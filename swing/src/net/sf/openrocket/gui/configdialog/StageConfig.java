package net.sf.openrocket.gui.configdialog;

import java.awt.Component;
import java.awt.Container;

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
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.OutsideComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.startup.Application;

public class StageConfig extends RocketComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	private BooleanModel parallelEnabledModel = null;
	private JPanel parallelEnabledPanel = null;
	
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
		parallelEnabledModel = new BooleanModel( component, "Parallel");
		parallelEnabledModel.setValue(false);
		JCheckBox parallelEnabled = new JCheckBox( parallelEnabledModel);
		parallelEnabled.setText(trans.get("RocketCompCfg.parallel.inline"));
		motherPanel.add(parallelEnabled, "wrap");

		JPanel enabledPanel = new JPanel( new MigLayout("fill"));
		this.parallelEnabledPanel = enabledPanel;
		
		enabledPanel.add(new JSeparator(SwingConstants.HORIZONTAL), "growx,wrap");

		// set radial distance 
		enabledPanel.add(new JLabel(trans.get("RocketCompCfg.parallel.radius")), "align left");
		DoubleModel radiusModel = new DoubleModel( stage, "RadialPosition", 0.0);
		JSpinner radiusSpinner = new JSpinner( radiusModel.getSpinnerModel());
		radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner ));
		enabledPanel.add(radiusSpinner , "growx, wrap, align right");

		// set angle around the primary stage
		enabledPanel.add(new JLabel(trans.get("RocketCompCfg.parallel.angle")), "align left");
		DoubleModel angleModel = new DoubleModel( stage, "AngularPosition", 0.0, Math.PI*2);
		JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
		angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
		enabledPanel.add(angleSpinner, "growx, wrap");

		enabledPanel.add(new JLabel(trans.get("RocketCompCfg.parallel.rotation")), "align left");
		DoubleModel rotationModel = new DoubleModel( stage, "Rotation", 0.0, Math.PI*2);
		JSpinner rotationSpinner = new JSpinner(rotationModel.getSpinnerModel());
		rotationSpinner.setEditor(new SpinnerEditor(rotationSpinner));
		enabledPanel.add(rotationSpinner, "growx, wrap");
		
		setDeepEnabled( enabledPanel, parallelEnabledModel.getValue());
		parallelEnabled.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setDeepEnabled( parallelEnabledPanel, parallelEnabledModel.getValue());
			}
		});

		motherPanel.add( enabledPanel , "growx, wrap");
				
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
