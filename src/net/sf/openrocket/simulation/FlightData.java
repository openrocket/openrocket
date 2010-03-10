package net.sf.openrocket.simulation;

import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;


public class FlightData {
	
	/**
	 * An immutable FlightData object with NaN data.
	 */
	public static final FlightData NaN_DATA;
	static {
		FlightData data = new FlightData();
		data.immute();
		NaN_DATA = data;
	}

	private boolean mutable = true;
	private final ArrayList<FlightDataBranch> branches = new ArrayList<FlightDataBranch>();
	
	private final WarningSet warnings = new WarningSet();
	
	private double maxAltitude = Double.NaN;
	private double maxVelocity = Double.NaN;
	private double maxAcceleration = Double.NaN;
	private double maxMachNumber = Double.NaN;
	private double timeToApogee = Double.NaN;
	private double flightTime = Double.NaN;
	private double groundHitVelocity = Double.NaN;

	
	/**
	 * Create a FlightData object with no content.  The resulting object is mutable.
	 */
	public FlightData() {
		
	}
	
	
	/**
	 * Construct an immutable FlightData object with no data branches but the specified
	 * summary information.
	 * 
	 * @param maxAltitude			maximum altitude.
	 * @param maxVelocity			maximum velocity.
	 * @param maxAcceleration		maximum acceleration.
	 * @param maxMachNumber			maximum Mach number.
	 * @param timeToApogee			time to apogee.
	 * @param flightTime			total flight time.
	 * @param groundHitVelocity		ground hit velocity.
	 */
	public FlightData(double maxAltitude, double maxVelocity, double maxAcceleration,
			double maxMachNumber, double timeToApogee, double flightTime,
			double groundHitVelocity) {
		this.maxAltitude = maxAltitude;
		this.maxVelocity = maxVelocity;
		this.maxAcceleration = maxAcceleration;
		this.maxMachNumber = maxMachNumber;
		this.timeToApogee = timeToApogee;
		this.flightTime = flightTime;
		this.groundHitVelocity = groundHitVelocity;
		
		this.immute();
	}


	/**
	 * Create an immutable FlightData object with the specified branches.
	 * 
	 * @param branches	the branches.
	 */
	public FlightData(FlightDataBranch ... branches) {
		this();
		
		for (FlightDataBranch b: branches)
			this.addBranch(b);
		
		calculateIntrestingValues();
		this.immute();
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
		if (!mutable)
			throw new IllegalStateException("FlightData has been made immutable");

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
	
	
	
	/**
	 * Calculate the max. altitude/velocity/acceleration, time to apogee, flight time
	 * and ground hit velocity.
	 */
	private void calculateIntrestingValues() {
		if (branches.isEmpty())
			return;
		
		FlightDataBranch branch = branches.get(0);
		maxAltitude = branch.getMaximum(FlightDataBranch.TYPE_ALTITUDE);
		maxVelocity = branch.getMaximum(FlightDataBranch.TYPE_VELOCITY_TOTAL);
		maxMachNumber = branch.getMaximum(FlightDataBranch.TYPE_MACH_NUMBER);

		flightTime = branch.getLast(FlightDataBranch.TYPE_TIME);
		if (branch.getLast(FlightDataBranch.TYPE_ALTITUDE) < 10) {
			groundHitVelocity = branch.getLast(FlightDataBranch.TYPE_VELOCITY_TOTAL);
		} else {
			groundHitVelocity = Double.NaN;
		}
		
		// Time to apogee
		List<Double> time = branch.get(FlightDataBranch.TYPE_TIME);
		List<Double> altitude = branch.get(FlightDataBranch.TYPE_ALTITUDE);
		
		if (time == null || altitude == null) {
			timeToApogee = Double.NaN;
			maxAcceleration = Double.NaN;
			return;
		}
		int index = 0;
		for (Double alt: altitude) {
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

		// Max. acceleration (must be after apogee time)
		if (branch.get(FlightDataBranch.TYPE_ACCELERATION_TOTAL) != null) {
			maxAcceleration = calculateMaxAcceleration();
		} else {
			maxAcceleration = Double.NaN;
		}
	}


	public void immute() {
		mutable = false;
	}
	public boolean isMutable() {
		return mutable;
	}
	
	

	/**
	 * Find the maximum acceleration before apogee.
	 */
	private double calculateMaxAcceleration() {
		
		// End check at first recovery device deployment
		double endTime = Double.MAX_VALUE;
		FlightDataBranch branch = this.getBranch(0);
		for (Pair<Double, FlightEvent> event: branch.getEvents()) {
			if (event.getV().getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
				if (event.getV().getTime() < endTime) {
					endTime = event.getV().getTime();
				}
			}
		}
		
		List<Double> time = branch.get(FlightDataBranch.TYPE_TIME);
		List<Double> acceleration = branch.get(FlightDataBranch.TYPE_ACCELERATION_TOTAL);
		
		if (time == null || acceleration == null) {
			return Double.NaN;
		}
		
		double max = 0;
		
		for (int i=0; i<time.size(); i++) {
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
