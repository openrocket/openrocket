package net.sf.openrocket.gui.print;

import net.sf.openrocket.gui.rocketfigure.TransitionShapes;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.util.Transformation;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

public class PrintableNoseCone extends AbstractPrintable<NoseCone> {

	/**
	 * If the component to be drawn is a nose cone, save a reference to it.
	 */
	private NoseCone target;

	/**
	 * Construct a printable nose cone.
	 *
	 * @param noseCone the component to print
	 */
	public PrintableNoseCone(NoseCone noseCone) {
		super(false, noseCone);
	}

	@Override
	protected void init(NoseCone component) {

		target = component;
		double radius = target.getForeRadius();
		if (radius < target.getAftRadius()) {
			radius = target.getAftRadius();
		}
		setSize((int) PrintUnit.METERS.toPoints(2 * radius) + 4,
				(int) PrintUnit.METERS.toPoints(target.getLength() + target.getAftShoulderLength()) + 4);
	}

	/**
	 * Draw a nose cone.  Presumes that the graphics context has already had the x/y position translated based on
     * where it should be drawn.
	 *
	 * @param g2 the graphics context
	 */
	@Override
	protected void draw(Graphics2D g2) {
		Shape[] shapes = TransitionShapes.getShapesSide(target, Transformation.rotate_x(0d), PrintUnit.METERS.toPoints(1));

		if (shapes != null && shapes.length > 0) {
			Rectangle r = shapes[0].getBounds();
			g2.translate(r.getHeight() / 2, 0);
			g2.rotate(Math.PI / 2);
			for (Shape shape : shapes) {
				g2.draw(shape);
			}
			g2.rotate(-Math.PI / 2);
		}
	}
}
