package info.openrocket.swing.gui.simulation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests completion tracking for concurrently executed simulation batches.
 */
class SimulationRunDialogTest {

	@Test
	void reportsCompletionOnlyAfterTheLastWorkerFinishes() {
		boolean[] completed = new boolean[3];

		assertFalse(SimulationRunDialog.markSimulationComplete(completed, 2));
		assertFalse(SimulationRunDialog.markSimulationComplete(completed, 0));
		assertTrue(SimulationRunDialog.markSimulationComplete(completed, 1));
	}

	@Test
	void duplicateCompletionDoesNotPublishAnotherBatchCompletion() {
		boolean[] completed = new boolean[1];

		assertTrue(SimulationRunDialog.markSimulationComplete(completed, 0));
		assertFalse(SimulationRunDialog.markSimulationComplete(completed, 0));
	}
}
