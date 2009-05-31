package net.sf.openrocket.unit;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Rocket;


/**
 * A group of units (eg. length, mass etc.).  Contains a list of different units of a same
 * quantity.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class UnitGroup {

	public static final UnitGroup UNITS_NONE;
	
	public static final UnitGroup UNITS_MOTOR_DIMENSIONS;
	public static final UnitGroup UNITS_LENGTH;
	public static final UnitGroup UNITS_DISTANCE;
	
	public static final UnitGroup UNITS_AREA;
	public static final UnitGroup UNITS_STABILITY;
	public static final UnitGroup UNITS_VELOCITY;
	public static final UnitGroup UNITS_ACCELERATION;
	public static final UnitGroup UNITS_MASS;
	public static final UnitGroup UNITS_ANGLE;
	public static final UnitGroup UNITS_DENSITY_BULK;
	public static final UnitGroup UNITS_DENSITY_SURFACE;
	public static final UnitGroup UNITS_DENSITY_LINE;
	public static final UnitGroup UNITS_FORCE;
	public static final UnitGroup UNITS_IMPULSE;
	
	/** Time in the order of less than a second (time step etc). */
	public static final UnitGroup UNITS_TIME_STEP;
	
	/** Time in the order of seconds (motor delay etc). */
	public static final UnitGroup UNITS_SHORT_TIME;
	
	/** Time in the order of the flight time of a rocket. */
	public static final UnitGroup UNITS_FLIGHT_TIME;
	public static final UnitGroup UNITS_ROLL;
	public static final UnitGroup UNITS_TEMPERATURE;
	public static final UnitGroup UNITS_PRESSURE;
	public static final UnitGroup UNITS_RELATIVE;
	public static final UnitGroup UNITS_ROUGHNESS;
	
	public static final UnitGroup UNITS_COEFFICIENT;
	
	
	public static final Map<String, UnitGroup> UNITS;
	
	
	/*
	 * Note:  Units may not use HTML tags.
	 */
	static {
		UNITS_NONE = new UnitGroup();
		UNITS_NONE.addUnit(Unit.NOUNIT2);
		
		UNITS_LENGTH = new UnitGroup();
		UNITS_LENGTH.addUnit(new GeneralUnit(0.001,"mm"));
		UNITS_LENGTH.addUnit(new GeneralUnit(0.01,"cm"));
		UNITS_LENGTH.addUnit(new GeneralUnit(1,"m"));
		UNITS_LENGTH.addUnit(new GeneralUnit(0.0254,"in"));
		UNITS_LENGTH.addUnit(new GeneralUnit(0.3048,"ft"));
		UNITS_LENGTH.setDefaultUnit(1);
		
		UNITS_MOTOR_DIMENSIONS = new UnitGroup();
		UNITS_MOTOR_DIMENSIONS.addUnit(new GeneralUnit(0.001,"mm"));
		UNITS_MOTOR_DIMENSIONS.addUnit(new GeneralUnit(0.01,"cm"));
		UNITS_MOTOR_DIMENSIONS.addUnit(new GeneralUnit(0.0254,"in"));
		UNITS_MOTOR_DIMENSIONS.setDefaultUnit(0);
		
		UNITS_DISTANCE = new UnitGroup();
		UNITS_DISTANCE.addUnit(new GeneralUnit(1,"m"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(1000,"km"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(0.3048,"ft"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(0.9144,"yd"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(1609.344,"mi"));
		
		UNITS_AREA = new UnitGroup();
		UNITS_AREA.addUnit(new GeneralUnit(pow2(0.001),"mm\u00b2"));
		UNITS_AREA.addUnit(new GeneralUnit(pow2(0.01),"cm\u00b2"));
		UNITS_AREA.addUnit(new GeneralUnit(1,"m\u00b2"));
		UNITS_AREA.addUnit(new GeneralUnit(pow2(0.0254),"in\u00b2"));
		UNITS_AREA.addUnit(new GeneralUnit(pow2(0.3048),"ft\u00b2"));
		UNITS_AREA.setDefaultUnit(1);
		
		
		UNITS_STABILITY = new UnitGroup();
		UNITS_STABILITY.addUnit(new GeneralUnit(0.001,"mm"));
		UNITS_STABILITY.addUnit(new GeneralUnit(0.01,"cm"));
		UNITS_STABILITY.addUnit(new GeneralUnit(0.0254,"in"));
		UNITS_STABILITY.addUnit(new CaliberUnit((Rocket)null));
		UNITS_STABILITY.setDefaultUnit(3);
		
		UNITS_VELOCITY = new UnitGroup();
		UNITS_VELOCITY.addUnit(new GeneralUnit(1, "m/s"));
		UNITS_VELOCITY.addUnit(new GeneralUnit(1/3.6, "km/h"));
		UNITS_VELOCITY.addUnit(new GeneralUnit(1/0.3048, "ft/s"));
		UNITS_VELOCITY.addUnit(new GeneralUnit(0.44704, "mph"));
		
		UNITS_ACCELERATION = new UnitGroup();
		UNITS_ACCELERATION.addUnit(new GeneralUnit(1, "m/s\u00b2"));
		UNITS_ACCELERATION.addUnit(new GeneralUnit(1/0.3048, "ft/s\00b2"));
		

		UNITS_MASS = new UnitGroup();
		UNITS_MASS.addUnit(new GeneralUnit(0.001,"g"));
		UNITS_MASS.addUnit(new GeneralUnit(1,"kg"));
		UNITS_MASS.addUnit(new GeneralUnit(0.0283495,"oz"));
		UNITS_MASS.addUnit(new GeneralUnit(0.0453592,"lb"));
		
		UNITS_ANGLE = new UnitGroup();
		UNITS_ANGLE.addUnit(new DegreeUnit());
		UNITS_ANGLE.addUnit(new FixedPrecisionUnit("rad",0.01));
		
		UNITS_DENSITY_BULK = new UnitGroup();
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(1000,"g/cm\u00b3"));
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(1,"kg/m\u00b3"));
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(1729.004,"oz/in\u00b3"));
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(16.01846,"lb/ft\u00b3"));

		UNITS_DENSITY_SURFACE = new UnitGroup();
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(10,"g/cm\u00b2"));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(0.001,"g/m\u00b2"));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(1,"kg/m\u00b2"));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(43.9418,"oz/in\u00b2"));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(0.30515173,"oz/ft\u00b2"));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(4.88243,"lb/ft\u00b2"));
		UNITS_DENSITY_SURFACE.setDefaultUnit(1);

		UNITS_DENSITY_LINE = new UnitGroup();
		UNITS_DENSITY_LINE.addUnit(new GeneralUnit(0.001,"g/m"));
		UNITS_DENSITY_LINE.addUnit(new GeneralUnit(1,"kg/m"));
		UNITS_DENSITY_LINE.addUnit(new GeneralUnit(0.0930102,"oz/ft"));

		UNITS_FORCE = new UnitGroup();
		UNITS_FORCE.addUnit(new GeneralUnit(1,"N"));
		UNITS_FORCE.addUnit(new GeneralUnit(4.448222,"lbf"));
		UNITS_FORCE.addUnit(new GeneralUnit(9.80665,"kgf"));

		UNITS_IMPULSE = new UnitGroup();
		UNITS_IMPULSE.addUnit(new GeneralUnit(1,"Ns"));
		UNITS_IMPULSE.addUnit(new GeneralUnit(4.448222, "lbf\u00b7s"));

		UNITS_TIME_STEP = new UnitGroup();
		UNITS_TIME_STEP.addUnit(new FixedPrecisionUnit("ms", 1, 0.001));
		UNITS_TIME_STEP.addUnit(new FixedPrecisionUnit("s", 0.01));
		UNITS_TIME_STEP.setDefaultUnit(1);

		UNITS_SHORT_TIME = new UnitGroup();
		UNITS_SHORT_TIME.addUnit(new GeneralUnit(1,"s"));

		UNITS_FLIGHT_TIME = new UnitGroup();
		UNITS_FLIGHT_TIME.addUnit(new GeneralUnit(1,"s"));
		UNITS_FLIGHT_TIME.addUnit(new GeneralUnit(60,"min"));
		
		UNITS_ROLL = new UnitGroup();
		UNITS_ROLL.addUnit(new GeneralUnit(1, "rad/s"));
		UNITS_ROLL.addUnit(new GeneralUnit(2*Math.PI, "r/s"));
		UNITS_ROLL.addUnit(new GeneralUnit(2*Math.PI/60, "rpm"));
		UNITS_ROLL.setDefaultUnit(1);

		UNITS_TEMPERATURE = new UnitGroup();
		UNITS_TEMPERATURE.addUnit(new FixedPrecisionUnit("K", 1));
		UNITS_TEMPERATURE.addUnit(new TemperatureUnit(1, 273.15, "\u00b0C"));
		UNITS_TEMPERATURE.addUnit(new TemperatureUnit(5.0/9.0, 459.67, "\u00b0F"));
		UNITS_TEMPERATURE.setDefaultUnit(1);
		
		UNITS_PRESSURE = new UnitGroup();
		UNITS_PRESSURE.addUnit(new FixedPrecisionUnit("mbar", 1, 1.0e2));
		UNITS_PRESSURE.addUnit(new FixedPrecisionUnit("bar", 0.001, 1.0e5));
		UNITS_PRESSURE.addUnit(new FixedPrecisionUnit("atm", 0.001, 1.01325e5));
		UNITS_PRESSURE.addUnit(new GeneralUnit(133.322, "mmHg"));
		UNITS_PRESSURE.addUnit(new GeneralUnit(3386.389, "inHg"));
		UNITS_PRESSURE.addUnit(new GeneralUnit(6894.757, "psi"));
		UNITS_PRESSURE.addUnit(new GeneralUnit(1, "Pa"));

		UNITS_RELATIVE = new UnitGroup();
		UNITS_RELATIVE.addUnit(new FixedPrecisionUnit("\u200b", 0.01));
		UNITS_RELATIVE.addUnit(new FixedPrecisionUnit("%", 1, 0.01));
		UNITS_RELATIVE.setDefaultUnit(1);

		
		UNITS_ROUGHNESS = new UnitGroup();
		UNITS_ROUGHNESS.addUnit(new GeneralUnit(0.000001, "\u03bcm"));
		UNITS_ROUGHNESS.addUnit(new GeneralUnit(0.0000254, "mil"));
		
		
		UNITS_COEFFICIENT = new UnitGroup();
		UNITS_COEFFICIENT.addUnit(new FixedPrecisionUnit("\u200b", 0.01));  // zero-width space
		

		HashMap<String,UnitGroup> map = new HashMap<String,UnitGroup>();
		map.put("NONE", UNITS_NONE);
		map.put("LENGTH", UNITS_LENGTH);
		map.put("MOTOR_DIMENSIONS", UNITS_MOTOR_DIMENSIONS);
		map.put("DISTANCE", UNITS_DISTANCE);
		map.put("VELOCITY", UNITS_VELOCITY);
		map.put("ACCELERATION", UNITS_ACCELERATION);
		map.put("AREA", UNITS_AREA);
		map.put("STABILITY", UNITS_STABILITY);
		map.put("MASS", UNITS_MASS);
		map.put("ANGLE", UNITS_ANGLE);
		map.put("DENSITY_BULK", UNITS_DENSITY_BULK);
		map.put("DENSITY_SURFACE", UNITS_DENSITY_SURFACE);
		map.put("DENSITY_LINE", UNITS_DENSITY_LINE);
		map.put("FORCE", UNITS_FORCE);
		map.put("IMPULSE", UNITS_IMPULSE);
		map.put("TIME_STEP", UNITS_TIME_STEP);
		map.put("SHORT_TIME", UNITS_SHORT_TIME);
		map.put("FLIGHT_TIME", UNITS_FLIGHT_TIME);
		map.put("ROLL", UNITS_ROLL);
		map.put("TEMPERATURE", UNITS_TEMPERATURE);
		map.put("PRESSURE", UNITS_PRESSURE);
		map.put("RELATIVE", UNITS_RELATIVE);
		map.put("ROUGHNESS", UNITS_ROUGHNESS);
		map.put("COEFFICIENT", UNITS_COEFFICIENT);

		UNITS = Collections.unmodifiableMap(map);
	}
	
	public static void setDefaultMetricUnits() {
		UNITS_LENGTH.setDefaultUnit("cm");
		UNITS_MOTOR_DIMENSIONS.setDefaultUnit("mm");
		UNITS_DISTANCE.setDefaultUnit("m");
		UNITS_AREA.setDefaultUnit("cm\u00b2");
		UNITS_STABILITY.setDefaultUnit("cal");
		UNITS_VELOCITY.setDefaultUnit("m/s");
		UNITS_ACCELERATION.setDefaultUnit("m/s\u00b2");
		UNITS_MASS.setDefaultUnit("g");
		UNITS_ANGLE.setDefaultUnit(0);
		UNITS_DENSITY_BULK.setDefaultUnit("g/cm\u00b3");
		UNITS_DENSITY_SURFACE.setDefaultUnit("g/m\u00b2");
		UNITS_DENSITY_LINE.setDefaultUnit("g/m");
		UNITS_FORCE.setDefaultUnit("N");
		UNITS_IMPULSE.setDefaultUnit("Ns");
		UNITS_TIME_STEP.setDefaultUnit("s");
		UNITS_FLIGHT_TIME.setDefaultUnit("s");
		UNITS_ROLL.setDefaultUnit("r/s");
		UNITS_TEMPERATURE.setDefaultUnit(1);
		UNITS_PRESSURE.setDefaultUnit("mbar");
		UNITS_RELATIVE.setDefaultUnit("%");
		UNITS_ROUGHNESS.setDefaultUnit("\u03bcm");
	}
	
	public static void setDefaultImperialUnits() {
		UNITS_LENGTH.setDefaultUnit("in");
		UNITS_MOTOR_DIMENSIONS.setDefaultUnit("in");
		UNITS_DISTANCE.setDefaultUnit("ft");
		UNITS_AREA.setDefaultUnit("in\u00b2");
		UNITS_STABILITY.setDefaultUnit("cal");
		UNITS_VELOCITY.setDefaultUnit("ft/s");
		UNITS_ACCELERATION.setDefaultUnit("ft/s\u00b2");
		UNITS_MASS.setDefaultUnit("oz");
		UNITS_ANGLE.setDefaultUnit(0);
		UNITS_DENSITY_BULK.setDefaultUnit("oz/in\u00b3");
		UNITS_DENSITY_SURFACE.setDefaultUnit("oz/ft\u00b2");
		UNITS_DENSITY_LINE.setDefaultUnit("oz/ft");
		UNITS_FORCE.setDefaultUnit("N");
		UNITS_IMPULSE.setDefaultUnit("Ns");
		UNITS_TIME_STEP.setDefaultUnit("s");
		UNITS_FLIGHT_TIME.setDefaultUnit("s");
		UNITS_ROLL.setDefaultUnit("r/s");
		UNITS_TEMPERATURE.setDefaultUnit(2);
		UNITS_PRESSURE.setDefaultUnit("mbar");
		UNITS_RELATIVE.setDefaultUnit("%");
		UNITS_ROUGHNESS.setDefaultUnit("mil");
	}
	
	
	
	public static UnitGroup stabilityUnits(Rocket rocket) {
		return new StabilityUnitGroup(rocket);
	}
	
	
	public static UnitGroup stabilityUnits(Configuration config) {
		return new StabilityUnitGroup(config);
	}
	

	//////////////////////////////////////////////////////

	
	private ArrayList<Unit> units = new ArrayList<Unit>();
	private int defaultUnit = 0;
	
	public int getUnitCount() {
		return units.size();
	}

	public Unit getDefaultUnit() {
		return units.get(defaultUnit);
	}
	
	public int getDefaultUnitIndex() {
		return defaultUnit;
	}
	
	public void setDefaultUnit(int n) {
		if (n<0 || n>=units.size()) {
			throw new IllegalArgumentException("index out of range: "+n);
		}
		defaultUnit = n;
	}
	
	/**
	 * Set the default unit based on the unit name.  Does nothing if the name
	 * does not match any of the units.
	 * 
	 * @param name	the unit name (<code>null</code> ok).
	 * @return		<code>true</code> if the the default was set, 
	 * 				<code>false</code> if a matching unit was not found.  
	 */
	public boolean setDefaultUnit(String name) {
		if (name == null)
			return false;
		
		for (int i=0; i < units.size(); i++) {
			if (name.equals(units.get(i).getUnit())) {
				setDefaultUnit(i);
				return true;
			}
		}
		return false;
	}

	
	public Unit getUnit(int n) {
		return units.get(n);
	}
	
	public int getUnitIndex(Unit u) {
		return units.indexOf(u);
	}
	
	public void addUnit(Unit u) {
		units.add(u);
	}
	
	public void addUnit(int n, Unit u) {
		units.add(n,u);
	}
	
	public void removeUnit(int n) {
		units.remove(n);
	}
	
	public boolean contains(Unit u) {
		return units.contains(u);
	}
	
	public Unit[] getUnits() {
		return units.toArray(new Unit[0]);
	}
	
	
	/**
	 * Return the value formatted by the default unit of this group.
	 * It is the same as calling <code>getDefaultUnit().toString(value)</code>.
	 * 
	 * @param value		the SI value to format.
	 * @return			the formatted string.
	 * @see 			Unit#toString(double)
	 */
	public String toString(double value) {
		return this.getDefaultUnit().toString(value);
	}
	
	
	/**
	 * Return the value formatted by the default unit of this group including the unit.
	 * It is the same as calling <code>getDefaultUnit().toStringUnit(value)</code>.
	 * 
	 * @param value		the SI value to format.
	 * @return			the formatted string.
	 * @see 			Unit#toStringUnit(double)
	 */
	public String toStringUnit(double value) {
		return this.getDefaultUnit().toStringUnit(value);
	}
	
	
	private static final Pattern STRING_PATTERN = Pattern.compile("^\\s*([0-9.,-]+)(.*?)$");
	/**
	 * Converts a string into an SI value.  If the string has one of the units in this
	 * group appended to it, that unit will be used in conversion.  Otherwise the default
	 * unit will be used.  If an unknown unit is specified or the value does not parse
	 * with <code>Double.parseDouble</code> then a <code>NumberFormatException</code> 
	 * is thrown.
	 * <p>
	 * This method is applicable only for simple units without e.g. powers.
	 * 
	 * @param str   the string to parse.
	 * @return		the SI value.
	 * @throws NumberFormatException   if the string cannot be parsed.
	 */
	public double fromString(String str) {
		Matcher matcher = STRING_PATTERN.matcher(str);
		
		if (!matcher.matches()) {
			throw new NumberFormatException("string did not match required pattern");
		}
		
		double value = Double.parseDouble(matcher.group(1));
		String unit = matcher.group(2).trim();
		
		if (unit.equals("")) {
			value = this.getDefaultUnit().fromUnit(value);
		} else {
			int i;
			for (i=0; i < units.size(); i++) {
				Unit u = units.get(i);
				if (unit.equalsIgnoreCase(u.getUnit())) {
					value = u.fromUnit(value);
					break;
				}
			}
			if (i >= units.size()) {
				throw new NumberFormatException("unknown unit "+unit);
			}
		}
		
		return value;
	}
	
	
	///////////////////////////
	
	
	/**
	 * A private class that switches the CaliberUnit to a rocket-specific CaliberUnit.
	 * All other methods are passed through to UNITS_STABILITY.
	 */
	private static class StabilityUnitGroup extends UnitGroup {
		
		private final CaliberUnit caliberUnit;
		
		
		public StabilityUnitGroup(Rocket rocket) {
			caliberUnit = new CaliberUnit(rocket);
		}
		
		public StabilityUnitGroup(Configuration config) {
			caliberUnit = new CaliberUnit(config);
		}
		

		////  Modify CaliberUnit to use local variable
		
		@Override
		public Unit getDefaultUnit() {
			return getUnit(UNITS_STABILITY.getDefaultUnitIndex());
		}

		@Override
		public Unit getUnit(int n) {
			Unit u = UNITS_STABILITY.getUnit(n);
			if (u instanceof CaliberUnit) {
				return caliberUnit;
			}
			return u;
		}

		@Override
		public int getUnitIndex(Unit u) {
			if (u instanceof CaliberUnit) {
				for (int i=0; i < UNITS_STABILITY.getUnitCount(); i++) {
					if (UNITS_STABILITY.getUnit(i) instanceof CaliberUnit)
						return i;
				}
			}
			return UNITS_STABILITY.getUnitIndex(u);
		}

		

		////  Pass on to UNITS_STABILITY
		
		@Override
		public int getDefaultUnitIndex() {
			return UNITS_STABILITY.getDefaultUnitIndex();
		}

		@Override
		public void setDefaultUnit(int n) {
			UNITS_STABILITY.setDefaultUnit(n);
		}

		@Override
		public int getUnitCount() {
			return UNITS_STABILITY.getUnitCount();
		}


		////  Unsupported methods
		
		@Override
		public void addUnit(int n, Unit u) {
			throw new UnsupportedOperationException("StabilityUnitGroup must not be modified");
		}

		@Override
		public void addUnit(Unit u) {
			throw new UnsupportedOperationException("StabilityUnitGroup must not be modified");
		}

		@Override
		public void removeUnit(int n) {
			throw new UnsupportedOperationException("StabilityUnitGroup must not be modified");
		}
	}
}
