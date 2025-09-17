package info.openrocket.core.aerodynamics.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lookup table supporting interpolation in Mach and optional angle-of-attack dimensions.
 */
public final class MachAoALookup {

	private static final Logger log = LoggerFactory.getLogger(MachAoALookup.class);

	private final NavigableMap<Double, List<Row>> rowsByMach;
	private final Set<String> valueColumns;
	private final boolean hasAoA;
	private final double minMach;
	private final double maxMach;
	private final double minAoA;
	private final double maxAoA;

	private boolean warnedLowMach;
	private boolean warnedHighMach;
	private boolean warnedLowAoA;
	private boolean warnedHighAoA;

	private MachAoALookup(NavigableMap<Double, List<Row>> rowsByMach,
			Set<String> valueColumns,
			boolean hasAoA,
			double minMach,
			double maxMach,
			double minAoA,
			double maxAoA) {
		this.rowsByMach = rowsByMach;
		this.valueColumns = valueColumns;
		this.hasAoA = hasAoA;
		this.minMach = minMach;
		this.maxMach = maxMach;
		this.minAoA = minAoA;
		this.maxAoA = maxAoA;
	}

	public boolean hasAoA() {
		return hasAoA;
	}

	public double getMinAoA() {
		return minAoA;
	}

	public double getMaxAoA() {
		return maxAoA;
	}

	public double interpolate(double mach, double aoaDegrees, String column) {
		String normalizedColumn = normalize(column);
		if (!valueColumns.contains(normalizedColumn)) {
			throw new IllegalArgumentException("Column '" + column + "' is not present in the table");
		}

		double clampedMach = clampMach(mach);

		Double lowerMach = rowsByMach.floorKey(clampedMach);
		Double upperMach = rowsByMach.ceilingKey(clampedMach);

		if (lowerMach == null) {
			lowerMach = rowsByMach.firstKey();
		}
		if (upperMach == null) {
			upperMach = rowsByMach.lastKey();
		}

		double lowerValue = interpolateAoA(rowsByMach.get(lowerMach), aoaDegrees, normalizedColumn);
		double upperValue = interpolateAoA(rowsByMach.get(upperMach), aoaDegrees, normalizedColumn);

		if (Double.compare(lowerMach, upperMach) == 0) {
			return lowerValue;
		}

		double fraction = (clampedMach - lowerMach) / (upperMach - lowerMach);
		return lerp(lowerValue, upperValue, fraction);
	}

	private double interpolateAoA(List<Row> rows, double aoaDegrees, String column) {
		if (!hasAoA || rows.size() == 1) {
			return rows.get(0).values.get(column);
		}

		double clampedAoA = clampAoA(aoaDegrees);

		Row lower = rows.get(0);
		Row upper = rows.get(rows.size() - 1);
		for (Row row : rows) {
			if (row.aoa <= clampedAoA) {
				lower = row;
			}
			if (row.aoa >= clampedAoA) {
				upper = row;
				break;
			}
		}

		if (Double.compare(lower.aoa, upper.aoa) == 0) {
			return lower.values.get(column);
		}

		double fraction = (clampedAoA - lower.aoa) / (upper.aoa - lower.aoa);
		double lowerVal = lower.values.get(column);
		double upperVal = upper.values.get(column);
		return lerp(lowerVal, upperVal, fraction);
	}

	private double clampMach(double mach) {
		if (mach < minMach) {
			if (!warnedLowMach) {
				log.warn("Requested Mach {} below table minimum {}; clamping", mach, minMach);
				warnedLowMach = true;
			}
			return minMach;
		}
		if (mach > maxMach) {
			if (!warnedHighMach) {
				log.warn("Requested Mach {} above table maximum {}; clamping", mach, maxMach);
				warnedHighMach = true;
			}
			return maxMach;
		}
		return mach;
	}

	private double clampAoA(double aoa) {
		if (!hasAoA) {
			return aoa;
		}
		if (aoa < minAoA) {
			if (!warnedLowAoA) {
				log.warn("Requested AoA {} below table minimum {}; clamping", aoa, minAoA);
				warnedLowAoA = true;
			}
			return minAoA;
		}
		if (aoa > maxAoA) {
			if (!warnedHighAoA) {
				log.warn("Requested AoA {} above table maximum {}; clamping", aoa, maxAoA);
				warnedHighAoA = true;
			}
			return maxAoA;
		}
		return aoa;
	}

	private static double lerp(double a, double b, double fraction) {
		return a + (b - a) * fraction;
	}

	public static Builder builder(Collection<String> valueColumns) {
		return new Builder(valueColumns);
	}

	public static Builder dragBuilder() {
		return new Builder(Set.of("cd"));
	}

	public static Builder stabilityBuilder() {
		return new Builder(Set.of("cn", "cm", "cp"));
	}

	static String normalize(String value) {
		String lower = value.trim().toLowerCase(Locale.ROOT);
		return lower.replaceAll("[\\s_]", "");
	}

	static Set<String> normalizeColumns(Collection<String> columns) {
		Set<String> normalized = new LinkedHashSet<>();
		for (String column : columns) {
			String name = normalize(column);
			if (name.equals("angleofattack")) {
				name = "aoa";
			}
			normalized.add(name);
		}
		return normalized;
	}

	private static MachAoALookup buildFromRows(List<Row> parsedRows, Set<String> normalizedColumns, boolean hasAoA) {
		NavigableMap<Double, List<Row>> byMach = new TreeMap<>();
		double minMach = Double.POSITIVE_INFINITY;
		double maxMach = Double.NEGATIVE_INFINITY;
		double minAoA = Double.POSITIVE_INFINITY;
		double maxAoA = Double.NEGATIVE_INFINITY;

		for (Row row : parsedRows) {
			byMach.computeIfAbsent(row.mach, m -> new ArrayList<>()).add(row);
			minMach = Math.min(minMach, row.mach);
			maxMach = Math.max(maxMach, row.mach);
			if (hasAoA) {
				minAoA = Math.min(minAoA, row.aoa);
				maxAoA = Math.max(maxAoA, row.aoa);
			}
		}

		for (List<Row> rows : byMach.values()) {
			rows.sort((a, b) -> Double.compare(a.aoa, b.aoa));
		}

		if (!hasAoA) {
			minAoA = Double.NaN;
			maxAoA = Double.NaN;
		}

		return new MachAoALookup(byMach, Set.copyOf(normalizedColumns), hasAoA, minMach, maxMach, minAoA, maxAoA);
	}

	public static final class Builder {
		private final Set<String> valueColumns;
		private final List<Row> rows = new ArrayList<>();
		private Boolean usesAoA = null;

		private Builder(Collection<String> valueColumns) {
			Set<String> normalized = normalizeColumns(valueColumns);
			if (normalized.isEmpty()) {
				throw new IllegalArgumentException("At least one value column is required");
			}
			this.valueColumns = Set.copyOf(normalized);
		}

		public Builder addData(double mach, Map<String, Double> values) {
			return addData(mach, null, values);
		}

		public Builder addData(double mach, Double aoaDegrees, Map<String, Double> values) {
			Objects.requireNonNull(values, "values");
			boolean rowHasAoA = aoaDegrees != null;
			if (usesAoA == null) {
				usesAoA = rowHasAoA;
			} else if (usesAoA.booleanValue() != rowHasAoA) {
				throw new IllegalArgumentException("Inconsistent AoA usage across data rows");
			}

			Map<String, Double> normalizedValues = new HashMap<>();
			for (String column : valueColumns) {
				Double value = findValue(values, column);
				if (value == null) {
					throw new IllegalArgumentException("Value for column '" + column + "' missing");
				}
				normalizedValues.put(column, value);
			}

			double aoa = rowHasAoA ? aoaDegrees.doubleValue() : 0.0;
			rows.add(new Row(mach, aoa, normalizedValues));
			return this;
		}

		public Builder addDragData(double mach, double cd) {
			ensureColumns(Set.of("cd"));
			return addData(mach, null, Map.of("cd", cd));
		}

		public Builder addDragData(double mach, double aoaDegrees, double cd) {
			ensureColumns(Set.of("cd"));
			return addData(mach, aoaDegrees, Map.of("cd", cd));
		}

		public Builder addStabilityData(double mach, double cn, double cm, double cp) {
			ensureColumns(Set.of("cn", "cm", "cp"));
			return addData(mach, null, Map.of("cn", cn, "cm", cm, "cp", cp));
		}

		public Builder addStabilityData(double mach, double aoaDegrees, double cn, double cm, double cp) {
			ensureColumns(Set.of("cn", "cm", "cp"));
			return addData(mach, aoaDegrees, Map.of("cn", cn, "cm", cm, "cp", cp));
		}

		public MachAoALookup build() {
			if (rows.isEmpty()) {
				throw new IllegalStateException("No lookup data added");
			}
			boolean hasAoA = usesAoA != null && usesAoA.booleanValue();
			return buildFromRows(new ArrayList<>(rows), valueColumns, hasAoA);
		}

		private static Double findValue(Map<String, Double> values, String column) {
			if (values.containsKey(column)) {
				return values.get(column);
			}
			for (Map.Entry<String, Double> entry : values.entrySet()) {
				if (normalize(entry.getKey()).equals(column)) {
					return entry.getValue();
				}
			}
			return null;
		}

		private void ensureColumns(Set<String> expected) {
			if (!valueColumns.equals(expected)) {
				throw new IllegalStateException("Builder configured for columns " + valueColumns +
						" cannot accept data for " + expected);
			}
		}
	}

	private static final class Row {
		final double mach;
		final double aoa;
		final Map<String, Double> values;

		Row(double mach, double aoa, Map<String, Double> values) {
			this.mach = mach;
			this.aoa = aoa;
			this.values = Collections.unmodifiableMap(new HashMap<>(values));
		}
	}
}
