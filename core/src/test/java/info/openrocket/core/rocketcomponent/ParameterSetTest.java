package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.BaseTestCase;

public class ParameterSetTest extends BaseTestCase {

	static int gid = 0;
	FlightConfigurableParameterSet<TestParameter> testSet = null;

	private class TestParameter implements FlightConfigurableParameter<TestParameter> {
		public final int id;

		public TestParameter() {
			id = gid++;
		}

		@Override
		public void update() {
		}

		@Override
		public boolean equals(Object other) {
			if (other instanceof TestParameter) {
				return (this.id == ((TestParameter) other).id);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return this.id;
		}

		@Override
		public String toString() {
			return "tp#:" + id;
		}

		@Override
		public TestParameter clone() {
			return new TestParameter();
		}

		@Override
		public TestParameter copy(final FlightConfigurationId copyId) {
			return new TestParameter();
		}
	}

	@BeforeEach
	public void localSetUp() {
		gid = 0;
		TestParameter defaultParam = new TestParameter();
		testSet = new FlightConfigurableParameterSet<TestParameter>(defaultParam);
	}

	// ================ Actual Tests ================

	@Test
	public void testEmptySet() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");
		TestParameter dtp = new TestParameter();
		testSet.setDefault(dtp);
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");
		assertEquals(testSet.getDefault(), dtp, "set stores default value correctly: ");
	}

	@Test
	public void testRetrieveDefault() {
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		// i.e. requesting the value for a non-existent config id should return the
		// default
		assertEquals(testSet.getDefault(), testSet.get(fcid2), "set stores id-value pair correctly : ");
		assertEquals(0, testSet.size(), "set contains wrong number of overrides: ");

		FlightConfigurationId fcid_def = FlightConfigurationId.DEFAULT_VALUE_FCID;
		assertEquals(testSet.getDefault(), testSet.get(fcid_def), "retrieving the via the special default key should produce the default value: ");
		assertEquals(0, testSet.size(), "set should still contain zero overrides: ");
	}

	@Test
	public void testSetGetSecond() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertEquals(testSet.get(fcid2), tp2, "set stores default value correctly: ");
		assertEquals(testSet.size(), 1, "set should contain zero overrides: ");
	}

	@Test
	public void testGetByNegativeIndex() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");

		// assertEquals
		assertThrows(IndexOutOfBoundsException.class, () -> testSet.get(-1));
	}

	@Test
	public void testGetByTooHighIndex() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");
		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		assertEquals(testSet.size(), 1, "set should contain one override: ");

		// assertEquals
		assertThrows(IndexOutOfBoundsException.class, () -> testSet.get(1));
	}

	@Test
	public void testGetIdsLength() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);

		TestParameter tp3 = new TestParameter();
		FlightConfigurationId fcid3 = new FlightConfigurationId();
		testSet.set(fcid3, tp3);

		assertEquals(testSet.size(), 2, "set should contain two overrides: ");

		// testSet.getSortedConfigurationIDs()
		// >> this function should ONLY return ids for the overrides
		assertEquals(testSet.getIds().size(), testSet.size(), "getIds() broken!\n" + testSet.toDebug());
		assertEquals(testSet.getIds().size(), testSet.getIds().size(), "getIds() broken!\n" + testSet.toDebug());
	}

	@Test
	public void testGetByIndex() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");

		TestParameter tp1 = new TestParameter();
		FlightConfigurationId fcid1 = new FlightConfigurationId();
		testSet.set(fcid1, tp1);

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);

		TestParameter tp3 = new TestParameter();
		FlightConfigurationId fcid3 = new FlightConfigurationId();
		testSet.set(fcid3, tp3);

		TestParameter tp4 = new TestParameter();
		FlightConfigurationId fcid4 = new FlightConfigurationId();
		testSet.set(fcid4, tp4);

		assertEquals(testSet.size(), 4, "set should contain two overrides: ");

		ArrayList<FlightConfigurationId> refList = new ArrayList<FlightConfigurationId>();
		refList.add(fcid1);
		refList.add(fcid2);
		refList.add(fcid3);
		refList.add(fcid4);

		// assertEquals
		assertEquals(testSet.get(0), testSet.get(refList.get(0)), "retrieve-by-index broken!\n" + testSet.toDebug());
		assertEquals(testSet.get(1), testSet.get(refList.get(1)), "retrieve-by-index broken!\n" + testSet.toDebug());
		assertEquals(testSet.get(2), testSet.get(refList.get(2)), "retrieve-by-index broken!\n" + testSet.toDebug());
		assertEquals(testSet.get(3), testSet.get(refList.get(3)), "retrieve-by-index broken!\n" + testSet.toDebug());
	}

	@Test
	public void testRemoveSecond() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertEquals(testSet.get(fcid2), tp2, "set stores default value correctly: ");
		assertEquals(testSet.size(), 1, "set should contain one override: ");

		testSet.set(fcid2, null);
		// fcid <=> tp2 should be stored....
		assertEquals(testSet.get(fcid2), testSet.getDefault(), "set stores default value correctly: ");
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");
	}

	@Test
	public void testGetByValue() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");
		assertEquals(testSet.getId(testSet.getDefault()), FlightConfigurationId.DEFAULT_VALUE_FCID, "retrieving the default value should produce the special default key: ");

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertEquals(testSet.size(), 1, "set should contain one override: ");
		assertEquals(testSet.get(fcid2), tp2, "set stores default value correctly: ");

		// now retrieve that same parameter by value
		FlightConfigurationId fcid3 = testSet.getId(tp2);
		assertEquals(testSet.size(), 1, "set should contain one override: ");
		assertEquals(fcid2, fcid3, "set stores default value correctly: ");
		assertEquals(testSet.get(fcid3), tp2, "set stores default value correctly: ");
	}

	@Test
	public void testCloneSecond() {
		assertEquals(testSet.size(), 0, "set should contain zero overrides: ");

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertEquals(testSet.size(), 1, "set should contain zero overrides: ");
		assertEquals(testSet.get(fcid2), tp2, "set stores default value correctly: ");

		FlightConfigurationId fcid3 = new FlightConfigurationId();
		testSet.copyFlightConfiguration(fcid2, fcid3);
		// fcid <=> tp2 should be stored....
		assertEquals(testSet.size(), 2, "set should contain zero overrides: ");
		assertNotEquals(testSet.get(fcid3), testSet.getDefault(), "set stores default value correctly: ");
	}

	/**
	 * Confirms the ordering of the flights are as inserted.
	 */
	@Test
	public void testOrdering() {
		TestParameter tp1 = new TestParameter();
		FlightConfigurationId fcid1 = new FlightConfigurationId();
		testSet.set(fcid1, tp1);

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);

		TestParameter tp3 = new TestParameter();
		FlightConfigurationId fcid3 = new FlightConfigurationId();
		testSet.set(fcid3, tp3);

		TestParameter tp4 = new TestParameter();
		FlightConfigurationId fcid4 = new FlightConfigurationId();
		testSet.set(fcid4, tp4);

		List<FlightConfigurationId> refList = new ArrayList<FlightConfigurationId>();
		refList.add(fcid1);
		refList.add(fcid2);
		refList.add(fcid3);
		refList.add(fcid4);

		assertEquals(refList, testSet.getIds());
	}
}
