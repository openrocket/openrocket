package info.openrocket.core.aerodynamics.barrowman;

import info.openrocket.core.util.MathUtil;

/**
 * Fin-body interference model for zero-incidence fins based on NACA Report 1307.
 *
 * <p>The report treats the two exposed fin panels as one wing.  OpenRocket calls
 * this class once for each radial fin orientation, so the factors are applied to
 * one half-wing at a time and the existing radial projection supplies the other
 * half.  The factors themselves are unchanged by that decomposition.</p>
 *
 * @see <a href="https://ntrs.nasa.gov/citations/19930091008">NACA Report 1307</a>
 */
final class NACA1307FinBodyInterference {
	private static final double SUBSONIC_MACH = 0.9;
	private static final double SUPERSONIC_MACH = 1.5;
	private static final double MAX_CHART_RADIUS_SEMISPAN_RATIO = 0.6;
	private static final double EDGE_EPSILON = 1.0e-6;
	private static final double PLANAR_CRITERION = 4.0;
	private static final double PLANAR_TRANSITION_WIDTH = 0.25;
	private static final double BETA_M_LIMIT_EPSILON = 1.0e-10;
	private static final double CHART_3_MINIMUM_BETA_ASPECT_RATIO = 2.0;
	private static final double CHART_3_TRANSITION_WIDTH = 0.25;

	private static final double[] CHART_RADIUS_SEMISPAN_RATIO = { 0.0, 0.2, 0.4, 0.6 };

	/*
	 * Chart 15 is an extrapolated low-aspect-ratio fairing between the exact
	 * slender-body ordinate and the planar solution at the equation-22 boundary.
	 * These normalized ordinates follow the dotted portions of the published
	 * curves.  Keeping the endpoints analytic lets the same fairing represent a
	 * finite afterbody, for which Report 1307 does not publish a separate chart.
	 * The digitization coordinates, normalization, interpolation rules, and
	 * acceptance tolerances are recorded in
	 * {@code doc/techdoc/naca1307-chart-digitization.md}.
	 */
	private static final double[] CHART_15_GUIDE_NORMALIZED_CRITERION = {
			0.0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0
	};
	private static final double[] CHART_15_GUIDE_PLANAR_WEIGHT = {
			0.0, 0.23, 0.43, 0.61, 0.75, 0.86, 0.94, 0.985, 1.0
	};

	/*
	 * Chart 15(c), the rectangular unswept planform, has materially different
	 * curvature for each radius family.  These independent normalized ordinates
	 * replace the former single-curve abscissa scaling, which put the r/s=0.6 CP
	 * about 0.14 root chords too far aft at beta*A=0.5.
	 */
	private static final double[] CHART_15_RECTANGULAR_NORMALIZED_CRITERION = {
			0.0, 0.25, 0.5, 0.75, 1.0
	};
	private static final double[][] CHART_15_RECTANGULAR_PLANAR_WEIGHT = {
			{ 0.0, 0.10, 0.70, 0.94, 1.0 },
			{ 0.0, 0.359, 0.705, 0.872, 1.0 },
			{ 0.0, 0.336, 0.690, 0.858, 1.0 },
			{ 0.0, 0.275, 0.550, 0.775, 1.0 }
	};

	/*
	 * Radius-dependent abscissa corrections fitted to the four r/s families in
	 * charts 15 and 16.  The indices are sweep family, taper family, and r/s.
	 * A value above one means that the corresponding curve reaches its planar or
	 * lifting-line endpoint sooner than the r/s=0 guide curve.
	 * See {@code doc/techdoc/naca1307-chart-digitization.md} for the source-panel
	 * mapping and fit anchors; in particular, chart 16(g)'s 1.75 is the ratio of
	 * the r/s=0 endpoint near beta*A=7 to the finite-radius endpoint near beta*A=4.
	 */
	private static final double[][][] CHART_15_RADIUS_PROGRESS_SCALE = {
			{
					{ 1.00, 0.98, 0.94, 0.90 },
					{ 1.00, 0.97, 0.92, 0.87 },
					{ 1.00, 0.97, 0.92, 0.87 }
			},
			{
					{ 1.00, 0.98, 0.93, 0.88 },
					{ 1.00, 0.98, 0.93, 0.88 },
					{ 1.00, 0.97, 0.92, 0.87 }
			},
			{
					{ 1.00, 1.00, 1.00, 1.00 },
					{ 1.00, 0.98, 0.94, 0.90 },
					{ 1.00, 0.97, 0.92, 0.87 }
			}
	};

	/*
	 * Linear-theory wing-incidence factors digitized from chart 3.  The first
	 * index selects beta*A=2, 3, or 4.  The report's beta*A=infinity curve is
	 * unity, and values above four are interpolated in reciprocal beta*A.
	 */
	private static final double[] CHART_3_RADIUS_SEMISPAN_RATIO = {
			0.0, 0.05, 0.10, 0.15, 0.20, 0.30, 0.40, 0.60, 0.80, 1.0
	};
	private static final double[] CHART_3_BETA_ASPECT_RATIO = { 2.0, 3.0, 4.0 };
	private static final double[][] CHART_3_INCIDENCE_FACTOR = {
			{ 1.000, 0.944, 0.875, 0.852, 0.865, 0.905, 0.925, 0.960, 0.985, 1.000 },
			{ 1.000, 0.945, 0.915, 0.920, 0.940, 0.960, 0.973, 0.990, 0.998, 1.000 },
			{ 1.000, 0.960, 0.944, 0.950, 0.965, 0.978, 0.986, 0.996, 1.000, 1.000 }
	};

	/*
	 * Sixteen-point Gauss-Legendre quadrature.  The planar pressure equations
	 * contain square-root behavior at their bounds, so Gaussian nodes are both
	 * faster and more stable than evaluating the endpoints with Simpson's rule.
	 */
	private static final double[] GAUSS_NODES = {
			-0.9894009349916499, -0.9445750230732326, -0.8656312023878318,
			-0.7554044083550030, -0.6178762444026438, -0.4580167776572274,
			-0.2816035507792589, -0.0950125098376374, 0.0950125098376374,
			0.2816035507792589, 0.4580167776572274, 0.6178762444026438,
			0.7554044083550030, 0.8656312023878318, 0.9445750230732326,
			0.9894009349916499
	};
	private static final double[] GAUSS_WEIGHTS = {
			0.0271524594117541, 0.0622535239386479, 0.0951585116824928,
			0.1246289712555339, 0.1495959888165767, 0.1691565193950025,
			0.1826034150449236, 0.1894506104550685, 0.1894506104550685,
			0.1826034150449236, 0.1691565193950025, 0.1495959888165767,
			0.1246289712555339, 0.0951585116824928, 0.0622535239386479,
			0.0271524594117541
	};

	/*
	 * Normalized fairings digitized from the dotted low-aspect-ratio curves of
	 * NACA Report 1307, chart 16.  The first index selects no leading-edge,
	 * midchord, or trailing-edge sweep; the second selects taper ratio 0, 1/2,
	 * or 1; and the last follows CHART_16_BETA_ASPECT_RATIO.  Each ordinate is
	 * the fraction between the exact slender-body and Appendix-D endpoints.
	 *
	 * Chart 16(g), the triangular no-trailing-edge-sweep case, approaches its
	 * lifting-line endpoint unusually slowly.  Keeping the chart's full beta*A
	 * range is therefore important; clamping every family at beta*A=4 would
	 * move this body's center of pressure too far forward.
	 */
	private static final double[] CHART_16_BETA_ASPECT_RATIO = {
			0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0,
			4.5, 5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0
	};
	private static final double[][][] CHART_16_LIFTING_LINE_WEIGHT = {
			{
					{ 0.0, 0.48, 0.68, 0.80, 0.88, 0.93, 0.96, 0.98, 1.0,
							1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 },
					{ 0.0, 0.44, 0.67, 0.80, 0.88, 0.93, 0.96, 0.98, 1.0,
							1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 },
					{ 0.0, 0.45, 0.66, 0.79, 0.86, 0.91, 0.94, 0.97, 1.0,
							1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 }
			},
			{
					{ 0.0, 0.35, 0.57, 0.71, 0.82, 0.90, 0.95, 0.99, 1.0,
							1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 },
					{ 0.0, 0.34, 0.54, 0.70, 0.82, 0.90, 0.94, 0.97, 1.0,
							1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 },
					{ 0.0, 0.45, 0.66, 0.79, 0.86, 0.91, 0.94, 0.97, 1.0,
							1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 }
			},
			{
					{ 0.0, 0.15, 0.29, 0.41, 0.53, 0.64, 0.73, 0.80, 0.86,
							0.90, 0.94, 0.96, 0.98, 0.99, 1.0, 1.0, 1.0 },
					{ 0.0, 0.30, 0.50, 0.65, 0.76, 0.84, 0.90, 0.95, 1.0,
							1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 },
					{ 0.0, 0.45, 0.66, 0.79, 0.86, 0.91, 0.94, 0.97, 1.0,
							1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 }
			}
	};
	private static final double[][][] CHART_16_RADIUS_BETA_SCALE = {
			{
					{ 1.00, 1.05, 1.10, 1.15 },
					{ 1.00, 1.02, 1.04, 1.06 },
					{ 1.00, 1.00, 1.00, 1.00 }
			},
			{
					{ 1.00, 1.15, 1.10, 1.05 },
					{ 1.00, 1.08, 1.05, 1.02 },
					{ 1.00, 1.00, 1.00, 1.00 }
			},
			{
					{ 1.00, 1.75, 1.75, 1.75 },
					{ 1.00, 1.12, 1.08, 1.05 },
					{ 1.00, 1.00, 1.00, 1.00 }
			}
	};

	private final double bodyRadius;
	private final double exposedSpan;
	private final double rootChord;
	private final double tipChord;
	private final double leadingEdgeSweep;
	private final double aspectRatio;
	private final double bodyEnd;
	private volatile CachedResult cachedResult;
	private volatile PressureIntegral recentPressureIntegral;
	private volatile PressureIntegral previousPressureIntegral;

	/**
	 * Create a model for one trapezoidal fin pair.
	 *
	 * @param bodyRadius body radius at the fin root
	 * @param exposedSpan span from the body surface to the fin tip
	 * @param rootChord exposed root chord
	 * @param tipChord exposed tip chord
	 * @param leadingEdgeSweep axial tip-leading-edge offset from the root leading edge
	 * @param aspectRatio effective aspect ratio of the two joined exposed panels
	 * @param bodyEnd axial end of the cylindrical body, measured from the fin-root leading edge
	 */
	NACA1307FinBodyInterference(double bodyRadius, double exposedSpan, double rootChord,
			double tipChord, double leadingEdgeSweep, double aspectRatio, double bodyEnd) {
		this.bodyRadius = bodyRadius;
		this.exposedSpan = exposedSpan;
		this.rootChord = rootChord;
		this.tipChord = tipChord;
		this.leadingEdgeSweep = leadingEdgeSweep;
		this.aspectRatio = aspectRatio;
		this.bodyEnd = bodyEnd;
	}

	/**
	 * Test whether the report's trapezoidal, constant-radius model covers this geometry.
	 *
	 * <p>Charts 15 and 16 end at {@code r/s=0.6}.  Their planform families interpolate
	 * between unswept leading, midchord, and trailing edges, which excludes a
	 * forward-swept leading edge or an aft-swept trailing edge.  Chart 15(a)
	 * deliberately omits its low-aspect-ratio extrapolation, so geometries that
	 * would require a contribution from that panel are also excluded.</p>
	 *
	 * @return {@code true} when the complete model can be evaluated
	 */
	boolean isApplicable() {
		if (!(bodyRadius > MathUtil.EPSILON) || !(exposedSpan > MathUtil.EPSILON)
				|| !(rootChord > MathUtil.EPSILON) || tipChord < 0 || tipChord > rootChord
				|| !(aspectRatio > MathUtil.EPSILON) || Double.isNaN(bodyEnd)
				|| bodyEnd + EDGE_EPSILON < rootChord) {
			return false;
		}

		double radiusSemispanRatio = getRadiusSemispanRatio();
		double trailingEdgeSweep = leadingEdgeSweep + tipChord - rootChord;
		double taperRatio = tipChord / rootChord;
		double leadingEdgeFraction = getLeadingEdgeSweepFraction();
		boolean requiresMissingChartFifteenPanel = taperRatio < 0.5 - EDGE_EPSILON
				&& leadingEdgeFraction < 0.5 - EDGE_EPSILON;
		return radiusSemispanRatio <= MAX_CHART_RADIUS_SEMISPAN_RATIO + EDGE_EPSILON
				&& leadingEdgeSweep >= -EDGE_EPSILON && trailingEdgeSweep <= EDGE_EPSILON
				&& !requiresMissingChartFifteenPanel;
	}

	/**
	 * Calculate the separate loads and the body-load center of pressure.
	 *
	 * @param mach free-stream Mach number
	 * @param wingLiftCurveSlope lift-curve slope of the joined exposed panels,
	 *                           based on their own area, per radian
	 * @return separate NACA interference factors and body CP
	 */
	Result calculate(double mach, double wingLiftCurveSlope) {
		CachedResult cached = cachedResult;
		if (cached != null && Double.doubleToLongBits(cached.mach()) == Double.doubleToLongBits(mach)
				&& Double.doubleToLongBits(cached.wingLiftCurveSlope())
				== Double.doubleToLongBits(wingLiftCurveSlope)) {
			return cached.result();
		}

		double radiusSemispanRatio = getRadiusSemispanRatio();
		double finFactor = calculateFinInBodyFactor(radiusSemispanRatio);
		double incidenceFactor = calculateWingIncidenceFactor(
				radiusSemispanRatio, mach, aspectRatio, isRectangularPlanform());
		BodyResult bodyResult;

		if (mach <= SUBSONIC_MACH) {
			bodyResult = calculateSubsonicBodyResult(mach, finFactor);
		} else if (mach >= SUPERSONIC_MACH) {
			bodyResult = calculateSupersonicBodyResult(mach, wingLiftCurveSlope, finFactor);
		} else {
			BodyResult subsonic = calculateSubsonicBodyResult(SUBSONIC_MACH, finFactor);
			BodyResult supersonic = calculateSupersonicBodyResult(SUPERSONIC_MACH,
					wingLiftCurveSlope, finFactor);
			double fraction = (mach - SUBSONIC_MACH) / (SUPERSONIC_MACH - SUBSONIC_MACH);
			bodyResult = interpolateBodyResult(subsonic, supersonic, fraction);
		}

		Result result = new Result(finFactor, bodyResult.factor(), bodyResult.cp(), incidenceFactor);
		/* Each radial instance normally requests the same result consecutively. */
		cachedResult = new CachedResult(mach, wingLiftCurveSlope, result);
		return result;
	}

	/**
	 * Equation 14 of NACA Report 1307, evaluated with {@code tau=r/s}.
	 *
	 * @param tau body-radius/total-semispan ratio
	 * @return fin lift in the presence of the body, divided by isolated-fin lift
	 */
	static double calculateFinInBodyFactor(double tau) {
		/*
		 * The printed-page-47 selection guide points one rectangular supersonic
		 * case to linear theory (chart 2), but the report's discussion on printed
		 * page 5 recommends this slender-body value for every combination because
		 * linear theory omits the observed loss of wing lift near the body.
		 */
		if (!(tau > EDGE_EPSILON)) {
			return 1.0;
		}
		if (tau >= 1.0) {
			return 2.0;
		}

		double inverseTau = 1.0 / tau;
		double angle = 0.5 * Math.atan(0.5 * (inverseTau - tau)) + Math.PI / 4.0;
		double braces = (1.0 + Math.pow(tau, 4)) * angle
				- MathUtil.pow2(tau) * (inverseTau - tau + 2.0 * Math.atan(tau));
		return (2.0 / Math.PI) * braces / MathUtil.pow2(1.0 - tau);
	}

	/**
	 * Equation 21, evaluated through its exact identity with equation 14.
	 *
	 * @param tau body-radius/total-semispan ratio
	 * @param finFactor equation-14 fin factor
	 * @return slender-body load carried by the body due to the fin
	 */
	static double calculateSlenderBodyInFinFactor(double tau, double finFactor) {
		return Math.max(0.0, MathUtil.pow2(1.0 + tau) - finFactor);
	}

	/**
	 * Equation 19 of NACA Report 1307 for wing incidence on a zero-angle body.
	 *
	 * <p>The report writes the equation in terms of semispan/radius.  The public
	 * model uses the reciprocal radius/semispan ratio to stay consistent with
	 * equations 14 and 21.  Equation 19 is strictly the report's slender-body
	 * result.  It is used as the common roll approximation for unsupported fin
	 * planforms because it depends only on radius/semispan and avoids an
	 * artificial planform-dependent jump.</p>
	 *
	 * @param radiusSemispanRatio body radius divided by total wing semispan
	 * @return fin lift in the presence of the body for wing incidence
	 */
	static double calculateWingIncidenceFactor(double radiusSemispanRatio) {
		if (!(radiusSemispanRatio > EDGE_EPSILON)) {
			return 1.0;
		}
		if (radiusSemispanRatio >= 1.0) {
			return 1.0;
		}

		double tau = 1.0 / radiusSemispanRatio;
		double tauSquared = MathUtil.pow2(tau);
		double tauMinusOne = tau - 1.0;
		double angle = Math.asin(MathUtil.clamp(
				(tauSquared - 1.0) / (tauSquared + 1.0), -1.0, 1.0));
		double common = MathUtil.pow2(tauSquared + 1.0)
				/ (tauSquared * MathUtil.pow2(tauMinusOne));
		double braces = MathUtil.pow2(Math.PI) * MathUtil.pow2(tau + 1.0)
				/ (4.0 * tauSquared)
				+ Math.PI * common * angle
				- 2.0 * Math.PI * (tau + 1.0) / (tau * tauMinusOne)
				+ common * MathUtil.pow2(angle)
				- 4.0 * (tau + 1.0) * angle / (tau * tauMinusOne)
				+ 8.0 / MathUtil.pow2(tauMinusOne)
						* Math.log((tauSquared + 1.0) / (2.0 * tau));
		return braces / MathUtil.pow2(Math.PI);
	}

	/**
	 * Select the report's wing-incidence approximation for cant-driven roll.
	 *
	 * <p>The selection guide on printed page 47 directs rectangular wings with
	 * {@code beta*A > 2} to chart 3.  Other shapes and regimes retain equation
	 * 19.  Chart 3 includes radius ratios through one, so this selection is also
	 * usable when the complete normal-force/CP model is outside its narrower
	 * chart-15/16 applicability limits.</p>
	 *
	 * @param radiusSemispanRatio body radius divided by total wing semispan
	 * @param mach free-stream Mach number
	 * @param aspectRatio effective aspect ratio of the joined exposed panels
	 * @param rectangularPlanform whether the exposed planform is rectangular
	 * @return selected lowercase wing-incidence interference factor
	 */
	static double calculateWingIncidenceFactor(double radiusSemispanRatio, double mach,
			double aspectRatio, boolean rectangularPlanform) {
		double slenderBodyFactor = calculateWingIncidenceFactor(radiusSemispanRatio);
		if (!rectangularPlanform || !(mach > 1.0) || !(aspectRatio > MathUtil.EPSILON)) {
			return slenderBodyFactor;
		}

		double beta = Math.sqrt(MathUtil.pow2(mach) - 1.0);
		double betaAspectRatio = beta * aspectRatio;
		if (!(betaAspectRatio > CHART_3_MINIMUM_BETA_ASPECT_RATIO)) {
			return slenderBodyFactor;
		}

		double chartFactor = interpolateChartThree(betaAspectRatio, radiusSemispanRatio);
		if (betaAspectRatio < CHART_3_MINIMUM_BETA_ASPECT_RATIO + CHART_3_TRANSITION_WIDTH) {
			double fraction = (betaAspectRatio - CHART_3_MINIMUM_BETA_ASPECT_RATIO)
					/ CHART_3_TRANSITION_WIDTH;
			return interpolate(slenderBodyFactor, chartFactor,
					smoothStep(MathUtil.clamp(fraction, 0.0, 1.0)));
		}
		return chartFactor;
	}

	/**
	 * Interpolate the chart-3 linear-theory families for a rectangular wing.
	 */
	private static double interpolateChartThree(double betaAspectRatio,
			double radiusSemispanRatio) {
		double radius = MathUtil.clamp(radiusSemispanRatio, 0.0, 1.0);
		double betaTwo = interpolateCurve(CHART_3_RADIUS_SEMISPAN_RATIO,
				CHART_3_INCIDENCE_FACTOR[0], radius);
		double betaThree = interpolateCurve(CHART_3_RADIUS_SEMISPAN_RATIO,
				CHART_3_INCIDENCE_FACTOR[1], radius);
		double betaFour = interpolateCurve(CHART_3_RADIUS_SEMISPAN_RATIO,
				CHART_3_INCIDENCE_FACTOR[2], radius);
		if (betaAspectRatio <= CHART_3_BETA_ASPECT_RATIO[1]) {
			return interpolate(betaTwo, betaThree,
					(betaAspectRatio - CHART_3_BETA_ASPECT_RATIO[0])
							/ (CHART_3_BETA_ASPECT_RATIO[1] - CHART_3_BETA_ASPECT_RATIO[0]));
		}
		if (betaAspectRatio <= CHART_3_BETA_ASPECT_RATIO[2]) {
			return interpolate(betaThree, betaFour,
					(betaAspectRatio - CHART_3_BETA_ASPECT_RATIO[1])
							/ (CHART_3_BETA_ASPECT_RATIO[2] - CHART_3_BETA_ASPECT_RATIO[1]));
		}

		double reciprocalFraction = 1.0 - CHART_3_BETA_ASPECT_RATIO[2] / betaAspectRatio;
		return interpolate(betaFour, 1.0, MathUtil.clamp(reciprocalFraction, 0.0, 1.0));
	}

	private BodyResult calculateSubsonicBodyResult(double mach, double finFactor) {
		double factor = calculateSlenderBodyInFinFactor(getRadiusSemispanRatio(), finFactor);
		double beta = Math.sqrt(Math.max(0.0, 1.0 - MathUtil.pow2(mach)));
		double chartWeight = interpolateChart16(beta * aspectRatio,
				getRadiusSemispanRatio());
		double slenderCp = calculateSlenderBodyCp();
		double liftingLineCp = calculateLiftingLineBodyCp();
		double cp = interpolate(slenderCp, liftingLineCp, chartWeight);
		return new BodyResult(factor, cp);
	}

	private BodyResult calculateSupersonicBodyResult(double mach, double wingLiftCurveSlope,
			double finFactor) {
		double slenderFactor = calculateSlenderBodyInFinFactor(getRadiusSemispanRatio(), finFactor);
		BodyResult slenderResult = new BodyResult(slenderFactor, calculateSlenderBodyCp());
		if (!(mach > 1.0) || !(wingLiftCurveSlope > MathUtil.EPSILON)) {
			return slenderResult;
		}

		double beta = Math.sqrt(MathUtil.pow2(mach) - 1.0);
		double criterion = calculatePlanarCriterion(beta);
		BodyResult lowAspectResult = calculateLowAspectSupersonicBodyResult(
				beta, wingLiftCurveSlope, slenderFactor);
		if (criterion <= PLANAR_CRITERION) {
			return lowAspectResult;
		}

		BodyResult planarResult = integratePlanarBodyPressure(beta, wingLiftCurveSlope);
		if (planarResult == null || !Double.isFinite(planarResult.factor())
				|| !Double.isFinite(planarResult.cp()) || planarResult.factor() < 0) {
			return lowAspectResult;
		}

		/*
		 * The report changes approximations at criterion=4.  Their values are
		 * close but not algebraically identical, so fair force and moment over a
		 * narrow interval to prevent a numerical step in CNa or CP.
		 */
		if (criterion < PLANAR_CRITERION + PLANAR_TRANSITION_WIDTH) {
			double fraction = (criterion - PLANAR_CRITERION) / PLANAR_TRANSITION_WIDTH;
			return interpolateBodyResult(lowAspectResult, planarResult,
					smoothStep(MathUtil.clamp(fraction, 0.0, 1.0)));
		}
		return planarResult;
	}

	/**
	 * Evaluate the left side of the report's equation-22 selection rule.
	 */
	private double calculatePlanarCriterion(double beta) {
		double taperRatio = tipChord / rootChord;
		return (1.0 + taperRatio) * aspectRatio
				* (beta + leadingEdgeSweep / exposedSpan);
	}

	/**
	 * Chart 15 low-aspect-ratio center of pressure, with the published fairing
	 * anchored to the pressure solution at the equation-22 boundary.
	 */
	private BodyResult calculateLowAspectSupersonicBodyResult(double beta,
			double wingLiftCurveSlope, double slenderFactor) {
		double boundaryBeta = calculatePlanarBoundaryBeta();
		if (!(boundaryBeta > MathUtil.EPSILON)) {
			BodyResult planar = integratePlanarBodyPressure(beta, wingLiftCurveSlope);
			return planar != null ? planar
					: new BodyResult(slenderFactor, calculateSlenderBodyCp());
		}

		BodyResult boundary = integratePlanarBodyPressure(boundaryBeta, wingLiftCurveSlope);
		if (boundary == null || !Double.isFinite(boundary.cp())) {
			return new BodyResult(slenderFactor, calculateSlenderBodyCp());
		}

		double normalizedCriterion = MathUtil.clamp(beta / boundaryBeta, 0.0, 1.0);
		double chartWeight = interpolateChart15(normalizedCriterion,
				getRadiusSemispanRatio());
		double cp = interpolate(calculateSlenderBodyCp(), boundary.cp(), chartWeight);
		return new BodyResult(slenderFactor, cp);
	}

	/**
	 * Mach parameter at which equation 22 is exactly equal to four.
	 */
	private double calculatePlanarBoundaryBeta() {
		double taperRatio = tipChord / rootChord;
		double sweepContribution = aspectRatio * leadingEdgeSweep / exposedSpan;
		double betaAspectRatio = PLANAR_CRITERION / (1.0 + taperRatio)
				- sweepContribution;
		return betaAspectRatio / aspectRatio;
	}

	/**
	 * Numerically integrate equations 23/25 and 66/69 over the body.
	 *
	 * <p>The upper streamwise bound is clipped at the actual body end.  This is
	 * the finite-afterbody form described below equation 70 and is also valid for
	 * the report's limiting with-afterbody and without-afterbody cases.</p>
	 */
	private BodyResult integratePlanarBodyPressure(double beta, double wingLiftCurveSlope) {
		PressureIntegral integral = getPlanarPressureIntegral(beta);
		if (!(integral.lift() > MathUtil.EPSILON)) {
			return new BodyResult(0.0, 0.0);
		}

		double exposedWingArea = exposedSpan * (rootChord + tipChord);
		double betaM = integral.betaM();
		double factor;
		if (betaM > 1.0 || Double.isInfinite(betaM)) {
			double edgeFactor = Double.isInfinite(betaM)
					? 1.0 : betaM / Math.sqrt(MathUtil.pow2(betaM) - 1.0);
			factor = 8.0 * edgeFactor * integral.lift()
					/ (Math.PI * beta * exposedWingArea * wingLiftCurveSlope);
		} else {
			factor = 16.0 * Math.pow(betaM, 1.5) * integral.lift()
					/ (Math.PI * beta * (betaM + 1.0) * exposedWingArea * wingLiftCurveSlope);
		}
		return new BodyResult(factor, integral.moment() / integral.lift());
	}

	/**
	 * Return the pressure integrals for a Mach parameter.  The final supersonic
	 * factor is inversely proportional to the caller's wing lift-curve slope, but
	 * the expensive pressure field is not.  Keeping the two most recent beta
	 * values covers the equation-22 boundary and current-Mach integrations while
	 * allowing the caller's nonlinear slope to change with angle of attack.
	 */
	private PressureIntegral getPlanarPressureIntegral(double beta) {
		PressureIntegral recent = recentPressureIntegral;
		if (matchesBeta(recent, beta)) {
			return recent;
		}

		PressureIntegral previous = previousPressureIntegral;
		if (matchesBeta(previous, beta)) {
			return previous;
		}

		PressureIntegral calculated = calculatePlanarPressureIntegral(beta);
		previousPressureIntegral = recent;
		recentPressureIntegral = calculated;
		return calculated;
	}

	private static boolean matchesBeta(PressureIntegral integral, double beta) {
		return integral != null
				&& Double.doubleToLongBits(integral.beta()) == Double.doubleToLongBits(beta);
	}

	private PressureIntegral calculatePlanarPressureIntegral(double beta) {
		double diameter = 2.0 * bodyRadius;
		double etaEnd = Math.min(diameter, bodyEnd / beta);
		if (!(etaEnd > MathUtil.EPSILON)) {
			return new PressureIntegral(beta, 0.0, 0.0, Double.NaN);
		}

		double m = leadingEdgeSweep <= EDGE_EPSILON
				? Double.POSITIVE_INFINITY : exposedSpan / leadingEdgeSweep;
		double betaM = beta * m;
		if (Double.isFinite(betaM) && Math.abs(betaM - 1.0) < BETA_M_LIMIT_EPSILON) {
			/* Equation 25 is the finite common limit of the two edge cases. */
			betaM = 1.0;
			m = 1.0 / beta;
		}

		double liftIntegral = 0.0;
		double momentIntegral = 0.0;
		double etaScale = etaEnd / 2.0;
		for (int i = 0; i < GAUSS_NODES.length; i++) {
			double eta = etaScale * (GAUSS_NODES[i] + 1.0);
			double lower = beta * eta;
			double upper = Math.min(rootChord + lower, bodyEnd);
			if (!(upper > lower)) {
				continue;
			}

			double xiScale = (upper - lower) / 2.0;
			double xiCenter = (upper + lower) / 2.0;
			double stripLift = 0.0;
			double stripMoment = 0.0;
			for (int j = 0; j < GAUSS_NODES.length; j++) {
				double xi = xiCenter + xiScale * GAUSS_NODES[j];
				double pressure = calculatePlanarPressureKernel(beta, m, betaM, eta, xi);
				double weight = GAUSS_WEIGHTS[j];
				stripLift += weight * pressure;
				stripMoment += weight * xi * pressure;
			}
			liftIntegral += GAUSS_WEIGHTS[i] * xiScale * stripLift;
			momentIntegral += GAUSS_WEIGHTS[i] * xiScale * stripMoment;
		}
		liftIntegral *= etaScale;
		momentIntegral *= etaScale;
		return new PressureIntegral(beta, liftIntegral, momentIntegral, betaM);
	}

	private static double calculatePlanarPressureKernel(double beta, double m, double betaM,
			double eta, double xi) {
		if (Double.isInfinite(betaM)) {
			return Math.acos(MathUtil.clamp(beta * eta / xi, -1.0, 1.0));
		}
		if (betaM > 1.0) {
			double argument = (xi / beta + betaM * eta) / (eta + m * xi);
			return Math.acos(MathUtil.clamp(argument, -1.0, 1.0));
		}

		double argument = (xi / beta - eta) / (eta + m * xi);
		return Math.sqrt(Math.max(0.0, argument));
	}

	/**
	 * Appendix D, equation D5, evaluated for the trapezoid's quarter-chord line.
	 */
	private double calculateLiftingLineBodyCp() {
		double weightedOffset = 0.0;
		double totalWeight = 0.0;
		double scale = exposedSpan / 2.0;
		for (int i = 0; i < GAUSS_NODES.length; i++) {
			double offset = scale * (GAUSS_NODES[i] + 1.0);
			double eta = bodyRadius + offset;
			double circulation = Math.sqrt(Math.max(0.0,
					1.0 - MathUtil.pow2(offset / exposedSpan)));
			double weight = GAUSS_WEIGHTS[i] * circulation / MathUtil.pow2(eta);
			totalWeight += weight;
			weightedOffset += weight * offset;
		}
		if (!(totalWeight > MathUtil.EPSILON)) {
			return rootChord / 4.0;
		}

		double quarterChordSweep = leadingEdgeSweep + tipChord / 4.0 - rootChord / 4.0;
		return rootChord / 4.0
				+ (weightedOffset / totalWeight) * quarterChordSweep / exposedSpan;
	}

	/**
	 * Slender-body endpoint used by charts 15 and 16 at {@code beta*A=0}.
	 */
	private double calculateSlenderBodyCp() {
		return leadingEdgeSweep / 2.0;
	}

	private double getRadiusSemispanRatio() {
		return bodyRadius / (bodyRadius + exposedSpan);
	}

	private boolean isRectangularPlanform() {
		return MathUtil.equals(rootChord, tipChord);
	}

	private static BodyResult interpolateBodyResult(BodyResult start, BodyResult end,
			double fraction) {
		double factor = interpolate(start.factor(), end.factor(), fraction);
		double momentFactor = interpolate(start.factor() * start.cp(), end.factor() * end.cp(), fraction);
		double cp = factor > MathUtil.EPSILON ? momentFactor / factor : 0.0;
		return new BodyResult(factor, cp);
	}

	/**
	 * Interpolate chart 15 in radius ratio, taper, and sweep family.
	 */
	double interpolateChart15(double normalizedCriterion,
			double radiusSemispanRatio) {
		double taperRatio = tipChord / rootChord;
		double leadingEdgeFraction = getLeadingEdgeSweepFraction();

		double noLeadingSweep = interpolateChart15TaperFamily(
				0, taperRatio, normalizedCriterion, radiusSemispanRatio);
		double noMidchordSweep = interpolateChart15TaperFamily(
				1, taperRatio, normalizedCriterion, radiusSemispanRatio);
		double noTrailingSweep = interpolateChart15TaperFamily(
				2, taperRatio, normalizedCriterion, radiusSemispanRatio);
		if (leadingEdgeFraction <= 0.5) {
			return interpolate(noLeadingSweep, noMidchordSweep, 2.0 * leadingEdgeFraction);
		}
		return interpolate(noMidchordSweep, noTrailingSweep,
				2.0 * leadingEdgeFraction - 1.0);
	}

	private static double interpolateChart15TaperFamily(int sweepFamily, double taperRatio,
			double normalizedCriterion, double radiusSemispanRatio) {
		double zeroTaper = interpolateChart15RadiusFamily(sweepFamily, 0,
				normalizedCriterion, radiusSemispanRatio);
		double halfTaper = interpolateChart15RadiusFamily(sweepFamily, 1,
				normalizedCriterion, radiusSemispanRatio);
		double fullTaper = interpolateChart15RadiusFamily(sweepFamily, 2,
				normalizedCriterion, radiusSemispanRatio);
		if (taperRatio <= 0.5) {
			return interpolate(zeroTaper, halfTaper, 2.0 * taperRatio);
		}
		return interpolate(halfTaper, fullTaper, 2.0 * taperRatio - 1.0);
	}

	private static double interpolateChart15RadiusFamily(int sweepFamily, int taperFamily,
			double normalizedCriterion, double radiusSemispanRatio) {
		double radiusZero = interpolateChart15RadiusCurve(
				sweepFamily, taperFamily, 0, normalizedCriterion);
		double radiusPointTwo = interpolateChart15RadiusCurve(
				sweepFamily, taperFamily, 1, normalizedCriterion);
		double radiusPointFour = interpolateChart15RadiusCurve(
				sweepFamily, taperFamily, 2, normalizedCriterion);
		double radiusPointSix = interpolateChart15RadiusCurve(
				sweepFamily, taperFamily, 3, normalizedCriterion);
		return interpolateRadiusFamily(radiusZero, radiusPointTwo,
				radiusPointFour, radiusPointSix, radiusSemispanRatio);
	}

	private static double interpolateChart15RadiusCurve(int sweepFamily, int taperFamily,
			int radiusFamily, double normalizedCriterion) {
		if (sweepFamily == 0 && taperFamily == 2) {
			return interpolateCurve(CHART_15_RECTANGULAR_NORMALIZED_CRITERION,
					CHART_15_RECTANGULAR_PLANAR_WEIGHT[radiusFamily], normalizedCriterion);
		}
		double scaledCriterion = MathUtil.clamp(normalizedCriterion
				* CHART_15_RADIUS_PROGRESS_SCALE[sweepFamily][taperFamily][radiusFamily],
				0.0, 1.0);
		return interpolateCurve(CHART_15_GUIDE_NORMALIZED_CRITERION,
				CHART_15_GUIDE_PLANAR_WEIGHT, scaledCriterion);
	}

	/**
	 * Interpolate the nine chart-16 planform families, including their four
	 * published radius/semispan curves.
	 */
	private double interpolateChart16(double betaAspectRatio, double radiusSemispanRatio) {
		double taperRatio = tipChord / rootChord;
		double leadingEdgeFraction = getLeadingEdgeSweepFraction();

		double noLeadingSweep = interpolateChart16TaperFamily(
				0, taperRatio, betaAspectRatio, radiusSemispanRatio);
		double noMidchordSweep = interpolateChart16TaperFamily(
				1, taperRatio, betaAspectRatio, radiusSemispanRatio);
		double noTrailingSweep = interpolateChart16TaperFamily(
				2, taperRatio, betaAspectRatio, radiusSemispanRatio);
		if (leadingEdgeFraction <= 0.5) {
			return interpolate(noLeadingSweep, noMidchordSweep, 2.0 * leadingEdgeFraction);
		}
		return interpolate(noMidchordSweep, noTrailingSweep,
				2.0 * leadingEdgeFraction - 1.0);
	}

	private double getLeadingEdgeSweepFraction() {
		double maximumSweep = rootChord - tipChord;
		if (maximumSweep <= EDGE_EPSILON) {
			return 0.0;
		}
		return MathUtil.clamp(leadingEdgeSweep / maximumSweep, 0.0, 1.0);
	}

	private static double interpolateChart16TaperFamily(int sweepFamily, double taperRatio,
			double betaAspectRatio, double radiusSemispanRatio) {
		double zeroTaper = interpolateChart16RadiusFamily(
				sweepFamily, 0, betaAspectRatio, radiusSemispanRatio);
		double halfTaper = interpolateChart16RadiusFamily(
				sweepFamily, 1, betaAspectRatio, radiusSemispanRatio);
		double fullTaper = interpolateChart16RadiusFamily(
				sweepFamily, 2, betaAspectRatio, radiusSemispanRatio);
		if (taperRatio <= 0.5) {
			return interpolate(zeroTaper, halfTaper, 2.0 * taperRatio);
		}
		return interpolate(halfTaper, fullTaper, 2.0 * taperRatio - 1.0);
	}

	private static double interpolateChart16RadiusFamily(int sweepFamily, int taperFamily,
			double betaAspectRatio, double radiusSemispanRatio) {
		double[] curve = CHART_16_LIFTING_LINE_WEIGHT[sweepFamily][taperFamily];
		double radiusZero = interpolateChart16RadiusCurve(
				curve, sweepFamily, taperFamily, 0, betaAspectRatio);
		double radiusPointTwo = interpolateChart16RadiusCurve(
				curve, sweepFamily, taperFamily, 1, betaAspectRatio);
		double radiusPointFour = interpolateChart16RadiusCurve(
				curve, sweepFamily, taperFamily, 2, betaAspectRatio);
		double radiusPointSix = interpolateChart16RadiusCurve(
				curve, sweepFamily, taperFamily, 3, betaAspectRatio);
		return interpolateRadiusFamily(radiusZero, radiusPointTwo,
				radiusPointFour, radiusPointSix, radiusSemispanRatio);
	}

	private static double interpolateChart16RadiusCurve(double[] curve, int sweepFamily,
			int taperFamily, int radiusFamily, double betaAspectRatio) {
		double scaledBetaAspectRatio = betaAspectRatio
				* CHART_16_RADIUS_BETA_SCALE[sweepFamily][taperFamily][radiusFamily];
		return interpolateCurve(CHART_16_BETA_ASPECT_RATIO, curve, scaledBetaAspectRatio);
	}

	private static double interpolateRadiusFamily(double radiusZero, double radiusPointTwo,
			double radiusPointFour, double radiusPointSix,
			double radiusSemispanRatio) {
		double radius = MathUtil.clamp(
				radiusSemispanRatio, 0.0, MAX_CHART_RADIUS_SEMISPAN_RATIO);
		if (radius <= CHART_RADIUS_SEMISPAN_RATIO[1]) {
			return interpolate(radiusZero, radiusPointTwo,
					radius / CHART_RADIUS_SEMISPAN_RATIO[1]);
		}
		if (radius <= CHART_RADIUS_SEMISPAN_RATIO[2]) {
			return interpolate(radiusPointTwo, radiusPointFour,
					(radius - CHART_RADIUS_SEMISPAN_RATIO[1])
							/ (CHART_RADIUS_SEMISPAN_RATIO[2] - CHART_RADIUS_SEMISPAN_RATIO[1]));
		}
		return interpolate(radiusPointFour, radiusPointSix,
				(radius - CHART_RADIUS_SEMISPAN_RATIO[2])
						/ (CHART_RADIUS_SEMISPAN_RATIO[3] - CHART_RADIUS_SEMISPAN_RATIO[2]));
	}

	private static double interpolateCurve(double[] abscissae, double[] ordinates,
			double abscissa) {
		if (abscissa <= abscissae[0]) {
			return ordinates[0];
		}
		for (int i = 1; i < abscissae.length; i++) {
			if (abscissa <= abscissae[i]) {
				double fraction = (abscissa - abscissae[i - 1])
						/ (abscissae[i] - abscissae[i - 1]);
				return interpolate(ordinates[i - 1], ordinates[i], fraction);
			}
		}
		return ordinates[ordinates.length - 1];
	}

	private static double interpolate(double start, double end, double fraction) {
		return start + fraction * (end - start);
	}

	private static double smoothStep(double value) {
		return value * value * (3.0 - 2.0 * value);
	}

	/**
	 * Separate NACA loads, with CP measured from the fin-root leading edge and
	 * the lowercase incidence factor kept distinct for cant-driven roll.
	 *
	 * @param finFactor fin lift divided by the caller's isolated-fin lift
	 * @param bodyFactor body carryover divided by isolated-fin lift; in the planar
	 *                   supersonic regime this contains the inverse caller slope
	 *                   and is meaningful only when multiplied back by isolated CNa
	 * @param bodyCp center of pressure of the body carryover load
	 * @param incidenceFactor selected equation-19 or chart-3 fin-incidence factor
	 */
	record Result(double finFactor, double bodyFactor, double bodyCp, double incidenceFactor) {
	}

	/** Body-only load and moment result used while selecting and blending regimes. */
	private record BodyResult(double factor, double cp) {
	}

	/** Slope-independent pressure integrals cached for the planar supersonic model. */
	private record PressureIntegral(double beta, double lift, double moment, double betaM) {
	}

	/** Last immutable result, shared safely by consecutive radial-fin calls. */
	private record CachedResult(double mach, double wingLiftCurveSlope, Result result) {
	}
}
