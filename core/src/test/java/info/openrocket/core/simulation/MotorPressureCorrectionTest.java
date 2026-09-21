package info.openrocket.core.simulation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.simulation.AbstractSimulationStepper;
import info.openrocket.core.simulation.RK4SimulationStepper;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;

public class MotorPressureCorrectionTest {

	@Test
	public void testMotorPressureCorrection() {

		final double motorLength = 0.1;
		final double motorDiameter = 0.05;
		
		final double nozzle1Diameter = motorDiameter/2;
		final double nozzle2Diameter = motorDiameter/4;
		
		final double time1 = 1;             // both motors are burning
		final double time2 = 3;             // motor2 burns out here
		final double pressureDifference = 1000;

		// Just enough rocket to test
		Rocket rocket = new Rocket();
		
		AxialStage axialStage = new AxialStage();
		rocket.addChild(axialStage);
		
		FlightConfiguration flightConfiguration = new FlightConfiguration(rocket);
		
		BodyTube bodyTube1 = new BodyTube();
		axialStage.addChild(bodyTube1);
		
		bodyTube1.setMotorMount(true);
		MotorConfiguration motor1Configuration = new MotorConfiguration(bodyTube1, flightConfiguration.getId());

		ThrustCurveMotor motor1 = new ThrustCurveMotor.Builder()
			.setDiameter(motorDiameter)
			.setLength(motorLength)
			.setTimePoints(new double[] { 0, 1, 3, 4 })
			.setThrustPoints(new double[] { 0, 2, 3, 0 })
			.setCGPoints(new CoordinateIF[] {
					new Coordinate(0.02, 0, 0, motorLength/2),
					new Coordinate(0.02, 0, 0, motorLength/2),
					new Coordinate(0.02, 0, 0, motorLength/2),
					new Coordinate(0.03, 0, 0, motorLength/2) })
			.build();
		
		motor1Configuration.setMotor(motor1);
		motor1Configuration.setNozzleExitDiameter(nozzle1Diameter);
		flightConfiguration.addMotor(motor1Configuration);
		
		BodyTube bodyTube2 = new BodyTube();
		axialStage.addChild(bodyTube2);
		
		bodyTube2.setMotorMount(true);
		MotorConfiguration motor2Configuration = new MotorConfiguration(bodyTube2, flightConfiguration.getId());

		ThrustCurveMotor motor2 = new ThrustCurveMotor.Builder()
			.setDiameter(motorDiameter)
			.setLength(motorLength)
			.setTimePoints(new double[] { 0, 1, 3 })
			.setThrustPoints(new double[] { 0, 2, 0 })
			.setCGPoints(new CoordinateIF[] {
					new Coordinate(0.02, 0, 0, motorLength/2),
					new Coordinate(0.02, 0, 0, motorLength/2),
					new Coordinate(0.02, 0, 0, motorLength/2) })
			.build();
		
		motor2Configuration.setMotor(motor2);
		motor2Configuration.setNozzleExitDiameter(nozzle2Diameter);
		flightConfiguration.addMotor(motor2Configuration);

		Simulation simulation = new Simulation(rocket);
		SimulationConditions simulationConditions = simulation.getOptions().toSimulationConditions();
		SimulationStatus simulationStatus = new SimulationStatus(flightConfiguration, simulationConditions);

		for (MotorClusterState motorClusterState : (List<MotorClusterState>)simulationStatus.getMotors()) {
			motorClusterState.ignite(0);
		}

		RK4SimulationStepper stepper = new RK4SimulationStepper();
		stepper.store = new RK4SimulationStepper.DataStore();
		stepper.store.flightConditions = new FlightConditions(flightConfiguration);

		// calculate expected results
		final double nozzle1Area = Math.PI * Math.pow(nozzle1Diameter/2, 2);
		final double thrust1Correction = nozzle1Area * pressureDifference;
		
		final double nozzle2Area = Math.PI * Math.pow(nozzle2Diameter/2, 2);
		final double thrust2Correction = nozzle2Area * pressureDifference;

		// at time 1, both motors are active
		simulationStatus.setSimulationTime(time1);
		
		try {
			// At standard pressure, thrust should simply be the value from the thrustcurve
			stepper.store.flightConditions.getAtmosphericConditions().setPressure(stepper.store.flightConditions.getAtmosphericConditions().STANDARD_PRESSURE);
			stepper.calculateFlightConditions(simulationStatus, stepper.store);

			assertEquals(nozzle1Area + nozzle2Area,
						 stepper.store.flightConditions.getThrustingNozzleExitArea(),
						 MathUtil.EPSILON,
						 "thrusting nozzle exit area incorrect");
			
			assertEquals(motor1.getThrust(time1) + motor2.getThrust(time1),
						 stepper.calculateThrust(simulationStatus, stepper.store),
						 MathUtil.EPSILON,
						 "Thrust at sea level incorrect");

		} catch (SimulationException e) {
            fail("Exception thrown testing sea level thrust: " + e);
        }

		try {
			// Test correction with both motors active
			stepper.store.flightConditions.getAtmosphericConditions().setPressure(stepper.store.flightConditions.getAtmosphericConditions().STANDARD_PRESSURE - pressureDifference);
			stepper.calculateFlightConditions(simulationStatus, stepper.store);

			assertEquals(motor1.getThrust(time1) + motor2.getThrust(time1) + thrust1Correction + thrust2Correction,
						 stepper.calculateThrust(simulationStatus, stepper.store),
						 MathUtil.EPSILON,
						 "Corrected thrust incorrect");
			
        } catch (SimulationException e) {
            fail("Exception thrown testing corrected thrust both motors: " + e);
        }

		// at time2, motor2 has burned out
		simulationStatus.setSimulationTime(time2);
		((List<MotorClusterState>) (simulationStatus.getMotors())).get(1).burnOut(time2);
		
		try {
			stepper.calculateFlightConditions(simulationStatus, stepper.store);

			// Check nozzle area
			assertEquals(nozzle1Area,
						 stepper.store.flightConditions.getThrustingNozzleExitArea(),
						 MathUtil.EPSILON,
						 "thrusting nozzle exit area incorrect");

			assertEquals(motor1.getThrust(time2) + thrust1Correction,
						 stepper.calculateThrust(simulationStatus, stepper.store),
						 MathUtil.EPSILON,
						 "Corrected thrust incorrect");
			
        } catch (SimulationException e) {
            fail("Exception thrown testing corrected thrust both motors: " + e);
        }

		

		
	}
}
