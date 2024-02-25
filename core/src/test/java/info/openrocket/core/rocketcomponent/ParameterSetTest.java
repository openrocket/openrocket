package info.openrocket.core.rocketcomponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.Before;
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

	@Before
	public void localSetUp() {
		gid = 0;
		TestParameter defaultParam = new TestParameter();
		testSet = new FlightConfigurableParameterSet<TestParameter>(defaultParam);
	}

	// ================ Actual Tests ================

	@Test
	public void testEmptySet() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);
		TestParameter dtp = new TestParameter();
		testSet.setDefault(dtp);
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);
		assertEquals("set stores default value correctly: ", testSet.getDefault(), dtp);
	}

	@Test
	public void testRetrieveDefault() {
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		// i.e. requesting the value for a non-existent config id should return the
		// default
		assertEquals("set stores id-value pair correctly : ", testSet.get(fcid2), testSet.getDefault());
		assertEquals("set contains wrong number of overrides: ", testSet.size(), 0);

		FlightConfigurationId fcid_def = FlightConfigurationId.DEFAULT_VALUE_FCID;
		assertEquals("retrieving the via the special default key should produce the default value: ",
				testSet.get(fcid_def), testSet.getDefault());
		assertEquals("set should still contain zero overrides: ", testSet.size(), 1);
	}

	@Test
	public void testSetGetSecond() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertEquals("set stores default value correctly: ", testSet.get(fcid2), tp2);
		assertEquals("set should contain zero overrides: ", testSet.size(), 1);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetByNegativeIndex() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);

		// assertEquals
		testSet.get(-1);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetByTooHighIndex() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);
		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		assertEquals("set should contain one override: ", testSet.size(), 1);

		// assertEquals
		testSet.get(1); // this should be off-by-one (too high)
	}

	@Test
	public void testGetIdsLength() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);

		TestParameter tp3 = new TestParameter();
		FlightConfigurationId fcid3 = new FlightConfigurationId();
		testSet.set(fcid3, tp3);

		assertEquals("set should contain two overrides: ", testSet.size(), 2);

		// testSet.getSortedConfigurationIDs()
		// >> this function should ONLY return ids for the overrides
		assertEquals("getIds() broken!\n" + testSet.toDebug(), testSet.getIds().size(), testSet.size());
		assertEquals("getIds() broken!\n" + testSet.toDebug(), testSet.getIds().size(), testSet.getIds().size());
	}

	@Test
	public void testGetByIndex() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);

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

		assertEquals("set should contain two overrides: ", testSet.size(), 4);

		ArrayList<FlightConfigurationId> refList = new ArrayList<FlightConfigurationId>();
		refList.add(fcid1);
		refList.add(fcid2);
		refList.add(fcid3);
		refList.add(fcid4);

		// assertEquals
		assertEquals("retrieve-by-index broken!\n" + testSet.toDebug(), testSet.get(0), testSet.get(refList.get(0)));
		assertEquals("retrieve-by-index broken!\n" + testSet.toDebug(), testSet.get(1), testSet.get(refList.get(1)));
		assertEquals("retrieve-by-index broken!\n" + testSet.toDebug(), testSet.get(2), testSet.get(refList.get(2)));
		assertEquals("retrieve-by-index broken!\n" + testSet.toDebug(), testSet.get(3), testSet.get(refList.get(3)));
	}

	@Test
	public void testRemoveSecond() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertEquals("set stores default value correctly: ", testSet.get(fcid2), tp2);
		assertEquals("set should contain one override: ", testSet.size(), 1);

		testSet.set(fcid2, null);
		// fcid <=> tp2 should be stored....
		assertEquals("set stores default value correctly: ", testSet.get(fcid2), testSet.getDefault());
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);
	}

	@Test
	public void testGetByValue() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);
		assertEquals("retrieving the default value should produce the special default key: ",
				testSet.getId(testSet.getDefault()), FlightConfigurationId.DEFAULT_VALUE_FCID);

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertEquals("set should contain one override: ", testSet.size(), 1);
		assertEquals("set stores default value correctly: ", testSet.get(fcid2), tp2);

		// now retrieve that same parameter by value
		FlightConfigurationId fcid3 = testSet.getId(tp2);
		assertEquals("set should contain one override: ", testSet.size(), 1);
		assertEquals("set stores default value correctly: ", fcid2, fcid3);
		assertEquals("set stores default value correctly: ", testSet.get(fcid3), tp2);
	}

	@Test
	public void testCloneSecond() {
		assertEquals("set should contain zero overrides: ", testSet.size(), 0);

		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertEquals("set should contain zero overrides: ", testSet.size(), 1);
		assertEquals("set stores default value correctly: ", testSet.get(fcid2), tp2);

		FlightConfigurationId fcid3 = new FlightConfigurationId();
		testSet.copyFlightConfiguration(fcid2, fcid3);
		// fcid <=> tp2 should be stored....
		assertEquals("set should contain zero overrides: ", testSet.size(), 2);
		assertNotEquals("set stores default value correctly: ", testSet.get(fcid3), testSet.getDefault());
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
