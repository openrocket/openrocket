package net.sf.openrocket.simulation;

import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/**
 * An immutable value object containing the mass data of a rocket.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class MassData {

	private final Coordinate cg;
	private final double longitudinalInertia;
	private final double rotationalInertia;
	private final double propellantMass;
	
	
	public MassData(Coordinate cg, double longitudinalInertia, double rotationalInertia, double propellantMass) {
		if (cg == null) {
			throw new IllegalArgumentException("cg is null");
		}
		this.cg = cg;
		this.longitudinalInertia = longitudinalInertia;
		this.rotationalInertia = rotationalInertia;
		this.propellantMass = propellantMass;
	}

	
	
	
	public Coordinate getCG() {
		return cg;
	}
	
	public double getLongitudinalInertia() {
		return longitudinalInertia;
	}
	
	public double getRotationalInertia() {
		return rotationalInertia;
	}

	public double getPropellantMass() {
		return propellantMass;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MassData))
			return false;
		
		MassData other = (MassData) obj;
		return (this.cg.equals(other.cg) && MathUtil.equals(this.longitudinalInertia, other.longitudinalInertia) &&
				MathUtil.equals(this.rotationalInertia, other.rotationalInertia)) && MathUtil.equals(this.propellantMass, other.propellantMass) ;
	}

	
	@Override
	public int hashCode() {
		return (int) (cg.hashCode() ^ Double.doubleToLongBits(longitudinalInertia) ^ Double.doubleToLongBits(rotationalInertia) ^ Double.doubleToLongBits(propellantMass) );
	}


	@Override
	public String toString() {
		return "MassData [cg=" + cg + ", longitudinalInertia=" + longitudinalInertia
				+ ", rotationalInertia=" + rotationalInertia + ", propellantMass="+propellantMass + "]";
	}
	
}
