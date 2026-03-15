package info.openrocket.core.document;

import static org.junit.jupiter.api.Assertions.*;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.simulation.exception.SimulationException;
import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.TestRockets;
import org.junit.jupiter.api.Test;

public class OpenRocketDocumentSimulationUndoRedoTest extends BaseTestCase {
	private static final double EPSILON = 1e-6;

	private static Simulation createSimulation(OpenRocketDocument document, Rocket rocket, String name) {
		Simulation simulation = new Simulation(document, rocket);
		simulation.setFlightConfigurationId(TestRockets.TEST_FCID_0);
		simulation.setName(name);
		simulation.getOptions().setISAAtmosphere(true);
		simulation.getOptions().setTimeStep(0.05);
		return simulation;
	}

	@Test
	public void undoRedoSimulationDeletion_restoresSimulationAndResults() throws SimulationException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = new OpenRocketDocument(rocket);

		Simulation simulation = createSimulation(document, rocket, "Delete me");
		document.addSimulation(simulation);

		simulation.simulate();
		FlightData dataBefore = simulation.getSimulatedData();
		assertNotNull(dataBefore);
		assertTrue(dataBefore.getMaxAltitude() > 0);

		document.clearUndo();

		document.addUndoPosition("Delete simulation");
		document.removeSimulation(simulation);
		assertEquals(0, document.getSimulationCount());

		document.undo();
		assertEquals(1, document.getSimulationCount());

		Simulation restored = document.getSimulation(0);
		assertEquals("Delete me", restored.getName());
		assertEquals(TestRockets.TEST_FCID_0, restored.getFlightConfigurationId());

		FlightData restoredData = restored.getSimulatedData();
		assertNotNull(restoredData, "Undoing simulation deletion should restore results");
		assertEquals(dataBefore.getMaxAltitude(), restoredData.getMaxAltitude(), 0.001);
		assertEquals(Simulation.Status.UPTODATE, restored.getStatus());

		assertTrue(document.isRedoAvailable());
		document.redo();
		assertEquals(0, document.getSimulationCount());
		assertFalse(document.isRedoAvailable());
	}

	@Test
	public void undoRedoSimulationSettingChanges_restoresSettingsAndKeepsResults() throws SimulationException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = new OpenRocketDocument(rocket);

		Simulation simulation = createSimulation(document, rocket, "Original");
		document.addSimulation(simulation);

		simulation.simulate();
		FlightData dataBefore = simulation.getSimulatedData();
		assertNotNull(dataBefore);

		double rodLengthBefore = simulation.getOptions().getLaunchRodLength();
		double maxAltitudeBefore = dataBefore.getMaxAltitude();

		document.clearUndo();

		document.addUndoPosition("Edit simulation");
		simulation.setName("Modified");
		simulation.getOptions().setLaunchRodLength(rodLengthBefore + 0.5);
		assertEquals(Simulation.Status.OUTDATED, simulation.getStatus());

		document.undo();
		assertEquals("Original", simulation.getName());
		assertEquals(rodLengthBefore, simulation.getOptions().getLaunchRodLength(), EPSILON);
		assertNotNull(simulation.getSimulatedData());
		assertEquals(maxAltitudeBefore, simulation.getSimulatedData().getMaxAltitude(), 0.001);
		assertEquals(Simulation.Status.UPTODATE, simulation.getStatus());

		document.redo();
		assertEquals("Modified", simulation.getName());
		assertEquals(rodLengthBefore + 0.5, simulation.getOptions().getLaunchRodLength(), EPSILON);
		assertNotNull(simulation.getSimulatedData());
		assertEquals(maxAltitudeBefore, simulation.getSimulatedData().getMaxAltitude(), 0.001);
		assertEquals(Simulation.Status.OUTDATED, simulation.getStatus());
	}

	@Test
	public void redoNotClearedBySimulationRunWhileInUndoHistory() throws SimulationException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = new OpenRocketDocument(rocket);

		Simulation simulation = createSimulation(document, rocket, "Original");
		document.addSimulation(simulation);
		document.clearUndo();

		document.addUndoPosition("Rename simulation");
		simulation.setName("Renamed");
		document.undo();

		assertEquals("Original", simulation.getName());
		assertTrue(document.isRedoAvailable());

		simulation.simulate();

		assertTrue(document.isRedoAvailable(), "Running a simulation should not clear redo history");
		document.redo();
		assertEquals("Renamed", simulation.getName());
	}

	@Test
	public void redoClearedByNewSimulationSettingChangeWhileInUndoHistory() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = new OpenRocketDocument(rocket);

		Simulation simulation = createSimulation(document, rocket, "Original");
		document.addSimulation(simulation);
		document.clearUndo();

		document.addUndoPosition("Rename simulation");
		simulation.setName("Renamed");
		document.undo();

		assertTrue(document.isRedoAvailable());

		simulation.setName("Different change");
		assertFalse(document.isRedoAvailable(), "A new simulation edit should clear redo history");
	}

	@Test
	public void getSimulationIndexAndRemoveSimulationUseIdentityNotEquals() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = new OpenRocketDocument(rocket);

		Simulation first = createSimulation(document, rocket, "Same");
		Simulation second = createSimulation(document, rocket, "Same");

		document.addSimulation(first);
		document.addSimulation(second);

		assertEquals(0, document.getSimulationIndex(first));
		assertEquals(1, document.getSimulationIndex(second));

		document.removeSimulation(second);
		assertEquals(1, document.getSimulationCount());
		assertSame(first, document.getSimulation(0));
		assertEquals(-1, document.getSimulationIndex(second));
	}

	@Test
	public void undoRedoDeletionWithMultipleSimulations_restoresOrderAndResults() throws SimulationException {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = new OpenRocketDocument(rocket);

		Simulation first = createSimulation(document, rocket, "First");
		Simulation second = createSimulation(document, rocket, "Second");
		document.addSimulation(first);
		document.addSimulation(second);

		first.simulate();
		second.simulate();
		assertNotNull(first.getSimulatedData());
		assertNotNull(second.getSimulatedData());

		document.clearUndo();

		document.addUndoPosition("Delete first simulation");
		document.removeSimulation(first);
		assertEquals(1, document.getSimulationCount());
		assertEquals("Second", document.getSimulation(0).getName());

		document.undo();
		assertEquals(2, document.getSimulationCount());
		assertEquals("First", document.getSimulation(0).getName());
		assertEquals("Second", document.getSimulation(1).getName());
		assertNotNull(document.getSimulation(0).getSimulatedData());
		assertNotNull(document.getSimulation(1).getSimulatedData());

		document.redo();
		assertEquals(1, document.getSimulationCount());
		assertEquals("Second", document.getSimulation(0).getName());
	}

	@Test
	public void undoRedoSimulationSettingChange_onlyAffectsEditedSimulation() {
		Rocket rocket = TestRockets.makeEstesAlphaIII();
		OpenRocketDocument document = new OpenRocketDocument(rocket);

		Simulation first = createSimulation(document, rocket, "First");
		Simulation second = createSimulation(document, rocket, "Second");
		document.addSimulation(first);
		document.addSimulation(second);
		document.clearUndo();

		document.addUndoPosition("Edit second simulation");
		second.setName("Second (edited)");

		document.undo();
		assertEquals("First", first.getName());
		assertEquals("Second", second.getName());

		document.redo();
		assertEquals("First", first.getName());
		assertEquals("Second (edited)", second.getName());
	}
}
