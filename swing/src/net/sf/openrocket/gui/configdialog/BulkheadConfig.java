package net.sf.openrocket.gui.configdialog;


import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;



@SuppressWarnings("serial")
public class BulkheadConfig extends RingComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public BulkheadConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
		super(d, c, parent);
		
		JPanel tab;
		
		tab = generalTab(trans.get("BulkheadCfg.tab.Diameter"), null, null,
				trans.get("BulkheadCfg.tab.Thickness"));
		//// General and General properties
		tabbedPane.insertTab(trans.get("BulkheadCfg.tab.General"), null, tab,
				trans.get("BulkheadCfg.tab.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);

		// Apply the custom focus travel policy to this panel
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
}