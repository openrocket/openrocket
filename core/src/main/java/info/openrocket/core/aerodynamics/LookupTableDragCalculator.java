package info.openrocket.core.aerodynamics;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import info.openrocket.core.aerodynamics.lookup.CsvMachAoALookup;
import info.openrocket.core.aerodynamics.lookup.MachAoALookup;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.PolyInterpolator;

/**
 * Drag calculator backed by a CSV lookup table.
 */
public class LookupTableDragCalculator implements DragCalculator {

	private static final String COLUMN_CD = "cd";
	private static final double[] AXIAL_POLY1;
	private static final double[] AXIAL_POLY2;

	static {
		PolyInterpolator interpolator = new PolyInterpolator(
				new double[] { 0, 17 * Math.PI / 180 },
				new double[] { 0, 17 * Math.PI / 180 });
		AXIAL_POLY1 = interpolator.interpolator(1, 1.3, 0, 0);

		interpolator = new PolyInterpolator(
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { 17 * Math.PI / 180, Math.PI / 2 },
				new double[] { Math.PI / 2 });
		AXIAL_POLY2 = interpolator.interpolator(1.3, 0, 0, 0, 0);
	}

	private final MachAoALookup table;

	public LookupTableDragCalculator(Path csvPath) {
		this(CsvMachAoALookup.fromCsv(csvPath, List.of(COLUMN_CD)));
	}

	public LookupTableDragCalculator(MachAoALookup table) {
		this.table = table;
	}

	@Override
	public DragCalculator newInstance() {
		return new LookupTableDragCalculator(table);
	}

	@Override
	public void calculateDrag(FlightConfiguration configuration,
			FlightConditions conditions,
			Map<RocketComponent, AerodynamicForces> componentForces,
			Map<RocketComponent, AerodynamicForces> assemblyForces,
			AerodynamicForces totalForces,
			WarningSet warnings) {

		double mach = conditions.getMach();
		double aoaDegrees = Math.toDegrees(conditions.getAOA());
		double cd = table.interpolate(mach, aoaDegrees, COLUMN_CD);

		if (componentForces != null) {
			for (Map.Entry<RocketComponent, AerodynamicForces> entry : componentForces.entrySet()) {
				AerodynamicForces forces = entry.getValue();
				if (forces == null) {
					continue;
				}
				forces.setFrictionCD(0);
				forces.setPressureCD(0);
				forces.setBaseCD(0);
				forces.setOverrideCD(0);
				forces.setCD(0);
				forces.setCDaxial(0);
			}
		}

		if (assemblyForces != null) {
			for (Map.Entry<RocketComponent, AerodynamicForces> entry : assemblyForces.entrySet()) {
				AerodynamicForces forces = entry.getValue();
				if (forces == null) {
					continue;
				}
				forces.setFrictionCD(0);
				forces.setPressureCD(0);
				forces.setBaseCD(0);
				forces.setOverrideCD(0);
				forces.setCD(0);
				forces.setCDaxial(0);
			}
		}

		if (totalForces != null) {
			totalForces.setFrictionCD(cd);
			totalForces.setPressureCD(0);
			totalForces.setBaseCD(0);
			totalForces.setOverrideCD(0);
			totalForces.setCD(cd);
			totalForces.setCDaxial(toAxialDrag(conditions, cd));
		}
	}

	@Override
	public double toAxialDrag(FlightConditions conditions, double cd) {
		double aoa = MathUtil.clamp(conditions.getAOA(), 0, Math.PI);
		boolean positive = aoa <= Math.PI / 2;
		if (!positive) {
			aoa = Math.PI - aoa;
		}
		double mul;
		if (aoa < 17 * Math.PI / 180) {
			mul = PolyInterpolator.eval(aoa, AXIAL_POLY1);
		} else {
			mul = PolyInterpolator.eval(aoa, AXIAL_POLY2);
		}
		double axial = mul * cd;
		return positive ? axial : -axial;
	}

	@Override
	public void voidAerodynamicCache() {
		// no-op
	}
}
