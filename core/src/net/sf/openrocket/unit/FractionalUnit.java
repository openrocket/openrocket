package net.sf.openrocket.unit;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class FractionalUnit extends Unit {

	// This is the base of the fractions.  ie, 16d for 1/16ths.
	private final int fractionBase;
	// This is 1d/fractionBase;
	private final double fractionValue;

	// This is the value used when incrementing/decrementing.
	private final double incrementValue;

	// If the actual value differs from the decimal representation by more than this,
	// we display as decimals.
	private final double epsilon;

	private final String unitLabel;

	public FractionalUnit(double multiplier, String unit, String unitLabel, int fractionBase, double incrementValue) {
		this( multiplier, unit, unitLabel, fractionBase, incrementValue, 0.1d/fractionBase);
	}

	public FractionalUnit(double multiplier, String unit, String unitLabel, int fractionBase, double incrementValue, double epsilon) {
		super(multiplier, unit);
		this.unitLabel = unitLabel;
		this.fractionBase = fractionBase;
		this.fractionValue = 1.0d/fractionBase;
		this.incrementValue = incrementValue;
		this.epsilon = epsilon;
	}

	@Override
	public double round(double value) {
		return roundTo( value, fractionValue );
	}

	private double roundTo( double value, double fraction ) {
		double remainder = Math.IEEEremainder( value, fraction );
		return value - remainder;
	}

	@Override
	public double getNextValue(double value) {
		double rounded = roundTo(value, incrementValue);
		if ( rounded <= value + epsilon) {
			rounded += incrementValue;
		}
		return rounded;
	}

	@Override
	public double getPreviousValue(double value) {
		double rounded = roundTo(value, incrementValue);
		if ( rounded >= value - epsilon ) {
			rounded -= incrementValue;
		}
		return rounded;
	}

	@Override
	public Tick[] getTicks(double start, double end, double minor, double major) {
		// Convert values
		start = toUnit(start);
		end = toUnit(end);
		minor = toUnit(minor);
		major = toUnit(major);

		if (minor <= 0 || major <= 0 || major < minor) {
			throw new IllegalArgumentException("getTicks called with minor="+minor+" major="+major);
		}

		ArrayList<Tick> ticks = new ArrayList<Tick>();

		int mod2,mod3,mod4;  // Moduli for minor-notable, major-nonnotable, major-notable
		double minstep;

		// Find the smallest possible step size
		double one=1;
		while (one > minor)
			one /= 2;
		while (one < minor)
			one *= 2;
		minstep = one;
		mod2 = 16;

		// Find step size for major ticks
		one = 1;
		while (one > major)
			one /= 10;
		while (one < major)
			one *= 10;
		if (one/2 >= major) {
			// major step is round-five, major-notable is next round-ten
			double majorstep = one/2;
			mod3 = (int)Math.round(majorstep/minstep);
			mod4 = mod3*2;
		} else {
			// major step is round-ten, major-notable is next round-ten
			mod3 = (int)Math.round(one/minstep);
			mod4 = mod3*10;
		}
		// Check for clashes between minor-notable and major-nonnotable
		if (mod3 == mod2) {
			if (mod2==2)
				mod2 = 1;  // Every minor tick is notable
			else
				mod2 = 5;  // Every fifth minor tick is notable
		}


		// Calculate starting position
		int pos = (int)Math.ceil(start/minstep);
		//		System.out.println("mod2="+mod2+" mod3="+mod3+" mod4="+mod4);
		while (pos*minstep <= end) {
			double unitValue = pos*minstep;
			double value = fromUnit(unitValue);

			if (pos%mod4 == 0)
				ticks.add(new Tick(value,unitValue,true,true));
			else if (pos%mod3 == 0)
				ticks.add(new Tick(value,unitValue,true,false));
			else if (pos%mod2 == 0)
				ticks.add(new Tick(value,unitValue,false,true));
			else
				ticks.add(new Tick(value,unitValue,false,false));

			pos++;
		}

		return ticks.toArray(new Tick[0]);
	}


	@Override
	public String toString(double value) {

		double correctVal = toUnit(value);
		double val = round(correctVal);


		if ( Math.abs( val - correctVal ) > epsilon ) {
			NumberFormat decFormat = new DecimalFormat("#.###");
			return decFormat.format(correctVal);
		}

		NumberFormat intFormat = new DecimalFormat("#");
		double sign = Math.signum(val);

		double posValue = sign * val;

		double intPart = Math.floor(posValue);

		double frac = Math.rint((posValue - intPart)/fractionValue);
		double fracBase = fractionBase;

		// Reduce fraction.
		while ( frac > 0 && fracBase > 2 && frac % 2 == 0 ) {
			frac /= 2.0;
			fracBase /= 2.0;
		}

		posValue *= sign;

		if ( frac == 0.0 )  {
			return intFormat.format(posValue);
		} else if (intPart == 0.0 ){
			return intFormat.format(sign*frac) + "/" + intFormat.format(fracBase);
		} else {
			return intFormat.format(sign*intPart) + " " + intFormat.format(frac) + "/" + intFormat.format(fracBase);
		}

	}

	@Override
	public String toStringUnit(double value) {
		if (Double.isNaN(value))
			return "N/A";

		String s = toString(value);
		s += " " + unitLabel;
		return s;
	}

}
