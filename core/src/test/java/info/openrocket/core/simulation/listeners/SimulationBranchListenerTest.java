package info.openrocket.core.simulation.listeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.FlightDataBranch;
import info.openrocket.core.simulation.SimulationStatus;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

/**
 * Tests for simulation branch listener functionality.
 * 
 * Verifies that startSimulationBranch() and endSimulationBranch() are called
 * correctly for single-stage and multi-stage rockets.
 */
public class SimulationBranchListenerTest extends BaseTestCase {

	private TestBranchListener listener;
	private Rocket rocket;
	private Simulation simulation;

	@BeforeEach
	public void setUpTest() {
		listener = new TestBranchListener();
		// Reset counters and lists for each test
		listener.state.startSimulationCallCount.set(0);
		listener.state.endSimulationCallCount.set(0);
		listener.state.startBranchCallCount.set(0);
		listener.state.endBranchCallCount.set(0);
		synchronized (listener.state.branchNames) {
			listener.state.branchNames.clear();
		}
		synchronized (listener.state.callOrder) {
			listener.state.callOrder.clear();
		}
		synchronized (listener.state.endBranchExceptions) {
			listener.state.endBranchExceptions.clear();
		}
		rocket = TestRockets.makeEstesAlphaIII();
		simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
	}

	/**
	 * Test that branch listeners are called for a single-stage rocket.
	 */
	@Test
	public void testSingleStageRocket() throws SimulationException {
		simulation.simulate(listener);

		// Should have one branch
		assertEquals(1, simulation.getSimulatedData().getBranchCount(),
				"Single-stage rocket should have one branch");

		// Verify listener was called correctly
		// Note: nested simulations (like computeCoastTime) no longer trigger user listeners,
		// so we should get exactly one call per event for a single-stage rocket
		assertEquals(1, listener.state.startSimulationCallCount.get(),
				"startSimulation should be called exactly once");
		assertEquals(1, listener.state.endSimulationCallCount.get(),
				"endSimulation should be called exactly once");
		assertEquals(1, listener.state.startBranchCallCount.get(),
				"startSimulationBranch should be called exactly once for single stage");
		assertEquals(1, listener.state.endBranchCallCount.get(),
				"endSimulationBranch should be called exactly once for single stage");
		// Verify they're called the same number of times
		assertEquals(listener.state.startBranchCallCount.get(), listener.state.endBranchCallCount.get(),
				"startSimulationBranch and endSimulationBranch should be called the same number of times");

		// Verify branch names match
		synchronized (listener.state.branchNames) {
			assertEquals(1, listener.state.branchNames.size(),
					"Should have exactly one branch name recorded");
			FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
			assertEquals(branch.getName(), listener.state.branchNames.get(0),
					"Branch name should match");
		}
	}

	/**
	 * Test that branch listeners are called for each branch in a multi-stage rocket.
	 */
	@Test
	public void testMultiStageRocket() throws SimulationException {
		// Use a two-stage rocket
		rocket = TestRockets.makeBeta();
		simulation = new Simulation(rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		rocket.getSelectedConfiguration().setAllStages();
		FlightConfigurationId fcid = rocket.getSelectedConfiguration().getFlightConfigurationID();
		simulation.setFlightConfigurationId(fcid);

		simulation.simulate(listener);

		// Should have multiple branches (sustainer + boosters)
		int branchCount = simulation.getSimulatedData().getBranchCount();
		assertTrue(branchCount >= 2,
				"Multi-stage rocket should have at least 2 branches");

		// Verify listener was called correctly
		// Note: nested simulations (like computeCoastTime) no longer trigger user listeners,
		// so we should get exactly one call per event
		assertEquals(1, listener.state.startSimulationCallCount.get(),
				"startSimulation should be called exactly once");
		assertEquals(1, listener.state.endSimulationCallCount.get(),
				"endSimulation should be called exactly once");
		// Branch listeners should be called exactly once per branch
		assertEquals(branchCount, listener.state.startBranchCallCount.get(),
				"startSimulationBranch should be called exactly once per branch");
		assertEquals(branchCount, listener.state.endBranchCallCount.get(),
				"endSimulationBranch should be called exactly once per branch");
		// Verify they're called the same number of times
		assertEquals(listener.state.startBranchCallCount.get(), listener.state.endBranchCallCount.get(),
				"startSimulationBranch and endSimulationBranch should be called the same number of times");

		// Verify branch names match
		synchronized (listener.state.branchNames) {
			assertEquals(branchCount, listener.state.branchNames.size(),
					"Should have recorded branch name for each branch");
			for (int i = 0; i < branchCount; i++) {
				FlightDataBranch branch = simulation.getSimulatedData().getBranch(i);
				assertEquals(branch.getName(), listener.state.branchNames.get(i),
						"Branch name should match for branch " + i);
			}
		}
	}

	/**
	 * Test that branch listeners are called in the correct order.
	 */
	@Test
	public void testListenerCallOrder() throws SimulationException {
		simulation.simulate(listener);

		// Verify call order: startSimulation -> startBranch -> endBranch -> endSimulation
		synchronized (listener.state.callOrder) {
			assertTrue(listener.state.callOrder.size() >= 4,
					"Should have at least 4 listener calls");

			// First call should be startSimulation
			assertEquals("startSimulation", listener.state.callOrder.get(0),
					"First call should be startSimulation");

			// Second call should be startSimulationBranch
			assertEquals("startSimulationBranch", listener.state.callOrder.get(1),
					"Second call should be startSimulationBranch");

			// Last call should be endSimulation
			assertEquals("endSimulation", listener.state.callOrder.get(listener.state.callOrder.size() - 1),
					"Last call should be endSimulation");

			// Second-to-last call should be endSimulationBranch
			assertEquals("endSimulationBranch", listener.state.callOrder.get(listener.state.callOrder.size() - 2),
					"Second-to-last call should be endSimulationBranch");
		}
	}

	/**
	 * Test that endSimulationBranch is called even when an exception occurs.
	 */
	@Test
	public void testBranchListenerCalledOnException() throws SimulationException {
		// Create a listener that throws an exception during branch execution
		ExceptionThrowingListener exceptionListener = new ExceptionThrowingListener();

		try {
			simulation.simulate(exceptionListener, listener);
		} catch (SimulationException e) {
			// Expected - the exception listener throws an exception
		}

		// Verify that endSimulationBranch was still called
		assertTrue(listener.state.endBranchCallCount.get() > 0,
				"endSimulationBranch should be called even when exception occurs");
		synchronized (listener.state.endBranchExceptions) {
			assertTrue(listener.state.endBranchExceptions.size() > 0,
					"endSimulationBranch should receive exception parameter");
		}
	}

	/**
	 * Test that endSimulationBranch receives null exception when branch completes normally.
	 */
	@Test
	public void testBranchListenerNormalCompletion() throws SimulationException {
		simulation.simulate(listener);

		// Verify that endSimulationBranch was called with null exception
		assertEquals(1, listener.state.endBranchCallCount.get(),
				"endSimulationBranch should be called exactly once");
		synchronized (listener.state.endBranchExceptions) {
			assertEquals(1, listener.state.endBranchExceptions.size(),
					"Should have exactly one exception record");
			assertNull(listener.state.endBranchExceptions.get(0),
					"Exception should be null for normal completion");
		}
	}

	/**
	 * Shared state object to track calls across listener clones.
	 */
	private static class SharedState {
		final AtomicInteger startSimulationCallCount = new AtomicInteger(0);
		final AtomicInteger endSimulationCallCount = new AtomicInteger(0);
		final AtomicInteger startBranchCallCount = new AtomicInteger(0);
		final AtomicInteger endBranchCallCount = new AtomicInteger(0);
		final List<String> branchNames = new ArrayList<>();
		final List<String> callOrder = new ArrayList<>();
		final List<SimulationException> endBranchExceptions = new ArrayList<>();
	}

	/**
	 * Test listener that tracks all branch listener calls.
	 * Uses shared state object to track calls across clones.
	 */
	private static class TestBranchListener extends AbstractSimulationListener {
		// Use shared state so clones can update the same counters
		final SharedState state = new SharedState();

		@Override
		public void startSimulation(SimulationStatus status) throws SimulationException {
			state.startSimulationCallCount.incrementAndGet();
			synchronized (state.callOrder) {
				state.callOrder.add("startSimulation");
			}
		}

		@Override
		public void endSimulation(SimulationStatus status, SimulationException exception) {
			state.endSimulationCallCount.incrementAndGet();
			synchronized (state.callOrder) {
				state.callOrder.add("endSimulation");
			}
		}

		@Override
		public void startSimulationBranch(SimulationStatus status) throws SimulationException {
			state.startBranchCallCount.incrementAndGet();
			FlightDataBranch branch = status.getFlightDataBranch();
			if (branch != null) {
				synchronized (state.branchNames) {
					state.branchNames.add(branch.getName());
				}
			}
			synchronized (state.callOrder) {
				state.callOrder.add("startSimulationBranch");
			}
		}

		@Override
		public void endSimulationBranch(SimulationStatus status, SimulationException exception) {
			state.endBranchCallCount.incrementAndGet();
			synchronized (state.endBranchExceptions) {
				state.endBranchExceptions.add(exception);
			}
			synchronized (state.callOrder) {
				state.callOrder.add("endSimulationBranch");
			}
		}
	}

	/**
	 * Test listener that throws an exception to test exception handling.
	 */
	private static class ExceptionThrowingListener extends AbstractSimulationListener {
		private int stepCount = 0;

		@Override
		public void postStep(SimulationStatus status) throws SimulationException {
			stepCount++;
			// Throw an exception after a few steps to test exception handling during branch execution
			if (stepCount >= 3) {
				throw new SimulationException("Test exception during branch execution");
			}
		}
	}
}

