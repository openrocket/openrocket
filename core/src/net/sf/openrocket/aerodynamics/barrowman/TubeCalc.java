package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Tube;
import net.sf.openrocket.util.MathUtil;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TubeCalc extends RocketComponentCalc {
	
	private final static Logger log = LoggerFactory.getLogger(TubeFinSetCalc.class);

	// air density (standard conditions)
	private final double rho = 1.225; // kg/m^3
	
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
		
		// These calculations come from a mix of theoretical and empirical
		// results, and are marked with (t) for theoretical and (e) for empirical.
		// The theoretical results should not be modified; the empirical can be adjusted
		// to better simulate real rockets as we get data.

		// For the sources of the empirical formulas, see Carello, Ivanov, and Mazza,
		// "Pressure drop in pipe lines for compressed air: comparison between experimental
		// and theoretical analysis", Transactions on Engineering Sciences vol 18,
		// ISSN 1743-35331998, 1998.
		
		// For the rockets for which we have data, the effect of the stagnation CD appears to be
		// overstated.  This code multiplies it be a factor of 0.7 to better match experimental
		// data

		// Need to check for tube inner area 0 in case of rockets using launch lugs with
		// an inner radius of 0 to emulate rail buttons (or just weird rockets, of course)
		double deltap;
		if (innerArea > MathUtil.EPSILON) {
			// Temperature
			final double T = conditions.getAtmosphericConditions().getTemperature();
			
			// Volume flow rate (t)
			final double Q = conditions.getVelocity() * innerArea;
			
			// Reynolds number (note Reynolds number for the interior of a pipe is based on diameter,
			// not length (t))
			final double Re = conditions.getVelocity() * diameter /
				conditions.getAtmosphericConditions().getKinematicViscosity();
			
			// friction coefficient (for smooth tube interior) (e)
			final double lambda = 1/MathUtil.pow2(2 * Math.log(0.5625 * Math.pow(Re, 0.875)) - 0.8);
			
			// pressure drop (e)
			final double P0 = 100; // standard pressure
			final double T0 = 273.15; // standard temperature
			deltap = (lambda * 8 * length * rho * MathUtil.pow2(Q) * T * P0) /
				(MathUtil.pow2(Math.PI) * Math.pow(diameter, 5) * T0 * conditions.getAtmosphericConditions().getPressure());
		} else {
			deltap = 0.0;
		}
		   
		// convert to CD and return
		return (deltap * innerArea + 0.7 * stagnationCD * frontalArea) / conditions.getRefArea();
	}
}
