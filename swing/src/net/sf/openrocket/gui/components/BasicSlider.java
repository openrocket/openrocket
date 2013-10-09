package net.sf.openrocket.gui.components;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * A simple slider that does not show the current value.  GTK l&f shows the value, and cannot 
 * be configured otherwise(!).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class BasicSlider extends JSlider {

	public BasicSlider(BoundedRangeModel brm) {
		this(brm,JSlider.HORIZONTAL,false);
	}
	
	public BasicSlider(BoundedRangeModel brm, int orientation) {
		this(brm,orientation,false);
	}
	
	public BasicSlider(BoundedRangeModel brm, int orientation, boolean inverted) {
		super(brm);
		setOrientation(orientation);
		setInverted(inverted);
		setUI(new BasicSliderUI(this));
	}

}
