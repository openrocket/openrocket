package net.sf.openrocket.simulation;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.util.Coordinate;

public class RK4SimulationStatus extends SimulationStatus implements Cloneable {
	private Coordinate launchRodDirection;
	
	private double previousAcceleration = 0;
	private AtmosphericConditions previousAtmosphericConditions;
	
	// Used for determining when to store aerodynamic computation warnings:
	private double maxZVelocity = 0;
	private double startWarningTime = -1;
	
	public RK4SimulationStatus(Configuration configuration,
			MotorInstanceConfiguration motorConfiguration,
			SimulationConditions simulationConditions ) {
		super(configuration, motorConfiguration, simulationConditions);
	}

	public RK4SimulationStatus( SimulationStatus other ) {
		super(other);
		if ( other instanceof RK4SimulationStatus ) {
			this.launchRodDirection = ((RK4SimulationStatus) other).launchRodDirection;
			this.previousAcceleration = ((RK4SimulationStatus) other).previousAcceleration;
			this.maxZVelocity = ((RK4SimulationStatus) other).maxZVelocity;
			this.startWarningTime = ((RK4SimulationStatus) other).startWarningTime;
			this.previousAtmosphericConditions = ((RK4SimulationStatus) other).previousAtmosphericConditions;
		}
	}
	public void setLaunchRodDirection(Coordinate launchRodDirection) {
		this.launchRodDirection = launchRodDirection;
	}
	
	
	public Coordinate getLaunchRodDirection() {
		return launchRodDirection;
	}
	
	

	public double getPreviousAcceleration() {
		return previousAcceleration;
	}
	
	
	public void setPreviousAcceleration(double previousAcceleration) {
		this.previousAcceleration = previousAcceleration;
	}
	
	
	public AtmosphericConditions getPreviousAtmosphericConditions() {
		return previousAtmosphericConditions;
	}
	
	
	public void setPreviousAtmosphericConditions(
			AtmosphericConditions previousAtmosphericConditions) {
		this.previousAtmosphericConditions = previousAtmosphericConditions;
	}
	
	
	public double getMaxZVelocity() {
		return maxZVelocity;
	}
	
	
	public void setMaxZVelocity(double maxZVelocity) {
		this.maxZVelocity = maxZVelocity;
	}
	
	
	public double getStartWarningTime() {
		return startWarningTime;
	}
	
	
	public void setStartWarningTime(double startWarningTime) {
		this.startWarningTime = startWarningTime;
	}
	
	@Override
	public RK4SimulationStatus clone() {
		return (RK4SimulationStatus) super.clone();
	}
	
}
