package info.openrocket.core.simulation;

import info.openrocket.core.motor.IgnitionEvent;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.MotorConfigurationId;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RocketComponent;

public class MotorClusterState {

	// for reference: set at initialization ONLY.
	final protected Motor motor;
	final protected MotorConfiguration config;
	final protected int motorCount;
	final protected double thrustDuration;

	// for state:
	protected double ignitionTime = Double.NaN;
	protected double cutoffTime = Double.NaN;
	protected double ejectionTime = Double.NaN;
	protected ThrustState currentState = ThrustState.ARMED;

	public MotorClusterState(final MotorConfiguration _config) {
		this.config = _config;

		this.motor = this.config.getMotor();
		this.motorCount = this.config.getMount().getComponentLocations().length;
		this.thrustDuration = this.motor.getBurnTimeEstimate();

		this.reset();
	}

	public double getIgnitionTime() {
		return ignitionTime;
	}

	public IgnitionEvent getIgnitionEvent() {
		return config.getIgnitionEvent();
	}

	public void ignite(final double _ignitionTime) {
		if (ThrustState.ARMED == currentState) {
			this.ignitionTime = _ignitionTime;
			this.currentState = this.currentState.getNext();
			// }else{
			// System.err.println("!! Attempted to ignite motor "+toDescription()
			// +" with current status="+this.currentState.getName()+" ... Ignoring.");
		}
	}

	public void burnOut(final double _burnOutTime) {
		if (ThrustState.THRUSTING == currentState) {
			this.cutoffTime = _burnOutTime;
			this.currentState = this.currentState.getNext();
			// }else{
			// System.err.println("!! Attempted to turn off motor state "+toDescription()+"
			// with current status="
			// +this.currentState.getName()+" ... Ignoring.");
		}
		if (!this.hasEjectionCharge()) {
			this.currentState = ThrustState.SPENT;
		}
	}

	public void expend(final double _ejectionTime) {
		if (ThrustState.DELAYING == currentState) {
			this.ejectionTime = _ejectionTime;
			this.currentState = this.currentState.getNext();
			// }else{
			// System.err.println("!! Attempted to mark as spent motor state
			// "+toDescription()+" with current status="
			// +this.currentState.getName()+" ... Ignoring.");
		}
	}

	public double getBurnTime() {
		return motor.getBurnTime();
	}

	/**
	 * Alias for "burnOut(double)"
	 */
	public void cutOff(final double _cutoffTime) {
		burnOut(_cutoffTime);
	}

	public MotorConfiguration getConfig() {
		return config;
	}

	public double getEjectionDelay() {
		return config.getEjectionDelay();
	}

	public MotorConfigurationId getID() {
		return config.getID();
	}

	public double getPropellantMass() {
		return (motor.getLaunchMass() - motor.getBurnoutMass());
	}

	public double getPropellantMass(final double motorTime) {
		return (motor.getPropellantMass(motorTime) - motor.getBurnoutMass());
	}

	public MotorMount getMount() {
		return config.getMount();
	}

	public Motor getMotor() {
		return this.motor;
	}

	double getCutOffTime() {
		return this.cutoffTime;
	}

	public double getMotorTime(final double _simulationTime) {
		return Math.max(_simulationTime - this.getIgnitionTime(), 0.0);
	}

	/**
	 * Compute the thrust
	 * 
	 * @param simulationTime
	 * @return
	 */
	public double getThrust(final double simulationTime) {
		if (this.currentState.isThrusting()) {
			double motorTime = this.getMotorTime(simulationTime);
			return this.motorCount * motor.getThrust(motorTime);

		} else {
			return 0.0;
		}
	}

	public boolean isPlugged() {
		return (this.config.getEjectionDelay() == Motor.PLUGGED_DELAY);
	}

	public boolean hasEjectionCharge() {
		return !isPlugged();
	}

	public boolean isDelaying() {
		return currentState == ThrustState.DELAYING;
	}

	public boolean isSpent() {
		return currentState == ThrustState.SPENT;
	}

	/**
	 * alias to 'resetToPreflight()'
	 */
	public void reset() {
		// i.e. in the "future"
		ignitionTime = Double.POSITIVE_INFINITY;
		cutoffTime = Double.POSITIVE_INFINITY;
		ejectionTime = Double.POSITIVE_INFINITY;

		currentState = ThrustState.ARMED;
	}

	public boolean testForIgnition(FlightConfiguration flightConfiguration, final FlightEvent _event) {
		RocketComponent mount = (RocketComponent) this.getMount();
		return getIgnitionEvent().isActivationEvent(flightConfiguration, _event, mount);
	}

	public String toDescription() {
		return String.format("%32s / %4s - %s",
				getMount().getDebugName(), this.motor.getDesignation(), this.currentState.getName());
	}

	@Override
	public String toString() {
		return this.motor.getDesignation();
	}

}
