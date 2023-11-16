package net.sf.openrocket.gui.components;

import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.UITheme;

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * A simple slider that does not show the current value.  GTK l&f shows the value, and cannot 
 * be configured otherwise(!).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

@SuppressWarnings("serial")
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
		setFocusable(false);
		if (UITheme.isLightTheme(GUIUtil.getUITheme())) {
			setUI(new BasicSliderUI(this));
		} else {
			setUI(new DarkBasicSliderUI(this));
		}
	}

}
