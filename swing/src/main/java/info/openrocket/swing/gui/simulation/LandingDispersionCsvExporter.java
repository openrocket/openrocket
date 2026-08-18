package info.openrocket.swing.gui.simulation;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.openrocket.core.simulation.montecarlo.LandingBody;
import info.openrocket.core.simulation.montecarlo.LandingPoint;
import info.openrocket.core.simulation.montecarlo.MonteCarloParameter;
import info.openrocket.core.simulation.montecarlo.MonteCarloResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloRunResult;
import info.openrocket.core.simulation.montecarlo.MonteCarloSettings;
import info.openrocket.core.simulation.montecarlo.UncertaintySpec;

/**
 * Writes landing-dispersion runs in a reproducible, machine-readable CSV format.
 * Each row represents one run/body pair and includes both the sampled input
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
		List<LandingBody> bodies = result.getLandingBodies();
		List<MonteCarloRunResult> runs = new ArrayList<>(result.getRunResults().size() + 1);
		runs.add(result.getNominalResult());
		runs.addAll(result.getRunResults());

		for (MonteCarloRunResult run : runs) {
			if (bodies.isEmpty()) {
				writeRow(writer, result, settings, run, null, null);
				continue;
			}
			for (LandingBody body : bodies) {
				writeRow(writer, result, settings, run, body, run.getLandingPoint(body.branchIndex()));
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
		fields.add("body_index");
		fields.add("body_name");
		fields.add("east_m");
		fields.add("north_m");
		fields.add("range_from_pad_m");
		fields.add("maximum_altitude_m");
		fields.add("flight_time_s");
		for (MonteCarloParameter parameter : MonteCarloParameter.values()) {
			fields.add(parameter.name().toLowerCase(Locale.ROOT) + "_delta_" + parameterUnit(parameter));
		}
		writeFields(writer, fields);
	}

	private static void writeRow(Writer writer, MonteCarloResult result, String settings,
			MonteCarloRunResult run, LandingBody body, LandingPoint point) throws IOException {
		List<String> fields = new ArrayList<>();
		fields.add(Integer.toString(result.getSettings().getSeed()));
		fields.add(Integer.toString(result.getSettings().getRunCount()));
		fields.add(settings);
		fields.add(Integer.toString(run.sample().getRunNumber()));
		fields.add(Integer.toString(run.sample().getSimulationSeed()));
		fields.add(status(run, point));
		fields.add(valueOrEmpty(run.failureMessage()));
		fields.add(body == null ? "" : Integer.toString(body.branchIndex()));
		fields.add(body == null ? "" : body.branchName());
		fields.add(point == null ? "" : Double.toString(point.east()));
		fields.add(point == null ? "" : Double.toString(point.north()));
		fields.add(point == null ? "" : Double.toString(point.rangeFromPad()));
		fields.add(finiteOrEmpty(run.maximumAltitude()));
		fields.add(finiteOrEmpty(run.flightTime()));
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

	private static String status(MonteCarloRunResult run, LandingPoint point) {
		if (run.failureMessage() != null) {
			return "failed";
		}
		if (point == null) {
			return "missing_landing";
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
