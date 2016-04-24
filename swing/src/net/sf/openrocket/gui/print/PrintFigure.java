/*
 * PrintFigure.java
 */
package net.sf.openrocket.gui.print;

import net.sf.openrocket.gui.scalefigure.RocketFigure;
import net.sf.openrocket.rocketcomponent.Rocket;

/**
 * A figure used to override the scale factor in RocketFigure.  This allows pinpoint scaling to allow a diagram
 * to fit in the width of the chosen page size.
 */
public class PrintFigure extends RocketFigure {
	private static final long serialVersionUID = -3843219909502782607L;

	/**
	 * Constructor.
	 * 
	 * @param configuration  the configuration
	 */
	public PrintFigure(final Rocket rkt) {
		super(rkt);
	}
	
	@Override
	protected double computeTy(int heightPx) {
		super.computeTy(heightPx);
		return 0;
	}
	
	public void setScale(final double theScale) {
		this.scale = theScale; //dpi/0.0254*scaling;
		updateFigure();
	}

    public double getFigureHeightPx() {
        return this.figureHeightPx;
    }
}
