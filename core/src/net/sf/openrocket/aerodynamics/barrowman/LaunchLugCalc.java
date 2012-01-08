package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.MathUtil;

public class LaunchLugCalc extends RocketComponentCalc {

	private double CDmul;
	private double refArea;
	
	public LaunchLugCalc(RocketComponent component) {
		super(component);
		
		LaunchLug lug = (LaunchLug)component;
		double ld = lug.getLength() / (2*lug.getOuterRadius());
		
		CDmul = Math.max(1.3 - ld, 1);
		refArea = Math.PI * MathUtil.pow2(lug.getOuterRadius()) - 
				  Math.PI * MathUtil.pow2(lug.getInnerRadius()) * Math.max(1 - ld, 0);
	}

	@Override
	public void calculateNonaxialForces(FlightConditions conditions,
			AerodynamicForces forces, WarningSet warnings) {
		// Nothing to be done
	}

	@Override
	public double calculatePressureDragForce(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {

		return CDmul*stagnationCD * refArea / conditions.getRefArea();
	}

}
