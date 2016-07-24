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

import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent.Position;
import net.sf.openrocket.rocketcomponent.SymmetricComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.unit.Tick;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Bounds;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

@SuppressWarnings("serial")
public class FinPointFigure extends JPanel implements ScaleFigure {
	

	// Number of pixels to leave at edges when fitting figure
	protected static final int DEFAULT_BORDER_PIXELS = 32;	
	
	protected static final float MINIMUM_CANVAS_SIZE_METERS = 0.01f; // i.e. 1 cm
	
	private static final Color GRID_LINE_COLOR = new Color( 137, 137, 137, 32);
	private static final float GRID_LINE_BASE_WIDTH = 0.001f;
	
	private static final int LINE_WIDTH_PIXELS = 1;
	// the size of the boxes around each fin point vertex
	private static final float BOX_WIDTH_PIXELS = 12; 
	
	private static final double MINOR_TICKS = 0.05;
	private static final double MAJOR_TICKS = 0.1;
	
	private final FreeformFinSet finset;
	private int modID = -1;
	
	// whatever this figure is drawing, in real-space coordinates:  meters
	protected Bounds drawableBounds_m = new Bounds(2); 
	
	protected Dimension originLocation_px = new Dimension(0,0);

	// actual size of panel in pixels; this panel may or may not be fully drawn
	protected Dimension preferredFigureSize_px = new Dimension(100,100);
	
 	
	// ========= lower-abstraction level variables
	protected int borderThickness_px = DEFAULT_BORDER_PIXELS;
	
	// actual number to multiply against rocket coordinates to display in UI
	// y'know, this *really* is a magic number of abritrary magnitude...
	protected final static double scale = 1000.0;
	
	// zoom factor, in the traditional % zoom units
	// 1.0 === 100% === no scaling.
	protected double zoom = 1.0;
	
	protected final List<StateChangeListener> listeners = new LinkedList<StateChangeListener>();
	
	// combines the translation and scale in one place: 
	// which frames does this transform between ?  
	private AffineTransform transform;
	
 	private Rectangle2D.Double[] finPointHandles = null;
 	
	
	public FinPointFigure(FreeformFinSet finset) {
		this.finset = finset;

		this.zoom = 1.0;
		// this.scale = scaleSubjectToCanvas_dpm * zoom;
		
		// useful for debugging -- shows a contrast against un-drawn space.
		setBackground(Color.WHITE);
		setOpaque(true);
		
		updateTransform();
	}

	private void dumpState( final String locationName ){
		System.err.println("dumpState from: "+this.getClass().getSimpleName()+"."+locationName);
		{
			System.err.println("    drawableBounds (m) X:"+drawableBounds_m.getX().toDebug());
			System.err.println("    drawableBounds (m) Y:"+drawableBounds_m.getY().toDebug());
		}
		System.err.println("    subclass.getPreferredSize(px):"+preferredFigureSize_px.width+", "+preferredFigureSize_px.height);
		System.err.println("    actual.getPreferredSize(px):"+getPreferredSize().width+", "+getPreferredSize().height);
		System.err.println("    act. size (px):"+this.getWidth()+", "+this.getHeight());
		System.err.println("  ");
		System.err.println(String.format("    current zoom= %6.4g%%)", this.zoom*100));
		System.err.println("    current scale = "+FinPointFigure.scale+")");
	}
	
	@Override
	public double getZoom() {
		return zoom;
	}
	
	@Override
	public double getAbsoluteScale() {
		return scale*zoom;
	}
	
	@Override
	public void setZoom( final double newZoomRequest) {
		if (Double.isInfinite(newZoomRequest) || Double.isNaN(newZoomRequest)){
			return;}
		
		final double newZoom = MathUtil.clamp( newZoomRequest, MINIMUM_ZOOM, MAXIMUM_ZOOM);
		
		if (Math.abs(this.zoom - newZoom) < MINIMUM_ZOOM){
			return;}
		
		this.zoom = newZoom;
		
		// dumpState(" setZoom( "+newZoomRequest*100+"% );");
		
		updateTransform();
		repaint();
	}
	
	@Override 
	public void zoomToSize( final Dimension requestedBounds ){
	    if( ( 0 == requestedBounds.width)||( 0 == requestedBounds.height)){
	        // invalid request values
	        return; 
	    }
	    
	    // in canvas-space
		Point2D.Double requestedSize_px = new Point2D.Double( requestedBounds.width - 2*borderThickness_px, requestedBounds.height - 2*borderThickness_px);
		
	    double widthZoom = requestedSize_px.x / scale / drawableBounds_m.getX().span();
	    double heightZoom = requestedSize_px.y / scale / drawableBounds_m.getY().span();
		double minZoom = Math.min( widthZoom, heightZoom);
        double clampedZoom = MathUtil.clamp( minZoom, MINIMUM_ZOOM, MAXIMUM_ZOOM);
		
        this.setZoom( clampedZoom );    
	}
	
	@Override
	public Dimension getBorderPixels() {
		return new Dimension(borderThickness_px, borderThickness_px);
	}
	
	@Override
	public void setBorderPixels( final int width, final int height) {
		this.borderThickness_px = Math.max( width, height);
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
		Graphics2D g2 = (Graphics2D) g.create();
		
		//dumpState("paintComponent(g)");
		
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

		paintBackgroundGrid( g2);
		
		paintRocketBody(g2);
		
		paintFinShape(g2);
	}
	
	public void paintBackgroundGrid( Graphics2D g2){
		Rectangle visible = g2.getClipBounds();
		int x0 = visible.x - 3;
		int x1 = visible.x + visible.width + 4;
		int y0 = visible.y - 3;
		int y1 = visible.y + visible.height + 4;
		
		final float grid_line_width = (float)(FinPointFigure.GRID_LINE_BASE_WIDTH/zoom);
		g2.setStroke(new BasicStroke( grid_line_width,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(FinPointFigure.GRID_LINE_COLOR);
	
		Unit unit;
		if (this.getParent() != null &&
				this.getParent().getParent() instanceof ScaleScrollPane) {
			unit = ((ScaleScrollPane) this.getParent().getParent()).getCurrentUnit();
		} else {
			unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
		}
	
		// vertical
		Tick[] verticalTicks = unit.getTicks(x0, x1, MINOR_TICKS, MAJOR_TICKS);
		Line2D.Double line = new Line2D.Double();
		for (Tick t : verticalTicks) {
			if (t.major) {
				line.setLine( t.value, y0, t.value, y1);
				g2.draw(line);
			}
		}
		
		// horizontal
		Tick[] horizontalTicks = unit.getTicks(y0, y1, MINOR_TICKS, MAJOR_TICKS);
		for (Tick t : horizontalTicks) {
			if (t.major) {
				line.setLine( x0, t.value, x1, t.value);
				g2.draw(line);
			}
		}
	}

	private void paintFinShape(Graphics2D g2){
        // excludes fin tab points
		final Coordinate[] drawPoints = finset.getFinPoints();
		
		Path2D.Double shape = new Path2D.Double();
		Coordinate startPoint= drawPoints[0];
		shape.moveTo( startPoint.x, startPoint.y);
		for (int i = 1; i < drawPoints.length; i++) {
			shape.lineTo( drawPoints[i].x, drawPoints[i].y);
		}

		final float finEdgeWidth_m = (float) (LINE_WIDTH_PIXELS / scale / zoom );
		g2.setStroke(new BasicStroke( finEdgeWidth_m, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(Color.BLUE);
		g2.draw(shape);
		
		// Fin point boxes
		final float boxWidth = (float) (BOX_WIDTH_PIXELS / scale / zoom);
		final float boxEdgeWidth_m = (float) ( LINE_WIDTH_PIXELS / scale / zoom );
        g2.setStroke(new BasicStroke( boxEdgeWidth_m, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        g2.setColor(new Color(150, 0, 0));
		
		final double boxHalfWidth = boxWidth/2;
		finPointHandles = new Rectangle2D.Double[ drawPoints.length];
		for (int i = 0; i < drawPoints.length; i++) {
			Coordinate c = drawPoints[i];
			finPointHandles[i] = new Rectangle2D.Double(c.x - boxHalfWidth, c.y - boxHalfWidth, boxWidth, boxWidth);
			g2.draw(finPointHandles[i]);
		}
	}
	
	private void paintRocketBody( Graphics2D g2){
		RocketComponent comp = finset.getParent();
		if( comp instanceof Transition ){
			paintBodyTransition(g2);
		}else{
			paintBodyTube(g2);			
		}
	}
	
	// NOTE:  This function drawns relative to the reference point of the BODY component
	// In other words: 0,0 == the front, foreRadius of the body component
	private void paintBodyTransition( Graphics2D g2){	
	    //Rectangle visible = g2.getClipBounds();
        
        // setup lines 
        final float bodyLineWidth = (float) ( LINE_WIDTH_PIXELS / scale / zoom ); 
        g2.setStroke(new BasicStroke( bodyLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        g2.setColor(Color.BLACK);

        Transition body = (Transition) finset.getParent();
        final float xResolution_m = 0.01f; // distance between draw points, in meters
        
        final double xFinStart = finset.asPositionValue(Position.TOP); //<<  in body frame
        // vv in fin-frame == draw-frame vv
        final double xOffset = -xFinStart;
        final double yOffset = -body.getRadius(xFinStart);
                
        Path2D.Double bodyShape = new Path2D.Double();
        // draw front-cap: 
        bodyShape.moveTo( xOffset, yOffset);
        bodyShape.lineTo( xOffset, yOffset + body.getForeRadius());

        final float length_m = (float)( body.getLength());
        Point2D.Double cur = new Point2D.Double ();
        for( double xBody = xResolution_m ; xBody < length_m;  xBody += xResolution_m ){
            // xBody is distance from front of parent body
            cur.x = xOffset + xBody; // offset from origin (front of fin)
            cur.y = yOffset + body.getRadius( xBody); // offset from origin ( fin-front-point ) 
            
            bodyShape.lineTo( cur.x, cur.y);
        }
        
        // draw end-cap
        bodyShape.lineTo( xOffset + length_m, yOffset + body.getAftRadius());
        bodyShape.lineTo( xOffset + length_m, yOffset);
        
        g2.draw(bodyShape);
	}
	
	private void paintBodyTube( Graphics2D g2){
		Rectangle visible = g2.getClipBounds();
		int x0 = visible.x - 3;
		int x1 = visible.x + visible.width + 4;
		
		final float bodyLineWidth = (float) ( LINE_WIDTH_PIXELS / scale / zoom ); 
		g2.setStroke(new BasicStroke( bodyLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(Color.BLACK);

		g2.drawLine((int) x0, 0, (int)x1, 0);
	}
	
	public int getIndexByPoint(double x, double y) {
		if (finPointHandles == null)
			return -1;
		
		// Calculate point in shapes' coordinates
		Point2D.Double p = new Point2D.Double(x, y);
		try {
			transform.inverseTransform(p, p);
		} catch (NoninvertibleTransformException e) {
			return -1;
		}
		
		for (int i = 0; i < finPointHandles.length; i++) {
			if (finPointHandles[i].contains(p))
				return i;
		}
		return -1;
	}
	
	
	public int getSegmentByPoint(double x, double y) {
		if (finPointHandles == null)
			return -1;
		
		// Calculate point in shapes' coordinates
		Point2D.Double p = new Point2D.Double(x, y);
		try {
			transform.inverseTransform(p, p);
		} catch (NoninvertibleTransformException e) {
			return -1;
		}
		
		double x0 = p.x;
		double y0 = p.y;
		double delta = BOX_WIDTH_PIXELS / scale;
		
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
		
		p.setLocation(p.x, p.y);
		return p;
	}
	
	// N.B. this method tells the rulers where to drawn themselves
	@Override
	public Dimension getOrigin() {
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			updateTransform();
		}
		
		return new Dimension(originLocation_px.width, originLocation_px.height);
	}
	
	private void calculateDimensions() {
        // update subject bounds
		drawableBounds_m.reset();
		// subsequent updates can only increase the size of the bounds, so this is the minimum size.
        drawableBounds_m.update( MINIMUM_CANVAS_SIZE_METERS, MINIMUM_CANVAS_SIZE_METERS);
        
        SymmetricComponent parent = (SymmetricComponent)this.finset.getParent();
        
        // N.B.: (0,0) is the fin front-- where it meets the parent body.
        final double xFinFront = finset.asPositionValue(Position.TOP); // x @ fin start, body frame
        
        // update to bound the parent body:
        final double xParentFront = -xFinFront;
        drawableBounds_m.getX().update( xParentFront);
        final double xParentBack = -xFinFront + parent.getLength();
        drawableBounds_m.getX().update( xParentBack );
        final double yParentCenter = -parent.getRadius(xFinFront); // from parent centerline to fin front.
        drawableBounds_m.getY().update( yParentCenter );
        // in 99% of fins, this bound is redundant, buuuuut just in case.  
        final double yParentMax = yParentCenter + Math.max( parent.getForeRadius(), parent.getAftRadius());
        drawableBounds_m.getY().update( yParentMax );
        
        // update to bounds the fin points:
        for (Coordinate c : finset.getFinPoints()) {
			// ignore the z coordinates; fins are treated as 2-D objects here.
			drawableBounds_m.update( c.x, c.y);
		}
	}
	
	private void calculateFigureSize(){
		// update preferred figure Size
		//final double INCHES_PER_METER = 39.3701;
		// dots     dots     inch
		// ----  = ------ * -----
		// meter    inch    meter
		//final double scale_dpm = GUIUtil.getDPI()* INCHES_PER_METER * zoom;
		final double figureSizeScale = scale*zoom;
		
		preferredFigureSize_px.width = (int)(drawableBounds_m.getX().span()*figureSizeScale) + 2*borderThickness_px;
		preferredFigureSize_px.height = (int)(drawableBounds_m.getY().span()*figureSizeScale) + 2*borderThickness_px;

		if( !preferredFigureSize_px.equals( getPreferredSize()) ){
			setPreferredSize( preferredFigureSize_px);
			revalidate();
		}
	}
	
	private void updateTransform(){
        
		calculateDimensions();
		calculateFigureSize();

		this.originLocation_px.width = borderThickness_px + (int)(-1*drawableBounds_m.getX().min*scale*zoom);
		this.originLocation_px.height = borderThickness_px + (int)(drawableBounds_m.getY().max*scale*zoom);
		
		// Calculate and store the transformation used
		transform = new AffineTransform();
		transform.translate((double)originLocation_px.width, (double)originLocation_px.height);
		transform.scale(scale*zoom , -scale*zoom );
	}
	
	public void updateFigure(){
		updateTransform();
	}

}
