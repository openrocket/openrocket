package info.openrocket.core.aerodynamics.lookup;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility for constructing {@link MachAoALookup} instances from CSV data.
 */
public final class CsvMachAoALookup {

	private CsvMachAoALookup() {
	}

	public static MachAoALookup fromCsv(Path path, Collection<String> requiredValueColumns) {
		return fromCsv(path, requiredValueColumns, ',');
	}

	public static MachAoALookup fromCsv(Path path, Collection<String> requiredValueColumns, char separator) {
		Objects.requireNonNull(path, "path");
		Objects.requireNonNull(requiredValueColumns, "requiredValueColumns");
		try {
			List<String> lines = Files.readAllLines(path);
			return parse(lines, requiredValueColumns, separator);
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to read lookup table from " + path, e);
		}
	}

	private static MachAoALookup parse(List<String> lines, Collection<String> requiredValueColumns, char separator) {
		Set<String> normalizedColumns = MachAoALookup.normalizeColumns(requiredValueColumns);
		MachAoALookup.Builder builder = MachAoALookup.builder(requiredValueColumns);
		Map<String, Integer> headerIndex = null;
		String splitRegex = Pattern.quote(String.valueOf(separator));

		for (String rawLine : lines) {
			String line = rawLine.trim();
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}

			if (headerIndex == null) {
				headerIndex = parseHeader(line, normalizedColumns, splitRegex);
				continue;
			}

			String[] tokens = line.split(splitRegex, -1);
			double mach = parse(tokens, headerIndex.get("mach"), "mach");
			Double aoaDegrees = null;
			if (headerIndex.containsKey("aoa")) {
				aoaDegrees = parse(tokens, headerIndex.get("aoa"), "aoa");
			}

			Map<String, Double> values = new HashMap<>();
			for (String column : normalizedColumns) {
				if (column.equals("aoa")) {
					continue;
				}
				double value = parse(tokens, headerIndex.get(column), column);
				values.put(column, value);
			}

			builder.addData(mach, aoaDegrees, values);
		}

		if (headerIndex == null) {
			throw new IllegalArgumentException("Lookup table is missing a header row");
		}

		return builder.build();
	}

	private static Map<String, Integer> parseHeader(String headerLine, Collection<String> requiredValueColumns, String splitRegex) {
		String[] headers = headerLine.split(splitRegex, -1);
		Map<String, Integer> result = new HashMap<>();
		for (int i = 0; i < headers.length; i++) {
			String normalized = MachAoALookup.normalize(headers[i]);
			if (normalized.equals("angleofattack")) {
				normalized = "aoa";
			}
			if (!normalized.isEmpty()) {
				result.putIfAbsent(normalized, i);
			}
		}

		if (!result.containsKey("mach")) {
			throw new IllegalArgumentException("Lookup table header must contain a 'mach' column");
		}

		for (String column : requiredValueColumns) {
			String normalized = MachAoALookup.normalize(column);
			if (normalized.equals("angleofattack")) {
				normalized = "aoa";
			}
			if (!result.containsKey(normalized)) {
				throw new IllegalArgumentException("Lookup table header missing required column '" + column + "'");
			}
		}

		return result;
	}

	private static double parse(String[] tokens, Integer index, String column) {
		if (index == null) {
			throw new IllegalArgumentException("Column index missing for '" + column + "'");
		}
		if (index >= tokens.length) {
			throw new IllegalArgumentException("Row missing value for column '" + column + "'");
		}
		String token = tokens[index].trim();
		try {
			return Double.parseDouble(token);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Illegal numeric value '" + token + "' in column '" + column + "'", e);
		}
	}
}
