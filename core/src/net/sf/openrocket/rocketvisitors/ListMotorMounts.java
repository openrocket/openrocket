package net.sf.openrocket.rocketvisitors;

import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;

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
