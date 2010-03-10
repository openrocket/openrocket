package net.sf.openrocket.motor;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;



/**
 * Abstract base class for motors.  The methods that must be implemented are
 * {@link #getTotalTime()}, {@link #getThrust(double)} and {@link #getCG(double)}.
 * Additionally the method {@link #getMaxThrust()} may be overridden for efficiency.
 * <p>
 * 
 * NOTE:  The current implementation of {@link #getAverageTime()} and 
 * {@link #getAverageThrust()} assume that the class is immutable!
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class Motor implements Comparable<Motor> {
	
	/**
	 * Enum of rocket motor types.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	public enum Type {
		SINGLE("Single-use", "Single-use solid propellant motor"), 
		RELOAD("Reloadable", "Reloadable solid propellant motor"), 
		HYBRID("Hybrid", "Hybrid rocket motor engine"), 
		UNKNOWN("Unknown", "Unknown motor type");
		
		private final String name;
		private final String description;
		
		Type(String name, String description) {
			this.name = name;
			this.description = description;
		}

		/**
		 * Return a short name of this motor type.
		 * @return  a short name of the motor type.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Return a long description of this motor type.
		 * @return  a description of the motor type.
		 */
		public String getDescription() {
			return description;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	
	/**
	 * Ejection charge delay value signifying a "plugged" motor with no ejection charge.
	 * The value is that of <code>Double.POSITIVE_INFINITY</code>.
	 */
	public static final double PLUGGED = Double.POSITIVE_INFINITY;
	
	
	/**
	 * Below what portion of maximum thrust is the motor chosen to be off when
	 * calculating average thrust and burn time.  NFPA 1125 defines the "official"
	 * burn time to be the time which the motor produces over 5% of its maximum thrust.
	 */
	public static final double AVERAGE_MARGINAL = 0.05;
	
	/* All data is cached, so divisions can be very tight. */
	private static final int DIVISIONS = 1000;

	
	//  Comparators:
	private static final Collator COLLATOR = Collator.getInstance(Locale.US);
	static {
		COLLATOR.setStrength(Collator.PRIMARY);
	}
	private static DesignationComparator DESIGNATION_COMPARATOR = new DesignationComparator();
	
	
	
	
	private final Manufacturer manufacturer;
	private final String designation;
	private final String description;
	private final Type motorType;
	private final String digest;
	
	private final double[] delays;
	
	private final double diameter;
	private final double length;
	
	/* Cached data */
	private double maxThrust = -1;
	private double avgTime = -1;
	private double avgThrust = -1;
	private double totalImpulse = -1;
	
	
	
	/**
	 * Sole constructor.  None of the parameters may be <code>null</code>.
	 * 
	 * @param manufacturer  the manufacturer of the motor.
	 * @param designation   the motor designation.
	 * @param description   further description, including any comments on the origin
	 * 						of the thrust curve.
	 * @param delays   		an array of the standard ejection charge delays.  A plugged
	 * 						motor (no ejection charge) is specified by a delay of
	 * 						{@link #PLUGGED} (<code>Double.POSITIVE_INFINITY</code>).
	 * @param diameter		maximum diameter of the motor
	 * @param length		length of the motor
	 */
	protected Motor(Manufacturer manufacturer, String designation, String description, 
			Type type, double[] delays, double diameter, double length, String digest) {

		if (manufacturer == null || designation == null || description == null ||
				type == null || delays == null) {
			throw new IllegalArgumentException("Parameters cannot be null.");
		}
		
		this.manufacturer = manufacturer;
		this.designation = designation;
		this.description = description.trim();
		this.motorType = type;
		this.delays = delays.clone();
		Arrays.sort(this.delays);
		this.diameter = diameter;
		this.length = length;
		this.digest = digest;
	}


	
	/**
	 * Return the total burn time of the motor.  The method {@link #getThrust(double)}
	 * must return zero for time values greater than the return value.
	 * 
	 * @return  the total burn time of the motor.
	 */
	public abstract double getTotalTime();


	/**
	 * Return the thrust of the motor at the specified time.
	 * 
	 * @param time  time since the ignition of the motor.
	 * @return      the thrust at the specified time.
	 */
	public abstract double getThrust(double time);
	
	
	/**
	 * Return the average thrust of the motor between times t1 and t2.
	 * 
	 * @param t1	starting time since the ignition of the motor.
	 * @param t2	end time since the ignition of the motor.
	 * @return		the average thrust during the time period.
	 */
	/* TODO: MEDIUM: Implement better method in subclass */
	public double getThrust(double t1, double t2) {
		double f = 0;
		f += getThrust(t1);
		f += getThrust(0.8*t1 + 0.2*t2);
		f += getThrust(0.6*t1 + 0.4*t2);
		f += getThrust(0.4*t1 + 0.6*t2);
		f += getThrust(0.2*t1 + 0.8*t2);
		f += getThrust(t2);
		return f/6;
	}

	
	/**
	 * Return the mass and CG of the motor at the specified time.
	 * 
	 * @param time  time since the ignition of the motor.
	 * @return      the mass and CG of the motor.
	 */
	public abstract Coordinate getCG(double time);
	
	
	
	/**
	 * Return the mass of the motor at the specified time.  The original mass
	 * of the motor can be queried by <code>getMass(0)</code> and the burnt mass
	 * by <code>getMass(Double.MAX_VALUE)</code>.
	 * 
	 * @param time  time since the ignition of the motor.
	 * @return      the mass of the motor.
	 */
	public double getMass(double time) {
		return getCG(time).weight;
	}
	
	
	/**
	 * Return the longitudal moment of inertia of the motor at the specified time.
	 * This default method assumes that the mass of the motor is evenly distributed
	 * in a cylinder with the diameter and length of the motor.
	 * 
	 * @param time	time since the ignition of the motor.
	 * @return		the longitudal moment of inertia of the motor.
	 */
	public double getLongitudalInertia(double time) {
		return getMass(time) * (3.0*MathUtil.pow2(diameter/2) + MathUtil.pow2(length))/12;
	}
	
	
	
	/**
	 * Return the rotational moment of inertia of the motor at the specified time.
	 * This default method assumes that the mass of the motor is evenly distributed
	 * in a cylinder with the diameter and length of the motor.
	 * 
	 * @param time	time since the ignition of the motor.
	 * @return		the rotational moment of inertia of the motor.
	 */
	public double getRotationalInertia(double time) {
		return getMass(time) * MathUtil.pow2(diameter) / 8;
	}
	
	
	
	
	/**
	 * Return the maximum thrust.  This implementation slices through the thrust curve
	 * searching for the maximum thrust.  Subclasses may wish to override this with a
	 * more efficient method.
	 * 
	 * @return  the maximum thrust of the motor
	 */
	public double getMaxThrust() {
		if (maxThrust < 0) {
			double time = getTotalTime();
			maxThrust = 0;
			
			for (int i=0; i < DIVISIONS; i++) {
				double t = time * i / DIVISIONS;
				double thrust = getThrust(t);
				
				if (thrust > maxThrust)
					maxThrust = thrust;
			}
		}
		return maxThrust;
	}
	
	
	/**
	 * Return the time used in calculating the average thrust.  The time is the
	 * length of time that the motor produces over 5% ({@link #AVERAGE_MARGINAL})
	 * of its maximum thrust.
	 * 
	 * @return  the nominal burn time.
	 */
	public double getAverageTime() {
		// Compute average time lazily
		if (avgTime < 0) {
			double max = getMaxThrust();
			double time = getTotalTime();
			
			avgTime = 0;
			for (int i=0; i <= DIVISIONS; i++) {
				double t = i*time/DIVISIONS;
				if (getThrust(t) >= max*AVERAGE_MARGINAL)
					avgTime++;
			}
			avgTime *= time/(DIVISIONS+1);
			
			if (Double.isNaN(avgTime))
				throw new BugException("Calculated avg. time is NaN for motor "+this);

		}
		return avgTime;
	}
	
	
	/**
	 * Return the calculated average thrust during the time the motor produces
	 * over 5% ({@link #AVERAGE_MARGINAL}) of its thrust.
	 * 
	 * @return  the nominal average thrust.
	 */
	public double getAverageThrust() {
		// Compute average thrust lazily
		if (avgThrust < 0) {
			double max = getMaxThrust();
			double time = getTotalTime();
			int points = 0;
			
			avgThrust = 0;
			for (int i=0; i <= DIVISIONS; i++) {
				double t = i*time/DIVISIONS;
				double thrust = getThrust(t);
				if (thrust >= max*AVERAGE_MARGINAL) {
					avgThrust += thrust;
					points++;
				}
			}
			if (points > 0)
				avgThrust /= points;
			
			if (Double.isNaN(avgThrust))
				throw new BugException("Calculated average thrust is NaN for motor "+this);
		}
		return avgThrust;
	}
	
	
	/**
	 * Return the total impulse of the motor.  This is calculated from the entire
	 * burn time, and therefore may differ from the value of {@link #getAverageTime()}
	 * and {@link #getAverageThrust()} multiplied together.
	 * 
	 * @return  the total impulse of the motor.
	 */
	public double getTotalImpulse() {
		// Compute total impulse lazily
		if (totalImpulse < 0) {
			double time = getTotalTime();
			double f0, t0;
			
			totalImpulse = 0;
			t0 = 0;
			f0 = getThrust(0);
			for (int i=1; i < DIVISIONS; i++) {
				double t1 = time * i / DIVISIONS;
				double f1 = getThrust(t1); 
				totalImpulse += 0.5*(f0+f1)*(t1-t0);
				t0 = t1;
				f0 = f1;
			}
			
			if (Double.isNaN(totalImpulse))
				throw new BugException("Calculated total impulse is NaN for motor "+this);
		}
		return totalImpulse;
	}
	

	/**
	 * Return the manufacturer of the motor.
	 * 
	 * @return the manufacturer
	 */
	public Manufacturer getManufacturer() {
		return manufacturer;
	}
	
	/**
	 * Return the designation of the motor.
	 * 
	 * @return the designation
	 */
	public String getDesignation() {
		return designation;
	}
	
	/**
	 * Return the designation of the motor, including a delay.
	 * 
	 * @param delay  the delay of the motor.
	 * @return		 designation with delay.
	 */
	public String getDesignation(double delay) {
		return getDesignation() + "-" + getDelayString(delay);
	}

	
	/**
	 * Return extra description for the motor.  This may include for example 
	 * comments on the source of the thrust curve.  The returned <code>String</code>
	 * may include new-lines.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	
	/**
	 * Return the motor type.
	 * 
	 * @return  the motorType
	 */
	public Type getMotorType() {
		return motorType;
	}



	/**
	 * Return the standard ejection charge delays for the motor.  "Plugged" motors
	 * with no ejection charge are signified by the value {@link #PLUGGED}
	 * (<code>Double.POSITIVE_INFINITY</code>).
	 * 
	 * @return  the list of standard ejection charge delays, which may be empty.
	 */
	public double[] getStandardDelays() {
		return delays.clone();
	}

	/**
	 * Return the maximum diameter of the motor.
	 * 
	 * @return the diameter
	 */
	public double getDiameter() {
		return diameter;
	}

	/**
	 * Return the length of the motor.  This should be a "characteristic" length,
	 * and the exact definition may depend on the motor type.  Typically this should
	 * be the length from the bottom of the motor to the end of the maximum diameter
	 * portion, ignoring any smaller ejection charge compartments.
	 * 
	 * @return the length
	 */
	public double getLength() {
		return length;
	}
	
	
	/**
	 * Return a digest string of this motor.  This digest should be computed from all
	 * flight-affecting data.  For example for thrust curve motors the thrust curve
	 * should be digested using suitable precision.  The intention is that the combination
	 * of motor type, manufacturer, designation, diameter, length and digest uniquely
	 * identify any particular motor data file.
	 * 
	 * @return	a string digest of this motor (0-60 chars)
	 */
	public String getDigestString() {
		return digest;
	}
	
	
	public boolean similar(Motor other) {
		// TODO: HIGH:  Merge with equals / cleanup

		// Tests manufacturer, designation, diameter and length
		if (this.compareTo(other) != 0)
			return false;
		
		// Compare total time
		if (Math.abs(this.getTotalTime() - other.getTotalTime()) > 0.5) {
			return false;
		}
		
		// Consider type only if neither is of unknown type
		if ((this.motorType != Type.UNKNOWN) && (other.motorType != Type.UNKNOWN) &&
				(this.motorType != other.motorType)) {
			return false;
		}

		// Compare delays if both have some delays defined
		if (this.delays.length != 0 && other.delays.length != 0) {
			if (this.delays.length != other.delays.length) {
				return false;
			}
			for (int i=0; i < delays.length; i++) {
				// INF - INF == NaN, which produces false when compared
				if (Math.abs(this.delays[i] - other.delays[i]) > 0.5) {
					return false;
				}
			}
		}
		
		double time = getTotalTime();
		for (int i=0; i < 10; i++) {
			double t = time * i/10;
			if (Math.abs(this.getThrust(t) - other.getThrust(t)) > 1) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Compares two <code>Motor</code> objects.  The motors are considered equal
	 * if they have identical manufacturers, designations and types, near-identical
	 * dimensions, burn times and delays and near-identical thrust curves
	 * (sampled at 10 equidistant points).
	 * <p>
	 * The comment field is ignored when comparing equality.
	 * <p>
	 * TODO: HIGH: Check for identical contents instead
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Motor))
			return false;
		
		Motor other = (Motor) o;
		
		// Tests manufacturer, designation, diameter and length
		if (this.compareTo(other) != 0)
			return false;
		
		// Compare total time
		if (Math.abs(this.getTotalTime() - other.getTotalTime()) > 0.5) {
			return false;
		}
		
		// Consider type only if neither is of unknown type
		if ((this.motorType != Type.UNKNOWN) && (other.motorType != Type.UNKNOWN) &&
				(this.motorType != other.motorType)) {
			return false;
		}

		// Compare delays
		if (this.delays.length != other.delays.length) {
			return false;
		}
		for (int i=0; i < delays.length; i++) {
			// INF - INF == NaN, which produces false when compared
			if (Math.abs(this.delays[i] - other.delays[i]) > 0.5) {
				return false;
			}
		}
		
		double time = getTotalTime();
		for (int i=0; i < 10; i++) {
			double t = time * i/10;
			if (Math.abs(this.getThrust(t) - other.getThrust(t)) > 1) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * A <code>hashCode</code> method compatible with the <code>equals</code>
	 * method.
	 */
	@Override
	public int hashCode() {
		return (manufacturer.hashCode() + designation.hashCode() + 
				((int)(length*1000)) + ((int)(diameter*1000)));
	}
	
	
	
	@Override
	public String toString() {
		return manufacturer + " " + designation;
	}
	
	
	//////////  Static methods

	
	/**
	 * Return a String representation of a delay time.  If the delay is {@link #PLUGGED},
	 * returns "P".
	 *  
	 * @param delay		the delay time.
	 * @return			the <code>String</code> representation.
	 */
	public static String getDelayString(double delay) {
		return getDelayString(delay,"P");
	}
	
	/**
	 * Return a String representation of a delay time.  If the delay is {@link #PLUGGED},
	 * <code>plugged</code> is returned.
	 *   
	 * @param delay  	the delay time.
	 * @param plugged  	the return value if there is no ejection charge.
	 * @return			the String representation.
	 */
	public static String getDelayString(double delay, String plugged) {
		if (delay == PLUGGED)
			return plugged;
		delay = Math.rint(delay*10)/10;
		if (MathUtil.equals(delay, Math.rint(delay)))
			return "" + ((int)delay);
		return "" + delay;
	}
	

	
	
	////////////  Comparation
	
	

	@Override
	public int compareTo(Motor other) {
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
		value = (int)((this.diameter - other.diameter)*1000000);
		if (value != 0)
			return value;
				
		// 4. Length
		value = (int)((this.length - other.length)*1000000);
		if (value != 0)
			return value;
		
		// 5. Total impulse
		value = (int)((this.getTotalImpulse() - other.getTotalImpulse())*1000);
		return value;
	}
	
	
	
	public static Comparator<String> getDesignationComparator() {
		return DESIGNATION_COMPARATOR;
	}
	
	
	/**
	 * Compares two motors by their designations.  The motors are ordered first
	 * by their motor class, second by their average thrust and lastly by any
	 * extra modifiers at the end of the designation.
	 * 
	 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
	 */
	private static class DesignationComparator implements Comparator<String> {
		private Pattern pattern = 
			Pattern.compile("^([0-9][0-9]+|1/([1-8]))?([a-zA-Z])([0-9]+)(.*?)$");
		
		@Override
		public int compare(String o1, String o2) {
			int value;
			Matcher m1, m2;
			
			m1 = pattern.matcher(o1);
			m2 = pattern.matcher(o2);
			
			if (m1.find() && m2.find()) {

				String o1Class = m1.group(3);
				int o1Thrust = Integer.parseInt(m1.group(4));
				String o1Extra = m1.group(5);
				
				String o2Class = m2.group(3);
				int o2Thrust = Integer.parseInt(m2.group(4));
				String o2Extra = m2.group(5);
				
				// 1. Motor class
				if (o1Class.equalsIgnoreCase("A") && o2Class.equalsIgnoreCase("A")) {
					//  1/2A and 1/4A comparison
					String sub1 = m1.group(2);
					String sub2 = m2.group(2);

					if (sub1 != null || sub2 != null) {
						if (sub1 == null)
							sub1 = "1";
						if (sub2 == null)
							sub2 = "1";
						value = -COLLATOR.compare(sub1,sub2);
						if (value != 0)
							return value;
					}
				}
				value = COLLATOR.compare(o1Class,o2Class);
				if (value != 0)
					return value;
				
				// 2. Average thrust
				if (o1Thrust != o2Thrust)
					return o1Thrust - o2Thrust;
				
				// 3. Extra modifier
				return COLLATOR.compare(o1Extra, o2Extra);
				
			} else {
				
				// Not understandable designation, simply compare strings
				return COLLATOR.compare(o1, o2);
			}
		}
	}
}
