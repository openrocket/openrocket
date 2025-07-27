package info.openrocket.core.simulation;

import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.RecoveryDevice;

public class BasicLandingStepper extends AbstractEulerStepper {

	@Override
	protected double computeCD(SimulationStatus status) {
		// Accumulate CD for all recovery devices
		double cd = 0;
		final InstanceMap imap = status.getConfiguration().getActiveInstances();
		for (RecoveryDevice c : status.getDeployedRecoveryDevices()) {
			cd += imap.count(c) * c.getCD() * c.getArea() / status.getConfiguration().getReferenceArea();
		}
		return cd;
	}
}
