package info.openrocket.core.document;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.AerodynamicForces;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.CaliberUnit;
import info.openrocket.core.unit.PercentageOfLengthUnit;
import info.openrocket.core.util.Chars;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;

/**
 * Computes the derived "design information" of a rocket: static statistics
 * (length, diameters, masses, CG/CP, stability, drag, inertia) and nose-to-fin-root
 * distances. This is the single source of these numbers, shared by the printed
 * design report, the CSV export, and the {@code <designInfo>} block in the .ork file.
 *
 * <p>All returned values are raw SI base units (metres, kilograms, kg·m², radians),
 * with {@link Double#NaN} where a value could not be computed. Presentation (choosing
 * display units, formatting) is left to the callers.</p>
 */
public final class DesignInfo {

	// Canonical field labels, shared across the report, CSV, and .ork file.
	public static final String LENGTH = "Length";
	public static final String MAX_DIAMETER = "Max Diameter";
	public static final String MASS_EMPTY = "Mass (Empty)";
	public static final String MASS_LOADED = "Mass (Loaded)";
	public static final String FINENESS = "Fineness (L/D)";
	public static final String CG_EMPTY = "CG (Empty)";
	public static final String CG_LOADED = "CG (Loaded)";
	public static final String CP = "CP";
	public static final String STABILITY_ON_PAD = "Stability (on pad)";
	public static final String STABILITY_PERCENT = "Stability (%)";
	public static final String DRAG_COEFFICIENT = "Drag Coeff.";
	public static final String NORMAL_FORCE_SLOPE = "Normal-Force Slope (CN" + Chars.ALPHA + ")";
	public static final String PITCH_INERTIA = "Pitch Inertia (Loaded)";
	public static final String ROLL_INERTIA = "Roll Inertia (Loaded)";

	private DesignInfo() {
	}

	/**
	 * Raw static statistics for a configuration, in SI base units. {@code stabilityMargin}
	 * is the CP&minus;CG distance in metres; convert to calibers/percent for display. Any
	 * field may be {@link Double#NaN} when unavailable.
	 */
	public record StaticStats(
			double length, double maxDiameter, double massEmpty, double massLoaded,
			double fineness, double cgEmpty, double cgLoaded, double cp,
			double stabilityMargin, double dragCoefficient, double normalForceSlope,
			double mach, double pitchInertia, double rollInertia) {
	}

	/** Nose-tip-to-fin-root distances (metres) for a single fin set. */
	public record FinMeasurement(int stageNumber, String stageName, String finName,
			double noseToRootTop, double noseToRootBottom) {
	}

	/** A single statistic ready for the .ork file: field name, SI/canonical value, and unit token. */
	public record FileStat(String field, double value, String unit) {
	}

	/**
	 * Compute the static statistics for the active stages of {@code configuration}, at the
	 * preference default Mach number, using {@code aero} for CP/CNa/CD.
	 */
	public static StaticStats computeStatistics(FlightConfiguration configuration, AerodynamicCalculator aero) {
		final double length = configuration.getLength();
		double diameter = Double.NaN;
		for (RocketComponent c : configuration.getCoreComponents()) {
			if (c instanceof SymmetricComponent sc) {
				diameter = MathUtil.max(diameter, sc.getForeRadius() * 2, sc.getAftRadius() * 2);
			}
		}
		final double fineness = (!Double.isNaN(diameter) && diameter > 0) ? length / diameter : Double.NaN;

		final RigidBody launch = MassCalculator.calculateLaunch(configuration);
		final RigidBody structure = MassCalculator.calculateStructure(configuration);
		final double massLoaded = launch.getMass();
		final double massEmpty = structure.getMass();
		final double cgLoaded = launch.getCM().getX();
		final double cgEmpty = structure.getCM().getX();
		final double pitchInertia = launch.getLongitudinalInertia();
		final double rollInertia = launch.getRotationalInertia();

		final double mach = Application.getPreferences().getDefaultMach();
		double cp = Double.NaN;
		double cna = Double.NaN;
		double cd = Double.NaN;
		try {
			final FlightConditions conditions = new FlightConditions(configuration);
			conditions.setMach(mach);
			conditions.setAOA(0);
			final WarningSet warnings = new WarningSet();
			final CoordinateIF cpCoord = aero.getWorstCP(configuration, conditions, warnings);
			if (cpCoord.getWeight() > MathUtil.EPSILON) {
				cp = cpCoord.getX();
			}
			final AerodynamicForces forces = aero.getAerodynamicForces(configuration, conditions, warnings);
			if (forces.getCP().getWeight() > MathUtil.EPSILON) {
				cna = forces.getCP().getWeight();
			}
			cd = forces.getCD();
		} catch (Exception e) {
			// Leave aerodynamic values as NaN; the design may not be aerodynamically computable.
		}
		final double stabilityMargin = !Double.isNaN(cp) ? cp - cgLoaded : Double.NaN;

		return new StaticStats(length, diameter, massEmpty, massLoaded, fineness,
				cgEmpty, cgLoaded, cp, stabilityMargin, cd, cna, mach, pitchInertia, rollInertia);
	}

	/**
	 * Compute the statistics as a list of {@link FileStat}s in SI/canonical units, suitable for
	 * writing to the {@code <designInfo>} block. Values that are unavailable (NaN) are omitted.
	 */
	public static List<FileStat> fileStatistics(FlightConfiguration configuration, AerodynamicCalculator aero) {
		final StaticStats s = computeStatistics(configuration, aero);
		final String machStr = new DecimalFormat("0.##").format(s.mach());
		final List<FileStat> out = new ArrayList<>();

		addFileStat(out, LENGTH, s.length(), "m");
		addFileStat(out, MAX_DIAMETER, s.maxDiameter(), "m");
		addFileStat(out, MASS_EMPTY, s.massEmpty(), "kg");
		addFileStat(out, MASS_LOADED, s.massLoaded(), "kg");
		addFileStat(out, FINENESS, s.fineness(), "");
		addFileStat(out, CG_EMPTY, s.cgEmpty(), "m");
		addFileStat(out, CG_LOADED, s.cgLoaded(), "m");
		addFileStat(out, CP, s.cp(), "m");
		if (!Double.isNaN(s.stabilityMargin())) {
			addFileStat(out, STABILITY_ON_PAD, new CaliberUnit(configuration).toUnit(s.stabilityMargin()), "cal");
			addFileStat(out, STABILITY_PERCENT, new PercentageOfLengthUnit(configuration).toUnit(s.stabilityMargin()), "%");
		}
		addFileStat(out, DRAG_COEFFICIENT + " (Ma " + machStr + ")", s.dragCoefficient(), "");
		addFileStat(out, NORMAL_FORCE_SLOPE, s.normalForceSlope(), "1/rad");
		addFileStat(out, PITCH_INERTIA, s.pitchInertia(), "kg*m^2");
		addFileStat(out, ROLL_INERTIA, s.rollInertia(), "kg*m^2");
		return out;
	}

	private static void addFileStat(List<FileStat> out, String field, double value, String unit) {
		if (!Double.isNaN(value)) {
			out.add(new FileStat(field, value, unit));
		}
	}

	/**
	 * Nose-tip-to-fin-root distances for every fin set in the rocket, grouped by stage in
	 * tree order. Fin sets that share a name within a stage are suffixed with {@code #N}.
	 */
	public static List<FinMeasurement> finMeasurements(Rocket rocket) {
		final Map<AxialStage, List<FinSet>> finSetsByStage = new LinkedHashMap<>();
		for (RocketComponent component : rocket) {
			if (component instanceof FinSet finSet) {
				finSetsByStage.computeIfAbsent(finSet.getStage(), s -> new ArrayList<>()).add(finSet);
			}
		}

		final List<FinMeasurement> measurements = new ArrayList<>();
		for (Map.Entry<AxialStage, List<FinSet>> entry : finSetsByStage.entrySet()) {
			final AxialStage stage = entry.getKey();
			final List<FinSet> finSets = entry.getValue();

			final Map<String, Integer> nameCounts = new HashMap<>();
			for (FinSet finSet : finSets) {
				nameCounts.merge(finSet.getName(), 1, Integer::sum);
			}
			final Map<String, Integer> nameSeen = new HashMap<>();

			for (FinSet finSet : finSets) {
				final CoordinateIF[] locations = finSet.getComponentLocations();
				if (locations.length == 0) {
					continue;
				}
				final double top = locations[0].getX();
				final double bottom = top + finSet.getLength();

				String finName = finSet.getName();
				if (nameCounts.get(finName) > 1) {
					finName = finName + " #" + nameSeen.merge(finName, 1, Integer::sum);
				}

				final int stageNumber = stage != null ? stage.getStageNumber() : -1;
				final String stageName = stage != null ? stage.getName() : "";
				measurements.add(new FinMeasurement(stageNumber, stageName, finName, top, bottom));
			}
		}
		return measurements;
	}
}
