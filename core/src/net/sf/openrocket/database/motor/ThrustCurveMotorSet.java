package net.sf.openrocket.database.motor;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.motor.DesignationComparator;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.Motor.Type;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.MathUtil;

public class ThrustCurveMotorSet implements Comparable<ThrustCurveMotorSet> {
	
	//  Comparators:
	private static final Collator COLLATOR = Collator.getInstance(Locale.US);
	static {
		COLLATOR.setStrength(Collator.PRIMARY);
	}
	private static final DesignationComparator DESIGNATION_COMPARATOR = new DesignationComparator();
	private static final ThrustCurveMotorComparator comparator = new ThrustCurveMotorComparator();
	
	
	
	private final ArrayList<ThrustCurveMotor> motors = new ArrayList<ThrustCurveMotor>();
	private final Map<ThrustCurveMotor, String> digestMap =
			new IdentityHashMap<ThrustCurveMotor, String>();
	
	private final List<Double> delays = new ArrayList<Double>();
	
	private Manufacturer manufacturer = null;
	private String designation = null;
	private String simplifiedDesignation = null;
	private double diameter = -1;
	private double length = -1;
	private long totalImpulse = 0;
	private Motor.Type type = Motor.Type.UNKNOWN;
	
	
	
	public void addMotor(ThrustCurveMotor motor) {
		
		// Check for first insertion
		if (motors.isEmpty()) {
			manufacturer = motor.getManufacturer();
			designation = motor.getDesignation();
			simplifiedDesignation = simplifyDesignation(designation);
			diameter = motor.getDiameter();
			length = motor.getLength();
			totalImpulse = Math.round((motor.getTotalImpulseEstimate()));
		}
		
		// Verify that the motor can be added
		if (!matches(motor)) {
			throw new IllegalArgumentException("Motor does not match the set:" +
					" manufacturer=" + manufacturer +
					" designation=" + designation +
					" diameter=" + diameter +
					" length=" + length +
					" set_size=" + motors.size() +
					" motor=" + motor);
		}
		
		// Update the type if now known
		if (type == Motor.Type.UNKNOWN) {
			type = motor.getMotorType();
			// Add "Plugged" option if hybrid
			if (type == Motor.Type.HYBRID) {
				if (!delays.contains(Motor.PLUGGED)) {
					delays.add(Motor.PLUGGED);
				}
			}
		}
		
		// Change the simplified designation if necessary
		if (!designation.equalsIgnoreCase(motor.getDesignation().trim())) {
			designation = simplifiedDesignation;
		}
		
		// Add the standard delays
		for (double d : motor.getStandardDelays()) {
			d = Math.rint(d);
			if (!delays.contains(d)) {
				delays.add(d);
			}
		}
		Collections.sort(delays);
		
		
		// Check whether to add as new motor or overwrite existing
		final String digest = motor.getDigest();
		for (int index = 0; index < motors.size(); index++) {
			Motor m = motors.get(index);
			
			if (digest.equals(digestMap.get(m)) &&
					motor.getDesignation().equals(m.getDesignation())) {
				
				// Match found, check which one to keep (or both) based on comment
				String newCmt = motor.getDescription().replaceAll("\\s+", " ").trim();
				String oldCmt = m.getDescription().replaceAll("\\s+", " ").trim();
				
				if (newCmt.length() == 0 || newCmt.equals(oldCmt)) {
					// Do not replace and do not add
					return;
				} else if (oldCmt.length() == 0) {
					// Replace existing motor
					motors.set(index, motor);
					digestMap.put(motor, digest);
					return;
				}
				// else continue search and add both
				
			}
		}
		
		// Motor not present, add it
		motors.add(motor);
		digestMap.put(motor, digest);
		Collections.sort(motors, comparator);
		
	}
	
	
	public boolean matches(ThrustCurveMotor m) {
		if (motors.isEmpty())
			return true;
		
		if (manufacturer != m.getManufacturer())
			return false;
		
		if (!MathUtil.equals(diameter, m.getDiameter()))
			return false;
		
		if (!MathUtil.equals(length, m.getLength()))
			return false;
		
		if ((type != Type.UNKNOWN) && (m.getMotorType() != Type.UNKNOWN) &&
				(type != m.getMotorType())) {
			return false;
		}
		
		if (!simplifiedDesignation.equalsIgnoreCase(simplifyDesignation(m.getDesignation())))
			return false;
		
		return true;
	}
	
	
	public List<ThrustCurveMotor> getMotors() {
		return motors.clone();
	}
	
	
	public int getMotorCount() {
		return motors.size();
	}
	
	
	/**
	 * Return the standard delays applicable to this motor type.  This is a union of
	 * all the delays of the motors included in this set.
	 * @return the delays
	 */
	public List<Double> getDelays() {
		return Collections.unmodifiableList(delays);
	}
	
	
	/**
	 * Return the manufacturer of this motor type.
	 * @return the manufacturer
	 */
	public Manufacturer getManufacturer() {
		return manufacturer;
	}
	
	
	/**
	 * Return the designation of this motor type.  This is either the exact or simplified
	 * designation, depending on what motors have been added.
	 * @return the designation
	 */
	public String getDesignation() {
		return designation;
	}
	
	
	/**
	 * Return the diameter of this motor type.
	 * @return the diameter
	 */
	public double getDiameter() {
		return diameter;
	}
	
	
	/**
	 * Return the length of this motor type.
	 * @return the length
	 */
	public double getLength() {
		return length;
	}
	
	
	/**
	 * Return the type of this motor type.  If any of the added motors has a type different
	 * from UNKNOWN, then that type will be returned.
	 * @return the type
	 */
	public Motor.Type getType() {
		return type;
	}
	
	/**
	 * Return the estimated total impulse for this motor type.
	 * @return estimated total impulse
	 */
	public long getTotalImpuse() {
		return totalImpulse;
	}
	
	
	@Override
	public String toString() {
		return "ThrustCurveMotorSet[" + manufacturer + " " + designation +
				", type=" + type + ", count=" + motors.size() + "]";
	}
	
	
	
	
	private static final Pattern SIMPLIFY_PATTERN = Pattern.compile("^[0-9]*[ -]*([A-Z][0-9]+).*");
	
	/**
	 * Simplify a motor designation, if possible.  This attempts to reduce the designation
	 * into a simple letter + number notation for the impulse class and average thrust.
	 * 
	 * @param str	the designation to simplify
	 * @return		the simplified designation, or the string itself if the format was not detected
	 */
	public static String simplifyDesignation(String str) {
		str = str.trim();
		Matcher m = SIMPLIFY_PATTERN.matcher(str);
		if (m.matches()) {
			return m.group(1);
		} else {
			return str.replaceAll("\\s", "");
		}
	}
	
	
	/**
	 * Comparator for deciding in which order to display matching motors.
	 */
	private static class ThrustCurveMotorComparator implements Comparator<ThrustCurveMotor> {
		
		@Override
		public int compare(ThrustCurveMotor o1, ThrustCurveMotor o2) {
			// 1. Designation
			if (!o1.getDesignation().equals(o2.getDesignation())) {
				return o1.getDesignation().compareTo(o2.getDesignation());
			}
			
			// 2. Number of data points (more is better)
			if (o1.getTimePoints().length != o2.getTimePoints().length) {
				return o2.getTimePoints().length - o1.getTimePoints().length;
			}
			
			// 3. Comment length (longer is better)
			return o2.getDescription().length() - o1.getDescription().length();
		}
		
	}
	
	
	@Override
	public int compareTo(ThrustCurveMotorSet other) {
		
		int value;
		
		// 1. Manufacturer
		value = COLLATOR.compare(this.manufacturer.getDisplayName(),
				other.manufacturer.getDisplayName());
		if (value != 0)
			return value;
		
		// 2. Designation
		value = DESIGNATION_COMPARATOR.compare(this.designation, other.designation);
		if (value != 0)
			return value;
		
		// 3. Diameter
		value = (int) ((this.diameter - other.diameter) * 1000000);
		if (value != 0)
			return value;
		
		// 4. Length
		value = (int) ((this.length - other.length) * 1000000);
		return value;
		
	}
	
}
