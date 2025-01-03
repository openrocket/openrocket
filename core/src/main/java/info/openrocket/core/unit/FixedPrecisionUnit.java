package info.openrocket.core.unit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class FixedPrecisionUnit extends Unit {

	private final double precision;
	private final String formatString;
	private final boolean displayTrailingZeros;
	private final DecimalFormat formatter;

	public FixedPrecisionUnit(String unit, double precision) {
		this(unit, precision, 1.0);
	}

	public FixedPrecisionUnit(String unit, double precision, double multiplier) {
		this(unit, precision, multiplier, true);
	}

	public FixedPrecisionUnit(String unit, double precision, double multiplier, boolean displayTrailingZeros) {
		super(multiplier, unit);

		this.precision = precision;
		this.displayTrailingZeros = displayTrailingZeros;

		// Calculate number of decimal places needed
		int decimals = 0;
		double p = precision;
		while ((p - Math.floor(p)) > 0.0000001) {
			p *= 10;
			decimals++;
		}

		// Create format string based on whether we want trailing zeros
		this.formatString = "%." + decimals + "f";

		// Initialize DecimalFormat for handling trailing zeros
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		StringBuilder pattern = new StringBuilder("0");
		if (decimals > 0) {
			pattern.append(".");
			if (displayTrailingZeros) {
				pattern.append("0".repeat(decimals));
			} else {
				pattern.append("#".repeat(decimals));
			}
		}
		this.formatter = new DecimalFormat(pattern.toString(), symbols);
	}

	@Override
	public double getNextValue(double value) {
		return round(value + precision);
	}

	@Override
	public double getPreviousValue(double value) {
		return round(value - precision);
	}

	@Override
	public double round(double value) {
		double scale = 1.0 / precision;
		return Math.round(value * scale) / scale;
	}

	@Override
	public String toString(double value) {
		double unitValue = this.toUnit(value);
		if (displayTrailingZeros) {
			return String.format(formatString, unitValue);
		} else {
			return formatter.format(unitValue);
		}
	}

	// TODO: LOW: This is copied from GeneralUnit, perhaps combine
	@Override
	public Tick[] getTicks(double start, double end, double minor, double major) {
		// Convert values
		start = toUnit(start);
		end = toUnit(end);
		minor = toUnit(minor);
		major = toUnit(major);

		if (minor <= 0 || major <= 0 || major < minor) {
			throw new IllegalArgumentException("getTicks called with minor=" + minor + " major=" + major);
		}

		ArrayList<Tick> ticks = new ArrayList<>();

		int mod2, mod3, mod4; // Moduli for minor-notable, major-nonnotable, major-notable
		double minstep;

		// Find the smallest possible step size
		double one = 1;
		while (one > minor)
			one /= 10;
		while (one < minor)
			one *= 10;
		// one is the smallest round-ten that is larger than minor
		if (one / 2 >= minor) {
			// smallest step is round-five
			minstep = one / 2;
			mod2 = 2; // Changed later if clashes with major ticks
		} else {
			minstep = one;
			mod2 = 10; // Changed later if clashes with major ticks
		}

		// Find step size for major ticks
		one = 1;
		while (one > major)
			one /= 10;
		while (one < major)
			one *= 10;
		if (one / 2 >= major) {
			// major step is round-five, major-notable is next round-ten
			double majorstep = one / 2;
			mod3 = (int) Math.round(majorstep / minstep);
			mod4 = mod3 * 2;
		} else {
			// major step is round-ten, major-notable is next round-ten
			mod3 = (int) Math.round(one / minstep);
			mod4 = mod3 * 10;
		}
		// Check for clashes between minor-notable and major-nonnotable
		if (mod3 == mod2) {
			if (mod2 == 2)
				mod2 = 1; // Every minor tick is notable
			else
				mod2 = 5; // Every fifth minor tick is notable
		}

		// Calculate starting position
		int pos = (int) Math.ceil(start / minstep);
		// System.out.println("mod2="+mod2+" mod3="+mod3+" mod4="+mod4);
		while (pos * minstep <= end) {
			double unitValue = pos * minstep;
			double value = fromUnit(unitValue);

			if (pos % mod4 == 0)
				ticks.add(new Tick(value, unitValue, true, true));
			else if (pos % mod3 == 0)
				ticks.add(new Tick(value, unitValue, true, false));
			else if (pos % mod2 == 0)
				ticks.add(new Tick(value, unitValue, false, true));
			else
				ticks.add(new Tick(value, unitValue, false, false));

			pos++;
		}

		return ticks.toArray(new Tick[0]);
	}

}
