package net.sf.openrocket.masscalc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.rocketcomponent.Configuration;

/**
 * Abstract base for mass calculators.  Provides functionality for cacheing mass data.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class AbstractMassCalculator implements MassCalculator {
	private static final Logger log = LoggerFactory.getLogger(AbstractMassCalculator.class);
	
	private int rocketMassModID = -1;
	private int rocketTreeModID = -1;
	
	
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
		// No-op
	}
	
}
