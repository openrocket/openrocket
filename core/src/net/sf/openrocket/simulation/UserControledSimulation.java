package net.sf.openrocket.simulation;

import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.exception.MotorIgnitionException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.simulation.listeners.SimulationListenerHelper;

public class UserControledSimulation extends BasicEventSimulationEngine {
	
	public Configuration configuration = null;
	public boolean m_CSimulationRunning = false;
	
	public UserControledSimulation() {
		// TODO Auto-generated constructor stub
		
	}
	
	//will not handel multiple stages in a rocket
	//
	public SimulationStatus StartSimulation(SimulationConditions sim, SimulationStatus Status, FlightData flight) throws SimulationException {
		if (sim == null)
			return null;
		
		// Set up flight data
		flight = new FlightData();
		
		// Set up rocket configuration
		configuration = setupConfiguration(sim);
		flightConfigurationId = configuration.getFlightConfigurationID();
		MotorInstanceConfiguration motorConfiguration = setupMotorConfiguration(configuration);
		if (motorConfiguration.getMotorIDs().isEmpty()) {
			throw new MotorIgnitionException(trans.get("BasicEventSimulationEngine.error.noMotorsDefined"));
		}
		
		status = new SimulationStatus(configuration, motorConfiguration, sim);
		status.getEventQueue().add(new FlightEvent(FlightEvent.Type.LAUNCH, 0, sim.getRocket()));
		{
			// main sustainer stage
			RocketComponent sustainer = configuration.getRocket().getChild(0);
			status.setFlightData(new FlightDataBranch(sustainer.getName(), FlightDataType.TYPE_TIME));
		}
		stages.add(status);
		
		SimulationListenerHelper.fireStartSimulation(status);
		
		return new SimulationStatus(status);
	}
	
	/* 
	 *  0 everythings fine
	 * -1 sim null
	 * -2 flightdata null
	 * -3 timestep not reasonable range
	 * -4 simulation finished
	 * */
	
	public int StepSimulation(FlightData flightData, SimulationStatus Status, double timestep) {
		if (flightData == null)
			return -2;
		if (timestep < .005 || timestep > 5)
			return -3;
		if (stages.size() == 0) {
			EndSimulation(flightData);
		}
		
		status = new SimulationStatus(Status);
		
		SimulationStatus stageStatus = stages.pop();
		if (stageStatus == null) {
			EndSimulation(flightData);
		}
		status = stageStatus;
		FlightDataBranch dataBranch = null;
		//dataBranch=simulateLoop();
		
		
		flightData.addBranch(dataBranch);
		warnmeifidosomthingwrong(flightData);
		
		return 0;
	}
	
	
	public int EndSimulation(FlightData flightData) {
		SimulationListenerHelper.fireEndSimulation(status, null);
		
		configuration.release();
		
		warnmeifidosomthingwrong(flightData);
		
		return -4;
	}
	
	protected int warnmeifidosomthingwrong(FlightData flightData) {
		flightData.getWarningSet().addAll(status.getWarnings());
		if (!flightData.getWarningSet().isEmpty()) {
			log.info("Warnings during simulation:  " + flightData.getWarningSet());
		}
		return 0;
		
	}
	
};
