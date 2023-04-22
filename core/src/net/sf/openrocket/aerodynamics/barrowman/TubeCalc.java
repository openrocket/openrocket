package net.sf.openrocket.aerodynamics.barrowman;

import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.logging.WarningSet;
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
	private final Tube tube;
	
	public TubeCalc(RocketComponent component) {
		super(component);

		tube = (Tube)component;
		
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
			// Temperature and Pressure
			final double T = conditions.getAtmosphericConditions().getTemperature();
			final double P = conditions.getAtmosphericConditions().getPressure();
			
			// Volume flow rate (t)
			final double Q = conditions.getVelocity() * innerArea;

			// Air viscosity
			final double mu = conditions.getAtmosphericConditions().getKinematicViscosity();

			// Air density
			final double rho = 1.225; // at standard temperature and pressure
			
			// Reynolds number (note Reynolds number for the interior of a pipe is based on diameter,
			// not length (t))
			final double Re = (4.0 * rho * Q) / (Math.PI * diameter * mu);
			
			// friction coefficient (for smooth tube interior) (e)
			final double lambda = 1/MathUtil.pow2(2 * Math.log(0.5625 * Math.pow(Re, 0.875)) - 0.8);
			
			// pressure drop (e)
			final double P0 = 101325; // standard pressure
			final double T0 = 273.15; // standard temperature
			deltap = ((lambda * 8 * length * rho * MathUtil.pow2(Q)) / (MathUtil.pow2(Math.PI) * Math.pow(diameter, 5)) * (T/T0) * (P0/P));
		} else {
			deltap = 0.0;
		}
		   
		// convert to CD and return
		final double cdpress = 2.0 * deltap / (conditions.getAtmosphericConditions().getDensity() * MathUtil.pow2(conditions.getVelocity()));
		final double cd =   (cdpress * innerArea + 0.43*(stagnationCD + baseCD) * frontalArea)/conditions.getRefArea();
		
		return cd;
	}
}
