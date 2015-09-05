package net.sf.openrocket.masscalc;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorId;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.MassData;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicMassCalculator implements MassCalculator {
	
	private static final double MIN_MASS = 0.001 * MathUtil.EPSILON;
	private static final Logger log = LoggerFactory.getLogger(MassCalculator.class);
	
	private int rocketMassModID = -1;
	private int rocketTreeModID = -1;
	
	/*
	 * Cached data.  All CG data is in absolute coordinates.  All moments of inertia
	 * are relative to their respective CG.
	 */
	private Coordinate[] cgCache = null;
	private double longitudinalInertiaCache[] = null;
	private double rotationalInertiaCache[] = null;
	
	
	
	//////////////////  Mass property calculations  ///////////////////
	
	
	/**
	 * Return the CG of the rocket with the specified motor status (no motors,
	 * ignition, burnout).
	 */
	@Override
	public Coordinate getCG(Configuration configuration, MassCalcType type) {
		checkCache(configuration);
		calculateStageCache(configuration);
		
		Coordinate totalCG = null;
		
		// Stage contribution
		for (int stage : configuration.getActiveStages()) {
			totalCG = cgCache[stage].average(totalCG);
		}
		
		if (totalCG == null)
			totalCG = Coordinate.NUL;
		
		// Add motor CGs
		String motorId = configuration.getFlightConfigurationID();
		if (type != MassCalcType.NO_MOTORS && motorId != null) {
			Iterator<MotorMount> iterator = configuration.motorIterator();
			while (iterator.hasNext()) {
				MotorMount mount = iterator.next();
				RocketComponent comp = (RocketComponent) mount;
				Motor motor = mount.getMotor(motorId);
				if (motor == null)
					continue;
				
				Coordinate motorCG = type.getCG(motor).add(mount.getMotorPosition(motorId));
				Coordinate[] cgs = comp.toAbsolute(motorCG);
				for (Coordinate cg : cgs) {
					totalCG = totalCG.average(cg);
				}
			}
		}
		
		return totalCG;
	}
	
	
	
	
	/**
	 * Return the CG of the rocket with the provided motor configuration.
	 */
	@Override
	public Coordinate getCG(Configuration configuration, MotorInstanceConfiguration motors) {
		checkCache(configuration);
		calculateStageCache(configuration);
		
		Coordinate totalCG = getCG(configuration, MassCalcType.NO_MOTORS);
		
		// Add motor CGs
		if (motors != null) {
			for (MotorId id : motors.getMotorIDs()) {
				int stage = ((RocketComponent) motors.getMotorMount(id)).getStageNumber();
				if (configuration.isStageActive(stage)) {
					
					MotorInstance motor = motors.getMotorInstance(id);
					Coordinate position = motors.getMotorPosition(id);
					Coordinate cg = motor.getCG().add(position);
					totalCG = totalCG.average(cg);
					
				}
			}
		}
		return totalCG;
	}
	
	/**
	 * Return the longitudinal inertia of the rocket with the specified motor instance
	 * configuration.
	 * 
	 * @param configuration		the current motor instance configuration
	 * @return					the longitudinal inertia of the rocket
	 */
	@Override
	public double getLongitudinalInertia(Configuration configuration, MotorInstanceConfiguration motors) {
		checkCache(configuration);
		calculateStageCache(configuration);
		
		final Coordinate totalCG = getCG(configuration, motors);
		double totalInertia = 0;
		
		// Stages
		for (int stage : configuration.getActiveStages()) {
			Coordinate stageCG = cgCache[stage];
			
			totalInertia += (longitudinalInertiaCache[stage] +
					stageCG.weight * MathUtil.pow2(stageCG.x - totalCG.x));
		}
		
		
		// Motors
		if (motors != null) {
			for (MotorId id : motors.getMotorIDs()) {
				int stage = ((RocketComponent) motors.getMotorMount(id)).getStageNumber();
				if (configuration.isStageActive(stage)) {
					MotorInstance motor = motors.getMotorInstance(id);
					Coordinate position = motors.getMotorPosition(id);
					Coordinate cg = motor.getCG().add(position);
					
					double inertia = motor.getLongitudinalInertia();
					totalInertia += inertia + cg.weight * MathUtil.pow2(cg.x - totalCG.x);
				}
			}
		}
		
		return totalInertia;
	}
	
	
	
	/**
	 * Return the rotational inertia of the rocket with the specified motor instance
	 * configuration.
	 * 
	 * @param configuration		the current motor instance configuration
	 * @return					the rotational inertia of the rocket
	 */
	@Override
	public double getRotationalInertia(Configuration configuration, MotorInstanceConfiguration motors) {
		checkCache(configuration);
		calculateStageCache(configuration);
		
		final Coordinate totalCG = getCG(configuration, motors);
		double totalInertia = 0;
		
		// Stages
		for (int stage : configuration.getActiveStages()) {
			Coordinate stageCG = cgCache[stage];
			
			totalInertia += (rotationalInertiaCache[stage] +
					stageCG.weight * (MathUtil.pow2(stageCG.y - totalCG.y) +
							MathUtil.pow2(stageCG.z - totalCG.z)));
		}
		
		
		// Motors
		if (motors != null) {
			for (MotorId id : motors.getMotorIDs()) {
				int stage = ((RocketComponent) motors.getMotorMount(id)).getStageNumber();
				if (configuration.isStageActive(stage)) {
					MotorInstance motor = motors.getMotorInstance(id);
					Coordinate position = motors.getMotorPosition(id);
					Coordinate cg = motor.getCG().add(position);
					
					double inertia = motor.getRotationalInertia();
					totalInertia += inertia + cg.weight * (MathUtil.pow2(cg.y - totalCG.y) +
							MathUtil.pow2(cg.z - totalCG.z));
				}
			}
		}
		
		return totalInertia;
	}
	
	/**
	 * Return the total mass of the motors
	 * 
	 * @param configuration		the current motor instance configuration
	 * @return					the total mass of all motors
	 */
	@Override
	public double getPropellantMass(Configuration configuration, MotorInstanceConfiguration motors) {
		double mass = 0;
		
		// add up the masses of all motors in the rocket
		if (motors != null) {
			for (MotorId id : motors.getMotorIDs()) {
				MotorInstance motor = motors.getMotorInstance(id);
				mass = mass + motor.getCG().weight - motor.getParentMotor().getEmptyCG().weight;
			}
		}
		return mass;
	}
	
	@Override
	public Map<RocketComponent, Coordinate> getCGAnalysis(Configuration configuration, MassCalcType type) {
		checkCache(configuration);
		calculateStageCache(configuration);
		
		Map<RocketComponent, Coordinate> map = new HashMap<RocketComponent, Coordinate>();
		
		for (RocketComponent c : configuration) {
			Coordinate[] cgs = c.toAbsolute(c.getCG());
			Coordinate totalCG = Coordinate.NUL;
			for (Coordinate cg : cgs) {
				totalCG = totalCG.average(cg);
			}
			map.put(c, totalCG);
		}
		
		map.put(configuration.getRocket(), getCG(configuration, type));
		
		return map;
	}
	
	////////  Cache computations  ////////
	
	private void calculateStageCache(Configuration config) {
		if (cgCache == null) {
			ArrayList<AxialStage> stageList = config.getRocket().getStageList();
			int stageCount = stageList.size();
			
			cgCache = new Coordinate[stageCount];
			longitudinalInertiaCache = new double[stageCount];
			rotationalInertiaCache = new double[stageCount];
			
			for (int i = 0; i < stageCount; i++) {
				RocketComponent stage = stageList.get(i);
				MassData data = calculateAssemblyMassData(stage);
				cgCache[i] = stage.toAbsolute(data.getCG())[0];
				longitudinalInertiaCache[i] = data.getLongitudinalInertia();
				rotationalInertiaCache[i] = data.getRotationalInertia();
			}
			
		}
	}
	
	
	
	/**
	 * Returns the mass and inertia data for this component and all subcomponents.
	 * The inertia is returned relative to the CG, and the CG is in the coordinates
	 * of the specified component, not global coordinates.
	 */
	private MassData calculateAssemblyMassData(RocketComponent parent) {
		Coordinate parentCG = Coordinate.ZERO;
		double longitudinalInertia = 0.0;
		double rotationalInertia = 0.0;
		
		// Calculate data for this component
		parentCG = parent.getComponentCG();
		if (parentCG.weight < MIN_MASS)
			parentCG = parentCG.setWeight(MIN_MASS);
		
		
		// Override only this component's data
		if (!parent.getOverrideSubcomponents()) {
			if (parent.isMassOverridden())
				parentCG = parentCG.setWeight(MathUtil.max(parent.getOverrideMass(), MIN_MASS));
			if (parent.isCGOverridden())
				parentCG = parentCG.setXYZ(parent.getOverrideCG());
		}
		
		longitudinalInertia = parent.getLongitudinalUnitInertia() * parentCG.weight;
		rotationalInertia = parent.getRotationalUnitInertia() * parentCG.weight;
		
		
		// Combine data for subcomponents
		for (RocketComponent sibling : parent.getChildren()) {
			Coordinate combinedCG;
			double dx2, dr2;
			
			// Compute data of sibling
			MassData siblingData = calculateAssemblyMassData(sibling);
			Coordinate[] siblingCGs = sibling.toRelative(siblingData.getCG(), parent);
			
			for (Coordinate siblingCG : siblingCGs) {
				
				// Compute CG of this + sibling
				combinedCG = parentCG.average(siblingCG);
				
				// Add effect of this CG change to parent inertia
				dx2 = pow2(parentCG.x - combinedCG.x);
				longitudinalInertia += parentCG.weight * dx2;
				
				dr2 = pow2(parentCG.y - combinedCG.y) + pow2(parentCG.z - combinedCG.z);
				rotationalInertia += parentCG.weight * dr2;
				
				
				// Add inertia of sibling
				longitudinalInertia += siblingData.getLongitudinalInertia();
				rotationalInertia += siblingData.getRotationalInertia();
				
				// Add effect of sibling CG change
				dx2 = pow2(siblingData.getCG().x - combinedCG.x);
				longitudinalInertia += siblingData.getCG().weight * dx2;
				
				dr2 = pow2(siblingData.getCG().y - combinedCG.y) + pow2(siblingData.getCG().z - combinedCG.z);
				rotationalInertia += siblingData.getCG().weight * dr2;
				
				// Set combined CG
				parentCG = combinedCG;
			}
		}
		
		// Override total data
		if (parent.getOverrideSubcomponents()) {
			if (parent.isMassOverridden()) {
				double oldMass = parentCG.weight;
				double newMass = MathUtil.max(parent.getOverrideMass(), MIN_MASS);
				longitudinalInertia = longitudinalInertia * newMass / oldMass;
				rotationalInertia = rotationalInertia * newMass / oldMass;
				parentCG = parentCG.setWeight(newMass);
			}
			if (parent.isCGOverridden()) {
				double oldx = parentCG.x;
				double newx = parent.getOverrideCGX();
				longitudinalInertia += parentCG.weight * pow2(oldx - newx);
				parentCG = parentCG.setX(newx);
			}
		}
		
		MassData parentData = new MassData(parentCG, longitudinalInertia, rotationalInertia, 0);
		return parentData;
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
	protected final void checkCache(Configuration configuration) {
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
		this.cgCache = null;
		this.longitudinalInertiaCache = null;
		this.rotationalInertiaCache = null;
	}
	
	
	
	
	@Override
	public int getModID() {
		return 0;
	}
	
	
	
	
	
}
