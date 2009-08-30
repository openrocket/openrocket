package net.sf.openrocket.aerodynamics;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.Iterator;
import java.util.Map;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


/**
 * A class that is the base of all aerodynamical calculations.
 * <p>
 * A {@link Configuration} object must be assigned to this class before any 
 * operations are allowed.  This can be done using the constructor or using 
 * the {@link #setConfiguration(Configuration)} method.  The default is a
 * <code>null</code> configuration, in which case the calculation
 * methods throw {@link NullPointerException}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public abstract class AerodynamicCalculator {
	
	private static final double MIN_MASS = 0.001 * MathUtil.EPSILON;

	/** Number of divisions used when calculating worst CP. */
	public static final int DIVISIONS = 360;
	
	/**
	 * A <code>WarningSet</code> that can be used if <code>null</code> is passed
	 * to a calculation method.
	 */
	protected WarningSet ignoreWarningSet = new WarningSet();
	
	/**
	 * The <code>Rocket</code> currently being calculated.
	 */
	protected Rocket rocket = null;
	
	protected Configuration configuration = null;
	
	
	/*
	 * Cached data.  All CG data is in absolute coordinates.  All moments of inertia
	 * are relative to their respective CG.
	 */
	private Coordinate[] cgCache = null;
	private Coordinate[] origCG = null;  // CG of non-overridden stage
	private double longitudalInertiaCache[] = null;
	private double rotationalInertiaCache[] = null;
	
	
	// TODO: LOW: Do not void unnecessary data (mass/aero separately)
	private int rocketModID = -1;
//	private int aeroModID = -1;
//	private int massModID = -1;

	/**
	 * No-options constructor.  The rocket is left as <code>null</code>.
	 */
	public AerodynamicCalculator() {
		
	}
	
	
	
	/**
	 * A constructor that sets the Configuration to be used.
	 * 
	 * @param config  the configuration to use
	 */
	public AerodynamicCalculator(Configuration config) {
		setConfiguration(config);
	}

	
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(Configuration config) {
		this.configuration = config;
		this.rocket = config.getRocket();
	}
	
	
	public abstract AerodynamicCalculator newInstance();
	
	
	//////////////////  Mass property calculations  ///////////////////
	
	
	/**
	 * Get the CG and mass of the current configuration with motors at the specified
	 * time.  The motor ignition times are taken from the configuration.
	 */
	public Coordinate getCG(double time) {
		Coordinate totalCG;
		
		totalCG = getEmptyCG();
		
		Iterator<MotorMount> iterator = configuration.motorIterator();
		while (iterator.hasNext()) {
			MotorMount mount = iterator.next();
			double ignition = configuration.getIgnitionTime(mount);
			Motor motor = mount.getMotor(configuration.getMotorConfigurationID());
			RocketComponent component = (RocketComponent) mount;
		
			double position = (component.getLength() - motor.getLength() 
					+ mount.getMotorOverhang());
			
			for (Coordinate c: component.toAbsolute(motor.getCG(time-ignition).
					add(position,0,0))) {
				totalCG = totalCG.average(c);
			}
		}
		
		return totalCG;
	}
	
	
	/**
	 * Get the CG and mass of the current configuration without motors.
	 * 
	 * @return			the CG of the configuration
	 */
	public Coordinate getEmptyCG() {
		checkCache();
		
		if (cgCache == null) {
			calculateStageCache();
		}
		
		Coordinate totalCG = null;
		for (int stage: configuration.getActiveStages()) {
			totalCG = cgCache[stage].average(totalCG);
		}
		
		if (totalCG == null)
			totalCG = Coordinate.NUL;
		
		return totalCG;
	}
	
	
	/**
	 * Return the longitudal inertia of the current configuration with motors at 
	 * the specified time.  The motor ignition times are taken from the configuration.
	 * 
	 * @param time		the time.
	 * @return			the longitudal moment of inertia of the configuration.
	 */
	public double getLongitudalInertia(double time) {
		checkCache();
		
		if (cgCache == null) {
			calculateStageCache();
		}
		
		final Coordinate totalCG = getCG(time);
		double totalInertia = 0;
		
		// Stages
		for (int stage: configuration.getActiveStages()) {
			Coordinate stageCG = cgCache[stage];
			
			totalInertia += (longitudalInertiaCache[stage] + 
					stageCG.weight * MathUtil.pow2(stageCG.x - totalCG.x));
		}
		
		
		// Motors
		Iterator<MotorMount> iterator = configuration.motorIterator();
		while (iterator.hasNext()) {
			MotorMount mount = iterator.next();
			double ignition = configuration.getIgnitionTime(mount);
			Motor motor = mount.getMotor(configuration.getMotorConfigurationID());
			RocketComponent component = (RocketComponent) mount;
		
			double position = (component.getLength() - motor.getLength() 
					+ mount.getMotorOverhang());
			
			double inertia = motor.getLongitudalInertia(time - ignition);
			for (Coordinate c: component.toAbsolute(motor.getCG(time-ignition).
					add(position,0,0))) {
				totalInertia += inertia + c.weight * MathUtil.pow2(c.x - totalCG.x);
			}
		}
		
		return totalInertia;
	}
	
	
	/**
	 * Return the rotational inertia of the configuration with motors at the specified time.
	 * The motor ignition times are taken from the configuration.
	 * 
	 * @param time		the time.
	 * @return			the rotational moment of inertia of the configuration.
	 */
	public double getRotationalInertia(double time) {
		checkCache();
		
		if (cgCache == null) {
			calculateStageCache();
		}
		
		final Coordinate totalCG = getCG(time);
		double totalInertia = 0;
		
		// Stages
		for (int stage: configuration.getActiveStages()) {
			Coordinate stageCG = cgCache[stage];
			
			totalInertia += rotationalInertiaCache[stage] + stageCG.weight * (
					MathUtil.pow2(stageCG.y-totalCG.y) + MathUtil.pow2(stageCG.z-totalCG.z)
			);
		}
		
		
		// Motors
		Iterator<MotorMount> iterator = configuration.motorIterator();
		while (iterator.hasNext()) {
			MotorMount mount = iterator.next();
			double ignition = configuration.getIgnitionTime(mount);
			Motor motor = mount.getMotor(configuration.getMotorConfigurationID());
			RocketComponent component = (RocketComponent) mount;
		
			double position = (component.getLength() - motor.getLength() 
					+ mount.getMotorOverhang());
			
			double inertia = motor.getRotationalInertia(time - ignition);
			for (Coordinate c: component.toAbsolute(motor.getCG(time-ignition).
					add(position,0,0))) {
				totalInertia += inertia + c.weight * (
						MathUtil.pow2(c.y - totalCG.y) + MathUtil.pow2(c.z - totalCG.z)
				);
			}
		}
		
		return totalInertia;
	}
	
	
	
	private void calculateStageCache() {
		int stages = rocket.getStageCount();
		
		cgCache = new Coordinate[stages];
		longitudalInertiaCache = new double[stages];
		rotationalInertiaCache = new double[stages];
		
		for (int i=0; i < stages; i++) {
			RocketComponent stage = rocket.getChild(i);
			MassData data = calculateAssemblyMassData(stage);
			cgCache[i] = stage.toAbsolute(data.cg)[0];
			longitudalInertiaCache[i] = data.longitudalInertia;
			rotationalInertiaCache[i] = data.rotationalInetria;
		}
	}
	
	
	
//	/**
//	 * Updates the stage CGs.
//	 */
//	private void calculateStageCGs() {
//		int stages = rocket.getStageCount();
//		
//		cgCache = new Coordinate[stages];
//		origCG = new Coordinate[stages];
//		
//		for (int i=0; i < stages; i++) {
//			Stage stage = (Stage) rocket.getChild(i);
//			Coordinate stageCG = null;
//			
//			Iterator<RocketComponent> iterator = stage.deepIterator();
//			while (iterator.hasNext()) {
//				RocketComponent component = iterator.next();
//
//				for (Coordinate c: component.toAbsolute(component.getCG())) {
//					stageCG = c.average(stageCG);
//				}
//			}
//
//			if (stageCG == null)
//				stageCG = Coordinate.NUL;
//			
//			origCG[i] = stageCG;
//			
//			if (stage.isMassOverridden()) {
//				stageCG = stageCG.setWeight(stage.getOverrideMass());
//			}
//			if (stage.isCGOverridden()) {
//				stageCG = stageCG.setXYZ(stage.getOverrideCG());
//			}
//			
////			System.out.println("Stage "+i+" CG:"+stageCG);
//			
//			cgCache[i] = stageCG;
//		}
//	}
//	
//	
//	private Coordinate calculateCG(RocketComponent component) {
//		Coordinate componentCG = Coordinate.NUL;
//		
//		// Compute CG of this component
//		Coordinate cg = component.getCG();
//		if (cg.weight < MIN_MASS)
//			cg = cg.setWeight(MIN_MASS);
//
//		for (Coordinate c: component.toAbsolute(cg)) {
//			componentCG = componentCG.average(c);
//		}
//		
//		// Compute CG with subcomponents
//		for (RocketComponent sibling: component.getChildren()) {
//			componentCG = componentCG.average(calculateCG(sibling));
//		}
//		
//		// Override mass/CG if subcomponents are also overridden
//		if (component.getOverrideSubcomponents()) {
//			if (component.isMassOverridden()) {
//				componentCG = componentCG.setWeight(
//						MathUtil.max(component.getOverrideMass(), MIN_MASS));
//			}
//			if (component.isCGOverridden()) {
//				componentCG = componentCG.setXYZ(component.getOverrideCG());
//			}
//		}
//		
//		return componentCG;
//	}
//	
//	
//	
//	private void calculateStageInertias() {
//		int stages = rocket.getStageCount();
//		
//		if (cgCache == null)
//			calculateStageCGs();
//		
//		longitudalInertiaCache = new double[stages];
//		rotationalInertiaCache = new double[stages];
//
//		for (int i=0; i < stages; i++) {
//			Coordinate stageCG = cgCache[i];
//			double stageLongitudalInertia = 0;
//			double stageRotationalInertia = 0;
//			
//			Iterator<RocketComponent> iterator = rocket.getChild(i).deepIterator();
//			while (iterator.hasNext()) {
//				RocketComponent component = iterator.next();
//				double li = component.getLongitudalInertia();
//				double ri = component.getRotationalInertia();
//				double mass = component.getMass();
//				
//				for (Coordinate c: component.toAbsolute(component.getCG())) {
//					stageLongitudalInertia += li + mass * MathUtil.pow2(c.x - stageCG.x);
//					stageRotationalInertia += ri + mass * (MathUtil.pow2(c.y - stageCG.y) +
//							MathUtil.pow2(c.z - stageCG.z));
//				}
//			}
//			
//			// Check for mass override of complete stage
//			if ((origCG[i].weight != cgCache[i].weight) && origCG[i].weight > 0.0000001) {
//				stageLongitudalInertia = (stageLongitudalInertia * cgCache[i].weight / 
//						origCG[i].weight);
//				stageRotationalInertia = (stageRotationalInertia * cgCache[i].weight / 
//						origCG[i].weight);
//			}
//			
//			longitudalInertiaCache[i] = stageLongitudalInertia;
//			rotationalInertiaCache[i] = stageRotationalInertia;
//		}
//	}
//	
	
	
	/**
	 * Returns the mass and inertia data for this component and all subcomponents.
	 * The inertia is returned relative to the CG, and the CG is in the coordinates
	 * of the specified component, not global coordinates.
	 */
	private MassData calculateAssemblyMassData(RocketComponent parent) {
		MassData parentData = new MassData();
		
		// Calculate data for this component
		parentData.cg = parent.getComponentCG();
		if (parentData.cg.weight < MIN_MASS)
			parentData.cg = parentData.cg.setWeight(MIN_MASS);
		
		
		// Override only this component's data
		if (!parent.getOverrideSubcomponents()) {
			if (parent.isMassOverridden())
				parentData.cg = parentData.cg.setWeight(MathUtil.max(parent.getOverrideMass(),MIN_MASS));
			if (parent.isCGOverridden())
				parentData.cg = parentData.cg.setXYZ(parent.getOverrideCG());
		}
		
		parentData.longitudalInertia = parent.getLongitudalUnitInertia() * parentData.cg.weight;
		parentData.rotationalInetria = parent.getRotationalUnitInertia() * parentData.cg.weight;
		
		
		// Combine data for subcomponents
		for (RocketComponent sibling: parent.getChildren()) {
			Coordinate combinedCG;
			double dx2, dr2;
			
			// Compute data of sibling
			MassData siblingData = calculateAssemblyMassData(sibling);
			Coordinate[] siblingCGs = sibling.toRelative(siblingData.cg, parent);
			
			for (Coordinate siblingCG: siblingCGs) {
			
				// Compute CG of this + sibling
				combinedCG = parentData.cg.average(siblingCG);
				
				// Add effect of this CG change to parent inertia
				dx2 = pow2(parentData.cg.x - combinedCG.x);
				parentData.longitudalInertia += parentData.cg.weight * dx2;
				
				dr2 = pow2(parentData.cg.y - combinedCG.y) + pow2(parentData.cg.z - combinedCG.z);
				parentData.rotationalInetria += parentData.cg.weight * dr2;
				
				
				// Add inertia of sibling
				parentData.longitudalInertia += siblingData.longitudalInertia;
				parentData.rotationalInetria += siblingData.rotationalInetria;
				
				// Add effect of sibling CG change
				dx2 = pow2(siblingData.cg.x - combinedCG.x);
				parentData.longitudalInertia += siblingData.cg.weight * dx2;
				
				dr2 = pow2(siblingData.cg.y - combinedCG.y) + pow2(siblingData.cg.z - combinedCG.z);
				parentData.rotationalInetria += siblingData.cg.weight * dr2;
				
				// Set combined CG
				parentData.cg = combinedCG;
			}
		}

		// Override total data
		if (parent.getOverrideSubcomponents()) {
			if (parent.isMassOverridden()) {
				double oldMass = parentData.cg.weight;
				double newMass = MathUtil.max(parent.getOverrideMass(), MIN_MASS);
				parentData.longitudalInertia = parentData.longitudalInertia * newMass / oldMass;
				parentData.rotationalInetria = parentData.rotationalInetria * newMass / oldMass;
				parentData.cg = parentData.cg.setWeight(newMass);
			}
			if (parent.isCGOverridden()) {
				double oldx = parentData.cg.x;
				double newx = parent.getOverrideCGX();
				parentData.longitudalInertia += parentData.cg.weight * pow2(oldx - newx);
				parentData.cg = parentData.cg.setX(newx);
			}
		}
		
		return parentData;
	}
	
	
	private static class MassData {
		public Coordinate cg = Coordinate.NUL;
		public double longitudalInertia = 0;
		public double rotationalInetria = 0;
	}
	
	
	
	
	
	////////////////  Aerodynamic calculators  ////////////////
	
	public abstract Coordinate getCP(FlightConditions conditions, WarningSet warnings);
	
	/*
	public abstract List<AerodynamicForces> getCPAnalysis(FlightConditions conditions, 
			WarningSet warnings);
	*/
	
	public abstract Map<RocketComponent, AerodynamicForces> 
		getForceAnalysis(FlightConditions conditions, WarningSet warnings);
	
	public abstract AerodynamicForces getAerodynamicForces(double time,
			FlightConditions conditions, WarningSet warnings);
	
	
	/* Calculate only axial forces (and do not warn about insane AOA etc) */
	public abstract AerodynamicForces getAxialForces(double time,
			FlightConditions conditions, WarningSet warnings);

	
	
	public Coordinate getWorstCP() {
		return getWorstCP(new FlightConditions(configuration), ignoreWarningSet);
	}
	
	/*
	 * The worst theta angle is stored in conditions.
	 */
	public Coordinate getWorstCP(FlightConditions conditions, WarningSet warnings) {
		FlightConditions cond = conditions.clone();
		Coordinate worst = new Coordinate(Double.MAX_VALUE);
		Coordinate cp;
		double theta = 0;
		
		for (int i=0; i < DIVISIONS; i++) {
			cond.setTheta(2*Math.PI*i/DIVISIONS);
			cp = getCP(cond, warnings);
			if (cp.x < worst.x) {
				worst = cp;
				theta = cond.getTheta();
			}
		}
		
		conditions.setTheta(theta);
		
		return worst;
	}
	
	
	
	
	
	/**
	 * Check the current cache consistency.  This method must be called by all
	 * methods that may use any cached data before any other operations are
	 * performed.  If the rocket has changed since the previous call to
	 * <code>checkCache()</code>, then either {@link #voidAerodynamicCache()} or
	 * {@link #voidMassCache()} (or both) are called.
	 * <p>
	 * This method performs the checking based on the rocket's modification IDs,
	 * so that these method may be called from listeners of the rocket itself.
	 */
	protected final void checkCache() {
		if (rocketModID != rocket.getModID()) {
			rocketModID = rocket.getModID();
			voidMassCache();
			voidAerodynamicCache();
		}
	}
	
	
	/**
	 * Void cached mass data.  This method is called whenever a change occurs in 
	 * the rocket structure that affects the mass of the rocket and when a new 
	 * Rocket is set.  This method must be overridden to void any cached data 
	 * necessary.  The method must call <code>super.voidMassCache()</code> during its 
	 * execution.
	 */
	protected void voidMassCache() {
		cgCache = null;
		longitudalInertiaCache = null;
		rotationalInertiaCache = null;
	}
	
	/**
	 * Void cached aerodynamic data.  This method is called whenever a change occurs in 
	 * the rocket structure that affects the aerodynamics of the rocket and when a new 
	 * Rocket is set.  This method must be overridden to void any cached data 
	 * necessary.  The method must call <code>super.voidAerodynamicCache()</code> during 
	 * its execution.
	 */
	protected void voidAerodynamicCache() {
		// No-op
	}
	
	
}
