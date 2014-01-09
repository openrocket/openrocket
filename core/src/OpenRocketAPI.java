import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationStatus;


public class OpenRocketAPI {
	
	private FlightData m_CFlightData;
	private SimulationConditions m_CSimulationConditions;
	private SimulationStatus m_CStatus;
	
	public int LoadRocket(String szFileName) {
		
		return loadorkfile(szFileName); //this needs to be more complex...
		
	}
	
	public void RunSimulation() {
		/*SimulationEngine boink = new BasicEventSimulationEngine; 
		
		try {
		m_CFlightData= boink.simulate(m_CSimulationConditions);
		} catch (SimulationException e) {
		// TODO Auto-generated catch block
		System.out.println("oops RunSimulation threw an error");
		}
		*/
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