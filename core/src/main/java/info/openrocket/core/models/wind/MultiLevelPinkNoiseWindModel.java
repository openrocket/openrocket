package info.openrocket.core.models.wind;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.DegreeUnit;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;

public class MultiLevelPinkNoiseWindModel implements WindModel {
	private List<LevelWindModel> levels;
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private final List<StateChangeListener> listeners = new ArrayList<>();

	private static final int REQUIRED_NR_OF_CSV_COLUMNS = 3;		// alt, speed, dir

	private AltitudeReference altitudeReference;

	public MultiLevelPinkNoiseWindModel() {
		this.levels = new ArrayList<>();
		this.altitudeReference = AltitudeReference.MSL;

		// Add a default wind level
		addInitialLevel();
	}

	public void addWindLevel(double altitude, double speed, double direction, Double standardDeviation) {
		PinkNoiseWindModel pinkNoiseModel = new PinkNoiseWindModel();
		pinkNoiseModel.setDirection(direction);
		pinkNoiseModel.setAverage(speed);
		if (standardDeviation != null) {
			pinkNoiseModel.setStandardDeviation(standardDeviation);
		}

		LevelWindModel newLevel = new LevelWindModel(altitude, pinkNoiseModel);
		newLevel.addChangeListener(e -> fireChangeEvent());
		int index = Collections.binarySearch(levels, newLevel, Comparator.comparingDouble(l -> l.altitude));
		if (index >= 0) {
			throw new IllegalArgumentException("Wind level already exists for altitude: " + altitude);
		}
		levels.add(-index - 1, newLevel);
		fireChangeEvent();
	}

	public void addWindLevel(double altitude, double speed, double direction) {
		addWindLevel(altitude, speed, direction, null);
	}

	private void addInitialLevel() {
		addWindLevel(0, prefs.getAverageWindModel().getAverage(), prefs.getAverageWindModel().getDirection(),
				prefs.getAverageWindModel().getStandardDeviation());
	}

	public void removeWindLevel(double altitude) {
		levels.removeIf(level -> level.altitude == altitude);
		fireChangeEvent();
	}

	public void removeWindLevelIdx(int index) {
		levels.remove(index);
		fireChangeEvent();
	}

	/**
	 * Clear all current levels.
	 */
	public void clearLevels() {
		levels.clear();
		fireChangeEvent();
	}

	/**
	 * Clear all current levels and add a default wind level.
	 */
	public void resetLevels() {
		levels.clear();
		addInitialLevel();
		fireChangeEvent();
	}

	public List<LevelWindModel> getLevels() {
		return new ArrayList<>(levels);
	}

	public void sortLevels() {
		levels.sort(Comparator.comparingDouble(l -> l.altitude));
	}

	@Override
	public Coordinate getWindVelocity(double time, double altitudeMSL, double altitudeAGL) {
		if (altitudeReference == AltitudeReference.MSL) {
			return getWindVelocity(time, altitudeMSL);
		} else {
			return getWindVelocity(time, altitudeAGL);
		}
	}

	@Override
	public Coordinate getWindVelocity(double time, double altitude) {
		if (levels.isEmpty()) {
			return Coordinate.ZERO;
		}

		int index = Collections.binarySearch(levels, new LevelWindModel(altitude, null),
				Comparator.comparingDouble(l -> l.altitude));

		// Retrieve the wind level if it exists
		if (index >= 0) {
			return levels.get(index).model.getWindVelocity(time, altitude);
		}

		// Extrapolation (take the value of the outer bounds)
		int insertionPoint = -index - 1;
		if (insertionPoint == 0) {
			return levels.get(0).model.getWindVelocity(time, altitude);
		}
		if (insertionPoint == levels.size()) {
			return levels.get(levels.size() - 1).model.getWindVelocity(time, altitude);
		}

		// Interpolation (take the value between the closest two bounds)
		LevelWindModel lowerLevel = levels.get(insertionPoint - 1);
		LevelWindModel upperLevel = levels.get(insertionPoint);
		double fraction = (altitude - lowerLevel.altitude) / (upperLevel.altitude - lowerLevel.altitude);

		Coordinate lowerVelocity = lowerLevel.model.getWindVelocity(time, altitude);
		Coordinate upperVelocity = upperLevel.model.getWindVelocity(time, altitude);

		return lowerVelocity.interpolate(upperVelocity, fraction);
	}

	public double getWindDirection(double time, double altitude) {
		Coordinate velocity = getWindVelocity(time, altitude);
		double direction = Math.atan2(velocity.x, velocity.y);

		// Normalize the result to be between 0 and 2*PI
		return (direction + 2 * Math.PI) % (2 * Math.PI);
	}

	/**
	 * Returns how the altitude reference is set.
	 * @return The altitude reference method used for altitude-wind relations
	 */
	public AltitudeReference getAltitudeReference() {
		return altitudeReference;
	}

	/**
	 * Set the altitude reference used for altitude-wind relations.
	 * @param altitudeReference the new altitude reference
	 */
	public void setAltitudeReference(AltitudeReference altitudeReference) {
		this.altitudeReference = altitudeReference;
		fireChangeEvent();
	}

	@Override
	public ModID getModID() {
		return ModID.ZERO; // You might want to create a specific ModID for this model
	}

	public void loadFrom(MultiLevelPinkNoiseWindModel source) {
		this.levels.clear();
		for (LevelWindModel level : source.levels) {
			this.levels.add(level.clone());
		}
		this.altitudeReference = source.altitudeReference;
	}

	/**
	 * Import wind levels from a CSV file with the specified settings.
	 *
	 * @param file The CSV file to import
	 * @param fieldSeparator The field separator used in the CSV file
	 * @param altitudeColumn The name or index of the altitude column
	 * @param speedColumn The name or index of the speed column
	 * @param directionColumn The name or index of the direction column
	 * @param stdDeviationColumn The name or index of the standard deviation column (can be empty)
	 * @param altitudeUnit The unit used for altitude values in the CSV
	 * @param speedUnit The unit used for speed values in the CSV
	 * @param directionUnit The unit used for direction values in the CSV
	 * @param stdDeviationUnit The unit used for standard deviation values in the CSV
	 * @param hasHeaders Whether the CSV file has headers
	 * @throws IllegalArgumentException If the file could not be loaded or the format is incorrect
	 */
	public void importLevelsFromCSV(File file, String fieldSeparator,
									String altitudeColumn, String speedColumn,
									String directionColumn, String stdDeviationColumn,
									Unit altitudeUnit, Unit speedUnit,
									Unit directionUnit, Unit stdDeviationUnit,
									boolean hasHeaders) throws IllegalArgumentException {
		String line;

		// Clear the current levels
		clearLevels();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			// Map column indices
			int altIndex = -1, speedIndex = -1, dirIndex = -1, stddevIndex = -1;

			if (hasHeaders) {
				// Read the first line as a header
				line = reader.readLine();
				if (line == null) {
					throw new IllegalArgumentException(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.EmptyFile"));
				}

				String[] headers = line.split(fieldSeparator, -1);  // -1 to keep empty trailing fields

				// Find column indices by name
				List<String> headersList = Arrays.asList(headers);
				altIndex = findColumnIndex(headersList, altitudeColumn, "altitude", true);
				speedIndex = findColumnIndex(headersList, speedColumn, "speed", true);
				dirIndex = findColumnIndex(headersList, directionColumn, "direction", true);

				// Standard deviation is optional
				if (!stdDeviationColumn.isEmpty()) {
					stddevIndex = findColumnIndex(headersList, stdDeviationColumn, "standard deviation", false);
				}
			} else {
				// No headers, parse column indices directly
				try {
					altIndex = Integer.parseInt(altitudeColumn);
					speedIndex = Integer.parseInt(speedColumn);
					dirIndex = Integer.parseInt(directionColumn);
					stddevIndex = stdDeviationColumn.isEmpty() ? -1 : Integer.parseInt(stdDeviationColumn);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.InvalidColumnIndex"));
				}
			}

			// Read data rows
			int lineNumber = hasHeaders ? 1 : 0;
			while ((line = reader.readLine()) != null) {
				lineNumber++;

				// Skip empty lines
				if (line.trim().isEmpty()) {
					continue;
				}

				String[] values = line.split(fieldSeparator, -1);  // -1 to keep empty trailing fields

				// Check if we have enough columns
				int maxColumnIndex = Math.max(Math.max(altIndex, speedIndex),
						Math.max(dirIndex, Math.max(stddevIndex, 0)));
				if (maxColumnIndex >= values.length) {
					throw new IllegalArgumentException(String.format(
							trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.NotEnoughColumnsInLine"),
							lineNumber));
				}

				// Extract and convert values
				double altitude = extractDoubleAndConvert(values, altIndex, "altitude", altitudeUnit);
				double speed = extractDoubleAndConvert(values, speedIndex, "speed", speedUnit);
				double direction = extractDoubleAndConvert(values, dirIndex, "direction", directionUnit);

				// Standard deviation is optional
				Double stddev = null;
				if (stddevIndex >= 0 && stddevIndex < values.length && !values[stddevIndex].trim().isEmpty()) {
					stddev = extractDoubleAndConvert(values, stddevIndex, "standard deviation", stdDeviationUnit);
				}

				// Add the wind level
				addWindLevel(altitude, speed, direction, stddev);
			}

			// Sort levels by altitude
			sortLevels();

			// Check if we have at least one level
			if (getLevels().isEmpty()) {
				throw new IllegalArgumentException(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.NoValidData"));
			}

		} catch (IOException e) {
			throw new IllegalArgumentException(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.CouldNotLoadFile") + " '"
					+ file.getName() + "'");
		}
	}

	public void importLevelsFromCSV(File file, String fieldSeparator) {
		importLevelsFromCSV(file, fieldSeparator, "altitude", "speed", "direction", "stddev",
				UnitGroup.UNITS_DISTANCE.getSIUnit(),
				UnitGroup.UNITS_WINDSPEED.getSIUnit(),
				new DegreeUnit(),	// This is more common in wind data
				UnitGroup.UNITS_WINDSPEED.getSIUnit(), true);
	}

	/**
	 * Extract a double value from a string array.
	 * Checks for both period and comma as decimal separator. Commas used as thousands separator are not supported.
	 * @param values The array of values
	 * @param index The index of the value to extract
	 * @param column The name of the column
	 * @return The extracted double value
	 */
	private double extractDouble(String[] values, int index, String column) {
		if (index >= values.length) {
			throw new IllegalArgumentException(String.format(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.MissingColumnValue"),
					column));
		}
		if (values[index] == null || values[index].trim().isEmpty()) {
			throw new IllegalArgumentException(String.format(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.EmptyOrNullValue"),
					column));
		}

		String value = values[index].trim();

		try {
			// Try parsing with period as decimal separator
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			try {
				// If that fails, try replacing last comma with period (for European format)
				int lastCommaIndex = value.lastIndexOf(",");
				if (lastCommaIndex != -1) {
					value = value.substring(0, lastCommaIndex) + "." +
							value.substring(lastCommaIndex + 1);
					return Double.parseDouble(value);
				}
				throw e; // Re-throw if no comma found
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.WrongFormat")
						+ "\nValue: '" + values[index] + "'");
			}
		}
	}

	/**
	 * Find a column index in a list of headers.
	 *
	 * @param headers The list of headers
	 * @param columnName The column name or index to find
	 * @param fieldName Display name of the field for error messages
	 * @param required Whether the column is required
	 * @return The index of the column
	 * @throws IllegalArgumentException If the column is required but not found
	 */
	private int findColumnIndex(List<String> headers, String columnName, String fieldName, boolean required) {
		int index = headers.indexOf(columnName);

		if (index == -1 && required) {
			throw new IllegalArgumentException(String.format(
					trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.ColumnNotFound"),
					fieldName, columnName));
		}

		return index;
	}

	/**
	 * Extract a double value from a string array and convert it to the appropriate unit.
	 *
	 * @param values The array of values
	 * @param index The index of the value to extract
	 * @param fieldName Display name of the field for error messages
	 * @param unit The unit of the value as specified in the CSV
	 * @return The extracted double value in SI units
	 */
	private double extractDoubleAndConvert(String[] values, int index, String fieldName, Unit unit) {
		// Extract the raw value
		double rawValue = extractDouble(values, index, fieldName);

		// Convert from the input unit to SI units (the internal unit system)
		if (unit != null) {
			return unit.fromUnit(rawValue);
		}

		return rawValue;
	}

	/**
	 * Get the index of a header in a list of headers
	 * @param headers The list of headers
	 * @param header The header to find
	 * @param required If the header is required
	 * @return The index of the header
	 */
	private int getHeaderIndex(List<String> headers, String header, boolean required) {
		int idx = headers.indexOf(header);
		if (idx == -1 && required) {
			throw new IllegalArgumentException(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.NoHeader") + " '"
					+ header + "'");
		}
		return idx;
	}

	private int getHeaderIndex(List<String> headers, String header) {
		return getHeaderIndex(headers, header, true);
	}

	@Override
	public MultiLevelPinkNoiseWindModel clone() {
		try {
			MultiLevelPinkNoiseWindModel clone = (MultiLevelPinkNoiseWindModel) super.clone();
			clone.levels = new ArrayList<>(this.levels.size());
			clone.loadFrom(this);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(); // This should never happen
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MultiLevelPinkNoiseWindModel that = (MultiLevelPinkNoiseWindModel) o;

		if (altitudeReference != that.altitudeReference) return false;

		// Compare the levels list
		if (levels.size() != that.levels.size()) return false;
		for (int i = 0; i < levels.size(); i++) {
			if (!levels.get(i).equals(that.levels.get(i))) return false;
		}

		// If we implement any additional fields in the future, we should compare them here

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(levels);
	}

	public static class LevelWindModel implements Cloneable, ChangeSource {
		protected double altitude;
		protected PinkNoiseWindModel model;

		private final List<StateChangeListener> listeners = new ArrayList<>();

		LevelWindModel(double altitude, PinkNoiseWindModel model) {
			this.altitude = altitude;
			this.model = model;
		}

		public double getAltitude() {
			return altitude;
		}

		public void setAltitude(double altitude) {
			this.altitude = altitude;
			fireChangeEvent();
		}

		public double getSpeed() {
			return model.getAverage();
		}

		public void setSpeed(double speed) {
			model.setAverage(speed);
		}

		public double getDirection() {
			return model.getDirection();
		}

		public void setDirection(double direction) {
			model.setDirection(direction);
		}

		public double getStandardDeviation() {
			return model.getStandardDeviation();
		}

		public void setStandardDeviation(double standardDeviation) {
			model.setStandardDeviation(standardDeviation);
		}

		public double getTurbulenceIntensity() {
			return model.getTurbulenceIntensity();
		}

		public void setTurbulenceIntensity(double turbulenceIntensity) {
			model.setTurbulenceIntensity(turbulenceIntensity);
		}

		public String getIntensityDescription() {
			return model.getIntensityDescription();
		}

		@Override
		public LevelWindModel clone() {
			try {
				LevelWindModel clone = (LevelWindModel) super.clone();
				clone.model = this.model.clone();
				return clone;
			} catch (CloneNotSupportedException e) {
				throw new AssertionError(); // This should never happen
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			LevelWindModel that = (LevelWindModel) o;
			return Double.compare(that.altitude, altitude) == 0 &&
					model.equals(that.model);
		}

		@Override
		public int hashCode() {
			return Objects.hash(altitude, model);
		}

		@Override
		public void addChangeListener(StateChangeListener listener) {
			listeners.add(listener);
			model.addChangeListener(listener);
		}

		@Override
		public void removeChangeListener(StateChangeListener listener) {
			listeners.remove(listener);
			model.removeChangeListener(listener);
		}

		public void fireChangeEvent() {
			EventObject event = new EventObject(this);
			// Copy the list before iterating to prevent concurrent modification exceptions.
			EventListener[] list = listeners.toArray(new EventListener[0]);
			for (EventListener l : list) {
				if (l instanceof StateChangeListener) {
					((StateChangeListener) l).stateChanged(event);
				}
			}
		}
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}

	public void fireChangeEvent() {
		EventObject event = new EventObject(this);
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(event);
			}
		}
	}
}