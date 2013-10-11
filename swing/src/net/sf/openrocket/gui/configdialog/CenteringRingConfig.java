package net.sf.openrocket.gui.configdialog;


import javax.swing.JPanel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;



public class CenteringRingConfig extends RingComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public CenteringRingConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		JPanel tab;
		
		//// Outer diameter: and Inner diameter: and Thickness:
		tab = generalTab(trans.get("CenteringRingCfg.tab.Outerdiam"),
				trans.get("CenteringRingCfg.tab.Innerdiam"), null,
				trans.get("CenteringRingCfg.tab.Thickness"));
		//// General and General properties
		tabbedPane.insertTab(trans.get("CenteringRingCfg.tab.General"), null, tab,
				trans.get("CenteringRingCfg.tab.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);
	}
	
}