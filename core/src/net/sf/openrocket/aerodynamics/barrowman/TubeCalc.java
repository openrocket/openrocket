package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Tube;
import net.sf.openrocket.util.MathUtil;

public abstract class TubeCalc extends RocketComponentCalc {

	private final double diameter;
	private final double length;
	protected double refArea;
	
	public TubeCalc(RocketComponent component) {
		super(component);

		Tube tube = (Tube)component;
		
		length = tube.getLength();
		diameter = 2 * tube.getInnerRadius();
		refArea = Math.PI * MathUtil.pow2(tube.getInnerRadius());
	}

	@Override
	public double calculatePressureCD(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {

		// calculation of pressure drop through pipe from "Atlas Copco Air Compendium", 1975,
		// quoted as equation 14 in Carello, Ivanov, and Mazza, "Pressure drop in pipe
		// lines for compressed air: comparison between experimental and theoretical analysis",
		// Transactions on Engineering Sciences vol 18, ISSN 1743-35331998, 1998.

		// Volume flow rate
		final double Q = conditions.getVelocity() * refArea;

		// pressure drop.
		final double deltap;
		if (refArea == 0) {
			warnings.add(Warning.ZERO_INNER_RADIUS);
			deltap = 0;
		} else {
			deltap = 1.6 * Math.pow(Q, 1.85) * length /
			(Math.pow(diameter, 5) * conditions.getAtmosphericConditions().getPressure());
		}
		
		// convert to CD and return
		return deltap * refArea / conditions.getRefArea();
	}

}
