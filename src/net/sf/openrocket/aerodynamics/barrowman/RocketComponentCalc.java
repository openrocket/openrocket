package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public abstract class RocketComponentCalc {

	public RocketComponentCalc(RocketComponent component) {

	}
	
	/**
	 * Calculate the non-axial forces produced by the component (normal and side forces,
	 * pitch, yaw and roll moments and CP position).  The values are stored in the
	 * <code>AerodynamicForces</code> object.  Additionally the value of CNa is computed
	 * and stored if possible without large amount of extra calculation, otherwise
	 * NaN is stored.  The CP coordinate is stored in local coordinates and moments are
	 * computed around the local origin.
	 * 
	 * @param conditions	the flight conditions.
	 * @param forces		the object in which to store the values.
	 * @param warnings		set in which to store possible warnings.
	 */
	public abstract void calculateNonaxialForces(FlightConditions conditions, 
			AerodynamicForces forces, WarningSet warnings);

	
	/**
	 * Calculates the pressure drag of the component.  This component does NOT include
	 * the effect of discontinuities in the rocket body.
	 * 
	 * @param conditions	the flight conditions.
	 * @param stagnationCD	the current stagnation drag coefficient
	 * @param baseCD		the current base drag coefficient
	 * @param warnings		set in which to store possible warnings
	 * @return				the pressure drag of the component
	 */
	public abstract double calculatePressureDragForce(FlightConditions conditions, 
			double stagnationCD, double baseCD, WarningSet warnings);
}
