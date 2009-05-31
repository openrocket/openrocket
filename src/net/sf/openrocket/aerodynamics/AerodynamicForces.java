package net.sf.openrocket.aerodynamics;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;

public class AerodynamicForces implements Cloneable {
	
	/** 
	 * The component this data is referring to.  Used in analysis methods.
	 * A total value is indicated by the component being the Rocket component. 
	 */
	public RocketComponent component = null;
	

	/** CG and mass */
	public Coordinate cg = null;

	/** Longitudal moment of inertia with reference to the CG. */
	public double longitudalInertia = Double.NaN;
	
	/** Rotational moment of inertia with reference to the CG. */
	public double rotationalInertia = Double.NaN;
	
	

	/** CP and CNa. */
	public Coordinate cp = null;
	
	
	/**
	 * Normal force coefficient derivative.  At values close to zero angle of attack
	 * this value may be poorly defined or NaN if the calculation method does not
	 * compute CNa directly.
	 */
	public double CNa = Double.NaN;

	
	/** Normal force coefficient. */
	public double CN = Double.NaN;

	/** Pitching moment coefficient, relative to the coordinate origin. */
	public double Cm = Double.NaN;
	
	/** Side force coefficient, Cy */
	public double Cside = Double.NaN;
	
	/** Yaw moment coefficient, Cn, relative to the coordinate origin. */
	public double Cyaw = Double.NaN;
	
	/** Roll moment coefficient, Cl, relative to the coordinate origin. */
	public double Croll = Double.NaN;
	
	/** Roll moment damping coefficient */
	public double CrollDamp = Double.NaN;
	
	/** Roll moment forcing coefficient */
	public double CrollForce = Double.NaN;
	

	
	/** Axial drag coefficient, CA */
	public double Caxial = Double.NaN;
	
	/** Total drag force coefficient, parallel to the airflow. */
	public double CD = Double.NaN;
	
	/** Drag coefficient due to fore pressure drag. */
	public double pressureCD = Double.NaN;
	
	/** Drag coefficient due to base drag. */
	public double baseCD = Double.NaN;
	
	/** Drag coefficient due to friction drag. */
	public double frictionCD = Double.NaN;
	
	
	public double pitchDampingMoment = Double.NaN;
	public double yawDampingMoment = Double.NaN;
	
	
	/**
	 * Reset all values to null/NaN.
	 */
	public void reset() {
		component = null;
		cg = null;
		longitudalInertia = Double.NaN;
		rotationalInertia = Double.NaN;

		cp = null;
		CNa = Double.NaN;
		CN = Double.NaN;
		Cm = Double.NaN;
		Cside = Double.NaN;
		Cyaw = Double.NaN;
		Croll = Double.NaN;
		CrollDamp = Double.NaN;
		CrollForce = Double.NaN;
		Caxial = Double.NaN;
		CD = Double.NaN;
		pitchDampingMoment = Double.NaN;
		yawDampingMoment = Double.NaN;
	}
	
	/**
	 * Zero all values to 0 / Coordinate.NUL.  Component is left as it was.
	 */
	public void zero() {
		// component untouched
		cg = Coordinate.NUL;
		longitudalInertia = 0;
		rotationalInertia = 0;

		cp = Coordinate.NUL;
		CNa = 0;
		CN = 0;
		Cm = 0;
		Cside = 0;
		Cyaw = 0;
		Croll = 0;
		CrollDamp = 0;
		CrollForce = 0;
		Caxial = 0;
		CD = 0;
		pitchDampingMoment = 0;
		yawDampingMoment = 0;
	}

	
	@Override
	public AerodynamicForces clone() {
		try {
			return (AerodynamicForces)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("CloneNotSupportedException?!?");
		}
	}
	
	@Override
	public String toString() {
		String text="AerodynamicForces[";
		
		if (component != null)
			text += "component:" + component + ",";
		if (cg != null)
			text += "cg:" + cg + ",";
		if (cp != null)
			text += "cp:" + cp + ",";
		if (!Double.isNaN(longitudalInertia))
			text += "longIn:" + longitudalInertia + ",";
		if (!Double.isNaN(rotationalInertia))
			text += "rotIn:" + rotationalInertia + ",";
		
		if (!Double.isNaN(CNa))
			text += "CNa:" + CNa + ",";
		if (!Double.isNaN(CN))
			text += "CN:" + CN + ",";
		if (!Double.isNaN(Cm))
			text += "Cm:" + Cm + ",";
		
		if (!Double.isNaN(Cside))
			text += "Cside:" + Cside + ",";
		if (!Double.isNaN(Cyaw))
			text += "Cyaw:" + Cyaw + ",";
		
		if (!Double.isNaN(Croll))
			text += "Croll:" + Croll + ",";
		if (!Double.isNaN(Caxial))
			text += "Caxial:" + Caxial + ",";
		
		if (!Double.isNaN(CD))
			text += "CD:" + CD + ",";

		if (text.charAt(text.length()-1) == ',')
			text = text.substring(0, text.length()-1);
		
		text += "]";
		return text;
	}
}
