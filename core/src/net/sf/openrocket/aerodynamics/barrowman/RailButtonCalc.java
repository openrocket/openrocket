package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;

public class RailButtonCalc extends RocketComponentCalc {
	
	private double refArea;

	public RailButtonCalc(RocketComponent component) {
		super(component);

		final RailButton button = (RailButton) component;

		final double outerArea = button.getTotalHeight() * button.getOuterDiameter();
		final double notchArea = (button.getOuterDiameter() - button.getInnerDiameter()) * button.getInnerHeight();

		refArea = (outerArea - notchArea) * button.getInstanceCount();
	}

	@Override
	public double calculateFrictionCD(FlightConditions conditions, double componentCf, WarningSet warnings) {
		// very small relative surface area, and slick
		return 0.0;
	}

	@Override
	public void calculateNonaxialForces(FlightConditions conditions, Transformation transform,
			AerodynamicForces forces, WarningSet warnings) {
		// Nothing to be done
	}

	@Override
	public double calculatePressureCD(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {

		// this is reasonably close for Reynolds numbers roughly 10e4 to 2*10e5, which takes us to low supersonic speeds.
		// see Hoerner p. 3-9 fig 12, we summarizes a bunch of sources
		// I expect we'll have compressibility effects having an impact well below that, so this is probably good up
		// to the transonic regime.
		double CDmul = 1.2;
												  
		return CDmul*stagnationCD * refArea / conditions.getRefArea();
	}
}
