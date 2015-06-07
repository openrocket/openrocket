package net.sf.openrocket.gui.configdialog;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.RocketComponent;


public abstract class RecoveryDeviceConfig extends RocketComponentConfig {
	
	private static final long serialVersionUID = 7263235700953855062L;
	
	protected final List<JComponent> altitudeComponents = new ArrayList<JComponent>();
	
	public RecoveryDeviceConfig(OpenRocketDocument d, RocketComponent component) {
		super(d, component);
	}
	
	
	
	@Override
	public void updateFields() {
		super.updateFields();
		
		if (altitudeComponents == null)
			return;
		
		boolean enabled = (((RecoveryDevice) component).getDeploymentConfiguration().getDefault().getDeployEvent() == DeployEvent.ALTITUDE);
		
		for (JComponent c : altitudeComponents) {
			c.setEnabled(enabled);
		}
	}
}
