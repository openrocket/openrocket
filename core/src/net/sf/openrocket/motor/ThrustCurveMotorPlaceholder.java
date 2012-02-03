package net.sf.openrocket.motor;

import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;

public class ThrustCurveMotorPlaceholder implements Motor {
	
	private final Motor.Type type;
	private final String manufacturer;
	private final String designation;
	private final double diameter;
	private final double length;
	private final String digest;
	private final double delay;
	private final double launchMass;
	private final double emptyMass;
	
	
	public ThrustCurveMotorPlaceholder(Type type, String manufacturer, String designation, double diameter, double length,
			String digest, double delay, double launchMass, double emptyMass) {
		this.type = type;
		this.manufacturer = manufacturer;
		this.designation = designation;
		this.diameter = diameter;
		this.length = length;
		this.digest = digest;
		this.delay = delay;
		this.launchMass = launchMass;
		this.emptyMass = emptyMass;
	}
	
	
	@Override
	public Type getMotorType() {
		return type;
	}
	
	public String getManufacturer() {
		return manufacturer;
	}
	
	@Override
	public String getDesignation() {
		return designation;
	}
	
	@Override
	public String getDesignation(double designationDelay) {
		return designation + "-" + ThrustCurveMotor.getDelayString(designationDelay);
	}
	
	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public double getDiameter() {
		return diameter;
	}
	
	@Override
	public double getLength() {
		return length;
	}
	
	@Override
	public String getDigest() {
		return digest;
	}
	
	public double getDelay() {
		return delay;
	}
	
	@Override
	public MotorInstance getInstance() {
		throw new BugException("Called getInstance on PlaceholderMotor");
	}
	
	@Override
	public Coordinate getLaunchCG() {
		return new Coordinate(length / 2, 0, 0, launchMass);
	}
	
	@Override
	public Coordinate getEmptyCG() {
		return new Coordinate(length / 2, 0, 0, emptyMass);
	}
	
	@Override
	public double getBurnTimeEstimate() {
		return Double.NaN;
	}
	
	@Override
	public double getAverageThrustEstimate() {
		return Double.NaN;
	}
	
	@Override
	public double getMaxThrustEstimate() {
		return Double.NaN;
	}
	
	@Override
	public double getTotalImpulseEstimate() {
		return Double.NaN;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(delay);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((designation == null) ? 0 : designation.hashCode());
		temp = Double.doubleToLongBits(diameter);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((digest == null) ? 0 : digest.hashCode());
		temp = Double.doubleToLongBits(emptyMass);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(launchMass);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(length);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((manufacturer == null) ? 0 : manufacturer.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ThrustCurveMotorPlaceholder other = (ThrustCurveMotorPlaceholder) obj;
		if (Double.doubleToLongBits(delay) != Double
				.doubleToLongBits(other.delay))
			return false;
		if (designation == null) {
			if (other.designation != null)
				return false;
		} else if (!designation.equals(other.designation))
			return false;
		if (Double.doubleToLongBits(diameter) != Double
				.doubleToLongBits(other.diameter))
			return false;
		if (digest == null) {
			if (other.digest != null)
				return false;
		} else if (!digest.equals(other.digest))
			return false;
		if (Double.doubleToLongBits(emptyMass) != Double
				.doubleToLongBits(other.emptyMass))
			return false;
		if (Double.doubleToLongBits(launchMass) != Double
				.doubleToLongBits(other.launchMass))
			return false;
		if (Double.doubleToLongBits(length) != Double
				.doubleToLongBits(other.length))
			return false;
		if (manufacturer == null) {
			if (other.manufacturer != null)
				return false;
		} else if (!manufacturer.equals(other.manufacturer))
			return false;
		if (type != other.type)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "ThrustCurveMotorPlaceholder [manufacturer=" + manufacturer
				+ ", designation=" + designation + "]";
	}
	
}
