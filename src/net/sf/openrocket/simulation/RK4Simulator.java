package net.sf.openrocket.simulation;

import java.util.Collection;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.AtmosphericConditions;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.GravityModel;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.aerodynamics.WindSimulator;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Quaternion;
import net.sf.openrocket.util.Rotation2D;


public class RK4Simulator extends FlightSimulator {
	
	/**
	 * A recommended reasonably accurate time step.
	 */
	public static final double RECOMMENDED_TIME_STEP = 0.05;
	
	/**
	 * A recommended maximum angle step value.
	 */
	public static final double RECOMMENDED_ANGLE_STEP = 3*Math.PI/180;
	
	/**
	 * Maximum roll step allowed.  This is selected as an uneven division of the full
	 * circle so that the simulation will sample the most wind directions
	 */
	private static final double MAX_ROLL_STEP_ANGLE = 28.32 * Math.PI/180;
//	private static final double MAX_ROLL_STEP_ANGLE = 8.32 * Math.PI/180;
	
	private static final double MAX_ROLL_RATE_CHANGE = 2 * Math.PI/180;
	private static final double MAX_PITCH_CHANGE = 2 * Math.PI/180;

	
	private static final boolean DEBUG = false;

	
	/* Single instance so it doesn't have to be created each semi-step. */
	private final FlightConditions flightConditions = new FlightConditions(null);
	
	
	private Coordinate linearAcceleration;
	private Coordinate angularAcceleration;
	
	// set by calculateFlightConditions and calculateAcceleration:
	private double timestep;
	private double oldTimestep;
	private AerodynamicForces forces;
	private double windSpeed;
	private double thrustForce, dragForce;
	private double lateralPitchRate = 0;
	
	private double rollAcceleration = 0;
	private double lateralPitchAcceleration = 0;
	
	private double maxVelocityZ = 0;
	private double startWarningTime = -1;
	
	private Rotation2D thetaRotation;

	
	public RK4Simulator() {
		super();
	}
	
	
	public RK4Simulator(AerodynamicCalculator calculator) {
		super(calculator);
	}

	

	

	@Override
	protected RK4SimulationStatus initializeSimulation(Configuration configuration, 
			SimulationConditions simulation) {

		RK4SimulationStatus status = new RK4SimulationStatus();
		
		status.startConditions = simulation;
		
		status.configuration = configuration;
		// TODO: LOW: Branch names
		status.flightData = new FlightDataBranch("Main", FlightDataBranch.TYPE_TIME);
		status.launchRod = true;
		status.time = 0.0;
		status.simulationStartTime = System.nanoTime();
		
		status.launchRodDirection = new Coordinate(
				Math.sin(simulation.getLaunchRodAngle()) * 
				Math.cos(simulation.getLaunchRodDirection()),
				Math.sin(simulation.getLaunchRodAngle()) *
				Math.sin(simulation.getLaunchRodDirection()),
				Math.cos(simulation.getLaunchRodAngle())
		);
		status.launchRodLength = simulation.getLaunchRodLength();
		
		// Take into account launch lug positions
		double lugPosition = Double.NaN;
		for (RocketComponent c: configuration) {
			if (c instanceof LaunchLug) {
				double pos = c.toAbsolute(new Coordinate(c.getLength()))[0].x;
				if (Double.isNaN(lugPosition) || pos > lugPosition) {
					lugPosition = pos;
				}
			}
		}
		if (!Double.isNaN(lugPosition)) {
			double maxX = 0;
			for (Coordinate c: configuration.getBounds()) {
				if (c.x > maxX)
					maxX = c.x;
			}
			if (maxX >= lugPosition) {
				status.launchRodLength = Math.max(0,
						status.launchRodLength - (maxX - lugPosition));
			}
		}
		
		
		Quaternion o = new Quaternion();
		o.multiplyLeft(Quaternion.rotation(
				new Coordinate(0, simulation.getLaunchRodAngle(), 0)));
		o.multiplyLeft(Quaternion.rotation(
				new Coordinate(0, 0, simulation.getLaunchRodDirection())));
		status.orientation = o;
		status.position = Coordinate.NUL;
		status.velocity = Coordinate.NUL;
		status.rotation = Coordinate.NUL;
		
		/*
		 * Force a very small deviation to the wind speed to avoid insanely
		 * perfect conditions (rocket dropping at exactly 180 deg AOA).
		 */
		status.windSimulator = new WindSimulator();
		status.windSimulator.setAverage(simulation.getWindSpeedAverage());
		status.windSimulator.setStandardDeviation(
				Math.max(simulation.getWindSpeedDeviation(), 0.005));
//		status.windSimulator.reset();
		
		status.gravityModel = new GravityModel(simulation.getLaunchLatitude());

		rollAcceleration = 0;
		lateralPitchAcceleration = 0;
		oldTimestep = -1;
		maxVelocityZ = 0;
		startWarningTime = -1;
		
		return status;
	}
	
	

	@Override
	protected Collection<FlightEvent> step(SimulationConditions simulation, 
			SimulationStatus simulationStatus) throws SimulationException {
		
		RK4SimulationStatus status = (RK4SimulationStatus)simulationStatus;
		
		////////  Perform RK4 integration:  ////////
		
		Coordinate k1a, k1v, k1ra, k1rv; // Acceleration, velocity, rot.acc, rot.vel
		Coordinate k2a, k2v, k2ra, k2rv;
		Coordinate k3a, k3v, k3ra, k3rv;
		Coordinate k4a, k4v, k4ra, k4rv;
		RK4SimulationStatus status2;
		
		
		// Calculate time step and store data after first call to calculateFlightConditions
		calculateFlightConditions(status);

		
		/*
		 * Select the time step to use.  It is the minimum of the following:
		 *  1. the user-specified time step
		 *  2. the maximum pitch step angle limit
		 *  3. the maximum roll step angle limit
		 *  4. the maximum roll rate change limit (using previous step acceleration)
		 *  5. the maximum pitch change limit (using previous step acceleration)
		 * 
		 * The last two are required since near the steady-state roll rate the roll rate
		 * may oscillate significantly even between the sub-steps of the RK4 integration.
		 * 
		 * Additionally a low-pass filter is applied to the time step selectively 
		 * if the new time step is longer than the previous time step.
		 */
		double dt1 = simulation.getTimeStep();
		double dt2 = simulation.getMaximumStepAngle() / lateralPitchRate;
		double dt3 = Math.abs(MAX_ROLL_STEP_ANGLE / flightConditions.getRollRate());
		double dt4 = Math.abs(MAX_ROLL_RATE_CHANGE / rollAcceleration);
		double dt5 = Math.abs(MAX_PITCH_CHANGE / lateralPitchAcceleration);
		timestep = MathUtil.min(dt1,dt2,dt3);
		timestep = MathUtil.min(timestep,dt4,dt5);
		
		if (oldTimestep > 0 && oldTimestep < timestep) {
			timestep = 0.3*timestep + 0.7*oldTimestep;
		}
		
		if (timestep < 0.001)
			timestep = 0.001;
		
		oldTimestep = timestep;
		if (DEBUG)
			System.out.printf("Time step: %.3f  dt1=%.3f dt2=%.3f dt3=%.3f dt4=%.3f dt5=%.3f\n",
			timestep,dt1,dt2,dt3,dt4,dt5);

		
		
		//// First position, k1 = f(t, y)

		//calculateFlightConditions already called
		calculateAcceleration(status);
		k1a = linearAcceleration;
		k1ra = angularAcceleration;
		k1v = status.velocity;
		k1rv = status.rotation;
		

		// Store the flight information
		storeData(status);
		
		
		
		//// Second position, k2 = f(t + h/2, y + k1*h/2)
		
		status2 = status.clone();
		status2.time = status.time + timestep/2;
		status2.position = status.position.add(k1v.multiply(timestep/2));
		status2.velocity = status.velocity.add(k1a.multiply(timestep/2));
		status2.orientation.multiplyLeft(Quaternion.rotation(k1rv.multiply(timestep/2)));
		status2.rotation = status.rotation.add(k1ra.multiply(timestep/2));
		
		calculateFlightConditions(status2);
		calculateAcceleration(status2);
		k2a = linearAcceleration;
		k2ra = angularAcceleration;
		k2v = status2.velocity;
		k2rv = status2.rotation;
		
		
		//// Third position, k3 = f(t + h/2, y + k2*h/2)
		
		status2.orientation = status.orientation.clone();  // All others are set explicitly
		status2.position = status.position.add(k2v.multiply(timestep/2));
		status2.velocity = status.velocity.add(k2a.multiply(timestep/2));
		status2.orientation.multiplyLeft(Quaternion.rotation(k2rv.multiply(timestep/2)));
		status2.rotation = status.rotation.add(k2ra.multiply(timestep/2));
		
		calculateFlightConditions(status2);
		calculateAcceleration(status2);
		k3a = linearAcceleration;
		k3ra = angularAcceleration;
		k3v = status2.velocity;
		k3rv = status2.rotation;

		
		
		//// Fourth position, k4 = f(t + h, y + k3*h)
		
		status2.orientation = status.orientation.clone();  // All others are set explicitly
		status2.time = status.time + timestep;
		status2.position = status.position.add(k3v.multiply(timestep));
		status2.velocity = status.velocity.add(k3a.multiply(timestep));
		status2.orientation.multiplyLeft(Quaternion.rotation(k3rv.multiply(timestep)));
		status2.rotation = status.rotation.add(k3ra.multiply(timestep));
		
		calculateFlightConditions(status2);
		calculateAcceleration(status2);
		k4a = linearAcceleration;
		k4ra = angularAcceleration;
		k4v = status2.velocity;
		k4rv = status2.rotation;
		
		
		
		//// Sum all together,  y(n+1) = y(n) + h*(k1 + 2*k2 + 2*k3 + k4)/6

		Coordinate deltaV, deltaP, deltaR, deltaO;
		deltaV = k2a.add(k3a).multiply(2).add(k1a).add(k4a).multiply(timestep/6);
		deltaP = k2v.add(k3v).multiply(2).add(k1v).add(k4v).multiply(timestep/6);
		deltaR = k2ra.add(k3ra).multiply(2).add(k1ra).add(k4ra).multiply(timestep/6);
		deltaO = k2rv.add(k3rv).multiply(2).add(k1rv).add(k4rv).multiply(timestep/6);
		
		if (DEBUG)
			System.out.println("Rot.Acc: "+deltaR+"  k1:"+k1ra+"  k2:"+k2ra+"  k3:"+k3ra+
					"  k4:"+k4ra);

		status.velocity = status.velocity.add(deltaV);
		status.position = status.position.add(deltaP);
		status.rotation = status.rotation.add(deltaR);
		status.orientation.multiplyLeft(Quaternion.rotation(deltaO));


		status.orientation.normalizeIfNecessary();
		
		status.time = status.time + timestep;

		
		return null;
	}


	
	/**
	 * Calculate the linear and angular acceleration at the given status.  The results
	 * are stored in the fields {@link #linearAcceleration} and {@link #angularAcceleration}.
	 *  
	 * @param status   the status of the rocket.
	 * @throws SimulationException 
	 */
	private void calculateAcceleration(RK4SimulationStatus status) throws SimulationException {
		
		/**
		 * Check whether to store warnings or not.  Warnings are ignored when on the 
		 * launch rod or 0.25 seconds after departure, and when the velocity has dropped
		 * below 20% of the max. velocity.
		 */
		WarningSet warnings = status.warnings;
		maxVelocityZ = MathUtil.max(maxVelocityZ, status.velocity.z);
		if (status.launchRod) {
			warnings = null;
		} else {
			if (status.velocity.z < 0.2 * maxVelocityZ)
				warnings = null;
			if (startWarningTime < 0)
				startWarningTime = status.time + 0.25;
		}
		if (status.time < startWarningTime)
			warnings = null;
		
		
		// Calculate aerodynamic forces  (only axial if still on launch rod)
		calculator.setConfiguration(status.configuration);

		if (status.launchRod) {
			forces = calculator.getAxialForces(status.time, flightConditions, warnings);
		} else {
			forces = calculator.getAerodynamicForces(status.time, flightConditions, warnings);
		}
		
		
		// Allow listeners to modify the forces
		int mod = flightConditions.getModCount();
		SimulationListener[] list = listeners.toArray(new SimulationListener[0]);
		for (SimulationListener l: list) {
			l.forceCalculation(status, flightConditions, forces);
		}
		if (flightConditions.getModCount() != mod) {
			status.warnings.add(Warning.LISTENERS_AFFECTED);
		}

		
		assert(!Double.isNaN(forces.CD));
		assert(!Double.isNaN(forces.CN));
		assert(!Double.isNaN(forces.Caxial));
		assert(!Double.isNaN(forces.Cm));
		assert(!Double.isNaN(forces.Cyaw));
		assert(!Double.isNaN(forces.Cside));
		assert(!Double.isNaN(forces.Croll));
		

		////////  Calculate forces and accelerations  ////////
		
		double dynP = (0.5 * flightConditions.getAtmosphericConditions().getDensity() *
				MathUtil.pow2(flightConditions.getVelocity()));
		double refArea = flightConditions.getRefArea();
		double refLength = flightConditions.getRefLength();
		
		
		// Linear forces
		thrustForce = calculateThrust(status, timestep);
		dragForce = forces.Caxial * dynP * refArea;
		double fN = forces.CN * dynP * refArea;
		double fSide = forces.Cside * dynP * refArea;
		
//		double sin = Math.sin(flightConditions.getTheta());
//		double cos = Math.cos(flightConditions.getTheta());
		
//		double forceX = - fN * cos - fSide * sin;
//		double forceY = - fN * sin - fSide * cos;
		double forceZ = thrustForce - dragForce;

		
//		linearAcceleration = new Coordinate(forceX / forces.cg.weight,
//				forceY / forces.cg.weight, forceZ / forces.cg.weight);
		linearAcceleration = new Coordinate(-fN / forces.cg.weight, -fSide / forces.cg.weight,
				forceZ / forces.cg.weight);
		
		linearAcceleration = thetaRotation.rotateZ(linearAcceleration);
		linearAcceleration = status.orientation.rotate(linearAcceleration);

		linearAcceleration = linearAcceleration.sub(0, 0, status.gravityModel.getGravity());
		
		
		// If still on launch rod, project acceleration onto launch rod direction and
		// set angular acceleration to zero.
		if (status.launchRod) {
			linearAcceleration = status.launchRodDirection.multiply(
					linearAcceleration.dot(status.launchRodDirection));
			angularAcceleration = Coordinate.NUL;
			rollAcceleration = 0;
			lateralPitchAcceleration = 0;
			return;
		}
				
		
		// Convert momenta
		double Cm = forces.Cm - forces.CN * forces.cg.x / refLength;
		double Cyaw = forces.Cyaw - forces.Cside * forces.cg.x / refLength;
		
//		double momX = (-Cm * sin - Cyaw * cos) * dynP * refArea * refLength;
//		double momY = ( Cm * cos - Cyaw * sin) * dynP * refArea * refLength;
		double momX = -Cyaw * dynP * refArea * refLength;
		double momY = Cm * dynP * refArea * refLength;
		
		double momZ = forces.Croll * dynP * refArea * refLength;
		if (DEBUG)
			System.out.printf("Croll:  %.3f  dynP=%.3f  momZ=%.3f\n",forces.Croll,dynP,momZ);
		
		assert(!Double.isNaN(momX));
		assert(!Double.isNaN(momY));
		assert(!Double.isNaN(momZ));
		assert(!Double.isNaN(forces.longitudalInertia));
		assert(!Double.isNaN(forces.rotationalInertia));
		
		angularAcceleration = new Coordinate(momX / forces.longitudalInertia,
				momY / forces.longitudalInertia, momZ / forces.rotationalInertia);

		rollAcceleration = angularAcceleration.z;
		// TODO: LOW: This should be hypot, but does it matter?
		lateralPitchAcceleration = MathUtil.max(Math.abs(angularAcceleration.x), 
				Math.abs(angularAcceleration.y));
		
		if (DEBUG)
			System.out.println("rot.inertia = "+forces.rotationalInertia);
		
		angularAcceleration = thetaRotation.rotateZ(angularAcceleration);
		
		angularAcceleration = status.orientation.rotate(angularAcceleration);
	}
	
	
	/**
	 * Calculate the flight conditions for the current rocket status.  The conditions
	 * are stored in the field {@link #flightConditions}.  Additional information that
	 * is calculated and will be stored in the flight data is also computed into the
	 * suitable fields.
	 * @throws SimulationException 
	 */
	private void calculateFlightConditions(RK4SimulationStatus status) throws SimulationException {

		flightConditions.setReference(status.configuration);

		
		//// Atmospheric conditions
		AtmosphericConditions atmosphere = status.startConditions.getAtmosphericModel().
			getConditions(status.position.z + status.startConditions.getLaunchAltitude());
		flightConditions.setAtmosphericConditions(atmosphere);

		
		//// Local wind speed and direction
		windSpeed = status.windSimulator.getWindSpeed(status.time);
		Coordinate airSpeed = status.velocity.add(windSpeed, 0, 0);
		airSpeed = status.orientation.invRotate(airSpeed);
		
		
        // Lateral direction:
        double len = MathUtil.hypot(airSpeed.x, airSpeed.y);
        if (len > 0.0001) {
            thetaRotation = new Rotation2D(airSpeed.y/len, airSpeed.x/len);
            flightConditions.setTheta(Math.atan2(airSpeed.y, airSpeed.x));
        } else {
            thetaRotation = Rotation2D.ID;
            flightConditions.setTheta(0);
        }

		double velocity = airSpeed.length();
        flightConditions.setVelocity(velocity);
        if (velocity > 0.01) {
            // aoa must be calculated from the monotonous cosine
            // sine can be calculated by a simple division
            flightConditions.setAOA(Math.acos(airSpeed.z / velocity), len / velocity);
        } else {
            flightConditions.setAOA(0);
        }
		
		
		// Roll, pitch and yaw rate
		Coordinate rot = status.orientation.invRotate(status.rotation);
		rot = thetaRotation.invRotateZ(rot);
		
		flightConditions.setRollRate(rot.z);
		if (len < 0.001) {
			flightConditions.setPitchRate(0);
			flightConditions.setYawRate(0);
			lateralPitchRate = 0;
		} else {
			flightConditions.setPitchRate(rot.y);
			flightConditions.setYawRate(rot.x);
			// TODO: LOW: set this as power of two?
			lateralPitchRate = MathUtil.hypot(rot.x, rot.y);
		}
		
		
		// Allow listeners to modify the conditions
		int mod = flightConditions.getModCount();
		SimulationListener[] list = listeners.toArray(new SimulationListener[0]);
		for (SimulationListener l: list) {
			l.flightConditions(status, flightConditions);
		}
		if (mod != flightConditions.getModCount()) {
			// Re-calculate cached values
			thetaRotation = new Rotation2D(flightConditions.getTheta());
			lateralPitchRate = MathUtil.hypot(flightConditions.getPitchRate(),
					flightConditions.getYawRate());
			status.warnings.add(Warning.LISTENERS_AFFECTED);
		}
		
	}
	
	
	
	private void storeData(RK4SimulationStatus status) {
		FlightDataBranch data = status.flightData;
		boolean extra = status.startConditions.getCalculateExtras();
		
		data.addPoint();
		data.setValue(FlightDataBranch.TYPE_TIME, status.time);
		data.setValue(FlightDataBranch.TYPE_ALTITUDE, status.position.z);
		data.setValue(FlightDataBranch.TYPE_POSITION_X, status.position.x);
		data.setValue(FlightDataBranch.TYPE_POSITION_Y, status.position.y);
		
		if (extra) {
			data.setValue(FlightDataBranch.TYPE_POSITION_XY, 
					MathUtil.hypot(status.position.x, status.position.y));
			data.setValue(FlightDataBranch.TYPE_POSITION_DIRECTION, 
					Math.atan2(status.position.y, status.position.x));
			
			data.setValue(FlightDataBranch.TYPE_VELOCITY_XY, 
					MathUtil.hypot(status.velocity.x, status.velocity.y));
			data.setValue(FlightDataBranch.TYPE_ACCELERATION_XY, 
					MathUtil.hypot(linearAcceleration.x, linearAcceleration.y));
			
			data.setValue(FlightDataBranch.TYPE_ACCELERATION_TOTAL,linearAcceleration.length());
			
			double Re = flightConditions.getVelocity() * 
					calculator.getConfiguration().getLength() / 
					flightConditions.getAtmosphericConditions().getKinematicViscosity();
			data.setValue(FlightDataBranch.TYPE_REYNOLDS_NUMBER, Re);
		}
		
		data.setValue(FlightDataBranch.TYPE_VELOCITY_Z, status.velocity.z);
		data.setValue(FlightDataBranch.TYPE_ACCELERATION_Z, linearAcceleration.z);
		
		data.setValue(FlightDataBranch.TYPE_VELOCITY_TOTAL, flightConditions.getVelocity());
		data.setValue(FlightDataBranch.TYPE_MACH_NUMBER, flightConditions.getMach());

		if (!status.launchRod) {
			data.setValue(FlightDataBranch.TYPE_CP_LOCATION, forces.cp.x);
			data.setValue(FlightDataBranch.TYPE_CG_LOCATION, forces.cg.x);
			data.setValue(FlightDataBranch.TYPE_STABILITY, 
					(forces.cp.x - forces.cg.x) / flightConditions.getRefLength());
		}
		data.setValue(FlightDataBranch.TYPE_MASS, forces.cg.weight);
		
		data.setValue(FlightDataBranch.TYPE_THRUST_FORCE, thrustForce);
		data.setValue(FlightDataBranch.TYPE_DRAG_FORCE, dragForce);
		
		if (!status.launchRod) {
			data.setValue(FlightDataBranch.TYPE_PITCH_MOMENT_COEFF,
					forces.Cm - forces.CN * forces.cg.x / flightConditions.getRefLength());
			data.setValue(FlightDataBranch.TYPE_YAW_MOMENT_COEFF, 
					forces.Cyaw - forces.Cside * forces.cg.x / flightConditions.getRefLength());
			data.setValue(FlightDataBranch.TYPE_NORMAL_FORCE_COEFF, forces.CN);
			data.setValue(FlightDataBranch.TYPE_SIDE_FORCE_COEFF, forces.Cside);
			data.setValue(FlightDataBranch.TYPE_ROLL_MOMENT_COEFF, forces.Croll);
			data.setValue(FlightDataBranch.TYPE_ROLL_FORCING_COEFF, forces.CrollForce);
			data.setValue(FlightDataBranch.TYPE_ROLL_DAMPING_COEFF, forces.CrollDamp);
			data.setValue(FlightDataBranch.TYPE_PITCH_DAMPING_MOMENT_COEFF, 
					forces.pitchDampingMoment);
		}
				
		data.setValue(FlightDataBranch.TYPE_DRAG_COEFF, forces.CD);
		data.setValue(FlightDataBranch.TYPE_AXIAL_DRAG_COEFF, forces.Caxial);
		data.setValue(FlightDataBranch.TYPE_FRICTION_DRAG_COEFF, forces.frictionCD);
		data.setValue(FlightDataBranch.TYPE_PRESSURE_DRAG_COEFF, forces.pressureCD);
		data.setValue(FlightDataBranch.TYPE_BASE_DRAG_COEFF, forces.baseCD);
		
		data.setValue(FlightDataBranch.TYPE_REFERENCE_LENGTH, flightConditions.getRefLength());
		data.setValue(FlightDataBranch.TYPE_REFERENCE_AREA, flightConditions.getRefArea());
		
		
		data.setValue(FlightDataBranch.TYPE_PITCH_RATE, flightConditions.getPitchRate());
		data.setValue(FlightDataBranch.TYPE_YAW_RATE, flightConditions.getYawRate());
		

		
		if (extra) {
			Coordinate c = status.orientation.rotateZ();
			double theta = Math.atan2(c.z, MathUtil.hypot(c.x, c.y));
			double phi = Math.atan2(c.y, c.x);
			if (phi < -(Math.PI-0.0001))
				phi = Math.PI;
			data.setValue(FlightDataBranch.TYPE_ORIENTATION_THETA, theta);
			data.setValue(FlightDataBranch.TYPE_ORIENTATION_PHI, phi);
		}
		
		data.setValue(FlightDataBranch.TYPE_AOA, flightConditions.getAOA());
		data.setValue(FlightDataBranch.TYPE_ROLL_RATE, flightConditions.getRollRate());

		data.setValue(FlightDataBranch.TYPE_WIND_VELOCITY, windSpeed);
		data.setValue(FlightDataBranch.TYPE_AIR_TEMPERATURE, 
				flightConditions.getAtmosphericConditions().temperature);
		data.setValue(FlightDataBranch.TYPE_AIR_PRESSURE, 
				flightConditions.getAtmosphericConditions().pressure);
		data.setValue(FlightDataBranch.TYPE_SPEED_OF_SOUND, 
				flightConditions.getAtmosphericConditions().getMachSpeed());

		
		data.setValue(FlightDataBranch.TYPE_TIME_STEP, timestep);
		data.setValue(FlightDataBranch.TYPE_COMPUTATION_TIME, 
				(System.nanoTime() - status.simulationStartTime)/1000000000.0);
		
		
//		data.setValue(FlightDataBranch.TYPE_, 0);

	}
	
	
}
