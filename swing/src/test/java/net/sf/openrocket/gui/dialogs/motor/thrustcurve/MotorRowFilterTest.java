package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.gui.dialogs.motor.thrustcurve.MotorFilterPanel;
import net.sf.openrocket.gui.dialogs.motor.thrustcurve.ThrustCurveMotorDatabaseModel;
import net.sf.openrocket.gui.dialogs.motor.thrustcurve.ImpulseClass;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.database.motor.ThrustCurveMotorSet;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Pair;
	
public class MotorRowFilterTest {
	// just "slightly" above or below limits; also accuracy of
	// equality tests
	// MotorRowFilter has hard-coded "slop" of 0.0015 in testing
	// minimum diameter, so EPSILON must be greater than this
	private final double EPSILON = 0.002;

	// I need to define a motor entry to test the filter
	private class MotorEntry extends MotorRowFilter.Entry<ThrustCurveMotorDatabaseModel, Integer> {
		private final ThrustCurveMotorDatabaseModel model;

		public MotorEntry(ThrustCurveMotorDatabaseModel model) {
			this.model = model;
		}

		@Override
		public Integer getIdentifier() {
			return 0;
		}

		@Override
		public ThrustCurveMotorDatabaseModel getModel() {
			return model;
		}

		@Override
		public ThrustCurveMotorSet getValue(int index) {
			return model.getMotorSet(index);
		}

		@Override
		public int getValueCount() {
			return 1;
		}
	}
	
	private void testMotor(ThrustCurveMotor motor, ArrayList goodSearchTerms, ImpulseClass lowImpulse, ImpulseClass impulse, ImpulseClass highImpulse) {

		ThrustCurveMotorSet motorSet = new ThrustCurveMotorSet();
		motorSet.addMotor(motor);
		
		List<ThrustCurveMotorSet> motorList = new ArrayList<ThrustCurveMotorSet>();
		motorList.add(motorSet);
		
		ThrustCurveMotorDatabaseModel model = new ThrustCurveMotorDatabaseModel(motorList);
		MotorRowFilter filter = new MotorRowFilter(model);
		MotorEntry entry = new MotorEntry(model);

		///////////////////////////////////
		// search terms filter
		ArrayList<String> badSearchTerms = new ArrayList<String>();

		// Two search terms, one present and one not
		badSearchTerms.add("xx");
		badSearchTerms.add("tec");
		filter.setSearchTerms(badSearchTerms);
		assertFalse(filter.include(entry));

		filter.setSearchTerms(goodSearchTerms);
		if (!goodSearchTerms.isEmpty()) {
			assertTrue(filter.include(entry));
		}

		////////////////////////////////////
		// minimum length set/get/filter
		double minLength = motor.getLength() + EPSILON;
		filter.setMinimumLength(minLength);
		assertEquals(minLength, filter.getMinimumLength(), EPSILON);
		assertFalse(filter.include(entry));

		filter.setMinimumLength(motor.getLength() - EPSILON);
		assertTrue(filter.include(entry));

		//////////////////////////////////
		// maximum length set/get/filter
		double maxLength = motor.getLength() - EPSILON;
		filter.setMaximumLength(maxLength);
		assertEquals(maxLength, filter.getMaximumLength(), EPSILON);
		assertFalse(filter.include(entry));

		filter.setMaximumLength(motor.getLength() + EPSILON);
		assertTrue(filter.include(entry));

		//////////////////////////////////
		// minimum diameter set/get/filter
		double minDiameter = motor.getDiameter() + EPSILON;
		filter.setMinimumDiameter(minDiameter);
		assertEquals(minDiameter, filter.getMinimumDiameter(), EPSILON);
		assertFalse(filter.include(entry));

		filter.setMinimumDiameter(motor.getDiameter() - EPSILON);
		assertTrue(filter.include(entry));
		
		//////////////////////////////////
		// maximum diameter set/get/filter
		double maxDiameter = motor.getDiameter() - EPSILON;
		filter.setMaximumDiameter(maxDiameter);
		assertEquals(maxDiameter, filter.getMaximumDiameter(), EPSILON);
		assertFalse(filter.include(entry));

		filter.setMaximumDiameter(motor.getDiameter() + EPSILON);
		assertTrue(filter.include(entry));

		//////////////////////////////////
		// manufacturer set/get/filter presence/absence
		List<Manufacturer> excludedManufacturers = new ArrayList<Manufacturer>();
		if (motor.getManufacturer() != Manufacturer.getManufacturer("UNKNOWN")) {
			excludedManufacturers.add(Manufacturer.getManufacturer("Estes"));		
			excludedManufacturers.add(motor.getManufacturer());
			filter.setExcludedManufacturers(excludedManufacturers);
			assertFalse(filter.include(entry));
		}
		
		excludedManufacturers.clear();
		excludedManufacturers.add(Manufacturer.getManufacturer("Estes"));
		excludedManufacturers.add(Manufacturer.getManufacturer("Loki"));
		filter.setExcludedManufacturers(excludedManufacturers);
		assertEquals("[Estes, Loki Research]", filter.getExcludedManufacturers().toString());
		assertTrue(filter.include(entry));

		//////////////////////////////////
		// minimum impulse class set/get/filter
		filter.setMinimumImpulse(highImpulse);
		assertEquals(highImpulse, filter.getMinimumImpulse());
		assertFalse(filter.include(entry));			

		filter.setMinimumImpulse(impulse);
		assertEquals(impulse, filter.getMinimumImpulse());		
		assertTrue(filter.include(entry));
		
		//////////////////////////////////		
		// maximum impulse class set/get/filter
		if (lowImpulse != impulse) {
			filter.setMaximumImpulse(lowImpulse);
			assertEquals(lowImpulse, filter.getMaximumImpulse());
		
			assertFalse(filter.include(entry));
		}

		filter.setMaximumImpulse(impulse);
		assertTrue(filter.include(entry));

		//////////////////////////////////		
		// hide unavailable set/get
		filter.setHideUnavailable(true);
		assertTrue(filter.isHideUnavailable());

		filter.setHideUnavailable(false);
		assertFalse(filter.isHideUnavailable());
	}

	@Test
	public void TestFullMotor() {
		// create a motor with all fields explicitly defined
		// This is the Aerotech H123 from thrustcurve.org, motor ID 917
		final ThrustCurveMotor fullMotor = new ThrustCurveMotor.Builder()
			.setCaseInfo("Aerotech 38/240")
			.setCGPoints(new Coordinate[] {new Coordinate(0.077, 0, 0, 125.0),
										   new Coordinate(0.077, 0, 0, 109.93),
										   new Coordinate(0.077, 0, 0, 82.3872),
										   new Coordinate(0.077, 0, 0, 75.6278),
										   new Coordinate(0.077, 0, 0, 69.1235),
										   new Coordinate(0.077, 0, 0, 40.4295),
										   new Coordinate(0.077, 0, 0, 30.4827),
										   new Coordinate(0.077, 0, 0, 17.4757),
										   new Coordinate(0.077, 0, 0, 7.526),
										   new Coordinate(0.077, 0, 0, 4.84713),
										   new Coordinate(0.077, 0, 0, 2.67857),
										   new Coordinate(0.077, 0, 0, 2.6)})
			.setDescription("Description of H123")
			.setDesignation("H123")
			.setDiameter(0.038)
			.setDigest("digestH123")
			.setInitialMass(293.3)
			.setLength(0.152)
			.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
			.setMotorType(Motor.Type.RELOAD)
			.setPropellantInfo("White Lightning")
			.setStandardDelays(new double[] {6, 10, 14})
			.setThrustPoints(new double[] {138.42, 116.45, 116.45, 112.18, 107.82, 86.29, 81.93, 64.72, 47.46, 43.15, 30.2, 0.0})
			.setTimePoints(new double[] {0.0, 0.2, 0.6, 0.7, 0.8, 1.3, 1.5, 1.8, 2.1, 2.2, 2.3, 2.6})
			.build();
		
		// two search terms, both present, one only a substring of a column
		ArrayList searchTerms = new ArrayList(2);
		searchTerms.add("aerotech");
		searchTerms.add("h12");
		
		testMotor(fullMotor, searchTerms, ImpulseClass.G, ImpulseClass.H, ImpulseClass.I);
	}

	@Test
	public void TestEmptyMotor() {
	// a motor with minimum fields explicitly defined
		final ThrustCurveMotor emptyMotor = new ThrustCurveMotor.Builder()
			.setTimePoints(new double[] {0, 1})
			.setThrustPoints(new double[] {0, 2})
			.setLength(0.10)
			.setCGPoints(new Coordinate[] {
					new Coordinate(0.05,0,0,0.05),
					new Coordinate(0.03,0,0,0.03)})
			.build();
		
		testMotor(emptyMotor, new ArrayList(), ImpulseClass.A, ImpulseClass.A, ImpulseClass.B);
	}
}

