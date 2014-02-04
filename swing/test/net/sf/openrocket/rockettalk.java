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
import net.sf.openrocket.startup.OpenRocketAPI_0;
import net.sf.openrocket.simulation.*;

import org.javatuples.*;
/**
 * @author nubjub
 */
public class rockettalk extends OpenRocketAPI_0{
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
		    this.LoadRocket("/home/panman/desk/src/openrocket/swing/test/net/sf/openrocket/rockettalk.ork", 0);
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
		double timestep = this.GetTimeStep();
		int count = 1;
		while(this.IsSimulationLoopRunning()){
			while(this.IsSimulationLoopRunning()){
				++count;
				this.SimulationStep();
				FlightDataBranch rc_f = flightData();
				FlightDataBranch tmp = rc_f;
				int i = rc_f.getLength();
				int y = i;
			}
		}
	}
	private FlightDataBranch flightData() {
		return this.m_CStatus.getFlightData();
	}

}
