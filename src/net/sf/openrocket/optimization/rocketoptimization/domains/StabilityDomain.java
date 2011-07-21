package net.sf.openrocket.optimization.rocketoptimization.domains;

import net.sf.openrocket.aerodynamics.AerodynamicCalculator;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.masscalc.BasicMassCalculator;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.masscalc.MassCalculator.MassCalcType;
import net.sf.openrocket.optimization.rocketoptimization.SimulationDomain;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Pair;
import net.sf.openrocket.util.Prefs;

/**
 * A simulation domain that limits the requires stability of the rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class StabilityDomain implements SimulationDomain {
	
	/*
	 * TODO: HIGH:  Should this rather inspect stability during flight
	 */

	private final double limit;
	private final boolean absolute;
	
	
	public StabilityDomain(double limit, boolean absolute) {
		this.limit = limit;
		this.absolute = absolute;
	}
	
	
	@Override
	public Pair<Double, Double> getDistanceToDomain(Simulation simulation) {
		Coordinate cp, cg;
		double cpx, cgx;
		double reference;
		
		/*
		 * These are instantiated each time because this class must be thread-safe.
		 * Caching would in any case be inefficient since the rocket changes all the time.
		 */
		AerodynamicCalculator aerodynamicCalculator = new BarrowmanCalculator();
		MassCalculator massCalculator = new BasicMassCalculator();
		

		Configuration configuration = simulation.getConfiguration();
		FlightConditions conditions = new FlightConditions(configuration);
		conditions.setMach(Prefs.getDefaultMach());
		conditions.setAOA(0);
		conditions.setRollRate(0);
		
		// TODO: HIGH: This re-calculates the worst theta value every time
		cp = aerodynamicCalculator.getWorstCP(configuration, conditions, null);
		cg = massCalculator.getCG(configuration, MassCalcType.LAUNCH_MASS);
		
		if (cp.weight > 0.000001)
			cpx = cp.x;
		else
			cpx = Double.NaN;
		
		if (cg.weight > 0.000001)
			cgx = cg.x;
		else
			cgx = Double.NaN;
		

		// Calculate the reference (absolute or relative)
		reference = cpx - cgx;
		if (!absolute) {
			double diameter = 0;
			for (RocketComponent c : configuration) {
				if (c instanceof SymmetricComponent) {
					double d1 = ((SymmetricComponent) c).getForeRadius() * 2;
					double d2 = ((SymmetricComponent) c).getAftRadius() * 2;
					diameter = MathUtil.max(diameter, d1, d2);
				}
			}
			
			reference = (cpx - cgx) / diameter;
		}
		
		System.out.println("DOMAIN: limit=" + limit + " reference=" + reference + " result=" + (limit - reference));
		
		return new Pair<Double, Double>(limit - reference, reference);
	}
	
}
