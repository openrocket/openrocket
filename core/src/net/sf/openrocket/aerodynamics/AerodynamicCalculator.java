package net.sf.openrocket.aerodynamics;

import java.util.Map;

import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Monitorable;

/**
 * An interface for performing aerodynamic calculations on rockets.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface AerodynamicCalculator extends Monitorable {
	
	/**
	 * Calculate the CP of the specified configuration.
	 * 
	 * @param configuration		the rocket configuration
	 * @param conditions		the flight conditions
	 * @param warnings			the set in which to place warnings, or <code>null</code>
	 * @return					the CP position in absolute coordinates
	 */
	public Coordinate getCP(Configuration configuration, FlightConditions conditions, WarningSet warnings);
	
	/**
	 * Calculate the aerodynamic forces acting upon the rocket.
	 * 
	 * @param configuration		the rocket configuration.
	 * @param conditions		the flight conditions.
	 * @param warnings			the set in which to place warnings, or <code>null</code>.
	 * @return					the aerodynamic forces acting upon the rocket.
	 */
	public AerodynamicForces getAerodynamicForces(Configuration configuration,
			FlightConditions conditions, WarningSet warnings);
	
	/**
	 * Calculate the aerodynamic forces acting upon the rocket with a component analysis.
	 * 
	 * @param configuration		the rocket configuration.
	 * @param conditions		the flight conditions.
	 * @param warnings			the set in which to place warnings, or <code>null</code>.
	 * @return					a map from the rocket components to the aerodynamic force portions that component
	 * 							exerts.  The map contains an value for the base rocket, which is the total
	 * 							aerodynamic forces.
	 */
	public Map<RocketComponent, AerodynamicForces> getForceAnalysis(Configuration configuration,
			FlightConditions conditions, WarningSet warnings);
	
	/**
	 * Calculate the worst CP occurring for any lateral wind angle.  The worst CP is returned and the theta angle
	 * that produces the worst CP is stored in the flight conditions.
	 * 
	 * @param configuration		the rocket configuration.
	 * @param conditions		the flight conditions.
	 * @param warnings			the set in which to place warnings, or <code>null</code>.
	 * @return					the worst (foremost) CP position for any lateral wind angle.
	 */
	public Coordinate getWorstCP(Configuration configuration, FlightConditions conditions,
			WarningSet warnings);
	
	/**
	 * Return a new instance of this aerodynamic calculator type.
	 * 
	 * @return	a new, independent instance of this aerodynamic calculator type
	 */
	public AerodynamicCalculator newInstance();
	
}
