package net.sf.openrocket.gui.configdialog;


import javax.swing.JPanel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;



public class BulkheadConfig extends RingComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public BulkheadConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		JPanel tab;
		
		tab = generalTab(trans.get("BulkheadCfg.tab.Diameter"), null, null,
				trans.get("BulkheadCfg.tab.Thickness"));
		//// General and General properties
		tabbedPane.insertTab(trans.get("BulkheadCfg.tab.General"), null, tab,
				trans.get("BulkheadCfg.tab.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);
	}
	
}