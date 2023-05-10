package net.sf.openrocket.gui.figureelements;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * A mark indicating the position of the center of gravity.  It is a blue circle with every
 * second quarter filled with blue.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class CGCaret extends Caret {
	private static final float RADIUS = 7;
	
	private static Area caret = null;
	
	/**
	 * Create a new CGCaret at the specified coordinates.
	 */
	public CGCaret(double x, double y) {
		super(x,y);
	}

	/**
	 * Returns the Area corresponding to the caret.  The Area object is created only once,
	 * after which the object is cloned for new copies.
	 */
	@Override
	protected Area getCaret() {
		if (caret != null) {
			return (Area)caret.clone();
		}

		Ellipse2D.Float e = new Ellipse2D.Float(-RADIUS,-RADIUS,2*RADIUS,2*RADIUS);
		caret = new Area(e);
		
		Area a;
		a = new Area(new Rectangle2D.Float(-RADIUS,-RADIUS,RADIUS,RADIUS));
		caret.subtract(a);
		a = new Area(new Rectangle2D.Float(0,0,RADIUS,RADIUS));
		caret.subtract(a);
		
		a = new Area(new Ellipse2D.Float(-RADIUS,-RADIUS,2*RADIUS,2*RADIUS));
		a.subtract(new Area(new Ellipse2D.Float(-RADIUS*0.9f,-RADIUS*0.9f,
				2*0.9f*RADIUS,2*0.9f*RADIUS)));
		caret.add(a);
		
		return (Area) caret.clone();
	}

	/**
	 * Return the color of the caret (blue).
	 */
	@Override
	protected Color getColor() {
		return Color.BLUE;
	}

}
