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

import net.sf.openrocket.utils.csv.*;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


public class OpenRocketAPI {

	private BasicEventSimulationEngine basicEngine = null;
	private SteppingEventSimulationEngine steppingEngine = null;
	
	//private boolean m_bIsSimulationStagesRunning = false;
	//private boolean m_bIsSimulationLoopRunning = false;
	protected FlightData m_CFlightData = null;
	//private FlightDataBranch m_CFlightDataBranch = null; //use GetFlightData()
	private SimulationConditions m_CSimulationConditions = null;
	protected RK4SimulationStatus m_CStatus;
	private CSVWriter CSVOutputFile = null; 
	//TODO: Not fully Implemented
	private double timeStep = 0.0;
	private int m_rand_seed = 0;
	//TODO: What should this failure value be??
	private double ERROR = -1.79769313486e+308;
	
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
		return GetFlightData().getLength();
	}
	/**
	 * Returns the time step for the current iteration of the simulation.
	 * 
	 * @return  double  End time of current time step - end time of previous time step.
	 */
	public double GetTimeStep(){
		double i = this.GetValue(FlightDataType.TYPE_TIME_STEP);
		if(!Double.isNaN(i))
			return i;
		return ERROR;
	}
	/**
	 * Returns the total time as of the end current step of the simulation
	 * @return  double  Total time of the current simulation
	 */
	public double GetTime(){
		FlightDataStep fds = GetFlightDataStep();
		double i = fds.get(FlightDataType.TYPE_TIME);
		double j = fds.get(FlightDataType.TYPE_TIME_STEP);
		if(!(Double.isNaN(i) || Double.isNaN(j)))
			return i + j;
		return 0;
	}
	/**
	 * Returns one value correlating to the key type and the
	 * current iteration.
	 *
	 * @param   FlightDataType  The data type to return
	 * @return  double          Value of the specified type
	 */
	public double GetValue(FlightDataType type){
		return GetValue(type, -1);
	}
	/**
	 * Returns one value correlating to the key type and the
	 * specified iteration.
	 * 
	 * @param   FlightDataType  This type of data to return
	 * @param   int             Step to obtain value from
	 * @return  double          Value requested
	 */
	public double GetValue(FlightDataType type, int step) {
		FlightDataStep fds = null;
		if(step < 0){
			fds = GetFlightDataStep();
		}
		else {
			//TODO: Untested
			fds = GetFlightDataStep(step);
		}
		double tsl = fds.get(type);
		if(!(Double.isNaN(tsl)))
			return tsl;
		return ERROR;
	}
	/**
	 * 
	 * @param   FlightDataType  Type of data to get the max of
	 * @return  double          Value requested
	 */
	public double GetMaximum(FlightDataType type){
		double i = GetFlightData().getMaximum(type);
		if(!Double.isNaN(i))
			return i;
		return ERROR;
	}
	/**
	 * 
	 * @param   FlightDataType  Type of data to get the min of
	 * @return  double          Value requested
	 */
	public double GetMinimum(FlightDataType type){
		double i = GetFlightData().getMinimum(type);
		if(!Double.isNaN(i))
			return i;
		return ERROR;
	}
	
	//TODO: Definately not implemented correctly
	public void SetValue(FlightDataType type, double value) {
		if(GetFlightData() == null) {
			System.out.println("ERROR NULL");
			return;
		}
		GetFlightData().setValue(type, value);
	}
	/**
	 * Writes the values for the specified iteration to the specified file
	 * 
	 * @param  string  filename to write to
	 */
	public int FlightDataStepToCSV(String filename){
		FlightDataStep fds_temp = GetFlightDataStep();
		return CSVWriter(filename,fds_temp);
	}
	/**
	 * Writes the values for the specified iteration to the specified file
	 * 
	 * @param  string  filename to write to
	 *                 "close" closes the csv file.
	 * @param  int     Desired iteration (the first iteration is iteration 1)
	 */
	public int FlightDataStepToCSV(String filename, int iteration){
		FlightDataStep fds_temp = GetFlightDataStep(iteration);
		return CSVWriter(filename,fds_temp);
	}
	/**
	 * Writes the values for the specified iteration to the specified file
	 * 
	 * @param  string  filename to write to
	 *                 "close" closes the csv file.
	 * @param  FlightDataBranch  FlightDataBranch to get data from
	 * @param  int     Desired iteration (the first iteration is iteration 1)
	 */
	public int FlightDataStepToCSV(String filename, FlightDataBranch branch, int iteration){
		FlightDataStep fds_temp = GetFlightDataStep(branch, iteration);
		return CSVWriter(filename,fds_temp);
	}
	private int CSVWriter(String file,FlightDataStep fds){
		if(CSVOutputFile != null){
			if(file.equals("close")){
				CSVOutputFile.close();
			}
			else if(!(CSVOutputFile.nameEquals(file))){
				CSVOutputFile.close();
				CSVOutputFile = new CSVWriter(file);
				CSVFile csvLines = CSVCreateLine(fds,true);
				CSVOutputFile.writeFile(csvLines);
			}
			else{
				CSVFile csvLines = CSVCreateLine(fds,false);
				CSVOutputFile.writeFile(csvLines);
			}
		}
		else{
			CSVOutputFile = new CSVWriter(file);
			CSVFile csvLines = CSVCreateLine(fds,true);
			CSVOutputFile.writeFile(csvLines);
		}
		return 0;
	}
	private CSVFile CSVCreateLine(FlightDataStep fds, boolean writeKeys){
		CSVFile csvLineS = new CSVFile();
		CSVLine csvLine = null;
		if(writeKeys){
			csvLine = csvLineS.newLine();
			csvLine.add("Branch Name");
			csvLine.add("Iteration");
			for (FlightDataType t : FlightDataType.ALL_TYPES) {
				csvLine.add(t.getName());
			}
		}
		csvLine = csvLineS.newLine();
		csvLine.add(fds.getBranchName());
		csvLine.add(fds.getIteration());
		for (FlightDataType t : FlightDataType.ALL_TYPES) {
			Double v = fds.get(t);
			if (!(v.equals(Double.NaN))) {
				csvLine.add(v);
			}
			else{
				csvLine.add(null);
			}
		}
		return csvLineS;
	}
	/**
	 * Returns the values for the current iteration of the simulation
	 * 
	 * @return  FlightDataStep
	 */
	public FlightDataStep GetFlightDataStep(){
		return new FlightDataStep(GetFlightData());
	}
	/**
	 * Returns the values for the specified iteration of the simulation
	 * 
	 * @param   int  Desired iteration (the first iteration is iteration 1)
	 * @return  FlightDataStep
	 */
	public FlightDataStep GetFlightDataStep(int i){
		return new FlightDataStep(GetFlightData(), i);
	}
	/**
	 * Returns the values for the specified iteration of the simulation
	 * 
	 * @param   FlightDataBranch  FlightDataBranch to get data from
	 * @param   int  Desired iteration (the first iteration is iteration 1)
	 * @return  FlightDataStep
	 */
	public FlightDataStep GetFlightDataStep(FlightDataBranch b, int i){
		return new FlightDataStep(b, i);
	}
	/**
	 * Returns the entire class containing all of the simulation data. The
	 * intention is to have this function be responsible for providing the
	 * correct FlightDataBranch, and should be called rather then using
	 * m_CFlightDataBranch directly. This is a shallow copy, probably not the
	 * best idea to be public.
	 * 
	 * @return  FlightDataBranch
	 */
	protected FlightDataBranch GetFlightData(){
		FlightDataBranch fdb_temp = null;
		try {
			fdb_temp = steppingEngine.getFlightData();
			if (fdb_temp == null) {
				throw new IllegalStateException("fdb_temp == null");
		}
		} catch (Throwable t) {
			System.err.println("OpenRocketAPI.GetFlightData() threw a m_CStatus related exception"+ t);
			System.err.println("OpenRocketAPI.GetFlightData() perhaps the simulation isn't running");
			fdb_temp= new FlightDataBranch("empty", FlightDataType.TYPE_TIME);
		}
		return fdb_temp;
	}
	/******************************************************************
	 * rocket simulation functions
	 * **************************************************************/
	/**
	 * Returns weather a simulation is currently running.
	 *  @return  boolean
	 */
	public boolean IsSimulationRunning(){
		if(steppingEngine != null){
			return steppingEngine.simulationRunning();
		}
		return false;
	}
	/**
	 * @return  boolean  IsSimulationRunning()
	 */
	public boolean IsSimulationLoopRunning(){return IsSimulationRunning();}

	/**
	 * The random seed can be set here making OpenRocket determinisitic.
	 * @param  int  random seed to use in simulation  
	 */
	public void SetRandomSeed(int rand_seed){
		m_rand_seed = rand_seed;
	}
	/**
	 * Sets the user-specified time step
	 * OpenRocket uses the minimum of the following
	 * the user-specified time step (or 1/5th of it if still on the launch rod)
	 * maxTimeStep
	 * maximum pitch step angle limit
	 * maximum roll step angle limit
	 * maximum roll rate change limit
	 * maximum pitch change limit
	 * 1/10th of the launch rod length if still on the launch rod
	 * 1.50 times the previous time step
	 * 
	 * @param  double  timeStep
	 */
	public void SetTimeStep(double timeStep){
		//TODO: This may be the place to provide feedback about invalid timestep
		this.timeStep = timeStep;
	}
	/**
	 * loads a rocket and simulationconditions from an ork file
	 * 
	 * */
	private OpenRocketDocument LoadRocket(String szFileName) {
		OpenRocketDocument Rocket = null;
		try {
			File Filename = new File(szFileName);
			System.out.println("loading rocket from "+szFileName);
			GeneralRocketLoader rocketLoader = new GeneralRocketLoader(Filename);
			Rocket = rocketLoader.load();
		} catch (RocketLoadException oops) {
			System.err.print("made a mistake file : ");
			System.err.println(szFileName+" "+oops.toString());
		}
		return Rocket;
	}
	public int SetupSimulation(String orkFile, int simToGrab, int randomSeed, int timeStep){
		OpenRocketDocument Rocket = LoadRocket(orkFile);
		if(Rocket == null){
			//System.err.println("ork file failed to load");
			return -4;
		}
		int simCount = Rocket.getSimulationCount();
		System.out.print("Number of Simulations in file: ");
		System.out.println(simCount);
		if (simCount == 0){
			//System.err.println("no simulatinos in ork file");
			return -1; 
		}
		if ((simCount < simToGrab)){
			//System.err.println("asked for simulation not present");
			return -2;
		}
		Simulation rocketSimulation = Rocket.getSimulation(simToGrab-1);
		if (rocketSimulation == null){
			//System.err.println("simulation is null");
			return -3;
		}
		System.out.print("Getting Simulation Conditions for: ");
		System.out.println(rocketSimulation.getName());
		System.out.println("status of rocket is " + rocketSimulation.getStatus());
		//It looks like the following is overly complicated, having tested it
		//without using SimulationOptions it appears to be necessary.
		SimulationOptions opt = rocketSimulation.getOptions();
		if (m_rand_seed != 0){
			opt.setRandomSeed(m_rand_seed);
		}
		m_CSimulationConditions = opt.toSimulationConditions();
		m_CSimulationConditions.setCalculateExtras(true);
		return 0;
	}
	/**
	 * runs simulation start to finish just like openrocket main.
	 * */
	public int RunSimulation(){
		//if(m_bIsSimulationStagesRunning==true){
		//	System.err.println("error calling RunSimulation while StartSimulation is running may Invalidate StartSimulations FlightData");
		//	return-1 ;}
		if(m_CSimulationConditions == null)
			{System.err.println("Simulation is not setup; Hint: SetupSimulation");
			return -2;}
		basicEngine = new BasicEventSimulationEngine();
		try{
			m_CFlightData = basicEngine.simulate(m_CSimulationConditions);
		} catch (SimulationException e) {
			System.err.println("oops RunSimulation threw an error");
			return -3;
		}
		return 0;
	}
	public int StepSimulation(int steps){
		if(steppingEngine == null){
			steppingEngine = new SteppingEventSimulationEngine();
			try {
				m_CFlightData = steppingEngine.initialize(m_CSimulationConditions);
			} catch (SimulationException e) {
				//System.err.println("steppingEngine.initialize:" + e);
				return -3;
			}
		}
		if(IsSimulationRunning()){
			try {
				return steppingEngine.simulate(steps);
			} catch (SimulationException e) {
				//System.err.println("steppingEngine.simulate:" + e);
				return -2;
			}
		}
		return -1;
	}

	/**********************************************************************
	 * seters and getters for simulation data
	 ********************************************************************* */
		public double setOrientationXYZ(double W,double X,double Y,double Z){
			if(m_CStatus == null)
				return -1;
			Quaternion xyzOrientation = new Quaternion(W,X,Y,Z);
			if(xyzOrientation != null)
				{
				m_CStatus.setRocketOrientationQuaternion(xyzOrientation);
				}
			return 0;
		}
		public double getOrientationX(){
			if(m_CStatus == null)
				return -1;
			Quaternion x = m_CStatus.getRocketOrientationQuaternion();
			if(x == null)
				return -2;
			return x.getX();
		}
		public double getOrientationY(){
			if(m_CStatus == null)
				return -1;
			Quaternion x = m_CStatus.getRocketOrientationQuaternion();
			if(x == null)
				return -2;
			return x.getY();
		}
		public double getOrientationZ(){
			if(m_CStatus == null)
				return -1;
			Quaternion x = m_CStatus.getRocketOrientationQuaternion();
			if(x == null)
				return -2;
			return x.getZ();
		}
		public double getOrientationW(){
			if(m_CStatus == null)
				return -1;
			Quaternion x = m_CStatus.getRocketOrientationQuaternion();
			if(x == null)
				return -2;
			return x.getW();
		}
		public double GetAccelerationX(){
			if(m_CStatus == null)
				return -1;
			return m_CStatus.getRocketLinearAcceleration();
		}
		public double GetVelocityZ(){
			if(m_CStatus == null)
				return -1;
			if(m_CStatus.getRocketVelocity() == null)
				return -2;
			return m_CStatus.getRocketVelocity().z;
		}
		public double GetVelocityX(){
			if(m_CStatus == null)
				return -1;
			if(m_CStatus.getRocketVelocity() == null)
				return -2;
			return m_CStatus.getRocketVelocity().x;
		}
		public double GetVelocityY(){
			if(m_CStatus == null)
				return -1;
			if(m_CStatus.getRocketVelocity() == null)
				return -2;
			return m_CStatus.getRocketVelocity().y;
		}
		public double GetCordinateX(){
			if(m_CStatus == null)
				return -1;
			Coordinate x = m_CStatus.getRocketPosition();
			if(x == null){
				return -2;
			}
			return x.x;
		}
		public double GetCordinateY(){
			if(m_CStatus == null)
				return -1;
			Coordinate x = m_CStatus.getRocketPosition();
			if(x == null){
				return -2;
			}
			return x.y;
		}	
		public double GetCordinateZ(){
			if(m_CStatus == null)
				return -1;
			Coordinate x = m_CStatus.getRocketPosition();
			if(x == null){
				return -2;
			}
			return x.z;
		}
		public double GetVelocityRotationX(){
			if(m_CStatus == null)
				return -1;
			Coordinate x = m_CStatus.getRocketRotationVelocity();
			if(x == null){
				return -2;
			}
			return x.x;
		}
		public double GetVelocityRotationY() {
			if(m_CStatus == null)
				return -1;
			Coordinate x = m_CStatus.getRocketRotationVelocity();
			if(x == null){
				return -2;
			}
			return x.y;
		}
		public double GetVelocityRotationZ() {
			if(m_CStatus == null)
				return -1;
			Coordinate x = m_CStatus.getRocketRotationVelocity();
			if(x == null){
				return -2;
			}
			return x.z;
		}
		public double Getsimulationrunningtime() {
			if(m_CStatus == null)
				return -1;
			return m_CStatus.getSimulationTime();
			}
		public boolean GetBoolTumbling() {
			if(m_CStatus == null)
				return false;
			return m_CStatus.isTumbling();
		}
		public boolean GetBoolMotorIgnited() {
			if(m_CStatus == null)
				return false;
			return m_CStatus.isMotorIgnited();
					}
		public boolean GetBoolApogeeReached() {
			if(m_CStatus == null)
				return false;
			return m_CStatus.isApogeeReached();
		}
		public boolean GetBoolLaunchRodCleared() {
			if(m_CStatus == null)
				return false;
			return m_CStatus.isLaunchRodCleared();
		}
		public boolean GetBoolLiftoff() {
			if(m_CStatus == null)
				return false;
			return m_CStatus.isLiftoff();
		}

		/****************************************************
		 * flight data maximums (RK4) functions
		 *****************************************************/

		public double getMaxAltitude(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getMaxAltitude();
		}
		public double getMaxVelocity(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getMaxVelocity();
		}
		public double getMaxAcceleration(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getTimeToApogee();
		}
		public double getMaxMachNumber(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getMaxMachNumber();
		}
		public double getTimeToApogee(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getTimeToApogee();
		}
		public double getFlightTime(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getFlightTime();
		}
		public double getGroundHitVelocity(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getGroundHitVelocity();
		}
		public double getLaunchRodVelocity(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getLaunchRodVelocity();
		}
		public double getDeploymentVelocity(){
			if (m_CFlightData == null)
				return -1;
			return m_CFlightData.getDeploymentVelocity();
		}
};
