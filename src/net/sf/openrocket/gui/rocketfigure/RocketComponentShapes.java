package net.sf.openrocket.gui.rocketfigure;


import java.awt.Shape;

import net.sf.openrocket.gui.scalefigure.RocketFigure;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Transformation;


/**
 * A catch-all, no-operation drawing component.
 */
public class RocketComponentShapes {

	protected static final double S = RocketFigure.EXTRA_SCALE;
	
	public static Shape[] getShapesSide(net.sf.openrocket.rocketcomponent.RocketComponent component,
			Transformation t) {
		// no-op
		Application.getExceptionHandler().handleErrorCondition("ERROR:  RocketComponent.getShapesSide called with "
				+ component);
		return new Shape[0];
	}
	
	public static Shape[] getShapesBack(net.sf.openrocket.rocketcomponent.RocketComponent component,
			Transformation t) {
		// no-op
		Application.getExceptionHandler().handleErrorCondition("ERROR:  RocketComponent.getShapesBack called with "
				+component);
		return new Shape[0];
	}
	
	
}
