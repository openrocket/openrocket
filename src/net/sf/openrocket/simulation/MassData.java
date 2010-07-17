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
	private final double longitudalInertia;
	private final double rotationalInertia;
	
	
	public MassData(Coordinate cg, double longitudalInertia, double rotationalInertia) {
		if (cg == null) {
			throw new IllegalArgumentException("cg is null");
		}
		this.cg = cg;
		this.longitudalInertia = longitudalInertia;
		this.rotationalInertia = rotationalInertia;
	}

	
	
	
	public Coordinate getCG() {
		return cg;
	}
	
	public double getLongitudalInertia() {
		return longitudalInertia;
	}
	
	public double getRotationalInertia() {
		return rotationalInertia;
	}

	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MassData))
			return false;
		
		MassData other = (MassData) obj;
		return (this.cg.equals(other.cg) && MathUtil.equals(this.longitudalInertia, other.longitudalInertia) &&
				MathUtil.equals(this.rotationalInertia, other.rotationalInertia));
	}

	
	@Override
	public int hashCode() {
		return (int) (cg.hashCode() ^ Double.doubleToLongBits(longitudalInertia) ^ Double.doubleToLongBits(rotationalInertia));
	}


	@Override
	public String toString() {
		return "MassData [cg=" + cg + ", longitudalInertia=" + longitudalInertia
				+ ", rotationalInertia=" + rotationalInertia + "]";
	}
	
}
