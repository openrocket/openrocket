package net.sf.openrocket.masscalc;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.Instanceable;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;

public class MassCalculator implements Monitorable {
	
	public static enum MassCalcType {
		NO_MOTORS {
			@Override
			public Coordinate getCG(Motor motor) {
				return Coordinate.NUL;
			}
		},
		LAUNCH_MASS {
			@Override
			public Coordinate getCG(Motor motor) {
				return motor.getLaunchCG();
			}
		},
		BURNOUT_MASS {
			@Override
			public Coordinate getCG(Motor motor) {
				return motor.getEmptyCG();
			}
		};
		
		public abstract Coordinate getCG(Motor motor);
	}
	
	private static final Logger log = LoggerFactory.getLogger(MassCalculator.class);
	
	public static final double MIN_MASS = 0.001 * MathUtil.EPSILON;
	
	private int rocketMassModID = -1;
	private int rocketTreeModID = -1;
	
	
	/*
	 * Cached data.  All CG data is in absolute coordinates.  All moments of inertia
	 * are relative to their respective CG.
	 */
	private HashMap< Integer, MassData> cache = new HashMap<Integer, MassData >(); 
//	private MassData dryData = null;
//	private MassData launchData = null;
//	private Vector< MassData> motorData =  new Vector<MassData>(); 
	
	// this turns on copious amounts of debug.  Recommend leaving this false 
	// until reaching code that causes troublesome conditions.
	public boolean debug = false; 
	
	//////////////////  Constructors ///////////////////
	public MassCalculator() {
	}
	
	//////////////////  Mass property calculations  ///////////////////

	
	/**
	 * Return the CG of the rocket with the specified motor status (no motors,
	 * ignition, burnout).
	 * 
	 * @param configuration		the rocket configuration
	 * @param type				the state of the motors (none, launch mass, burnout mass)
	 * @return					the CG of the configuration
	 */
	public Coordinate getCG(FlightConfiguration configuration, MassCalcType type) {
		return getCM( configuration, type);
	}
	
	public Coordinate getCM(FlightConfiguration config, MassCalcType type) {
		checkCache(config);
		calculateStageCache(config);
		
		// Stage contribution
		Coordinate dryCM = Coordinate.ZERO;
		for (AxialStage stage : config.getActiveStages()) {
			Integer stageNumber = stage.getStageNumber();
			MassData stageData = cache.get( stageNumber);
			if( null == stageData ){
				throw new BugException("method: calculateStageCache(...) is faulty-- returned null data for an active stage: "+stage.getName()+"("+stage.getStageNumber()+")");
			}
			dryCM = stageData.cm.average(dryCM);
			
		}

		
		Coordinate totalCM=null;
		if( MassCalcType.NO_MOTORS == type ){
			totalCM = dryCM;
		}else{
			MassData motorData = getMotorMassData(config, type);
			Coordinate motorCM = motorData.getCM();
			totalCM = dryCM.average(motorCM);
		}
		
		return totalCM;
	}
	
	/**
	 * Compute the CM of all motors, given a configuration and type
	 * 
	 * @param configuration		the rocket configuration
	 * @param motors			the motor configuration
	 * @return					the CG of the configuration
	 */
	public MassData getMotorMassData(FlightConfiguration config, MassCalcType type) {
		if( MassCalcType.NO_MOTORS == type ){
			return MassData.ZERO_DATA;
		}
		
//		// vvvv DEVEL vvvv
//		//String massFormat = "    [%2s]: %-16s    %6s x %6s  =  %6s += %6s  @ (%s, %s, %s )";
//		String inertiaFormat = "    [%2s](%2s): %-16s    %6s  %6s";
//		if( debug){
//			System.err.println("====== ====== getMotorMassData( config:"+config.toDebug()+", type: "+type.name()+") ====== ====== ====== ====== ====== ======");
//			//System.err.println(String.format(massFormat, " #", "<Designation>","Mass","Count","Config","Sum", "x","y","z"));
//			System.err.println(String.format(inertiaFormat, " #","ct", "<Designation>","I_ax","I_tr"));
//		}
//		// ^^^^ DEVEL ^^^^

		MassData allMotorData = MassData.ZERO_DATA;
		//int motorIndex = 0;
		for (MotorConfiguration mtrConfig : config.getActiveMotors() ) {
			ThrustCurveMotor mtr = (ThrustCurveMotor) mtrConfig.getMotor();
			
			MotorMount mount = mtrConfig.getMount();
			RocketComponent mountComp = (RocketComponent)mount;
			Coordinate[] locations = mountComp.getLocations(); // location of mount, w/in entire rocket
			int instanceCount = locations.length; 
			double motorXPosition = mtrConfig.getX();  // location of motor from mount
			
			Coordinate localCM = type.getCG( mtr );  // CoM from beginning of motor
			localCM = localCM.setWeight( localCM.weight * instanceCount);
			// a *bit* hacky :P
			Coordinate curMotorCM = localCM.setX( localCM.x + locations[0].x + motorXPosition );
			
			double motorMass = curMotorCM.weight;
			double Ir_single = mtrConfig.getUnitRotationalInertia()*motorMass;
			double It_single = mtrConfig.getUnitLongitudinalInertia()*motorMass;
			double Ir=0;
			double It=0;
			if( 1 == instanceCount ){
				Ir=Ir_single;
				It=It_single;	
			}else{
				It = It_single * instanceCount;
			
				Ir = Ir_single*instanceCount;
				// these need more complex instancing code...
				for( Coordinate coord : locations ){
					double distance = Math.hypot( coord.y, coord.z);
					Ir += motorMass*Math.pow( distance, 2);
				}
			}
			
			MassData configData = new MassData( curMotorCM, Ir, It);
			allMotorData = allMotorData.add( configData );
			
			// BEGIN DEVEL
			//if( debug){
				// // Inertia
				// System.err.println(String.format( inertiaFormat, motorIndex, instanceCount, mtr.getDesignation(), Ir, It));
				// // mass only
				//double singleMass = type.getCG( mtr ).weight;
				//System.err.println(String.format( massFormat, motorIndex, mtr.getDesignation(), 
				//		singleMass, instanceCount, curMotorCM.weight, allMotorData.getMass(),curMotorCM.x, curMotorCM.y, curMotorCM.z ));
			//}
			//motorIndex++;
			// END DEVEL	
		}
	
		return allMotorData;
	}
	
	/**
	 * Return the longitudinal inertia of the rocket with the specified motor instance
	 * configuration.
	 * 
	 * @param config		the current motor instance configuration
	 * @param type 				the type of analysis to pull
	 * @return					the longitudinal inertia of the rocket
	 */
	public double getLongitudinalInertia(FlightConfiguration config, MassCalcType type) {
		checkCache(config);
		calculateStageCache(config);
		
		MassData structureData = MassData.ZERO_DATA; 
		
		// Stages
		for (AxialStage stage : config.getActiveStages()) {
			int stageNumber = stage.getStageNumber();
			
			MassData stageData = cache.get(stageNumber);
			structureData = structureData.add(stageData);
		}
		
		MassData motorData = MassData.ZERO_DATA;
		if( MassCalcType.NO_MOTORS != type ){
			motorData = getMotorMassData(config, type);
		}
		

		MassData totalData = structureData.add( motorData);
		if(debug){
			System.err.println(String.format("  >> Structural MassData: %s", structureData.toDebug()));	
			System.err.println(String.format("  >> Motor MassData:      %s", motorData.toDebug()));	
			System.err.println(String.format("==>> Combined MassData:   %s", totalData.toDebug()));	
		}
		
		return totalData.getLongitudinalInertia();
	}
	
	
	/**
	 * Compute the rotational inertia of the provided configuration with specified motors.
	 * 
	 * @param config		the current motor instance configuration
	 * @param type				the type of analysis to get
	 * @return					the rotational inertia of the configuration
	 */
	public double getRotationalInertia(FlightConfiguration config, MassCalcType type) {
		checkCache(config);
		calculateStageCache(config);
		
		MassData structureData = MassData.ZERO_DATA;
		
		// Stages
		for (AxialStage stage : config.getActiveStages()) {
			int stageNumber = stage.getStageNumber();
			
			MassData stageData = cache.get(stageNumber);
			structureData = structureData.add(stageData);
		}
		
		MassData motorData = MassData.ZERO_DATA;
		if( MassCalcType.NO_MOTORS != type ){
			motorData = getMotorMassData(config, type);
		}
		
		MassData totalData = structureData.add( motorData);
		if(debug){
			System.err.println(String.format("  >> Structural MassData: %s", structureData.toDebug()));	
			System.err.println(String.format("  >> Motor MassData:      %s", motorData.toDebug()));	
			System.err.println(String.format("==>> Combined MassData:   %s", totalData.toDebug()));	
		}
		
		return totalData.getRotationalInertia();
	}
	
	
	/**
	 * Return the total mass of the motors
	 * 
	 * @param motors			the motor configuration
	 * @param configuration		the current motor instance configuration
	 * @return					the total mass of all motors
	 */
	public double getPropellantMass(FlightConfiguration configuration, MassCalcType calcType ){
		double mass = 0;
		//throw new BugException("getPropellantMass is not yet implemented.... ");
		// add up the masses of all motors in the rocket
		if ( MassCalcType.NO_MOTORS != calcType ){
			for (MotorConfiguration curConfig : configuration.getActiveMotors()) {
				int instanceCount = curConfig.getMount().getInstanceCount();
				mass = mass + curConfig.getPropellantMass()*instanceCount;
			}
		}
		return mass;
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
	public Map<RocketComponent, Coordinate> getCGAnalysis(FlightConfiguration configuration, MassCalcType type) {
		checkCache(configuration);
		calculateStageCache(configuration);
		
		Map<RocketComponent, Coordinate> map = new HashMap<RocketComponent, Coordinate>();
		
		for (RocketComponent comp : configuration.getActiveComponents()) {
			Coordinate[] cgs = comp.toAbsolute(comp.getCG());
			Coordinate totalCG = Coordinate.NUL;
			for (Coordinate cg : cgs) {
				totalCG = totalCG.average(cg);
			}
			map.put(comp, totalCG);
		}
		
		map.put(configuration.getRocket(), getCG(configuration, type));
		
		return map;
	}
	
	////////  Cache computations  ////////
	
	private void calculateStageCache(FlightConfiguration config) {
		int stageCount = config.getActiveStageCount();
		if(debug){
			System.err.println(">> Calculating massData cache for config: "+config.toDebug()+"  with "+stageCount+" stages");
		}
		if( 0 < stageCount ){ 
			for( AxialStage curStage : config.getActiveStages()){
				int index = curStage.getStageNumber();
				MassData stageData = calculateAssemblyMassData( curStage);
				cache.put(index, stageData);
			}
		}
		
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
		
		Coordinate parentCM = component.getComponentCG();
		double parentIx = component.getRotationalUnitInertia() * parentCM.weight;
		double parentIt = component.getLongitudinalUnitInertia() * parentCM.weight;
		MassData parentData = new MassData( parentCM, parentIx, parentIt);
		
		if (!component.getOverrideSubcomponents()) {
			if (component.isMassOverridden())
				parentCM = parentCM.setWeight(MathUtil.max(component.getOverrideMass(), MIN_MASS));
			if (component.isCGOverridden())
				parentCM = parentCM.setXYZ(component.getOverrideCG());
		}
		if(( debug) &&( 0 < component.getChildCount()) && (MIN_MASS < parentCM.weight)){
			System.err.println(String.format("%-32s: %s ",indent+">>["+ component.getName()+"]", parentData.toDebug() ));
		}
		
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

		
		MassData resultantData = parentData; // default if not instanced
		// compensate for component-instancing propogating to children's data 
		int instanceCount = component.getInstanceCount();
		boolean hasChildren = ( 0 < component.getChildCount());
		if (( 1 < instanceCount )&&( hasChildren )){
//			if(( debug )){
//				System.err.println(String.format("%s  Found instanceable with %d children: %s (t= %s)", 
//						indent, component.getInstanceCount(), component.getName(), component.getClass().getSimpleName() ));
//			}
			
			final double curIxx = childrenData.getIxx(); // MOI about x-axis
			final double curIyy = childrenData.getIyy(); // MOI about y axis
			final double curIzz = childrenData.getIzz(); // MOI about z axis
			
			Coordinate eachCM = childrenData.cm;
			MassData instAccumData = new MassData();  // accumulator for instance MassData
			Coordinate[] instanceLocations = ((Instanceable) component).getInstanceOffsets();
         	for( Coordinate curOffset : instanceLocations ){
//         		if( debug){
//         			//System.err.println(String.format("%-32s: %s", indent+"  inst Accum", instAccumData.toCMDebug() ));
//         			System.err.println(String.format("%-32s: %s", indent+"  inst Accum", instAccumData.toDebug() ));
//				}
         		
				Coordinate instanceCM = curOffset.add(eachCM);
				
				MassData instanceData = new MassData( instanceCM, curIxx, curIyy, curIzz);
				
				// 3) Project the template data to the new CM 
				//    and add to the total
				instAccumData = instAccumData.add( instanceData);
			}
			
         	childrenData = instAccumData;
		}

		// combine the parent's and children's data
		resultantData = parentData.add( childrenData);
		
		if( debug){
			System.err.println(String.format("%-32s: %s ", indent+"<==>["+component.getName()+"][asbly]", resultantData.toDebug()));
		}

		// move to parent's reference point
		resultantData = resultantData.move( component.getOffset() );
		if( component instanceof ParallelStage ){
			// hacky correction for the fact Booster Stages aren't direct subchildren to the rocket
			resultantData = resultantData.move( component.getParent().getOffset() );
		}
		
		// Override total data
		if (component.getOverrideSubcomponents()) {
			if( debug){
				System.err.println(String.format("%-32s: %s ", indent+"vv["+component.getName()+"][asbly]", resultantData.toDebug()));
			}
			if (component.isMassOverridden()) {
				double oldMass = resultantData.getMass();
				double newMass = MathUtil.max(component.getOverrideMass(), MIN_MASS);
				Coordinate newCM = resultantData.getCM().setWeight(newMass);
				
				double newIxx = resultantData.getIxx() * newMass / oldMass;
				double newIyy = resultantData.getIyy() * newMass / oldMass;
				double newIzz = resultantData.getIzz() * newMass / oldMass;

				resultantData = new MassData( newCM, newIxx, newIyy, newIzz );
			}
			if (component.isCGOverridden()) {
				double oldx = resultantData.getCM().x;
				double newx = component.getOverrideCGX();
				Coordinate delta = new Coordinate(newx-oldx, 0, 0);
				if(debug){
					System.err.println(String.format("%-32s: x: %g => %g  (%g)", indent+"    88", oldx, newx, delta.x)); 
				}
				resultantData = resultantData.move( delta );
			}
		}
		
		if( debug){
			System.err.println(String.format("%-32s: %s ", indent+"<<["+component.getName()+"][asbly]", resultantData.toDebug()));
		}
				
		
		return resultantData;
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
	protected final void checkCache(FlightConfiguration configuration) {
		if (rocketMassModID != configuration.getRocket().getMassModID() ||
				rocketTreeModID != configuration.getRocket().getTreeModID()) {
			rocketMassModID = configuration.getRocket().getMassModID();
			rocketTreeModID = configuration.getRocket().getTreeModID();
			log.debug("Voiding the mass cache");
			voidMassCache();
		}
	}
	
	/**
	 * Void cached mass data.  This method is called whenever a change occurs in 
	 * the rocket structure that affects the mass of the rocket and when a new 
	 * Rocket is used.  This method must be overridden to void any cached data 
	 * necessary.  The method must call <code>super.voidMassCache()</code> during 
	 * its execution.
	 */
	protected void voidMassCache() {
		this.cache.clear();
	}
	
	
	
	@Override
	public int getModID() {
		return 0;
	}
	
}
