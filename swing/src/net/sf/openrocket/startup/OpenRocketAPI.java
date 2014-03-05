package net.sf.openrocket.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.simulation.*;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.CSVExport;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.logging.LoggingSystemSetup;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.simulation.BasicEventSimulationEngine;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.SimulationConditions;
import net.sf.openrocket.simulation.SteppingEventSimulationEngine;
import net.sf.openrocket.simulation.exception.ReturnTypeException;
import net.sf.openrocket.simulation.exception.SimulationException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.APIGuiModule;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Quaternion;
import net.sf.openrocket.utils.csv.*;
import ch.qos.logback.classic.Level;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class OpenRocketAPI {
	private BasicEventSimulationEngine basicEngine = null;
	private SteppingEventSimulationEngine steppingEngine = null;
	protected FlightData m_CFlightData = null;
	private SimulationConditions m_CSimulationConditions = null;
	private CSVWriter CSVOutputFile = null;

	private final static Logger log = LoggerFactory.getLogger(OpenRocketAPI.class);
	
	public int setlogfile(String filename) {
		
	
		LoggingSystemSetup.addFileAppender(filename,Level.DEBUG);
		return 0;
	}

	public OpenRocketAPI() {
		APIGuiModule guiModule = new APIGuiModule();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		Application.setInjector(injector);

		guiModule.startLoader();// might just do some initializing
		
		LoggingSystemSetup.setupLoggingAppender();
		if (System.getProperty("openrocket.debug") != null) {
			LoggingSystemSetup.addConsoleAppender();
		}
	}

	/****************************************************
	 * flight data functions
	 *****************************************************/

	/**
	 * Returns the current iteration of the simulation
	 * 
	 * @return (int) iteration
	 */
	public int GetIteration() {
		return GetFlightData().getLength();
	}

	/**
	 * Returns the time step for the current iteration of the simulation.
	 * 
	 * @return (double) End time of current time step - end time of previous
	 *         time step.
	 * @throws ReturnTypeException 
	 */
	public double GetTimeStep() throws ReturnTypeException {
		double i = this.GetValue(FlightDataType.TYPE_TIME_STEP);
		if (!Double.isNaN(i))
			return i;
		throw new ReturnTypeException("Return Value is NaN");
	}

	/**
	 * Returns the total time as of the end current step of the simulation
	 * 
	 * @return (double) Total time of the current simulation
	 */
	public double GetTime() {
		FlightDataStep fds = GetFlightDataStep();
		double i = fds.get(FlightDataType.TYPE_TIME);
		double j = fds.get(FlightDataType.TYPE_TIME_STEP);
		if (!(Double.isNaN(i) || Double.isNaN(j)))
			return i + j;
		return 0;
	}

	/**
	 * Returns one value correlating to the key type and the specified
	 * iteration.
	 * 
	 * @param type (FlightDataType) Type of data to get the min of
	 * @return (double) Value requested
	 * @throws ReturnTypeException 
	 */
	public double GetValue(FlightDataType type) throws ReturnTypeException {
		return GetValue(type, -1);
	}

	/**
	 * Returns one value correlating to the key type and the specified
	 * iteration.
	 * 
	 * @param type (FlightDataType) Type of data to get the min of
	 * @param step (int) Step to obtain value from
	 * @return (double) Value requested
	 * @throws ReturnTypeException 
	 */
	public double GetValue(FlightDataType type, int step) throws ReturnTypeException {
		FlightDataStep fds = null;
		if (step < 0) {
			fds = GetFlightDataStep();
		} else {
			fds = GetFlightDataStep(step);
		}
		double tsl = fds.get(type);
		if (!(Double.isNaN(tsl)))
			return tsl;
		throw new ReturnTypeException("Return Value is NaN");
	}

	/**
	 * @param type (FlightDataType) Type of data to get the min of
	 * @return (double) Value requested
	 * @throws ReturnTypeException 
	 */
	public double GetMaximum(FlightDataType type) throws ReturnTypeException {
		double i = GetFlightData().getMaximum(type);
		if (!Double.isNaN(i))
			return i;
		throw new ReturnTypeException("Return Value is NaN");
	}

	/**
	 * @param type (FlightDataType) Type of data to get the min of
	 * @return (double) Value requested
	 * @throws ReturnTypeException 
	 */
	public double GetMinimum(FlightDataType type) throws ReturnTypeException {
		double i = GetFlightData().getMinimum(type);
		if (!Double.isNaN(i))
			return i;
		throw new ReturnTypeException("Return Value is NaN");
	}

	/**
	 * Writes the values for the specified iteration to the specified file
	 * 
	 * @param filename (string) file to write to 
	 *                          "close" closes the csv file.
	 * @return (int) error code.
	 */
	public int FlightDataStepToCSV(String filename) {
		FlightDataStep fds_temp = null;
		if (!filename.equals("close"))
			fds_temp = GetFlightDataStep();
		return CSVWriter(filename, fds_temp);
	}

	/**
	 * Writes the values for the specified iteration to the specified file
	 * 
	 * @param filename (string) file to write to 
	 *                          "close" closes the csv file.
	 * @param iteration (int) Desired iteration 
	 *                        (the first iteration is 1)
	 * @return (int) error code.
	 */
	public int FlightDataStepToCSV(String filename, int iteration) {
		FlightDataStep fds_temp = GetFlightDataStep(iteration);
		return CSVWriter(filename, fds_temp);
	}

	/**
	 * Writes the values for the specified iteration to the specified file
	 * 
	 * @param filename (string) file to write to 
	 *                          "close" closes the csv file.
	 * @param branch (FlightDataBranch) FlightDataBranch to get data from
	 * @param iteration (int) Desired iteration 
	 *                        (the first iteration is 1)
	 * @return (int) error code.
	 */
	public int FlightDataStepToCSV(String filename, FlightDataBranch branch, int iteration){
		FlightDataStep fds_temp = GetFlightDataStep(branch, iteration);
		return CSVWriter(filename, fds_temp);
	}

	/**
	 * Outputs to filename CSV of the entire simulation.
	 * 
	 * @param filename (String)
	 * @return (int) result
	 */
	public int FullCSVOut(String filename) {
		try {
			CSVOutputFile = new CSVWriter(filename);
		} catch (Throwable t) {
			return -1;
		}
		if (m_CFlightData == null) {
			return -2;
		}
		int branches = m_CFlightData.getBranchCount();
		FlightDataBranch fdb = null;
		for (int j = 0; j < branches; j++) {
			fdb = m_CFlightData.getBranch(j);
			int fdb_length = fdb.getLength() + 1; // (iterations start at 1)
			for (int i = 1; i < fdb_length; i++) {
				FlightDataStepToCSV(filename, fdb, i);
			}
		}
		CSVOutputFile.close();
		return 0;
	}

	private int CSVWriter(String file, FlightDataStep fds) {
		if (CSVOutputFile != null) {
			if (file.equals("close")) {
				CSVOutputFile.close();
			} else if (!(CSVOutputFile.nameEquals(file))) {
				CSVOutputFile.close();
				try {
					CSVOutputFile = new CSVWriter(file);
				} catch (Throwable t) {
					return -1;
				}
				CSVFile csvLines = CSVCreateLine(fds, true);
				CSVOutputFile.writeFile(csvLines);
			} else {
				CSVFile csvLines = CSVCreateLine(fds, false);
				CSVOutputFile.writeFile(csvLines);
			}
		} else {
			try {
				CSVOutputFile = new CSVWriter(file);
			} catch (Throwable t) {
				return -1;
			}
			CSVFile csvLines = CSVCreateLine(fds, true);
			CSVOutputFile.writeFile(csvLines);
		}
		return 0;
	}

	private CSVFile CSVCreateLine(FlightDataStep fds, boolean writeKeys) {
		CSVFile csvLineS = new CSVFile();
		CSVLine csvLine = null;
		if (writeKeys) {
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
			} else {
				csvLine.add(null);
			}
		}
		return csvLineS;
	}
	/**
	 * Matches the export from the GUI Export data functionality
	 * In the GUI: Select ALL variables, ',' field separator,
	 *   Only check Comments: "Included field descriptions"
	 *   Comment character: '#'
	 * This function will export each stage to a separate file.
	 *   
	 * @param filename (String)
	 * @return (int)
	 * @throws IOException
	 */
	public int CSVTest(String filename) throws IOException {
		int branch_len = m_CFlightData.getBranchCount();
		for (int i =0; i < branch_len; i++){
			FlightDataBranch fdb = m_CFlightData.getBranch(i);
			FlightDataType[] keys = fdb.getTypes();
			int l = keys.length;
			Unit[] units = new Unit[l];
			for (int j =0; j < l; j++){
				units[j] = keys[j].getUnitGroup().getUnit(0);
			}
			String f = filename + "." + Integer.toString(i) + "_" + fdb.getBranchName() +  ".csv";
			OutputStream output = new FileOutputStream(f);
			CSVExport.exportCSV(output, m_CSimulationConditions.getSimulation(), fdb, keys, units, ",", "#", false, true, false);
		}
		return 0;
	}
	/**
	 * Returns the values for the current iteration of the simulation
	 * 
	 * @return (FlightDataStep)
	 */
	public FlightDataStep GetFlightDataStep() {
		return new FlightDataStep(GetFlightData());
	}

	/**
	 * Returns the values for the specified iteration of the simulation
	 * 
	 * @param i (int) Desired iteration (the first iteration is iteration 1)
	 * @return (FlightDataStep)
	 */
	public FlightDataStep GetFlightDataStep(int i) {
		return new FlightDataStep(GetFlightData(), i);
	}

	/**
	 * Returns the values for the specified iteration of the simulation
	 * 
	 * @param i (FlightDataBranch) FlightDataBranch to get data from
	 * @param b (int) Desired iteration (the first iteration is iteration 1)
	 * @return (FlightDataStep)
	 */
	protected FlightDataStep GetFlightDataStep(FlightDataBranch b, int i) {
		return new FlightDataStep(b, i);
	}

	/**
	 * Returns the entire class containing all of the simulation data. The
	 * intention is to have this function be responsible for providing the
	 * correct FlightDataBranch, and should be called rather then using
	 * m_CFlightDataBranch directly. This is a shallow copy, probably not the
	 * best idea to be public.
	 * 
	 * @return (FlightDataBranch)
	 */
	protected FlightDataBranch GetFlightData() {
		FlightDataBranch fdb_temp = null;
		try {
			fdb_temp = steppingEngine.getFlightData();
			if (fdb_temp == null) {
				throw new IllegalStateException("fdb_temp == null");
			}
		} catch (Throwable t) {
			log.warn("OpenRocketAPI.GetFlightData() threw a m_CStatus related exception" + t);
			log.warn("OpenRocketAPI.GetFlightData() perhaps the simulation isn't running");
			fdb_temp = new FlightDataBranch("empty", FlightDataType.TYPE_TIME);
		}
		return fdb_temp;
	}

	/******************************************************************
	 * rocket simulation functions
	 * **************************************************************/
	/**
	 * Returns whether a simulation is currently running.
	 * 
	 * @return (boolean)
	 */
	public boolean SimulationIsRunning() {
		if (steppingEngine != null) {
			if (steppingEngine.simulationRunning()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether the a simulation is currently running.
	 * 
	 * @return (boolean)
	 */
	public boolean SimulationLoopIsRunning() {
		return SimulationIsRunning();
	}

	/**
	 * Sets the user-specified time step 
	 * OpenRocket uses the minimum of the following;
	 * The user-specified time step 
	 *    (or 1/5th of it if still on the launch rod)
	 * maxTimeStep 
	 * maximum pitch step angle limit 
	 * maximum roll step angle limit 
	 * maximum roll rate change limit 
	 * maximum pitch change limit
	 * 1/10th of the launch rod length 
	 *    if still on the launch rod 1.50 times the
	 * previous time step
	 * 
	 * @param timeStep (double)
	 */
	public void SetTimeStep(double timeStep) {
		if (steppingEngine != null) {
			steppingEngine.setTimeStep(timeStep);
		}
	}

	/**
	 * Loads a rocket from an ork file
	 * 
	 * @param filename (String) name of the ork file to load
	 * @return (OpenRocketDocument) loaded from the fileName
	 * */
	protected OpenRocketDocument LoadRocket(String fileName) {
		OpenRocketDocument Rocket = null;
		try {
			File Filename = new File(fileName);
			log.info("loading rocket from " + fileName);
			GeneralRocketLoader rocketLoader = new GeneralRocketLoader(Filename);
			Rocket = rocketLoader.load();
		} catch (RocketLoadException oops) {
			log.error("made a mistake file : ");
			log.error(fileName + " " + oops.toString());
		}
		return Rocket;
	}

	/**
	 * Setup a new simulation
	 * 
	 * @param orkFile (String) Name of the ork file to use
	 * @param simToGrab (int) Simulation in the ork file to use. 
	 *                        The first simulation is 1.
	 * @param randomSeed (int) Random seed to use.
	 * @param timeStep (double) TimeStep to use.
	 * @return (int) result
	 */
	public int SimulationSetup(String orkFile) {
		return SimulationSetup(orkFile, 1, 0, 0);
	}

	/**
	 * Setup a new simulation
	 * 
	 * @param orkFile (String) Name of the ork file to use
	 * @param simToGrab (int) Simulation in the ork file to use. 
	 *                        The first simulation is 1.
	 * @param randomSeed (int) Random seed to use.
	 * @param timeStep (double) TimeStep to use.
	 * @return (int) result
	 */
	public int SimulationSetup(String orkFile, int simToGrab) {
		return SimulationSetup(orkFile, simToGrab, 0, 0);
	}

	/**
	 * Setup a new simulation
	 * 
	 * @param orkFile (String) Name of the ork file to use
	 * @param simToGrab (int) Simulation in the ork file to use. 
	 *                        The first simulation is 1.
	 * @param randomSeed (int) Random seed to use.
	 * @param timeStep (double) TimeStep to use.
	 * @return (int) result
	 */
	public int SimulationSetup(String orkFile, int simToGrab, int randomSeed) {
		return SimulationSetup(orkFile, simToGrab, randomSeed, 0);
	}

	/**
	 * Setup a new simulation
	 * 
	 * @param orkFile (String) Name of the ork file to use
	 * @param simToGrab (int) Simulation in the ork file to use. 
	 *                        The first simulation is 1.
	 * @param randomSeed (int) Random seed to use.
	 * @param timeStep (double) TimeStep to use.
	 * @return (int) result
	 */
	public int SimulationSetup(String orkFile, int simToGrab, int randomSeed,
			double timeStep) {
		OpenRocketDocument Rocket = LoadRocket(orkFile);
		if (Rocket == null) {
			// System.err.println("ork file failed to load");
			return -4;
		}
		int simCount = Rocket.getSimulationCount();
		log.info("Number of Simulations in file: ");
		log.info(String.valueOf(simCount));
		if (simCount == 0) {
			log.error("no simulatinos in ork file");
			return -1;
		}
		if ((simToGrab < 1) || (simCount < simToGrab)) {
			log.error("asked for simulation not present");
			return -2;
		}
		Simulation rocketSimulation = Rocket.getSimulation(simToGrab - 1);
		if (rocketSimulation == null) {
			log.error("simulation is null");
			return -3;
		}
		log.info("Getting Simulation Conditions for: ");
		log.info(rocketSimulation.getName());
		log.info("status of rocket is " + rocketSimulation.getStatus());
		// It looks like the following is overly complicated, having tested it
		// without using SimulationOptions it appears to be necessary.
		SimulationOptions opt = rocketSimulation.getOptions();
		if (randomSeed != 0) {
			opt.setRandomSeed(randomSeed);
		}
		if (timeStep != 0) {
			// TODO: check for min / max (validate this parameter)
			opt.setTimeStep(timeStep);
		}
		m_CSimulationConditions = opt.toSimulationConditions();
		m_CSimulationConditions.setSimulation(rocketSimulation);
		m_CSimulationConditions.setCalculateExtras(true);
		return 0;
	}

	/**
	 * Runs simulation start to finish just like OpenRocket main. The simulation
	 * must be setup with SetupSimulation first.
	 * 
	 * @return (int) result
	 */
	public int SimulationRun() {
		if (m_CSimulationConditions == null) {
			log.error("Simulation is not setup; Hint: SetupSimulation");
			return -2;
		}
		basicEngine = new BasicEventSimulationEngine();
		try {
			m_CFlightData = basicEngine.simulate(m_CSimulationConditions);
		} catch (SimulationException e) {
			log.error("oops RunSimulation threw an error");
			return -3;
		}
		return 0;
	}

	/**
	 * Step a simulation has has been setup with SetupSimulation.
	 * 
	 * @param steps (int) Number of steps to attempt to iterat before returning.
	 * @return (int) result
	 */
	public int SimulationStep(int steps) {
		if (steppingEngine == null) {
			steppingEngine = new SteppingEventSimulationEngine();
			try {
				m_CFlightData = steppingEngine.initialize(m_CSimulationConditions);
			} catch (SimulationException e) {
				log.error("steppingEngine.initialize:" + e);
				return -2;
			}
		}
		if (SimulationIsRunning()) {
			try {
				return steppingEngine.simulate(steps);
			} catch (SimulationException e) {
				log.error("steppingEngine.simulate:" + e);
				return -1;
			}
		}
		return 0;
	}

	/**********************************************************************
	 * seters and getters for simulation data
	 ********************************************************************* */
	public double setOrientationXYZ(double W, double X, double Y, double Z) {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Quaternion xyzOrientation = new Quaternion(W, X, Y, Z);
		if (xyzOrientation != null) {
			m_CStatus.setRocketOrientationQuaternion(xyzOrientation);
		}
		return 0;
	}

	public double getOrientationX() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Quaternion x = m_CStatus.getRocketOrientationQuaternion();
		if (x == null)
			return -2;
		return x.getX();
	}

	public double getOrientationY() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Quaternion x = m_CStatus.getRocketOrientationQuaternion();
		if (x == null)
			return -2;
		return x.getY();
	}

	public double getOrientationZ() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Quaternion x = m_CStatus.getRocketOrientationQuaternion();
		if (x == null)
			return -2;
		return x.getZ();
	}

	public double getOrientationW() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Quaternion x = m_CStatus.getRocketOrientationQuaternion();
		if (x == null)
			return -2;
		return x.getW();
	}

	/*
	 * public double GetAccelerationX(){ SimulationStatus m_CStatus =
	 * steppingEngine.getSimulationStatus(); if(m_CStatus == null) return -1;
	 * return m_CStatus.getRocketLinearAcceleration(); }
	 */
	public double GetVelocityZ() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		if (m_CStatus.getRocketVelocity() == null)
			return -2;
		return m_CStatus.getRocketVelocity().z;
	}

	public double GetVelocityX() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		if (m_CStatus.getRocketVelocity() == null)
			return -2;
		return m_CStatus.getRocketVelocity().x;
	}

	public double GetVelocityY() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		if (m_CStatus.getRocketVelocity() == null)
			return -2;
		return m_CStatus.getRocketVelocity().y;
	}

	public double GetCordinateX() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Coordinate x = m_CStatus.getRocketPosition();
		if (x == null) {
			return -2;
		}
		return x.x;
	}

	public double GetCordinateY() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Coordinate x = m_CStatus.getRocketPosition();
		if (x == null) {
			return -2;
		}
		return x.y;
	}

	public double GetCordinateZ() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Coordinate x = m_CStatus.getRocketPosition();
		if (x == null) {
			return -2;
		}
		return x.z;
	}

	public double GetVelocityRotationX() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Coordinate x = m_CStatus.getRocketRotationVelocity();
		if (x == null) {
			return -2;
		}
		return x.x;
	}

	public double GetVelocityRotationY() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Coordinate x = m_CStatus.getRocketRotationVelocity();
		if (x == null) {
			return -2;
		}
		return x.y;
	}

	public double GetVelocityRotationZ() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		Coordinate x = m_CStatus.getRocketRotationVelocity();
		if (x == null) {
			return -2;
		}
		return x.z;
	}

	public double Getsimulationrunningtime() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return -1;
		return m_CStatus.getSimulationTime();
	}

	public boolean GetBoolTumbling() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return false;
		return m_CStatus.isTumbling();
	}

	public boolean GetBoolMotorIgnited() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return false;
		return m_CStatus.isMotorIgnited();
	}

	public boolean GetBoolApogeeReached() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return false;
		return m_CStatus.isApogeeReached();
	}

	public boolean GetBoolLaunchRodCleared() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return false;
		return m_CStatus.isLaunchRodCleared();
	}

	public boolean GetBoolLiftoff() {
		SimulationStatus m_CStatus = steppingEngine.getSimulationStatus();
		if (m_CStatus == null)
			return false;
		return m_CStatus.isLiftoff();
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
};
