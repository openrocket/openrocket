package net.sf.openrocket.simulation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Mutable;

/**
 * A collection of various flight data.  This is the result of a simulation, or importing
 * data into the software.  The data includes:
 * <ul>
 * 	<li>A number of generally interesting values of a simulation, such as max. altitude and velocity
 * 	<li>A number (or zero) of flight data branches containing the actual data
 * 	<li>A WarningSet including warnings that occurred during simulation
 * </ul> 
 * <p>
 * A FlightData object can be made immutable by calling {@link #immute()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlightData {
	private static final Logger log = LoggerFactory.getLogger(FlightData.class);
	
	/**
	 * An immutable FlightData object with NaN data.
	 */
	public static final FlightData NaN_DATA;
	static {
		FlightData data = new FlightData();
		data.immute();
		NaN_DATA = data;
	}
	
	private Mutable mutable = new Mutable();
	
	private final ArrayList<FlightDataBranch> branches = new ArrayList<FlightDataBranch>();
	
	private final WarningSet warnings = new WarningSet();
	
	private double maxAltitude = Double.NaN;
	private double maxVelocity = Double.NaN;
	private double maxAcceleration = Double.NaN;
	private double maxMachNumber = Double.NaN;
	private double timeToApogee = Double.NaN;
	private double flightTime = Double.NaN;
	private double groundHitVelocity = Double.NaN;
	private double launchRodVelocity = Double.NaN;
	private double deploymentVelocity = Double.NaN;
	
	
	/**
	 * Create a FlightData object with no content.  The resulting object is mutable.
	 */
	public FlightData() {
		
	}
	
	
	/**
	 * Construct a FlightData object with no data branches but the specified
	 * summary information.  The resulting object is mutable.
	 * 
	 * @param maxAltitude			maximum altitude.
	 * @param maxVelocity			maximum velocity.
	 * @param maxAcceleration		maximum acceleration.
	 * @param maxMachNumber			maximum Mach number.
	 * @param timeToApogee			time to apogee.
	 * @param flightTime			total flight time.
	 * @param groundHitVelocity		ground hit velocity.
	 * @param launchRodVelocity     velocity at launch rod clearance
	 * @param deploymentVelocity    velocity at deployment
	 */
	public FlightData(double maxAltitude, double maxVelocity, double maxAcceleration,
			double maxMachNumber, double timeToApogee, double flightTime,
			double groundHitVelocity, double launchRodVelocity, double deploymentVelocity) {
		this.maxAltitude = maxAltitude;
		this.maxVelocity = maxVelocity;
		this.maxAcceleration = maxAcceleration;
		this.maxMachNumber = maxMachNumber;
		this.timeToApogee = timeToApogee;
		this.flightTime = flightTime;
		this.groundHitVelocity = groundHitVelocity;
		this.launchRodVelocity = launchRodVelocity;
		this.deploymentVelocity = deploymentVelocity;
	}
	
	
	/**
	 * Create a FlightData object with the specified branches.  The resulting object is mutable.
	 * 
	 * @param branches	the branches.
	 */
	public FlightData(FlightDataBranch... branches) {
		this();
		
		for (FlightDataBranch b : branches)
			this.addBranch(b);
		
		calculateIntrestingValues();
	}
	
	


	/**
	 * Returns the warning set associated with this object.  This WarningSet cannot be
	 * set, so simulations must use this warning set to store their warnings.
	 * The returned WarningSet should not be modified otherwise.
	 * 
	 * @return	the warnings generated during this simulation.
	 */
	public WarningSet getWarningSet() {
		return warnings;
	}
	
	
	public void addBranch(FlightDataBranch branch) {
		mutable.check();
		
		branch.immute();
		branches.add(branch);
		
		if (branches.size() == 1) {
			calculateIntrestingValues();
		}
	}
	
	public int getBranchCount() {
		return branches.size();
	}
	
	public FlightDataBranch getBranch(int n) {
		return branches.get(n);
	}
	
	

	public double getMaxAltitude() {
		return maxAltitude;
	}
	
	public double getMaxVelocity() {
		return maxVelocity;
	}
	
	/**
	 * NOTE:  This value only takes into account flight phase.
	 */
	public double getMaxAcceleration() {
		return maxAcceleration;
	}
	
	public double getMaxMachNumber() {
		return maxMachNumber;
	}
	
	public double getTimeToApogee() {
		return timeToApogee;
	}
	
	public double getFlightTime() {
		return flightTime;
	}
	
	public double getGroundHitVelocity() {
		return groundHitVelocity;
	}
	
	public double getLaunchRodVelocity() {
		return launchRodVelocity;
	}
	

	public double getDeploymentVelocity() {
		return deploymentVelocity;
	}


	/**
	 * Calculate the max. altitude/velocity/acceleration, time to apogee, flight time
	 * and ground hit velocity.
	 */
	private void calculateIntrestingValues() {
		if (branches.isEmpty())
			return;
		
		FlightDataBranch branch = branches.get(0);
		maxAltitude = branch.getMaximum(FlightDataType.TYPE_ALTITUDE);
		maxVelocity = branch.getMaximum(FlightDataType.TYPE_VELOCITY_TOTAL);
		maxMachNumber = branch.getMaximum(FlightDataType.TYPE_MACH_NUMBER);
		
		flightTime = branch.getLast(FlightDataType.TYPE_TIME);
		if (branch.getLast(FlightDataType.TYPE_ALTITUDE) < 10) {
			groundHitVelocity = branch.getLast(FlightDataType.TYPE_VELOCITY_TOTAL);
		} else {
			groundHitVelocity = Double.NaN;
		}
		

		// Time to apogee
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);
		List<Double> altitude = branch.get(FlightDataType.TYPE_ALTITUDE);
		
		if (time == null || altitude == null) {
			timeToApogee = Double.NaN;
			maxAcceleration = Double.NaN;
			return;
		}
		int index = 0;
		for (Double alt : altitude) {
			if (alt != null) {
				if (MathUtil.equals(alt, maxAltitude))
					break;
			}
			
			index++;
		}
		if (index < time.size())
			timeToApogee = time.get(index);
		else
			timeToApogee = Double.NaN;
		

		// Launch rod velocity
		for (FlightEvent event : branch.getEvents()) {
			if (event.getType() == FlightEvent.Type.LAUNCHROD) {
				double t = event.getTime();
				List<Double> velocity = branch.get(FlightDataType.TYPE_VELOCITY_TOTAL);
				launchRodVelocity = MathUtil.interpolate( time, velocity, t);
			} else if ( event.getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
				double t = event.getTime();
				List<Double> velocity = branch.get(FlightDataType.TYPE_VELOCITY_TOTAL);
				deploymentVelocity = MathUtil.interpolate( time, velocity, t);
			}
		}
		
		// Max. acceleration (must be after apogee time)
		if (branch.get(FlightDataType.TYPE_ACCELERATION_TOTAL) != null) {
			maxAcceleration = calculateMaxAcceleration();
		} else {
			maxAcceleration = Double.NaN;
		}
		
		log.debug("Computed flight values:" +
				" maxAltitude=" + maxAltitude +
				" maxVelocity=" + maxVelocity +
				" maxAcceleration=" + maxAcceleration +
				" maxMachNumber=" + maxMachNumber +
				" timeToApogee=" + timeToApogee +
				" flightTime=" + flightTime +
				" groundHitVelocity=" + groundHitVelocity +
				" launchRodVelocity=" + launchRodVelocity);
	}
	
	
	public void immute() {
		mutable.immute();
		warnings.immute();
		for (FlightDataBranch b : branches) {
			b.immute();
		}
	}
	
	
	public boolean isMutable() {
		return mutable.isMutable();
	}
	
	

	/**
	 * Find the maximum acceleration before apogee.
	 */
	private double calculateMaxAcceleration() {
		
		// End check at first recovery device deployment
		double endTime = Double.MAX_VALUE;
		
		FlightDataBranch branch = this.getBranch(0);
		for (FlightEvent event : branch.getEvents()) {
			if (event.getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
				if (event.getTime() < endTime) {
					endTime = event.getTime();
				}
			}
		}
		
		List<Double> time = branch.get(FlightDataType.TYPE_TIME);
		List<Double> acceleration = branch.get(FlightDataType.TYPE_ACCELERATION_TOTAL);
		
		if (time == null || acceleration == null) {
			return Double.NaN;
		}
		
		double max = 0;
		
		for (int i = 0; i < time.size(); i++) {
			if (time.get(i) >= endTime) {
				break;
			}
			double a = acceleration.get(i);
			if (a > max)
				max = a;
		}
		
		return max;
	}
}
