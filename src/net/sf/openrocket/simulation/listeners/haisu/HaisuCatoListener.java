package net.sf.openrocket.simulation.listeners.haisu;

import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.simulation.SimulationStatus;
import net.sf.openrocket.simulation.listeners.AbstractSimulationListener;

public class HaisuCatoListener extends AbstractSimulationListener {

	private static final double POSITION = 0.8;
	private static final double CNA = 5.16;
	
	private final double alpha;

	public HaisuCatoListener(double alpha) {
		this.alpha = alpha;
	}
	
	@Override
	public void forceCalculation(SimulationStatus status, FlightConditions conditions, 
			AerodynamicForces forces) {

		double cn = CNA * alpha;
		double cm = cn * POSITION / conditions.getRefLength();
		
		double theta = conditions.getTheta();
		double costheta = Math.cos(theta);
		
		forces.CN += cn * costheta;
		forces.Cm += cm * costheta;
		
		if (Math.abs(costheta) < 0.99) {
			System.err.println("THETA = "+(theta*180/Math.PI)+ " aborting...");
			System.exit(1);
		}
	}

}
