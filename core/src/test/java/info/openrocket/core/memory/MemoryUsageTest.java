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
 * and asserts that the JVM's live heap usage remains below a generous upper bound.
 * <p>
 * The threshold is intentionally high so the test remains stable across different
 * CI environments, while still catching regressions where we accidentally retain
 * large graphs in memory.
 */
public class MemoryUsageTest extends BaseTestCase {

	private static final long MAX_HEAP_USED_BYTES = 30L * 1024 * 1024; // 30 MiB

	@Test
	public void heapUsageRemainsWithinBudget() throws SimulationException, InterruptedException {
		// Warm-up: run a few typical operations that allocate appreciable heap.
		for (int i = 0; i < 3; i++) {
			runSampleSimulation();
		}

		// Encourage GC so we measure retained objects rather than transient allocations.
		requestFullGc();

		long used = getHeapUsedBytes();

		assertTrue(used < MAX_HEAP_USED_BYTES,
				"Expected heap usage < " + MAX_HEAP_USED_BYTES + " bytes, but was " + used);
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
