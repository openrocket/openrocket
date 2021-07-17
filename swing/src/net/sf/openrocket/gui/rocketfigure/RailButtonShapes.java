package net.sf.openrocket.gui.rocketfigure;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;


public class RailButtonShapes extends RocketComponentShape {

	/**
	 * The rail button's shape is basically 3 cylinders stacked on top of each other. To achieve this 3D shape in a 2D
	 * way, the top and bottom faces of the cylinder are drawn as 2D ellipses and the side faces are 2D lines. To be
	 * able to select the side faces, extra invisible rectangles are added. This is because otherwise, there would just
	 * be empty space between the 2D lines and thus the rail button could not be selected.
	 * @param component
	 * @param transformation
	 * @return
	 */
	public static RocketComponentShape[] getShapesSide( final RocketComponent component, final Transformation transformation) {
		final RailButton btn = (RailButton)component;

		final double baseHeight = btn.getStandoff();
		final double innerHeight = btn.getInnerHeight();
		final double flangeHeight = btn.getFlangeHeight();
		
		final double outerDiameter = btn.getOuterDiameter();
		final double outerRadius = outerDiameter/2;
		final double innerDiameter = btn.getInnerDiameter();
		final double innerRadius = innerDiameter/2;
		
		// instance absolute location
		final Coordinate loc = transformation.transform(Coordinate.ZERO);
		
		final Coordinate unitOrientation = transformation.transform(new Coordinate(0,1,0));
		final double view_rotation_rad = -Math.atan2(unitOrientation.y, unitOrientation.z) + Math.PI/2;
		final double angle_offset_rad = btn.getAngleOffset();
		final double sinr = Math.abs(Math.sin(angle_offset_rad + view_rotation_rad));
		final double cosr = Math.cos(angle_offset_rad + view_rotation_rad);
		
		final double baseHeightcos = baseHeight*cosr;
		final double innerHeightcos = innerHeight*cosr;
		final double flangeHeightcos = flangeHeight*cosr;

		Path2D.Double path = new Path2D.Double();
		Path2D.Double pathInvis = new Path2D.Double();	// Path for the invisible triangles
		{// central pillar
			final double drawWidth = outerDiameter;
			final double drawHeight = outerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( loc.x, loc.y );
			Point2D.Double lowerLeft = new Point2D.Double( center.x - outerRadius, center.y-outerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x, lowerLeft.y, drawWidth, drawHeight), false);
			
			path.append( new Line2D.Double( lowerLeft.x,  center.y, lowerLeft.x, (center.y+baseHeightcos) ), false);
			path.append( new Line2D.Double( (center.x+outerRadius),  center.y, (center.x+outerRadius), (center.y+baseHeightcos) ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x, (lowerLeft.y+baseHeightcos), drawWidth, drawHeight), false);

			// Invisible rectangle
			double y_invis;
			if (baseHeightcos >= 0) {
				y_invis = center.y;
			}
			else {
				y_invis = center.y + baseHeightcos;
			}
			pathInvis.append(new Rectangle2D.Double(center.x-outerRadius, y_invis, drawWidth, Math.abs(baseHeightcos)), false);
		}
		
		{// inner flange
			final double drawWidth = innerDiameter;
			final double drawHeight = innerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( loc.x, loc.y + baseHeightcos);
			final Point2D.Double lowerLeft = new Point2D.Double( center.x - innerRadius, center.y-innerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x, lowerLeft.y, drawWidth, drawHeight), false);
			
			path.append( new Line2D.Double( lowerLeft.x,  center.y, lowerLeft.x, (center.y+innerHeightcos) ), false);
			path.append( new Line2D.Double( (center.x+innerRadius),  center.y, (center.x+innerRadius), (center.y+innerHeightcos) ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x, (lowerLeft.y+innerHeightcos), drawWidth, drawHeight), false);

			// Invisible rectangle
			double y_invis;
			if (innerHeightcos >= 0) {
				y_invis = center.y;
			}
			else {
				y_invis = center.y + innerHeightcos;
			}
			pathInvis.append(new Rectangle2D.Double(center.x-innerRadius, y_invis, drawWidth, Math.abs(innerHeightcos)), false);
		}
		{// outer flange
			final double drawWidth = outerDiameter;
			final double drawHeight = outerDiameter*sinr;
			final Point2D.Double center = new Point2D.Double( loc.x, loc.y+baseHeightcos+innerHeightcos);
			final Point2D.Double lowerLeft = new Point2D.Double( center.x - outerRadius, center.y-outerRadius*sinr);
			path.append( new Ellipse2D.Double( lowerLeft.x, lowerLeft.y, drawWidth, drawHeight), false);
			
			path.append( new Line2D.Double( lowerLeft.x,  center.y, lowerLeft.x, (center.y+flangeHeightcos) ), false);
			path.append( new Line2D.Double( (center.x+outerRadius),  center.y, (center.x+outerRadius), (center.y+flangeHeightcos) ), false);
			
			path.append( new Ellipse2D.Double( lowerLeft.x, (lowerLeft.y+flangeHeightcos), drawWidth, drawHeight), false);

			// Invisible rectangle
			double y_invis;
			if (flangeHeightcos >= 0) {
				y_invis = center.y;
			}
			else {
				y_invis = center.y + flangeHeightcos;
			}
			pathInvis.append(new Rectangle2D.Double(center.x-outerRadius, y_invis, drawWidth, Math.abs(flangeHeightcos)), false);
		}

		RocketComponentShape[] shapes = RocketComponentShape.toArray(new Shape[]{ path }, component);
		RocketComponentShape[] shapesInvis = RocketComponentShape.toArray(new Shape[]{ pathInvis }, component);

		for (RocketComponentShape s : shapesInvis)
			s.setColor(Color.INVISIBLE);

		RocketComponentShape[] total = Arrays.copyOf(shapes, shapes.length + shapesInvis.length);
		System.arraycopy(shapesInvis, 0, total, shapes.length, shapesInvis.length);
		return total;
	}
	

	public static RocketComponentShape[] getShapesBack( final RocketComponent component, final Transformation transformation) {
		final RailButton btn = (RailButton)component;

		final double baseHeight = btn.getStandoff();
		final double innerHeight = btn.getInnerHeight();
		final double flangeHeight = btn.getFlangeHeight();

		final double outerDiameter = btn.getOuterDiameter();
		final double outerRadius = outerDiameter/2;
		final double innerDiameter = btn.getInnerDiameter();
		final double innerRadius = innerDiameter/2;
		
		// instance absolute location
		final Coordinate loc = transformation.transform(Coordinate.ZERO);
		
		final Coordinate unitOrientation = transformation.transform(new Coordinate(0,1,0));
		final double view_rotation_rad = -Math.atan2(unitOrientation.y, unitOrientation.z) + Math.PI/2;
		final double angle_offset_rad = btn.getAngleOffset();
		final double combined_angle_rad = angle_offset_rad + view_rotation_rad;

		final double sinr = Math.sin(combined_angle_rad);
		final double cosr = Math.cos(combined_angle_rad);
		
		Path2D.Double path = new Path2D.Double();

		// base
		path.append( getRotatedRectangle( loc.z, loc.y, outerRadius, baseHeight, combined_angle_rad), false );
		
		{// inner
			final double delta_r = baseHeight;
			final double delta_y = delta_r*cosr;
			final double delta_z = delta_r*sinr;
			path.append( getRotatedRectangle( loc.z+delta_z, loc.y+delta_y, innerRadius, innerHeight, combined_angle_rad), false);
		}
		{// outer flange
			final double delta_r = baseHeight + innerHeight;
			final double delta_y = delta_r*cosr;
			final double delta_z = delta_r*sinr;
			path.append( getRotatedRectangle( loc.z+delta_z, loc.y+delta_y, outerRadius, flangeHeight, combined_angle_rad), false);
		}

		return RocketComponentShape.toArray( new Shape[]{ path }, component);
	}
	
	
	
	public static Shape getRotatedRectangle( final double x, final double y, final double radius, final double height, final double angle_rad ){
		Path2D.Double rect = new Path2D.Double();
		final double sinr = Math.sin(angle_rad);
		final double cosr = Math.cos(angle_rad);
		
		rect.moveTo( (x-radius*cosr), (y+radius*sinr));
		rect.lineTo( (x-radius*cosr+height*sinr), (y+radius*sinr+height*cosr));
		rect.lineTo( (x+radius*cosr+height*sinr), (y-radius*sinr+height*cosr));
		rect.lineTo( (x+radius*cosr), (y-radius*sinr));
		rect.closePath();
		
		return rect;
	}
}
