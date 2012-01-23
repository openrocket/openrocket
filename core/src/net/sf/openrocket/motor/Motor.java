package net.sf.openrocket.motor;

import net.sf.openrocket.util.Coordinate;

public interface Motor {
	
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
	public static final double MARGINAL_THRUST = 0.05;
	
	
	
	/**
	 * Return the motor type.
	 * 
	 * @return  the motorType
	 */
	public Type getMotorType();
	
	
	/**
	 * Return the designation of the motor.
	 * 
	 * @return the designation
	 */
	public String getDesignation();
	
	/**
	 * Return the designation of the motor, including a delay.
	 * 
	 * @param delay  the delay of the motor.
	 * @return		 designation with delay.
	 */
	public String getDesignation(double delay);
	
	
	/**
	 * Return extra description for the motor.  This may include for example 
	 * comments on the source of the thrust curve.  The returned <code>String</code>
	 * may include new-lines.
	 * 
	 * @return the description
	 */
	public String getDescription();
	
	
	/**
	 * Return the maximum diameter of the motor.
	 * 
	 * @return the diameter
	 */
	public double getDiameter();
	
	/**
	 * Return the length of the motor.  This should be a "characteristic" length,
	 * and the exact definition may depend on the motor type.  Typically this should
	 * be the length from the bottom of the motor to the end of the maximum diameter
	 * portion, ignoring any smaller ejection charge compartments.
	 * 
	 * @return the length
	 */
	public double getLength();
	
	public String getDigest();
	
	public MotorInstance getInstance();
	
	
	public Coordinate getLaunchCG();
	
	public Coordinate getEmptyCG();
	
	
	/**
	 * Return an estimate of the burn time of this motor, or NaN if an estimate is unavailable.
	 */
	public double getBurnTimeEstimate();
	
	/**
	 * Return an estimate of the average thrust of this motor, or NaN if an estimate is unavailable.
	 */
	public double getAverageThrustEstimate();
	
	/**
	 * Return an estimate of the maximum thrust of this motor, or NaN if an estimate is unavailable.
	 */
	public double getMaxThrustEstimate();
	
	/**
	 * Return an estimate of the total impulse of this motor, or NaN if an estimate is unavailable.
	 */
	public double getTotalImpulseEstimate();
	
}
