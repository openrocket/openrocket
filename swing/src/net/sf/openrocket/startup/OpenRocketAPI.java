package net.sf.openrocket.startup;

import java.io.File;

import net.sf.openrocket.simulation.*;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.simulation.BasicEventSimulationEngine;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationEngine;
import net.sf.openrocket.simulation.SimulationOptions;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.GuiModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


public class OpenRocketAPI {
	
	private boolean m_bIsSimulationStagesRunning=false;
	private boolean m_bIsSimulationLoopRunning=false;
	private FlightData m_CFlightData = null;
	private SimulationConditions m_CSimulationConditions = null;
	private SimulationStatus m_CStatus;
	private UserControledSimulation m_CRocket=null;
	

	public double GetVelocityX() {
		if(m_CStatus==null)
			return -1;
		if(m_CStatus.getRocketVelocity()==null)
			return -2;
		return m_CStatus.getRocketVelocity().x;
	}
	
	public double GetVelocityZ() {
		if(m_CStatus==null)
			return -1;
		if(m_CStatus.getRocketVelocity()==null)
			return -2;
		return m_CStatus.getRocketVelocity().z;
	}
	
	public double GetVelocityY() {
		if(m_CStatus==null)
			return -1;
		if(m_CStatus.getRocketVelocity()==null)
			return -2;
		return m_CStatus.getRocketVelocity().y;
	}
	
	
	public boolean IsSimulationStagesRunning(){return m_bIsSimulationStagesRunning;}
	
	public boolean IsSimulationLoopRunning(){return m_bIsSimulationLoopRunning;}
	
	
	public int StartSimulation(){
		m_CRocket=new UserControledSimulation();
		try{
		m_CStatus=m_CRocket.firstInitialize(m_CSimulationConditions,m_CStatus, m_CFlightData);
		if(m_CStatus==null)
			{System.out.println("temp is not valid");
			return -1;
			}
		m_CFlightData=new FlightData();
		
		m_bIsSimulationLoopRunning=true;
		m_bIsSimulationStagesRunning=true;
		}
		catch(SimulationException e)
		{System.out.println(e);}
		return 0;
	}
	
	public double GetTimeStep()
	{
		return m_CSimulationConditions.getTimeStep();
	}
	
	public int SimulationStep(){
		if(m_CSimulationConditions==null)
			return -1;
		return SimulationStep((int) m_CSimulationConditions.getTimeStep() );
		}
	
	public int SimulationStep(double timestep){
		int temp=0;
		if(m_bIsSimulationLoopRunning!=true)
			{System.out.println("not running");
			return -1;}
		if(m_CRocket==null)
			{System.out.println("Rocket is null");
			return -2;}
		m_CStatus=m_CRocket.step(m_CStatus,m_CFlightData, timestep);
		
		if(m_CStatus==null)
			{m_bIsSimulationLoopRunning=false;
			temp=-3;
			}
		return temp;
		}
	
	public int StagesStep(){
		if(m_CRocket==null)
			return -1;
		
		m_CStatus=m_CRocket.stagestep(m_CFlightData, m_CStatus);
		if(m_CStatus==null)
			m_bIsSimulationStagesRunning=false;
			
		return 0;
	}
	
	public int LoadRocket(String szFileName){
		
		return LoadRocket(szFileName,0);
	}
	
	public int LoadRocket(String szFileName,int simtograb) {
		try {
			
			GuiModule guiModule = new GuiModule();
			Module pluginModule = new PluginModule();
			Injector injector = Guice.createInjector(guiModule, pluginModule);
			Application.setInjector(injector);
			
			guiModule.startLoader();//might just do some initializing
			
			File Filename = new File(szFileName);
			System.out.println(szFileName);
			
			GeneralRocketLoader test = new GeneralRocketLoader(Filename);
			OpenRocketDocument temp = test.load();
			
			System.out.print("Number of Simulations in file: ");
			System.out.println(temp.getSimulationCount());
			if (!(temp.getSimulationCount() < simtograb))
			{
				SimulationOptions temp2 = temp.getSimulation(simtograb).getSimulatedConditions();
				if (temp2 != null)
				{
					System.out.print("Getting Simulation Conditions for: ");
					System.out.println(temp.getSimulation(simtograb).getName());
					m_CSimulationConditions = temp2.toSimulationConditions();
				}
				else{
					System.out.println("simulation is null");
				}
			}else{
				System.out.println("no simulations found");
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
		if(m_CSimulationConditions == null)
			{System.out.println("no simulation data");
			return;}
		SimulationEngine boink = new BasicEventSimulationEngine();
		
		try {
			m_CFlightData = boink.simulate(m_CSimulationConditions);
		} catch (SimulationException e) {
			// TODO Auto-generated catch block
			System.out.println("oops RunSimulation threw an error");
		}
		
	};
	
	public double getMaxAltitude() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getMaxAltitude();
	}
	
	public double getMaxVelocity() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getMaxVelocity();
	}
	
	public double getMaxAcceleration() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getTimeToApogee();
	}
	
	public double getMaxMachNumber() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getMaxMachNumber();
	}
	
	public double getTimeToApogee() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getTimeToApogee();
	}
	
	public double getFlightTime() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getFlightTime();
	}
	
	public double getGroundHitVelocity() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getGroundHitVelocity();
	}
	
	public double getLaunchRodVelocity() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getLaunchRodVelocity();
	}
	
	public double getDeploymentVelocity() {
		if (m_CFlightData == null)
			return -1;
		return m_CFlightData.getDeploymentVelocity();
	}
	
	
	private int loadorkfile(String filename) {
		return 1;
	}
	
};
