package net.sf.openrocket.masscalc;

import java.util.ArrayList;
import java.util.Map;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Transformation;

/**
 * 
 * @author teyrana (aka Daniel Williams) <equipoise@gmail.com> 
 *
 */
public class MassCalculation {

	/**
	 * NOTE:  Multiple enums may map to the same settings.  This allows a caller to use the
	 *     enum which best matches their use case.
	 */
	public enum Type {
		// no motor data, even if the configuration contains active engines
		STRUCTURE(     true,  false, false ),
		
		// n.b.  'motor-mass' calculations are to be preferred over 'propellant-mass' calculations because they are computationally simpler:
		//       not only are they faster, but *slightly* more accurate because of fewer calculation rounding errors
		MOTOR(        false,  true,  true ),
		
		BURNOUT(       true,  true, false ),
		
		LAUNCH(        true,  true,  true );
		
		public final boolean includesStructure;
		public final boolean includesMotorCasing;
		public final boolean includesPropellant;
		
		Type( double simulationTime ) {
			includesStructure = false;
			includesMotorCasing = true;
			includesPropellant = true;
		}
		
	    Type( boolean include_structure, boolean include_casing, boolean include_prop) {
			this.includesStructure = include_structure;
			this.includesMotorCasing = include_casing;
			this.includesPropellant = include_prop;
		}   
	}
	
	// =========== Instance Functions ========================
	
	public void merge( final MassCalculation other ) {
		if( MIN_MASS < other.getMass()) {
			// Adjust Center-of-mass
			this.addMass( other.getCM() );
			this.bodies.addAll( other.bodies );
		}
	}

	public void addInertia( final RigidBody data ) {
		this.bodies.add( data );
	}
	
	public void addMass( final Coordinate pointMass ) {
		if( MIN_MASS > this.centerOfMass.weight ){
		    this.centerOfMass = pointMass;
		}else {
			this.centerOfMass = this.centerOfMass.average( pointMass);
		}
	}
	
	public MassCalculation copy( final RocketComponent _root, final Transformation _transform){
		return new MassCalculation( this.type, this.config, this.simulationTime, _root, _transform, this.analysisMap);
	}
		
	public Coordinate getCM() {
		return this.centerOfMass;
	}
	
	public double getMass() {
		return this.centerOfMass.weight;
	}
	
	public double getLongitudinalInertia() {
		return this.inertia.Iyy;
	}
	
	public double getRotationalInertia() {
		return this.inertia.Ixx;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		MassCalculation other = (MassCalculation) obj;
		return ((this.centerOfMass.equals(other.centerOfMass)) 
				&& (this.config.equals( other.config))
				&& (this.simulationTime == other.simulationTime)
				&& (this.type == other.type) );
	}
	
	@Override
	public int hashCode() {
		return (int) (this.centerOfMass.hashCode());
	}

	public MassCalculation( final Type _type, final FlightConfiguration _config, final double _time,
							final RocketComponent _root, final Transformation _transform,
							Map<Integer, CMAnalysisEntry> _map)
	{
		type = _type;
		config = _config;
		simulationTime = _time;
		root = _root;
		transform = _transform;
		analysisMap = _map;

		reset();
	}
	
	public void setCM( final Coordinate newCM ) {
		this.centerOfMass = newCM;
	}	

	public void reset(){
		centerOfMass = Coordinate.ZERO;
		inertia = RigidBody.EMPTY;
		bodies.clear();
	}
	
	public int size() {
		return this.bodies.size();
	}
	
	public String toCMDebug(){ 
		return String.format("cm= %.6fg@[%.6f,%.6f,%.6f]", centerOfMass.weight, centerOfMass.x, centerOfMass.y, centerOfMass.z);
	}

//	public String toMOIDebug(){
//		return String.format("I_cm=[ %.8f, %.8f, %.8f ]", inertia.getIxx(), inertia.getIyy(), inertia.getIzz() ));	 
//	}

	@Override
	public String toString() {
		return this.toCMDebug();
	}

	
	// =========== Instance Member Variables ========================
	
	private static final double MIN_MASS = MathUtil.EPSILON;

	// === package-private ===
	final FlightConfiguration config;
	final double simulationTime;
	final RocketComponent root;
	final Transformation transform;
	final Type type;
	
	// center-of-mass only.
	Coordinate centerOfMass = Coordinate.ZERO;
	
	// center-of-mass AND moment-of-inertia data.
	RigidBody inertia = RigidBody.EMPTY;
	
	// center-of-mass AND moment-of-inertia data.
	final ArrayList<RigidBody> bodies = new ArrayList<RigidBody>();

	String prefix = "";

	Map<Integer, CMAnalysisEntry> analysisMap;

	// =========== Private Instance Functions ========================

	private MassCalculation calculateMountData(){
		if( ! config.isComponentActive(this.root)) {
			return this;
		}
		
		final MotorMount mount = (MotorMount)root;
		MotorConfiguration motorConfig = mount.getMotorConfig( config.getId() );
		final Motor motor = motorConfig.getMotor();
		if( motorConfig.isEmpty() ){
			return this;
		}


		final double mountXPosition = root.getPosition().x;
		
		final int instanceCount = root.getInstanceCount();

		final double motorXPosition = motorConfig.getX();  // location of motor from mount
		final Coordinate[] offsets = root.getInstanceOffsets();
		
		double eachMass;
		double eachCMx;  // CoM from beginning of motor
		
		if ( this.type.includesMotorCasing && this.type.includesPropellant ){
			eachMass = motor.getTotalMass( simulationTime );
			eachCMx = motor.getCMx( simulationTime);
		}else if( this.type.includesMotorCasing ) {
			eachMass = motor.getTotalMass( Motor.PSEUDO_TIME_BURNOUT );
			eachCMx = motor.getCMx( Motor.PSEUDO_TIME_BURNOUT );
		} else {
			final double eachMotorMass = motor.getTotalMass( simulationTime );
			final double eachMotorCMx = motor.getCMx( simulationTime); // CoM from beginning of motor
			final double eachCasingMass = motor.getBurnoutMass();
			final double eachCasingCMx = motor.getBurnoutCGx();
			
			eachMass = eachMotorMass - eachCasingMass;
			eachCMx = (eachMotorCMx*eachMotorMass - eachCasingCMx*eachCasingMass)/eachMass;
		}

//		System.err.println(String.format("%-40s|Motor: %s....  Mass @%f = %.6f", prefix, motorConfig.toDescription(), simulationTime, eachMass ));


		// coordinates in rocket frame; Ir, It about CoM.
		final Coordinate clusterLocalCM = new Coordinate( mountXPosition + motorXPosition + eachCMx, 0, 0, eachMass*instanceCount);
		
		double clusterBaseIr = motorConfig.getUnitRotationalInertia()*instanceCount*eachMass;
		
		double clusterIt = motorConfig.getUnitLongitudinalInertia()*instanceCount*eachMass;
		
		// if more than 1 moter => motors are not an the centerline => adjust via parallel-axis theorem
		double clusterIr = clusterBaseIr; 
		if( 1 < instanceCount ){
			for( Coordinate coord : offsets ){
				double distance = Math.hypot( coord.y, coord.z);
				clusterIr += eachMass*Math.pow( distance, 2);
			}
		}
		
		final Coordinate clusterCM = transform.transform( clusterLocalCM  );
		addMass( clusterCM );

		if(null != this.analysisMap) {
			CMAnalysisEntry entry = analysisMap.get(motor.getDesignation().hashCode());
			if (null == entry){
				entry = new CMAnalysisEntry(motor);
				analysisMap.put(motor.getDesignation().hashCode(), entry);
			}
			entry.updateEachMass(eachMass);
			entry.updateAverageCM(clusterCM);
		}

		RigidBody clusterMOI = new RigidBody( clusterCM, clusterIr, clusterIt, clusterIt );
		addInertia( clusterMOI );
		
		return this;
	}
	
	/**
	 * Returns the mass and inertia data for this component and all subcomponents.
	 * The inertia is returned relative to the CG, and the CG is in the coordinates
	 * of the specified component, not global coordinates.
	 *
	 */
	/* package-scope */ MassCalculation calculateAssembly(){
		
		if (this.type.includesStructure) {
			MassCalculation structureCalc = this.copy(this.root, this.transform);
			structureCalc.calculateStructure();
			this.merge(structureCalc);
		}

		if (this.type.includesMotorCasing || this.type.includesPropellant) {
			MassCalculation motorCalc = this.copy(this.root, this.transform);
			motorCalc.calculateMotors();
			this.merge(motorCalc);
		}

		return this;
	}

	MassCalculation calculateStructure() {
		final RocketComponent component = this.root;
		final Transformation parentTransform = this.transform;
		final int instanceCount = component.getInstanceCount();
		final Coordinate[] allInstanceOffsets = component.getInstanceLocations();
		final double[] allInstanceAngles = component.getInstanceAngles();

		// // vvv DEBUG
		// if( this.config.isComponentActive(component) ){
		// 	System.err.println(String.format( "%s>>[%s]....", prefix, component.getName()));
		// }

		if(null != analysisMap) {
			if (this.config.isComponentActive(component) && (! analysisMap.containsKey(component.hashCode()))){
				CMAnalysisEntry entry = new CMAnalysisEntry(component);
				analysisMap.put(component.hashCode(), entry);
			}
		}

		// iterate over the aggregated instances for the whole tree.
		MassCalculation children = this.copy(component, parentTransform );
		for( int currentInstanceNumber = 0; currentInstanceNumber < instanceCount; ++currentInstanceNumber) {
			final Coordinate currentInstanceOffset = allInstanceOffsets[currentInstanceNumber];
			final Transformation offsetTransform = Transformation.getTranslationTransform( currentInstanceOffset );

			final double currentInstanceAngle = allInstanceAngles[currentInstanceNumber];
			final Transformation angleTransform = Transformation.getAxialRotation(currentInstanceAngle);

			final Transformation currentTransform = parentTransform.applyTransformation(offsetTransform)
														.applyTransformation(angleTransform);

			for (RocketComponent child : component.getChildren()) {
				// child data, relative to rocket reference frame
				MassCalculation eachChild = copy(child, currentTransform);
				
				eachChild.prefix = prefix + "....";
				eachChild.calculateStructure(); 
				
				// accumulate children's data
				children.merge( eachChild );
			}
		}
		
		if( MIN_MASS < children.getMass() ) {
			this.merge( children );
			// // vvv DEBUG
			// System.err.println(String.format( "%s....assembly mass (incl/children):  %s", prefix, this.toCMDebug()));
		}
		
		if (this.config.isComponentActive(component) ){
			Coordinate compCM = component.getComponentCG();

			if (!component.getOverrideSubcomponents()) {
				if (component.isMassOverridden()) {
					compCM = compCM.setWeight(MathUtil.max(component.getOverrideMass(), MIN_MASS));
					// // vvv DEBUG
					// System.err.println(String.format( "%s....mass overridden to:  %s", prefix, compCM.toPreciseString()));
				}
				if (component.isCGOverridden())
					compCM = compCM.setXYZ(component.getOverrideCG());
			}

			// mass data for *this component only* in the rocket-frame
			compCM = parentTransform.transform( compCM.add(component.getPosition()) );
			this.addMass( compCM );

			if(null != analysisMap){
				if( component instanceof ComponentAssembly) {
					// For ComponentAssemblies, record the _assembly_ information
					CMAnalysisEntry entry = analysisMap.get(component.hashCode());
					entry.updateEachMass(children.getMass() / component.getInstanceCount());
					entry.updateAverageCM(this.centerOfMass);
				}else{
					// For actual components, record the mass of the component, and disregard children
					CMAnalysisEntry entry = analysisMap.get(component.hashCode());
					entry.updateEachMass(compCM.weight);
					entry.updateAverageCM(compCM);
				}
			}
			
			double compIx = component.getRotationalUnitInertia() * compCM.weight;
			double compIt = component.getLongitudinalUnitInertia() * compCM.weight;
			RigidBody componentInertia = new RigidBody( compCM, compIx, compIt, compIt );
			
			this.addInertia( componentInertia );
			// // vvv DEBUG
			// if( 0 < compCM.weight ) {
			// 	System.err.println(String.format( "%s....componentData:            %s", prefix, compCM.toPreciseString() ));
			// }

			if (component.getOverrideSubcomponents()) {
				if (component.isMassOverridden()) {
					double newMass = MathUtil.max(component.getOverrideMass(), MIN_MASS);
					Coordinate newCM = this.getCM().setWeight( newMass );
					this.setCM( newCM );
				}
				
				if (component.isCGOverridden()) {
					Coordinate newCM = this.getCM().setX( component.getOverrideCGX() ); 
					this.setCM( newCM );
				}
			}
		}
		
		// // vvv DEBUG
		// if( this.config.isComponentActive(component) && 0 < this.getMass() ) {
		// 	System.err.println(String.format( "%s....<< return data @ %s:   %s", prefix, component.getName(), this.toCMDebug() ));
		// }
		// // ^^^ DEBUG
		
		return this;
	}

	MassCalculation calculateMotors() {
		final RocketComponent component = this.root;
		final Transformation parentTransform = this.transform;
		
		final int instanceCount = component.getInstanceCount();
		Coordinate[] instanceLocations = component.getInstanceLocations();

//		// vvv DEBUG
//		if( this.config.isComponentActive(component) ){
//			System.err.println(String.format( "%s[%s]....", prefix, component.getName()));
//		}

		if (component.isMotorMount()) {
			MassCalculation propellant = this.copy(component, parentTransform);
			
			propellant.calculateMountData();

			this.merge( propellant );

//			// vvv DEBUG
//			if( 0 < propellant.getMass() ) {
//				System.err.println(String.format( "%s........++ propellantData: %s", prefix, propellant.toCMDebug()));
//			}

		}
		
		// iterate over the aggregated instances for the whole tree.
		MassCalculation children = this.copy(component, parentTransform );
		for( int instanceNumber = 0; instanceNumber < instanceCount; ++instanceNumber) {
			Coordinate currentLocation = instanceLocations[instanceNumber];
			Transformation currentTransform = parentTransform.applyTransformation( Transformation.getTranslationTransform( currentLocation ));
			
			for (RocketComponent child : component.getChildren()) {
				// child data, relative to rocket reference frame
				MassCalculation eachChild = copy( child, currentTransform);
				
				eachChild.prefix = prefix + "....";
				eachChild.calculateMotors(); 
				
				// accumulate children's data
				children.merge( eachChild );
			}
		}
		
		if( MIN_MASS < children.getMass() ) {
			this.merge( children );
			//System.err.println(String.format( "%s....assembly mass (incl/children):  %s", prefix, this.toCMDebug()));
		}
		
//		// vvv DEBUG
//		if( this.config.isComponentActive(component) && 0 < this.getMass() ) {
//			System.err.println(String.format( "%s....<< return assemblyData:   %s (tree @%s)", prefix, this.toCMDebug(), component.getName() ));
//		}
//      // ^^^ DEBUG
		
		return this;
	}
	
	/** 
	 * MOI Calculation needs to be a two-step process:
	 * (1) calculate overall Center-of-Mass (CM) first (down inline with data-gathering)
	 * (2) Move MOIs to CM via parallel axis theorem (this method)
	 *
	 * @return freshly calculated Moment-of-Inertia matrix
	 */
	/* package-scope */ RigidBody calculateMomentOfInertia() {
		double Ir=0, It=0;
		for( final RigidBody eachLocal : this.bodies ){
			final RigidBody eachGlobal = eachLocal.rebase( this.centerOfMass );
			Ir += eachGlobal.Ixx;
			It += eachGlobal.Iyy;
		}
		
		return new RigidBody( centerOfMass, Ir, It, It );
	}

}

