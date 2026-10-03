package info.openrocket.swing.gui.figure3d.animation;

/** Linear interpolation over time-sampled flight data, held at the first and last samples. */
public final class TimeSeries {
	private TimeSeries() {
	}

	/**
	 * Returns the value at the given time, interpolated linearly between the samples around it
	 * and held constant before the first and after the last sample.
	 *
	 * @param times  sample times, ascending
	 * @param values sample values, parallel to {@code times}
	 * @param time   the time to evaluate
	 * @return the interpolated value, or 0 without samples
	 */
	public static double interpolate(double[] times, double[] values, double time) {
		int count = Math.min(times.length, values.length);
		if (count == 0) {
			return 0.0;
		}
		int upper = firstSampleAfter(times, count, time);
		if (upper == 0) {
			return values[0];
		}
		if (upper >= count) {
			return values[count - 1];
		}
		int lower = upper - 1;
		double span = times[upper] - times[lower];
		double fraction = span > 0.0 ? (time - times[lower]) / span : 0.0;
		return values[lower] + (values[upper] - values[lower]) * fraction;
	}

	/** Index of the first of the first {@code count} samples strictly after the time. */
	private static int firstSampleAfter(double[] times, int count, double time) {
		int low = 0;
		int high = count;
		while (low < high) {
			int middle = (low + high) >>> 1;
			if (times[middle] <= time) {
				low = middle + 1;
			} else {
				high = middle;
			}
		}
		return low;
	}
}
