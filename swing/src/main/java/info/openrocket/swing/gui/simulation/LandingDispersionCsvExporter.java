package info.openrocket.swing.gui.simulation;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.openrocket.core.simulation.montecarlo.LandingPoint;
import info.openrocket.core.simulation.montecarlo.MonteCarloBranchResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloFlightBranch;
import info.openrocket.core.simulation.montecarlo.MonteCarloMetric;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloRunResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.simulation.montecarlo.UncertaintySpec;

/**
 * Writes Monte Carlo runs in a reproducible, machine-readable CSV format.
 * Each row represents one run/flight-branch pair and includes both the sampled input
 * deviations and the analysis configuration needed to interpret them.
 */
final class LandingDispersionCsvExporter {
	private LandingDispersionCsvExporter() {
	}

	/**
	 * Write the nominal trajectory followed by every dispersed trajectory.
	 *
	 * @param writer destination writer; ownership remains with the caller
	 * @param result completed landing-dispersion analysis
	 * @throws IOException if the destination cannot be written
	 */
	static void write(Writer writer, MonteCarloResult result) throws IOException {
		writeHeader(writer);
		String settings = encodeSettings(result.getSettings());
		List<MonteCarloFlightBranch> branches = result.getFlightBranches();
		List<MonteCarloRunResult> runs = new ArrayList<>(result.getRunResults().size() + 1);
		runs.add(result.getNominalResult());
		runs.addAll(result.getRunResults());

		for (MonteCarloRunResult run : runs) {
			if (branches.isEmpty()) {
				writeRow(writer, result, settings, run, null, null);
				continue;
			}
			for (MonteCarloFlightBranch branch : branches) {
				writeRow(writer, result, settings, run, branch, run.getLandingPoint(branch.branchId()));
			}
		}
	}

	private static void writeHeader(Writer writer) throws IOException {
		List<String> fields = new ArrayList<>();
		fields.add("analysis_seed");
		fields.add("configured_run_count");
		fields.add("uncertainty_settings_si");
		fields.add("run");
		fields.add("simulation_seed");
		fields.add("status");
		fields.add("failure_message");
		fields.add("branch_id");
		fields.add("branch_index");
		fields.add("branch_name");
		fields.add("east_m");
		fields.add("north_m");
		fields.add("range_from_pad_m");
		for (MonteCarloMetric metric : MonteCarloMetric.values()) {
			fields.add(metric.name().toLowerCase(Locale.ROOT) + "_" + metricUnit(metric));
		}
		for (MonteCarloParameter parameter : MonteCarloParameter.values()) {
			fields.add(parameter.name().toLowerCase(Locale.ROOT) + "_delta_" + parameterUnit(parameter));
		}
		writeFields(writer, fields);
	}

	private static void writeRow(Writer writer, MonteCarloResult result, String settings,
			MonteCarloRunResult run, MonteCarloFlightBranch branch, LandingPoint point) throws IOException {
		List<String> fields = new ArrayList<>();
		fields.add(Integer.toString(result.getSettings().getSeed()));
		fields.add(Integer.toString(result.getSettings().getRunCount()));
		fields.add(settings);
		fields.add(Integer.toString(run.sample().getRunNumber()));
		fields.add(Integer.toString(run.sample().getSimulationSeed()));
		MonteCarloBranchResult branchResult = branch == null ? null : run.getBranchResult(branch.branchId());
		String failure = run.failureMessage() != null ? run.failureMessage()
				: branchResult == null ? null : branchResult.failureMessage();
		fields.add(status(run, branchResult, failure));
		fields.add(valueOrEmpty(failure));
		fields.add(branch == null ? "" : branch.branchId());
		int branchIndex = branchResult == null ? -1 : branchResult.branchIndex();
		fields.add(branchIndex < 0 ? "" : Integer.toString(branchIndex));
		fields.add(branch == null ? "" : branch.branchName());
		fields.add(point == null ? "" : Double.toString(point.east()));
		fields.add(point == null ? "" : Double.toString(point.north()));
		fields.add(point == null ? "" : Double.toString(point.rangeFromPad()));
		for (MonteCarloMetric metric : MonteCarloMetric.values()) {
			fields.add(branchResult == null ? "" : finiteOrEmpty(branchResult.getMetric(metric)));
		}
		for (MonteCarloParameter parameter : MonteCarloParameter.values()) {
			fields.add(Double.toString(run.sample().getVariation(parameter)));
		}
		writeFields(writer, fields);
	}

	private static String encodeSettings(MonteCarloSettings settings) {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<MonteCarloParameter, UncertaintySpec> entry : settings.getUncertainties().entrySet()) {
			if (builder.length() > 0) {
				builder.append(';');
			}
			builder.append(entry.getKey().name())
					.append(':').append(entry.getValue().distribution().name())
					.append(':').append(entry.getValue().spread());
		}
		return builder.toString();
	}

	private static String parameterUnit(MonteCarloParameter parameter) {
		if (parameter.isRelative()) {
			return "relative";
		}
		return switch (parameter) {
			case WIND_SPEED -> "m_per_s";
			case WIND_DIRECTION, LAUNCH_GUIDE_ANGLE, LAUNCH_GUIDE_DIRECTION -> "rad";
			case CG_AXIAL -> "m";
			case IGNITION_DELAY, DEPLOYMENT_DELAY -> "s";
			default -> throw new IllegalStateException("Missing CSV unit for parameter " + parameter);
		};
	}

	private static String metricUnit(MonteCarloMetric metric) {
		return switch (metric) {
			case APOGEE_ALTITUDE -> "m";
			case MAXIMUM_VELOCITY, LANDING_VELOCITY -> "m_per_s";
			case MAXIMUM_MACH -> "coefficient";
			case MAXIMUM_ACCELERATION -> "m_per_s2";
			case TIME_TO_APOGEE, FLIGHT_TIME -> "s";
		};
	}

	private static String status(MonteCarloRunResult run, MonteCarloBranchResult branch, String failure) {
		if (failure != null) {
			return "failed";
		}
		if (branch == null) {
			return "missing_branch";
		}
		return run.sample().getRunNumber() == 0 ? "nominal" : "success";
	}

	private static String finiteOrEmpty(double value) {
		return Double.isFinite(value) ? Double.toString(value) : "";
	}

	private static String valueOrEmpty(String value) {
		return value == null ? "" : value;
	}

	private static void writeFields(Writer writer, List<String> fields) throws IOException {
		for (int i = 0; i < fields.size(); i++) {
			if (i > 0) {
				writer.write(',');
			}
			writeQuoted(writer, fields.get(i));
		}
		writer.write(System.lineSeparator());
	}

	private static void writeQuoted(Writer writer, String value) throws IOException {
		writer.write('"');
		writer.write(value.replace("\"", "\"\""));
		writer.write('"');
	}
}
