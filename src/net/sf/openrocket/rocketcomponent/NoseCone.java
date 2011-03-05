package net.sf.openrocket.rocketcomponent;

/**
 * Rocket nose cones of various types.  Implemented as a transition with the
 * fore radius == 0.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class NoseCone extends Transition {


	/********* Constructors **********/
	public NoseCone() {
		this(Transition.Shape.OGIVE, 6*DEFAULT_RADIUS, DEFAULT_RADIUS);
	}

	public NoseCone(Transition.Shape type, double length, double radius) {
		super();
		super.setType(type);
		super.setForeRadiusAutomatic(false);
		super.setForeRadius(0);
		super.setForeShoulderLength(0);
		super.setForeShoulderRadius(0.9*radius);
		super.setForeShoulderThickness(0);
		super.setForeShoulderCapped(filled);
		super.setThickness(0.002);
		super.setLength(length);
		super.setClipped(false);

	}


	/********** Get/set methods for component parameters **********/

	@Override
	public double getForeRadius() {
		return 0;
	}

	@Override
	public void setForeRadius(double r) {
		// No-op
	}

	@Override
	public boolean isForeRadiusAutomatic() {
		return false;
	}

	@Override
	public void setForeRadiusAutomatic(boolean b) {
		// No-op
	}

	@Override
	public double getForeShoulderLength() {
		return 0;
	}

	@Override
	public double getForeShoulderRadius() {
		return 0;
	}

	@Override
	public double getForeShoulderThickness() {
		return 0;
	}

	@Override
	public boolean isForeShoulderCapped() {
		return false;
	}

	@Override
	public void setForeShoulderCapped(boolean capped) {
		// No-op
	}

	@Override
	public void setForeShoulderLength(double foreShoulderLength) {
		// No-op
	}

	@Override
	public void setForeShoulderRadius(double foreShoulderRadius) {
		// No-op
	}

	@Override
	public void setForeShoulderThickness(double foreShoulderThickness) {
		// No-op
	}

	@Override
	public boolean isClipped() {
		return false;
	}

	@Override
	public void setClipped(boolean b) {
		// No-op
	}



	/********** RocketComponent methods **********/

	/**
	 * Return component name.
	 */
	@Override
	public String getComponentName() {
		return "Nose cone";
	}

}
