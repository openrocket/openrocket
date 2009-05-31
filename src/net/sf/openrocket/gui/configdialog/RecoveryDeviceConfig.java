package net.sf.openrocket.gui.configdialog;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RocketComponent;


public abstract class RecoveryDeviceConfig extends RocketComponentConfig {

	protected final List<JComponent> altitudeComponents = new ArrayList<JComponent>();
	
	public RecoveryDeviceConfig(RocketComponent component) {
		super(component);
	}

	
	
	@Override
	public void updateFields() {
		super.updateFields();
		
		if (altitudeComponents == null)
			return;
		
		boolean enabled = (((RecoveryDevice)component).getDeployEvent() 
				== RecoveryDevice.DeployEvent.ALTITUDE); 
		
		for (JComponent c: altitudeComponents) {
			c.setEnabled(enabled);
		}
	}
}
