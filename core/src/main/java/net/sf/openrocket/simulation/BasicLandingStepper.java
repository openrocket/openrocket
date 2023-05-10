package net.sf.openrocket.simulation;

import net.sf.openrocket.rocketcomponent.InstanceMap;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;

public class BasicLandingStepper extends AbstractEulerStepper {

	@Override
	protected double computeCD(SimulationStatus status) {
		// Accumulate CD for all recovery devices
		cd = 0;
		final InstanceMap imap = status.getConfiguration().getActiveInstances();
		for (RecoveryDevice c : status.getDeployedRecoveryDevices()) {
			cd += imap.count(c) * c.getCD() * c.getArea() / status.getConfiguration().getReferenceArea();
		}
		return cd;
	}
}
