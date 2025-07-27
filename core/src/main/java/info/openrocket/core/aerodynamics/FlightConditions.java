package info.openrocket.core.aerodynamics;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.Monitorable;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.core.util.ModID;

/**
 * A class defining the momentary flight conditions of a rocket, including
 * the angle of attack, lateral wind angle, atmospheric conditions etc.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightConditions implements Cloneable, ChangeSource, Monitorable {
	private static final double MIN_BETA = 0.25;

	private List<EventListener> listenerList = new ArrayList<>();
	private EventObject event = new EventObject(this);

	/** Reference length used in calculations. */
	private double refLength = 1.0;

	/** Reference area used in calculations. */
	private double refArea = Math.PI * 0.25;

	/** Angle of attack. */
	private double aoa = 0;

	/** Sine of the angle of attack. */
	private double sinAOA = 0;

	/**
	 * The fraction <code>sin(aoa) / aoa</code>. At an AOA of zero this value
	 * must be one. This value may be used in many cases to avoid checking for
	 * division by zero.
	 */
	private double sincAOA = 1.0;

	/** Lateral wind direction. */
	private double theta = 0;

	/** Current Mach speed. */
	private double mach = 0.3;

	/**
	 * Sqrt(1 - M^2) for M<1
	 * Sqrt(M^2 - 1) for M>1
	 */
	private double beta = calculateBeta(mach);

	/** Current roll rate. */
	private double rollRate = 0;

	/** Current pitch rate. */
	private double pitchRate = 0;
	/** Current yaw rate. */
	private double yawRate = 0;

	private Coordinate pitchCenter = Coordinate.NUL;

	private AtmosphericConditions atmosphericConditions = new AtmosphericConditions();

	private ModID modID;

	/**
	 * Sole constructor. The reference length is initialized to the reference length
	 * of the <code>Configuration</code>, and the reference area accordingly.
	 * If <code>config</code> is <code>null</code>, then the reference length is set
	 * to 1 meter.
	 * 
	 * @param config the configuration of which the reference length is taken.
	 */
	public FlightConditions(FlightConfiguration config) {
		if (config != null)
			setRefLength(config.getReferenceLength());
	}

	/**
	 * Set the reference length from the given configuration.
	 * 
	 * @param config the configuration from which to get the reference length.
	 */
	public void setReference(FlightConfiguration config) {
		setRefLength(config.getReferenceLength());
	}

	/**
	 * Set the reference length and area.
	 * fires change event
	 */
	public void setRefLength(double length) {
		if (refLength == length)
			return;
		
		refLength = length;
		refArea = Math.PI * MathUtil.pow2(length / 2);

		fireChangeEvent();
	}

	/**
	 * @return the reference length.
	 */
	public double getRefLength() {
		return refLength;
	}

	/**
	 * Set the reference area and length.
	 * fires change event
	 */
	public void setRefArea(double area) {
		if (refArea == area)
			return;
		
		refArea = area;
		refLength = MathUtil.safeSqrt(area / Math.PI) * 2;

		fireChangeEvent();
	}

	/**
	 * @return the reference area.
	 */
	public double getRefArea() {
		return refArea;
	}

	/**
	 * Sets the angle of attack. It calculates values also for the methods
	 * {@link #getSinAOA()} and {@link #getSincAOA()}.
	 * fires change event if it's different from previous value
	 * 
	 * @param aoa the angle of attack.
	 */
	public void setAOA(double aoa) {
		aoa = MathUtil.clamp(aoa, 0, Math.PI);
		if (MathUtil.equals(this.aoa, aoa))
			return;

		this.aoa = aoa;
		if (aoa < 0.001) {
			this.sinAOA = aoa;
			this.sincAOA = 1.0;
		} else {
			this.sinAOA = Math.sin(aoa);
			this.sincAOA = sinAOA / aoa;
		}

		fireChangeEvent();
	}

	/**
	 * Sets the angle of attack with the sine. The value <code>sinAOA</code> is
	 * assumed
	 * to be the sine of <code>aoa</code> for cases in which this value is known.
	 * The AOA must still be specified, as the sine is not unique in the range
	 * of 0..180 degrees.
	 * fires change event if it's different from previous value
	 * 
	 * @param aoa    the angle of attack in radians.
	 * @param sinAOA the sine of the angle of attack.
	 */
	public void setAOA(double aoa, double sinAOA) {
		aoa = MathUtil.clamp(aoa, 0, Math.PI);
		sinAOA = MathUtil.clamp(sinAOA, 0, 1);
		if (MathUtil.equals(this.aoa, aoa))
			return;

		assert (Math.abs(Math.sin(aoa) - sinAOA) < 0.0001) : "Illegal sine: aoa=" + aoa + " sinAOA=" + sinAOA;

		this.aoa = aoa;
		this.sinAOA = sinAOA;
		if (aoa < 0.001) {
			this.sincAOA = 1.0;
		} else {
			this.sincAOA = sinAOA / aoa;
		}

		fireChangeEvent();
	}

	/**
	 * @return the angle of attack.
	 */
	public double getAOA() {
		return aoa;
	}

	/**
	 * @return the sine of the angle of attack.
	 */
	public double getSinAOA() {
		return sinAOA;
	}

	/**
	 * @return the sinc of the angle of attack (sin(AOA) / AOA). This method returns
	 *         one if the angle of attack is zero.
	 */
	public double getSincAOA() {
		return sincAOA;
	}

	/**
	 * Set the direction of the lateral airflow.
	 * fires change event if it's different from previous value
	 * 
	 */
	public void setTheta(double theta) {
		if (MathUtil.equals(this.theta, theta))
			return;
		this.theta = theta;

		fireChangeEvent();
	}

	/**
	 * @return the direction of the lateral airflow.
	 */
	public double getTheta() {
		return theta;
	}

	/**
	 * Set the current Mach speed. This should be (but is not required to be) in
	 * reference to the speed of sound of the atmospheric conditions.
	 * 
	 * fires change event if it's different from previous value
	 */
	public void setMach(double mach) {
		mach = Math.max(mach, 0);
		if (MathUtil.equals(this.mach, mach))
			return;

		this.mach = mach;
		this.beta = calculateBeta(mach);

		fireChangeEvent();
	}

	/**
	 * @return the current Mach speed.
	 */
	public double getMach() {
		return mach;
	}

	/**
	 * Returns the current rocket velocity, calculated from the Mach number and the
	 * speed of sound. If either of these parameters are changed, the velocity
	 * changes
	 * as well.
	 * 
	 * @return the velocity of the rocket.
	 */
	public double getVelocity() {
		return mach * atmosphericConditions.getMachSpeed();
	}

	/**
	 * Sets the Mach speed according to the given velocity and the current speed of
	 * sound.
	 * 
	 * @param velocity the current velocity.
	 */
	public void setVelocity(double velocity) {
		setMach(velocity / atmosphericConditions.getMachSpeed());
	}

	/**
	 * @return sqrt(abs(1 - Mach^2)). This is calculated in the setting call and is
	 *         therefore fast.
	 */
	public double getBeta() {
		return beta;
	}

	/**
	 * Calculate the beta value (compressibility factor/Prandtl-Glauert correction factor) for the given Mach number.
	 * @param mach the Mach number.
	 * @return the beta value.
	 */
	private static double calculateBeta(double mach) {
		if (mach < 1) {
			return MathUtil.max(MIN_BETA, MathUtil.safeSqrt(1 - mach * mach));
		} else {
			return MathUtil.max(MIN_BETA, MathUtil.safeSqrt(mach * mach - 1));
		}
	}

	/**
	 * @return the current roll rate.
	 */
	public double getRollRate() {
		return rollRate;
	}

	/**
	 * Set the current roll rate.
	 * fires change event if it's different from previous
	 */
	public void setRollRate(double rate) {
		if (MathUtil.equals(this.rollRate, rate))
			return;

		this.rollRate = rate;
		
		fireChangeEvent();
	}

	/**
	 * 
	 * @return current pitch rate
	 */
	public double getPitchRate() {
		return pitchRate;
	}

	/**
	 * sets the pitch rate
	 * fires change event if it's different from previous
	 * 
	 * @param pitchRate
	 */
	public void setPitchRate(double pitchRate) {
		if (MathUtil.equals(this.pitchRate, pitchRate))
			return;
		this.pitchRate = pitchRate;
		fireChangeEvent();
	}

	/**
	 * 
	 * @return current yaw rate
	 */
	public double getYawRate() {
		return yawRate;
	}

	public void setYawRate(double yawRate) {
		if (MathUtil.equals(this.yawRate, yawRate))
			return;
		this.yawRate = yawRate;
		fireChangeEvent();
	}

	/**
	 * @return the pitchCenter
	 */
	public Coordinate getPitchCenter() {
		return pitchCenter;
	}

	/**
	 * @param pitchCenter the pitchCenter to set
	 */
	public void setPitchCenter(Coordinate pitchCenter) {
		if (this.pitchCenter.equals(pitchCenter))
			return;
		this.pitchCenter = pitchCenter;
		fireChangeEvent();
	}

	/**
	 * Return the current atmospheric conditions. Note that this method returns a
	 * reference to the {@link AtmosphericConditions} object used by this object.
	 * Changes made to the object will modify the encapsulated object, but will NOT
	 * generate change events.
	 * 
	 * @return the current atmospheric conditions.
	 */
	public AtmosphericConditions getAtmosphericConditions() {
		return atmosphericConditions;
	}

	/**
	 * Set the current atmospheric conditions. This method will fire a change event
	 * if a change occurs.
	 */
	public void setAtmosphericConditions(AtmosphericConditions cond) {
		if (atmosphericConditions.equals(cond))
			return;

		atmosphericConditions = cond;
		fireChangeEvent();
	}

	/**
	 * Retrieve the modification count of this object.
	 * 
	 * @return modification ID
	 */
	@Override
	public ModID getModID() {
		return modID;
	}

	@Override
	public String toString() {
		return String.format("FlightConditions[" +
				"aoa=%.2f\u00b0," +
				"theta=%.2f\u00b0," +
				"mach=%.3f," +
				"rollRate=%.2f," +
				"pitchRate=%.2f," +
				"yawRate=%.2f," +
				"refLength=%.3f," +
				"pitchCenter=" + pitchCenter.toString() + "," +
				"atmosphericConditions=" + atmosphericConditions.toString() +
				"]",
				aoa * 180 / Math.PI, theta * 180 / Math.PI, mach, rollRate, pitchRate, yawRate, refLength);
	}

	/**
	 * @return a copy of the flight conditions. The copy has no listeners. The
	 *         atmospheric conditions is also cloned.
	 */
	@Override
	public FlightConditions clone() {
		try {
			FlightConditions cond = (FlightConditions) super.clone();
			cond.listenerList = new ArrayList<>();
			cond.event = new EventObject(cond);
			cond.atmosphericConditions = atmosphericConditions.clone();
			return cond;
		} catch (CloneNotSupportedException e) {
			throw new BugException("clone not supported!", e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FlightConditions))
			return false;

		FlightConditions other = (FlightConditions) obj;

		return (MathUtil.equals(this.refLength, other.refLength) &&
				MathUtil.equals(this.aoa, other.aoa) &&
				MathUtil.equals(this.theta, other.theta) &&
				MathUtil.equals(this.mach, other.mach) &&
				MathUtil.equals(this.rollRate, other.rollRate) &&
				MathUtil.equals(this.pitchRate, other.pitchRate) &&
				MathUtil.equals(this.yawRate, other.yawRate) &&
				this.pitchCenter.equals(other.pitchCenter)
				&& this.atmosphericConditions.equals(other.atmosphericConditions));
	}

	@Override
	public int hashCode() {
		return (int) (1000 * (refLength + aoa + theta + mach + rollRate + pitchRate + yawRate));
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {
		listenerList.add(0, listener);
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listenerList.remove(listener);
	}

	/**
	 * wake up call to listeners
	 */
	protected void fireChangeEvent() {
		modID = new ModID();
		
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] listeners = listenerList.toArray(new EventListener[0]);
		for (EventListener l : listeners) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			}
		}
	}
}
