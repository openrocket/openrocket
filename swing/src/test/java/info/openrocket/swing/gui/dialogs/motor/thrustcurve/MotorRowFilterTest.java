package info.openrocket.swing.gui.dialogs.motor.thrustcurve;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import org.junit.jupiter.api.Test;

import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.database.motor.ThrustCurveMotorSet;

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
		
		List<ThrustCurveMotorSet> motorList = new ArrayList<>();
		motorList.add(motorSet);
		
		ThrustCurveMotorDatabaseModel model = new ThrustCurveMotorDatabaseModel(motorList);
		MotorRowFilter filter = new MotorRowFilter(model);
		MotorEntry entry = new MotorEntry(model);

		///////////////////////////////////
		// search terms filter
		ArrayList<String> badSearchTerms = new ArrayList<>();

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
		List<Manufacturer> excludedManufacturers = new ArrayList<>();
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
		assertEquals(filter.getExcludedManufacturers().toString(), "[Estes, Loki Research]");
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
			.setCGPoints(new CoordinateIF[] {new Coordinate(0.077, 0, 0, 125.0),
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
			.setCGPoints(new CoordinateIF[] {
					new Coordinate(0.05,0,0,0.05),
					new Coordinate(0.03,0,0,0.03)})
			.build();
		
		testMotor(emptyMotor, new ArrayList(), ImpulseClass.A, ImpulseClass.A, ImpulseClass.B);
	}

	@Test
	public void testSourceFilter() {
		ThrustCurveMotor tcMotor = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setDesignation("H123")
				.setDataSource("thrustcurve.org")
				.setDiameter(0.038)
				.setLength(0.152)
				.setTimePoints(new double[] { 0.0, 1.0, 2.0 })
				.setThrustPoints(new double[] { 0.0, 120.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.076, 0, 0, 0.25),
						new Coordinate(0.076, 0, 0, 0.20),
						new Coordinate(0.076, 0, 0, 0.15)
				})
				.setDigest("tc-only")
				.setUserDefined(false)
				.build();

		ThrustCurveMotor rasaeroMotor = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("NoThrust"))
				.setDesignation("Dart")
				.setDataSource("RASAero")
				.setDiameter(0.001)
				.setLength(0.01)
				.setTimePoints(new double[] { 0.0, 1.0 })
				.setThrustPoints(new double[] { 0.0, 0.001 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.005, 0, 0, 0.01),
						new Coordinate(0.005, 0, 0, 0.01)
				})
				.setDigest("rasaero-only")
				.setUserDefined(false)
				.build();

		ThrustCurveMotor userOnlyMotor = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setDesignation("M1550R")
				.setDiameter(0.075)
				.setLength(0.560)
				.setTimePoints(new double[] { 0.0, 1.0, 2.0 })
				.setThrustPoints(new double[] { 0.0, 1550.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.28, 0, 0, 4.5),
						new Coordinate(0.28, 0, 0, 3.2),
						new Coordinate(0.28, 0, 0, 2.0)
				})
				.setDigest("user-only")
				.setUserDefined(true)
				.build();

		ThrustCurveMotor mixedDbMotor = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setDesignation("K550W")
				.setDataSource("thrustcurve.org")
				.setDiameter(0.054)
				.setLength(0.326)
				.setTimePoints(new double[] { 0.0, 1.0, 2.0 })
				.setThrustPoints(new double[] { 0.0, 550.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.163, 0, 0, 1.45),
						new Coordinate(0.163, 0, 0, 1.10),
						new Coordinate(0.163, 0, 0, 0.73)
				})
				.setDigest("mixed-db")
				.setUserDefined(false)
				.build();

		ThrustCurveMotor mixedUserMotor = new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("AeroTech"))
				.setDesignation("K550W")
				.setDiameter(0.054)
				.setLength(0.326)
				.setTimePoints(new double[] { 0.0, 0.5, 2.0 })
				.setThrustPoints(new double[] { 0.0, 620.0, 0.0 })
				.setCGPoints(new CoordinateIF[] {
						new Coordinate(0.163, 0, 0, 1.45),
						new Coordinate(0.163, 0, 0, 1.10),
						new Coordinate(0.163, 0, 0, 0.73)
				})
				.setDigest("mixed-user")
				.setUserDefined(true)
				.build();

		ThrustCurveMotorSet tcSet = new ThrustCurveMotorSet();
		tcSet.addMotor(tcMotor);

		ThrustCurveMotorSet rasaeroSet = new ThrustCurveMotorSet();
		rasaeroSet.addMotor(rasaeroMotor);

		ThrustCurveMotorSet userSet = new ThrustCurveMotorSet();
		userSet.addMotor(userOnlyMotor);

		ThrustCurveMotorSet mixedSet = new ThrustCurveMotorSet();
		mixedSet.addMotor(mixedDbMotor);
		mixedSet.addMotor(mixedUserMotor);

		ThrustCurveMotorDatabaseModel model = new ThrustCurveMotorDatabaseModel(
				List.of(tcSet, rasaeroSet, userSet, mixedSet));
		MotorRowFilter filter = new MotorRowFilter(model);

		MotorRowFilter.Entry<ThrustCurveMotorDatabaseModel, Integer> tcEntry = entryForRow(model, 0);
		MotorRowFilter.Entry<ThrustCurveMotorDatabaseModel, Integer> rasaeroEntry = entryForRow(model, 1);
		MotorRowFilter.Entry<ThrustCurveMotorDatabaseModel, Integer> userEntry = entryForRow(model, 2);
		MotorRowFilter.Entry<ThrustCurveMotorDatabaseModel, Integer> mixedEntry = entryForRow(model, 3);

		MotorSource tcSource = MotorSource.forDatabaseSource("thrustcurve.org");
		MotorSource rasaeroSource = MotorSource.forDatabaseSource("RASAero");

		assertTrue(filter.getExcludedSources().isEmpty());
		assertTrue(filter.include(tcEntry));
		assertTrue(filter.include(rasaeroEntry));
		assertTrue(filter.include(userEntry));
		assertTrue(filter.include(mixedEntry));

		// Exclude only RASAero: ThrustCurve.org, user, and mixed remain included.
		filter.setExcludedSources(List.of(rasaeroSource));
		assertTrue(filter.include(tcEntry));
		assertFalse(filter.include(rasaeroEntry));
		assertTrue(filter.include(userEntry));
		assertTrue(filter.include(mixedEntry));

		// Exclude user-defined only: mixed still included because of its ThrustCurve.org curve.
		filter.setExcludedSources(List.of(MotorSource.USER_DEFINED));
		assertTrue(filter.include(tcEntry));
		assertTrue(filter.include(rasaeroEntry));
		assertFalse(filter.include(userEntry));
		assertTrue(filter.include(mixedEntry));

		// Exclude all database sources: only user and mixed remain.
		filter.setExcludedSources(List.of(tcSource, rasaeroSource));
		assertFalse(filter.include(tcEntry));
		assertFalse(filter.include(rasaeroEntry));
		assertTrue(filter.include(userEntry));
		assertTrue(filter.include(mixedEntry));

		// Reset with null clears exclusions.
		filter.setExcludedSources(null);
		assertTrue(filter.getExcludedSources().isEmpty());
		assertTrue(filter.include(tcEntry));
		assertTrue(filter.include(rasaeroEntry));
		assertTrue(filter.include(userEntry));
		assertTrue(filter.include(mixedEntry));
	}

	private MotorRowFilter.Entry<ThrustCurveMotorDatabaseModel, Integer> entryForRow(
			ThrustCurveMotorDatabaseModel model, int row) {
		return new MotorRowFilter.Entry<>() {
			@Override
			public Integer getIdentifier() {
				return row;
			}

			@Override
			public ThrustCurveMotorDatabaseModel getModel() {
				return model;
			}

			@Override
			public ThrustCurveMotorSet getValue(int index) {
				return model.getMotorSet(row);
			}

			@Override
			public int getValueCount() {
				return 1;
			}
		};
	}
}

