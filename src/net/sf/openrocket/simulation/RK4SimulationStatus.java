package net.sf.openrocket.simulation;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.util.Coordinate;

public class RK4SimulationStatus extends SimulationStatus {
	private Coordinate launchRodDirection;
	
	private double previousAcceleration = 0;
	private AtmosphericConditions previousAtmosphericConditions;
	
	// Used for determining when to store aerodynamic computation warnings:
	private double maxZVelocity = 0;
	private double startWarningTime = -1;
	
	
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
