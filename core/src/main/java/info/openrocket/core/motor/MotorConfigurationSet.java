package info.openrocket.core.motor;

import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.FlightConfigurableParameterSet;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.MotorMount;

import java.util.Map;

/**
 * FlightConfigurationSet for motors.
 * This is used for motors, where the default value is always no motor.
 */
public class MotorConfigurationSet extends FlightConfigurableParameterSet<MotorConfiguration> {
	public static final int DEFAULT_MOTOR_EVENT_TYPE = ComponentChangeEvent.MOTOR_CHANGE
			| ComponentChangeEvent.EVENT_CHANGE;

	public MotorConfigurationSet(final MotorMount mount) {
		super(new MotorConfiguration(mount, FlightConfigurationId.DEFAULT_VALUE_FCID));
	}

	/**
	 * Construct a copy of an existing FlightConfigurationSet.
	 * 
	 * @param configSet another flightConfiguration to copy data from.
	 * @param component the rocket component on which events are fired when the
	 *                  parameter values are changed
	 * @param eventType the event type that will be fired on changes
	 */
	public MotorConfigurationSet(FlightConfigurableParameterSet<MotorConfiguration> sourceSet,
			final MotorMount newMount) {
		// creates a new empty config w/ default value
		super(new MotorConfiguration(newMount, FlightConfigurationId.DEFAULT_VALUE_FCID));

		for (MotorConfiguration sourceConfig : sourceSet) {
			FlightConfigurationId nextFCID = sourceConfig.getFCID();
			MotorConfiguration nextValue = new MotorConfiguration(newMount, nextFCID, sourceConfig);
			set(nextFCID, nextValue);
		}
	}

	@Override
	public void setDefault(MotorConfiguration value) {
		throw new UnsupportedOperationException("Cannot change default value of motor configuration");
	}

	@Override
	public String toDebug() {
		StringBuilder buffer = new StringBuilder();
		final MotorMount mnt = this.getDefault().getMount();
		buffer.append(String.format(" ====== Dumping MotorConfigurationSet: %d motors in %s ======\n",
				this.size(), mnt.getDebugName()));

		for (Map.Entry<FlightConfigurationId, MotorConfiguration> entry : this.map.entrySet()) {
			FlightConfigurationId loopFCID = entry.getKey();
			MotorConfiguration curConfig = entry.getValue();
			if (this.isDefault(loopFCID)) {
				buffer.append("  [DEF]");
			} else {
				buffer.append("       ");
			}

			buffer.append(String.format("@%10s=[fcid//%8s][mid//%8s][    %8s ign@: %12s]\n",
					loopFCID.toShortKey(),
					curConfig.getFCID().toShortKey(),
					curConfig.getMID().toShortKey(),
					curConfig.toMotorName(),
					curConfig.toIgnitionDescription()));

		}
		return buffer.toString();
	}

}
