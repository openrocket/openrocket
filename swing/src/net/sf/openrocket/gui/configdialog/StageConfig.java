package net.sf.openrocket.gui.configdialog;

import javax.swing.JComboBox;
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
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Stage;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.startup.Application;

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
