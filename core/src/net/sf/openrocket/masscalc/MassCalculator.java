package net.sf.openrocket.masscalc;

import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.util.*;

public class MassCalculator implements Monitorable {
	
	public static final double MIN_MASS = MathUtil.EPSILON;

	/*
	 * Cached data.  All CG data is in absolute coordinates.  All moments of inertia
	 * are relative to their respective CG.
	 */
	//	private HashMap< Integer, MassData> stageMassCache = new HashMap<Integer, MassData >();
	//	private MassData rocketSpentMassCache;
	//	private MassData propellantMassCache;

	private int modId = 0;
	
	//////////////////  Constructors ///////////////////
	public MassCalculator() {
	}
	
	////////////////// Public Accessors ///////////////////
	
	
	/**
	 * Calculates mass data of the rocket's structure
	 * - includes structure 
	 * - excludes motors
	 * - excludes propellant
	 *  
	 * @param config		the rocket configuration to calculate for
	 * @return				the MassData struct of the rocket
	 */
	public static RigidBody calculateStructure( final FlightConfiguration config) {
		return calculate( MassCalculation.Type.STRUCTURE, config, Motor.PSEUDO_TIME_EMPTY );
	}	
	
	/**
	 * Calculates mass data of the rocket's burnout mass
	 * - includes structure 
	 * - includes motors
	 * - for Black Powder & Composite motors, this generally *excludes* propellant
	 *  
	 * @param config		the rocket configuration to calculate for
	 * @return				the MassData struct of the rocket at burnout
	 */
	public static RigidBody calculateBurnout( final FlightConfiguration config) {
		return calculate( MassCalculation.Type.BURNOUT, config, Motor.PSEUDO_TIME_BURNOUT );
	}	

	/**
	 * Calculates mass data of the rocket's motor(s) at launch
	 * - excludes structure
	 * - includes motors
	 * - includes propellant
	 *  
	 * @param config		the rocket configuration to calculate for
	 * @return				the MassData struct of the motors at launch
	 */
	public static RigidBody calculateMotor( final FlightConfiguration config) {
		return calculate( MassCalculation.Type.MOTOR, config, Motor.PSEUDO_TIME_LAUNCH );
	}	
	
	/**
	 * Compute the rocket's launch mass properties, given a configuration
	 * - includes structure
	 * - includes motors
	 * - includes propellant
	 *  
	 * @param config             the rocket configuration
	 * @return					 the MassData struct of the rocket at launch
	 */
	public static RigidBody calculateLaunch( final FlightConfiguration config ){
		return calculate( MassCalculation.Type.LAUNCH, config, Motor.PSEUDO_TIME_LAUNCH );
	}
	
	/** calculates the massdata for all motors in the rocket given the simulation status.
	 * - excludes structure
	 * - includes motors
	 * - includes propellant
	 * 
	 * @param status  CurrentSimulation status to calculate data with
	 * @return  combined mass data for all propellant
	 */
	public static RigidBody calculateMotor( final SimulationStatus status ){
		return calculate( MassCalculation.Type.MOTOR, status ); 
	}

	////////////////// Mass property Wrappers  ///////////////////
	// all mass calculation calls should probably call through one of these two wrappers. 
	
	// convenience wrapper -- use this to implicitly create a plain MassCalculation object with common parameters
	public static RigidBody calculate( final MassCalculation.Type _type, final SimulationStatus status ){
		final FlightConfiguration config = status.getConfiguration();
		final double time = status.getSimulationTime();
		MassCalculation calculation= new MassCalculation( _type, config, time, config.getRocket(), Transformation.IDENTITY, null);
		
		calculation.calculateAssembly();
		RigidBody result = calculation.calculateMomentOfInertia();
		return result;
	}
	
	// convenience wrapper -- use this to implicitly create a plain MassCalculation object with common parameters 
	public static RigidBody calculate( final MassCalculation.Type _type, final FlightConfiguration _config,  double _time){
		MassCalculation calculation = new MassCalculation( _type, _config, _time, _config.getRocket(), Transformation.IDENTITY, null);
		calculation.calculateAssembly();
		return calculation.calculateMomentOfInertia();
	}

	/**
	 * Compute an analysis of the per-component CG's of the provided configuration.
	 * The returned map will contain an entry for each physical rocket component (not stages)
	 * with its corresponding (best-effort) CG.  Overriding of subcomponents is ignored.
	 * The CG of the entire configuration with motors is stored in the entry with the corresponding
	 * Rocket as the key.
	 *
	 * Deprecated:
	 * This function is fundamentally broken, because it asks for a calculation which ignores instancing.
	 * This function will work with simple rockets, but will be misleading or downright wrong for others.
	 *
	 * This is a problem with using a single-typed map:
	 * [1] multiple instances of components are not allowed, and must be merged.
	 * [2] propellant / motor data does not have a corresponding RocketComponent.
	 *     ( or mount-data collides with motor-data )
	 *
	 * @return a list of CG coordinates for every instance of this component
	 */
	public static Map<Integer,CMAnalysisEntry> getCMAnalysis(FlightConfiguration config) {

		Map<Integer,CMAnalysisEntry> analysisMap = new HashMap<>();

		MassCalculation calculation = new MassCalculation(
				MassCalculation.Type.LAUNCH,
				config,
				Motor.PSEUDO_TIME_LAUNCH,
				config.getRocket(),
				Transformation.IDENTITY,
				analysisMap);

		calculation.calculateAssembly();

		CMAnalysisEntry totals = new CMAnalysisEntry(config.getRocket());
		totals.totalCM = calculation.centerOfMass;
		totals.eachMass = calculation.centerOfMass.weight;
		analysisMap.put(config.getRocket().hashCode(), totals);

		return analysisMap;
	}
	
	////////////////// Mass property calculations  ///////////////////
	@Override
	public int getModID() {
		return this.modId;
	}
	
}
