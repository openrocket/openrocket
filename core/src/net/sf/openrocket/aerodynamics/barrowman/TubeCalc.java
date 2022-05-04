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
		// These calculations come from a mix of theoretical and empirical
		// results, and are marked with (t) for theoretical and (e) for empirical.
		// The theoretical results should not be modified; the empirical can be adjusted
		// to better simulate real rockets as we get data.

		// Temperature
		final double T = conditions.getAtmosphericConditions().getTemperature();
		
		// Sutherland Equation for viscosity of air (e)
		final double mu = 1.458e-6 * Math.pow(T, 3/2) / (T + 110.4); //

		// Volume flow rate (t)
		final double Q = conditions.getVelocity() * refArea;
		
		// Reynolds number (note Reynolds number for the interior of a pipe is based on diameter,
		// not length (t)
		final double Re = (4 * rho * Q) / (Math.PI * diameter * mu);
		
		// quoted as equation 12 in Carello, Ivanov, and Mazza, "Pressure drop in pipe
		// lines for compressed air: comparison between experimental and theoretical analysis",
		// Transactions on Engineering Sciences vol 18, ISSN 1743-35331998, 1998.

		// friction coefficient (for tube interior) (e)
		final double lambda = 0.3164 * Math.pow(Re, -0.25);
		
		// pressure drop (e)
		// 101.325 is standard pressure
		// Power in equation is 5 in original source.  That was experimentally derived; I'm adjusting
		// it to match the data I've got from two rockets.
		final double deltap = (lambda * 8 * length * rho * MathUtil.pow2(Q) * T * 101.325) /
			(MathUtil.pow2(Math.PI) * Math.pow(diameter, 4.8) * 273 * conditions.getAtmosphericConditions().getPressure());

		// convert to CD and return
		return deltap * refArea / conditions.getRefArea();
	}

}
