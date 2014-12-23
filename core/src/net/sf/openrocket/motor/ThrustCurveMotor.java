package net.sf.openrocket.motor;

import java.io.Serializable;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.util.ArrayUtils;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;
import net.sf.openrocket.util.MathUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ThrustCurveMotor implements Motor, Comparable<ThrustCurveMotor>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1490333207132694479L;
	
	private static final Logger log = LoggerFactory.getLogger(ThrustCurveMotor.class);
	
	public static final double MAX_THRUST = 10e6;
	
	//  Comparators:
	private static final Collator COLLATOR = Collator.getInstance(Locale.US);
	static {
		COLLATOR.setStrength(Collator.PRIMARY);
	}
	private static final DesignationComparator DESIGNATION_COMPARATOR = new DesignationComparator();
	
	private final String digest;
	
	private final Manufacturer manufacturer;
	private final String designation;
	private final String description;
	private final Motor.Type type;
	private final double[] delays;
	private final double diameter;
	private final double length;
	private final double[] time;
	private final double[] thrust;
	private final Coordinate[] cg;
	
	private double maxThrust;
	private double burnTime;
	private double averageThrust;
	private double totalImpulse;
	
	/**
	 * Deep copy constructor.
	 * Constructs a new ThrustCurveMotor from an existing ThrustCurveMotor.
	 * @param m
	 */
	protected ThrustCurveMotor(ThrustCurveMotor m) {
		this.digest = m.digest;
		this.manufacturer = m.manufacturer;
		this.designation = m.designation;
		this.description = m.description;
		this.type = m.type;
		this.delays = ArrayUtils.copyOf(m.delays, m.delays.length);
		this.diameter = m.diameter;
		this.length = m.length;
		this.time = ArrayUtils.copyOf(m.time, m.time.length);
		this.thrust = ArrayUtils.copyOf(m.thrust, m.thrust.length);
		this.cg = new Coordinate[m.cg.length];
		for (int i = 0; i < cg.length; i++) {
			this.cg[i] = m.cg[i].clone();
		}
		this.maxThrust = m.maxThrust;
		this.burnTime = m.burnTime;
		this.averageThrust = m.averageThrust;
		this.totalImpulse = m.totalImpulse;
	}
	
	/**
	 * Sole constructor.  Sets all the properties of the motor.
	 * 
	 * @param manufacturer  the manufacturer of the motor.
	 * @param designation   the designation of the motor.
	 * @param description   extra description of the motor.
	 * @param type			the motor type
	 * @param delays		the delays defined for this thrust curve
	 * @param diameter      diameter of the motor.
	 * @param length        length of the motor.
	 * @param time          the time points for the thrust curve.
	 * @param thrust        thrust at the time points.
	 * @param cg            cg at the time points.
	 */
	public ThrustCurveMotor(Manufacturer manufacturer, String designation, String description,
			Motor.Type type, double[] delays, double diameter, double length,
			double[] time, double[] thrust, Coordinate[] cg, String digest) {
		this.digest = digest;
		// Check argument validity
		if ((time.length != thrust.length) || (time.length != cg.length)) {
			throw new IllegalArgumentException("Array lengths do not match, " +
					"time:" + time.length + " thrust:" + thrust.length +
					" cg:" + cg.length);
		}
		if (time.length < 2) {
			throw new IllegalArgumentException("Too short thrust-curve, length=" +
					time.length);
		}
		for (int i = 0; i < time.length - 1; i++) {
			if (time[i + 1] < time[i]) {
				throw new IllegalArgumentException("Time goes backwards, " +
						"time[" + i + "]=" + time[i] + " " +
						"time[" + (i + 1) + "]=" + time[i + 1]);
			}
		}
		if (!MathUtil.equals(time[0], 0)) {
			throw new IllegalArgumentException("Curve starts at time " + time[0]);
		}
		if (!MathUtil.equals(thrust[0], 0)) {
			throw new IllegalArgumentException("Curve starts at thrust " + thrust[0]);
		}
		if (!MathUtil.equals(thrust[thrust.length - 1], 0)) {
			throw new IllegalArgumentException("Curve ends at thrust " +
					thrust[thrust.length - 1]);
		}
		for (double t : thrust) {
			if (t < 0) {
				throw new IllegalArgumentException("Negative thrust.");
			}
			if (t > MAX_THRUST || Double.isNaN(t)) {
				throw new IllegalArgumentException("Invalid thrust " + t);
			}
		}
		for (Coordinate c : cg) {
			if (c.isNaN()) {
				throw new IllegalArgumentException("Invalid CG " + c);
			}
			if (c.x < 0) {
				throw new IllegalArgumentException("Invalid CG position " + String.format("%f", c.x) + ": CG is below the start of the motor.");
			}
			if (c.x > length) {
				throw new IllegalArgumentException("Invalid CG position: " + String.format("%f", c.x) + ": CG is above the end of the motor.");
			}
			if (c.weight < 0) {
				throw new IllegalArgumentException("Negative mass " + c.weight + "at time=" + time[Arrays.asList(cg).indexOf(c)]);
			}
		}
		
		if (type != Motor.Type.SINGLE && type != Motor.Type.RELOAD &&
				type != Motor.Type.HYBRID && type != Motor.Type.UNKNOWN) {
			throw new IllegalArgumentException("Illegal motor type=" + type);
		}
		
		
		this.manufacturer = manufacturer;
		this.designation = designation;
		this.description = description;
		this.type = type;
		this.delays = delays.clone();
		this.diameter = diameter;
		this.length = length;
		this.time = time.clone();
		this.thrust = thrust.clone();
		this.cg = cg.clone();
		
		computeStatistics();
	}
	
	
	
	/**
	 * Get the manufacturer of this motor.
	 * 
	 * @return the manufacturer
	 */
	public Manufacturer getManufacturer() {
		return manufacturer;
	}
	
	
	/**
	 * Return the array of time points for this thrust curve.
	 * @return	an array of time points where the thrust is sampled
	 */
	public double[] getTimePoints() {
		return time.clone();
	}
	
	/**
	 * Returns the array of thrust points for this thrust curve.
	 * @return	an array of thrust samples
	 */
	public double[] getThrustPoints() {
		return thrust.clone();
	}
	
	/**
	 * Returns the array of CG points for this thrust curve.
	 * @return	an array of CG samples
	 */
	public Coordinate[] getCGPoints() {
		return cg.clone();
	}
	
	/**
	 * Return a list of standard delays defined for this motor.
	 * @return	a list of standard delays
	 */
	public double[] getStandardDelays() {
		return delays.clone();
	}
	
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * NOTE: In most cases you want to examine the motor type of the ThrustCurveMotorSet,
	 * not the ThrustCurveMotor itself.
	 */
	@Override
	public Type getMotorType() {
		return type;
	}
	
	
	@Override
	public String getDesignation() {
		return designation;
	}
	
	@Override
	public String getDesignation(double delay) {
		return designation + "-" + getDelayString(delay);
	}
	
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public double getDiameter() {
		return diameter;
	}
	
	@Override
	public double getLength() {
		return length;
	}
	
	
	@Override
	public MotorInstance getInstance() {
		return new ThrustCurveMotorInstance();
	}
	
	
	@Override
	public Coordinate getLaunchCG() {
		return cg[0];
	}
	
	@Override
	public Coordinate getEmptyCG() {
		return cg[cg.length - 1];
	}
	
	
	
	
	@Override
	public double getBurnTimeEstimate() {
		return burnTime;
	}
	
	@Override
	public double getAverageThrustEstimate() {
		return averageThrust;
	}
	
	@Override
	public double getMaxThrustEstimate() {
		return maxThrust;
	}
	
	@Override
	public double getTotalImpulseEstimate() {
		return totalImpulse;
	}
	
	@Override
	public String getDigest() {
		return digest;
	}
	
	
	/**
	 * Compute the general statistics of this motor.
	 */
	private void computeStatistics() {
		
		// Maximum thrust
		maxThrust = 0;
		for (double t : thrust) {
			if (t > maxThrust)
				maxThrust = t;
		}
		
		
		// Burn start time
		double thrustLimit = maxThrust * MARGINAL_THRUST;
		double burnStart, burnEnd;
		
		int pos;
		for (pos = 1; pos < thrust.length; pos++) {
			if (thrust[pos] >= thrustLimit)
				break;
		}
		if (pos >= thrust.length) {
			throw new BugException("Could not compute burn start time, maxThrust=" + maxThrust +
					" limit=" + thrustLimit + " thrust=" + Arrays.toString(thrust));
		}
		if (MathUtil.equals(thrust[pos - 1], thrust[pos])) {
			// For safety
			burnStart = (time[pos - 1] + time[pos]) / 2;
		} else {
			burnStart = MathUtil.map(thrustLimit, thrust[pos - 1], thrust[pos], time[pos - 1], time[pos]);
		}
		
		
		// Burn end time
		for (pos = thrust.length - 2; pos >= 0; pos--) {
			if (thrust[pos] >= thrustLimit)
				break;
		}
		if (pos < 0) {
			throw new BugException("Could not compute burn end time, maxThrust=" + maxThrust +
					" limit=" + thrustLimit + " thrust=" + Arrays.toString(thrust));
		}
		if (MathUtil.equals(thrust[pos], thrust[pos + 1])) {
			// For safety
			burnEnd = (time[pos] + time[pos + 1]) / 2;
		} else {
			burnEnd = MathUtil.map(thrustLimit, thrust[pos], thrust[pos + 1],
					time[pos], time[pos + 1]);
		}
		
		
		// Burn time
		burnTime = Math.max(burnEnd - burnStart, 0);
		
		
		// Total impulse and average thrust
		totalImpulse = 0;
		averageThrust = 0;
		
		for (pos = 0; pos < time.length - 1; pos++) {
			double t0 = time[pos];
			double t1 = time[pos + 1];
			double f0 = thrust[pos];
			double f1 = thrust[pos + 1];
			
			totalImpulse += (t1 - t0) * (f0 + f1) / 2;
			
			if (t0 < burnStart && t1 > burnStart) {
				double fStart = MathUtil.map(burnStart, t0, t1, f0, f1);
				averageThrust += (fStart + f1) / 2 * (t1 - burnStart);
			} else if (t0 >= burnStart && t1 <= burnEnd) {
				averageThrust += (f0 + f1) / 2 * (t1 - t0);
			} else if (t0 < burnEnd && t1 > burnEnd) {
				double fEnd = MathUtil.map(burnEnd, t0, t1, f0, f1);
				averageThrust += (f0 + fEnd) / 2 * (burnEnd - t0);
			}
		}
		
		if (burnTime > 0) {
			averageThrust /= burnTime;
		} else {
			averageThrust = 0;
		}
		
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
		return getDelayString(delay, "P");
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
		delay = Math.rint(delay * 10) / 10;
		if (MathUtil.equals(delay, Math.rint(delay)))
			return "" + ((int) delay);
		return "" + delay;
	}
	
	
	
	////////  Motor instance implementation  ////////
	private class ThrustCurveMotorInstance implements MotorInstance {
		
		private int position;
		
		// Previous time step value
		private double prevTime;
		
		// Average thrust during previous step
		private double stepThrust;
		// Instantaneous thrust at current time point
		private double instThrust;
		
		// Average CG during previous step
		private Coordinate stepCG;
		// Instantaneous CG at current time point
		private Coordinate instCG;
		
		private final double unitRotationalInertia;
		private final double unitLongitudinalInertia;
		private final Motor parentMotor;
		
		private int modID = 0;
		
		public ThrustCurveMotorInstance() {
			log.debug("ThrustCurveMotor:  Creating motor instance of " + ThrustCurveMotor.this);
			position = 0;
			prevTime = 0;
			instThrust = 0;
			stepThrust = 0;
			instCG = cg[0];
			stepCG = cg[0];
			unitRotationalInertia = Inertia.filledCylinderRotational(getDiameter() / 2);
			unitLongitudinalInertia = Inertia.filledCylinderLongitudinal(getDiameter() / 2, getLength());
			parentMotor = ThrustCurveMotor.this;
		}
		
		@Override
		public Motor getParentMotor() {
			return parentMotor;
		}
		
		@Override
		public double getTime() {
			return prevTime;
		}
		
		@Override
		public Coordinate getCG() {
			return stepCG;
		}
		
		@Override
		public double getLongitudinalInertia() {
			return unitLongitudinalInertia * stepCG.weight;
		}
		
		@Override
		public double getRotationalInertia() {
			return unitRotationalInertia * stepCG.weight;
		}
		
		@Override
		public double getThrust() {
			return stepThrust;
		}
		
		@Override
		public boolean isActive() {
			return prevTime < time[time.length - 1];
		}
		
		@Override
		public void step(double nextTime, double acceleration, AtmosphericConditions cond) {
			
			if (!(nextTime >= prevTime)) {
				// Also catches NaN
				throw new IllegalArgumentException("Stepping backwards in time, current=" +
						prevTime + " new=" + nextTime);
			}
			if (MathUtil.equals(prevTime, nextTime)) {
				return;
			}
			
			modID++;
			
			if (position >= time.length - 1) {
				// Thrust has ended
				prevTime = nextTime;
				stepThrust = 0;
				instThrust = 0;
				stepCG = cg[cg.length - 1];
				return;
			}
			
			
			// Compute average & instantaneous thrust
			if (nextTime < time[position + 1]) {
				
				// Time step between time points
				double nextF = MathUtil.map(nextTime, time[position], time[position + 1],
						thrust[position], thrust[position + 1]);
				stepThrust = (instThrust + nextF) / 2;
				instThrust = nextF;
				
			} else {
				
				// Portion of previous step
				stepThrust = (instThrust + thrust[position + 1]) / 2 * (time[position + 1] - prevTime);
				
				// Whole steps
				position++;
				while ((position < time.length - 1) && (nextTime >= time[position + 1])) {
					stepThrust += (thrust[position] + thrust[position + 1]) / 2 *
							(time[position + 1] - time[position]);
					position++;
				}
				
				// End step
				if (position < time.length - 1) {
					instThrust = MathUtil.map(nextTime, time[position], time[position + 1],
							thrust[position], thrust[position + 1]);
					stepThrust += (thrust[position] + instThrust) / 2 *
							(nextTime - time[position]);
				} else {
					// Thrust ended during this step
					instThrust = 0;
				}
				
				stepThrust /= (nextTime - prevTime);
				
			}
			
			// Compute average and instantaneous CG (simple average between points)
			Coordinate nextCG;
			if (position < time.length - 1) {
				nextCG = MathUtil.map(nextTime, time[position], time[position + 1],
						cg[position], cg[position + 1]);
			} else {
				nextCG = cg[cg.length - 1];
			}
			stepCG = instCG.add(nextCG).multiply(0.5);
			instCG = nextCG;
			
			// Update time
			prevTime = nextTime;
		}
		
		@Override
		public MotorInstance clone() {
			try {
				return (MotorInstance) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new BugException("CloneNotSupportedException", e);
			}
		}
		
		@Override
		public int getModID() {
			return modID;
		}
	}
	
	
	
	@Override
	public int compareTo(ThrustCurveMotor other) {
		
		int value;
		
		// 1. Manufacturer
		value = COLLATOR.compare(this.manufacturer.getDisplayName(),
				((ThrustCurveMotor) other).manufacturer.getDisplayName());
		if (value != 0)
			return value;
		
		// 2. Designation
		value = DESIGNATION_COMPARATOR.compare(this.getDesignation(), other.getDesignation());
		if (value != 0)
			return value;
		
		// 3. Diameter
		value = (int) ((this.getDiameter() - other.getDiameter()) * 1000000);
		if (value != 0)
			return value;
		
		// 4. Length
		value = (int) ((this.getLength() - other.getLength()) * 1000000);
		return value;
		
	}
	
	
}
