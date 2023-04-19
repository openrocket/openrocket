package net.sf.openrocket.gui.figureelements;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

public abstract class Caret implements FigureElement {
	private double x,y;
	
	/**
	 * Creates a new caret at the specified coordinates.
	 */
	public Caret(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Sets the position of the caret to the new coordinates.
	 */
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Paints the caret to the Graphics2D element.
	 */
	@Override
	public void paint(Graphics2D g2, double scale) {
		Area caret = getCaret();
		AffineTransform t = new AffineTransform(1.0/scale, 0, 0, 1.0/scale, x, y);
		caret.transform(t);

		g2.setColor(getColor());
		g2.fill(caret);
	}

	
	@Override
	public void paint(Graphics2D g2, double scale, Rectangle visible) {
		throw new UnsupportedOperationException("paint() with rectangle unsupported.");
	}

	/**
	 * Return the Area object corresponding to the mark.
	 */
	protected abstract Area getCaret();
	
	/**
	 * Return the color to be used when drawing the mark.
	 */
	protected abstract Color getColor();
}
