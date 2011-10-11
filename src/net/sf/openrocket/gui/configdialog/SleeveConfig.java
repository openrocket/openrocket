package net.sf.openrocket.gui.configdialog;


import javax.swing.JPanel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;



public class SleeveConfig extends RingComponentConfig {
	private static final Translator trans = Application.getTranslator();
	
	public SleeveConfig(OpenRocketDocument d, RocketComponent c) {
		super(d, c);
		
		JPanel tab;
		//// Outer diameter:
		//// Inner diameter:
		//// Wall thickness:
		//// Length:
		tab = generalTab(trans.get("SleeveCfg.tab.Outerdiam"), trans.get("SleeveCfg.tab.Innerdiam"),
				trans.get("SleeveCfg.tab.Wallthickness"), trans.get("SleeveCfg.tab.Length"));
		//// General and General properties
		tabbedPane.insertTab(trans.get("SleeveCfg.tab.General"), null, tab,
				trans.get("SleeveCfg.tab.Generalproperties"), 0);
		tabbedPane.setSelectedIndex(0);
	}
	
}