package info.openrocket.swing.gui.figureelements;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/**
 * A mark indicating the position of the center of pressure.  It is a red filled circle
 * inside a slightly larger red circle.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class CPCaret extends Caret {
	private static final float RADIUS = 7;
	
	private static Area caret = null;

	private static Color CPColor;

	static {
		initColors();
	}

	/**
	 * Create a new CPCaret at the specified coordinates.
	 */
	public CPCaret(double x, double y) {
		super(x,y);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(CPCaret::updateColors);
	}

	private static void updateColors() {
		CPColor = GUIUtil.getUITheme().getCPColor();
	}

	/**
	 * Returns the Area object of the caret.  The Area object is created only once,
	 * after which new copies are cloned from it.
	 */
	@Override
	protected Area getCaret() {
		if (caret != null) {
			return (Area)caret.clone();
		}

		Ellipse2D.Float e = new Ellipse2D.Float(-RADIUS,-RADIUS,2*RADIUS,2*RADIUS);
		caret = new Area(e);

		caret.subtract(new Area(new Ellipse2D.Float(-RADIUS*0.9f,-RADIUS*0.9f,
				2*0.9f*RADIUS,2*0.9f*RADIUS)));
		
		caret.add(new Area(new Ellipse2D.Float(-RADIUS*0.75f,-RADIUS*0.75f,
				2*0.75f*RADIUS,2*0.75f*RADIUS)));
		
		return (Area) caret.clone();
	}

	
	/**
	 * Return the color of the caret (red).
	 */
	@Override
	protected Color getColor() {
		return CPColor;
	}
}
