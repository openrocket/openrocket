package net.sf.openrocket.gui.scalefigure;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import net.sf.openrocket.gui.figureelements.FigureElement;
import net.sf.openrocket.gui.util.ColorConversion;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Reflection;
import net.sf.openrocket.util.Transformation;

/**
 * A <code>ScaleFigure</code> that draws a complete rocket.  Extra information can
 * be added to the figure by the methods {@link #addRelativeExtra(FigureElement)},
 * {@link #clearRelativeExtra()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketFigure extends AbstractScaleFigure {
	private static final long serialVersionUID = 1L;
	
	private static final String ROCKET_FIGURE_PACKAGE = "net.sf.openrocket.gui.rocketfigure";
	private static final String ROCKET_FIGURE_SUFFIX = "Shapes";
	
	public static final int TYPE_SIDE = 1;
	public static final int TYPE_BACK = 2;
	
	// Width for drawing normal and selected components
	public static final double NORMAL_WIDTH = 1.0;
	public static final double SELECTED_WIDTH = 2.0;
	

	private Configuration configuration;
	private RocketComponent[] selection = new RocketComponent[0];
	
	private int type = TYPE_SIDE;
	
	private double rotation;
	private Transformation transformation;
	
	private double translateX, translateY;
	


	/*
	 * figureComponents contains the corresponding RocketComponents of the figureShapes
	 */
	private final ArrayList<Shape> figureShapes = new ArrayList<Shape>();
	private final ArrayList<RocketComponent> figureComponents =
			new ArrayList<RocketComponent>();
	
	private double minX = 0, maxX = 0, maxR = 0;
	// Figure width and height in SI-units and pixels
	private double figureWidth = 0, figureHeight = 0;
	protected int figureWidthPx = 0, figureHeightPx = 0;
	
	private AffineTransform g2transformation = null;
	
	private final ArrayList<FigureElement> relativeExtra = new ArrayList<FigureElement>();
	private final ArrayList<FigureElement> absoluteExtra = new ArrayList<FigureElement>();
	
	
	/**
	 * Creates a new rocket figure.
	 */
	public RocketFigure(Configuration configuration) {
		super();
		
		this.configuration = configuration;
		
		this.rotation = 0.0;
		this.transformation = Transformation.rotate_x(0.0);
		
		updateFigure();
	}
	
	
	/**
	 * Set the configuration displayed by the figure.  It may use the same or different rocket.
	 * 
	 * @param configuration		the configuration to display.
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
		updateFigure();
	}
	
	
	@Override
	public Dimension getOrigin() {
		return new Dimension((int) translateX, (int) translateY);
	}
	
	@Override
	public double getFigureHeight() {
		return figureHeight;
	}
	
	@Override
	public double getFigureWidth() {
		return figureWidth;
	}
	
	
	public RocketComponent[] getSelection() {
		return selection;
	}
	
	public void setSelection(RocketComponent[] selection) {
		if (selection == null) {
			this.selection = new RocketComponent[0];
		} else {
			this.selection = selection;
		}
		updateFigure();
	}
	
	
	public double getRotation() {
		return rotation;
	}
	
	public Transformation getRotateTransformation() {
		return transformation;
	}
	
	public void setRotation(double rot) {
		if (MathUtil.equals(rotation, rot))
			return;
		this.rotation = rot;
		this.transformation = Transformation.rotate_x(rotation);
		updateFigure();
	}
	
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		if (type != TYPE_BACK && type != TYPE_SIDE) {
			throw new IllegalArgumentException("Illegal type: " + type);
		}
		if (this.type == type)
			return;
		this.type = type;
		updateFigure();
	}
	
	



	/**
	 * Updates the figure shapes and figure size.
	 */
	@Override
	public void updateFigure() {
		figureShapes.clear();
		figureComponents.clear();
		
		calculateSize();
		
		// Get shapes for all active components
		for (RocketComponent c : configuration) {
			Shape[] s = getShapes(c);
			for (int i = 0; i < s.length; i++) {
				figureShapes.add(s[i]);
				figureComponents.add(c);
			}
		}
		
		repaint();
		fireChangeEvent();
	}
	
	
	public void addRelativeExtra(FigureElement p) {
		relativeExtra.add(p);
	}
	
	public void removeRelativeExtra(FigureElement p) {
		relativeExtra.remove(p);
	}
	
	public void clearRelativeExtra() {
		relativeExtra.clear();
	}
	
	
	public void addAbsoluteExtra(FigureElement p) {
		absoluteExtra.add(p);
	}
	
	public void removeAbsoluteExtra(FigureElement p) {
		absoluteExtra.remove(p);
	}
	
	public void clearAbsoluteExtra() {
		absoluteExtra.clear();
	}
	
	
	/**
	 * Paints the rocket on to the Graphics element.
	 * <p>
	 * Warning:  If paintComponent is used outside the normal Swing usage, some Swing
	 * dependent parameters may be left wrong (mainly transformation).  If it is used,
	 * the RocketFigure should be repainted immediately afterwards.
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		

		AffineTransform baseTransform = g2.getTransform();
		
		// Update figure shapes if necessary
		if (figureShapes == null)
			updateFigure();
		

		double tx, ty;
		// Calculate translation for figure centering
		if (figureWidthPx + 2 * borderPixelsWidth < getWidth()) {
			
			// Figure fits in the viewport
			if (type == TYPE_BACK)
				tx = getWidth() / 2;
			else
				tx = (getWidth() - figureWidthPx) / 2 - minX * scale;
			
		} else {
			
			// Figure does not fit in viewport
			if (type == TYPE_BACK)
				tx = borderPixelsWidth + figureWidthPx / 2;
			else
				tx = borderPixelsWidth - minX * scale;
			
		}
		
		ty = computeTy(figureHeightPx);
		
		if (Math.abs(translateX - tx) > 1 || Math.abs(translateY - ty) > 1) {
			// Origin has changed, fire event
			translateX = tx;
			translateY = ty;
			fireChangeEvent();
		}
		

		// Calculate and store the transformation used
		// (inverse is used in detecting clicks on objects)
		g2transformation = new AffineTransform();
		g2transformation.translate(translateX, translateY);
		// Mirror position Y-axis upwards
		g2transformation.scale(scale / EXTRA_SCALE, -scale / EXTRA_SCALE);
		
		g2.transform(g2transformation);
		
		// Set rendering hints appropriately
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		

		// Draw all shapes
		
		for (int i = 0; i < figureShapes.size(); i++) {
			RocketComponent c = figureComponents.get(i);
			Shape s = figureShapes.get(i);
			boolean selected = false;
			
			// Check if component is in the selection
			for (int j = 0; j < selection.length; j++) {
				if (c == selection[j]) {
					selected = true;
					break;
				}
			}
			
			// Set component color and line style
			net.sf.openrocket.util.Color color = c.getColor();
			if (color == null) {
				color = Application.getPreferences().getDefaultColor(c.getClass());
			}
			g2.setColor(ColorConversion.toAwtColor(color));
			
			LineStyle style = c.getLineStyle();
			if (style == null)
				style = Application.getPreferences().getDefaultLineStyle(c.getClass());
			
			float[] dashes = style.getDashes();
			for (int j = 0; j < dashes.length; j++) {
				dashes[j] *= EXTRA_SCALE / scale;
			}
			
			if (selected) {
				g2.setStroke(new BasicStroke((float) (SELECTED_WIDTH * EXTRA_SCALE / scale),
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashes, 0));
				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_PURE);
			} else {
				g2.setStroke(new BasicStroke((float) (NORMAL_WIDTH * EXTRA_SCALE / scale),
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashes, 0));
				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_NORMALIZE);
			}
			g2.draw(s);
			
		}
		
		g2.setStroke(new BasicStroke((float) (NORMAL_WIDTH * EXTRA_SCALE / scale),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		

		// Draw motors
		String motorID = configuration.getFlightConfigurationID();
		Color fillColor = ((SwingPreferences)Application.getPreferences()).getMotorFillColor();
		Color borderColor = ((SwingPreferences)Application.getPreferences()).getMotorBorderColor();
		Iterator<MotorMount> iterator = configuration.motorIterator();
		while (iterator.hasNext()) {
			MotorMount mount = iterator.next();
			Motor motor = mount.getMotor(motorID);
			double length = motor.getLength();
			double radius = motor.getDiameter() / 2;
			
			Coordinate[] position = ((RocketComponent) mount).toAbsolute(
					new Coordinate(((RocketComponent) mount).getLength() +
							mount.getMotorOverhang() - length));
			
			for (int i = 0; i < position.length; i++) {
				position[i] = transformation.transform(position[i]);
			}
			
			for (Coordinate coord : position) {
				Shape s;
				if (type == TYPE_SIDE) {
					s = new Rectangle2D.Double(EXTRA_SCALE * coord.x,
							EXTRA_SCALE * (coord.y - radius), EXTRA_SCALE * length,
							EXTRA_SCALE * 2 * radius);
				} else {
					s = new Ellipse2D.Double(EXTRA_SCALE * (coord.z - radius),
							EXTRA_SCALE * (coord.y - radius), EXTRA_SCALE * 2 * radius,
							EXTRA_SCALE * 2 * radius);
				}
				g2.setColor(fillColor);
				g2.fill(s);
				g2.setColor(borderColor);
				g2.draw(s);
			}
		}
		


		// Draw relative extras
		for (FigureElement e : relativeExtra) {
			e.paint(g2, scale / EXTRA_SCALE);
		}
		
		// Draw absolute extras
		g2.setTransform(baseTransform);
		Rectangle rect = this.getVisibleRect();
		
		for (FigureElement e : absoluteExtra) {
			e.paint(g2, 1.0, rect);
		}
		
	}
	
	protected double computeTy(int heightPx) {
		final double ty;
		if (heightPx + 2 * borderPixelsHeight < getHeight()) {
			ty = getHeight() / 2;
		} else {
			ty = borderPixelsHeight + heightPx / 2;
		}
		return ty;
	}
	
	
	public RocketComponent[] getComponentsByPoint(double x, double y) {
		// Calculate point in shapes' coordinates
		Point2D.Double p = new Point2D.Double(x, y);
		try {
			g2transformation.inverseTransform(p, p);
		} catch (NoninvertibleTransformException e) {
			return new RocketComponent[0];
		}
		
		LinkedHashSet<RocketComponent> l = new LinkedHashSet<RocketComponent>();
		
		for (int i = 0; i < figureShapes.size(); i++) {
			if (figureShapes.get(i).contains(p))
				l.add(figureComponents.get(i));
		}
		return l.toArray(new RocketComponent[0]);
	}
	
	

	/**
	 * Gets the shapes required to draw the component.
	 * 
	 * @param component
	 * @param params
	 * @return
	 */
	private Shape[] getShapes(RocketComponent component) {
		Reflection.Method m;
		
		// Find the appropriate method
		switch (type) {
		case TYPE_SIDE:
			m = Reflection.findMethod(ROCKET_FIGURE_PACKAGE, component, ROCKET_FIGURE_SUFFIX, "getShapesSide",
					RocketComponent.class, Transformation.class);
			break;
		
		case TYPE_BACK:
			m = Reflection.findMethod(ROCKET_FIGURE_PACKAGE, component, ROCKET_FIGURE_SUFFIX, "getShapesBack",
					RocketComponent.class, Transformation.class);
			break;
		
		default:
			throw new BugException("Unknown figure type = " + type);
		}
		
		if (m == null) {
			Application.getExceptionHandler().handleErrorCondition("ERROR: Rocket figure paint method not found for "
					+ component);
			return new Shape[0];
		}
		
		return (Shape[]) m.invokeStatic(component, transformation);
	}
	
	

	/**
	 * Gets the bounds of the figure, i.e. the maximum extents in the selected dimensions.
	 * The bounds are stored in the variables minX, maxX and maxR.
	 */
	private void calculateFigureBounds() {
		Collection<Coordinate> bounds = configuration.getBounds();
		
		if (bounds.isEmpty()) {
			minX = 0;
			maxX = 0;
			maxR = 0;
			return;
		}
		
		minX = Double.MAX_VALUE;
		maxX = Double.MIN_VALUE;
		maxR = 0;
		for (Coordinate c : bounds) {
			double x = c.x, r = MathUtil.hypot(c.y, c.z);
			if (x < minX)
				minX = x;
			if (x > maxX)
				maxX = x;
			if (r > maxR)
				maxR = r;
		}
	}
	
	
	public double getBestZoom(Rectangle2D bounds) {
		double zh = 1, zv = 1;
		if (bounds.getWidth() > 0.0001)
			zh = (getWidth() - 2 * borderPixelsWidth) / bounds.getWidth();
		if (bounds.getHeight() > 0.0001)
			zv = (getHeight() - 2 * borderPixelsHeight) / bounds.getHeight();
		return Math.min(zh, zv);
	}
	
	

	/**
	 * Calculates the necessary size of the figure and set the PreferredSize 
	 * property accordingly.
	 */
	private void calculateSize() {
		calculateFigureBounds();
		
		switch (type) {
		case TYPE_SIDE:
			figureWidth = maxX - minX;
			figureHeight = 2 * maxR;
			break;
		
		case TYPE_BACK:
			figureWidth = 2 * maxR;
			figureHeight = 2 * maxR;
			break;
		
		default:
			assert (false) : "Should not occur, type=" + type;
			figureWidth = 0;
			figureHeight = 0;
		}
		
		figureWidthPx = (int) (figureWidth * scale);
		figureHeightPx = (int) (figureHeight * scale);
		
		Dimension d = new Dimension(figureWidthPx + 2 * borderPixelsWidth,
				figureHeightPx + 2 * borderPixelsHeight);
		
		if (!d.equals(getPreferredSize()) || !d.equals(getMinimumSize())) {
			setPreferredSize(d);
			setMinimumSize(d);
			revalidate();
		}
	}
	
	public Rectangle2D getDimensions() {
		switch (type) {
		case TYPE_SIDE:
			return new Rectangle2D.Double(minX, -maxR, maxX - minX, 2 * maxR);
			
		case TYPE_BACK:
			return new Rectangle2D.Double(-maxR, -maxR, 2 * maxR, 2 * maxR);
			
		default:
			throw new BugException("Illegal figure type = " + type);
		}
	}
	
}
