package info.openrocket.swing.gui.configdialog;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.DeploymentConfiguration.DeployEvent;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.rocketcomponent.RocketComponent;


public abstract class RecoveryDeviceConfig extends RocketComponentConfig {
	
	private static final long serialVersionUID = 7263235700953855062L;
	
	protected final List<JComponent> altitudeComponents = new ArrayList<JComponent>();
	
	public RecoveryDeviceConfig(OpenRocketDocument d, RocketComponent component, JDialog parent) {
		super(d, component, parent);
	}
	
	
	
	@Override
	public void updateFields() {
		super.updateFields();
		
		if (altitudeComponents == null || altitudeComponents.size() == 0)
			return;
		
		boolean enabled = (((RecoveryDevice) component).getDeploymentConfigurations().getDefault().getDeployEvent() == DeployEvent.ALTITUDE);
		
		for (JComponent c : altitudeComponents) {
			c.setEnabled(enabled);
		}
	}
}
