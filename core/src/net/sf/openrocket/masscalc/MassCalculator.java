package net.sf.openrocket.masscalc;

import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.masscalc.MassCalculation.Type;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.Transformation;

public class MassCalculator implements Monitorable {
	
	public static final double MIN_MASS = MathUtil.EPSILON;

	/*
	 * Cached data.  All CG data is in absolute coordinates.  All moments of inertia
	 * are relative to their respective CG.
	 */
	//	private HashMap< Integer, MassData> stageMassCache = new HashMap<Integer, MassData >();
	//	private MassData rocketSpentMassCache;
	//	private MassData propellantMassCache;

	private int modId=0;
	
	//////////////////  Constructors ///////////////////
	public MassCalculator() {
	}
	
	////////////////// Public Accessors ///////////////////
	
	
	/**
	 * Calculates mass data of the rocket's burnout mass
	 * - includes structure 
	 * - includes motors
	 * - for Black Powder & Composite motors, this generally *excludes* propellant
	 *  
	 * @param configuration		the rocket configuration to calculate for
	 * @return					the MassData struct of the motors at burnout
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
	 * @param configuration		the rocket configuration to calculate for
	 * @return					the MassData struct of the motors at burnout
	 */
	public static RigidBody calculateBurnout( final FlightConfiguration config) {
		return calculate( MassCalculation.Type.BURNOUT, config, Motor.PSEUDO_TIME_BURNOUT );
	}	
	
	public static RigidBody calculateMotor( final FlightConfiguration config) {
		return calculate( MassCalculation.Type.MOTOR, config, Motor.PSEUDO_TIME_LAUNCH );
	}	
	
	/**
	 * Compute the burnout mass properties all motors, given a configuration
	 * 
	 * Will calculate data for: MassCalcType.BURNOUT_MASS
	 *  
	 * @param config             the rocket configuration
	 * @return					the MassData struct of the motors at burnout
	 */
	public static RigidBody calculateLaunch( final FlightConfiguration config ){
		return calculate( MassCalculation.Type.LAUNCH, config, Motor.PSEUDO_TIME_LAUNCH );
	}
	
	/** calculates the massdata for all propellant in the rocket given the simulation status.
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
		MassCalculation calculation= new MassCalculation( _type, config, time, config.getRocket(), Transformation.IDENTITY);
		
		calculation.calculateAssembly();
		RigidBody result = calculation.calculateMomentOfInertia();
		return result;
	}
	
	// convenience wrapper -- use this to implicitly create a plain MassCalculation object with common parameters 
	public static RigidBody calculate( final MassCalculation.Type _type, final FlightConfiguration _config,  double _time ){
		MassCalculation calculation = new MassCalculation( _type, _config, _time, _config.getRocket(), Transformation.IDENTITY);
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
	 * @param configuration		the rocket configuration
	 * @param type				the state of the motors (none, launch mass, burnout mass)
	 * @return					a map from each rocket component to its corresponding CG.
	 */
	@Deprecated
	public Map<RocketComponent, Coordinate> getCGAnalysis(FlightConfiguration configuration) {
		//		revalidateCache(configuration);
		
		Map<RocketComponent, Coordinate> map = new HashMap<RocketComponent, Coordinate>();
		
		Coordinate rocketCG = Coordinate.ZERO;
		for (RocketComponent comp : configuration.getActiveComponents()) {
			Coordinate[] cgs = comp.toAbsolute(comp.getCG());
			Coordinate stageCG = Coordinate.NUL;
			for (Coordinate cg : cgs) {
				stageCG = stageCG.average(cg);
			}
			map.put(comp, stageCG);
			
			rocketCG.average( stageCG);
		}
		
		map.put(configuration.getRocket(), rocketCG );
		
		return map;
	}

	
	////////////////// Mass property calculations  ///////////////////
	

	
	
	@Override
	public int getModID() {
		return this.modId;
	}
	
}
