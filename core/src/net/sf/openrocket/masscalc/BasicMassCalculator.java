package net.sf.openrocket.masscalc;

import static net.sf.openrocket.util.MathUtil.pow2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorId;
import net.sf.openrocket.motor.MotorInstance;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

public class BasicMassCalculator extends AbstractMassCalculator {
	
	private static final double MIN_MASS = 0.001 * MathUtil.EPSILON;
	

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
	public double getPropellantMass(Configuration configuration, MotorInstanceConfiguration motors){
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
			
			int stages = config.getRocket().getStageCount();
			
			cgCache = new Coordinate[stages];
			longitudinalInertiaCache = new double[stages];
			rotationalInertiaCache = new double[stages];
			
			for (int i = 0; i < stages; i++) {
				RocketComponent stage = config.getRocket().getChild(i);
				MassData data = calculateAssemblyMassData(stage);
				cgCache[i] = stage.toAbsolute(data.cg)[0];
				longitudinalInertiaCache[i] = data.longitudinalInertia;
				rotationalInertiaCache[i] = data.rotationalInetria;
			}
			
		}
	}
	
	

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
				parentData.cg = parentData.cg.setWeight(MathUtil.max(parent.getOverrideMass(), MIN_MASS));
			if (parent.isCGOverridden())
				parentData.cg = parentData.cg.setXYZ(parent.getOverrideCG());
		}
		
		parentData.longitudinalInertia = parent.getLongitudinalUnitInertia() * parentData.cg.weight;
		parentData.rotationalInetria = parent.getRotationalUnitInertia() * parentData.cg.weight;
		

		// Combine data for subcomponents
		for (RocketComponent sibling : parent.getChildren()) {
			Coordinate combinedCG;
			double dx2, dr2;
			
			// Compute data of sibling
			MassData siblingData = calculateAssemblyMassData(sibling);
			Coordinate[] siblingCGs = sibling.toRelative(siblingData.cg, parent);
			
			for (Coordinate siblingCG : siblingCGs) {
				
				// Compute CG of this + sibling
				combinedCG = parentData.cg.average(siblingCG);
				
				// Add effect of this CG change to parent inertia
				dx2 = pow2(parentData.cg.x - combinedCG.x);
				parentData.longitudinalInertia += parentData.cg.weight * dx2;
				
				dr2 = pow2(parentData.cg.y - combinedCG.y) + pow2(parentData.cg.z - combinedCG.z);
				parentData.rotationalInetria += parentData.cg.weight * dr2;
				

				// Add inertia of sibling
				parentData.longitudinalInertia += siblingData.longitudinalInertia;
				parentData.rotationalInetria += siblingData.rotationalInetria;
				
				// Add effect of sibling CG change
				dx2 = pow2(siblingData.cg.x - combinedCG.x);
				parentData.longitudinalInertia += siblingData.cg.weight * dx2;
				
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
				parentData.longitudinalInertia = parentData.longitudinalInertia * newMass / oldMass;
				parentData.rotationalInetria = parentData.rotationalInetria * newMass / oldMass;
				parentData.cg = parentData.cg.setWeight(newMass);
			}
			if (parent.isCGOverridden()) {
				double oldx = parentData.cg.x;
				double newx = parent.getOverrideCGX();
				parentData.longitudinalInertia += parentData.cg.weight * pow2(oldx - newx);
				parentData.cg = parentData.cg.setX(newx);
			}
		}
		
		return parentData;
	}
	
	
	private static class MassData {
		public Coordinate cg = Coordinate.NUL;
		public double longitudinalInertia = 0;
		public double rotationalInetria = 0;
	}
	
	
	@Override
	protected void voidMassCache() {
		super.voidMassCache();
		this.cgCache = null;
		this.longitudinalInertiaCache = null;
		this.rotationalInertiaCache = null;
	}
	
	


	@Override
	public int getModID() {
		return 0;
	}
	


}
