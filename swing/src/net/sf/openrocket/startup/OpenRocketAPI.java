package net.sf.openrocket.startup;

import java.io.File;
import java.util.List;

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
import net.sf.openrocket.startup.APIGuiModule;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Quaternion;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


public class OpenRocketAPI {
	
	private boolean m_bIsSimulationStagesRunning=false;
	private boolean m_bIsSimulationLoopRunning=false;
	private FlightData m_CFlightMaximums = null;
	private FlightDataBranch m_CFlightData = null;
	private SimulationConditions m_CSimulationConditions = null;
	protected RK4SimulationStatus m_CStatus;
	private UserControledSimulation m_CRocket=null;
	
	
	
	public int setlogfile(String filename){		
		return 0;
	}
	
	public OpenRocketAPI(){
		
		APIGuiModule guiModule = new APIGuiModule();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		Application.setInjector(injector);
		
		guiModule.startLoader();//might just do some initializing
		
	}

	/****************************************************
	 * flight data functions
	 *****************************************************/

	/**
	 * Returns the current iteration of the simulation
	 * 
	 * @return
	 */
	public int GetItteration(){
		return m_CFlightData.getLength();
	}
	/**
	 * Returns the time step for the current iteration of the simulation.
	 * 
	 * @return		end time of current time step - end time of previous time step.
	 */
	public double GetTimeStep()
	{
		List<Double> ts = m_CFlightData.get(FlightDataType.TYPE_TIME_STEP);
		double t_1 = ts.get(ts.size()-1);
		double t_0 = ts.get(ts.size()-2);
		return t_1 - t_0;
	}
	/**
	 * Returns the entire class containing all of the simulation data.
	 * 
	 * @return		FlightDataBranch type.
	 */
	public FlightDataBranch GetFlightData(){
		return m_CFlightData;
	}
	
	/**
	 * Returns the values for the current iteration of the simulation
	 * 
	 * @return		FlightDataStep type.
	 */
	public FlightDataStep GetFlightDataStep(){
		return null;
	}
	
	/**
	 * Returns the values for the specified iteration of the simulation
	 * 
	 * @return		FlightDataStep type.
	 */
	public FlightDataStep GetFlightDataStep(int i){
		return null;
	}
	/******************************************************************
	 * rocket simulation functions
	 * **************************************************************/
	
	public boolean IsSimulationStagesRunning(){return m_bIsSimulationStagesRunning;}
	
	public boolean IsSimulationLoopRunning(){return m_bIsSimulationLoopRunning;}
	
	public int StartSimulation(){
		m_CRocket=new UserControledSimulation();
		FlightData fm_temp = new FlightData();
		try{
		m_CStatus=m_CRocket.firstInitialize(m_CSimulationConditions,m_CStatus, fm_temp);
		if(m_CStatus==null)
			{System.err.println("simulation is not valid");
			return -1;
			}
		m_CFlightMaximums = fm_temp;
		m_CFlightData = m_CStatus.getFlightData();
		m_bIsSimulationLoopRunning=true;
		m_bIsSimulationStagesRunning=true;
		}
		catch(SimulationException e)
		{System.out.println(e);}
		return 0;
	}

	public int SimulationStep(){
		if(m_CSimulationConditions==null)
			return -1;
		return SimulationStep((int) m_CSimulationConditions.getTimeStep() );
		}
	
	public int SimulationStep(double timestep){
		int temp=0;
		if(m_bIsSimulationLoopRunning!=true)
			{System.err.println("not running");
			return -1;}
		if(m_CRocket==null)
			{System.err.println("Rocket is null");
			return -2;}
		if(m_CStatus==null)
		{System.err.println("simualtion is null");
		return -2;}
		
		m_CStatus.setPreviousTimeStep(timestep);
		m_CStatus=m_CRocket.step(m_CStatus,m_CFlightMaximums);
		
		if(m_CStatus==null)
			{m_bIsSimulationLoopRunning=false;
			temp=-3;
			}
		return temp;
		}
	
	public int StagesStep(){
		if(m_CRocket==null)
			return -1;
		
		m_CStatus=m_CRocket.stagestep(m_CFlightMaximums, m_CStatus);
		if(m_CStatus==null)
			m_bIsSimulationStagesRunning=false;
			
		return 0;
	}
	
	public int LoadRocket(String szFileName){
		
		return LoadRocket(szFileName,0);
	}
	
	public int LoadRocket(String szFileName,int simtograb) {
		try {
			File Filename = new File(szFileName);
			System.out.println("loading rocket from "+szFileName);
			
			GeneralRocketLoader test = new GeneralRocketLoader(Filename);
			OpenRocketDocument temp = test.load();
			
			int simCount = temp.getSimulationCount();
			System.out.print("Number of Simulations in file: ");
			System.out.println(simCount);
			if (simCount == 0)
			{
				return -1;
			}
			if (!(simCount < simtograb))
			{
				SimulationOptions temp2 = temp.getSimulation(simtograb).getSimulatedConditions();
				if (temp2 != null)
				{
					System.out.print("Getting Simulation Conditions for: ");
					System.out.println(temp.getSimulation(simtograb).getName());
					m_CSimulationConditions = temp2.toSimulationConditions();
					temp2.toSimulationConditions();
				}
				else{
					System.err.println("simulation is null");
				}
			}else{
				System.out.println("no simulations found");
				return -2;
			}
			//return loadorkfile(szFileName); //this needs to be more complex...
		} catch (RocketLoadException oops) {
			System.err.print("made a mistake file : ");
			System.err.print(szFileName);
			System.err.print(oops.toString());
			return 1;
			
		}
		return 0;
	}
	
	public void RunSimulation() {
		if(m_bIsSimulationStagesRunning==true){
			System.err.println("warning calling RunSimulation while StartSimulation is running may Invalidate StartSimulations FlightData");
			return;}
		if(m_CSimulationConditions == null)
			{System.err.println("no simulation data");
			return;}
		SimulationEngine boink = new BasicEventSimulationEngine();
		
		try {
			m_CFlightMaximums = boink.simulate(m_CSimulationConditions);
		} catch (SimulationException e) {
			System.err.println("oops RunSimulation threw an error");
		}
		
	};

	/****************************************************
	 * flight data maximums (RK4) functions
	 *****************************************************/
	
	public double getMaxAltitude() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getMaxAltitude();
	}
	public double getMaxVelocity() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getMaxVelocity();
	}
	public double getMaxAcceleration() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getTimeToApogee();
	}
	public double getMaxMachNumber() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getMaxMachNumber();
	}
	public double getTimeToApogee() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getTimeToApogee();
	}
	public double getFlightTime() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getFlightTime();
	}
	public double getGroundHitVelocity() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getGroundHitVelocity();
	}
	public double getLaunchRodVelocity() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getLaunchRodVelocity();
	}
	public double getDeploymentVelocity() {
		if (m_CFlightMaximums == null)
			return -1;
		return m_CFlightMaximums.getDeploymentVelocity();
	}
	
	/**********************************************************************
	 * seters and getters for simulation data
	 ********************************************************************* */
		

		public double GetAccelerationX() {
			if(m_CStatus==null)
				return -1;
			return m_CStatus.getRocketLinearAcceleration();
		}
		
		public double GetVelocityX() {
			if(m_CStatus==null)
				return -1;
			if(m_CStatus.getRocketVelocity()==null)
				return -2;
			return m_CStatus.getRocketVelocity().x;
		}
		
		public double setOrientationXYZ(double W,double X,double Y,double Z){
			if(m_CStatus==null)
				return -1;
			Quaternion xyzOrientation=new Quaternion(W,X,Y,Z);
			if(xyzOrientation!=null)
				{
				m_CStatus.setRocketOrientationQuaternion(xyzOrientation);
				}
			return 0;
		}
		
		public double getOrientationX(){
			if(m_CStatus==null)
				return -1;
			Quaternion x=m_CStatus.getRocketOrientationQuaternion();
			if(x==null)
				return -2;
			return x.getX();
			
		}
		public double getOrientationY(){
			if(m_CStatus==null)
				return -1;
			Quaternion x=m_CStatus.getRocketOrientationQuaternion();
			if(x==null)
				return -2;
			return x.getY();
		}
		public double getOrientationZ(){
			if(m_CStatus==null)
				return -1;
			Quaternion x=m_CStatus.getRocketOrientationQuaternion();
			if(x==null)
				return -2;
			return x.getZ();
		}
		public double getOrientationW(){
			if(m_CStatus==null)
				return -1;
			Quaternion x=m_CStatus.getRocketOrientationQuaternion();
			if(x==null)
				return -2;
			return x.getW();
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

		public double GetCordinateX() {

			if(m_CStatus==null)
				return -1;
			Coordinate x=m_CStatus.getRocketPosition();
			if(x==null){
				return -2;
			}
			return x.x;
		}
		public double GetCordinateY() {

			if(m_CStatus==null)
				return -1;
			Coordinate x=m_CStatus.getRocketPosition();
			if(x==null){
				return -2;
			}
			return x.y;
		}	
		public double GetCordinateZ() {

			if(m_CStatus==null)
				return -1;
			Coordinate x=m_CStatus.getRocketPosition();
			if(x==null){
				return -2;
			}
			return x.z;
		}
		
		public double GetVelocityRotationX() {

			if(m_CStatus==null)
				return -1;
			Coordinate x=m_CStatus.getRocketRotationVelocity();
			if(x==null){
				return -2;
			}
			return x.x;
		}
		public double GetVelocityRotationY() {

			if(m_CStatus==null)
				return -1;
			Coordinate x=m_CStatus.getRocketRotationVelocity();
			if(x==null){
				return -2;
			}
			return x.y;
		}
		public double GetVelocityRotationZ() {

			if(m_CStatus==null)
				return -1;
			Coordinate x=m_CStatus.getRocketRotationVelocity();
			if(x==null){
				return -2;
			}
			return x.z;
		}
		
		public double GetsimulationrunningtimeX() {

			if(m_CStatus==null)
				return -1;
			return m_CStatus.getSimulationTime();
			}
		
		public boolean GetBoolTumbling() {
			if(m_CStatus==null)
				return false;
			return m_CStatus.isTumbling();
		}
		public boolean GetBoolMotorIgnited() {
			if(m_CStatus==null)
				return false;
			return m_CStatus.isMotorIgnited();
					}
		public boolean GetBoolApogeeReached() {
			if(m_CStatus==null)
				return false;
			return m_CStatus.isApogeeReached();
		}
		public boolean GetBoolLaunchRodCleared() {
			if(m_CStatus==null)
				return false;
			return m_CStatus.isLaunchRodCleared();
		}
		public boolean GetBoolLiftoff() {
			if(m_CStatus==null)
				return false;
			return m_CStatus.isLiftoff();
		}
};
