package info.openrocket.swing.gui.configdialog;


import javax.swing.JDialog;
import javax.swing.JPanel;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;



public class ThicknessRingComponentConfig extends RingComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public ThicknessRingComponentConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
		super(d, c, parent);
		
		JPanel tab;

		//// Length:
		//// Outer diameter:
		//// Inner diameter:
		//// Wall thickness:
		tab = generalTab(trans.get("ThicknessRingCompCfg.tab.Length"), trans.get("ThicknessRingCompCfg.tab.Outerdiam"),
				trans.get("ThicknessRingCompCfg.tab.Innerdiam"),
				trans.get("ThicknessRingCompCfg.tab.Wallthickness"));
		//// General and General properties
		tabbedPane.insertTab(trans.get("ThicknessRingCompCfg.tab.General"), null, tab,
				trans.get("ThicknessRingCompCfg.tab.Generalprop"), 0);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this panel
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
}