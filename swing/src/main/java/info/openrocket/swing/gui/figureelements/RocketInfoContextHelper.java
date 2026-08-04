package info.openrocket.swing.gui.figureelements;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.simulation.FlightData;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;

/**
 * Shared helpers for populating {@link RocketInfo} from the current document/configuration context.
 */
public final class RocketInfoContextHelper {

	private RocketInfoContextHelper() {
	}

	/**
	 * Computed physical properties of a rocket configuration.
	 * The 2D view uses {@code cp} and {@code cg} for caret positioning; both views
	 * use {@code length} to guard figure-level updates.
	 */
	public record RocketPhysics(CoordinateIF cp, CoordinateIF cg, double length) {}

	/**
	 * Computes the physical properties of {@code configuration} (length, diameter,
	 * CG, CP, mass) and applies them to {@code rocketInfo}.  Returns the raw CP/CG
	 * vectors and rocket length so callers can do view-specific work (e.g. 2D
	 * projection, caret positioning) without re-computing.
	 */
	public static RocketPhysics computePhysics(
			FlightConfiguration configuration,
			FlightConditions conditions,
			WarningSet warnings,
			AerodynamicCalculator aerodynamicCalculator,
			boolean useWorstCp,
			RocketInfo rocketInfo) {

		double length = configuration.getLength();

		double diameter = Double.NaN;
		for (RocketComponent c : configuration.getCoreComponents()) {
			if (c instanceof SymmetricComponent sc) {
				diameter = MathUtil.max(diameter, sc.getForeRadius() * 2, sc.getAftRadius() * 2);
			}
		}

		CoordinateIF cp = calculateCp(configuration, conditions, warnings, aerodynamicCalculator, useWorstCp);
		CoordinateIF cg = MassCalculator.calculateLaunch(configuration).getCM();
		RigidBody emptyInfo = MassCalculator.calculateStructure(configuration);

		rocketInfo.setLength(length);
		rocketInfo.setDiameter(diameter);
		applyCgAndCp(rocketInfo, cg, cp);
		rocketInfo.setMassWithMotors(cg.getWeight());
		rocketInfo.setMassWithoutMotors(emptyInfo.getMass());
		rocketInfo.setWarnings(warnings);

		return new RocketPhysics(cp, cg, length);
	}

	/**
	 * Applies CG and CP coordinates to the values shown by {@link RocketInfo}.
	 *
	 * <p>A coordinate is only meaningful when its weight is non-zero.  In
	 * particular, {@code getWorstCP()} returns a sentinel at {@link Double#MAX_VALUE}
	 * when a design has no normal-force contribution, such as a lone body tube.
	 * Both the 2D information panel and the 3D HUD must pass through this method so
	 * they display that case as CP zero instead of exposing the sentinel.</p>
	 */
	public static void applyCgAndCp(RocketInfo rocketInfo, CoordinateIF cg, CoordinateIF cp) {
		if (cg != null) {
			rocketInfo.setCG(cg.getWeight() > MassCalculator.MIN_MASS ? cg.getX() : 0);
		}
		if (cp != null) {
			rocketInfo.setCP(cp.getWeight() > MathUtil.EPSILON ? cp.getX() : 0);
		}
	}

	public static CoordinateIF calculateCp(
			FlightConfiguration configuration,
			FlightConditions conditions,
			WarningSet warnings,
			AerodynamicCalculator aerodynamicCalculator,
			boolean useWorstCp) {
		warnings.clear();
		if (useWorstCp) {
			return aerodynamicCalculator.getWorstCP(configuration, conditions, warnings);
		}
		return aerodynamicCalculator.getCP(configuration, conditions, warnings);
	}

	public static Simulation findCurrentConfigurationSimulation(
			OpenRocketDocument document,
			FlightConfiguration configuration) {
		for (Simulation simulation : document.getSimulations()) {
			if (simulation.getFlightConfigurationId().compareTo(configuration.getFlightConfigurationID()) == 0) {
				return simulation;
			}
		}
		return null;
	}

	public static boolean shouldShowCalculatingState(
			FlightConfiguration configuration,
			Simulation simulation) {
		if (!configuration.hasMotors()) {
			return false;
		}
		if (simulation == null) {
			return true;
		}
		return !Simulation.isStatusUpToDate(simulation.getStatus());
	}

	public static void applyCurrentConfigurationSimulation(
			OpenRocketDocument document,
			FlightConfiguration configuration,
			RocketInfo rocketInfo) {
		Simulation simulation = findCurrentConfigurationSimulation(document, configuration);
		rocketInfo.setSimulation(simulation);
		if (simulation == null) {
			rocketInfo.setFlightData(FlightData.NaN_DATA);
		}
		rocketInfo.setCalculatingData(shouldShowCalculatingState(configuration, simulation));
	}
}
