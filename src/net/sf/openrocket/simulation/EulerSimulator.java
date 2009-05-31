package net.sf.openrocket.simulation;

import java.util.Collection;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.AtmosphericConditions;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.GravityModel;
import net.sf.openrocket.aerodynamics.WindSimulator;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Quaternion;




/**
 * A flight simulator based on Euler integration.  This class is out of date and
 * deprecated in favor of the Runge-Kutta simulator RK4Simulator.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
@Deprecated
public class EulerSimulator extends FlightSimulator {
	/**
	 * Maximum roll step allowed.  This is selected as an uneven division of the full
	 * circle so that the simulation will sample the most wind directions
	 */
	private static final double MAX_ROLL_STEP_ANGLE = 28.32 * Math.PI/180;
	
	private static final boolean DEBUG = false;
	
	
	private FlightConditions flightConditions = null;
	private double currentStep;
	private double lateralPitchRate = 0;  // set by calculateFlightConditions 
	
	public EulerSimulator(AerodynamicCalculator calculator) {
		super(calculator);
	}

	

	protected FlightDataBranch newFlightData(String name) {
		return new FlightDataBranch(name, FlightDataBranch.TYPE_TIME);  // TODO: ???
	}

	

	@Override
	protected RK4SimulationStatus initializeSimulation(Configuration configuration, 
			SimulationConditions simulation) {

		RK4SimulationStatus status = new RK4SimulationStatus();
		
		status.startConditions = simulation;
		
		status.configuration = configuration;
		status.flightData = newFlightData("Main");
		status.launchRod = true;
		status.time = 0.0;
		
		
		status.launchRodDirection = new Coordinate(
				Math.sin(simulation.getLaunchRodAngle()) * 
				Math.cos(simulation.getLaunchRodDirection()),
				Math.sin(simulation.getLaunchRodAngle()) *
				Math.sin(simulation.getLaunchRodDirection()),
				Math.cos(simulation.getLaunchRodAngle())
		);
		status.launchRodLength = simulation.getLaunchRodLength();
		// TODO: take into account launch lug positions
		
		
		Quaternion o = new Quaternion();
		o.multiplyLeft(Quaternion.rotation(
				new Coordinate(0, simulation.getLaunchRodAngle(), 0)));
		o.multiplyLeft(Quaternion.rotation(
				new Coordinate(0, 0, simulation.getLaunchRodDirection())));
		status.orientation = o;
		status.position = Coordinate.NUL;
		status.velocity = Coordinate.NUL;
		status.rotation = Coordinate.NUL;
		
		status.windSimulator = new WindSimulator();
		status.windSimulator.setAverage(simulation.getWindSpeedAverage());
		status.windSimulator.setStandardDeviation(simulation.getWindSpeedDeviation());
//		status.windSimulator.reset();
		
		currentStep = simulation.getTimeStep();
		
		status.gravityModel = new GravityModel(simulation.getLaunchLatitude());

		flightConditions = null;
		
		return status;
	}
	
	

	@Override
	protected Collection<FlightEvent> step(SimulationConditions simulation, 
			SimulationStatus simulationStatus) {
		
		RK4SimulationStatus status = (RK4SimulationStatus)simulationStatus;
		FlightDataBranch data = status.flightData;
		
		
		// Add data point and values
		data.addPoint();
		data.setValue(FlightDataBranch.TYPE_TIME, status.time);

		if (DEBUG)
			System.out.println("original direction: "+status.orientation.rotateZ());
		
		// Calculate current flight conditions
		if (flightConditions == null) {
			flightConditions = new FlightConditions(status.configuration);
		}
		calculateFlightConditions(flightConditions, status);

		if (DEBUG)
			System.out.println("flightConditions="+flightConditions);
		
		
		// Calculate time step
		double timestep;
		
//		double normalStep = simulation.getTimeStep();
//		double lateralStep = simulation.getMaximumStepAngle() / lateralPitchRate;
//		double rotationStep = Math.abs(MAX_ROLL_STEP_ANGLE / flightConditions.getRollRate());
//		if (lateralStep < normalStep || rotationStep < normalStep) {
//			System.out.printf("   t=%.3f STEP normal %.3f lateral %.3f rotation %.3f\n",
//					status.time, normalStep, lateralStep, rotationStep);
//		}

		
		timestep = MathUtil.min(simulation.getTimeStep(), 
				simulation.getMaximumStepAngle() / lateralPitchRate,
				Math.abs(MAX_ROLL_STEP_ANGLE / flightConditions.getRollRate()));
		if (timestep < 0.0001)
			timestep = 0.0001;
		
		
		
		// Calculate aerodynamic forces  (only axial if still on launch rod)
		AerodynamicForces forces;
		calculator.setConfiguration(status.configuration);

		if (status.launchRod) {
			forces = calculator.getAxialForces(status.time, flightConditions, status.warnings);
		} else {
			forces = calculator.getAerodynamicForces(status.time,
					flightConditions, status.warnings);
		}

		
		// Check in case of NaN
		assert(!Double.isNaN(forces.CD));
		assert(!Double.isNaN(forces.CN));
		assert(!Double.isNaN(forces.Caxial));
		assert(!Double.isNaN(forces.Cm));
		assert(!Double.isNaN(forces.Cyaw));
		assert(!Double.isNaN(forces.Cside));
		assert(!Double.isNaN(forces.Croll));
		

		//// Calculate forces and accelerations
		
		double dynP = (0.5 * flightConditions.getAtmosphericConditions().getDensity() *
				MathUtil.pow2(flightConditions.getVelocity()));
		double refArea = flightConditions.getRefArea();
		double refLength = flightConditions.getRefLength();
		
		
		// Linear forces
		double thrust = calculateThrust(status, currentStep);
		double dragForce = forces.Caxial * dynP * refArea;
		double fN = forces.CN * dynP * refArea;
		double fSide = forces.Cside * dynP * refArea;
		
		double sin = Math.sin(flightConditions.getTheta());
		double cos = Math.cos(flightConditions.getTheta());
		
		double forceX = - fN * cos - fSide * sin;
		double forceY = - fN * sin - fSide * cos;
		double forceZ = thrust - dragForce;

		
		Coordinate acceleration = new Coordinate(forceX / forces.cg.weight,
				forceY / forces.cg.weight, forceZ / forces.cg.weight);
		
		if (DEBUG)
			System.out.println("   acceleration before rotation: "+acceleration);
		acceleration = status.orientation.rotate(acceleration);
		if (DEBUG)
			System.out.println("   acceleration after  rotation: "+acceleration);

		acceleration = acceleration.sub(0, 0, status.gravityModel.getGravity());
		
		
		// Convert momenta
		double Cm = forces.Cm - forces.CN * forces.cg.x / refLength;
		double momX = (-Cm * sin - forces.Cyaw * cos) * dynP * refArea * refLength;
		double momY = ( Cm * cos - forces.Cyaw * sin) * dynP * refArea * refLength;
		double momZ = forces.Croll * dynP * refArea * refLength;
		
		assert(!Double.isNaN(momX));
		assert(!Double.isNaN(momY));
		assert(!Double.isNaN(momZ));
		assert(!Double.isNaN(forces.longitudalInertia));
		assert(!Double.isNaN(forces.rotationalInertia));
		
		Coordinate rotAcc = new Coordinate(momX / forces.longitudalInertia,
				momY / forces.longitudalInertia, momZ / forces.rotationalInertia);
		rotAcc = status.orientation.rotate(rotAcc);
		
//		System.out.println("   rotAcc="+rotAcc);
//		System.out.println("   momY="+momY+" lI="+forces.longitudalInertia
//				+" Cm="+Cm+" forces.Cm="+forces.Cm+" cg="+forces.cg);
//		System.out.println("   orient before update:"+status.orientation);
		

		
		// Perform position integration
		Coordinate avgVel = status.velocity.add(acceleration.multiply(currentStep/2));
		status.velocity = status.velocity.add(acceleration.multiply(currentStep));
		
		if (status.launchRod) {
			// Project velocity onto launch rod direction
			status.velocity = status.launchRodDirection.multiply(
					status.velocity.dot(status.launchRodDirection));
			avgVel = status.launchRodDirection.multiply(avgVel.dot(status.launchRodDirection));
		}
		
		status.position = status.position.add(avgVel.multiply(currentStep));

		if (status.launchRod) {
			// Avoid sinking into ground when on the launch rod 
			if (status.position.z < 0) {
//				System.out.println("Corrected sinking from pos:"+status.position+
//						" vel:"+status.velocity);
				status.position = Coordinate.NUL;
				status.velocity = Coordinate.NUL;
			}
		}

		
		if (!status.launchRod) {
			// Integrate rotation when off launch rod
			Coordinate avgRot = status.rotation.add(rotAcc.multiply(currentStep/2));
			status.rotation = status.rotation.add(rotAcc.multiply(currentStep));
			Quaternion stepRotation = Quaternion.rotation(avgRot.multiply(currentStep));
			status.orientation.multiplyLeft(stepRotation);

			status.orientation.normalize();
			
			if (DEBUG)
				System.out.println("   step rotation "+ 
						(avgRot.length()*currentStep*180/Math.PI) +"\u00b0 " +
						"step="+currentStep+" after: "+status.orientation.rotateZ());
		}

		status.time += currentStep;
		
		
		
		// Check rotation angle step length and correct time step if necessary
		double rot = status.rotation.length() * currentStep;
		if (rot > simulation.getMaximumStepAngle()) {
			currentStep /= 2;
			if (DEBUG)
				System.out.println("  *** Step division to: "+currentStep);
		}
		if ((rot < simulation.getMaximumStepAngle()/3) &&
				(currentStep < simulation.getTimeStep() - 0.000001)) {
			currentStep *= 2;
			if (DEBUG)
				System.out.println("  *** Step multiplication to: "+currentStep);
		}
		
		
		
		// Store values
		data.setValue(FlightDataBranch.TYPE_ACCELERATION_Z, acceleration.z);
		data.setValue(FlightDataBranch.TYPE_ACCELERATION_TOTAL, acceleration.length());
		data.setValue(FlightDataBranch.TYPE_VELOCITY_TOTAL, status.velocity.length());
		
		data.setValue(FlightDataBranch.TYPE_AXIAL_DRAG_COEFF, forces.CD);
		data.setValue(FlightDataBranch.TYPE_FRICTION_DRAG_COEFF, forces.frictionCD);
		data.setValue(FlightDataBranch.TYPE_PRESSURE_DRAG_COEFF, forces.pressureCD);
		data.setValue(FlightDataBranch.TYPE_BASE_DRAG_COEFF, forces.baseCD);
		
		data.setValue(FlightDataBranch.TYPE_CP_LOCATION, forces.cp.x);
		data.setValue(FlightDataBranch.TYPE_CG_LOCATION, forces.cg.x);
		
		data.setValue(FlightDataBranch.TYPE_MASS, forces.cg.weight);
		
		data.setValue(FlightDataBranch.TYPE_THRUST_FORCE, thrust);
		data.setValue(FlightDataBranch.TYPE_DRAG_FORCE, dragForce);

		
		Coordinate c = status.orientation.rotateZ();
		double theta = Math.atan2(c.z, MathUtil.hypot(c.x, c.y));
		double phi = Math.atan2(c.y, c.x);
		data.setValue(FlightDataBranch.TYPE_ORIENTATION_THETA, theta);
		data.setValue(FlightDataBranch.TYPE_ORIENTATION_PHI, phi);
		
		return null;
	}

	
	
	/*
	 * TODO: MEDIUM:  Many parameters are stored one time step late, how to fix?
	 */
	private void calculateFlightConditions(FlightConditions flightConditions,
			RK4SimulationStatus status) {

		// Atmospheric conditions
		AtmosphericConditions cond = status.startConditions.getAtmosphericModel().
			getConditions(status.position.z + status.startConditions.getLaunchAltitude());
		flightConditions.setAtmosphericConditions(cond);
		status.flightData.setValue(FlightDataBranch.TYPE_AIR_TEMPERATURE, cond.temperature);
		status.flightData.setValue(FlightDataBranch.TYPE_AIR_PRESSURE, cond.pressure);
		status.flightData.setValue(FlightDataBranch.TYPE_SPEED_OF_SOUND, cond.getMachSpeed());

		
		// Local wind speed and direction
		double wind = status.windSimulator.getWindSpeed(status.time);
		status.flightData.setValue(FlightDataBranch.TYPE_WIND_VELOCITY, wind);
		
		Coordinate windSpeed = status.velocity.sub(wind, 0, 0);
		windSpeed = status.orientation.invRotate(windSpeed);
		
		double theta = Math.atan2(windSpeed.y, windSpeed.x);
		double velocity = windSpeed.length();
		double aoa = Math.acos(windSpeed.z / velocity);

		if (Double.isNaN(theta) || Double.isInfinite(theta))
			theta = 0;
		if (Double.isNaN(velocity) || Double.isInfinite(velocity))
			velocity = 0;
		if (Double.isNaN(aoa) || Double.isInfinite(aoa))
			aoa = 0;
		
		flightConditions.setTheta(theta);
		flightConditions.setAOA(aoa);
		flightConditions.setVelocity(velocity);
		
		status.flightData.setValue(FlightDataBranch.TYPE_AOA, aoa);
		
		// Roll, pitch and yaw rate
		Coordinate rot = status.orientation.invRotate(status.rotation);
		flightConditions.setRollRate(rot.z);
		status.flightData.setValue(FlightDataBranch.TYPE_ROLL_RATE, rot.z);
		double len = MathUtil.hypot(windSpeed.x, windSpeed.y);
		if (len < 0.001) {
			flightConditions.setPitchRate(0);
			flightConditions.setYawRate(0);
			lateralPitchRate = 0;
		} else {
			double sinTheta = windSpeed.x / len;
			double cosTheta = windSpeed.y / len;
			flightConditions.setPitchRate(cosTheta*rot.x + sinTheta*rot.y);
			flightConditions.setYawRate(sinTheta*rot.x + cosTheta*rot.y);
			lateralPitchRate = MathUtil.hypot(rot.x, rot.y);
		}
	}

}
