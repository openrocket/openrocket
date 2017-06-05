package net.sf.openrocket.motor;

import java.io.Serializable;
import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Inertia;
import net.sf.openrocket.util.MathUtil;

public class ThrustCurveMotor implements Motor, Comparable<ThrustCurveMotor>, Serializable {
	private static final long serialVersionUID = -1490333207132694479L;
	
	@SuppressWarnings("unused")
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
	private double maxThrust;
	private double burnTimeEstimate;
	private double averageThrust;
	private double totalImpulse;
	private boolean available = true;
	
	private double unitRotationalInertia;
	private double unitLongitudinalInertia;
	
	public static class Builder {
		
		ThrustCurveMotor motor = new ThrustCurveMotor();
		
		public Builder setAverageThrustEstimate(double v) {
			motor.averageThrust = v;
			return this;
		}
		
		public Builder setBurnTimeEstimate(double v) {
			motor.burnTimeEstimate = v;
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
			
			motor.unitRotationalInertia = Inertia.filledCylinderRotational( motor.diameter / 2);
			motor.unitLongitudinalInertia = Inertia.filledCylinderLongitudinal( motor.diameter / 2, motor.length);

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
	
	/* 
	 * find the index to data that corresponds to the given time:
	 *  
	 * Pseudo Index is two parts: 
	 *     integer : simple index into the array
	 *     fractional part:  [0,1]: weighting to interpolate between the given index and the next index. 
	 *  
	 * @param is time after motor ignition, in seconds
	 * @return a pseudo index to this motor's data. 
	 */
	protected double getPseudoIndex( final double motorTime ){
		if(( time.length == 0 )||( 0 > motorTime )){
			return Double.NaN;
		}
		
		final int lowerIndex = getIndex( motorTime);
		final double fraction = getIndexFraction( motorTime, lowerIndex );
		return ((double)lowerIndex)+fraction;
	}
	
	private int getIndex( final double motorTime ){
		int lowerBoundIndex=0;
		int upperBoundIndex=0;
		while( ( upperBoundIndex < time.length ) && ( motorTime >= time[upperBoundIndex] )){
			lowerBoundIndex = upperBoundIndex;
			++upperBoundIndex;
		}	
		
		return lowerBoundIndex; 
	}
	
	private double getIndexFraction( final double motorTime, final int index ){
		final double SNAP_DISTANCE = 0.0001;
		
		final int lowerBoundIndex= index;
		final int upperBoundIndex= index+1;
		
		// we are already at the end of the time array.
		if( upperBoundIndex == time.length ){
			return 0.;
		}
		
		final double lowerBoundTime = time[lowerBoundIndex];
		final double upperBoundTime = time[upperBoundIndex];
		final double timeFraction  = motorTime - lowerBoundTime;
		final double indexFraction = ( timeFraction ) / ( upperBoundTime - lowerBoundTime );
		
		if( SNAP_DISTANCE > indexFraction  ){
			// round down to previous index
			return 0.;
		}else if( (1-SNAP_DISTANCE)< indexFraction ){
			// round up to next index
			return 1.;
		}else{
			// general case 
			return indexFraction;
		}
	}
	
	@Override
	public double getAverageThrust( final double startTime, final double endTime ) {
		
		int timeIndex = 0;
		
		while( timeIndex < time.length-2 && startTime > time[timeIndex+1] ) {
			timeIndex++;
		}
		
		if ( timeIndex == time.length ) {
			return 0.0;
		}
		
		if ( endTime <= time[timeIndex+1] ) {
			// we are completely within this time slice so the computation of the average is pretty easy:
			double avgImpulse = MathUtil.map(startTime, time[timeIndex], time[timeIndex+1], thrust[timeIndex], thrust[timeIndex+1]);
			avgImpulse += MathUtil.map(endTime, time[timeIndex], time[timeIndex+1], thrust[timeIndex], thrust[timeIndex+1]);
			avgImpulse /= 2.0;
			return avgImpulse;
		}
		
		// portion from startTime through time[timeIndex]
		double avgImpulse = 0.0;
		// For numeric stability.
		if( time[timeIndex+1] - startTime > 0.001 ) {
			avgImpulse = (MathUtil.map(startTime, time[timeIndex], time[timeIndex+1], thrust[timeIndex], thrust[timeIndex+1]) + thrust[timeIndex+1])
					/ 2.0 * (time[timeIndex+1] - startTime);
		}
		
		// Now add the whole steps;
		timeIndex++;
		while( timeIndex < time.length -1 && endTime >= time[timeIndex+1] ) {
			avgImpulse += (thrust[timeIndex] + thrust[timeIndex+1]) / 2.0 * (time[timeIndex+1]-time[timeIndex]);
			timeIndex++;
		}
		
		// Now add the bit after the last time index
		if ( timeIndex < time.length -1 ) {
			double endInstImpulse = MathUtil.map( endTime,  time[timeIndex], time[timeIndex+1], thrust[timeIndex], thrust[timeIndex+1]);
			avgImpulse += (thrust[timeIndex] + endInstImpulse) / 2.0 * (endTime - time[timeIndex]);
		}
		
		return avgImpulse / (endTime - startTime);
	}
	
	@Override
	public double getThrust( final double motorTime ){
		double pseudoIndex = getPseudoIndex( motorTime );
		
		final double thrustAtTime= ThrustCurveMotor.interpolateAtIndex( thrust, pseudoIndex);
		return thrustAtTime;
	}
	
	@Override
	public double getCMx( final double motorTime ){
		double pseudoIndex = getPseudoIndex( motorTime );
		return this.interpolateCenterOfMassAtIndex( pseudoIndex).x;
	}
	
	
	
	public String getCaseInfo() {
		return caseInfo;
	}
	
	public CaseInfo getCaseInfoEnum() {
		return CaseInfo.parse(caseInfo);
	}
	
	public CaseInfo[] getCompatibleCases() {
		CaseInfo myCase = getCaseInfoEnum();
		if (myCase == null) {
			return new CaseInfo[] {};
		}
		return myCase.getCompatibleCases();
	}
	
	public String getPropellantInfo() {
		return propellantInfo;
	}
	
	
	public double getInitialMass() {
		return initialMass;
	}
	
	
	/**
	 * Returns the array of thrust points for this thrust curve.
	 * @return	an array of thrust samples
	 */
	public double[] getThrustPoints() {
		return thrust.clone();
	}
	
	//	/**
	//	 * Returns the array of CG points for this thrust curve.
	//	 * @return	an array of CG samples
	//	 */
	//	public double[] getCGxPoints() {
	//		return cgx;
	//	}
	
	public Coordinate[] getCGPoints(){
		return cg;
	}
	
	//	/**
	//	 * Returns the array of Mass values for this thrust curve.
	//	 * @return	an array of Masses
	//	 */
	//	public double[] getMassPoints() {
	//		return mass;
	//	}
	
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
	
	public double getUnitLongitudinalInertia() {
		return this.unitLongitudinalInertia;
	}
	
	public double getUnitRotationalInertia() {
		return this.unitRotationalInertia;
	}
	
	@Override
	public double getUnitIxx() {
		return this.unitRotationalInertia; 
	}
	
	@Override
	public double getUnitIyy() {
		return this.unitLongitudinalInertia;
	}
	
	@Override
	public double getUnitIzz(){
		return this.unitLongitudinalInertia;
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
	public double getLaunchCGx() {
		return cg[0].x;//cgx[0];
	}
	
	@Override
	public double getBurnoutCGx() {
		return cg[cg.length - 1].x;// cgx[ cg.length - 1];
	}
	
	@Override
	public double getLaunchMass() {
		return cg[0].weight;//mass[0];
	}
	
	@Override
	public double getBurnoutMass() {
		return cg[cg.length-1].weight; //mass[mass.length - 1];
	}	
	
	@Override
	public double getBurnTime() {
		return time[time.length-1];
	}
	
	private static double interpolateAtIndex( final double[] values, final double pseudoIndex ){
		final double SNAP_TOLERANCE = 0.0001;
		
		final int lowerIndex = (int)pseudoIndex;
		final int upperIndex= lowerIndex+1;
		
		final double lowerFrac = pseudoIndex - ((double) lowerIndex);
		final double upperFrac = 1-lowerFrac;
		
		
		// if the pseudo 
		if( SNAP_TOLERANCE > lowerFrac ){
			// index ~= int ... therefore:
			return values[ lowerIndex ];
		}else if( SNAP_TOLERANCE > upperFrac ){
			return values[ upperIndex ];
		}
		
		final double lowerValue = values[lowerIndex];
		final double upperValue = values[upperIndex];
		
		// return simple linear inverse interpolation
		return ( lowerValue*upperFrac + upperValue*lowerFrac );
	}
	
	
	/**
	 * for testing.  In practice, the return value should generally match the parameter value, (except for error conditions)
	 * 
	 * @ignore javadoc ignore
	 * @param motorTime
	 * @return the time at requested time
	 */
	public double getTime( final double motorTime ){
		final double pseudoIndex = getPseudoIndex( motorTime);
		final double foundTime = ThrustCurveMotor.interpolateAtIndex( this.time, pseudoIndex);
		return foundTime;
	}
	
	@Override
	public double getTotalMass( final double motorTime){
		final double pseudoIndex = getPseudoIndex( motorTime); 
		return interpolateCenterOfMassAtIndex( pseudoIndex).weight;
	}
	
	public double getPropellantMass(){
		return (getLaunchMass() - getBurnoutMass());
	}
	
	@Override
	public double getPropellantMass( final Double motorTime){
		final double pseudoIndex = getPseudoIndex( motorTime); 
		final double totalMass = interpolateCenterOfMassAtIndex( pseudoIndex).weight;
		return totalMass - this.getBurnoutMass();
	}
	
	protected Coordinate interpolateCenterOfMassAtIndex( final double pseudoIndex ){
		final double SNAP_TOLERANCE = 0.0001;
		
		final double upperFrac = pseudoIndex%1;
		final double lowerFrac = 1-upperFrac;
		final int lowerIndex = (int)pseudoIndex;
		final int upperIndex= lowerIndex+1;
		
		// if the pseudo index is close to an integer:
		if( SNAP_TOLERANCE > (1-lowerFrac) ){
			return cg[ (int) pseudoIndex ];
		}else if( SNAP_TOLERANCE > upperFrac ){
			return cg[ (int)upperIndex ];
		}
		
		final Coordinate lowerValue = cg[lowerIndex].multiply(lowerFrac);
		final Coordinate upperValue = cg[upperIndex].multiply(upperFrac);
		
		// return simple linear interpolation
		return lowerValue.average( upperValue );
	}
	
	public int getDataSize() {
		return this.time.length;
	}
	
	@Override
	public double getBurnTimeEstimate() {
		return burnTimeEstimate;
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
	
	public double getCutOffTime() {
		return time[time.length - 1];
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
		burnTimeEstimate = Math.max(burnEnd - burnStart, 0);
		
		
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
		
		if (burnTimeEstimate > 0) {
			averageThrust /= burnTimeEstimate;
		} else {
			averageThrust = 0;
		}
		
	}
	
	
	//////////  Static methods
	
	/**
	 * Return a String representation of a delay time.  If the delay is {@link #PLUGGED_DELAY},
	 * returns "P".
	 *  
	 * @param delay		the delay time.
	 * @return			the <code>String</code> representation.
	 */
	public static String getDelayString(double delay) {
		return getDelayString(delay, "P");
	}
	
	/**
	 * Return a String representation of a delay time.  If the delay is {@link #PLUGGED_DELAY},
	 * <code>plugged</code> is returned.
	 *   
	 * @param delay  	the delay time.
	 * @param plugged  	the return value if there is no ejection charge.
	 * @return			the String representation.
	 */
	public static String getDelayString(double delay, String plugged) {
		if (delay == PLUGGED_DELAY)
			return plugged;
		delay = Math.rint(delay * 10) / 10;
		if (MathUtil.equals(delay, Math.rint(delay)))
			return "" + ((int) delay);
		return "" + delay;
	}
	
	/**
	 * This is the number of data points of measured thrust, CGx, mass, time.
	 * 
	 * @return return the size of the data arrays
	 */
	public int getSampleSize(){
		return time.length;
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
