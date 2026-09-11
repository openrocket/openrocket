package info.openrocket.core.memory;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import org.junit.jupiter.api.Test;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;

/**
 * Basic integration-style test that exercises a representative OpenRocket workflow
 * and asserts that repeating it does not retain a growing amount of heap.
 * <p>
 * The test measures the heap <em>delta</em> across repeated simulations rather than
 * absolute heap usage.  Absolute usage includes environmental noise the test does not
 * care about — the JVM baseline, library class metadata (Jackson, etc.), and one-time
 * caches — which varies with JVM version, dependencies, and GC timing.  A before/after
 * delta cancels that noise out and isolates what we actually want to catch: simulations
 * leaking objects that survive garbage collection.
 */
public class MemoryUsageTest extends BaseTestCase {

	/**
	 * Maximum heap growth tolerated across the measured simulation runs.  Correctly
	 * released simulations should retain essentially nothing between iterations; a real
	 * per-run leak would accumulate well past this bound.
	 */
	private static final long MAX_HEAP_GROWTH_BYTES = 5L * 1024 * 1024; // 5 MiB

	@Test
	public void heapUsageRemainsWithinBudget() throws SimulationException, InterruptedException {
		// Warm up once so one-time initialization (class loading, motor/atmosphere data,
		// static caches) is already resident and therefore excluded from the measurement.
		runSampleSimulation();

		// Establish a clean baseline after that one-time cost has been paid.
		requestFullGc();
		long heapBefore = getHeapUsedBytes();

		// Repeat the workload; a per-run leak would accumulate across these iterations.
		for (int i = 0; i < 3; i++) {
			runSampleSimulation();
		}

		// Measure retained objects rather than transient allocations.
		requestFullGc();
		long heapAfter = getHeapUsedBytes();
		long heapDelta = heapAfter - heapBefore;

		assertTrue(heapDelta < MAX_HEAP_GROWTH_BYTES,
				"Expected heap growth < " + MAX_HEAP_GROWTH_BYTES + " bytes, but was " + heapDelta);
	}

	private static void runSampleSimulation() throws SimulationException {
		Simulation simulation = new Simulation(TestRockets.makeEstesAlphaIII());
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.simulate();
	}

	private static long getHeapUsedBytes() {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		return memoryMXBean.getHeapMemoryUsage().getUsed();
	}

	private static void requestFullGc() throws InterruptedException {
		System.gc();
		Thread.sleep(50L);
		System.runFinalization();
		System.gc();
		Thread.sleep(50L);
	}
}
