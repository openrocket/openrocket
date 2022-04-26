package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;

public class LaunchLugCalc extends TubeCalc {

	public LaunchLugCalc(RocketComponent component) {
		super(component);
	}

	@Override
	public void calculateNonaxialForces(FlightConditions conditions, Transformation transform,
			AerodynamicForces forces, WarningSet warnings) {
		// Nothing to be done
	}

	@Override
	public double calculateFrictionCD(FlightConditions conditions, double componentCf, WarningSet warnings) {
		// launch lug doesn't add enough area to worry about
		return 0;
	}
}
