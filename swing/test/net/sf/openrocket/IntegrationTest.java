package net.sf.openrocket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.Action;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.database.ComponentPresetDao;
import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.motor.MotorDatabase;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.motor.GeneralMotorLoader;
import net.sf.openrocket.gui.main.UndoRedoAction;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.masscalc.BasicMassCalculator;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.masscalc.MassCalculator.MassCalcType;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.utils.CoreServicesModule;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.util.Modules;

/**
 * This class contains various integration tests that simulate user actions that
 * might be performed.
 */
@RunWith(JMock.class)
public class IntegrationTest {
	Mockery context = new JUnit4Mockery();
	
	private OpenRocketDocument document;
	private Action undoAction, redoAction;
	
	private AerodynamicCalculator aeroCalc = new BarrowmanCalculator();
	private MassCalculator massCalc = new BasicMassCalculator();
	private Configuration config;
	private FlightConditions conditions;
	private String massComponentID = null;
	
	@BeforeClass
	public static void setUp() throws Exception {
		Module applicationModule = new CoreServicesModule();
		
		Module pluginModule = new PluginModule();
		
		Module debugTranslator = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new DebugTranslator(null));
			}
		};
		
		Module dbOverrides = new AbstractModule() {
			@Override
			protected void configure() {
				bind(ComponentPresetDao.class).toProvider(new EmptyComponentDbProvider());
				bind(MotorDatabase.class).toProvider(new MotorDbProvider());
			}
		};
		
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator), pluginModule, dbOverrides);
		Application.setInjector(injector);
	}
	
	/**
	 * Tests loading a simple rocket design, modifying it, simulating 
	 * it and the undo/redo mechanism in various combinations.
	 */
	@Test
	public void testSimpleRocket() throws SimulationException {
		System.setProperty("openrocket.unittest", "true");
		
		document = loadRocket("simplerocket.ork");
		
		undoAction = UndoRedoAction.newUndoAction(document);
		redoAction = UndoRedoAction.newRedoAction(document);
		config = document.getSimulation(0).getConfiguration();
		conditions = new FlightConditions(config);
		
		// Test undo state
		checkUndoState(null, null);
		
		// Compute cg+cp + altitude
		checkCgCp(0.248, 0.0645, 0.320, 12.0);
		checkAlt(48.2);
		
		// Mass modification
		document.addUndoPosition("Modify mass");
		checkUndoState(null, null);
		massComponent().setComponentMass(0.01);
		checkUndoState("Modify mass", null);
		
		// Check cg+cp + altitude
		checkCgCp(0.230, 0.0745, 0.320, 12.0);
		checkAlt(37.2);
		
		// Non-change
		document.addUndoPosition("No change");
		checkUndoState("Modify mass", null);
		
		// Non-funcitonal change
		document.addUndoPosition("Name change");
		checkUndoState("Modify mass", null);
		massComponent().setName("Foobar component");
		checkUndoState("Name change", null);
		
		// Check cg+cp
		checkCgCp(0.230, 0.0745, 0.320, 12.0);
		
		// Aerodynamic modification
		document.addUndoPosition("Remove component");
		checkUndoState("Name change", null);
		document.getRocket().getChild(0).removeChild(0);
		checkUndoState("Remove component", null);
		
		// Check cg+cp + altitude
		checkCgCp(0.163, 0.0613, 0.275, 9.95);
		checkAlt(45.0);
		
		// Undo "Remove component" change
		undoAction.actionPerformed(new ActionEvent(this, 0, "foo"));
		assertTrue(document.getRocket().getChild(0).getChild(0) instanceof NoseCone);
		checkUndoState("Name change", "Remove component");
		
		// Check cg+cp + altitude
		checkCgCp(0.230, 0.0745, 0.320, 12.0);
		checkAlt(37.2);
		
		// Undo "Name change" change
		undoAction.actionPerformed(new ActionEvent(this, 0, "foo"));
		assertEquals("Extra mass", massComponent().getName());
		checkUndoState("Modify mass", "Name change");
		
		// Check cg+cp
		checkCgCp(0.230, 0.0745, 0.320, 12.0);
		
		// Undo "Modify mass" change
		undoAction.actionPerformed(new ActionEvent(this, 0, "foo"));
		assertEquals(0, massComponent().getComponentMass(), 0);
		checkUndoState(null, "Modify mass");
		
		// Check cg+cp + altitude
		checkCgCp(0.248, 0.0645, 0.320, 12.0);
		checkAlt(48.2);
		
		// Redo "Modify mass" change
		redoAction.actionPerformed(new ActionEvent(this, 0, "foo"));
		assertEquals(0.010, massComponent().getComponentMass(), 0.00001);
		checkUndoState("Modify mass", "Name change");
		
		// Check cg+cp + altitude
		checkCgCp(0.230, 0.0745, 0.320, 12.0);
		checkAlt(37.2);
		
		// Mass modification
		document.addUndoPosition("Modify mass2");
		checkUndoState("Modify mass", "Name change");
		massComponent().setComponentMass(0.015);
		checkUndoState("Modify mass2", null);
		
		// Check cg+cp + altitude
		checkCgCp(0.223, 0.0795, 0.320, 12.0);
		checkAlt(32.7);
		
		// Perform component movement
		document.startUndo("Move component");
		document.getRocket().freeze();
		RocketComponent bodytube = document.getRocket().getChild(0).getChild(1);
		RocketComponent innertube = bodytube.getChild(2);
		RocketComponent engineblock = innertube.getChild(0);
		assertTrue(innertube.removeChild(engineblock));
		bodytube.addChild(engineblock, 0);
		checkUndoState("Modify mass2", null);
		document.getRocket().thaw();
		checkUndoState("Move component", null);
		document.stopUndo();
		
		// Check cg+cp + altitude
		checkCgCp(0.221, 0.0797, 0.320, 12.0);
		checkAlt(32.7);
		
		// Modify mass without setting undo description
		massComponent().setComponentMass(0.020);
		checkUndoState("Modify mass2", null);
		
		// Check cg+cp + altitude
		checkCgCp(0.215, 0.0847, 0.320, 12.0);
		checkAlt(29.0);
		
		// Undo "Modify mass2" change
		undoAction.actionPerformed(new ActionEvent(this, 0, "foo"));
		assertEquals(0.015, massComponent().getComponentMass(), 0.0000001);
		checkUndoState("Move component", "Modify mass2");
		
		// Check cg+cp + altitude
		checkCgCp(0.221, 0.0797, 0.320, 12.0);
		checkAlt(32.7);
		
		// Undo "Move component" change
		undoAction.actionPerformed(new ActionEvent(this, 0, "foo"));
		assertTrue(document.getRocket().getChild(0).getChild(1).getChild(2).getChild(0) instanceof EngineBlock);
		checkUndoState("Modify mass2", "Move component");
		
		// Check cg+cp + altitude
		checkCgCp(0.223, 0.0795, 0.320, 12.0);
		checkAlt(32.7);
		
		// Redo "Move component" change
		redoAction.actionPerformed(new ActionEvent(this, 0, "foo"));
		assertTrue(document.getRocket().getChild(0).getChild(1).getChild(0) instanceof EngineBlock);
		checkUndoState("Move component", "Modify mass2");
		
		// Check cg+cp + altitude
		checkCgCp(0.221, 0.0797, 0.320, 12.0);
		checkAlt(32.7);
		
	}
	
	/* *******************
	 * * Utility Methods *
	 * *******************
	 */
	
	private static ThrustCurveMotor readMotor() {
		GeneralMotorLoader loader = new GeneralMotorLoader();
		InputStream is = IntegrationTest.class.getResourceAsStream("Estes_A8.rse");
		assertNotNull("Problem in unit test, cannot find Estes_A8.rse", is);
		try {
			for (Motor m : loader.load(is, "Estes_A8.rse")) {
				return (ThrustCurveMotor) m;
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException: " + e);
		}
		throw new RuntimeException("Could not load motor");
	}
	
	private static class EmptyComponentDbProvider implements Provider<ComponentPresetDao> {
		
		final ComponentPresetDao db = new ComponentPresetDatabase();
		
		@Override
		public ComponentPresetDao get() {
			return db;
		}
	}
	
	private static class MotorDbProvider implements Provider<ThrustCurveMotorSetDatabase> {
		
		final ThrustCurveMotorSetDatabase db = new ThrustCurveMotorSetDatabase();
		
		public MotorDbProvider() {
			db.addMotor(readMotor());
			
			assertEquals(1, db.getMotorSets().size());
		}
		
		@Override
		public ThrustCurveMotorSetDatabase get() {
			return db;
		}
	}
	
	private MassComponent massComponent() {
		if (massComponentID == null) {
			massComponentID = document.getRocket().getChild(0).getChild(1).getChild(0).getID();
		}
		return (MassComponent) document.getRocket().findComponent(massComponentID);
	}
	
	private void checkUndoState(String undoDesc, String redoDesc) {
		if (undoDesc == null) {
			assertEquals("[UndoRedoAction.OpenRocketDocument.Undo]", undoAction.getValue(Action.NAME));
			assertFalse(undoAction.isEnabled());
		} else {
			assertEquals("[UndoRedoAction.OpenRocketDocument.Undo] (" + undoDesc + ")", undoAction.getValue(Action.NAME));
			assertTrue(undoAction.isEnabled());
		}
		if (redoDesc == null) {
			assertEquals("[UndoRedoAction.OpenRocketDocument.Redo]", redoAction.getValue(Action.NAME));
			assertFalse(redoAction.isEnabled());
		} else {
			assertEquals("[UndoRedoAction.OpenRocketDocument.Redo] (" + redoDesc + ")", redoAction.getValue(Action.NAME));
			assertTrue(redoAction.isEnabled());
		}
	}
	
	private void checkCgCp(double cgx, double mass, double cpx, double cna) {
		Coordinate cg, cp;
		
		cg = massCalc.getCG(config, MassCalcType.LAUNCH_MASS);
		assertEquals(cgx, cg.x, 0.001);
		assertEquals(mass, cg.weight, 0.0005);
		
		cp = aeroCalc.getWorstCP(config, conditions, null);
		assertEquals(cpx, cp.x, 0.001);
		assertEquals(cna, cp.weight, 0.1);
	}
	
	
	private void checkAlt(double expected) throws SimulationException {
		Simulation simulation = document.getSimulation(0);
		double actual;
		
		// Simulate + check altitude
		simulation.simulate();
		actual = simulation.getSimulatedData().getBranch(0).getMaximum(FlightDataType.TYPE_ALTITUDE);
		assertEquals(expected, actual, 0.5);
	}
	
	private OpenRocketDocument loadRocket(String fileName) {
		GeneralRocketLoader loader = new GeneralRocketLoader(new File(fileName));
		InputStream is = this.getClass().getResourceAsStream(fileName);
		String failMsg = String.format("Problem in unit test, cannot find %s", fileName);
		assertNotNull(failMsg, is);
		
		OpenRocketDocument rocketDoc = null;
		try {
			rocketDoc = loader.load(is);
		} catch (RocketLoadException e) {
			fail("RocketLoadException while loading file " + fileName + " : " + e.getMessage());
		}
		
		try {
			is.close();
		} catch (IOException e) {
			fail("Unable to close input stream for file " + fileName + ": " + e.getMessage());
		}
		
		return rocketDoc;
	}
	
}
