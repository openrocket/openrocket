package net.sf.openrocket;

import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.exception.ReturnTypeException;
import net.sf.openrocket.startup.*;

public class testAPI {
	
	/**
	 * @param args
	 * @throws ReturnTypeException 
	 */
	public static void main(String[] args) throws ReturnTypeException {
		// TODO Auto-generated method stub
		System.out.println("got into a working test");
		OpenRocketAPI test = new OpenRocketAPI();
		int value=0;
		String []rocketfilename = {"threeStageRocket.ork","basicrocket1simulation.ork","blort.ork","foo.ork"};
		value=test.SimulationSetup("psas/"+rocketfilename[0],1);
		System.out.println("load sucsesful "+value);
	
		//test.RunSimulation();
		System.out.print("fligh time is ");
		System.out.print(test.getFlightTime());
		System.out.println("last acceleration is ");
		//System.out.print(test.getFlightDataSpecial(FlightDataType.TYPE_ACCELERATION_Z));
		System.out.println(" "+FlightDataType.TYPE_ROLL_RATE.getUnitGroup().toString());

		System.out.println("---------------------------------------------------");
		test.SimulationStep(1);
	    while(test.SimulationIsRunning()){
	    	System.out.println("simulationstep return value is: "+value);
	    	System.out.print("time is: ");

	    	System.out.println(test.Getsimulationrunningtime());
	    	System.out.println("rotatinal position x is "+test.GetValue(FlightDataType.TYPE_POSITION_X));
	    	System.out.println("rotatinal position y is "+test.GetValue(FlightDataType.TYPE_POSITION_Y));
	    	System.out.println("rotatinal position z is "+test.GetValue(FlightDataType.TYPE_ALTITUDE));
	    	System.out.println("PHI quotient is "+test.GetValue(FlightDataType.TYPE_ORIENTATION_PHI));
	    	System.out.println("THETA quotient is "+test.GetValue(FlightDataType.TYPE_ORIENTATION_THETA));
	    	//test.RunSimulation();
	    		
	    	/*try{
	    		System.in.read();
	    		}catch(IOException a)
	    		{
	    			System.out.println("error in read");	    		
	    		}*/

	    		value=test.SimulationStep(1);
	    }
	    System.out.println("yeah it works!!!!!!!!!!");
	}
	
}
