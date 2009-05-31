package net.sf.openrocket.gui.configdialog;


import javax.swing.JPanel;

import net.sf.openrocket.rocketcomponent.RocketComponent;



public class SleeveConfig extends RingComponentConfig {

	public SleeveConfig(RocketComponent c) {
		super(c);
		
		JPanel tab;
		
		tab = generalTab("Outer diameter:", "Inner diameter:", "Wall thickness:", "Length:");
		tabbedPane.insertTab("General", null, tab, "General properties", 0);
		tabbedPane.setSelectedIndex(0);
	}
	
}