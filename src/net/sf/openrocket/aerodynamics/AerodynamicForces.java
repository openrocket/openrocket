package net.sf.openrocket.aerodynamics;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Monitorable;

public class AerodynamicForces implements Cloneable, Monitorable {
	
	/** 
	 * The component this data is referring to.  Used in analysis methods.
	 * A total value is indicated by the component being the Rocket component. 
	 */
	private RocketComponent component = null;
	

	/** CP and CNa. */
	private Coordinate cp = null;
	
	
	/**
	 * Normal force coefficient derivative.  At values close to zero angle of attack
	 * this value may be poorly defined or NaN if the calculation method does not
	 * compute CNa directly.
	 */
	private double CNa = Double.NaN;

	
	/** Normal force coefficient. */
	private double CN = Double.NaN;

	/** Pitching moment coefficient, relative to the coordinate origin. */
	private double Cm = Double.NaN;
	
	/** Side force coefficient, Cy */
	private double Cside = Double.NaN;
	
	/** Yaw moment coefficient, Cn, relative to the coordinate origin. */
	private double Cyaw = Double.NaN;
	
	/** Roll moment coefficient, Cl, relative to the coordinate origin. */
	private double Croll = Double.NaN;
	
	/** Roll moment damping coefficient */
	private double CrollDamp = Double.NaN;
	
	/** Roll moment forcing coefficient */
	private double CrollForce = Double.NaN;
	

	
	/** Axial drag coefficient, CA */
	private double Caxial = Double.NaN;
	
	/** Total drag force coefficient, parallel to the airflow. */
	private double CD = Double.NaN;
	
	/** Drag coefficient due to fore pressure drag. */
	private double pressureCD = Double.NaN;
	
	/** Drag coefficient due to base drag. */
	private double baseCD = Double.NaN;
	
	/** Drag coefficient due to friction drag. */
	private double frictionCD = Double.NaN;
	
	
	private double pitchDampingMoment = Double.NaN;
	private double yawDampingMoment = Double.NaN;
	
	private int modID = 0;
	
	
	public void setComponent(RocketComponent component) {
		this.component = component;
		modID++;
	}

	public RocketComponent getComponent() {
		return component;
	}

	public void setCP(Coordinate cp) {
		this.cp = cp;
		modID++;
	}

	public Coordinate getCP() {
		return cp;
	}

	public void setCNa(double cNa) {
		CNa = cNa;
		modID++;
	}

	public double getCNa() {
		return CNa;
	}

	public void setCN(double cN) {
		CN = cN;
		modID++;
	}

	public double getCN() {
		return CN;
	}

	public void setCm(double cm) {
		Cm = cm;
		modID++;
	}

	public double getCm() {
		return Cm;
	}

	public void setCside(double cside) {
		Cside = cside;
		modID++;
	}

	public double getCside() {
		return Cside;
	}

	public void setCyaw(double cyaw) {
		Cyaw = cyaw;
		modID++;
	}

	public double getCyaw() {
		return Cyaw;
	}

	public void setCroll(double croll) {
		Croll = croll;
		modID++;
	}

	public double getCroll() {
		return Croll;
	}

	public void setCrollDamp(double crollDamp) {
		CrollDamp = crollDamp;
		modID++;
	}

	public double getCrollDamp() {
		return CrollDamp;
	}

	public void setCrollForce(double crollForce) {
		CrollForce = crollForce;
		modID++;
	}

	public double getCrollForce() {
		return CrollForce;
	}

	public void setCaxial(double caxial) {
		Caxial = caxial;
		modID++;
	}

	public double getCaxial() {
		return Caxial;
	}

	public void setCD(double cD) {
		CD = cD;
		modID++;
	}

	public double getCD() {
		return CD;
	}

	public void setPressureCD(double pressureCD) {
		this.pressureCD = pressureCD;
		modID++;
	}

	public double getPressureCD() {
		return pressureCD;
	}

	public void setBaseCD(double baseCD) {
		this.baseCD = baseCD;
		modID++;
	}

	public double getBaseCD() {
		return baseCD;
	}

	public void setFrictionCD(double frictionCD) {
		this.frictionCD = frictionCD;
		modID++;
	}

	public double getFrictionCD() {
		return frictionCD;
	}

	public void setPitchDampingMoment(double pitchDampingMoment) {
		this.pitchDampingMoment = pitchDampingMoment;
		modID++;
	}

	public double getPitchDampingMoment() {
		return pitchDampingMoment;
	}

	public void setYawDampingMoment(double yawDampingMoment) {
		this.yawDampingMoment = yawDampingMoment;
		modID++;
	}

	public double getYawDampingMoment() {
		return yawDampingMoment;
	}

	
	
	/**
	 * Reset all values to null/NaN.
	 */
	public void reset() {
		setComponent(null);

		setCP(null);
		setCNa(Double.NaN);
		setCN(Double.NaN);
		setCm(Double.NaN);
		setCside(Double.NaN);
		setCyaw(Double.NaN);
		setCroll(Double.NaN);
		setCrollDamp(Double.NaN);
		setCrollForce(Double.NaN);
		setCaxial(Double.NaN);
		setCD(Double.NaN);
		setPitchDampingMoment(Double.NaN);
		setYawDampingMoment(Double.NaN);
	}
	
	/**
	 * Zero all values to 0 / Coordinate.NUL.  Component is left as it was.
	 */
	public void zero() {
		// component untouched

		setCP(Coordinate.NUL);
		setCNa(0);
		setCN(0);
		setCm(0);
		setCside(0);
		setCyaw(0);
		setCroll(0);
		setCrollDamp(0);
		setCrollForce(0);
		setCaxial(0);
		setCD(0);
		setPitchDampingMoment(0);
		setYawDampingMoment(0);
	}

	
	@Override
	public AerodynamicForces clone() {
		try {
			return (AerodynamicForces)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new BugException("CloneNotSupportedException?!?");
		}
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof AerodynamicForces))
			return false;
		AerodynamicForces other = (AerodynamicForces) obj;
		
		return (MathUtil.equals(this.getCNa(), other.getCNa()) &&
				MathUtil.equals(this.getCN(), other.getCN()) &&
				MathUtil.equals(this.getCm(), other.getCm()) &&
				MathUtil.equals(this.getCside(), other.getCside()) &&
				MathUtil.equals(this.getCyaw(), other.getCyaw()) &&
				MathUtil.equals(this.getCroll(), other.getCroll()) &&
				MathUtil.equals(this.getCrollDamp(), other.getCrollDamp()) &&
				MathUtil.equals(this.getCrollForce(), other.getCrollForce()) &&
				MathUtil.equals(this.getCaxial(), other.getCaxial()) &&
				MathUtil.equals(this.getCD(), other.getCD()) &&
				MathUtil.equals(this.getPressureCD(), other.getPressureCD()) &&
				MathUtil.equals(this.getBaseCD(), other.getBaseCD()) &&
				MathUtil.equals(this.getFrictionCD(), other.getFrictionCD()) &&
				MathUtil.equals(this.getPitchDampingMoment(), other.getPitchDampingMoment()) &&
				MathUtil.equals(this.getYawDampingMoment(), other.getYawDampingMoment()) &&
				this.getCP().equals(other.getCP()));
	}
	
	@Override
	public int hashCode() {
		return (int) (1000*(this.getCD()+this.getCaxial()+this.getCNa())) + this.getCP().hashCode();
	}
	
	
	@Override
	public String toString() {
		String text="AerodynamicForces[";
		
		if (getComponent() != null)
			text += "component:" + getComponent() + ",";
		if (getCP() != null)
			text += "cp:" + getCP() + ",";
		
		if (!Double.isNaN(getCNa()))
			text += "CNa:" + getCNa() + ",";
		if (!Double.isNaN(getCN()))
			text += "CN:" + getCN() + ",";
		if (!Double.isNaN(getCm()))
			text += "Cm:" + getCm() + ",";
		
		if (!Double.isNaN(getCside()))
			text += "Cside:" + getCside() + ",";
		if (!Double.isNaN(getCyaw()))
			text += "Cyaw:" + getCyaw() + ",";
		
		if (!Double.isNaN(getCroll()))
			text += "Croll:" + getCroll() + ",";
		if (!Double.isNaN(getCaxial()))
			text += "Caxial:" + getCaxial() + ",";
		
		if (!Double.isNaN(getCD()))
			text += "CD:" + getCD() + ",";

		if (text.charAt(text.length()-1) == ',')
			text = text.substring(0, text.length()-1);
		
		text += "]";
		return text;
	}

	@Override
	public int getModID() {
		return modID;
	}
}
