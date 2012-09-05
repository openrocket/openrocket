package net.sf.openrocket.masscalc;

import java.util.Map;

import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;

public interface MassCalculator extends Monitorable {
	
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
	
	/**
	 * Compute the CG of the provided configuration.
	 * 
	 * @param configuration		the rocket configuration
	 * @param type				the state of the motors (none, launch mass, burnout mass)
	 * @return					the CG of the configuration
	 */
	public Coordinate getCG(Configuration configuration, MassCalcType type);
	
	/**
	 * Compute the CG of the provided configuration with specified motors.
	 * 
	 * @param configuration		the rocket configuration
	 * @param motors			the motor configuration
	 * @return					the CG of the configuration
	 */
	public Coordinate getCG(Configuration configuration, MotorInstanceConfiguration motors);
	
	/**
	 * Compute the longitudinal inertia of the provided configuration with specified motors.
	 * 
	 * @param configuration		the rocket configuration
	 * @param motors			the motor configuration
	 * @return					the longitudinal inertia of the configuration
	 */
	public double getLongitudinalInertia(Configuration configuration, MotorInstanceConfiguration motors);
	
	/**
	 * Compute the rotational inertia of the provided configuration with specified motors.
	 * 
	 * @param configuration		the rocket configuration
	 * @param motors			the motor configuration
	 * @return					the rotational inertia of the configuration
	 */
	public double getRotationalInertia(Configuration configuration, MotorInstanceConfiguration motors);
	
	/**
	 * Return the total mass of the motors
	 * 
	 * @param motors			the motor configuration
	 * @param configuration		the current motor instance configuration
	 * @return					the total mass of all motors
	 */
	public double getPropellantMass(Configuration configuration, MotorInstanceConfiguration motors);	
	
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
	public Map<RocketComponent, Coordinate> getCGAnalysis(Configuration configuration, MassCalcType type);
	

}
