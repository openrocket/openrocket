package info.openrocket.core.aerodynamics;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceMap;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Coordinate;

/**
 * Stability calculator backed by a CSV lookup table.
 */
public class LookupTableStabilityCalculator implements StabilityCalculator {

	private static final String COLUMN_CN = "cn";
	private static final String COLUMN_CM = "cm";
	private static final String COLUMN_CP = "cp";

	private final MachAoALookup table;
	private double stallMargin = Double.POSITIVE_INFINITY;

	public LookupTableStabilityCalculator(Path csvPath) {
		this(CsvMachAoALookup.fromCsv(csvPath, List.of(COLUMN_CN, COLUMN_CM, COLUMN_CP)));
	}

	public LookupTableStabilityCalculator(MachAoALookup table) {
		this.table = table;
	}

	@Override
	public StabilityCalculator newInstance() {
		return new LookupTableStabilityCalculator(table);
	}

	@Override
	public double getStallMargin() {
		return stallMargin;
	}

	@Override
	public Coordinate getCP(FlightConfiguration configuration, FlightConditions conditions, WarningSet warnings) {
		return calculateNonAxialForces(configuration, conditions, warnings).getCP();
	}

	@Override
	public AerodynamicForces calculateNonAxialForces(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		double mach = conditions.getMach();
		double aoaDegrees = Math.toDegrees(conditions.getAOA());

		double cn = table.interpolate(mach, aoaDegrees, COLUMN_CN);
		double cm = table.interpolate(mach, aoaDegrees, COLUMN_CM);
		double cp = table.interpolate(mach, aoaDegrees, COLUMN_CP);

		AerodynamicForces forces = new AerodynamicForces().zero();
		forces.setCN(cn);
		forces.setCm(cm);
		forces.setCP(new Coordinate(cp, 0, 0, 1));
		forces.setCside(0);
		forces.setCyaw(0);
		forces.setCroll(0);
		forces.setCrollDamp(0);
		forces.setCrollForce(0);

		if (table.hasAoA()) {
			double maxAoARadians = Math.toRadians(table.getMaxAoA());
			stallMargin = maxAoARadians - conditions.getAOA();
		} else {
			stallMargin = Double.POSITIVE_INFINITY;
		}

		return forces;
	}

	@Override
	public StabilityForceBreakdown getForceAnalysis(FlightConfiguration configuration,
			FlightConditions conditions, WarningSet warnings) {
		AerodynamicForces total = calculateNonAxialForces(configuration, conditions, warnings);
		total.setComponent(configuration.getRocket());

		Map<RocketComponent, AerodynamicForces> eachMap = new LinkedHashMap<>();
		Map<RocketComponent, AerodynamicForces> assemblyMap = new LinkedHashMap<>();

		InstanceMap instances = configuration.getActiveInstances();
		for (RocketComponent component : instances.keySet()) {
			AerodynamicForces zero = new AerodynamicForces().zero();
			zero.setComponent(component);
			zero.setCN(0);
			zero.setCm(0);
			zero.setCP(Coordinate.ZERO);
			zero.setCside(0);
			zero.setCyaw(0);
			zero.setCroll(0);
			zero.setCrollDamp(0);
			zero.setCrollForce(0);

			if (component instanceof ComponentAssembly) {
				assemblyMap.put(component, zero);
			} else if (component.isAerodynamic()) {
				eachMap.put(component, zero);
			}
		}

		assemblyMap.put(configuration.getRocket(), total);

		return new StabilityForceBreakdown(eachMap, assemblyMap);
	}

	@Override
	public void calculateDampingMoments(FlightConfiguration configuration, FlightConditions conditions,
			AerodynamicForces total) {
		total.setPitchDampingMoment(0);
		total.setYawDampingMoment(0);
	}

	@Override
	public void checkGeometry(FlightConfiguration configuration, RocketComponent component, WarningSet warnings) {
		// Geometry validation is not required for lookup-based data
	}

	@Override
	public void voidAerodynamicCache() {
		// no-op
	}
}
