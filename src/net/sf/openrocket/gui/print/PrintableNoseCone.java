package net.sf.openrocket.gui.print;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import net.sf.openrocket.gui.rocketfigure.TransitionShapes;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.Transformation;

public class PrintableNoseCone extends AbstractPrintableTransition {
	
	/**
	 * If the component to be drawn is a nose cone, save a reference to it.
	 */
	private NoseCone target;
	
	/**
	 * Construct a printable nose cone.
	 *
	 * @param noseCone the component to print
	 */
	public PrintableNoseCone(Transition noseCone) {
		super(false, noseCone);
	}
	
	@Override
	protected void init(Transition component) {
		
		target = (NoseCone) component;
		double radius = target.getForeRadius();
		if (radius < target.getAftRadius()) {
			radius = target.getAftRadius();
		}
		setSize((int) PrintUnit.METERS.toPoints(2 * radius) + marginX,
				(int) PrintUnit.METERS.toPoints(target.getLength() + target.getAftShoulderLength()) + marginY);
	}
	
	/**
	 * Draw a nose cone.
	 *
	 * @param g2 the graphics context
	 */
	@Override
	protected void draw(Graphics2D g2) {
		Shape[] shapes = TransitionShapes.getShapesSide(target, Transformation.rotate_x(0d), PrintUnit.METERS.toPoints(1));
		
		if (shapes != null && shapes.length > 0) {
			Rectangle r = shapes[0].getBounds();
			g2.translate(marginX + r.getHeight() / 2, marginY);
			g2.rotate(Math.PI / 2);
			for (Shape shape : shapes) {
				g2.draw(shape);
			}
			g2.rotate(-Math.PI / 2);
		}
	}
}
