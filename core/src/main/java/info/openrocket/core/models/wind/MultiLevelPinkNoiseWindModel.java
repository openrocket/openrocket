package info.openrocket.core.models.wind;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;

public class MultiLevelPinkNoiseWindModel implements WindModel {
	private List<LevelWindModel> levels;

	private final List<StateChangeListener> listeners = new ArrayList<>();

	public MultiLevelPinkNoiseWindModel() {
		this.levels = new ArrayList<>();
	}

	public void addWindLevel(double altitude, double speed, double direction, double standardDeviation) {
		PinkNoiseWindModel pinkNoiseModel = new PinkNoiseWindModel();
		pinkNoiseModel.setAverage(speed);
		pinkNoiseModel.setStandardDeviation(standardDeviation);
		pinkNoiseModel.setDirection(direction);

		LevelWindModel newLevel = new LevelWindModel(altitude, pinkNoiseModel);
		newLevel.addChangeListener(e -> fireChangeEvent());
		int index = Collections.binarySearch(levels, newLevel, Comparator.comparingDouble(l -> l.altitude));
		if (index >= 0) {
			throw new IllegalArgumentException("Wind level already exists for altitude: " + altitude);
		}
		levels.add(-index - 1, newLevel);
		fireChangeEvent();
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

		public double getTurblenceIntensity() {
			return model.getTurbulenceIntensity();
		}

		public void setTurbulenceIntensity(double turbulenceIntensity) {
			model.setTurbulenceIntensity(turbulenceIntensity);
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