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

public class MotorPressureCorrectionTest {

	@Test
	public void testMotorPressureCorrection() {

		final double motorDiameter = 0.05;
		final double nozzleDiameter = motorDiameter/2;
		final double length = 0.1;
		final double time = 1;
		final double pressureDifference = 1000;

		ThrustCurveMotor motor = new ThrustCurveMotor.Builder()
			.setDiameter(motorDiameter)
			.setLength(0.1)
			.setTimePoints(new double[] { 0, 1, 3, 4 })
			.setThrustPoints(new double[] { 0, 2, 3, 0 })
			.setCGPoints(new CoordinateIF[] {
					new Coordinate(0.02, 0, 0, length/2),
					new Coordinate(0.02, 0, 0, length/2),
					new Coordinate(0.02, 0, 0, length/2),
					new Coordinate(0.03, 0, 0, length/2) })
			.build();

		Rocket rocket = new Rocket();
		
		AxialStage axialStage = new AxialStage();
		rocket.addChild(axialStage);
		
		BodyTube bodyTube = new BodyTube();
		axialStage.addChild(bodyTube);
		
		bodyTube.setMotorMount(true);

		Simulation simulation = new Simulation(rocket);
		
		FlightConfiguration flightConfiguration = new FlightConfiguration(rocket);
		
		MotorConfiguration motorConfiguration = new MotorConfiguration(bodyTube, flightConfiguration.getId());
		motorConfiguration.setMotor(motor);
		
		flightConfiguration.addMotor(motorConfiguration);
		
		SimulationConditions simulationConditions = simulation.getOptions().toSimulationConditions();
		SimulationStatus simulationStatus = new SimulationStatus(flightConfiguration, simulationConditions);

		((List<MotorClusterState>)simulationStatus.getMotors()).get(0).ignite(0);
		simulationStatus.setSimulationTime(time);

		RK4SimulationStepper stepper = new RK4SimulationStepper();
		stepper.store = new RK4SimulationStepper.DataStore();
		stepper.store.flightConditions = new FlightConditions(flightConfiguration);
		
		try {

			// At standard pressure, thrust should simply be the value from the thrustcurve
			stepper.store.flightConditions.getAtmosphericConditions().setPressure(stepper.store.flightConditions.getAtmosphericConditions().STANDARD_PRESSURE);
			motorConfiguration.setNozzleExitDiameter(nozzleDiameter);

			stepper.calculateFlightConditions(simulationStatus, stepper.store);
			
			double thrust = stepper.calculateThrust(simulationStatus, stepper.store);
			assertEquals(motor.getThrust(time), thrust, "Thrust at sea level incorrect");

		} catch (SimulationException e) {
            fail("Exception thrown testing sea level thrust: " + e);
        }

		try {
			// thrust correction is exit area * pressure difference
			double thrustCorrection = Math.PI * Math.pow(nozzleDiameter/2, 2) * pressureDifference;

			stepper.store.flightConditions.getAtmosphericConditions().setPressure(stepper.store.flightConditions.getAtmosphericConditions().STANDARD_PRESSURE - pressureDifference);
			double thrust2 = stepper.calculateThrust(simulationStatus, stepper.store);
			assertEquals(motor.getThrust(time) + thrustCorrection, thrust2, "Corrected thrust incorrect");
			
        } catch (SimulationException e) {
            fail("Exception thrown testing corrected thrust: " + e);
        }

		
	}
}
