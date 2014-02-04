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
import java.util.List;
import java.util.Vector;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.startup.OpenRocketAPI;
import net.sf.openrocket.simulation.*;

import org.javatuples.*;
/**
 * @author nubjub
 */
public class rockettalk extends OpenRocketAPI{
	SimulationStatus rc_simStatus;
	FlightDataBranch rc_flightData;
	ArrayList<FlightDataBranch> rc_fdl;

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
		    this.LoadRocket("/home/panman/desk/src/openrocket/swing/test/net/sf/openrocket/rockettalk_simple.ork", 0);
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
	public void test() {
		this.RunSimulation();
		this.StartSimulation();
		int iteration =-1;
		int count = -1;
		double timestep =-1;
		while(this.IsSimulationLoopRunning()){
			while(this.IsSimulationLoopRunning()){
				count = this.SimulationStep();
				iteration = this.GetIteration();
				timestep = this.GetTimeStep();
				FlightDataBranch rc_f = flightData();
				FlightDataStep rc_s = flightDataStep();
				rc_s.get(FlightDataType.TYPE_ACCELERATION_TOTAL);
				Coordinate c1 = this.m_CStatus.getRocketVelocity();
				Coordinate c2 = this.m_CStatus.getRocketPosition();
				int i = rc_f.getLength();
				int y = i;
			}
	        this.StagesStep();
		}
	}
	private FlightDataBranch flightData() {
		return this.GetFlightData();
	}
	private FlightDataStep flightDataStep() {
		return this.GetFlightDataStep();
	}

}
