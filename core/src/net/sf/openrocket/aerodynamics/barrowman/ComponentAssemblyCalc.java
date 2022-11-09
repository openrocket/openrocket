package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Transformation;

/*
 * Aerodynamic properties of a component assembly.  Since an "assembly"
 * has no impact except as a summation of its subparts, just returns 0
 *
 */
public class ComponentAssemblyCalc extends RocketComponentCalc {
	public ComponentAssemblyCalc(RocketComponent c) {
		super(c);
	}

	@Override
	public void calculateNonaxialForces(FlightConditions conditions, Transformation transform,
										AerodynamicForces forces, WarningSet warnings) {
		// empty
	}

	@Override
	public double calculateFrictionCD(FlightConditions conditions, double componentCf, WarningSet warnings) {
		return 0;
	}
	
	@Override
	public double calculatePressureCD(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {
		return 0;
	}

	
}
