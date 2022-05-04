package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Tube;
import net.sf.openrocket.util.MathUtil;

public abstract class TubeCalc extends RocketComponentCalc {

	private final double diameter;
	private final double length;
	protected final double innerArea;
	private final double totalArea;
	private final double frontalArea;
	
	public TubeCalc(RocketComponent component) {
		super(component);

		Tube tube = (Tube)component;
		
		length = tube.getLength();
		diameter = 2 * tube.getInnerRadius();
		innerArea = Math.PI * MathUtil.pow2(tube.getInnerRadius());
		totalArea = Math.PI * MathUtil.pow2(tube.getOuterRadius());
		frontalArea = totalArea - innerArea;
	}

	@Override
	public double calculatePressureCD(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {

		// Need to check for tube inner area 0 in case of rockets using launch lugs with
		// an inner radius of 0 to emulate rail buttons (or just weird rockets, of course)
		final double deltap;
		if (innerArea > MathUtil.EPSILON) {
			// calculation of pressure drop through pipe from "Atlas Copco Air Compendium",
			// 1975, quoted as equation 14 in Carello, Ivanov, and Mazza, "Pressure drop
			// in pipe lines for compressed air: comparison between experimental and
			// theoretical analysis", Transactions on Engineering Sciences vol 18,
			// ISSN 1743-35331998, 1998.

			// Volume flow rate
			final double Q = conditions.getVelocity() * innerArea;
			
			// pressure drop
			deltap = 1.6 * Math.pow(Q, 1.85) * length /
				(Math.pow(diameter, 5) * conditions.getAtmosphericConditions().getPressure());
		} else {
			deltap = 0.0;
		}
		
		// convert to CD and return
		return (deltap * innerArea + stagnationCD * frontalArea) / conditions.getRefArea();
	}

}
