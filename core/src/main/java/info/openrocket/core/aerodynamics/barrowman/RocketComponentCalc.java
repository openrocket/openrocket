package info.openrocket.core.aerodynamics.barrowman;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;

public abstract class RocketComponentCalc {

	public RocketComponentCalc(RocketComponent component) {

	}

	/**
	 * Calculate the non-axial forces produced by the component (normal and side
	 * forces,
	 * pitch, yaw and roll moments and CP position). The values are stored in the
	 * <code>AerodynamicForces</code> object. Additionally the value of CNa is
	 * computed
	 * and stored if possible without large amount of extra calculation, otherwise
	 * NaN is stored. The CP coordinate is stored in local coordinates and moments
	 * are
	 * computed around the local origin.
	 * 
	 * @param conditions the flight conditions.
	 * @param transform  transformation from InstanceMap to get rotations rotations
	 * @param forces     the object in which to store the values.
	 * @param warnings   set in which to store possible warnings.
	 */
	public abstract void calculateNonaxialForces(FlightConditions conditions, Transformation transform,
			AerodynamicForces forces, WarningSet warnings);

	/**
	 * Calculates the friction drag of the component.
	 *
	 * @param conditions  the flight conditions
	 * @param componentCF component coefficient of friction, calculated in
	 *                    BarrowmanCalculator
	 * @param warnings    set in which to to store possible warnings
	 * @return the friction drag coefficient of the component
	 */
	public abstract double calculateFrictionCD(FlightConditions conditions, double componentCf, WarningSet warnings);

	/**
	 * Calculates the pressure drag of the component. This does NOT include
	 * the effect of discontinuities in the rocket body, nor trailing edge
	 * (base) drag — see {@link #calculateComponentBaseCD}.
	 *
	 * @param conditions   the flight conditions.
	 * @param stagnationCD the current stagnation drag coefficient
	 * @param baseCD       the current base drag coefficient
	 * @param warnings     set in which to store possible warnings
	 * @return the pressure drag coefficient of the component
	 */
	public abstract double calculatePressureCD(FlightConditions conditions,
			double stagnationCD, double baseCD, WarningSet warnings);

	/**
	 * Calculates the base (trailing edge) drag of the component.
	 * The default implementation returns 0; subclasses with meaningful
	 * base drag (e.g. fins) should override.
	 *
	 * @param conditions the flight conditions.
	 * @param baseCD     the current base drag coefficient
	 * @param warnings   set in which to store possible warnings
	 * @return the base drag coefficient of the component
	 */
	public double calculateComponentBaseCD(FlightConditions conditions,
			double baseCD, WarningSet warnings) {
		return 0;
	}

	/**
	 * Calculation of Reynolds Number
	 * 
	 * @param length     characteristic length
	 * @param conditions Flight conditions taken into account
	 * @return Reynolds Number
	 */
	public double calculateReynoldsNumber(double length, FlightConditions conditions) {
		return conditions.getVelocity() * length /
				conditions.getAtmosphericConditions().getKinematicViscosity();
	}

	/** CP position along the MAC at subsonic speeds, as a fraction of the MAC. */
	protected static final double SUBSONIC_CP_POS = 0.25;
	private static final double TRANSONIC_CP_START_MACH = 0.5;
	private static final double TRANSONIC_CP_END_MACH = 2.0;

	/**
	 * Offset in the numerator of the approximate supersonic CP formula
	 * <code>f(x) = (x - offset) / (2*x - 1)</code>, where
	 * <code>x = ar*beta</code>. Fleeman rounds the theoretical value
	 * <code>2/3</code> to <code>0.67</code>; the rounded value is retained for
	 * compatibility with the existing OpenRocket model.
	 */
	private static final double SUPERSONIC_CP_OFFSET = 0.67;

	/**
	 * NACA Report 1307, equation (63), states that the supersonic expression is
	 * valid only for <code>ar*beta &gt; 1</code>.
	 */
	private static final double SUPERSONIC_CP_VALIDITY_BOUNDARY = 1.0;

	/**
	 * Start of the bounded bridge between the quarter chord and the lower boundary
	 * of the source formula. It is derived by solving for the point where the
	 * rounded formula equals the subsonic CP position:
	 * <pre>
	 *   (x - offset) / (2*x - 1) = subsonicCP
	 *   x = (offset - subsonicCP) / (1 - 2*subsonicCP) = 0.84
	 * </pre>
	 * Below this point the only available generic fallback is the subsonic
	 * quarter-chord position. Between this point and the source boundary a cubic
	 * Hermite bridge provides a continuous value and slope without evaluating the
	 * source formula on its nonphysical branch.
	 */
	private static final double LOW_AR_CP_BRIDGE_START =
			(SUPERSONIC_CP_OFFSET - SUBSONIC_CP_POS) / (1 - 2 * SUBSONIC_CP_POS);

	/**
	 * Largest normalized endpoint slope for which the fifth-order transonic
	 * interpolation is monotone. Its quadratic coefficient is
	 * <code>delta * (10 - 6*slopeRatio)</code>, which becomes negative above
	 * <code>5/3</code> and initially moves the CP in the wrong direction.
	 */
	private static final double MONOTONIC_QUINTIC_SLOPE_RATIO = 5.0 / 3.0;

	/**
	 * The CP position along the MAC at supersonic speeds, as a fraction of the MAC.
	 * <p>
	 * The published expression is used only at and above its
	 * <code>ar*beta = 1</code> applicability boundary. Below that boundary, where a
	 * generic planform-independent low-aspect-ratio theory is unavailable, a bounded
	 * cubic bridge joins it to the quarter-chord fallback. This removes the pole at
	 * <code>ar*beta = 0.5</code> while preserving value and slope continuity.
	 *
	 * @param arBeta the fin aspect ratio multiplied by the Prandtl factor beta
	 * @return the CP position along the MAC
	 */
	protected static double supersonicCPPos(double arBeta) {
		if (arBeta <= LOW_AR_CP_BRIDGE_START) {
			return SUBSONIC_CP_POS;
		}
		if (arBeta >= SUPERSONIC_CP_VALIDITY_BOUNDARY) {
			return sourceSupersonicCPPos(arBeta);
		}

		final double width = SUPERSONIC_CP_VALIDITY_BOUNDARY - LOW_AR_CP_BRIDGE_START;
		final double t = (arBeta - LOW_AR_CP_BRIDGE_START) / width;
		final double t2 = t * t;
		final double t3 = t2 * t;
		final double sourcePosition = sourceSupersonicCPPos(SUPERSONIC_CP_VALIDITY_BOUNDARY);
		final double sourceGradient = sourceSupersonicCPGradient(SUPERSONIC_CP_VALIDITY_BOUNDARY);

		// Cubic Hermite basis. The quarter-chord endpoint has zero gradient.
		final double startBasis = 2 * t3 - 3 * t2 + 1;
		final double endBasis = -2 * t3 + 3 * t2;
		final double endGradientBasis = t3 - t2;
		return startBasis * SUBSONIC_CP_POS + endBasis * sourcePosition +
				endGradientBasis * width * sourceGradient;
	}

	/**
	 * Interpolate the CP position between Mach 0.5 and Mach 2.
	 * <p>
	 * For ordinary aspect ratios this is the original fifth-order interpolation,
	 * calculated directly from its endpoint constraints rather than rounded symbolic
	 * coefficients. At low aspect ratios the endpoint slope becomes too large relative
	 * to its displacement for that polynomial to remain monotone. In that case a
	 * shape-preserving continuation of the limiting monotone polynomial is used. Both
	 * forms match the regularized Mach-2 position and first derivative.
	 *
	 * @param mach        Mach number, between 0.5 and 2
	 * @param aspectRatio fin aspect ratio
	 * @return the CP position along the MAC
	 */
	protected static double transonicCPPos(double mach, double aspectRatio) {
		final double machRange = TRANSONIC_CP_END_MACH - TRANSONIC_CP_START_MACH;
		final double t = (mach - TRANSONIC_CP_START_MACH) / machRange;
		final double arBetaAtMach2 = aspectRatio * Math.sqrt(3);
		final double endpointPosition = supersonicCPPos(arBetaAtMach2);
		final double delta = endpointPosition - SUBSONIC_CP_POS;

		if (delta <= 0) {
			return SUBSONIC_CP_POS;
		}

		/*
		 * At Mach 2, d(ar*beta)/dM = 2*ar/sqrt(3). Multiplying by the
		 * 1.5-wide normalized Mach interval reduces the normalized endpoint slope to
		 * arBeta*d(CP)/d(arBeta).
		 */
		final double normalizedEndpointSlope =
				arBetaAtMach2 * supersonicCPGradient(arBetaAtMach2);
		final double slopeRatio = normalizedEndpointSlope / delta;

		if (slopeRatio <= MONOTONIC_QUINTIC_SLOPE_RATIO) {
			final double t2 = t * t;
			final double t3 = t2 * t;
			final double t4 = t3 * t;
			final double t5 = t4 * t;

			return SUBSONIC_CP_POS +
					(10 * delta - 6 * normalizedEndpointSlope) * t2 +
					(-20 * delta + 14 * normalizedEndpointSlope) * t3 +
					(15 * delta - 11 * normalizedEndpointSlope) * t4 +
					(3 * normalizedEndpointSlope - 4 * delta) * t5;
		}

		/*
		 * At slopeRatio=5/3 the normalized limiting quintic is
		 * B(t)=(10/3)t^3-(10/3)t^4+t^5. Raising B to slopeRatio/(5/3)
		 * preserves monotonicity and gives exactly the requested endpoint slope.
		 */
		final double t2 = t * t;
		final double t3 = t2 * t;
		final double limitingQuintic = t3 * (10.0 / 3.0 - (10.0 / 3.0) * t + t2);
		return SUBSONIC_CP_POS + delta * Math.pow(limitingQuintic,
				slopeRatio / MONOTONIC_QUINTIC_SLOPE_RATIO);
	}

	/** Evaluate the source expression in its stated applicability domain. */
	private static double sourceSupersonicCPPos(double arBeta) {
		return (arBeta - SUPERSONIC_CP_OFFSET) / (2 * arBeta - 1);
	}

	/** Return the source expression's derivative with respect to ar*beta. */
	private static double sourceSupersonicCPGradient(double arBeta) {
		final double denominator = 2 * arBeta - 1;
		return (2 * SUPERSONIC_CP_OFFSET - 1) / (denominator * denominator);
	}

	/** Return the derivative of the regularized supersonic CP curve. */
	private static double supersonicCPGradient(double arBeta) {
		if (arBeta <= LOW_AR_CP_BRIDGE_START) {
			return 0;
		}
		if (arBeta >= SUPERSONIC_CP_VALIDITY_BOUNDARY) {
			return sourceSupersonicCPGradient(arBeta);
		}

		final double width = SUPERSONIC_CP_VALIDITY_BOUNDARY - LOW_AR_CP_BRIDGE_START;
		final double t = (arBeta - LOW_AR_CP_BRIDGE_START) / width;
		final double t2 = t * t;
		final double sourcePosition = sourceSupersonicCPPos(SUPERSONIC_CP_VALIDITY_BOUNDARY);
		final double sourceGradient = sourceSupersonicCPGradient(SUPERSONIC_CP_VALIDITY_BOUNDARY);

		final double startBasisGradient = 6 * t2 - 6 * t;
		final double endBasisGradient = -startBasisGradient;
		final double endGradientBasisGradient = 3 * t2 - 2 * t;
		return (startBasisGradient * SUBSONIC_CP_POS + endBasisGradient * sourcePosition) / width +
				endGradientBasisGradient * sourceGradient;
	}

}
