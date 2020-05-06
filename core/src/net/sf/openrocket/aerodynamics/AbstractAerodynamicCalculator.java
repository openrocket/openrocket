package net.sf.openrocket.aerodynamics;

import java.util.Map;

import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Coordinate;


/**
 * An abstract aerodynamic calculator implementation, that offers basic implementation
 * of some methods and methods for cache validation and purging.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public abstract class AbstractAerodynamicCalculator implements AerodynamicCalculator {
	
	/** Number of divisions used when calculating worst CP. */
	public static final int DIVISIONS = 360;
	
	/**
	 * A <code>WarningSet</code> that can be used if <code>null</code> is passed
	 * to a calculation method.
	 */
	protected WarningSet ignoreWarningSet = new WarningSet();
	
	/** The aerodynamic modification ID of the latest rocket */
	private int rocketAeroModID = -1;
	private int rocketTreeModID = -1;
	
	


	////////////////  Aerodynamic calculators  ////////////////
	
	@Override
	public abstract Coordinate getCP(FlightConfiguration configuration, FlightConditions conditions,
			WarningSet warnings);
	
	@Override
	public abstract Map<RocketComponent, AerodynamicForces> getForceAnalysis(FlightConfiguration configuration, FlightConditions conditions,
				WarningSet warnings);
	
	@Override
	public abstract AerodynamicForces getAerodynamicForces(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings);
	
	

	/*
	 * The worst theta angle is stored in conditions.
	 */
	@Override
	public Coordinate getWorstCP(FlightConfiguration configuration, FlightConditions conditions,
			WarningSet warnings) {
		FlightConditions cond = conditions.clone();
		Coordinate worst = new Coordinate(Double.MAX_VALUE);
		Coordinate cp;
		double theta = 0;
		
		for (int i = 0; i < DIVISIONS; i++) {
			cond.setTheta(2 * Math.PI * i / DIVISIONS);
			cp = getCP(configuration, cond, warnings);
			if ((cp.weight > MathUtil.EPSILON) && (cp.x < worst.x)) {
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
	 * <code>checkCache()</code>, then {@link #voidAerodynamicCache()} is called.
	 * <p>
	 * This method performs the checking based on the rocket's modification IDs,
	 * so that these method may be called from listeners of the rocket itself.
	 * 
	 * @param	configuration	the configuration of the current call
	 */
	protected final void checkCache(FlightConfiguration configuration) {
		if (rocketAeroModID != configuration.getRocket().getAerodynamicModID() ||
				rocketTreeModID != configuration.getRocket().getTreeModID()) {
			// // vvvv DEVEL vvvv
			// log.error("Voiding the aerodynamic cache because modIDs changed...", new BugException(" unsure why modID has changed..."));
			// // ^^^^ DEVEL ^^^^
			
			rocketAeroModID = configuration.getRocket().getAerodynamicModID();
			rocketTreeModID = configuration.getRocket().getTreeModID();
			voidAerodynamicCache();
		}
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
