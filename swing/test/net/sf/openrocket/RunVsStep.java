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
public class RunVsStep extends OpenRocketAPI{
	SimulationStatus rc_simStatus;
	FlightDataBranch rc_flightData;
	ArrayList<FlightDataBranch> rc_fdl;
	double [][] sensor_matrix = {{1,0,0},{0,1,0},{0,0,1}};

	public RunVsStep(){
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
		    this.LoadRocket("/home/panman/desk/src/openrocket/resources-psas/threeStageRocket.ork");
		    this.SetRandomSeed(1);
		}
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
	public void testStepper() {
		this.RunSimulation();
		this.StartSimulation();
		while(this.IsSimulationLoopRunning()){
			while(this.IsSimulationLoopRunning()){
				int rval = GetData();
			}
			this.StagesStep();
		}
		this.FlightDataStepToCSV("close");
	}
	private int GetData(){
		int iteration = this.SimulationStep();
		if(this.GetTimeStep() > 0){
			return this.FlightDataStepToCSV("/home/panman/desk/src/openrocket/resources-psas/stepper.csv");
		}
		return -1;
	}
	private FlightDataBranch flightData() {
		return this.GetFlightData();
	}
	private FlightDataStep flightDataStep() {
		return this.GetFlightDataStep();
	}

}
