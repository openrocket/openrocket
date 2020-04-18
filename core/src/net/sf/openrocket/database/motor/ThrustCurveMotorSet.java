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
	private final Map<ThrustCurveMotor, String> digestMap = new IdentityHashMap<ThrustCurveMotor, String>();
	
	private final List<Double> delays = new ArrayList<Double>();
	
	private Manufacturer manufacturer = null;
	private String designation = null;
	private String simplifiedDesignation = null;
	private double diameter = -1;
	private double length = -1;
	private long totalImpulse = 0;
	private Motor.Type type = Motor.Type.UNKNOWN;
	private String caseInfo = null;
	private boolean available = true;
	
	/**
	 * adds a motor into the set, 
	 * uses digest and designation to determinate if a motor is present or not
	 * @param motor	the motor to be added
	 */
	public void addMotor(ThrustCurveMotor motor) {
		
		checkFirstInsertion(motor);
		verifyMotor(motor);
		updateType(motor);
		checkChangeSimplifiedDesignation(motor);
		addStandardDelays(motor);
		if(!checkMotorOverwrite(motor)){
			motors.add(motor);
			digestMap.put(motor, motor.getDigest());
			Collections.sort(motors, comparator);
		}	
		
	}

	/**
	 * checks whether a motor is present, overwriting it if
	 * @param motor	the motor to be checked
	 * @return	if there was an overwrite or not, returns true if all is equals
	 */
	private boolean checkMotorOverwrite(ThrustCurveMotor motor) {
		final String digest = motor.getDigest();
		for (int index = 0; index < motors.size(); index++) {
			Motor m = motors.get(index);
			
			if (isMotorPresent(motor, digest, m)) {
					
				// Match found, check which one to keep (or both) based on comment
				String newCmt = getFormattedDescription(motor);
				String oldCmt = getFormattedDescription(m);
				if (isNewDescriptionIrrelevant(newCmt, oldCmt)) {
					return true;
				} else if (oldCmt.length() == 0) {
					motors.set(index, motor);
					digestMap.put(motor, digest);
					return true;
				}
				// else continue search and add both
				
			}
		}
		return false;
	}

	/**
	 * checks if a motor is in the maps
	 * @param motor		the motor to be checked
	 * @param digest	the digest of the motor
	 * @param m			the current motor being checked with
	 * @return	wheter the motor is or no
	 */
	private boolean isMotorPresent(ThrustCurveMotor motor, final String digest, Motor m) {
		return digest.equals(digestMap.get(m)) &&
				motor.getDesignation().equals(m.getDesignation());
	}
	
	/**
	 * get a description from the motor
	 * @param motor	the motor
	 * @return		the description of the motor
	 */
	private String getFormattedDescription(Motor motor) {
		return motor.getDescription().replaceAll("\\s+", " ").trim();
	}


	/**
	 * checks if the new commit message is empty or equals to the old commit
	 * @param newCmt	the new commit message
	 * @param oldCmt	the old commit message
	 * @return	whether the new commit is empty or equals to the old commit
	 */
	private boolean isNewDescriptionIrrelevant(String newCmt, String oldCmt) {
		return newCmt.length() == 0 || newCmt.equals(oldCmt);
	}


	/**
	 * adds the standard delay if aplicable
	 * @param motor	the motor to be considered
	 */
	private void addStandardDelays(ThrustCurveMotor motor) {
		// Add the standard delays
		for (double d : motor.getStandardDelays()) {
			d = Math.rint(d);
			if (!delays.contains(d)) {
				delays.add(d);
			}
		}
		Collections.sort(delays);
	}


	/**
	 * checks if simplified designation should be changed with the given motor
	 * @param motor	the motor to be checked with
	 */
	private void checkChangeSimplifiedDesignation(ThrustCurveMotor motor) {
		// Change the simplified designation if necessary
		if (!designation.equalsIgnoreCase(motor.getDesignation().trim())) {
			designation = simplifiedDesignation;
		}
		
		if (caseInfo == null) {
			caseInfo = motor.getCaseInfo();
		}
	}


	/**
	 * checks if the cached type should be changed with the given motor
	 * if it's hybrid, delays will be added
	 * @param motor	the motor to be checked with
	 */
	private void updateType(ThrustCurveMotor motor) {
		// Update the type if now known
		if (type == Motor.Type.UNKNOWN) {
			type = motor.getMotorType();
			// Add "Plugged" option if hybrid
			if (type == Motor.Type.HYBRID) {
				if (!delays.contains(Motor.PLUGGED_DELAY)) {
					delays.add(Motor.PLUGGED_DELAY);
				}
			}
		}
	}


	/**
	 * verifies if a motor is valid for this set
	 * @param motor	the motor to be checked
	 */
	private void verifyMotor(ThrustCurveMotor motor) {
		if (!matches(motor)) {
			throw new IllegalArgumentException("Motor does not match the set:" +
					" manufacturer=" + manufacturer +
					" designation=" + designation +
					" diameter=" + diameter +
					" length=" + length +
					" set_size=" + motors.size() +
					" motor=" + motor);
		}
	}


	/**
	 * checks if the given motor is the first one
	 * the ifrst motor inserted is what will difine the rest of the motors in the set
	 * @param motor	the motor to be checked
	 */
	private void checkFirstInsertion(ThrustCurveMotor motor) {
		if (motors.isEmpty()) {
			manufacturer = motor.getManufacturer();
			designation = motor.getDesignation();
			simplifiedDesignation = simplifyDesignation(designation);
			diameter = motor.getDiameter();
			length = motor.getLength();
			totalImpulse = Math.round((motor.getTotalImpulseEstimate()));
			caseInfo = motor.getCaseInfo();
			available = motor.isAvailable();
		}
	}
	
	/**
	 * Checks if a motor can be added with the set
	 * A set contains motors of same manufacturer, diameter, length and type
	 * @param m	the motor to be checked with
	 * @return	if the motor passed the test or not
	 */
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
		
		if (caseInfo != null && !caseInfo.equalsIgnoreCase(m.getCaseInfo()))
			return false;
			
		return true;
	}
	
	/**
	 * returns a new list with the stored motors
	 * @return	list 
	 */
	public List<ThrustCurveMotor> getMotors() {
		return motors.clone();
	}
	
	
	/**
	 * 
	 * @return number of motor in the set
	 */
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
	public long getTotalImpulse() {
		return totalImpulse;
	}
	
	/**
	 * returns the case info of the motor
	 * @return	the motor's case information
	 */
	public String getCaseInfo() {
		return caseInfo;
	}
	
	/**
	 * checks if the motor is available for other calculations
	 * @return	if the motor is available
	 */
	public boolean isAvailable() {
		return available;
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
			if (o1.getSampleSize() != o2.getSampleSize()) {
				return o2.getSampleSize() - o1.getSampleSize();
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
