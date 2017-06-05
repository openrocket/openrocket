package net.sf.openrocket.masscalc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.Instanceable;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.MotorClusterState;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;

public class MassCalculator implements Monitorable {

	//private static final Logger log = LoggerFactory.getLogger(MassCalculator.class);
	public static final double MIN_MASS = 0.001 * MathUtil.EPSILON;
	
	private int rocketMassModID = -1;
	private int rocketTreeModID = -1;
	
	
	/*
	 * Cached data.  All CG data is in absolute coordinates.  All moments of inertia
	 * are relative to their respective CG.
	 */
	private HashMap< Integer, MassData> stageMassCache = new HashMap<Integer, MassData >();
	private MassData rocketSpentMassCache;
	private MassData propellantMassCache;
	
	
	//////////////////  Constructors ///////////////////
	public MassCalculator() {
	}
	
	//////////////////  Mass property calculations  ///////////////////
	

	public MassData getRocketSpentMassData( final FlightConfiguration config ){
		revalidateCache( config);
		return this.rocketSpentMassCache;
	}
	

	public MassData getRocketLaunchMassData( final FlightConfiguration config ){
		revalidateCache( config);
		return rocketSpentMassCache.add( propellantMassCache );
	}
    	
	
	/** calculates the massdata for all propellant in the rocket given the simulation status.
	 * 
	 * @param status  CurrentSimulation status to calculate data with
	 * @return  combined mass data for all propellant
	 */
	public MassData getPropellantMassData( final SimulationStatus status ){
		revalidateCache( status );
		
		return propellantMassCache;
	}
	
	
	/** calculates the massdata @ launch for all propellant in the rocket 
	 * 
	 * @param status  CurrentSimulation status to calculate data with
	 * @return  combined mass data for all propellant
	 */
	protected MassData calculatePropellantMassData( final FlightConfiguration config ){
		MassData allPropellantData = MassData.ZERO_DATA;
		
		Collection<MotorConfiguration> activeMotorList = config.getActiveMotors();
		for (MotorConfiguration mtrConfig : activeMotorList ) {
			MassData curMotorConfigData = calculateClusterPropellantData( mtrConfig, Motor.PSEUDO_TIME_LAUNCH );
			
			allPropellantData = allPropellantData.add( curMotorConfigData );
		}
		
		return allPropellantData;
	}
	
	/** calculates the massdata @ launch for all propellant in the rocket 
	 * 
	 * @param status  CurrentSimulation status to calculate data with
	 * @return  combined mass data for all propellant
	 */
	protected MassData calculatePropellantMassData( final SimulationStatus status ){
		MassData allPropellantData = MassData.ZERO_DATA;

		Collection<MotorClusterState> motorStates = status.getActiveMotors();
		for (MotorClusterState state: motorStates) {
			final double motorTime = state.getMotorTime( status.getSimulationTime() );

			MassData clusterPropData = calculateClusterPropellantData( state.getConfig(), motorTime );
				
			allPropellantData = allPropellantData.add( clusterPropData);
		}

		return allPropellantData;
	}
	
	// helper method to calculate the propellant mass data for a given motor cluster( i.e. MotorConfiguration)
	private MassData calculateClusterPropellantData( final MotorConfiguration mtrConfig, final double motorTime ){
		final Motor mtr = mtrConfig.getMotor();
		final MotorMount mnt = mtrConfig.getMount();
		final int instanceCount = mnt.getInstanceCount();
		
		// location of mount, w/in entire rocket
		final Coordinate[] locations = mnt.getLocations();
		final double motorXPosition = mtrConfig.getX();  // location of motor from mount
		
		final double propMassEach = mtr.getPropellantMass( motorTime );
		final double propCMxEach = mtr.getCMx( motorTime); // CoM from beginning of motor

		// coordinates in rocket frame; Ir, It about CoM.
		final Coordinate curClusterCM = new Coordinate( locations[0].x + motorXPosition + propCMxEach, 0, 0, propMassEach*instanceCount);
		
		final double unitRotationalInertiaEach = mtrConfig.getUnitRotationalInertia();
		final double unitLongitudinalInertiaEach = mtrConfig.getUnitLongitudinalInertia();
		double Ir=unitRotationalInertiaEach*instanceCount*propMassEach;
		double It=unitLongitudinalInertiaEach*instanceCount*propMassEach;

		if( 1 < instanceCount ){
			// if not on rocket centerline, then add an offset factor, according to the parallel axis theorem: 
			for( Coordinate coord : locations ){
				double distance = Math.hypot( coord.y, coord.z);
				Ir += propMassEach*Math.pow( distance, 2);
			}
		}
		
		return new MassData( curClusterCM, Ir, It);
	}
	
	/**
	 * Calculates mass data of the rocket burnout mass
	 * 
	 * I.O.W., this mass data is invariant during thrust (see also: calculatePropellantMassData(...) )  
	 * 
	 * @param configuration		a given rocket configuration
	 * @return					the CG of the configuration
	 */
	protected MassData calculateBurnoutMassData( final FlightConfiguration config) {
		MassData exceptMotorsMassData = calculateStageData( config);

		MassData motorMassData = calculateMotorBurnoutMassData( config);
			
		return exceptMotorsMassData.add( motorMassData );
	}

	private MassData calculateStageData( final FlightConfiguration config ){
	    MassData componentData = MassData.ZERO_DATA;
       
        // Stages
        for (AxialStage stage : config.getActiveStages()) {
        	int stageNumber = stage.getStageNumber();
               
        	MassData stageData = this.calculateAssemblyMassData( stage );
        	
        	stageMassCache.put(stageNumber, stageData);
        	
        	componentData = componentData.add(stageData);
        }
       
        return componentData;
	}
	

	/**
	 * Compute the burnout mass properties all motors, given a configuration
	 * 
	 * Will calculate data for:MassCalcType.BURNOUT_MASS
	 *  
	 * @param configuration		the rocket configuration
	 * @return					the MassData struct of the motors at burnout
	 */
	private MassData calculateMotorBurnoutMassData(FlightConfiguration config) {	
		
		MassData allMotorData = MassData.ZERO_DATA;
		
		//int motorIndex = 0;
		for (MotorConfiguration mtrConfig : config.getActiveMotors() ) {
			Motor mtr = (Motor) mtrConfig.getMotor();
			MotorMount mount = mtrConfig.getMount();
			
			// use 'mount.getLocations()' because:  
			// 1) includes ALL clustering sources! 
			// 2) location of mount, w/in entire rocket
			// 3) Note: mount.getInstanceCount()  ONLY indicates instancing of the mount's cluster, not parent components (such as stages)
			Coordinate[] locations = mount.getLocations();
			int instanceCount = locations.length; 
			double motorXPosition = mtrConfig.getX();  // location of motor from mount
			
			final double burnoutMassEach = mtr.getBurnoutMass();
			final double burnoutCMx = mtr.getBurnoutCGx(); // CoM from beginning of motor
			
			final Coordinate clusterCM = new Coordinate( locations[0].x + motorXPosition + burnoutCMx, 0, 0, burnoutMassEach*instanceCount);
			
			final double unitRotationalInertia = mtrConfig.getUnitRotationalInertia();
			final double unitLongitudinalInertia = mtrConfig.getUnitLongitudinalInertia();

			double Ir=(unitRotationalInertia*burnoutMassEach)*instanceCount;
			double It=(unitLongitudinalInertia*burnoutMassEach)*instanceCount;
			if( 1 < instanceCount ){
				for( Coordinate coord : locations ){
					double distance_squared = ((coord.y*coord.y) + (coord.z*coord.z));
					double instance_correction = burnoutMassEach*distance_squared;
					
					Ir += instance_correction;
				}
			}
			
			MassData configData = new MassData( clusterCM, Ir, It);
			allMotorData = allMotorData.add( configData );
			
		}
		
		return allMotorData;
	}


	/**
	 * Return the total mass of the motors
	 * 
	 * @param motors			the motor configuration
	 * @param configuration		the current motor instance configuration
	 * @return					the total mass of all motors
	 */
	public double getPropellantMass(SimulationStatus status ){
		return (getPropellantMassData( status )).getCM().weight;
	}
	
	/**
	 * Compute an analysis of the per-component CG's of the provided configuration.
	 * The returned map will contain an entry for each physical rocket component (not stages)
	 * with its corresponding (best-effort) CG.  Overriding of subcomponents is ignored.
	 * The CG of the entire configuration with motors is stored in the entry with the corresponding
	 * Rocket as the key.
	 * 
	 * @param configuration		the rocket configuration
	 * @param type				the state of the motors (none, launch mass, burnout mass)
	 * @return					a map from each rocket component to its corresponding CG.
	 */
	public Map<RocketComponent, Coordinate> getCGAnalysis(FlightConfiguration configuration) {
		revalidateCache(configuration);
		
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
	
	
	/**
	 * Returns the mass and inertia data for this component and all subcomponents.
	 * The inertia is returned relative to the CG, and the CG is in the coordinates
	 * of the specified component, not global coordinates.
	 */
	private MassData calculateAssemblyMassData(RocketComponent component) {
		return calculateAssemblyMassData(component, "....");
	}
	
	private MassData calculateAssemblyMassData(RocketComponent component, String indent) {
		
		Coordinate compCM = component.getComponentCG();
		double compIx = component.getRotationalUnitInertia() * compCM.weight;
		double compIt = component.getLongitudinalUnitInertia() * compCM.weight;
		if( 0 > compCM.weight ){
			throw new BugException("  computed a negative rotational inertia value.");
		}
		if( 0 > compIx ){
			throw new BugException("  computed a negative rotational inertia value.");
		}
		if( 0 > compIt ){
			throw new BugException("  computed a negative longitudinal inertia value.");
		}
		
		if (!component.getOverrideSubcomponents()) {
			if (component.isMassOverridden())
				compCM = compCM.setWeight(MathUtil.max(component.getOverrideMass(), MIN_MASS));
			if (component.isCGOverridden())
				compCM = compCM.setXYZ(component.getOverrideCG());
		}
		
		// default if not instanced (instance count == 1)
		MassData assemblyData = new MassData( compCM, compIx, compIt);
		
		MassData childrenData = MassData.ZERO_DATA;
		// Combine data for subcomponents
		for (RocketComponent child : component.getChildren()) {
			if( child instanceof ParallelStage ){
				// this stage will be tallied separately... skip.
				continue;
			}
			
			// child data, relative to parent's reference frame
			MassData childData = calculateAssemblyMassData(child, indent+"....");
			
			childrenData  = childrenData.add( childData );
		}
		assemblyData = assemblyData.add( childrenData);
		
		// if instanced, adjust children's data too. 
		if ( 1 < component.getInstanceCount() ){
			
			final double curIxx = childrenData.getIxx(); // MOI about x-axis
			final double curIyy = childrenData.getIyy(); // MOI about y axis
			final double curIzz = childrenData.getIzz(); // MOI about z axis
			
			Coordinate templateCM = assemblyData.cm;
			MassData instAccumData = new MassData();  // accumulator for instance MassData
			Coordinate[] instanceLocations = ((Instanceable) component).getInstanceOffsets();
			for( Coordinate curOffset : instanceLocations ){
				Coordinate instanceCM = curOffset.add(templateCM);
				MassData instanceData = new MassData( instanceCM, curIxx, curIyy, curIzz);
				
				// 3) Project the template data to the new CM 
				//    and add to the total
				instAccumData = instAccumData.add( instanceData);
			}
			
			assemblyData = instAccumData;
		}
		
		
		// move to parent's reference point
		assemblyData = assemblyData.move( component.getOffset() );
		if( component instanceof ParallelStage ){
			// hacky correction for the fact Booster Stages aren't direct subchildren to the rocket
			assemblyData = assemblyData.move( component.getParent().getOffset() );
		}
		
		// Override total data
		if (component.getOverrideSubcomponents()) {				
			if (component.isMassOverridden()) {
				double oldMass = assemblyData.getMass();
				double newMass = MathUtil.max(component.getOverrideMass(), MIN_MASS);
				Coordinate newCM = assemblyData.getCM().setWeight(newMass);
				
				double newIxx = assemblyData.getIxx() * newMass / oldMass;
				double newIyy = assemblyData.getIyy() * newMass / oldMass;
				double newIzz = assemblyData.getIzz() * newMass / oldMass;
				
				assemblyData = new MassData( newCM, newIxx, newIyy, newIzz );
			}
			if (component.isCGOverridden()) {
				double oldx = assemblyData.getCM().x;
				double newx = component.getOverrideCGX();
				Coordinate delta = new Coordinate(newx-oldx, 0, 0);
				
				assemblyData = assemblyData.move( delta );
			}
		}
		
		return assemblyData;
	}
	
	
	public void revalidateCache( final SimulationStatus status ){
		rocketSpentMassCache = calculateBurnoutMassData( status.getConfiguration() );
			
		propellantMassCache = calculatePropellantMassData( status); 		
	}
	
	public void revalidateCache( final FlightConfiguration config ){
		checkCache( config);
		if( null == rocketSpentMassCache ){
			rocketSpentMassCache = calculateBurnoutMassData(config);
		}
		if( null == propellantMassCache ){
			propellantMassCache = calculatePropellantMassData( config);
		}
	}
	
	/**
	 * Check the current cache consistency.  This method must be called by all
	 * methods that may use any cached data before any other operations are
	 * performed.  If the rocket has changed since the previous call to
	 * <code>checkCache()</code>, then {@link #voidMassCache()} is called.
	 * <p>
	 * This method performs the checking based on the rocket's modification IDs,
	 * so that these method may be called from listeners of the rocket itself.
	 * 
	 * @param	configuration	the configuration of the current call
	 */
	protected final boolean checkCache(FlightConfiguration configuration) {
		if (rocketMassModID != configuration.getRocket().getMassModID() ||
				rocketTreeModID != configuration.getRocket().getTreeModID()) {
			rocketMassModID = configuration.getRocket().getMassModID();
			rocketTreeModID = configuration.getRocket().getTreeModID();
			voidMassCache();
			return false;
		}
		return true;
	}
	
	/**
	 * Void cached mass data.  This method is called whenever a change occurs in 
	 * the rocket structure that affects the mass of the rocket and when a new 
	 * Rocket is used.  This method must be overridden to void any cached data 
	 * necessary.  The method must call <code>super.voidMassCache()</code> during 
	 * its execution.
	 */
	protected void voidMassCache() {
		this.stageMassCache.clear();
		this.rocketSpentMassCache=null;
		this.propellantMassCache=null;
	}
	
	
	@Override
	public int getModID() {
		return 0;
	}
	
}
