package info.openrocket.core.rocketvisitors;

import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class ListMotorMounts extends ListComponents<RocketComponent> {

	public ListMotorMounts() {
		super(RocketComponent.class);
	}

	@Override
	protected void doAction(RocketComponent visitable) {
		if (visitable instanceof MotorMount && ((MotorMount) visitable).isMotorMount()) {
			components.add(visitable);
		}
	}
}
