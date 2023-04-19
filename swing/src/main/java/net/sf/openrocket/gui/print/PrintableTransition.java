package net.sf.openrocket.gui.print;

import net.sf.openrocket.rocketcomponent.Transition;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * This class allows for a Transition to be printable.  It does so by decorating an existing transition (which will not be
 * modified) and rendering it within a JPanel.  The JPanel is not actually visualized on a display, but instead renders
 * it to a print device.
 * <p/>
 * Note: Currently nose cones are only supported by drawing the 2D projection of the profile.  A more useful approach
 * may be to draw a myriahedral projection that can be cut out and bent to form the shape.
 */
public class PrintableTransition extends AbstractPrintable<Transition> {

	/**
	 * Dashed array value.
	 */
	private final static float dash1[] = { 4.0f };
	/**
	 * The dashed stroke for glue tab.
	 */
	private final static BasicStroke dashed = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER,
			10.0f, dash1, 0.0f);

	/**
	 * The layout is an outer arc, an inner arc, and two lines one either endpoints that connect the arcs.
	 * Most of the math involves transposing geometric cartesian coordinates to the Java AWT coordinate system.
	 */
	private Path2D gp;

	/**
	 * The glue tab.
	 */
	private Path2D glueTab1;

	/**
	 * The alignment marks.
	 */
	private Line2D tick1, tick2;

	/**
	 * The x coordinates for the two ticks drawn at theta degrees.
	 */
	private int tick3X, tick4X;

	/**
	 * The angle, in degrees.
	 */
	private float theta;

	/**
	 * The x,y coordinates for where the virtual circle center is located.
	 */
	private int circleCenterX, circleCenterY;

	/**
	 * Constructor.
	 *
	 * @param transition the transition to print
	 */
	public PrintableTransition(Transition transition) {
		super(transition);
	}

    /**
     * Compute the basic values of each arc of the transition/shroud.  This is adapted from
     * <a href="http://www.rocketshoppe.com/info/Transitions.pdf">The Properties of
     * Model Rocket Body Tube Transitions, by J.R. Brohm</a>
     *
     * @param component the component
     */
	@Override
	protected void init(Transition component) {

		double r1 = component.getAftRadius();
		double r2 = component.getForeRadius();

		//Regardless of orientation, we have the convention of R1 as the smaller radius.  Flip if different.
		if (r1 > r2) {
			r1 = r2;
			r2 = component.getAftRadius();
		}
		double len = component.getLength();
		double v = r2 - r1;
		double tmp = Math.sqrt(v * v + len * len);
		double factor = tmp / v;

		theta = (float) (360d * v / tmp);

		int r1InPoints = (int) PrintUnit.METERS.toPoints(r1 * factor);
		int r2InPoints = (int) PrintUnit.METERS.toPoints(r2 * factor);

		int x = 0;
		int tabOffset = 35;
		int y = tabOffset;

		Arc2D.Double outerArc = new Arc2D.Double();
		Arc2D.Double innerArc = new Arc2D.Double();

		//If the arcs are more than 3/4 of a circle, then assume the height (y) is the same as the radius of the bigger arc.
		if (theta >= 270) {
			y += r2InPoints;
		}
		//If the arc is between 1/2 and 3/4 of a circle, then compute the actual height based upon the angle and radius
		//of the bigger arc.
		else if (theta >= 180) {
			double thetaRads = Math.toRadians(theta - 180);
			y += (int) ((Math.cos(thetaRads) * r2InPoints) * Math.tan(thetaRads));
		}

		circleCenterY = y;
		circleCenterX = r2InPoints + x;

		//Create the larger arc.
		outerArc.setArcByCenter(circleCenterX, circleCenterY, r2InPoints, 180, theta, Arc2D.OPEN);

		//Create the smaller arc.
		innerArc.setArcByCenter(circleCenterX, circleCenterY, r1InPoints, 180, theta, Arc2D.OPEN);

		//Create the line between the start of the larger arc and the start of the smaller arc.
		Path2D.Double line = new Path2D.Double();
		line.setWindingRule(Path2D.WIND_NON_ZERO);
		line.moveTo(x, y);
		final int width = r2InPoints - r1InPoints;
		line.lineTo(width + x, y);

		//Create the line between the endpoint of the larger arc and the endpoint of the smaller arc.
		Path2D.Double closingLine = new Path2D.Double();
		closingLine.setWindingRule(Path2D.WIND_NON_ZERO);
		Point2D innerArcEndPoint = innerArc.getEndPoint();
		closingLine.moveTo(innerArcEndPoint.getX(), innerArcEndPoint.getY());
		Point2D outerArcEndPoint = outerArc.getEndPoint();
		closingLine.lineTo(outerArcEndPoint.getX(), outerArcEndPoint.getY());

		//Add all shapes to the polygon path.
		gp = new Path2D.Float(GeneralPath.WIND_EVEN_ODD, 4);
		gp.append(line, false);
		gp.append(outerArc, false);
		gp.append(closingLine, false);
		gp.append(innerArc, false);

		//Create the glue tab.
		glueTab1 = new Path2D.Float(GeneralPath.WIND_EVEN_ODD, 4);
		glueTab1.moveTo(x, y);
		glueTab1.lineTo(x + tabOffset, y - tabOffset);
		glueTab1.lineTo(width + x - tabOffset, y - tabOffset);
		glueTab1.lineTo(width + x, y);

		//Create tick marks for alignment, 1/4 of the width in from either edge
		int fromEdge = width / 4;
		final int tickLength = 8;
		//Upper left
		tick1 = new Line2D.Float(x + fromEdge, y, x + fromEdge, y + tickLength);
		//Upper right
		tick2 = new Line2D.Float(x + width - fromEdge, y, x + width - fromEdge, y + tickLength);

		tick3X = r2InPoints - fromEdge;
		tick4X = r1InPoints + fromEdge;

		setSize(gp.getBounds().width, gp.getBounds().height + tabOffset);
	}

	/**
	 * Draw alignment marks on an angle.
	 *
	 * @param g2    the graphics context
	 * @param x     the center of the circle's x coordinate
	 * @param y     the center of the circle's y
	 * @param line  the line to draw
	 * @param myTheta the angle
	 */
	private void drawAlignmentMarks(Graphics2D g2, int x, int y, Line2D.Float line, float myTheta) {
		g2.translate(x, y);
		g2.rotate(Math.toRadians(-myTheta));
		g2.draw(line);
		g2.rotate(Math.toRadians(myTheta));
		g2.translate(-x, -y);
	}

	/**
	 * Draw a transition.
	 *
	 * @param g2 the graphics context
	 */
	@Override
	protected void draw(Graphics2D g2) {
		//Render it.
		g2.draw(gp);
		g2.draw(tick1);
		g2.draw(tick2);
		drawAlignmentMarks(g2, circleCenterX,
				circleCenterY,
				new Line2D.Float(-tick3X, 0, -tick3X, -8),
				theta);
		drawAlignmentMarks(g2, circleCenterX,
				circleCenterY,
				new Line2D.Float(-tick4X, 0, -tick4X, -8),
				theta);

		g2.setStroke(dashed);
		g2.draw(glueTab1);
	}

}
