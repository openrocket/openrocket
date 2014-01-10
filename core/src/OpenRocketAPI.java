import java.io.File;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.simulation.BasicEventSimulationEngine;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationEngine;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.simulation.exception.SimulationException;


public class OpenRocketAPI {
	
	private FlightData m_CFlightData;
	private SimulationConditions m_CSimulationConditions;
	
	//private SimulationStatus m_CStatus; //will be used later.
	
	public int LoadRocket(String szFileName) {
		try {
			File Filename = new File(szFileName);
			GeneralRocketLoader test = new GeneralRocketLoader(Filename);
			OpenRocketDocument temp = test.load();
			if (temp.getSimulationCount() > 0)
			{
				SimulationOptions temp2 = temp.getSimulation(0).getSimulatedConditions();
				if (temp2 != null)
				{
					m_CSimulationConditions = temp2.toSimulationConditions();
				}
			}
			//return loadorkfile(szFileName); //this needs to be more complex...
		} catch (RocketLoadException oops) {
			System.out.print("made a mistake file : ");
			System.out.print(szFileName);
			System.out.print(oops.toString());
			return 1;
			
		}
		return 0;
	}
	
	public void RunSimulation() {
		SimulationEngine boink = new BasicEventSimulationEngine();
		
		try {
			m_CFlightData = boink.simulate(m_CSimulationConditions);
		} catch (SimulationException e) {
			// TODO Auto-generated catch block
			System.out.println("oops RunSimulation threw an error");
		}
		
	};
	
	public double getMaxAltitude() {
		return m_CFlightData.getMaxAltitude();
	}
	
	public double getMaxVelocity() {
		return m_CFlightData.getMaxVelocity();
	}
	
	public double getMaxAcceleration() {
		return m_CFlightData.getTimeToApogee();
	}
	
	public double getMaxMachNumber() {
		return m_CFlightData.getMaxMachNumber();
	}
	
	public double getTimeToApogee() {
		return m_CFlightData.getTimeToApogee();
	}
	
	public double getFlightTime() {
		return m_CFlightData.getFlightTime();
	}
	
	public double getGroundHitVelocity() {
		return m_CFlightData.getGroundHitVelocity();
	}
	
	public double getLaunchRodVelocity() {
		return m_CFlightData.getLaunchRodVelocity();
	}
	
	public double getDeploymentVelocity() {
		return m_CFlightData.getDeploymentVelocity();
	}
	
	
	private int loadorkfile(String filename) {
		return 1;
	}
	
};