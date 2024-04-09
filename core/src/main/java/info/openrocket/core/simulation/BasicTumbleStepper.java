package info.openrocket.core.simulation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;

public class BasicTumbleStepper extends AbstractEulerStepper {

	public double computeCD(SimulationStatus status) {

		// Computed based on Sampo's experimentation as documented in techdoc.pdf.

		// Magic constants from techdoc.pdf
		final double cDFin = 1.42;
		final double cDBt = 0.56;
		// Fin efficiency. Index is number of fins. The 0th entry is arbitrary and used
		// to
		// offset the indexes so finEff[1] is the coefficient for one fin from the table
		// in techdoc.pdf
		final double[] finEff = { 0.0, 0.5, 1.0, 1.41, 1.81, 1.73, 1.90, 1.85 };

		// compute the fin and body tube projected areas
		double aFins = 0.0;
		double aBt = 0.0;
		final InstanceMap imap = status.getConfiguration().getActiveInstances();
		for (Map.Entry<RocketComponent, ArrayList<InstanceContext>> entry : imap.entrySet()) {
			final RocketComponent component = entry.getKey();

			if (!component.isAerodynamic()) {
				continue;
			}

			// iterate across component instances
			final ArrayList<InstanceContext> contextList = entry.getValue();
			for (InstanceContext context : contextList) {

				if (component instanceof FinSet) {
					final FinSet finComponent = ((FinSet) component);
					final double finArea = finComponent.getPlanformArea();
					int finCount = finComponent.getFinCount();

					// check bounds on finCount.
					if (finCount >= finEff.length) {
						finCount = finEff.length - 1;
					}

					aFins += finArea * finEff[finCount] / finComponent.getFinCount();

				} else if (component instanceof SymmetricComponent) {
					aBt += ((SymmetricComponent) component).getComponentPlanformArea();
				}
			}
		}

		return (cDFin * aFins + cDBt * aBt) / status.getConfiguration().getReferenceArea();
	}

}
