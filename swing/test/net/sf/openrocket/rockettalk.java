/**
 * 
 */
package net.sf.openrocket;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.startup.OpenRocketAPI;
import net.sf.openrocket.simulation.*;

/**
 * @author nubjub
 */
public class rockettalk extends OpenRocketAPI{
	SimulationStatus rc_simStatus;
	FlightDataBranch rc_flightData;
	ArrayList<FlightDataBranch> rc_fdl;
	double [][] sensor_matrix = {{1,0,0},{0,1,0},{0,0,1}};

	public rockettalk(){
		super();
	}
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {
		    System.out.println("Opening file");
		catch (Exception e){
			System.out.println("Failure to open file");
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		//this.RunSimulation();
		this.StartSimulation();
		double iteration =-1;
        double simTime = 0;
		while(this.IsSimulationLoopRunning()){
			while(this.IsSimulationLoopRunning()){
				this.SimulationStep();
				iteration = this.GetIteration();
				simTime = this.GetTime();
				
			double[] p = GetData();
			}
	        this.StagesStep();
			}
		}
	private double[] GetData(){
		int iteration = this.GetIteration();
		double[] p = new double[12];
		//Gyro
		p[0] = this.GetValue(FlightDataType.TYPE_PITCH_RATE);
		p[1] = this.GetValue(FlightDataType.TYPE_YAW_RATE);
		p[2] = this.GetValue(FlightDataType.TYPE_ROLL_RATE);
		
		double[] vec = new double[3];
		vec[0] = this.GetValue(FlightDataType.TYPE_ACCELERATION_LINEAR_X);
	    vec[1] = this.GetValue(FlightDataType.TYPE_ACCELERATION_LINEAR_Y);
	    double gravity = this.GetValue(FlightDataType.TYPE_GRAVITY);
	    vec[2] = this.GetValue(FlightDataType.TYPE_ACCELERATION_LINEAR_Z) + gravity;
	    
	    double[] nVec = new double[3];
	    p[4] = vec[0] * sensor_matrix[0][0] + vec[1] * sensor_matrix[0][1] + vec[2] * sensor_matrix[0][2];
	    p[5] = vec[0] * sensor_matrix[1][0] + vec[1] * sensor_matrix[1][1] + vec[2] * sensor_matrix[1][2];
	    p[6] = vec[0] * sensor_matrix[2][0] + vec[1] * sensor_matrix[2][1] + vec[2] * sensor_matrix[2][2];
	    
	    return p;
	}
	private FlightDataBranch flightData() {
		return this.GetFlightData();
	}
	private FlightDataStep flightDataStep() {
		return this.GetFlightDataStep();
	}

}
