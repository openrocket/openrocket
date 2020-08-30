package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;

public class RailButtonCalc extends RocketComponentCalc {

	private double CDmul;
	private double refArea;
	
	public RailButtonCalc(RocketComponent component) {
		super(component);
		
		final RailButton button = (RailButton) component;
		
		final double outerArea = button.getTotalHeight() / button.getOuterDiameter();
		final double notchArea = 2*( (button.getOuterDiameter() - button.getInnerDiameter()) * button.getInnerHeight());

		CDmul = 1.0;
		refArea = outerArea - notchArea;
	}

	@Override
	public void calculateNonaxialForces(FlightConditions conditions, Transformation transform,
			AerodynamicForces forces, WarningSet warnings) {
		// Nothing to be done
	}

	@Override
	public double calculatePressureDragForce(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {
		
		return CDmul*stagnationCD * refArea / conditions.getRefArea();
	}

}
