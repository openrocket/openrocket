package info.openrocket.core.aerodynamics.barrowman;

import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Tube;
import info.openrocket.core.util.MathUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TubeCalc extends RocketComponentCalc {

	private final static Logger log = LoggerFactory.getLogger(TubeFinSetCalc.class);

	private final Tube tube;
	private final double diameter;
	private final double length;
	protected final double innerArea;
	private final double totalArea;
	private final double frontalArea;
	private final double epsilon;

	public TubeCalc(RocketComponent component) {
		super(component);

		tube = (Tube) component;

		length = tube.getLength();
		diameter = 2 * tube.getInnerRadius();
		innerArea = Math.PI * MathUtil.pow2(tube.getInnerRadius());
		totalArea = Math.PI * MathUtil.pow2(tube.getOuterRadius());
		frontalArea = totalArea - innerArea;
		epsilon = tube.getFinish().getRoughnessSize(); // roughness; note we don't maintain surface roughness of
		// interior separately from exterior.
	}

	@Override
	public double calculatePressureCD(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings) {

		// If we aren't moving, treat CD as 0
		final double v = conditions.getVelocity();
		if (v < MathUtil.EPSILON)
			return 0;

		// Need to check for tube inner area 0 in case of rockets using launch lugs with
		// an inner radius of 0 to emulate rail guides (or just weird rockets, of
		// course)
		double tubeCD = 0.0;
		double deltap;
		if (innerArea > MathUtil.EPSILON) {
			// Current atmospheric conditions
			final double p = conditions.getAtmosphericConditions().getPressure();
			final double t = conditions.getAtmosphericConditions().getTemperature();
			final double rho = conditions.getAtmosphericConditions().getDensity();

			// Reynolds number (note Reynolds number for the interior of a pipe is based on
			// diameter,
			// not length (t))
			final double Re = v * diameter / conditions.getAtmosphericConditions().getKinematicViscosity();

			// friction coefficient using Swamee-Jain equation
			double f = 0.25 / MathUtil.pow2(Math.log10((epsilon / (3.7 * diameter) + 5.74 / Math.pow(Re, 0.9))));

			// If we're supersonic, apply a correction
			if (conditions.getMach() > 1) {
				f = f / conditions.getBeta();
			}

			// pressure drop using Darcy-Weissbach equation
			deltap = f * (length * rho * MathUtil.pow2(v)) / (2 * diameter);

			// drag coefficient of tube interior from pressure drop
			tubeCD = 2 * (deltap * innerArea) / (rho * MathUtil.pow2(v) * innerArea);
		}

		// convert to CD and return
		final double cd = (tubeCD * innerArea + 0.7 * (stagnationCD + baseCD) * frontalArea) / conditions.getRefArea();
		return cd;
	}
}
