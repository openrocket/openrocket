package net.sf.openrocket.startup;

import java.io.File;
import java.util.List;

import net.sf.openrocket.simulation.*;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.simulation.BasicEventSimulationEngine;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SimulationEngine;
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
	private FlightData m_CFlightData = null;
	private FlightDataBranch m_CFlightDataBranch = null;
	private SimulationConditions m_CSimulationConditions = null;
	protected RK4SimulationStatus m_CStatus;
	private UserControledSimulation m_CRocket=null;
	//TODO: Make this a parameter that can be specified
	private double timeStep = 1.0; //There is a value in the ork file that is added to this.	
	
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
	public int GetIteration(){
		return m_CFlightDataBranch.getLength();
	}
	/**
	 * Returns one value correlating to the key type and the
	 * current iteration.
	 *
	 * @param		Specify this type of data to return
	 * @return		double
	 */
	public double GetValue(FlightDataType type) {
		List<Double> tsl = m_CFlightDataBranch.get(type);
		if(tsl == null) return -2;
		int tsl_size = tsl.size();
		if(tsl_size < 1) return -1;
		return tsl.get(tsl_size -1);
	}
	/**
	 * Returns the time step for the current iteration of the simulation.
	 * 
	 * @return		end time of current time step - end time of previous time step.
	 */
	public double GetTimeStep()	{
		double i = this.GetValue(FlightDataType.TYPE_TIME_STEP);
		if(i < 0){
			return -1;
		}
		return i;
	}
	/**
	 * Returns the total time as of the current step of the simulation
	 * @return
	 */
	public double GetTime(){
		double i = this.GetValue(FlightDataType.TYPE_TIME_STEP);
		if(i == -2){
			return -1;
		}
		if(i == -1){
			return 0;
		}
		return i;
	}
	/**
	 * Returns one value correlating to the key type and the
	 * specified iteration.
	 * 
	 * @param		Specify this type of data to return
	 * @return		double
	 */
	public double GetValue(FlightDataType type, int step) {
		FlightDataStep currentStep = GetFlightDataStep(step);
		return currentStep.get(type);
	}
	
	public void SetValue(FlightDataType type, double value) {
		if(m_CFlightDataBranch == null) {
			System.out.println("ERROR NULL");
			return;
		}
		
		m_CFlightDataBranch.setValue(type, value);
	}
	
	/**
	 * Returns the values for the current iteration of the simulation
	 * 
	 * @return		FlightDataStep type.
	 */
	public FlightDataStep GetFlightDataStep(){
		return new FlightDataStep(m_CFlightDataBranch);
	}
	
	/**
	 * Returns the values for the specified iteration of the simulation
	 * 
	 * @param		Desired iteration (the first iteration is iteration 1)
	 * @return		FlightDataStep type.
	 */
	public FlightDataStep GetFlightDataStep(int i){
		return new FlightDataStep(m_CFlightDataBranch, i);
	}
	
	/**
	 * Returns the entire class containing all of the simulation data.
	 * This is a shallow copy, probably not the best idea to be public.
	 * 
	 * @return		FlightDataBranch type.
	 */
	protected FlightDataBranch GetFlightData(){
		FlightDataBranch fdb_temp = null;
		try {
			fdb_temp = m_CStatus.getFlightData();
			if (fdb_temp == null) {
				throw new IllegalStateException("fdb_temp == null");
			}
		} catch (Throwable t) {
			System.err
					.println("OpenRocketAPI.GetFlightData() threw a m_CStatus related exception"+ t);
			//throw new IllegalStateException(t);
			fdb_temp= new FlightDataBranch("empty", FlightDataType.TYPE_TIME);
		}
		return fdb_temp;
	}
	/******************************************************************
	 * rocket simulation functions
	 * **************************************************************/
	
	public boolean IsSimulationStagesRunning(){return m_bIsSimulationStagesRunning;}
	
	public boolean IsSimulationLoopRunning(){return m_bIsSimulationLoopRunning;}
	
	public int StartSimulation(){
		return StartSimulation(new FlightDataBranch("psas",FlightDataType.TYPE_TIME));
		}
	
	public int StartSimulation(FlightDataBranch CBranch){
		if(m_CSimulationConditions==null)
			return -2;
		m_CRocket=new UserControledSimulation();
		FlightData fm_temp=new FlightData();
		if(CBranch!=null){
			fm_temp.addBranch(CBranch);
			m_CSimulationConditions.setCalculateExtras(true);}
		try{
		m_CStatus=m_CRocket.firstInitialize(m_CSimulationConditions,m_CStatus, fm_temp);
		if(m_CStatus==null)
			{System.err.println("simulation is not valid");
			return -1;
			}
		m_CFlightData = fm_temp;
		//TODO: Flight data class can potentially have more then one Branch.
		m_CFlightDataBranch = m_CStatus.getFlightData();
		m_bIsSimulationLoopRunning=true;
		m_bIsSimulationStagesRunning=true;
		}
		catch(SimulationException e)
		{System.out.println(e);}
		return 0;
	}

	public int SimulationStep(){
		return SimulationStep( this.timeStep );
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
		
		m_CStatus.setPreviousTimeStep(.5);
		m_CStatus=m_CRocket.step(m_CStatus,m_CFlightData);
		
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
		
		return LoadRocket(szFileName,1);
	}
	
	/*
	 * loads a rocket and simulationconditions from an ork file
	 * 
	 * 0 everything went fine;
	 * -1 simcount==0 //no simulations in file 
	 * -2 !(simcount < simtograb) //you asked for a simulation not present
	 * -3 simulation data not present in simulation
	 * -4 exception thrown
	 * */
	
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
			{simtograb--;
				Simulation temp2 = temp.getSimulation(simtograb);
				if (temp2 != null)
				{
					System.out.print("Getting Simulation Conditions for: ");
					System.out.println(temp.getSimulation(simtograb).getName());
					System.out.println("status of rocket is "+temp2.getStatus());
					m_CSimulationConditions = temp2.getOptions().toSimulationConditions();
					
				}
				else{
					//System.err.println("simulation is null");
					return -3;
				}
			}else{
				//System.err.println("assked for simulation not present");
				return -2;
			}
			//TODO: return loadorkfile(szFileName); //this needs to be more complex...
		} catch (RocketLoadException oops) {
			System.err.print("made a mistake file : ");
			System.err.println(szFileName+" "+oops.toString());
			return -4;
			
		}
		return 0;
	}

	/*
	 * runs simulation start to finish just like openrocket main.
	 * */
	
	public int RunSimulation() {
		if(m_bIsSimulationStagesRunning==true){
			System.err.println("error calling RunSimulation while StartSimulation is running may Invalidate StartSimulations FlightData");
			return-1 ;}
		if(m_CSimulationConditions == null)
			{System.err.println("no simulation data");
			return -2;}
		m_CSimulationConditions.setCalculateExtras(true);
		SimulationEngine boink = new BasicEventSimulationEngine();
		
		try {
			m_CFlightData = boink.simulate(m_CSimulationConditions);
			System.out.print("Number of branches in simulation: ");
			System.out.println(m_CFlightData.getBranchCount());
		} catch (SimulationException e) {
			System.err.println("oops RunSimulation threw an error");
			return -3;
		}
		return 0;
	}

	/****************************************************
	 * flight data maximums (RK4) functions
	 *****************************************************/
	
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
	
	/**********************************************************************
	 * seters and getters for simulation data
	 ********************************************************************* */
	//TODO: These should all be remade to use m_CFlightDataBranch and 	

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
		//TODO: These Orientations do not change during flight.
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
		
		public double Getsimulationrunningtime() {

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
