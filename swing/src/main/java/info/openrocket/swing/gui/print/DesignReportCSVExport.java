package info.openrocket.swing.gui.print;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.DesignType;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.StringUtils;

/**
 * Exports the design-report information (metadata and static statistics) as CSV.
 * The picture/diagram parts of the printed report have no CSV equivalent and are
 * omitted; only the textual data is written.
 *
 * <p>The layout is a simple long format with columns {@code Scope,Field,Value,Unit}: a
 * {@code Design} scope for the metadata, a {@code Rocket} scope for the whole-rocket
 * statistics, and one scope per stage (named after the stage) for multi-stage designs.
 * The value and unit are kept in separate columns. The statistics come from
 * {@link DesignReport#computeStaticStats} so the CSV and the printed report always agree.</p>
 */
public final class DesignReportCSVExport {

	private static final String DESIGN_SCOPE = "Design";
	private static final String ROCKET_SCOPE = "Rocket";

	private DesignReportCSVExport() {
	}

	/**
	 * Write the design information for the given document as CSV to the writer.
	 *
	 * @param document the document whose rocket is exported
	 * @param writer   the destination writer (the caller is responsible for closing it)
	 * @throws IOException if writing fails
	 */
	public static void export(final OpenRocketDocument document, final Writer writer) throws IOException {
		final Rocket rocket = document.getRocket();
		final FlightConfiguration configuration = rocket.getSelectedConfiguration();
		configuration.setAllStages();
		final AerodynamicCalculator aero = new BarrowmanCalculator();

		writeRow(writer, "Scope", "Field", "Value", "Unit");

		// Design metadata (no units)
		writeRow(writer, DESIGN_SCOPE, "Name", rocket.getName(), "");
		if (rocket.getDesigner() != null && !rocket.getDesigner().trim().isEmpty()) {
			writeRow(writer, DESIGN_SCOPE, "Designer", rocket.getDesigner().trim(), "");
		}
		final DesignType designType = rocket.getDesignType();
		if (designType != null) {
			writeRow(writer, DESIGN_SCOPE, "Design Type", designType.getName(), "");
		}
		if (rocket.getKitName() != null && !rocket.getKitName().trim().isEmpty()) {
			writeRow(writer, DESIGN_SCOPE, "Kit", rocket.getKitName().trim(), "");
		}
		writeRow(writer, DESIGN_SCOPE, "Stages", Integer.toString(rocket.getStageCount()), "");

		// Whole-rocket statistics
		writeStats(writer, ROCKET_SCOPE, DesignReport.computeStaticStats(configuration, aero));

		// Per-stage statistics (multi-stage designs only)
		if (rocket.getStageCount() > 1) {
			for (AxialStage stage : configuration.getActiveStages()) {
				final FlightConfiguration stageConfig = configuration.clone();
				stageConfig.setOnlyStage(stage.getStageNumber());
				writeStats(writer, stage.getName(), DesignReport.computeStaticStats(stageConfig, aero));
			}
		}

		// Fin root positions, per fin set (scoped by stage)
		writeFinMeasurements(writer, rocket);

		writer.flush();
	}

	/**
	 * Write, for every fin set in the rocket, the axial distance from the nose-cone tip
	 * to the top (fore) and bottom (aft) of the fin root. The rows are grouped by stage
	 * (scoped by the stage name for multi-stage designs, or {@code Rocket} for a single
	 * stage) and identified by the fin set's name, so the measurements are given per fin
	 * set and per stage. Fin sets that share a name within a stage are numbered to keep
	 * them distinct.
	 *
	 * @param writer the destination writer
	 * @param rocket the rocket to measure
	 * @throws IOException if writing fails
	 */
	private static void writeFinMeasurements(final Writer writer, final Rocket rocket) throws IOException {
		final Unit lengthUnit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
		final boolean multiStage = rocket.getStageCount() > 1;

		// Group fin sets by stage, preserving tree order.
		final Map<AxialStage, List<FinSet>> finSetsByStage = new LinkedHashMap<>();
		for (RocketComponent component : rocket) {
			if (component instanceof FinSet finSet) {
				finSetsByStage.computeIfAbsent(finSet.getStage(), s -> new ArrayList<>()).add(finSet);
			}
		}

		for (Map.Entry<AxialStage, List<FinSet>> entry : finSetsByStage.entrySet()) {
			final AxialStage stage = entry.getKey();
			final List<FinSet> finSets = entry.getValue();
			final String scope = (multiStage && stage != null) ? stage.getName() : ROCKET_SCOPE;

			// Count names to know which need a disambiguating suffix.
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
				// The fin set's reference point is the fore end of the root; the root
				// chord (its length) extends aft from there.
				final double rootTop = locations[0].getX();
				final double rootBottom = rootTop + finSet.getLength();

				String finName = finSet.getName();
				if (nameCounts.get(finName) > 1) {
					finName = finName + " #" + nameSeen.merge(finName, 1, Integer::sum);
				}

				writeRow(writer, scope, finName + ": Nose to top of fin root",
						lengthUnit.toString(rootTop), lengthUnit.getUnit());
				writeRow(writer, scope, finName + ": Nose to bottom of fin root",
						lengthUnit.toString(rootBottom), lengthUnit.getUnit());
			}
		}
	}

	private static void writeStats(final Writer writer, final String scope, final List<DesignReport.Stat> stats)
			throws IOException {
		for (DesignReport.Stat stat : stats) {
			writeRow(writer, scope, stat.label(), stat.value(), stat.unit());
		}
	}

	private static void writeRow(final Writer writer, final String scope, final String field, final String value,
			final String unit) throws IOException {
		writer.write(StringUtils.escapeCSV(scope));
		writer.write(',');
		writer.write(StringUtils.escapeCSV(field));
		writer.write(',');
		writer.write(StringUtils.escapeCSV(value));
		writer.write(',');
		writer.write(StringUtils.escapeCSV(unit));
		writer.write("\r\n");
	}
}
