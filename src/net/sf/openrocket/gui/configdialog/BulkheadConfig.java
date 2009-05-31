package net.sf.openrocket.gui.configdialog;


import javax.swing.JPanel;

import net.sf.openrocket.rocketcomponent.RocketComponent;



public class BulkheadConfig extends RingComponentConfig {

	public BulkheadConfig(RocketComponent c) {
		super(c);
		
		JPanel tab;
		
		tab = generalTab("Radius:", null, null, "Thickness:");
		tabbedPane.insertTab("General", null, tab, "General properties", 0);
		tabbedPane.setSelectedIndex(0);
	}
	
}