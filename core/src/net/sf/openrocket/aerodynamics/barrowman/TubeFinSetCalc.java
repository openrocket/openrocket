package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;

public class TubeFinSetCalc extends RocketComponentCalc {
	
	public TubeFinSetCalc(RocketComponent component) {
		super(component);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void calculateNonaxialForces(FlightConditions conditions, AerodynamicForces forces, WarningSet warnings) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public double calculatePressureDragForce(FlightConditions conditions, double stagnationCD, double baseCD, WarningSet warnings) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
