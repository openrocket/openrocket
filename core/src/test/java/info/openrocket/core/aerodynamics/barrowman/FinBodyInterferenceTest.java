package info.openrocket.core.aerodynamics.barrowman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import org.junit.jupiter.api.Test;

public class FinBodyInterferenceTest {
	private static final double EPSILON = 1.0e-12;
	private static final double TAU = 0.25;

	/**
	 * Equations 14 and 21 must retain their exact slender-body sum.
	 */
	@Test
	public void separatesSlenderBodyInterferenceFactors() {
		double finFactor = NACA1307FinBodyInterference.calculateFinInBodyFactor(TAU);
		double bodyFactor = NACA1307FinBodyInterference.calculateSlenderBodyInFinFactor(TAU, finFactor);

		assertEquals(1.5625, finFactor + bodyFactor, EPSILON);
		assertTrue(finFactor > 1.0);
		assertTrue(bodyFactor > 0.0);
	}

	/**
	 * Equation 19 uses the lowercase incidence factor, which is distinct from
	 * the uppercase body-angle-of-attack factor in equation 14.
	 */
	@Test
	public void reproducesNacaWingIncidenceFactor() {
		double incidenceFactor = NACA1307FinBodyInterference.calculateWingIncidenceFactor(0.2);
		double angleOfAttackFactor = NACA1307FinBodyInterference.calculateFinInBodyFactor(0.2);

		assertEquals(0.94, incidenceFactor, 0.005);
		assertEquals(1.16, angleOfAttackFactor, 0.005);
		assertTrue(incidenceFactor < angleOfAttackFactor);
	}

	/**
	 * Chart 3 replaces equation 19 for rectangular wings when beta*A exceeds
	 * two.  These source points are read from the beta*A=3 and 4 curves.
	 */
	@Test
	public void reproducesNacaChartThreeRectangularWingIncidenceFactors() {
		double betaThreeMach = Math.sqrt(10.0);
		double betaFourMach = Math.sqrt(17.0);

		assertEquals(0.940, NACA1307FinBodyInterference.calculateWingIncidenceFactor(
				0.2, betaThreeMach, 1.0, true), 0.006);
		assertEquals(0.965, NACA1307FinBodyInterference.calculateWingIncidenceFactor(
				0.2, betaFourMach, 1.0, true), 0.006);
	}

	/**
	 * The report's chart-selection condition must not leak into nonrectangular
	 * fins or below its beta*A boundary.
	 */
	@Test
	public void limitsChartThreeToItsPublishedSelectionRegion() {
		double slenderBodyFactor =
				NACA1307FinBodyInterference.calculateWingIncidenceFactor(0.2);
		double betaTwoMach = Math.sqrt(5.0);
		double betaThreeMach = Math.sqrt(10.0);

		assertEquals(slenderBodyFactor,
				NACA1307FinBodyInterference.calculateWingIncidenceFactor(
						0.2, betaTwoMach, 1.0, true), EPSILON);
		assertEquals(slenderBodyFactor,
				NACA1307FinBodyInterference.calculateWingIncidenceFactor(
						0.2, betaThreeMach, 1.0, false), EPSILON);
	}

	/**
	 * The two approximations differ at the selection boundary, so the narrow
	 * engineering fairing must prevent a Mach-dependent roll-force step.
	 */
	@Test
	public void remainsContinuousAtChartThreeSelectionBoundary() {
		double delta = 1.0e-6;
		double belowMach = Math.sqrt(1.0 + Math.pow(2.0 - delta, 2));
		double aboveMach = Math.sqrt(1.0 + Math.pow(2.0 + delta, 2));

		double below = NACA1307FinBodyInterference.calculateWingIncidenceFactor(
				0.2, belowMach, 1.0, true);
		double above = NACA1307FinBodyInterference.calculateWingIncidenceFactor(
				0.2, aboveMach, 1.0, true);

		assertEquals(below, above, 1.0e-9);
	}

	/**
	 * Reproduce the wing-body entries of the Mach-2 computing example in
	 * NACA Report 1307, table I.  The source values are rounded to two or three
	 * decimal places, so the assertions use the corresponding chart precision.
	 */
	@Test
	public void reproducesNacaTableOneWingBodyExample() {
		NACA1307FinBodyInterference model = createTableOneModel(Double.POSITIVE_INFINITY);
		double liftCurveSlope = 0.0406 * 180.0 / Math.PI;

		NACA1307FinBodyInterference.Result result = model.calculate(2.0, liftCurveSlope);

		assertEquals(1.16, result.finFactor(), 0.005);
		assertEquals(0.23, result.bodyFactor(), 0.01);
		assertEquals(0.938, result.bodyCp() / 2.25, 0.015);
		assertEquals(0.94, result.incidenceFactor(), 0.005);
	}

	/**
	 * Validate against the independent wind-tunnel result for combination 2a
	 * in table II.  The report gives beta*CLa=5.69 experimentally and 6.01
	 * from its method for this Mach-1.5 triangular wing-body configuration.
	 */
	@Test
	public void matchesPublishedWingBodyExperiment() {
		double mach = 1.5;
		double beta = Math.sqrt(mach * mach - 1.0);
		double totalSemispan = 1.0;
		double bodyRadius = 0.201 * totalSemispan;
		double exposedSpan = totalSemispan - bodyRadius;
		double rootChord = exposedSpan;
		double betaWingLiftCurveSlope = 4.0;
		double betaNoseLiftCurveSlope = 0.44;
		NACA1307FinBodyInterference model = new NACA1307FinBodyInterference(
				bodyRadius, exposedSpan, rootChord, 0.0, rootChord, 4.0,
				Double.POSITIVE_INFINITY);

		NACA1307FinBodyInterference.Result result = model.calculate(
				mach, betaWingLiftCurveSlope / beta);
		double estimatedBetaLiftCurveSlope = betaNoseLiftCurveSlope
				+ betaWingLiftCurveSlope * (result.finFactor() + result.bodyFactor());

		assertEquals(5.69, estimatedBetaLiftCurveSlope, 0.569);
	}

	/**
	 * At beta*A=7, the slowest chart-16 family has reached its Appendix-D
	 * lifting-line result.  The no-trailing-edge-sweep triangular case at
	 * r/s=0.2 reads about 0.41 root chords from the chart.
	 */
	@Test
	public void reproducesNacaChartSixteenLiftingLineEndpoint() {
		NACA1307FinBodyInterference model = new NACA1307FinBodyInterference(
				0.562, 2.25, 2.25, 0.0, 2.25, 7.0, Double.POSITIVE_INFINITY);
		NACA1307FinBodyInterference.Result result = model.calculate(0.0, 2.4);

		assertEquals(0.41, result.bodyCp() / 2.25, 0.015);
		double radiusSemispanRatio = 0.562 / (0.562 + 2.25);
		assertEquals(Math.pow(1.0 + radiusSemispanRatio, 2),
				result.finFactor() + result.bodyFactor(), EPSILON);
	}

	/**
	 * The nonzero-radius curves in chart 16(g) reach their lifting-line value
	 * near beta*A=4, unlike the r/s=0 curve which continues to about seven.
	 */
	@Test
	public void reproducesNacaChartSixteenRadiusFamily() {
		double rootChord = 2.25;
		NACA1307FinBodyInterference model = new NACA1307FinBodyInterference(
				0.5625, 2.25, rootChord, 0.0, rootChord, 4.0, Double.POSITIVE_INFINITY);

		double bodyCp = model.calculate(0.0, 2.4).bodyCp() / rootChord;

		assertEquals(0.41, bodyCp, 0.015);
	}

	/**
	 * Reproduce interior points from the two triangular extremes in chart 16.
	 * These cases ensure that the slow no-trailing-edge-sweep fairing is not
	 * replaced by the much faster no-leading-edge-sweep curve.
	 */
	@Test
	public void reproducesNacaChartSixteenPlanformFamilies() {
		double bodyRadius = 0.562;
		double exposedSpan = 2.248;
		double rootChord = 4.0 * exposedSpan;
		NACA1307FinBodyInterference noLeadingEdgeSweep = new NACA1307FinBodyInterference(
				bodyRadius, exposedSpan, rootChord, 0.0, 0.0, 1.0, Double.POSITIVE_INFINITY);
		NACA1307FinBodyInterference noTrailingEdgeSweep = new NACA1307FinBodyInterference(
				bodyRadius, exposedSpan, rootChord, 0.0, rootChord, 1.0, Double.POSITIVE_INFINITY);

		double noLeadingEdgeCp = noLeadingEdgeSweep.calculate(0.0, 2.4).bodyCp() / rootChord;
		double noTrailingEdgeCp = noTrailingEdgeSweep.calculate(0.0, 2.4).bodyCp() / rootChord;

		assertEquals(0.14, noLeadingEdgeCp, 0.01);
		assertEquals(0.46, noTrailingEdgeCp, 0.01);
	}

	/**
	 * Clipping the pressure field at the root trailing edge represents the
	 * report's no-afterbody case and must move the body load forward.
	 */
	@Test
	public void accountsForFiniteAfterbody() {
		double liftCurveSlope = 0.0406 * 180.0 / Math.PI;
		NACA1307FinBodyInterference.Result withAfterbody =
				createTableOneModel(Double.POSITIVE_INFINITY).calculate(2.0, liftCurveSlope);
		NACA1307FinBodyInterference.Result withoutAfterbody =
				createTableOneModel(2.25).calculate(2.0, liftCurveSlope);

		assertTrue(withoutAfterbody.bodyFactor() < withAfterbody.bodyFactor());
		assertTrue(withoutAfterbody.bodyCp() < withAfterbody.bodyCp());
	}

	/**
	 * Chart 15 supplies the low-aspect-ratio CP and remains compatible with a
	 * finite afterbody by retaining the same normalized fairing.
	 */
	@Test
	public void accountsForFiniteAfterbodyAtLowAspectRatio() {
		double rootChord = 4.0 / 3.0;
		double liftCurveSlope = 2.4;
		NACA1307FinBodyInterference withAfterbody = new NACA1307FinBodyInterference(
				0.25, 1.0, rootChord, rootChord, 0.0, 1.5, Double.POSITIVE_INFINITY);
		NACA1307FinBodyInterference withoutAfterbody = new NACA1307FinBodyInterference(
				0.25, 1.0, rootChord, rootChord, 0.0, 1.5, rootChord);

		NACA1307FinBodyInterference.Result withResult =
				withAfterbody.calculate(1.6, liftCurveSlope);
		NACA1307FinBodyInterference.Result withoutResult =
				withoutAfterbody.calculate(1.6, liftCurveSlope);

		assertEquals(withResult.bodyFactor(), withoutResult.bodyFactor(), EPSILON);
		assertTrue(withoutResult.bodyCp() < withResult.bodyCp());
	}

	/**
	 * Chart 15(c) publishes independent radius-family curves.  Checking their
	 * interior ordinates prevents them from being collapsed back onto a shared
	 * curve with small abscissa adjustments.
	 */
	@Test
	public void reproducesNacaChartFifteenRectangularRadiusFamilies() {
		NACA1307FinBodyInterference model = new NACA1307FinBodyInterference(
				0.25, 1.0, 1.0, 1.0, 0.0, 2.0, Double.POSITIVE_INFINITY);

		assertEquals(0.359, model.interpolateChart15(0.25, 0.2), 0.006);
		assertEquals(0.705, model.interpolateChart15(0.50, 0.2), 0.006);
		assertEquals(0.872, model.interpolateChart15(0.75, 0.2), 0.006);
		assertEquals(0.336, model.interpolateChart15(0.25, 0.4), 0.006);
		assertEquals(0.690, model.interpolateChart15(0.50, 0.4), 0.006);
		assertEquals(0.858, model.interpolateChart15(0.75, 0.4), 0.006);
		assertEquals(0.275, model.interpolateChart15(0.25, 0.6), 0.006);
		assertEquals(0.550, model.interpolateChart15(0.50, 0.6), 0.006);
		assertEquals(0.775, model.interpolateChart15(0.75, 0.6), 0.006);
	}

	/**
	 * Report 1307 explicitly omits the chart-15(a) low-aspect-ratio
	 * extrapolation.  A geometry that needs that panel must use the fallback,
	 * while either adjacent published family remains usable.
	 */
	@Test
	public void rejectsGeometryThatRequiresMissingChartFifteenPanel() {
		NACA1307FinBodyInterference missingPanel = new NACA1307FinBodyInterference(
				0.2, 0.8, 1.0, 0.0, 0.0, 2.0, Double.POSITIVE_INFINITY);
		NACA1307FinBodyInterference halfTaperBoundary = new NACA1307FinBodyInterference(
				0.2, 0.8, 1.0, 0.5, 0.0, 2.0, Double.POSITIVE_INFINITY);
		NACA1307FinBodyInterference midchordBoundary = new NACA1307FinBodyInterference(
				0.2, 0.8, 1.0, 0.0, 0.5, 2.0, Double.POSITIVE_INFINITY);

		assertFalse(missingPanel.isApplicable());
		assertTrue(halfTaperBoundary.isApplicable());
		assertTrue(midchordBoundary.isApplicable());
	}

	/**
	 * Force and moment must not jump where equation 22 changes from chart 15 to
	 * the planar pressure model.
	 */
	@Test
	public void remainsContinuousAtEquationTwentyTwoBoundary() {
		double rootChord = 4.0 / 3.0;
		double boundaryMach = 5.0 / 3.0;
		double delta = 1.0e-7;
		NACA1307FinBodyInterference model = new NACA1307FinBodyInterference(
				0.25, 1.0, rootChord, rootChord, 0.0, 1.5, Double.POSITIVE_INFINITY);

		NACA1307FinBodyInterference.Result below = model.calculate(
				boundaryMach - delta, 2.4);
		NACA1307FinBodyInterference.Result above = model.calculate(
				boundaryMach + delta, 2.4);

		assertEquals(below.bodyFactor(), above.bodyFactor(), 1.0e-6);
		assertEquals(below.bodyCp(), above.bodyCp(), 1.0e-5);
		assertTrue(below.bodyCp() > 0.5 * rootChord);
	}

	/**
	 * Equations 23 and 25 have a finite common limit at m*beta=1.
	 */
	@Test
	public void remainsContinuousAtSonicLeadingEdgeLimit() {
		double boundaryMach = Math.sqrt(5.0);
		double delta = 1.0e-8;
		NACA1307FinBodyInterference model = new NACA1307FinBodyInterference(
				0.25, 1.0, 2.0, 0.0, 2.0, 2.0, Double.POSITIVE_INFINITY);

		NACA1307FinBodyInterference.Result below = model.calculate(
				boundaryMach - delta, 2.4);
		NACA1307FinBodyInterference.Result at = model.calculate(boundaryMach, 2.4);
		NACA1307FinBodyInterference.Result above = model.calculate(
				boundaryMach + delta, 2.4);

		assertEquals(below.bodyFactor(), at.bodyFactor(), 1.0e-5);
		assertEquals(at.bodyFactor(), above.bodyFactor(), 1.0e-5);
		assertEquals(below.bodyCp(), at.bodyCp(), 1.0e-5);
		assertEquals(at.bodyCp(), above.bodyCp(), 1.0e-5);
	}

	/**
	 * Chart 4(a), printed page 49, includes the subsonic-leading-edge branch.
	 * At beta*m=0.5 and 2*beta*r/c_r=1.5, its ordinate is approximately 1.52.
	 */
	@Test
	public void reproducesNacaChartFourSubsonicLeadingEdgeBranch() {
		double mach = 1.5;
		double beta = Math.sqrt(mach * mach - 1.0);
		double bodyRadius = 0.6;
		double exposedSpan = 0.4;
		double rootChord = 2.0 * beta * bodyRadius / 1.5;
		double betaWingLiftCurveSlope = 4.0;
		NACA1307FinBodyInterference model = new NACA1307FinBodyInterference(
				bodyRadius, exposedSpan, rootChord, 0.0, rootChord,
				4.0 * exposedSpan / rootChord,
				Double.POSITIVE_INFINITY);

		NACA1307FinBodyInterference.Result result = model.calculate(
				mach, betaWingLiftCurveSlope / beta);
		double totalSemispan = bodyRadius + exposedSpan;
		double chartOrdinate = result.bodyFactor() * betaWingLiftCurveSlope
				* (totalSemispan / bodyRadius - 1.0);

		assertEquals(1.52, chartOrdinate, 0.10);
	}

	/**
	 * The planar carryover is an absolute NACA load.  Its stored factor changes
	 * inversely with the caller's isolated-wing slope, so the applied load and
	 * pressure center remain unchanged when only that normalization changes.
	 */
	@Test
	public void preservesAbsolutePlanarLoadAcrossWingSlopeNormalization() {
		NACA1307FinBodyInterference model = createTableOneModel(Double.POSITIVE_INFINITY);
		NACA1307FinBodyInterference.Result first = model.calculate(2.0, 2.4);
		NACA1307FinBodyInterference.Result second = model.calculate(2.0, 3.6);

		assertEquals(first.bodyFactor() * 2.4, second.bodyFactor() * 3.6, EPSILON);
		assertEquals(first.bodyCp(), second.bodyCp(), EPSILON);
	}

	/**
	 * Flush equal-radius body tubes are one physical cylinder for the pressure
	 * model, even when the design tree splits them into separate components.
	 */
	@Test
	public void includesFollowingEqualRadiusBodyTubeInAfterbody() {
		AfterbodyFixture fixture = createAfterbodyFixture(0.1);

		double bodyEnd = FinSetCalc.calculateCylindricalAfterbodyEnd(
				fixture.fins(), fixture.firstTube());

		assertEquals(2.4, bodyEnd, EPSILON);
	}

	/**
	 * A radius change invalidates the constant-cylinder assumption and ends the
	 * NACA integration at the parent tube rather than extrapolating aft.
	 */
	@Test
	public void stopsAfterbodyAtRadiusChange() {
		AfterbodyFixture fixture = createAfterbodyFixture(0.12);

		double bodyEnd = FinSetCalc.calculateCylindricalAfterbodyEnd(
				fixture.fins(), fixture.firstTube());

		assertEquals(0.4, bodyEnd, EPSILON);
	}

	/**
	 * The body load and its moment are blended together through the transonic
	 * interval, avoiding a CP discontinuity at either endpoint.
	 */
	@Test
	public void remainsContinuousAtTransonicBoundaries() {
		NACA1307FinBodyInterference model = createTableOneModel(Double.POSITIVE_INFINITY);
		double liftCurveSlope = 0.0406 * 180.0 / Math.PI;

		NACA1307FinBodyInterference.Result belowSubsonic = model.calculate(0.9 - 1.0e-7,
				liftCurveSlope);
		NACA1307FinBodyInterference.Result aboveSubsonic = model.calculate(0.9 + 1.0e-7,
				liftCurveSlope);
		NACA1307FinBodyInterference.Result belowSupersonic = model.calculate(1.5 - 1.0e-7,
				liftCurveSlope);
		NACA1307FinBodyInterference.Result aboveSupersonic = model.calculate(1.5 + 1.0e-7,
				liftCurveSlope);

		assertEquals(belowSubsonic.bodyFactor(), aboveSubsonic.bodyFactor(), 1.0e-6);
		assertEquals(belowSubsonic.bodyCp(), aboveSubsonic.bodyCp(), 1.0e-6);
		assertEquals(belowSupersonic.bodyFactor(), aboveSupersonic.bodyFactor(), 1.0e-6);
		assertEquals(belowSupersonic.bodyCp(), aboveSupersonic.bodyCp(), 1.0e-6);
	}

	/**
	 * Unsupported geometries retain the simplified issue-2489 correction.
	 */
	@Test
	public void fallbackIncludesBodyContributionAtSubsonicSpeeds() {
		assertEquals(1.5625, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 0.5), EPSILON);
		assertEquals(1.5625, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 0.9), EPSILON);
	}

	/**
	 * The fallback body term is smoothly removed through the transonic interval.
	 */
	@Test
	public void fallbackBlendsBodyContributionThroughTransonicSpeeds() {
		assertEquals(1.40625, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 1.2), EPSILON);
	}

	/**
	 * The fallback retains the established classical correction supersonically.
	 */
	@Test
	public void fallbackRetainsClassicalCorrectionAtSupersonicSpeeds() {
		assertEquals(1.25, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 1.5), EPSILON);
		assertEquals(1.25, FinSetCalc.calculateBodyFinInterferenceFactor(TAU, 3.0), EPSILON);
	}

	/**
	 * The small-angle NACA model is smoothly removed before stall and is never
	 * reused for reverse flow.
	 */
	@Test
	public void limitsNacaModelToForwardLinearAngles() {
		assertEquals(1.0, FinSetCalc.calculateNacaApplicabilityWeight(Math.toRadians(10.0)), EPSILON);
		assertEquals(0.5, FinSetCalc.calculateNacaApplicabilityWeight(Math.toRadians(15.0)), EPSILON);
		assertEquals(0.0, FinSetCalc.calculateNacaApplicabilityWeight(Math.toRadians(20.0)), EPSILON);
		assertEquals(0.0, FinSetCalc.calculateNacaApplicabilityWeight(Math.toRadians(160.0)), EPSILON);
	}

	private static NACA1307FinBodyInterference createTableOneModel(double bodyEnd) {
		return new NACA1307FinBodyInterference(
				0.562, 2.25, 2.25, 0.0, 2.25, 4.0, bodyEnd);
	}

	private static AfterbodyFixture createAfterbodyFixture(double followingRadius) {
		Rocket rocket = new Rocket();
		AxialStage stage = new AxialStage();
		rocket.addChild(stage);
		BodyTube firstTube = new BodyTube(1.0, 0.1);
		stage.addChild(firstTube);
		BodyTube followingTube = new BodyTube(2.0, followingRadius);
		stage.addChild(followingTube);
		TrapezoidFinSet fins = new TrapezoidFinSet(4, 0.4, 0.2, 0.1, 0.2);
		firstTube.addChild(fins);
		fins.setAxialMethod(AxialMethod.BOTTOM);
		fins.setAxialOffset(0.0);
		rocket.enableEvents();
		return new AfterbodyFixture(firstTube, fins);
	}

	private record AfterbodyFixture(BodyTube firstTube, TrapezoidFinSet fins) {
	}
}
