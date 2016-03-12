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
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.unit.Tick;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Bounds;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

@SuppressWarnings("serial")
public class FinPointFigure extends JPanel implements ScaleFigure, Scrollable {
	/* eventually, refactor 'Scrollable to AbstractScaleFigure*/

	// the size of the boxes around each fin point vertex
	private static final int SQUARE_WIDTH_PIXELS = 4; 

	// Number of pixels to leave at edges when fitting figure
	protected static final int DEFAULT_BORDER_PIXELS = 20;	
	
//	private static final float FIN_SEGMENT_LINE_WIDTH = 1;
//	private static final float BODY_LINE_WIDTH = 1;
//	private static final float GRID_LINE_WIDTH = 1;
	
	private static final int UNIT_SCROLL_INCREMENT_DIVISOR= 10;
	private static final int UNIT_SCROLL_MINIMUM_INCREMENT_PIXELS = 1;
	private static final int BLOCK_SCROLL_INCREMENT_DIVISOR = 100;
	private static final int BLOCK_SCROLL_MINIMUM_INCREMENT_PIXELS = 10;
	

	private final FreeformFinSet finset;
	private int modID = -1;
	
	// whatever this figure is drawing, in real-space coordinates:  meters
	protected Bounds finBounds_m = new Bounds(2);	
	
	// actual size of panel in pixels; this panel may or may not be fully drawn
	protected Dimension preferredFigureSize_px = new Dimension(100,100);
	
	protected Dimension visible_px = new Dimension(0,0);
	
	// desired size? actual size? 
	protected Dimension canvasSize_px = new Dimension(0,0);
	
	// from 0,0 => draw rectangle from origin_px + drawn_px
	protected Dimension rocketCenter_px = new Dimension(0,0);
	protected Dimension originLocation_px = new Dimension(0,0);
	protected Dimension drawnSize_px = preferredFigureSize_px;
	
	// old formulation
	private double translateX = 0;
	private double translateY = 0;
 	
 	
	// ========= lower-abstraction level variables
	protected int borderThickness_pixels = DEFAULT_BORDER_PIXELS;
	protected final double base_scale;
	
	// actual number to multiply against rocket coordinates to display in UI 
	protected double scale = 1.0;
	
	// zoom factor, in the traditional % zoom units
	// 1.0 === 100% === no scaling.
	protected double zoom = 1.0;
	
	protected final List<StateChangeListener> listeners = new LinkedList<StateChangeListener>();
	
	// combines the translation and scale in one place: 
	// which frames does this transform between ?  
	private AffineTransform transform;
	
 	// location of points?
 	private Rectangle2D.Double[] handles = null;
 	
	
	public FinPointFigure(FreeformFinSet finset) {
		this.finset = finset;
		
		// this result in a dots-per-meter scale factor 
		this.base_scale = GUIUtil.getDPI()* INCHES_PER_METER;
		this.zoom = 1.0;
		this.scale = base_scale * zoom;
			
		drawnSize_px = new Dimension( 0,0);

		setBackground(Color.WHITE);
		setOpaque(true);
		
		updateTransform();
	}

	private void dumpState( final String locationName ){
		dumpState( locationName, null );
	}

	private void dumpState( final String locationName, final String description ){
		boolean showDetail=false;
		System.err.println("dumpState from: "+this.getClass().getSimpleName()+"."+locationName);
		if( null != description){
			System.err.println("    Note: "+description);}
		if( showDetail ){
			System.err.println("    rocketBounds (m) X:"+finBounds_m.getX().toDebug());
			System.err.println("    rocketBounds (m) Y:"+finBounds_m.getY().toDebug());
		}else{	
			System.err.println("    rocketSize (m)   ("+finBounds_m.getX().span()+", "+finBounds_m.getY().span()+")");
			System.err.println("    rocketCenter (m) ("+finBounds_m.getX().center()+", "+finBounds_m.getY().center()+")");
		}
		System.err.println("    figureSize (px):  w: "+preferredFigureSize_px.width+"  h: "+preferredFigureSize_px.height);
		System.err.println("    canvasSize (px):  w: "+canvasSize_px.width+"  h: "+canvasSize_px.height);
		System.err.println("    this.width/height (px):"+this.getWidth()+", "+this.getHeight());
		final Dimension prefSize = this.getPreferredSize();
		System.err.println("    actual pref. Size(px):"+prefSize.width+", "+prefSize.height);		
		System.err.println("  ");
		System.err.println("    current zoom= "+this.zoom*100+"%)");
	}
	
	@Override
	public double getZoom() {
		return zoom;
	}
	
	@Override
	public double getAbsoluteScale() {
		return scale;
	}
	
	@Override
	public void setZoom( final double newZoomRequest) {
		if (Double.isInfinite(newZoomRequest) || Double.isNaN(newZoomRequest)){
			return;}
		
		final double newZoom = MathUtil.clamp( newZoomRequest, MINIMUM_ZOOM, MAXIMUM_ZOOM);
		
		if (Math.abs(this.zoom - newZoom) < 0.01){
			return;}
		
		this.zoom = newZoom;
		this.scale = base_scale * this.zoom;

		// vv DEBUG vv 
		dumpState(" setZoom( "+newZoomRequest*100+"% );");
		
		updateTransform();
		repaint();
	}
	
	@Override 
	public void zoomToSize( Dimension bounds ){
		double zh = 1, zv = 1;
		int w = bounds.width - 2 * borderThickness_pixels - 20;
		int h = bounds.height - 2 * borderThickness_pixels - 20;
		
		if (w < 10)
			w = 10;
		if (h < 10)
			h = 10;
		
		zh = (w) / getFigureWidth();
		zv = (h) / getFigureHeight();
		
		double s = Math.min(zh, zv) / base_scale - 0.001;
		
		// Restrict to 100%
		if (s > 1.0) {
			s = 1.0;
		}
		
		setZoom(s);
	}
	
	@Override
	public void zoomToBounds( final Dimension center , final Dimension bounds ){
		throw new IllegalStateException("This method is not yet implemented!");
	}
	
	@Override
	public Dimension getBorderPixels() {
		return new Dimension(borderThickness_pixels, borderThickness_pixels);
	}
	
	@Override
	public void setBorderPixels( final int width, final int height) {
		this.borderThickness_pixels = Math.max( width, height);
	}
	
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add( listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireChangeEvent() {
		EventObject changeEvent = new EventObject(this);
		for (EventListener l : listeners) {
			((StateChangeListener) l).stateChanged(changeEvent);
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			updateTransform();
		}
		
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

		// paintBackgroundGrid( g2);
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

		// paintRocketBody(g2);
		g2.setStroke(new BasicStroke((float) (3.0 * EXTRA_SCALE / scale),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(Color.GRAY);

		g2.drawLine((int) (x0 * EXTRA_SCALE), 0, (int) (x1 * EXTRA_SCALE), 0);

		// paintFinShape(g2);
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
		double s = SQUARE_WIDTH_PIXELS * EXTRA_SCALE / scale;
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
		double delta = SQUARE_WIDTH_PIXELS / scale;
		
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
	
	public double getFigureWidth() {
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			calculateDimensions();
		}
		// TODO: this doesn't make sense, but preserves existing behavior
		return finBounds_m.getX().span();
	}
	
	public double getFigureHeight() {
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			calculateDimensions();
		}
		// TODO: this doesn't make sense, but preserves existing behavior
		return finBounds_m.getX().span();
	}
	
	
	private void calculateDimensions() {
		finBounds_m.reset();

		for (Coordinate c : finset.getFinPoints()) {
			// ignore the z coordinates; they pointsinto the figure and provide no useful information.
			finBounds_m.update(c.x,c.y);
		}
		
		final double EPSILON_X = 0.01f;
		finBounds_m.getX().inflate(-EPSILON_X, EPSILON_X);
		
		final double finWidth_m = finBounds_m.getX().span();
		final double finHeight_m = finBounds_m.getY().span();
		
		this.preferredFigureSize_px = new Dimension(
				(int) (finWidth_m* scale + 2 * borderThickness_pixels),
				(int) (finHeight_m* scale + 2 * borderThickness_pixels));

		if( !preferredFigureSize_px.equals( getPreferredSize()) ){
			setPreferredSize( preferredFigureSize_px);
			revalidate();
		}
		
		// vv DEBUG vv
		dumpState( "calculateDimensions() ");
	}
	
	private void updateTransform(){
		calculateDimensions();
		final Point2D.Double rocketSize_m = finBounds_m.getSpanAsPoint2D();
		final Point2D.Double rocketCenter_m = finBounds_m.getCenterAsPoint2D();
		
		final double rocketWidth_m = rocketSize_m.x;
		final double rocketHeight_m = rocketSize_m.y;

		final int prefWidth = preferredFigureSize_px.width;
		final int prefHeight = preferredFigureSize_px.height;
		
		double new_x_t, new_y_t;
		// Calculate translation for figure centering
		if (rocketWidth_m * scale + 2 * borderThickness_pixels < getWidth()) {
			// Figure fits in the viewport
			new_x_t = (getWidth() - rocketWidth_m * scale) / 2 - finBounds_m.getX().min * scale;			
		} else {
			// Figure does not fit in viewport
			new_x_t = borderThickness_pixels - finBounds_m.getX().min* scale;
		}
		

		if (rocketHeight_m * scale + 2 * borderThickness_pixels < getHeight()) {
			new_y_t = getHeight() - borderThickness_pixels;
		} else {
			new_y_t = borderThickness_pixels + rocketHeight_m * scale;
		}
		
		if (Math.abs(translateX - new_x_t) > 1 || Math.abs(translateY - new_y_t) > 1) {
			// Origin has changed, fire event
			translateX = new_x_t;
			translateY = new_y_t;
			fireChangeEvent();
		}

		// Calculate and store the transformation used
		transform = new AffineTransform();
		transform.translate(translateX, translateY);
		transform.scale(scale / EXTRA_SCALE, -scale / EXTRA_SCALE);
	}
	
	public void updateFigure(){
		updateTransform();
	}
	
	// ======  ====== 'Scrollable' interface methods ====== ====== 


	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}


	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}



	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		int value=0;
		if( orientation == SwingConstants.VERTICAL ){
			value = this.drawnSize_px.width / FinPointFigure.BLOCK_SCROLL_INCREMENT_DIVISOR; 
		}else if( orientation == SwingConstants.VERTICAL ){
			value = this.drawnSize_px.width / FinPointFigure.BLOCK_SCROLL_INCREMENT_DIVISOR;
		}
		
		value = Math.max( value, FinPointFigure.BLOCK_SCROLL_MINIMUM_INCREMENT_PIXELS);
		return value;
	}


	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		int value=0;
		if( orientation == SwingConstants.VERTICAL ){
			value = this.drawnSize_px.width / FinPointFigure.UNIT_SCROLL_INCREMENT_DIVISOR; 
		}else if( orientation == SwingConstants.VERTICAL ){
			value = this.drawnSize_px.width / FinPointFigure.UNIT_SCROLL_INCREMENT_DIVISOR;
		}
		
		value = Math.max( value, FinPointFigure.UNIT_SCROLL_MINIMUM_INCREMENT_PIXELS);
		return value;
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return this.preferredFigureSize_px;
	}
	


}
