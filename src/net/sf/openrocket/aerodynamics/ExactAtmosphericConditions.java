package net.sf.openrocket.aerodynamics;

/**
 * A class containing more accurate methods for computing the atmospheric properties.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ExactAtmosphericConditions extends AtmosphericConditions {

	@Override
	public double getDensity() {
		// TODO Auto-generated method stub
		return super.getDensity();
	}

	@Override
	public double getKinematicViscosity() {
		// TODO Auto-generated method stub
		return super.getKinematicViscosity();
	}

	@Override
	public double getMachSpeed() {
		return 331.3 * Math.sqrt(1 + (temperature - 273.15)/273.15);
	}

}
