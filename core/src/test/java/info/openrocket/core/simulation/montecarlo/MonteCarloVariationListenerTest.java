package info.openrocket.core.simulation.montecarlo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.models.atmosphere.AtmosphericConditions;
import info.openrocket.core.rocketcomponent.RecoveryDevice;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.Coordinate;

public class MonteCarloVariationListenerTest {
	private static final double EPSILON = 1.0e-12;

	@Test
	public void testMassAndCgVariationPreservePhysicalScaling() {
		EnumMap<MonteCarloParameter, Double> values = new EnumMap<>(MonteCarloParameter.class);
		values.put(MonteCarloParameter.TOTAL_MASS, 0.10);
		values.put(MonteCarloParameter.CG_AXIAL, 0.01);
		MonteCarloSample sample = new MonteCarloSample(1, 42, values);
		MonteCarloVariationListener listener = new MonteCarloVariationListener(sample);
		RigidBody nominal = new RigidBody(new Coordinate(0.5, 0.02, 0.03, 2), 3, 4, 5);

		RigidBody varied = listener.postMassCalculation(null, nominal);
		assertEquals(2.2, varied.getMass(), EPSILON);
		assertEquals(0.51, varied.getCM().getX(), EPSILON);
		// Only the axial component is varied; no stepper reads a radial CG offset.
		assertEquals(0.02, varied.getCM().getY(), EPSILON);
		assertEquals(0.03, varied.getCM().getZ(), EPSILON);
		assertEquals(3.3, varied.getIxx(), EPSILON);
		assertEquals(4.4, varied.getIyy(), EPSILON);
		assertEquals(5.5, varied.getIzz(), EPSILON);
	}

	@Test
	public void testAtmosphereAerodynamicsAndThrustAreScaled() {
		EnumMap<MonteCarloParameter, Double> values = new EnumMap<>(MonteCarloParameter.class);
		values.put(MonteCarloParameter.AIR_DENSITY, 0.10);
		values.put(MonteCarloParameter.AXIAL_DRAG, 0.20);
		values.put(MonteCarloParameter.NORMAL_FORCE, -0.10);
		values.put(MonteCarloParameter.THRUST, 0.05);
		MonteCarloVariationListener listener = listener(values);

		AtmosphericConditions nominalAtmosphere = new AtmosphericConditions(280, 90_000);
		AtmosphericConditions variedAtmosphere = listener.postAtmosphericModel(null, nominalAtmosphere);
		assertNotSame(nominalAtmosphere, variedAtmosphere);
		assertEquals(90_000, nominalAtmosphere.getPressure(), EPSILON);
		assertEquals(nominalAtmosphere.getPressure() * 1.1, variedAtmosphere.getPressure(), EPSILON);

		SimulationStatus status = mock(SimulationStatus.class);
		when(status.getDeployedRecoveryDevices()).thenReturn(Collections.emptySet());
		AerodynamicForces forces = forces();
		AerodynamicForces variedForces = listener.postAerodynamicCalculation(status, forces);
		assertSame(forces, variedForces);
		assertEquals(2.4, variedForces.getCDaxial(), EPSILON);
		assertEquals(2.7, variedForces.getCN(), EPSILON);
		assertEquals(3.6, variedForces.getCm(), EPSILON);
		assertEquals(1.8, variedForces.getCP().getWeight(), EPSILON);
		assertEquals(105, listener.postSimpleThrustCalculation(status, 100), EPSILON);
	}

	@Test
	public void testRecoveryDragAppliesOnlyAfterDeployment() {
		EnumMap<MonteCarloParameter, Double> values = new EnumMap<>(MonteCarloParameter.class);
		values.put(MonteCarloParameter.RECOVERY_DRAG, 0.25);
		MonteCarloVariationListener listener = listener(values);
		SimulationStatus status = mock(SimulationStatus.class);
		when(status.getDeployedRecoveryDevices()).thenReturn(Collections.emptySet());
		assertNull(listener.postAerodynamicCalculation(status, forces()));

		when(status.getDeployedRecoveryDevices()).thenReturn(Set.of(mock(RecoveryDevice.class)));
		AerodynamicForces varied = listener.postAerodynamicCalculation(status, forces());
		assertEquals(2.5, varied.getCDaxial(), EPSILON);
	}

	@Test
	public void testNegativeDeploymentDelayCannotPrecedeTrigger() throws SimulationException {
		EnumMap<MonteCarloParameter, Double> values = new EnumMap<>(MonteCarloParameter.class);
		values.put(MonteCarloParameter.DEPLOYMENT_DELAY, -2.0);
		MonteCarloVariationListener listener = listener(values);
		SimulationStatus status = mock(SimulationStatus.class);
		when(status.getSimulationTime()).thenReturn(10.0);
		RecoveryDevice device = mock(RecoveryDevice.class);
		FlightEvent event = new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT, 11.0, device);

		assertFalse(listener.addFlightEvent(status, event));
		ArgumentCaptor<FlightEvent> eventCaptor = ArgumentCaptor.forClass(FlightEvent.class);
		verify(status).addEvent(eventCaptor.capture());
		assertEquals(10.001, eventCaptor.getValue().getTime(), EPSILON);
		assertSame(device, eventCaptor.getValue().getSource());
	}

	@Test
	public void testIgnitionOffsetSkipsTheInitialLaunchIgnition() throws SimulationException {
		EnumMap<MonteCarloParameter, Double> values = new EnumMap<>(MonteCarloParameter.class);
		values.put(MonteCarloParameter.IGNITION_DELAY, 0.5);
		MonteCarloVariationListener listener = listener(values);

		// Before liftoff the event is the pad ignition; shifting it would only translate
		// the whole flight in time, so it must pass through untouched.
		SimulationStatus onPad = mock(SimulationStatus.class);
		when(onPad.isLiftoff()).thenReturn(false);
		FlightEvent padIgnition = new FlightEvent(FlightEvent.Type.IGNITION, 0.0, null);
		assertTrue(listener.addFlightEvent(onPad, padIgnition));
		verify(onPad, never()).addEvent(any());

		// An air-start queued after liftoff is genuinely dispersive and must be shifted.
		SimulationStatus inFlight = mock(SimulationStatus.class);
		when(inFlight.isLiftoff()).thenReturn(true);
		when(inFlight.getSimulationTime()).thenReturn(4.0);
		FlightEvent airStart = new FlightEvent(FlightEvent.Type.IGNITION, 5.0, null);
		assertFalse(listener.addFlightEvent(inFlight, airStart));
		ArgumentCaptor<FlightEvent> eventCaptor = ArgumentCaptor.forClass(FlightEvent.class);
		verify(inFlight).addEvent(eventCaptor.capture());
		assertEquals(5.5, eventCaptor.getValue().getTime(), EPSILON);
	}

	private static MonteCarloVariationListener listener(EnumMap<MonteCarloParameter, Double> values) {
		return new MonteCarloVariationListener(new MonteCarloSample(1, 42, values));
	}

	private static AerodynamicForces forces() {
		AerodynamicForces forces = new AerodynamicForces();
		forces.setCP(new Coordinate(1, 0, 0, 2));
		forces.setCDaxial(2);
		forces.setCD(2);
		forces.setPressureCD(2);
		forces.setBaseCD(2);
		forces.setFrictionCD(2);
		forces.setOverrideCD(2);
		forces.setCN(3);
		forces.setCm(4);
		forces.setCside(5);
		forces.setCyaw(6);
		forces.setPitchDampingMoment(7);
		forces.setYawDampingMoment(8);
		return forces;
	}
}
