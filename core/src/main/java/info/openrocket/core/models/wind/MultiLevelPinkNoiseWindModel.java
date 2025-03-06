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
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;

public class MultiLevelPinkNoiseWindModel implements WindModel {
	private List<LevelWindModel> levels;
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private final List<StateChangeListener> listeners = new ArrayList<>();

	private static final int REQUIRED_NR_OF_CSV_COLUMNS = 3;		// alt, speed, dir
	private static final String COLUMN_ALTITUDE = "alt";
	private static final String COLUMN_SPEED = "speed";
	private static final String COLUMN_DIRECTION = "dir";
	private static final String COLUMN_STDDEV = "stddev";

	public MultiLevelPinkNoiseWindModel() {
		this.levels = new ArrayList<>();

		// Add a default wind level
		addWindLevel(0, prefs.getAverageWindModel().getAverage(), prefs.getAverageWindModel().getDirection(),
				prefs.getAverageWindModel().getStandardDeviation());
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

	public void removeWindLevel(double altitude) {
		levels.removeIf(level -> level.altitude == altitude);
		fireChangeEvent();
	}

	public void removeWindLevelIdx(int index) {
		levels.remove(index);
		fireChangeEvent();
	}

	public void clearLevels() {
		levels.clear();
		fireChangeEvent();
	}

	public List<LevelWindModel> getLevels() {
		return new ArrayList<>(levels);
	}

	public void sortLevels() {
		levels.sort(Comparator.comparingDouble(l -> l.altitude));
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

	@Override
	public ModID getModID() {
		return ModID.ZERO; // You might want to create a specific ModID for this model
	}

	public void loadFrom(MultiLevelPinkNoiseWindModel source) {
		this.levels.clear();
		for (LevelWindModel level : source.levels) {
			this.levels.add(level.clone());
		}
	}

	/**
	 * Import the wind levels from a CSV file
	 * @param file The file to import
	 * @param fieldSeparator The field separator used in the CSV file
	 * @throws IllegalArgumentException If the file could not be loaded or the format is incorrect
	 */
	public void importLevelsFromCSV(File file, String fieldSeparator) throws IllegalArgumentException {
		String line;

		// Clear the current levels
		clearLevels();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			// Read the first line as a header
			line = reader.readLine();
			String[] headers = line.split(fieldSeparator);
			sanityCheckColumnSize(fieldSeparator, headers, line);

			List<String> headersList = Arrays.asList(headers);
			int altIndex = getHeaderIndex(headersList, COLUMN_ALTITUDE);
			int speedIndex = getHeaderIndex(headersList, COLUMN_SPEED);
			int dirIndex = getHeaderIndex(headersList, COLUMN_DIRECTION);
			int stddevIndex = getHeaderIndex(headersList, COLUMN_STDDEV, false);

			while ((line = reader.readLine()) != null) {
				// Ignore empty lines
				if (line.isEmpty()) {
					continue;
				}
				String[] values = line.split(fieldSeparator);
				sanityCheckColumnSize(fieldSeparator, values, line);
				double altitude = extractDouble(values, altIndex, COLUMN_ALTITUDE);
				double speed = extractDouble(values, speedIndex, COLUMN_SPEED);
				double direction = MathUtil.deg2rad(extractDouble(values, dirIndex, COLUMN_DIRECTION));
				Double stddev;
				if (stddevIndex != -1) {
					stddev = extractDouble(values, stddevIndex, COLUMN_STDDEV);
				} else {
					stddev = null;
				}

				// Add the wind level
				if (stddev == null) {
					addWindLevel(altitude, speed, direction);
				} else {
					addWindLevel(altitude, speed, direction, stddev);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.CouldNotLoadFile") + " '"
					+ file.getName() + "'");
		}
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
						+ " Value: '" + values[index] + "'");
			}
		}
	}

	/**
	 * Check if the number of columns in a line is correct
	 * @param fieldSeparator The field separator used in the CSV file
	 * @param columns The columns in the line
	 * @param line The line that was read
	 */
	private void sanityCheckColumnSize(String fieldSeparator, String[] columns, String line) {
		int nrOfColumns = columns.length;
		if (nrOfColumns < REQUIRED_NR_OF_CSV_COLUMNS) {
			String[] msg = {
					String.format(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.NotEnoughColumns1"),
							nrOfColumns, line),
					String.format(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.NotEnoughColumns2"),
							REQUIRED_NR_OF_CSV_COLUMNS, "alt, speed & dir"),
					String.format(trans.get("MultiLevelPinkNoiseWindModel.msg.importLevelsError.NotEnoughColumns3"),
							fieldSeparator),
			};
			throw new IllegalArgumentException(String.join("\n", msg));
		}
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