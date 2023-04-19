package net.sf.openrocket.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.aerodynamics.WarningSet;

/**
 * Tests the FlightData object.
 * 
 * @author Billy Olsen
 */
public class TestFlightData {

	/**
	 * Test method for {@link net.sf.openrocket.simulation.FlightData#FlightData()}.
	 */
	@Test
	public void testFlightData() {
		FlightData data = new FlightData();
		
		WarningSet warnings = data.getWarningSet();
		assertNotNull(warnings);
		assertTrue(warnings.isEmpty());
		
		assertEquals(0, data.getBranchCount());
		assertEquals(Double.NaN, data.getDeploymentVelocity(), 0.00);
		assertEquals(Double.NaN, data.getFlightTime(), 0.00);
		assertEquals(Double.NaN, data.getGroundHitVelocity(), 0.00);
		assertEquals(Double.NaN, data.getLaunchRodVelocity(), 0.00);
		assertEquals(Double.NaN, data.getMaxAcceleration(), 0.00);
		assertEquals(Double.NaN, data.getMaxAltitude(), 0.00);
		assertEquals(Double.NaN, data.getMaxMachNumber(), 0.00);
		assertEquals(Double.NaN, data.getTimeToApogee(), 0.00);
		
	}
	
	/**
	 * Tests flight data created from summary data.
	 */
	@Test
	public void testFlightDataFromSummaryData() {
		double deploymentVelocity = 14.8;
		double flightTime = 69.1;
		double groundHitVelocity = 3.4;
		double launchRodVelocity = 17.5;
		double maxAcceleration = 156.2;
		double maxVelocity = 105.9;
		double maxAltitude = 355.1;
		double maxMachNumber = 0.31;
		double timeToApogee = 7.96;
		
		FlightData data = new FlightData(maxAltitude, maxVelocity, maxAcceleration,
				                         maxMachNumber, timeToApogee, flightTime,
				                         groundHitVelocity, launchRodVelocity,
				                         deploymentVelocity);
		
		WarningSet warnings = data.getWarningSet();
		assertNotNull(warnings);
		assertTrue(warnings.isEmpty());
		
		assertEquals(0, data.getBranchCount());
		assertEquals(deploymentVelocity, data.getDeploymentVelocity(), 0.00);
		assertEquals(flightTime, data.getFlightTime(), 0.00);
		assertEquals(groundHitVelocity, data.getGroundHitVelocity(), 0.00);
		assertEquals(launchRodVelocity, data.getLaunchRodVelocity(), 0.00);
		assertEquals(maxAcceleration, data.getMaxAcceleration(), 0.00);
		assertEquals(maxAltitude, data.getMaxAltitude(), 0.00);
		assertEquals(maxMachNumber, data.getMaxMachNumber(), 0.00);
		assertEquals(timeToApogee, data.getTimeToApogee(), 0.00);
	}
	
	/**
	 * Test method for {@link net.sf.openrocket.simulation.FlightData#FlightData(net.sf.openrocket.simulation.FlightDataBranch[])}.
	 */
	@Test
	public void testFlightDataFlightDataBranchArray() {		
		FlightData data = new FlightData(new FlightDataBranch("Test", FlightDataType.TYPE_TIME));
		
		WarningSet warnings = data.getWarningSet();
		assertNotNull(warnings);
		assertTrue(warnings.isEmpty());
		
		assertEquals(1, data.getBranchCount());
		
		data = new FlightData(new FlightDataBranch("Test 1", FlightDataType.TYPE_TIME),
		                      new FlightDataBranch("Test 2", FlightDataType.TYPE_TIME));
		
		warnings = data.getWarningSet();
		assertNotNull(warnings);
		assertTrue(warnings.isEmpty());
		
		assertEquals(2, data.getBranchCount());
	}
	
	private FlightDataBranch createFlightDataBranch(final String name, final FlightDataType dataType, final double[] values) {
		final FlightDataBranch branch = new FlightDataBranch(name, dataType);
		addDataPoints(branch, dataType, values);
		return branch;
	}
	
	private void addDataPoints(final FlightDataBranch branch, final FlightDataType dataType, final double[] values) {
		for (int i=0; i < values.length; i++) {
			branch.addPoint();
			branch.setValue(dataType, values[i]);
		}
	}

	/**
	 * Test method for {@link net.sf.openrocket.simulation.FlightData#getMaxAltitude()}.
	 */
	@Test
	public void testGetMaxAltitudeCalculated() {
		final double[] altitudes = new double[] {
				10.5,
				37.771,
				37.5,
				5.1,
				0.0
			};
		FlightDataBranch branch = 
				createFlightDataBranch("Test Max Alt", FlightDataType.TYPE_ALTITUDE, altitudes);
		
		FlightData data = new FlightData(branch);

		assertEquals(37.771, data.getMaxAltitude(), 0.000);
	}
	
	/**
	 * Test method for {@link net.sf.openrocket.simulation.FlightData#getMaxVelocity()}.
	 */
	@Test
	public void testGetMaxVelocityCalculated() {
		final double[] velocities = new double[] {
				10.5,
				23.7,
				35.5,
				30.1,
				0.0
			};
		FlightDataBranch branch = 
				createFlightDataBranch("Test Max Velocity", FlightDataType.TYPE_VELOCITY_TOTAL, velocities);
		
		FlightData data = new FlightData(branch);

		assertEquals(35.5, data.getMaxVelocity(), 0.000);
	}
		
	/**
	 * Test method for {@link net.sf.openrocket.simulation.FlightData#getMaxMachNumber()}.
	 */
	@Test
	public void testGetMaxMachNumberCalculated() {
		final double[] machs = new double[] {
				0.1,
				0.2,
				0.333,
				0.3,
				0.1
			};
		FlightDataBranch branch = 
				createFlightDataBranch("Test Max Mach", FlightDataType.TYPE_MACH_NUMBER, machs);
		
		FlightData data = new FlightData(branch);

		assertEquals(0.333, data.getMaxMachNumber(), 0.000);
	}
	
	/**
	 * Test method for {@link net.sf.openrocket.simulation.FlightData#getFlightTime()}.
	 */
	@Test
	public void testGetFlightTime() {
		final double[] times = new double[] {
				1.0,
				5.0,
				15.0,
				20.1,
				30.2
			};
		FlightDataBranch branch =
				createFlightDataBranch("Test Flight Time", FlightDataType.TYPE_TIME, times);
		
		// Flight time is calculated as the last time entry
		FlightData data = new FlightData(branch);
		assertEquals(30.2, data.getFlightTime(), 0.000);
	}
	
	/**
	 * Test method for {@link net.sf.openrocket.simulation.FlightData#getGroundHitVelocity()}.
	 */
	@Test
	public void testGetGroundHitVelocity() {
		/*
		 * Setup a flight profile where there is data logged for every second.
		 * The time events will start at 1, rather than 0 so the time into the
		 * data log is the index + 1.0 seconds.
		 * 
		 * i.e. at second 1, the velocity becomes 1.2 m/s, the altitude of the
		 * rocket is 1.2, and the LiftOff event is saved.
		 * 
		 * This loosely approximates a flight of about 21 seconds where it
		 * flies up quickly and comes down quickly. The actual flight profile
		 * is not as important as the fact that a profile exists.
		 * 
		 * Each array is stored as 
		 */
		final double[] velocities = new double[] {
				// launch to burn out
				1.2, 15.0, 31.1, 45.6,
				// burn out to apogee
				33.6, 21.2, 14.1, 6.8, 0.0,
				// apogee to ejection charge
				2.4, 4.7, 8.6, 9.23,
				// ejection charge to parachute deployment
				7.1, 6.2, 6.2, 6.2, 6.2,
				// parachute deployment to ground hit
				6.2, 0.0, 0.0
			};
		final double[] altitudes = new double[] {
				// launch to burn out
				1.2, 16.2, 47.3, 92.9,
				// burn out to apogee
				126.5, 147.7, 161.8, 168.6, 168.6,
				// apogee to ejection charge
				166.2, 161.5, 152.9, 143.67,
				// ejection charge to parachute deployment
				136.57, 113.81, 91.05, 68.29, 45.53,
				// parachute deployment to ground hit
				22.77, 0.0, 0.0
			};
		final FlightEvent.Type[] eventTypes = new FlightEvent.Type[] {
				// launch to burn out
				FlightEvent.Type.LIFTOFF, FlightEvent.Type.LAUNCHROD,
				FlightEvent.Type.ALTITUDE, FlightEvent.Type.BURNOUT,
				
				// burn out to apogee
				FlightEvent.Type.ALTITUDE, FlightEvent.Type.ALTITUDE,
				FlightEvent.Type.ALTITUDE, FlightEvent.Type.ALTITUDE,
				FlightEvent.Type.APOGEE,
				
				// apogee to ejection charge
				FlightEvent.Type.ALTITUDE, FlightEvent.Type.ALTITUDE,
				FlightEvent.Type.ALTITUDE, FlightEvent.Type.EJECTION_CHARGE,
				
				// ejection charge to parachute deployment
				FlightEvent.Type.ALTITUDE, FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
				FlightEvent.Type.ALTITUDE, FlightEvent.Type.ALTITUDE,
				FlightEvent.Type.ALTITUDE,
				
				// parachute deployment to ground hit
				FlightEvent.Type.GROUND_HIT, FlightEvent.Type.ALTITUDE,
				FlightEvent.Type.SIMULATION_END
			};
		
		assertEquals(velocities.length, eventTypes.length);
		assertEquals(velocities.length, altitudes.length);
		
		// This flight data branch only needs to record for time,
		// altitude and velocity.
		FlightDataBranch branch =
				new FlightDataBranch("Ground Hit Velocities",
				                     FlightDataType.TYPE_TIME,
				                     FlightDataType.TYPE_ALTITUDE,
				                     FlightDataType.TYPE_VELOCITY_TOTAL);
		
		for (int i = 0; i < velocities.length; i++) {
			branch.addPoint();
			// the data entries are 1 second ahead of the index
			double time = i + 1.0;
			branch.setValue(FlightDataType.TYPE_TIME, time);
			branch.setValue(FlightDataType.TYPE_ALTITUDE, altitudes[i]);
			branch.setValue(FlightDataType.TYPE_VELOCITY_TOTAL, velocities[i]);
			branch.addEvent(new FlightEvent(eventTypes[i], time));
		}
		
		FlightData data = new FlightData(branch);
		
		assertEquals(6.2, data.getGroundHitVelocity(), 0.000);
	}
	
}
