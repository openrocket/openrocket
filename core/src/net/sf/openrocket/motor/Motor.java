package net.sf.openrocket.motor;

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
	
	public static final double PSEUDO_TIME_EMPTY = Double.NaN;
	public static final double PSEUDO_TIME_LAUNCH = 0.0;
	public static final double PSEUDO_TIME_BURNOUT = Double.MAX_VALUE;
	
		
	/**
	 * Ejection charge delay value signifying a "plugged" motor with no ejection charge.
	 * The value is that of <code>Double.POSITIVE_INFINITY</code>.
	 */
	public static final double PLUGGED_DELAY = Double.POSITIVE_INFINITY;
	
	
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
	
	public double getAverageThrust( final double startTime, final double endTime );
	
	public double getLaunchCGx();
	
	public double getBurnoutCGx();
	
	public double getLaunchMass();
	
	public double getBurnoutMass();
	
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


	double getBurnTime();

	
	/**
	 * Return the thrust at a time offset from motor ignition
	 * 
	 * this is probably a badly-designed way to expose the thrust, but it's not worth worrying about until 
	 * there's a second (non-trivial) type of motor to support...
	 *
 	 * @param motorTime  time (in seconds) since motor ignition
 	 * @return thrust (double, in Newtons) at given time
 	 */
	public double getThrust( final double motorTime);
	
	/**
	 * Return the mass at a time offset from motor ignition
	 * 
     * @param motorTime  time (in seconds) since motor ignition
	 */
	public double getTotalMass( final double motorTime);

	public double getPropellantMass( final Double motorTime);
	
	/** Return the mass at a given time 
	 * 
	 * @param motorTime  time (in seconds) since motor ignition
	 * @return
	 */
	public double getCMx( final double motorTime);
	
	public double getUnitIxx();
	
	public double getUnitIyy();
	
	public double getUnitIzz();

}
