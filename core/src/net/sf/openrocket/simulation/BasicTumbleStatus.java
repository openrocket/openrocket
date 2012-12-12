package net.sf.openrocket.simulation;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.Configuration;

public class BasicTumbleStatus extends SimulationStatus {

	private double tumbleCd;
	
	public BasicTumbleStatus(Configuration configuration,
			MotorInstanceConfiguration motorConfiguration,
			SimulationConditions simulationConditions) {
		super(configuration, motorConfiguration, simulationConditions);
		computeTumbleCd();
	}

	public BasicTumbleStatus(SimulationStatus orig) {
		super(orig);
		if ( orig instanceof BasicTumbleStatus ) {
			this.tumbleCd = ((BasicTumbleStatus) orig).tumbleCd;
		}
	}

	public double getTumbleCd( ) {
		return tumbleCd;
	}
	
	
	public void computeTumbleCd() {
		// FIXME - probably want to compute the overall CD more accurately.  Perhaps average
		// CD over three AoA: 0, 90, 180.  In any case, using barrowman to compute this Cd is
		// completely wrong.
		WarningSet warnings = new WarningSet();
		FlightConditions cond = new FlightConditions(this.getConfiguration());
		cond.setAOA(Math.PI);
		AerodynamicForces forces = this.getSimulationConditions().getAerodynamicCalculator().getAerodynamicForces(this.getConfiguration(), cond, warnings);
		tumbleCd = forces.getCD();
	}
}
