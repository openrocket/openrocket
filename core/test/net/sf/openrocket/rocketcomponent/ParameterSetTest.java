package net.sf.openrocket.rocketcomponent;

//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThat;
//import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;


public class ParameterSetTest extends BaseTestCase {
		
	private final static double EPSILON = MathUtil.EPSILON*1E3; 
	
	private class Parameter implements FlightConfigurableParameter<Parameter> {
		 	
		public Parameter(){}
		
		@Override
		public Parameter clone(){ return null; }
		 	
		@Override
		public void update(){}
		 	
		@Override
		public void addChangeListener(StateChangeListener listener){}
			
		@Override
		public void removeChangeListener(StateChangeListener listener){} 	
			
	};
	
	
	@Test
	public void testEmptyRocket() {
		//FlightConfigurableParameterSet<Parameter> testSet = new FlightConfigurableParameterSet<Parameter>();
		
		
	}
	

	
}
