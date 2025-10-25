package info.openrocket.core.simulation;

import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.Quaternion;

public final class AccelerationData {

	private CoordinateIF linearAccelerationRC;
	private CoordinateIF rotationalAccelerationRC;
	private CoordinateIF linearAccelerationWC;
	private CoordinateIF rotationalAccelerationWC;
	// Rotates from rocket coordinates to world coordinates
	private final Quaternion rotation;

	public AccelerationData(CoordinateIF linearAccelerationRC, CoordinateIF rotationalAccelerationRC,
							CoordinateIF linearAccelerationWC, CoordinateIF rotationalAccelerationWC,
							Quaternion rotation) {

		if ((linearAccelerationRC == null && linearAccelerationWC == null) ||
				(rotationalAccelerationRC == null && rotationalAccelerationWC == null) ||
				rotation == null) {
			throw new IllegalArgumentException("Parameter is null: " +
					" linearAccelerationRC=" + linearAccelerationRC +
					" linearAccelerationWC=" + linearAccelerationWC +
					" rotationalAccelerationRC=" + rotationalAccelerationRC +
					" rotationalAccelerationWC=" + rotationalAccelerationWC +
					" rotation=" + rotation);
		}
		this.linearAccelerationRC = linearAccelerationRC;
		this.rotationalAccelerationRC = rotationalAccelerationRC;
		this.linearAccelerationWC = linearAccelerationWC;
		this.rotationalAccelerationWC = rotationalAccelerationWC;
		this.rotation = rotation;
	}

	public CoordinateIF getLinearAccelerationRC() {
		if (linearAccelerationRC == null) {
			linearAccelerationRC = rotation.invRotate(linearAccelerationWC);
		}
		return linearAccelerationRC;
	}

	public CoordinateIF getRotationalAccelerationRC() {
		if (rotationalAccelerationRC == null) {
			rotationalAccelerationRC = rotation.invRotate(rotationalAccelerationWC);
		}
		return rotationalAccelerationRC;
	}

	public CoordinateIF getLinearAccelerationWC() {
		if (linearAccelerationWC == null) {
			linearAccelerationWC = rotation.rotate(linearAccelerationRC);
		}
		return linearAccelerationWC;
	}

	public CoordinateIF getRotationalAccelerationWC() {
		if (rotationalAccelerationWC == null) {
			rotationalAccelerationWC = rotation.rotate(rotationalAccelerationRC);
		}
		return rotationalAccelerationWC;
	}

	public Quaternion getRotation() {
		return rotation;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AccelerationData))
			return false;
		AccelerationData other = (AccelerationData) obj;
		return (this.getLinearAccelerationRC().equals(other.getLinearAccelerationRC()) &&
				this.getRotationalAccelerationRC().equals(other.getRotationalAccelerationRC()));
	}

	@Override
	public int hashCode() {
		return getLinearAccelerationRC().hashCode() ^ getRotationalAccelerationRC().hashCode();
	}

}
