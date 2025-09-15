package info.openrocket.core.aerodynamics;

import java.util.LinkedHashMap;
import java.util.Map;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ModID;

/**
 * An aerodynamic calculator that uses the extended Barrowman method by delegating
 * stability and drag calculations to dedicated calculators.
 */
public class BarrowmanCalculator extends AbstractAerodynamicCalculator {
	private final StabilityCalculator stabilityCalculator;
	private final DragCalculator dragCalculator;

	public BarrowmanCalculator() {
		this(new BarrowmanStabilityCalculator(), new BarrowmanDragCalculator());
	}

	public BarrowmanCalculator(StabilityCalculator stabilityCalculator, DragCalculator dragCalculator) {
		if (stabilityCalculator == null || dragCalculator == null) {
			throw new IllegalArgumentException("Calculators must not be null");
		}
		this.stabilityCalculator = stabilityCalculator;
		this.dragCalculator = dragCalculator;
	}

	@Override
	public BarrowmanCalculator newInstance() {
		return new BarrowmanCalculator(stabilityCalculator.newInstance(), dragCalculator.newInstance());
	}

	@Override
	public double getStallMargin() {
		return stabilityCalculator.getStallMargin();
	}

	@Override
	public Coordinate getCP(FlightConfiguration configuration, FlightConditions conditions,
			WarningSet warnings) {
		checkCache(configuration);
		WarningSet actualWarnings = (warnings != null) ? warnings : ignoreWarningSet;
		return stabilityCalculator.getCP(configuration, conditions, actualWarnings);
	}

	@Override
	public Map<RocketComponent, AerodynamicForces> getForceAnalysis(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		checkCache(configuration);

		WarningSet actualWarnings = (warnings != null) ? warnings : ignoreWarningSet;
		StabilityForceBreakdown breakdown = stabilityCalculator.getForceAnalysis(configuration, conditions,
				actualWarnings);

		Map<RocketComponent, AerodynamicForces> eachMap = breakdown.getComponentForces();
		Map<RocketComponent, AerodynamicForces> assemblyMap = breakdown.getAssemblyForces();

		AerodynamicForces rocketForces = assemblyMap.get(configuration.getRocket());
		dragCalculator.calculateDrag(configuration, conditions, eachMap, assemblyMap, rocketForces, actualWarnings);

		InstanceMap instMap = configuration.getActiveInstances();
		Map<RocketComponent, AerodynamicForces> finalMap = new LinkedHashMap<>();
		for (RocketComponent comp : instMap.keySet()) {
			AerodynamicForces forces;
			if (comp instanceof ComponentAssembly) {
				forces = assemblyMap.get(comp);
			} else if (comp.isAerodynamic()) {
				forces = eachMap.get(comp);
			} else {
				continue;
			}

			if (forces.getCP().isNaN()) {
				forces.setCP(Coordinate.ZERO);
			}
			if (Double.isNaN(forces.getBaseCD())) {
				forces.setBaseCD(0);
			}
			if (Double.isNaN(forces.getPressureCD())) {
				forces.setPressureCD(0);
			}
			if (Double.isNaN(forces.getFrictionCD())) {
				forces.setFrictionCD(0);
			}
			if (Double.isNaN(forces.getOverrideCD())) {
				forces.setOverrideCD(0);
			}

			double cd = forces.getBaseCD() + forces.getPressureCD() + forces.getFrictionCD() + forces.getOverrideCD();
			forces.setCD(cd);
			forces.setCDaxial(dragCalculator.toAxialDrag(conditions, cd));

			finalMap.put(comp, forces);
		}

		return finalMap;
	}

	@Override
	public AerodynamicForces getAerodynamicForces(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		checkCache(configuration);

		WarningSet actualWarnings = (warnings != null) ? warnings : ignoreWarningSet;

		AerodynamicForces total = stabilityCalculator.calculateNonAxialForces(configuration, conditions, actualWarnings);

		dragCalculator.calculateDrag(configuration, conditions, null, null, total, actualWarnings);

		stabilityCalculator.calculateDampingMoments(configuration, conditions, total);
		total.setCm(total.getCm() - total.getPitchDampingMoment());
		total.setCyaw(total.getCyaw() - total.getYawDampingMoment());

		return total;
	}

	@Override
	public void checkGeometry(FlightConfiguration configuration, RocketComponent component, WarningSet warnings) {
		stabilityCalculator.checkGeometry(configuration, component, warnings);
	}

	@Override
	protected void voidAerodynamicCache() {
		super.voidAerodynamicCache();
		stabilityCalculator.voidAerodynamicCache();
		dragCalculator.voidAerodynamicCache();
	}

	@Override
	public ModID getModID() {
		return ModID.ZERO;
	}

	public static double calculateStagnationCD(double m) {
		return BarrowmanDragCalculator.calculateStagnationCD(m);
	}

	public static double calculateBaseCD(double m) {
		return BarrowmanDragCalculator.calculateBaseCD(m);
	}
}
