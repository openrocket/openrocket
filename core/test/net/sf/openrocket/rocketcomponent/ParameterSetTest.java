package net.sf.openrocket.rocketcomponent;

//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThat;
//import static org.junit.Assert.assertTrue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;


public class ParameterSetTest extends BaseTestCase {
		

	static int gid=0; 
	FlightConfigurableParameterSet<TestParameter> testSet = null;
	
	private class TestParameter implements FlightConfigurableParameter<TestParameter> {
		public final int id;
		
		public TestParameter(){
			id = gid++;
		}
 	
		@Override
		public void update(){}
		
		@Override
		public boolean equals( Object other ){
			if( other instanceof TestParameter){
				return (this.id == ((TestParameter)other).id);
			}
			return false;
		}

		@Override
		public int hashCode(){
			return this.id;
		}
		
		@Override
		public String toString(){
			return "tp#:"+id;
		}

        @Override
        public TestParameter clone(){
            return new TestParameter();
        }

        @Override
        public TestParameter copy( final FlightConfigurationId copyId){
            return new TestParameter();
        }
	};
	
	@Before
	public void localSetUp() {
		gid=0;
		TestParameter defaultParam = new TestParameter();
		testSet = new FlightConfigurableParameterSet<TestParameter>( defaultParam );
	}
	
	// ================ Actual Tests ================

	@Test
	public void testEmptySet() {
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));
		TestParameter dtp = new TestParameter();
		testSet.setDefault( dtp);
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));
		assertThat("set stores default value correctly: ", testSet.getDefault(), equalTo( dtp )); 
	}
	
	@Test
	public void testRetrieveDefault(){
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		// i.e. requesting the value for a non-existent config id should return the default
		assertThat("set stores id-value pair correctly : ", testSet.get(fcid2), equalTo( testSet.getDefault() )); 
		assertThat("set contains wrong number of overrides: ", testSet.size(), equalTo( 0 ));		
		
		FlightConfigurationId fcid_def = FlightConfigurationId.DEFAULT_VALUE_FCID;
		assertThat("retrieving the via the special default key should produce the default value: ", testSet.get(fcid_def), equalTo( testSet.getDefault() )); 
		assertThat("set should still contain zero overrides: ", testSet.size(), equalTo( 0 ));
	}
		
	@Test
	public void testSetGetSecond(){
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));		
		
		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertThat("set stores default value correctly: ", testSet.get(fcid2), equalTo( tp2 ));
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 1 ));
	}

	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetByNegativeIndex() {
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));		
		
		//assertThat
		testSet.get(-1);
	}


	@Test(expected=IndexOutOfBoundsException.class)
	public void testGetByTooHighIndex() {
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));		
		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		assertThat("set should contain one override: ", testSet.size(), equalTo( 1 ));		
		
		//assertThat
		testSet.get(1);   // this should be off-by-one (too high)
	}

	@Test
	public void testGetIdsLength(){
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));		
		
		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		
		TestParameter tp3 = new TestParameter();
		FlightConfigurationId fcid3 = new FlightConfigurationId();
		testSet.set(fcid3, tp3);
		
		assertThat("set should contain two overrides: ", testSet.size(), equalTo( 2 ));

		// testSet.getSortedConfigurationIDs()
		// >> this function should ONLY return ids for the overrides
		assertThat("getIds() broken!\n"+testSet.toDebug(), testSet.getIds().size(), equalTo( testSet.size()));
		assertThat("getIds() broken!\n"+testSet.toDebug(), testSet.getIds().size(), equalTo( testSet.getIds().size() ) );
	}
	
	@Test
	public void testGetByIndex(){
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));		


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
		
		assertThat("set should contain two overrides: ", testSet.size(), equalTo( 4 ));

		ArrayList<FlightConfigurationId> refList = new ArrayList<FlightConfigurationId>();
		refList.add(fcid1);
		refList.add(fcid2);
		refList.add(fcid3);
		refList.add(fcid4);
	    Collections.sort(refList); 		// Java 1.7:
	    
		//assertThat
	    assertThat("retrieve-by-index broken!\n"+testSet.toDebug(), testSet.get(0), equalTo( testSet.get( refList.get(0))));
		assertThat("retrieve-by-index broken!\n"+testSet.toDebug(), testSet.get(1), equalTo( testSet.get( refList.get(1))));
		assertThat("retrieve-by-index broken!\n"+testSet.toDebug(), testSet.get(2), equalTo( testSet.get( refList.get(2))));
		assertThat("retrieve-by-index broken!\n"+testSet.toDebug(), testSet.get(3), equalTo( testSet.get( refList.get(3))));
	}

	
	@Test
	public void testRemoveSecond(){
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));		
		
		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertThat("set stores default value correctly: ", testSet.get(fcid2), equalTo( tp2 )); 
		assertThat("set should contain one override: ", testSet.size(), equalTo( 1 ));
		
		testSet.set(fcid2, null);
		// fcid <=> tp2 should be stored....
		assertThat("set stores default value correctly: ", testSet.get(fcid2), equalTo( testSet.getDefault() )); 
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));
	}
	

	@Test
	public void testGetByValue(){
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));		
		assertThat("retrieving the default value should produce the special default key: ", 
				testSet.getId(testSet.getDefault()), equalTo( FlightConfigurationId.DEFAULT_VALUE_FCID));
		
		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertThat("set should contain one override: ", testSet.size(), equalTo( 1 ));
		assertThat("set stores default value correctly: ", testSet.get(fcid2), equalTo( tp2 )); 

		// now retrieve that same parameter by value
		FlightConfigurationId fcid3 = testSet.getId(tp2);
		assertThat("set should contain one override: ", testSet.size(), equalTo( 1 ));
		assertThat("set stores default value correctly: ", fcid2, equalTo( fcid3 )); 
		assertThat("set stores default value correctly: ", testSet.get( fcid3), equalTo( tp2 ));
	}
	
	
	@Test
	public void testCloneSecond(){
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 0 ));
		
		TestParameter tp2 = new TestParameter();
		FlightConfigurationId fcid2 = new FlightConfigurationId();
		testSet.set(fcid2, tp2);
		// fcid <=> tp2 should be stored....
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 1 ));
		assertThat("set stores default value correctly: ", testSet.get(fcid2), equalTo( tp2 )); 
		
		FlightConfigurationId fcid3 = new FlightConfigurationId();
		testSet.copyFlightConfiguration(fcid2, fcid3);
		// fcid <=> tp2 should be stored....
		assertThat("set should contain zero overrides: ", testSet.size(), equalTo( 2 ));
		assertThat("set stores default value correctly: ", testSet.get(fcid3), not( testSet.getDefault() )); 
	}
	
	
	
}
