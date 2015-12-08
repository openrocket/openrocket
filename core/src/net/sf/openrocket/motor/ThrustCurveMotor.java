package net.sf.openrocket.motor;

import java.io.Serializable;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.models.atmosphere.AtmosphericConditions;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;
import net.sf.openrocket.util.MathUtil;


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
	
	private String digest;
	
	private Manufacturer manufacturer;
	private String designation;
	private String description;
	private Motor.Type type;
	private double[] delays;
	private double diameter;
	private double length;
	private double[] time;
	private double[] thrust;
	private Coordinate[] cg;
	
	private String caseInfo;
	private String propellantInfo;
	
	private double initialMass;
	private double propellantMass;
	private double maxThrust;
	private double burnTime;
	private double averageThrust;
	private double totalImpulse;
	private boolean available = true;
	
	public static class Builder {
		
		ThrustCurveMotor motor = new ThrustCurveMotor();
		
		public Builder setAverageThrustEstimate(double v) {
			motor.averageThrust = v;
			return this;
		}
		
		public Builder setBurnTimeEstimate(double v) {
			motor.burnTime = v;
			return this;
		}
		
		public Builder setCaseInfo(String v) {
			motor.caseInfo = v;
			return this;
		}
		
		public Builder setCGPoints(Coordinate[] cg) {
			motor.cg = cg;
			return this;
		}
		
		public Builder setDescription(String d) {
			motor.description = d;
			return this;
		}
		
		public Builder setDesignation(String d) {
			motor.designation = d;
			return this;
		}
		
		public Builder setDiameter(double v) {
			motor.diameter = v;
			return this;
		}
		
		public Builder setDigest(String d) {
			motor.digest = d;
			return this;
		}
		
		public Builder setInitialMass(double v) {
			motor.initialMass = v;
			return this;
		}
		
		public Builder setLength(double v) {
			motor.length = v;
			return this;
		}
		
		public Builder setManufacturer(Manufacturer m) {
			motor.manufacturer = m;
			return this;
		}
		
		public Builder setMaxThrustEstimate(double v) {
			motor.maxThrust = v;
			return this;
		}
		
		public Builder setMotorType(Motor.Type t) {
			motor.type = t;
			return this;
		}
		
		public Builder setPropellantInfo(String v) {
			motor.propellantInfo = v;
			return this;
		}
		
		public Builder setPropellantMass(double v) {
			motor.propellantMass = v;
			return this;
		}
		
		public Builder setStandardDelays(double[] d) {
			motor.delays = d;
			return this;
		}
		
		public Builder setThrustPoints(double[] d) {
			motor.thrust = d;
			return this;
		}
		
		public Builder setTimePoints(double[] d) {
			motor.time = d;
			return this;
		}
		
		public Builder setTotalThrustEstimate(double v) {
			motor.totalImpulse = v;
			return this;
		}
		
		public Builder setAvailablity(boolean avail) {
			motor.available = avail;
			return this;
		}
		
		public ThrustCurveMotor build() {
			// Check argument validity
			if ((motor.time.length != motor.thrust.length) || (motor.time.length != motor.cg.length)) {
				throw new IllegalArgumentException("Array lengths do not match, " +
						"time:" + motor.time.length + " thrust:" + motor.thrust.length +
						" cg:" + motor.cg.length);
			}
			if (motor.time.length < 2) {
				throw new IllegalArgumentException("Too short thrust-curve, length=" + motor.time.length);
			}
			for (int i = 0; i < motor.time.length - 1; i++) {
				if (motor.time[i + 1] < motor.time[i]) {
					throw new IllegalArgumentException("Time goes backwards, " +
							"time[" + i + "]=" + motor.time[i] + " " +
							"time[" + (i + 1) + "]=" + motor.time[i + 1]);
				}
			}
			if (!MathUtil.equals(motor.time[0], 0)) {
				throw new IllegalArgumentException("Curve starts at time " + motor.time[0]);
			}
			if (!MathUtil.equals(motor.thrust[0], 0)) {
				throw new IllegalArgumentException("Curve starts at thrust " + motor.thrust[0]);
			}
			if (!MathUtil.equals(motor.thrust[motor.thrust.length - 1], 0)) {
				throw new IllegalArgumentException("Curve ends at thrust " +
						motor.thrust[motor.thrust.length - 1]);
			}
			for (double t : motor.thrust) {
				if (t < 0) {
					throw new IllegalArgumentException("Negative thrust.");
				}
				if (t > MAX_THRUST || Double.isNaN(t)) {
					throw new IllegalArgumentException("Invalid thrust " + t);
				}
			}
			for (Coordinate c : motor.cg) {
				if (c.isNaN()) {
					throw new IllegalArgumentException("Invalid CG " + c);
				}
				if (c.x < 0) {
					throw new IllegalArgumentException("Invalid CG position " + String.format("%f", c.x) + ": CG is below the start of the motor.");
				}
				if (c.x > motor.length) {
					throw new IllegalArgumentException("Invalid CG position: " + String.format("%f", c.x) + ": CG is above the end of the motor.");
				}
				if (c.weight < 0) {
					throw new IllegalArgumentException("Negative mass " + c.weight + "at time=" + motor.time[Arrays.asList(motor.cg).indexOf(c)]);
				}
			}
			
			if (motor.type != Motor.Type.SINGLE && motor.type != Motor.Type.RELOAD &&
					motor.type != Motor.Type.HYBRID && motor.type != Motor.Type.UNKNOWN) {
				throw new IllegalArgumentException("Illegal motor type=" + motor.type);
			}
			
			
			motor.computeStatistics();
			
			return motor;
		}
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
	
	public String getCaseInfo() {
		return caseInfo;
	}
	
	
	public String getPropellantInfo() {
		return propellantInfo;
	}
	
	
	public double getInitialMass() {
		return initialMass;
	}
	
	
	public double getPropellantMass() {
		return propellantMass;
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
	
	public boolean isAvailable() {
		return available;
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
