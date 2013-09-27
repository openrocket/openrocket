package net.sf.openrocket.gui.scalefigure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.unit.Tick;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;


// TODO: MEDIUM:  the figure jumps and bugs when using automatic fitting

public class FinPointFigure extends AbstractScaleFigure {
	
	private static final int BOX_SIZE = 4;
	
	private final FreeformFinSet finset;
	private int modID = -1;
	
	private double minX, maxX, maxY;
	private double figureWidth = 0;
	private double figureHeight = 0;
	private double translateX = 0;
	private double translateY = 0;
	
	private AffineTransform transform;
	private Rectangle2D.Double[] handles = null;
	
	
	public FinPointFigure(FreeformFinSet finset) {
		this.finset = finset;
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			calculateDimensions();
		}
		

		double tx, ty;
		// Calculate translation for figure centering
		if (figureWidth * scale + 2 * borderPixelsWidth < getWidth()) {
			
			// Figure fits in the viewport
			tx = (getWidth() - figureWidth * scale) / 2 - minX * scale;
			
		} else {
			
			// Figure does not fit in viewport
			tx = borderPixelsWidth - minX * scale;
			
		}
		

		if (figureHeight * scale + 2 * borderPixelsHeight < getHeight()) {
			ty = getHeight() - borderPixelsHeight;
		} else {
			ty = borderPixelsHeight + figureHeight * scale;
		}
		
		if (Math.abs(translateX - tx) > 1 || Math.abs(translateY - ty) > 1) {
			// Origin has changed, fire event
			translateX = tx;
			translateY = ty;
			fireChangeEvent();
		}
		

		if (Math.abs(translateX - tx) > 1 || Math.abs(translateY - ty) > 1) {
			// Origin has changed, fire event
			translateX = tx;
			translateY = ty;
			fireChangeEvent();
		}
		

		// Calculate and store the transformation used
		transform = new AffineTransform();
		transform.translate(translateX, translateY);
		transform.scale(scale / EXTRA_SCALE, -scale / EXTRA_SCALE);
		
		// TODO: HIGH:  border Y-scale upwards
		
		g2.transform(transform);
		
		// Set rendering hints appropriately
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		


		Rectangle visible = g2.getClipBounds();
		double x0 = ((double) visible.x - 3) / EXTRA_SCALE;
		double x1 = ((double) visible.x + visible.width + 4) / EXTRA_SCALE;
		double y0 = ((double) visible.y - 3) / EXTRA_SCALE;
		double y1 = ((double) visible.y + visible.height + 4) / EXTRA_SCALE;
		

		// Background grid
		
		g2.setStroke(new BasicStroke((float) (1.0 * EXTRA_SCALE / scale),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(new Color(0, 0, 255, 30));
		
		Unit unit;
		if (this.getParent() != null &&
				this.getParent().getParent() instanceof ScaleScrollPane) {
			unit = ((ScaleScrollPane) this.getParent().getParent()).getCurrentUnit();
		} else {
			unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
		}
		
		// vertical
		Tick[] ticks = unit.getTicks(x0, x1,
				ScaleScrollPane.MINOR_TICKS / scale,
				ScaleScrollPane.MAJOR_TICKS / scale);
		Line2D.Double line = new Line2D.Double();
		for (Tick t : ticks) {
			if (t.major) {
				line.setLine(t.value * EXTRA_SCALE, y0 * EXTRA_SCALE,
						t.value * EXTRA_SCALE, y1 * EXTRA_SCALE);
				g2.draw(line);
			}
		}
		
		// horizontal
		ticks = unit.getTicks(y0, y1,
				ScaleScrollPane.MINOR_TICKS / scale,
				ScaleScrollPane.MAJOR_TICKS / scale);
		for (Tick t : ticks) {
			if (t.major) {
				line.setLine(x0 * EXTRA_SCALE, t.value * EXTRA_SCALE,
						x1 * EXTRA_SCALE, t.value * EXTRA_SCALE);
				g2.draw(line);
			}
		}
		




		// Base rocket line
		g2.setStroke(new BasicStroke((float) (3.0 * EXTRA_SCALE / scale),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(Color.GRAY);
		
		g2.drawLine((int) (x0 * EXTRA_SCALE), 0, (int) (x1 * EXTRA_SCALE), 0);
		

		// Fin shape
		Coordinate[] points = finset.getFinPoints();
		Path2D.Double shape = new Path2D.Double();
		shape.moveTo(0, 0);
		for (int i = 1; i < points.length; i++) {
			shape.lineTo(points[i].x * EXTRA_SCALE, points[i].y * EXTRA_SCALE);
		}
		
		g2.setStroke(new BasicStroke((float) (1.0 * EXTRA_SCALE / scale),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(Color.BLACK);
		g2.draw(shape);
		

		// Fin point boxes
		g2.setColor(new Color(150, 0, 0));
		double s = BOX_SIZE * EXTRA_SCALE / scale;
		handles = new Rectangle2D.Double[points.length];
		for (int i = 0; i < points.length; i++) {
			Coordinate c = points[i];
			handles[i] = new Rectangle2D.Double(c.x * EXTRA_SCALE - s, c.y * EXTRA_SCALE - s, 2 * s, 2 * s);
			g2.draw(handles[i]);
		}
		
	}
	
	

	public int getIndexByPoint(double x, double y) {
		if (handles == null)
			return -1;
		
		// Calculate point in shapes' coordinates
		Point2D.Double p = new Point2D.Double(x, y);
		try {
			transform.inverseTransform(p, p);
		} catch (NoninvertibleTransformException e) {
			return -1;
		}
		
		for (int i = 0; i < handles.length; i++) {
			if (handles[i].contains(p))
				return i;
		}
		return -1;
	}
	
	
	public int getSegmentByPoint(double x, double y) {
		if (handles == null)
			return -1;
		
		// Calculate point in shapes' coordinates
		Point2D.Double p = new Point2D.Double(x, y);
		try {
			transform.inverseTransform(p, p);
		} catch (NoninvertibleTransformException e) {
			return -1;
		}
		
		double x0 = p.x / EXTRA_SCALE;
		double y0 = p.y / EXTRA_SCALE;
		double delta = BOX_SIZE / scale;
		
		//System.out.println("Point: " + x0 + "," + y0);
		//System.out.println("delta: " + (BOX_SIZE / scale));
		
		Coordinate[] points = finset.getFinPoints();
		for (int i = 1; i < points.length; i++) {
			double x1 = points[i - 1].x;
			double y1 = points[i - 1].y;
			double x2 = points[i].x;
			double y2 = points[i].y;
			
			//			System.out.println("point1:"+x1+","+y1+" point2:"+x2+","+y2);
			
			double u = Math.abs((x2 - x1) * (y1 - y0) - (x1 - x0) * (y2 - y1)) /
						MathUtil.hypot(x2 - x1, y2 - y1);
			//System.out.println("Distance of segment " + i + " is " + u);
			if (u < delta)
				return i;
		}
		
		return -1;
	}
	
	
	public Point2D.Double convertPoint(double x, double y) {
		Point2D.Double p = new Point2D.Double(x, y);
		try {
			transform.inverseTransform(p, p);
		} catch (NoninvertibleTransformException e) {
			assert (false) : "Should not occur";
			return new Point2D.Double(0, 0);
		}
		
		p.setLocation(p.x / EXTRA_SCALE, p.y / EXTRA_SCALE);
		return p;
	}
	
	

	@Override
	public Dimension getOrigin() {
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			calculateDimensions();
		}
		return new Dimension((int) translateX, (int) translateY);
	}
	
	@Override
	public double getFigureWidth() {
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			calculateDimensions();
		}
		return figureWidth;
	}
	
	@Override
	public double getFigureHeight() {
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			calculateDimensions();
		}
		return figureHeight;
	}
	
	
	private void calculateDimensions() {
		minX = 0;
		maxX = 0;
		maxY = 0;
		
		for (Coordinate c : finset.getFinPoints()) {
			if (c.x < minX)
				minX = c.x;
			if (c.x > maxX)
				maxX = c.x;
			if (c.y > maxY)
				maxY = c.y;
		}
		
		if (maxX < 0.01)
			maxX = 0.01;
		
		figureWidth = maxX - minX;
		figureHeight = maxY;
		

		Dimension d = new Dimension((int) (figureWidth * scale + 2 * borderPixelsWidth),
				(int) (figureHeight * scale + 2 * borderPixelsHeight));
		
		if (!d.equals(getPreferredSize()) || !d.equals(getMinimumSize())) {
			setPreferredSize(d);
			setMinimumSize(d);
			revalidate();
		}
	}
	
	

	@Override
	public void updateFigure() {
		repaint();
	}
	


}
