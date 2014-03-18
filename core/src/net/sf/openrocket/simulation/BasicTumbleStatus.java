package net.sf.openrocket.simulation;

import java.util.Iterator;

import net.sf.openrocket.motor.MotorInstanceConfiguration;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;

public class BasicTumbleStatus extends SimulationStatus {
	
	// Magic constants from techdoc.pdf
	private final static double cDFin = 1.42;
	private final static double cDBt = 0.56;
	// Fin efficiency.  Index is number of fins.  The 0th entry is arbitrary and used to
	// offset the indexes so finEff[1] is the coefficient for one fin from the table in techdoc.pdf
	private final static double[] finEff = { 0.0, 0.5, 1.0, 1.41, 1.81, 1.73, 1.90, 1.85 };
	
	private final double drag;
	
	public BasicTumbleStatus(Configuration configuration,
			MotorInstanceConfiguration motorConfiguration,
			SimulationConditions simulationConditions) {
		super(configuration, motorConfiguration, simulationConditions);
		this.drag = computeTumbleDrag();
	}
	
	public BasicTumbleStatus(SimulationStatus orig) {
		super(orig);
		if (orig instanceof BasicTumbleStatus) {
			this.drag = ((BasicTumbleStatus) orig).drag;
		} else {
			this.drag = computeTumbleDrag();
		}
	}
	
	public double getTumbleDrag() {
		return drag;
	}
	
	
	private double computeTumbleDrag() {
		
		// Computed based on Sampo's experimentation as documented in the pdf.
		
		// compute the fin and body tube projected areas
		double aFins = 0.0;
		double aBt = 0.0;
		Rocket r = this.getConfiguration().getRocket();
		Iterator<RocketComponent> componentIterator = r.iterator();
		while (componentIterator.hasNext()) {
			RocketComponent component = componentIterator.next();
			if (!component.isAerodynamic()) {
				continue;
			}
			if (component instanceof FinSet) {
				
				double finComponent = ((FinSet) component).getFinArea();
				int finCount = ((FinSet) component).getFinCount();
				// check bounds on finCount.
				if (finCount >= finEff.length) {
					finCount = finEff.length - 1;
				}
				
				aFins += finComponent * finEff[finCount];
				
			} else if (component instanceof SymmetricComponent) {
				aBt += ((SymmetricComponent) component).getComponentPlanformArea();
			}
		}
		
		return (cDFin * aFins + cDBt * aBt);
	}
}
