package net.sf.openrocket.gui.configdialog;


import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;



@SuppressWarnings("serial")
public class CenteringRingConfig extends RingComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public CenteringRingConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
		super(d, c, parent);
		
		JPanel tab;
		
		//// Outer diameter: and Inner diameter: and Thickness:
		tab = generalTab(trans.get("CenteringRingCfg.tab.Outerdiam"),
				trans.get("CenteringRingCfg.tab.Innerdiam"), null,
				trans.get("CenteringRingCfg.tab.Thickness"));
		//// General and General properties
		tabbedPane.insertTab(trans.get("CenteringRingCfg.tab.General"), null, tab,
				trans.get("CenteringRingCfg.tab.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this panel
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
}