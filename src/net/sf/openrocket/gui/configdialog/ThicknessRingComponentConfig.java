package net.sf.openrocket.gui.configdialog;


import javax.swing.JPanel;

import net.sf.openrocket.rocketcomponent.RocketComponent;



public class ThicknessRingComponentConfig extends RingComponentConfig {

	public ThicknessRingComponentConfig(RocketComponent c) {
		super(c);
		
		JPanel tab;
		
		tab = generalTab("Outer diameter:", "Inner diameter:", "Wall thickness:", "Length:");
		tabbedPane.insertTab("General", null, tab, "General properties", 0);
		tabbedPane.setSelectedIndex(0);
	}
	
}