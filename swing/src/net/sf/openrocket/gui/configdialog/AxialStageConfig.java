package net.sf.openrocket.gui.configdialog;

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
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration.SeparationEvent;
import net.sf.openrocket.startup.Application;

public class AxialStageConfig extends ComponentAssemblyConfig {
	private static final long serialVersionUID = -944969957186522471L;
	private static final Translator trans = Application.getTranslator();
	
	public AxialStageConfig(OpenRocketDocument document, RocketComponent component, JDialog parent) {
		super(document, component, parent);
		
		// Stage separation config (for non-first stage)
		if (component.getStageNumber() > 0) {
			JPanel tab = separationTab((AxialStage) component);
			tabbedPane.insertTab(trans.get("StageConfig.tab.Separation"), null, tab,
					trans.get("StageConfig.tab.Separation.ttip"), 0);
			tabbedPane.setSelectedIndex(0);
		}
	}
	
	
	private JPanel separationTab(AxialStage stage) {
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Select separation event
		panel.add(new StyledLabel(trans.get("StageConfig.separation.lbl.title") + " " + CommonStrings.dagger, Style.BOLD), "spanx, wrap rel");

		StageSeparationConfiguration sepConfig = stage.getSeparationConfiguration();
		
		JComboBox<?> combo = new JComboBox<StageSeparationConfiguration.SeparationEvent>(
				new EnumModel<StageSeparationConfiguration.SeparationEvent>( sepConfig, "SeparationEvent", SeparationEvent.values()));
		
		//combo.setSelectedItem(sepConfig);
		panel.add(combo, "");
		
		// ... and delay
		panel.add(new JLabel(trans.get("StageConfig.separation.lbl.plus")), "");
		
		DoubleModel dm = new DoubleModel( sepConfig, "SeparationDelay", 0);
		JSpinner spin = new JSpinner(dm.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "width 45");
		
		//// seconds
		panel.add(new JLabel(trans.get("StageConfig.separation.lbl.seconds")), "wrap unrel");
		
		panel.add(new StyledLabel(CommonStrings.override_description, -1), "spanx, wrap para");
		
		return panel;
	}

	
}
