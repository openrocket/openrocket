package info.openrocket.core.models.wind;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;

public class MultiLevelWindModel implements WindModel {

	private List<WindLevel> levels;

	public MultiLevelWindModel() {
		this.levels = new ArrayList<>();
	}

	public void addWindLevel(double altitude, double speed, double direction) {
		WindLevel newLevel = new WindLevel(altitude, speed, direction);
		int index = Collections.binarySearch(levels, newLevel, Comparator.comparingDouble(l -> l.altitude));
		if (index >= 0) {
			throw new IllegalArgumentException("Wind level already exists for altitude: " + altitude);
		}
		levels.add(-index - 1, newLevel);
	}

	public void removeWindLevel(double altitude) {
		levels.removeIf(level -> level.altitude == altitude);
	}

	public void removeWindLevelIdx(int index) {
		levels.remove(index);
	}

	public List<WindLevel> getLevels() {
		return new ArrayList<>(levels);
	}

	public void resortLevels() {
		levels.sort(Comparator.comparingDouble(l -> l.altitude));
	}

	@Override
	public Coordinate getWindVelocity(double time, double altitude) {
		if (levels.isEmpty()) {
			return Coordinate.ZERO;
		}

		int index = Collections.binarySearch(levels, new WindLevel(altitude, 0, 0),
				Comparator.comparingDouble(l -> l.altitude));

		// Retrieve the wind level if it exists
		if (index >= 0) {
			return levels.get(index).toCoordinate();
		}

		// Extrapolation (take the value of the outer bounds)
		int insertionPoint = -index - 1;
		if (insertionPoint == 0) {
			return levels.get(0).toCoordinate();
		}
		if (insertionPoint == levels.size()) {
			return levels.get(levels.size() - 1).toCoordinate();
		}

		// Interpolation (take the value between the closest two bounds)
		WindLevel lower = levels.get(insertionPoint - 1);
		WindLevel upper = levels.get(insertionPoint);

		double fraction = (altitude - lower.altitude) / (upper.altitude - lower.altitude);
		double speed = MathUtil.interpolate(lower.speed, upper.speed, fraction);
		double direction = MathUtil.interpolate(lower.direction, upper.direction, fraction);

		return new Coordinate(speed * Math.sin(direction), speed * Math.cos(direction), 0);
	}

	public double getWindDirection(double altitude) {
		if (levels.isEmpty()) {
			return 0;
		}

		int index = Collections.binarySearch(levels, new WindLevel(altitude, 0, 0),
				Comparator.comparingDouble(l -> l.altitude));

		if (index >= 0) {
			return levels.get(index).direction;
		}

		int insertionPoint = -index - 1;
		if (insertionPoint == 0) {
			return levels.get(0).direction;
		}
		if (insertionPoint == levels.size()) {
			return levels.get(levels.size() - 1).direction;
		}

		WindLevel lower = levels.get(insertionPoint - 1);
		WindLevel upper = levels.get(insertionPoint);

		double fraction = (altitude - lower.altitude) / (upper.altitude - lower.altitude);
		return MathUtil.interpolate(lower.direction, upper.direction, fraction);
	}

	@Override
	public ModID getModID() {
		return ModID.ZERO; // You might want to create a specific ModID for this model
	}

	public void loadFrom(MultiLevelWindModel source) {
		this.levels.clear();
		for (WindLevel level : source.levels) {
			this.levels.add(level.clone());
		}
	}

	@Override
	public MultiLevelWindModel clone() {
		try {
			MultiLevelWindModel clone = (MultiLevelWindModel) super.clone();
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
		MultiLevelWindModel that = (MultiLevelWindModel) o;
		return levels.equals(that.levels);
	}

	@Override
	public int hashCode() {
		return levels.hashCode();
	}

	public static class WindLevel implements Cloneable {
		public double altitude;
		public double speed;
		public double direction;

		public WindLevel(double altitude, double speed, double direction) {
			this.altitude = altitude;
			this.speed = Math.max(speed, 0);
			this.direction = direction;
		}

		Coordinate toCoordinate() {
			return new Coordinate(speed * Math.sin(direction), speed * Math.cos(direction), 0);
		}

		public void loadFrom(WindLevel source) {
			this.altitude = source.altitude;
			this.speed = source.speed;
			this.direction = source.direction;
		}

		@Override
		public WindLevel clone() {
			try {
				WindLevel clone = (WindLevel) super.clone();
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
			WindLevel windLevel = (WindLevel) o;
			return Double.compare(windLevel.altitude, altitude) == 0 &&
					Double.compare(windLevel.speed, speed) == 0 &&
					Double.compare(windLevel.direction, direction) == 0;
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + Double.hashCode(altitude);
			result = 31 * result + Double.hashCode(speed);
			result = 31 * result + Double.hashCode(direction);
			return result;
		}
	}
}