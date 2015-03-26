package net.sf.openrocket.unit;

import static net.sf.openrocket.util.Chars.CUBED;
import static net.sf.openrocket.util.Chars.DEGREE;
import static net.sf.openrocket.util.Chars.DOT;
import static net.sf.openrocket.util.Chars.MICRO;
import static net.sf.openrocket.util.Chars.PERMILLE;
import static net.sf.openrocket.util.Chars.SQUARED;
import static net.sf.openrocket.util.Chars.ZWSP;
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
	public static final UnitGroup UNITS_ALL_LENGTHS;
	public static final UnitGroup UNITS_DISTANCE;
	
	public static final UnitGroup UNITS_AREA;
	public static final UnitGroup UNITS_STABILITY;
	/**
	 * This unit group contains only the caliber unit that never scales the originating "SI" value.
	 * It can be used in cases where the originating value is already in calibers to obtains the correct unit.
	 */
	public static final UnitGroup UNITS_STABILITY_CALIBERS;
	public static final UnitGroup UNITS_VELOCITY;
	public static final UnitGroup UNITS_WINDSPEED;
	public static final UnitGroup UNITS_ACCELERATION;
	public static final UnitGroup UNITS_MASS;
	public static final UnitGroup UNITS_INERTIA;
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
	public static final UnitGroup UNITS_FREQUENCY;
	
	public static final UnitGroup UNITS_ENERGY;
	public static final UnitGroup UNITS_POWER;
	public static final UnitGroup UNITS_MOMENTUM;
	public static final UnitGroup UNITS_VOLTAGE;
	public static final UnitGroup UNITS_CURRENT;
	
	
	public static final Map<String, UnitGroup> UNITS; // keys such as "LENGTH", "VELOCITY"
	public static final Map<String, UnitGroup> SIUNITS; // keys such a "m", "m/s"
	
	
	/*
	 * Note:  Units may not use HTML tags.
	 * 
	 * The scaling value "X" is obtained by "one of this unit is X of SI units"
	 * Type into Google for example:  "1 in^2 in m^2"
	 */
	static {
		UNITS_NONE = new UnitGroup();
		UNITS_NONE.addUnit(Unit.NOUNIT);
		UNITS_NONE.setDefaultUnit(0);
		
		UNITS_ENERGY = new UnitGroup();
		UNITS_ENERGY.addUnit(new GeneralUnit(1, "J"));
		UNITS_ENERGY.addUnit(new GeneralUnit(1e-7, "erg"));
		UNITS_ENERGY.addUnit(new GeneralUnit(1.055, "BTU"));
		UNITS_ENERGY.addUnit(new GeneralUnit(4.184, "cal"));
		UNITS_ENERGY.addUnit(new GeneralUnit(1.3558179483314, "ft" + DOT + "lbf"));
		UNITS_ENERGY.setDefaultUnit(0);
		
		UNITS_POWER = new UnitGroup();
		UNITS_POWER.addUnit(new GeneralUnit(1e-3, "mW"));
		UNITS_POWER.addUnit(new GeneralUnit(1, "W"));
		UNITS_POWER.addUnit(new GeneralUnit(1e3, "kW"));
		UNITS_POWER.addUnit(new GeneralUnit(1e-7, "ergs"));
		UNITS_POWER.addUnit(new GeneralUnit(745.699872, "hp"));
		UNITS_POWER.setDefaultUnit(1);
		
		UNITS_MOMENTUM = new UnitGroup();
		UNITS_MOMENTUM.addUnit(new GeneralUnit(1, "kg" + DOT + "m/s"));
		UNITS_MOMENTUM.setDefaultUnit(0);
		
		UNITS_VOLTAGE = new UnitGroup();
		UNITS_VOLTAGE.addUnit(new GeneralUnit(1e-3, "mV"));
		UNITS_VOLTAGE.addUnit(new GeneralUnit(1, "V"));
		UNITS_VOLTAGE.setDefaultUnit(1);
		
		UNITS_CURRENT = new UnitGroup();
		UNITS_CURRENT.addUnit(new GeneralUnit(1e-3, "mA"));
		UNITS_CURRENT.addUnit(new GeneralUnit(1, "A"));
		UNITS_CURRENT.setDefaultUnit(1);
		
		UNITS_LENGTH = new UnitGroup();
		UNITS_LENGTH.addUnit(new GeneralUnit(0.001, "mm"));
		UNITS_LENGTH.addUnit(new GeneralUnit(0.01, "cm"));
		UNITS_LENGTH.addUnit(new GeneralUnit(1, "m"));
		UNITS_LENGTH.addUnit(new InchUnit(0.0254, "in"));
		UNITS_LENGTH.addUnit(new FractionalUnit(0.0254, "in/64", "in", 64, 1d / 16d, 0.5d / 64d));
		UNITS_LENGTH.addUnit(new GeneralUnit(0.3048, "ft"));
		UNITS_LENGTH.setDefaultUnit(1);
		
		UNITS_MOTOR_DIMENSIONS = new UnitGroup();
		UNITS_MOTOR_DIMENSIONS.addUnit(new GeneralUnit(0.001, "mm"));
		UNITS_MOTOR_DIMENSIONS.addUnit(new GeneralUnit(0.01, "cm"));
		UNITS_MOTOR_DIMENSIONS.addUnit(new GeneralUnit(1, "m"));
		UNITS_MOTOR_DIMENSIONS.addUnit(new GeneralUnit(0.0254, "in"));
		UNITS_MOTOR_DIMENSIONS.setDefaultUnit(0);
		
		UNITS_DISTANCE = new UnitGroup();
		UNITS_DISTANCE.addUnit(new GeneralUnit(1, "m"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(1000, "km"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(0.3048, "ft"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(0.9144, "yd"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(1609.344, "mi"));
		UNITS_DISTANCE.addUnit(new GeneralUnit(1852, "nmi"));
		UNITS_DISTANCE.setDefaultUnit(0);
		
		UNITS_ALL_LENGTHS = new UnitGroup();
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(0.001, "mm"));
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(0.01, "cm"));
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(1, "m"));
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(1000, "km"));
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(0.0254, "in"));
		UNITS_ALL_LENGTHS.addUnit(new FractionalUnit(0.0254, "in/64", "in", 64, 1d / 16d, 0.5d / 64d));
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(0.3048, "ft"));
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(0.9144, "yd"));
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(1609.344, "mi"));
		UNITS_ALL_LENGTHS.addUnit(new GeneralUnit(1852, "nmi"));
		UNITS_ALL_LENGTHS.setDefaultUnit(2);
		
		UNITS_AREA = new UnitGroup();
		UNITS_AREA.addUnit(new GeneralUnit(pow2(0.001), "mm" + SQUARED));
		UNITS_AREA.addUnit(new GeneralUnit(pow2(0.01), "cm" + SQUARED));
		UNITS_AREA.addUnit(new GeneralUnit(1, "m" + SQUARED));
		UNITS_AREA.addUnit(new GeneralUnit(pow2(0.0254), "in" + SQUARED));
		UNITS_AREA.addUnit(new GeneralUnit(pow2(0.3048), "ft" + SQUARED));
		UNITS_AREA.setDefaultUnit(1);
		
		
		UNITS_STABILITY = new UnitGroup();
		UNITS_STABILITY.addUnit(new GeneralUnit(0.001, "mm"));
		UNITS_STABILITY.addUnit(new GeneralUnit(0.01, "cm"));
		UNITS_STABILITY.addUnit(new GeneralUnit(1, "m"));
		UNITS_STABILITY.addUnit(new GeneralUnit(0.0254, "in"));
		UNITS_STABILITY.addUnit(new CaliberUnit((Rocket) null));
		UNITS_STABILITY.setDefaultUnit(4);
		
		UNITS_STABILITY_CALIBERS = new UnitGroup();
		UNITS_STABILITY_CALIBERS.addUnit(new GeneralUnit(1, "cal"));
		UNITS_STABILITY_CALIBERS.setDefaultUnit(0);
		
		
		UNITS_VELOCITY = new UnitGroup();
		UNITS_VELOCITY.addUnit(new GeneralUnit(1, "m/s"));
		UNITS_VELOCITY.addUnit(new GeneralUnit(1 / 3.6, "km/h"));
		UNITS_VELOCITY.addUnit(new GeneralUnit(0.3048, "ft/s"));
		UNITS_VELOCITY.addUnit(new GeneralUnit(0.44704, "mph"));
		UNITS_VELOCITY.addUnit(new GeneralUnit(0.51444445, "kt"));
		UNITS_VELOCITY.setDefaultUnit(0);
		
		UNITS_WINDSPEED = new UnitGroup();
		UNITS_WINDSPEED.addUnit(new GeneralUnit(1, "m/s"));
		UNITS_WINDSPEED.addUnit(new GeneralUnit(1 / 3.6, "km/h"));
		UNITS_WINDSPEED.addUnit(new GeneralUnit(0.3048, "ft/s"));
		UNITS_WINDSPEED.addUnit(new GeneralUnit(0.44704, "mph"));
		UNITS_WINDSPEED.addUnit(new GeneralUnit(0.51444445, "kt"));
		UNITS_WINDSPEED.setDefaultUnit(0);
		
		UNITS_ACCELERATION = new UnitGroup();
		UNITS_ACCELERATION.addUnit(new GeneralUnit(1, "m/s" + SQUARED));
		UNITS_ACCELERATION.addUnit(new GeneralUnit(0.3048, "ft/s" + SQUARED));
		UNITS_ACCELERATION.addUnit(new GeneralUnit(9.80665, "G"));
		UNITS_ACCELERATION.setDefaultUnit(0);
		
		UNITS_MASS = new UnitGroup();
		UNITS_MASS.addUnit(new GeneralUnit(0.001, "g"));
		UNITS_MASS.addUnit(new GeneralUnit(1, "kg"));
		UNITS_MASS.addUnit(new GeneralUnit(0.0283495231, "oz"));
		UNITS_MASS.addUnit(new GeneralUnit(0.45359237, "lb"));
		UNITS_MASS.setDefaultUnit(0);
		
		UNITS_INERTIA = new UnitGroup();
		UNITS_INERTIA.addUnit(new GeneralUnit(0.0001, "kg" + DOT + "cm" + SQUARED));
		UNITS_INERTIA.addUnit(new GeneralUnit(1, "kg" + DOT + "m" + SQUARED));
		UNITS_INERTIA.addUnit(new GeneralUnit(1.82899783e-5, "oz" + DOT + "in" + SQUARED));
		UNITS_INERTIA.addUnit(new GeneralUnit(0.000292639653, "lb" + DOT + "in" + SQUARED));
		UNITS_INERTIA.addUnit(new GeneralUnit(0.0421401101, "lb" + DOT + "ft" + SQUARED));
		UNITS_INERTIA.addUnit(new GeneralUnit(1.35581795, "lbf" + DOT + "ft" + DOT + "s" + SQUARED));
		UNITS_INERTIA.setDefaultUnit(1);
		
		UNITS_ANGLE = new UnitGroup();
		UNITS_ANGLE.addUnit(new DegreeUnit());
		UNITS_ANGLE.addUnit(new FixedPrecisionUnit("rad", 0.01));
		UNITS_ANGLE.addUnit(new GeneralUnit(1.0 / 3437.74677078, "arcmin"));
		UNITS_ANGLE.setDefaultUnit(0);
		
		UNITS_DENSITY_BULK = new UnitGroup();
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(1000, "g/cm" + CUBED));
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(1000, "kg/dm" + CUBED));
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(1, "kg/m" + CUBED));
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(1729.99404, "oz/in" + CUBED));
		UNITS_DENSITY_BULK.addUnit(new GeneralUnit(16.0184634, "lb/ft" + CUBED));
		UNITS_DENSITY_BULK.setDefaultUnit(0);
		
		UNITS_DENSITY_SURFACE = new UnitGroup();
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(10, "g/cm" + SQUARED));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(0.001, "g/m" + SQUARED));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(1, "kg/m" + SQUARED));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(43.9418487, "oz/in" + SQUARED));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(0.305151727, "oz/ft" + SQUARED));
		UNITS_DENSITY_SURFACE.addUnit(new GeneralUnit(4.88242764, "lb/ft" + SQUARED));
		UNITS_DENSITY_SURFACE.setDefaultUnit(1);
		
		UNITS_DENSITY_LINE = new UnitGroup();
		UNITS_DENSITY_LINE.addUnit(new GeneralUnit(0.001, "g/m"));
		UNITS_DENSITY_LINE.addUnit(new GeneralUnit(1, "kg/m"));
		UNITS_DENSITY_LINE.addUnit(new GeneralUnit(0.0930102465, "oz/ft"));
		UNITS_DENSITY_LINE.setDefaultUnit(0);
		
		UNITS_FORCE = new UnitGroup();
		UNITS_FORCE.addUnit(new GeneralUnit(1, "N"));
		UNITS_FORCE.addUnit(new GeneralUnit(4.44822162, "lbf"));
		UNITS_FORCE.addUnit(new GeneralUnit(9.80665, "kgf"));
		UNITS_FORCE.setDefaultUnit(0);
		
		UNITS_IMPULSE = new UnitGroup();
		UNITS_IMPULSE.addUnit(new GeneralUnit(1, "Ns"));
		UNITS_IMPULSE.addUnit(new GeneralUnit(4.44822162, "lbf" + DOT + "s"));
		UNITS_IMPULSE.setDefaultUnit(0);
		
		UNITS_TIME_STEP = new UnitGroup();
		UNITS_TIME_STEP.addUnit(new FixedPrecisionUnit("ms", 1, 0.001));
		UNITS_TIME_STEP.addUnit(new FixedPrecisionUnit("s", 0.01));
		UNITS_TIME_STEP.setDefaultUnit(1);
		
		UNITS_SHORT_TIME = new UnitGroup();
		UNITS_SHORT_TIME.addUnit(new GeneralUnit(1, "s"));
		UNITS_SHORT_TIME.setDefaultUnit(0);
		
		UNITS_FLIGHT_TIME = new UnitGroup();
		UNITS_FLIGHT_TIME.addUnit(new GeneralUnit(1, "s"));
		UNITS_FLIGHT_TIME.addUnit(new GeneralUnit(60, "min"));
		UNITS_FLIGHT_TIME.setDefaultUnit(0);
		
		UNITS_ROLL = new UnitGroup();
		UNITS_ROLL.addUnit(new GeneralUnit(1, "rad/s"));
		UNITS_ROLL.addUnit(new GeneralUnit(Math.PI / 180, DEGREE + "/s"));
		UNITS_ROLL.addUnit(new GeneralUnit(2 * Math.PI, "r/s"));
		UNITS_ROLL.addUnit(new GeneralUnit(2 * Math.PI / 60, "rpm"));
		UNITS_ROLL.setDefaultUnit(1);
		
		UNITS_TEMPERATURE = new UnitGroup();
		UNITS_TEMPERATURE.addUnit(new FixedPrecisionUnit("K", 1));
		UNITS_TEMPERATURE.addUnit(new TemperatureUnit(1, 273.15, DEGREE + "C"));
		UNITS_TEMPERATURE.addUnit(new TemperatureUnit(5.0 / 9.0, 459.67, DEGREE + "F"));
		UNITS_TEMPERATURE.setDefaultUnit(1);
		
		UNITS_PRESSURE = new UnitGroup();
		UNITS_PRESSURE.addUnit(new FixedPrecisionUnit("mbar", 1, 1.0e2));
		UNITS_PRESSURE.addUnit(new FixedPrecisionUnit("bar", 0.001, 1.0e5));
		UNITS_PRESSURE.addUnit(new FixedPrecisionUnit("atm", 0.001, 1.01325e5));
		UNITS_PRESSURE.addUnit(new GeneralUnit(101325.0 / 760.0, "mmHg"));
		UNITS_PRESSURE.addUnit(new GeneralUnit(3386.389, "inHg"));
		UNITS_PRESSURE.addUnit(new GeneralUnit(6894.75729, "psi"));
		UNITS_PRESSURE.addUnit(new GeneralUnit(1, "Pa"));
		UNITS_PRESSURE.setDefaultUnit(0);
		
		UNITS_RELATIVE = new UnitGroup();
		UNITS_RELATIVE.addUnit(new FixedPrecisionUnit("" + ZWSP, 0.01, 1.0));
		UNITS_RELATIVE.addUnit(new GeneralUnit(0.01, "%"));
		UNITS_RELATIVE.addUnit(new FixedPrecisionUnit("" + PERMILLE, 1, 0.001));
		UNITS_RELATIVE.setDefaultUnit(1);
		
		
		UNITS_ROUGHNESS = new UnitGroup();
		UNITS_ROUGHNESS.addUnit(new GeneralUnit(0.000001, MICRO + "m"));
		UNITS_ROUGHNESS.addUnit(new GeneralUnit(0.0000254, "mil"));
		UNITS_ROUGHNESS.addUnit(new GeneralUnit(1, "m"));
		UNITS_ROUGHNESS.setDefaultUnit(0);
		
		
		UNITS_COEFFICIENT = new UnitGroup();
		UNITS_COEFFICIENT.addUnit(new FixedPrecisionUnit("" + ZWSP, 0.01)); // zero-width space
		UNITS_COEFFICIENT.setDefaultUnit(0);
		
		
		// This is not used by OpenRocket, and not extensively tested:
		UNITS_FREQUENCY = new UnitGroup();
		UNITS_FREQUENCY.addUnit(new FrequencyUnit(.001, "mHz"));
		UNITS_FREQUENCY.addUnit(new FrequencyUnit(1, "Hz"));
		UNITS_FREQUENCY.addUnit(new FrequencyUnit(1000, "kHz"));
		UNITS_FREQUENCY.setDefaultUnit(1);
		
		
		HashMap<String, UnitGroup> map = new HashMap<String, UnitGroup>();
		map.put("NONE", UNITS_NONE);
		map.put("LENGTH", UNITS_LENGTH);
		map.put("ALL_LENGTHS", UNITS_ALL_LENGTHS);
		map.put("MOTOR_DIMENSIONS", UNITS_MOTOR_DIMENSIONS);
		map.put("DISTANCE", UNITS_DISTANCE);
		map.put("VELOCITY", UNITS_VELOCITY);
		map.put("ACCELERATION", UNITS_ACCELERATION);
		map.put("AREA", UNITS_AREA);
		map.put("STABILITY", UNITS_STABILITY);
		map.put("MASS", UNITS_MASS);
		map.put("INERTIA", UNITS_INERTIA);
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
		map.put("VOLTAGE", UNITS_VOLTAGE);
		map.put("CURRENT", UNITS_CURRENT);
		map.put("ENERGY", UNITS_ENERGY);
		map.put("POWER", UNITS_POWER);
		map.put("MOMENTUM", UNITS_MOMENTUM);
		map.put("FREQUENCY", UNITS_FREQUENCY);
		map.put("WINDSPEED", UNITS_WINDSPEED);
		
		UNITS = Collections.unmodifiableMap(map);
		
		HashMap<String, UnitGroup> simap = new HashMap<String, UnitGroup>();
		simap.put("m", UNITS_ALL_LENGTHS);
		simap.put("m^2", UNITS_AREA);
		simap.put("m/s", UNITS_VELOCITY);
		simap.put("m/s^2", UNITS_ACCELERATION);
		simap.put("kg", UNITS_MASS);
		simap.put("kg m^2", UNITS_INERTIA);
		simap.put("kg/m^3", UNITS_DENSITY_BULK);
		simap.put("N", UNITS_FORCE);
		simap.put("Ns", UNITS_IMPULSE);
		simap.put("s", UNITS_FLIGHT_TIME);
		simap.put("Pa", UNITS_PRESSURE);
		simap.put("V", UNITS_VOLTAGE);
		simap.put("A", UNITS_CURRENT);
		simap.put("J", UNITS_ENERGY);
		simap.put("W", UNITS_POWER);
		simap.put("kg m/s", UNITS_MOMENTUM);
		simap.put("Hz", UNITS_FREQUENCY);
		simap.put("K", UNITS_TEMPERATURE);
		
		SIUNITS = Collections.unmodifiableMap(simap);
	}
	
	public static void setDefaultMetricUnits() {
		UNITS_LENGTH.setDefaultUnit("cm");
		UNITS_MOTOR_DIMENSIONS.setDefaultUnit("mm");
		UNITS_DISTANCE.setDefaultUnit("m");
		UNITS_AREA.setDefaultUnit("cm" + SQUARED);
		UNITS_STABILITY.setDefaultUnit("cal");
		UNITS_VELOCITY.setDefaultUnit("m/s");
		UNITS_ACCELERATION.setDefaultUnit("m/s" + SQUARED);
		UNITS_MASS.setDefaultUnit("g");
		UNITS_INERTIA.setDefaultUnit("kg" + DOT + "m" + SQUARED);
		UNITS_ANGLE.setDefaultUnit("" + DEGREE);
		UNITS_DENSITY_BULK.setDefaultUnit("g/cm" + CUBED);
		UNITS_DENSITY_SURFACE.setDefaultUnit("g/m" + SQUARED);
		UNITS_DENSITY_LINE.setDefaultUnit("g/m");
		UNITS_FORCE.setDefaultUnit("N");
		UNITS_IMPULSE.setDefaultUnit("Ns");
		UNITS_TIME_STEP.setDefaultUnit("s");
		UNITS_FLIGHT_TIME.setDefaultUnit("s");
		UNITS_ROLL.setDefaultUnit("r/s");
		UNITS_TEMPERATURE.setDefaultUnit(DEGREE + "C");
		UNITS_WINDSPEED.setDefaultUnit("m/s");
		UNITS_PRESSURE.setDefaultUnit("mbar");
		UNITS_RELATIVE.setDefaultUnit("%");
		UNITS_ROUGHNESS.setDefaultUnit(MICRO + "m");
	}
	
	public static void setDefaultImperialUnits() {
		UNITS_LENGTH.setDefaultUnit("in");
		UNITS_MOTOR_DIMENSIONS.setDefaultUnit("in");
		UNITS_DISTANCE.setDefaultUnit("ft");
		UNITS_AREA.setDefaultUnit("in" + SQUARED);
		UNITS_STABILITY.setDefaultUnit("cal");
		UNITS_VELOCITY.setDefaultUnit("ft/s");
		UNITS_ACCELERATION.setDefaultUnit("ft/s" + SQUARED);
		UNITS_MASS.setDefaultUnit("oz");
		UNITS_INERTIA.setDefaultUnit("lb" + DOT + "ft" + SQUARED);
		UNITS_ANGLE.setDefaultUnit("" + DEGREE);
		UNITS_DENSITY_BULK.setDefaultUnit("oz/in" + CUBED);
		UNITS_DENSITY_SURFACE.setDefaultUnit("oz/ft" + SQUARED);
		UNITS_DENSITY_LINE.setDefaultUnit("oz/ft");
		UNITS_FORCE.setDefaultUnit("N");
		UNITS_IMPULSE.setDefaultUnit("Ns");
		UNITS_TIME_STEP.setDefaultUnit("s");
		UNITS_FLIGHT_TIME.setDefaultUnit("s");
		UNITS_ROLL.setDefaultUnit("r/s");
		UNITS_TEMPERATURE.setDefaultUnit(DEGREE + "F");
		UNITS_WINDSPEED.setDefaultUnit("mph");
		UNITS_PRESSURE.setDefaultUnit("mbar");
		UNITS_RELATIVE.setDefaultUnit("%");
		UNITS_ROUGHNESS.setDefaultUnit("mil");
	}
	
	
	/**
	 * Return a UnitGroup for stability units based on the rocket.
	 * 
	 * @param rocket	the rocket from which to calculate the caliber
	 * @return			the unit group
	 */
	public static UnitGroup stabilityUnits(Rocket rocket) {
		return new StabilityUnitGroup(rocket);
	}
	
	
	/**
	 * Return a UnitGroup for stability units based on the rocket configuration.
	 * 
	 * @param config	the rocket configuration from which to calculate the caliber
	 * @return			the unit group
	 */
	public static UnitGroup stabilityUnits(Configuration config) {
		return new StabilityUnitGroup(config);
	}
	
	
	/**
	 * Return a UnitGroup for stability units based on a constant caliber.
	 * 
	 * @param reference	the constant reference length
	 * @return			the unit group
	 */
	public static UnitGroup stabilityUnits(double reference) {
		return new StabilityUnitGroup(reference);
	}
	
	
	//////////////////////////////////////////////////////
	
	
	protected ArrayList<Unit> units = new ArrayList<Unit>();
	protected int defaultUnit = 0;
	
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
		if (n < 0 || n >= units.size()) {
			throw new IllegalArgumentException("index out of range: " + n);
		}
		defaultUnit = n;
	}
	
	public Unit getSIUnit() {
		for (Unit u : units) {
			if (u.multiplier == 1) {
				return u;
			}
		}
		return UNITS_NONE.getDefaultUnit();
	}
	
	/**
	 * Find a unit by approximate unit name.  Only letters and (ordinary) numbers are
	 * considered in the matching.  This method is mainly means for testing, allowing
	 * a simple means to obtain a particular unit.
	 * 
	 * @param str	the unit name.
	 * @return		the corresponding unit, or <code>null</code> if not found.
	 */
	public Unit findApproximate(String str) {
		str = str.replaceAll("\\W", "").trim();
		for (Unit u : units) {
			String name = u.getUnit().replaceAll("\\W", "").trim();
			if (str.equalsIgnoreCase(name))
				return u;
		}
		return null;
	}
	
	/**
	 * Set the default unit based on the unit name.  Throws an exception if a
	 * unit with the provided name is not available.
	 * 
	 * @param   name	the unit name.
	 * @throws  IllegalArgumentException	if the corresponding unit is not found in the group.
	 */
	public void setDefaultUnit(String name) throws IllegalArgumentException {
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getUnit().equals(name)) {
				setDefaultUnit(i);
				return;
			}
		}
		throw new IllegalArgumentException("name=" + name);
	}
	
	public Unit getUnit(String name) throws IllegalArgumentException {
		for (int i = 0; i < units.size(); i++) {
			if (units.get(i).getUnit().equals(name)) {
				return units.get(i);
			}
		}
		throw new IllegalArgumentException("name=" + name);
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
	
	public boolean contains(Unit u) {
		return units.contains(u);
	}
	
	public Unit[] getUnits() {
		return units.toArray(new Unit[0]);
	}
	
	/**
	 * Return the value in SI units from the default unit of this group.
	 * It is the same as calling <code>getDefaultUnit().fromUnit(value)</code>
	 * 
	 * @param value the default unit value to convert
	 * @return the value in SI units.
	 * @see Unit#fromUnit(double)
	 */
	public double fromUnit(double value) {
		return this.getDefaultUnit().fromUnit(value);
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
	
	
	
	
	
	/**
	 * Creates a new Value object with the specified value and the default unit of this group.
	 * 
	 * @param value	the value to set.
	 * @return		a new Value object.
	 */
	public Value toValue(double value) {
		return this.getDefaultUnit().toValue(value);
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + this.getSIUnit().toString();
	}
	
	@Override
	public boolean equals(Object o) {
		UnitGroup u = (UnitGroup) o;
		int size = units.size();
		if (size != u.units.size()) {
			return false;
		}
		
		for (int i = 0; i < size; i++) {
			if (!units.get(i).equals(u.units.get(i))) {
				return false;
			}
		}
		
		return true;
		
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
			for (i = 0; i < units.size(); i++) {
				Unit u = units.get(i);
				if (unit.equalsIgnoreCase(u.getUnit())) {
					value = u.fromUnit(value);
					break;
				}
			}
			if (i >= units.size()) {
				throw new NumberFormatException("unknown unit " + unit);
			}
		}
		
		return value;
	}
	
	
	///////////////////////////
	
	
	@Override
	public int hashCode() {
		int code = 0;
		for (Unit u : units) {
			code = code + u.hashCode();
		}
		return code;
	}
	
	/**
	 * A private class that switches the CaliberUnit to a rocket-specific CaliberUnit.
	 * All other methods are passed through to UNITS_STABILITY.
	 */
	private static class StabilityUnitGroup extends UnitGroup {
		
		public StabilityUnitGroup(double ref) {
			this(new CaliberUnit(ref));
		}
		
		public StabilityUnitGroup(Rocket rocket) {
			this(new CaliberUnit(rocket));
		}
		
		public StabilityUnitGroup(Configuration config) {
			this(new CaliberUnit(config));
		}
		
		private StabilityUnitGroup(CaliberUnit caliberUnit) {
			this.units.addAll(UnitGroup.UNITS_STABILITY.units);
			this.defaultUnit = UnitGroup.UNITS_STABILITY.defaultUnit;
			for (int i = 0; i < units.size(); i++) {
				if (units.get(i) instanceof CaliberUnit) {
					units.set(i, caliberUnit);
				}
			}
		}
		
		
		@Override
		public void setDefaultUnit(int n) {
			super.setDefaultUnit(n);
			UNITS_STABILITY.setDefaultUnit(n);
		}
	}
}
