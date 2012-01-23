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
	
	
}
