package net.sf.openrocket.unit;

import java.util.ArrayList;

public class GeneralUnit extends Unit {

	@SuppressWarnings("unused")
	private final int significantNumbers;
	private final int decimalRounding;
	
	// Values smaller that this are rounded using decimal rounding
	// [pre-calculated as 10^(significantNumbers-1)]
	private final double decimalLimit; 
	
	// Pre-calculated as 10^significantNumbers
	private final double significantNumbersLimit;
	
	
	public GeneralUnit(double multiplier, String unit) {
		this(multiplier, unit, 2, 10);
	}
	
	public GeneralUnit(double multiplier, String unit, int significantNumbers) {
		this(multiplier, unit, significantNumbers, 10);
	}
	
	public GeneralUnit(double multiplier, String unit, int significantNumbers, int decimalRounding) {
		super(multiplier, unit);
		assert(significantNumbers>0);
		assert(decimalRounding>0);
		
		this.significantNumbers = significantNumbers;
		this.decimalRounding = decimalRounding;
		
		double d=1;
		double e=10;
		for (int i=1; i<significantNumbers; i++) {
			d *= 10.0;
			e *= 10.0;
		}
		decimalLimit = d;
		significantNumbersLimit = e;
	}

	@Override
	public double round(double value) {
		if (value < decimalLimit) {
			// Round to closest 1/decimalRounding
			return Math.rint(value*decimalRounding)/decimalRounding;
		} else {
			// Round to given amount of significant numbers
			double m = 1;
			while (value >= significantNumbersLimit) {
				m *= 10.0;
				value /= 10.0;
			}
			return Math.rint(value)*m;
		}
	}

	// TODO: LOW: untested
	// start, end and scale in this units
//	@Override
	public ArrayList<Tick> getTicks(double start, double end, double scale) {
		ArrayList<Tick> ticks = new ArrayList<Tick>();
		@SuppressWarnings("unused")
		double delta;
		@SuppressWarnings("unused")
		int normal, major;

		// TODO: LOW: more fine-grained (e.g.  0||||5||||10||||15||||20)
		if (scale <= 1.0/decimalRounding) {
			delta = 1.0/decimalRounding;
			normal = 1;
			major = decimalRounding;
		} else if (scale <= 1.0) {
			delta = 1.0/decimalRounding;
			normal = decimalRounding;
			major = decimalRounding*10;
		} else {
			double r = scale;
			delta = 1;
			while (r > 10) {
				r /= 10;
				delta *= 10;
			}
			normal = 10;
			major = 100;   // TODO: LOW: More fine-grained with 5
		}
		
//		double v = Math.ceil(start/delta)*delta;
//		int n = (int)Math.round(v/delta);		
//		while (v <= end) {
//			if (n%major == 0)
//				ticks.add(new Tick(v,Tick.MAJOR));
//			else if (n%normal == 0)
//				ticks.add(new Tick(v,Tick.NORMAL));
//			else
//				ticks.add(new Tick(v,Tick.MINOR));
//			v += delta;
//			n++;
//		}
		
		return ticks;
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
			one /= 10;
		while (one < minor)
			one *= 10;
		// one is the smallest round-ten that is larger than minor
		if (one/2 >= minor) {
			// smallest step is round-five
			minstep = one/2;
			mod2 = 2;  // Changed later if clashes with major ticks
		} else {
			minstep = one;
			mod2 = 10;  // Changed later if clashes with major ticks
		}
		
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
	public double getNextValue(double value) {
		// TODO: HIGH: Auto-generated method stub
		return value+1;
	}

	@Override
	public double getPreviousValue(double value) {
		// TODO: HIGH: Auto-generated method stub
		return value-1;
	}
	
	
	///// TESTING:
	
	private static void printTicks(double start, double end, double minor, double major) {
		Tick[] ticks = Unit.NOUNIT.getTicks(start, end, minor, major);
		String str = "Ticks for ("+start+","+end+","+minor+","+major+"):";
		for (int i=0; i<ticks.length; i++) {
			str += " "+ticks[i].value;
			if (ticks[i].major) {
				if (ticks[i].notable)
					str += "*";
				else
					str += "o";
			} else {
				if (ticks[i].notable)
					str += "_";
				else
					str += " ";
			}
		}
		System.out.println(str);
	}
	public static void main(String[] arg) {
		printTicks(0,100,1,10);
		printTicks(4.7,11.0,0.15,0.7);
	}
	
}
