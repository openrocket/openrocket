package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;

/**
 * Rocket nose cones of various types.  Implemented as a transition with the
 * fore radius == 0.
 * <p>
 * The normal nose cone can be converted to a tail cone by setting the {@link #isFlipped} parameter.
 * This will flip all the aft side dimensions with the fore side dimensions.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class NoseCone extends Transition implements InsideColorComponent {
	private static final Translator trans = Application.getTranslator();

	private InsideColorComponentHandler insideColorComponentHandler = new InsideColorComponentHandler(this);
	private boolean isFlipped;		// If true, the nose cone is converted to a tail cone
	
	/********* Constructors **********/
	public NoseCone() {
		this(Transition.Shape.OGIVE, 6 * DEFAULT_RADIUS, DEFAULT_RADIUS);
	}
	
	public NoseCone(Transition.Shape type, double length, double radius) {
		super();
		this.isFlipped = false;
		super.setType(type);
		super.setThickness(0.002);
		super.setLength(length);
		super.setClipped(false);
		resetForeRadius();
		
		super.setAftRadiusAutomatic(false);
		super.setAftRadius(radius);

		super.displayOrder_side = 1;		// Order for displaying the component in the 2D side view
		super.displayOrder_back = 0;		// Order for displaying the component in the 2D back view
	}

	/********** Nose cone dimensions  **********/

	/**
	 * Returns the base radius of the nose cone (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #getAftRadius()} because it works for both normal and flipped nose cones.
	 */
	public double getBaseRadius() {
		return isFlipped ? getForeRadius() : getAftRadius();
	}

	/**
	 * Returns the raw base radius of the nose cone (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #getAftRadiusNoAutomatic()} because it works for both normal and flipped nose cones.
	 */
	public double getBaseRadiusNoAutomatic() {
		return isFlipped ? getForeRadiusNoAutomatic() : getAftRadiusNoAutomatic();
	}

	/**
	 * Sets the base radius of the nose cone (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #setAftRadius(double)} because it works for both normal and flipped nose cones.
	 */
	public void setBaseRadius(double radius) {
		if (isFlipped) {
			setForeRadius(radius);
		} else {
			setAftRadius(radius);
		}
	}

	/**
	 * Returns whether the base radius of the nose cone takes it settings from the previous/next component
	 * (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #isAftRadiusAutomatic()} because it works for both normal and flipped nose cones.
	 */
	public boolean isBaseRadiusAutomatic() {
		return isFlipped ? isForeRadiusAutomatic() : isAftRadiusAutomatic();
	}

	/**
	 * Sets whether the base radius of the nose cone takes it settings from the previous/next component
	 * (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #setAftRadiusAutomatic(boolean)} because it works for both normal and flipped nose cones.
	 */
	public void setBaseRadiusAutomatic(boolean auto) {
		if (isFlipped) {
			setForeRadiusAutomatic(auto);
		} else {
			setAftRadiusAutomatic(auto);
		}

	}

	/**
	 * Returns the shoulder length, regardless of how the nose cone is flipped (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #getAftShoulderLength()} because it works for both normal and flipped nose cones.
	 */
	public double getShoulderLength() {
		return isFlipped ? getForeShoulderLength() : getAftShoulderLength();
	}

	/**
	 * Sets the shoulder length (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #setAftShoulderLength(double)} because it works for both normal and flipped nose cones.
	 */
	public void setShoulderLength(double length) {
		if (isFlipped) {
			setForeShoulderLength(length);
		} else {
			setAftShoulderLength(length);
		}
	}

	/**
	 * Returns the shoulder radius (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #getAftShoulderRadius()} because it works for both normal and flipped nose cones.
	 */
	public double getShoulderRadius() {
		return isFlipped ? getForeShoulderRadius() : getAftShoulderRadius();
	}

	/**
	 * Sets the shoulder radius (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #setAftShoulderRadius(double)} because it works for both normal and flipped nose cones.
	 */
	public void setShoulderRadius(double radius) {
		if (isFlipped) {
			setForeShoulderRadius(radius);
		} else {
			setAftShoulderRadius(radius);
		}
	}

	/**
	 * Returns the shoulder thickness (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #getAftShoulderThickness()} because it works for both normal and flipped nose cones.
	 */
	public double getShoulderThickness() {
		return isFlipped ? getForeShoulderThickness() : getAftShoulderThickness();
	}

	/**
	 * Sets the shoulder thickness (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #setAftShoulderRadius(double)} because it works for both normal and flipped nose cones.
	 */
	public void setShoulderThickness(double thickness) {
		if (isFlipped) {
			setForeShoulderThickness(thickness);
		} else {
			setAftShoulderThickness(thickness);
		}
	}

	/**
	 * Returns the shoulder cap (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #isAftShoulderCapped()} because it works for both normal and flipped nose cones.
	 */
	public boolean isShoulderCapped() {
		return isFlipped ? isForeShoulderCapped() : isAftShoulderCapped();
	}

	/**
	 * Sets the shoulder cap (independent of whether the nose cone is flipped or not).
	 * This method should be used over {@link #setAftShoulderCapped(boolean)} because it works for both normal and flipped nose cones.
	 */
	public void setShoulderCapped(boolean capped) {
		if (isFlipped) {
			setForeShoulderCapped(capped);
		} else {
			setAftShoulderCapped(capped);
		}
	}

	/********** Other  **********/

	/**
	 * Return true if the nose cone is flipped, i.e. converted to a tail cone, false if it is a regular nose cone.
	 */
	public boolean isFlipped() {
		return isFlipped;
	}

	/**
	 * Set the nose cone to be flipped, i.e. converted to a tail cone, or set it to be a regular nose cone.
	 * @param flipped if true, the nose cone is converted to a tail cone, if false it is a regular nose cone.
	 * @param sanityCheck whether to check if the auto radius parameter can be used for the new nose cone orientation
	 */
	public void setFlipped(boolean flipped, boolean sanityCheck) {
		for (RocketComponent listener : configListeners) {
			if (listener instanceof NoseCone) {
				((NoseCone) listener).setFlipped(flipped, sanityCheck);
			}
		}

		if (isFlipped == flipped) {
			return;
		}

		boolean previousByPass = isBypassComponentChangeEvent();
		setBypassChangeEvent(true);
		if (flipped) {
			setForeRadius(getAftRadiusNoAutomatic());
			setForeRadiusAutomatic(isAftRadiusAutomatic(), sanityCheck);
			setForeShoulderLength(getAftShoulderLength());
			setForeShoulderRadius(getAftShoulderRadius());
			setForeShoulderThickness(getAftShoulderThickness());
			setForeShoulderCapped(isAftShoulderCapped());

			resetAftRadius();
		} else {
			setAftRadius(getForeRadiusNoAutomatic());
			setAftRadiusAutomatic(isForeRadiusAutomatic(), sanityCheck);
			setAftShoulderLength(getForeShoulderLength());
			setAftShoulderRadius(getForeShoulderRadius());
			setAftShoulderThickness(getForeShoulderThickness());
			setAftShoulderCapped(isForeShoulderCapped());

			resetForeRadius();
		}
		setBypassChangeEvent(previousByPass);

		isFlipped = flipped;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	/**
	 * Set the nose cone to be flipped, i.e. converted to a tail cone, or set it to be a regular nose cone.
	 * @param flipped if true, the nose cone is converted to a tail cone, if false it is a regular nose cone.
	 */
	public void setFlipped(boolean flipped) {
		setFlipped(flipped, true);
	}

	private void resetForeRadius() {
		setForeRadius(0);
		setForeRadiusAutomatic(false);
		setForeShoulderLength(0);
		setForeShoulderRadius(0);
		setForeShoulderThickness(0);
		setForeShoulderCapped(false);
	}

	private void resetAftRadius() {
		setAftRadius(0);
		setAftRadiusAutomatic(false);
		setAftShoulderLength(0);
		setAftShoulderRadius(0);
		setAftShoulderThickness(0);
		setAftShoulderCapped(false);
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
	
	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.NOSE_CONE;
	}

	@Override
	protected void loadFromPreset(ComponentPreset preset) {
		// We first need to unflip, because the preset loading always applies settings for a normal nose cone (e.g. aft diameter)
		boolean flipped = isFlipped;
		setFlipped(false);
		//Many parameters are handled by the super class Transition.loadFromPreset
		super.loadFromPreset(preset);
		setFlipped(flipped);
	}

	/**
	 * Return component name.
	 */
	@Override
	public String getComponentName() {
		//// Nose cone
		return trans.get("NoseCone.NoseCone");
	}


	@Override
	public InsideColorComponentHandler getInsideColorComponentHandler() {
		return this.insideColorComponentHandler;
	}

	@Override
	public void setInsideColorComponentHandler(InsideColorComponentHandler handler) {
		this.insideColorComponentHandler = handler;
	}
}
