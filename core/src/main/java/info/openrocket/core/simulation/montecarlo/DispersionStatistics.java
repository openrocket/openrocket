package info.openrocket.core.simulation.montecarlo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Descriptive landing-dispersion statistics in the horizontal plane.
 * <p>
 * Containment radii are empirical nearest-rank quantiles about the sample mean.
 * Ellipses are covariance ellipses and therefore describe the data rather than
 * guaranteeing a particular coverage when the landing cloud is non-Gaussian.
 */
public final class DispersionStatistics {
	private final int sampleCount;
	private final double meanEast;
	private final double meanNorth;
	private final double covarianceEastEast;
	private final double covarianceNorthNorth;
	private final double covarianceEastNorth;
	private final double majorVariance;
	private final double minorVariance;
	private final double majorAxisAngle;
	private final List<Double> distancesFromMean;

	private DispersionStatistics(List<LandingPoint> points) {
		if (points.isEmpty()) {
			throw new IllegalArgumentException("At least one landing point is required");
		}

		this.sampleCount = points.size();
		double eastTotal = 0;
		double northTotal = 0;
		for (LandingPoint point : points) {
			eastTotal += point.east();
			northTotal += point.north();
		}
		this.meanEast = eastTotal / sampleCount;
		this.meanNorth = northTotal / sampleCount;

		double eastEast = 0;
		double northNorth = 0;
		double eastNorth = 0;
		List<Double> radii = new ArrayList<>(sampleCount);
		for (LandingPoint point : points) {
			double eastDelta = point.east() - meanEast;
			double northDelta = point.north() - meanNorth;
			eastEast += eastDelta * eastDelta;
			northNorth += northDelta * northDelta;
			eastNorth += eastDelta * northDelta;
			radii.add(Math.hypot(eastDelta, northDelta));
		}

		double denominator = sampleCount > 1 ? sampleCount - 1.0 : 1.0;
		this.covarianceEastEast = eastEast / denominator;
		this.covarianceNorthNorth = northNorth / denominator;
		this.covarianceEastNorth = eastNorth / denominator;

		double trace = covarianceEastEast + covarianceNorthNorth;
		double discriminant = Math.hypot(covarianceEastEast - covarianceNorthNorth,
				2 * covarianceEastNorth);
		this.majorVariance = Math.max(0, (trace + discriminant) / 2);
		this.minorVariance = Math.max(0, (trace - discriminant) / 2);
		this.majorAxisAngle = 0.5 * Math.atan2(2 * covarianceEastNorth,
				covarianceEastEast - covarianceNorthNorth);
		radii.sort(Comparator.naturalOrder());
		this.distancesFromMean = List.copyOf(radii);
	}

	public static DispersionStatistics from(List<LandingPoint> points) {
		return new DispersionStatistics(points);
	}

	public int getSampleCount() {
		return sampleCount;
	}

	public double getMeanEast() {
		return meanEast;
	}

	public double getMeanNorth() {
		return meanNorth;
	}

	public double getMeanRangeFromPad() {
		return Math.hypot(meanEast, meanNorth);
	}

	/**
	 * Bearing of the sample mean, clockwise from north in radians.
	 */
	public double getMeanBearing() {
		return normalizeBearing(Math.atan2(meanEast, meanNorth));
	}

	public double getCovarianceEastEast() {
		return covarianceEastEast;
	}

	public double getCovarianceNorthNorth() {
		return covarianceNorthNorth;
	}

	public double getCovarianceEastNorth() {
		return covarianceEastNorth;
	}

	/**
	 * Empirical nearest-rank containment radius about the sample mean.
	 *
	 * @param probability desired fraction in the range (0, 1]
	 */
	public double getContainmentRadius(double probability) {
		if (!(probability > 0) || probability > 1) {
			throw new IllegalArgumentException("Probability must be in the range (0, 1]");
		}
		int index = Math.max(0, (int) Math.ceil(probability * sampleCount) - 1);
		return distancesFromMean.get(index);
	}

	/**
	 * Return a covariance ellipse at the requested Mahalanobis sigma level.
	 */
	public DispersionEllipse getEllipse(double sigma) {
		if (!Double.isFinite(sigma) || sigma < 0) {
			throw new IllegalArgumentException("Sigma must be finite and non-negative");
		}
		return new DispersionEllipse(meanEast, meanNorth, sigma * Math.sqrt(majorVariance),
				sigma * Math.sqrt(minorVariance), majorAxisAngle);
	}

	/**
	 * Bearing of the major ellipse axis, clockwise from north in radians.
	 */
	public double getMajorAxisBearing() {
		double eastComponent = Math.cos(majorAxisAngle);
		double northComponent = Math.sin(majorAxisAngle);
		return normalizeBearing(Math.atan2(eastComponent, northComponent));
	}

	private static double normalizeBearing(double bearing) {
		double twoPi = 2 * Math.PI;
		return (bearing % twoPi + twoPi) % twoPi;
	}

	/**
	 * Covariance ellipse in data coordinates.
	 *
	 * @param centerEast ellipse center east displacement
	 * @param centerNorth ellipse center north displacement
	 * @param semiMajor semi-major axis length
	 * @param semiMinor semi-minor axis length
	 * @param angle counter-clockwise angle of the major axis from east
	 */
	public record DispersionEllipse(double centerEast, double centerNorth, double semiMajor,
			double semiMinor, double angle) {
	}
}
