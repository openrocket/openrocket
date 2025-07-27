package info.openrocket.swing.gui.scalefigure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.unit.Tick;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BoundingBox;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.StateChangeListener;


@SuppressWarnings("serial")
public class FinPointFigure extends AbstractScaleFigure {

	private static final int GRID_LINE_BASE_WIDTH_PIXELS = 1;

	private static final int LINE_WIDTH_FIN_PIXELS = 1;
	private static final int LINE_WIDTH_BODY_PIXELS = 2;
	private static final int LINE_WIDTH_HIGHLIGHT_PIXELS = 3;

	// the size of the boxes around each fin point vertex
	private static final int LINE_WIDTH_BOX_PIXELS = 1;
	private static final float BOX_WIDTH_PIXELS = 12;
	private static final float SELECTED_BOX_WIDTH_PIXELS = BOX_WIDTH_PIXELS + 4;
	private static final double MINOR_TICKS = 10.0;
	private static final double MAJOR_TICKS = 100.0;

	private final FreeformFinSet finset;
	private ModID modID = ModID.INVALID;

	protected BoundingBox finBounds_m = null;
	// Fin parent bounds
	protected BoundingBox mountBoundsMax_m = null;		// Bounds using the maximum parent radius
	protected BoundingBox mountBoundsMin_m = null;		// Bound using the minimum parent radius

	protected final List<StateChangeListener> listeners = new LinkedList<>();

	private Rectangle2D.Double[] finPointHandles = null;
	private int selectedIndex = -1;
	private int highlightIndex = -1;					// The first index of the segment to highlight when snapping to a fin point

	private static Color backgroundColor;
	private static Color finPointBodyLineColor;
	private static Color finPointGridMajorLineColor;
	private static Color finPointGridMinorLineColor;
	private static Color finPointPointColor;
	private static Color finPointSelectedPointColor;
	private static Color finPointSnapHighlightColor;

	static {
		initColors();
	}
	
	public FinPointFigure(FreeformFinSet finset) {
		this.finset = finset;

		setBackground(backgroundColor);
		setOpaque(true);

		updateFigure();
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(FinPointFigure::updateColors);
	}

	public static void updateColors() {
		backgroundColor = GUIUtil.getUITheme().getBackgroundColor();
		finPointBodyLineColor = GUIUtil.getUITheme().getFinPointBodyLineColor();
		finPointGridMajorLineColor = GUIUtil.getUITheme().getFinPointGridMajorLineColor();
		finPointGridMinorLineColor = GUIUtil.getUITheme().getFinPointGridMinorLineColor();
		finPointPointColor = GUIUtil.getUITheme().getFinPointPointColor();
		finPointSelectedPointColor = GUIUtil.getUITheme().getFinPointSelectedPointColor();
		finPointSnapHighlightColor = GUIUtil.getUITheme().getFinPointSnapHighlightColor();
	}

	@Override
	public Point getAutoZoomPoint(){
		return new Point( Math.max(0, (originLocation_px.x - borderThickness_px.width)), 0);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();

		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			updateTransform();
		}

		g2.transform(projection);
		
		// Set rendering hints appropriately
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		paintBackgroundGrid(g2);

		paintRocketBody(g2);
		
		paintFinShape(g2);
		paintHighlight(g2);
		paintFinHandles(g2);
	}
	
	public void paintBackgroundGrid( Graphics2D g2) {
		Rectangle visible = g2.getClipBounds();
		final double x0 = visible.x - 3;
		final double x1 = visible.x + visible.width + 4;
		final double y0 = visible.y - 3;
		final double y1 = visible.y + visible.height + 4;

		final float grid_line_width = (float)(GRID_LINE_BASE_WIDTH_PIXELS/this.scale);
		g2.setStroke(new BasicStroke( grid_line_width,
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

		Unit unit;
		if (this.getParent() != null && this.getParent().getParent() instanceof ScaleScrollPane) {
			unit = ((ScaleScrollPane) this.getParent().getParent()).getCurrentUnit();
		} else {
			unit = UnitGroup.UNITS_LENGTH.getDefaultUnit();
		}

		// vertical
		Tick[] verticalTicks = unit.getTicks(x0, x1, MINOR_TICKS / this.scale, MAJOR_TICKS / this.scale);
		Line2D.Double line = new Line2D.Double();
		for (Tick t : verticalTicks) {
			if (t.major) {
				g2.setColor(finPointGridMajorLineColor);
				line.setLine( t.value, y0, t.value, y1);
				g2.draw(line);
			}else{
				g2.setColor(finPointGridMinorLineColor);
				line.setLine( t.value, y0, t.value, y1);
				g2.draw(line);
			}
		}

		// horizontal
		Tick[] horizontalTicks = unit.getTicks(y0, y1, MINOR_TICKS / this.scale, MAJOR_TICKS / this.scale);
		for (Tick t : horizontalTicks) {
			if (t.major) {
				g2.setColor(finPointGridMajorLineColor);
				line.setLine( x0, t.value, x1, t.value);
				g2.draw(line);
			}else{
				g2.setColor(finPointGridMinorLineColor);
				line.setLine( x0, t.value, x1, t.value);
				g2.draw(line);
			}
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

	// NOTE:  This function draws relative to the reference point of the BODY component
	// In other words: 0,0 == the front, foreRadius of the body component
	private void paintBodyTransition( Graphics2D g2){

		// setup lines
		final float bodyLineWidth = (float) ( LINE_WIDTH_BODY_PIXELS / scale );
		g2.setStroke(new BasicStroke( bodyLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(finPointBodyLineColor);

		Transition body = (Transition) finset.getParent();
		final float xResolution_m = 0.01f; // distance between draw points, in meters

		final double xFinStart = finset.getAxialOffset(AxialMethod.TOP); //<<  in body frame

		// vv in fin-frame == draw-frame vv
		final double xOffset = -xFinStart;
		final double yOffset = -body.getRadius(xFinStart);
		final float length_m = (float)( body.getLength());

		Path2D.Double bodyShape = new Path2D.Double();
		// draw front-cap:
		bodyShape.moveTo( xOffset, yOffset);
		bodyShape.lineTo( xOffset, yOffset + body.getForeRadius());

		// draw edge
		Point2D.Double cur = new Point2D.Double();
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
		// in-figure left extent
		final double xFore = mountBoundsMax_m.min.x;
		// in-figure right extent
		final double xAft = mountBoundsMax_m.max.x;
		// in-figure right extent
		final double yCenter = mountBoundsMax_m.min.y;

		Path2D.Double shape = new Path2D.Double();
		shape.moveTo( xFore, yCenter );
		shape.lineTo( xFore, 0);         // body tube fore edge
		shape.lineTo( xAft, 0);          // body tube side
		shape.lineTo( xAft, yCenter);    // body tube aft edge

		final float bodyLineWidth = (float) ( LINE_WIDTH_BODY_PIXELS / scale );
		g2.setStroke(new BasicStroke( bodyLineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(finPointBodyLineColor);
		g2.draw(shape);
	}

	private void paintFinShape(final Graphics2D g2){
		// excludes fin tab points
		final Coordinate[] drawPoints = finset.getFinPointsWithRoot();

		Path2D.Double shape = new Path2D.Double();
		Coordinate startPoint= drawPoints[0];
		shape.moveTo( startPoint.x, startPoint.y);
		for (int i = 1; i < drawPoints.length; i++) {
			shape.lineTo( drawPoints[i].x, drawPoints[i].y);
		}

		final float finEdgeWidth_m = (float) (LINE_WIDTH_FIN_PIXELS / scale  );
		g2.setStroke(new BasicStroke( finEdgeWidth_m, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(finPointBodyLineColor);
		g2.draw(shape);
	}

	/**
	 * Paints the highlight line between the two points when snapping to a fin point.
	 * @param g2 The graphics context to paint to.
	 */
	private void paintHighlight(final Graphics2D g2) {
		final Coordinate[] points = finset.getFinPointsWithRoot();

		if (highlightIndex < 0 || highlightIndex > points.length - 1) {
			return;
		}

		Coordinate start = points[highlightIndex];
		Coordinate end = points[highlightIndex+1];

		final float highlightWidth_m = (float) (LINE_WIDTH_HIGHLIGHT_PIXELS / scale  );
		g2.setStroke(new BasicStroke(highlightWidth_m));
		g2.setColor(finPointSnapHighlightColor);
		g2.draw(new Line2D.Double(start.x, start.y, end.x, end.y));
	}
	
	private void paintFinHandles(final Graphics2D g2) {
		// Excludes fin tab points
		final Coordinate[] drawPoints = finset.getFinPoints();

		// Fin point boxes
		final float boxWidth = (float) (BOX_WIDTH_PIXELS / scale );
		final float boxHalfWidth = boxWidth/2;

		final float boxEdgeWidth_m = (float) ( LINE_WIDTH_BOX_PIXELS / scale );
		g2.setStroke(new BasicStroke( boxEdgeWidth_m, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setColor(finPointPointColor);

		finPointHandles = new Rectangle2D.Double[ drawPoints.length];
		for (int currentIndex = 0; currentIndex < drawPoints.length; currentIndex++) {
			Coordinate c = drawPoints[currentIndex];

			if( currentIndex == selectedIndex ) {
				final float selBoxWidth = (float) (SELECTED_BOX_WIDTH_PIXELS / scale );
				final float selBoxHalfWidth = selBoxWidth/2;

				final Rectangle2D.Double selectedPointHighlight = new Rectangle2D.Double(c.x - selBoxHalfWidth, c.y - selBoxHalfWidth, selBoxWidth, selBoxWidth);

				// Switch to the highlight color
				g2.setColor(finPointSelectedPointColor);
				g2.draw(selectedPointHighlight);

				// Reset to the normal color
				g2.setColor(finPointPointColor);
			}

			// Normal boxes
			finPointHandles[currentIndex] = new Rectangle2D.Double(c.x - boxHalfWidth, c.y - boxHalfWidth, boxWidth, boxWidth);

			g2.draw(finPointHandles[currentIndex]);
		}
	}

	private Point2D.Double getPoint( final int x, final int y){
		if (finPointHandles == null)
			return null;

		 // Calculate point in shapes' coordinates
		 Point2D.Double p = new Point2D.Double(x, y);
		 try {
				 projection.inverseTransform(p, p);
				 return p;
		 } catch (NoninvertibleTransformException e) {
				 return null;
		 }
	}
	
	public int getIndexByPoint(final int x, final int y) {
		final Point2D.Double p = getPoint(x,y);
		if (p == null)
			return -1;

		 for (int i = 0; i < finPointHandles.length; i++) {
			 if (finPointHandles[i].contains(p)) {
				 return i;
			 }
		 }

		 return -1;
	}

	public int getSegmentByPoint(final int x, final int y) {
		final Point2D.Double p = getPoint(x,y);
		if (p == null)
			 return -1;

		final double threshold = BOX_WIDTH_PIXELS / scale;
		
		Coordinate[] points = finset.getFinPoints();
		for (int i = 1; i < points.length; i++) {
			double x1 = points[i - 1].x;
			double y1 = points[i - 1].y;
			double x2 = points[i].x;
			double y2 = points[i].y;
			
			final double segmentLength = MathUtil.hypot(x2 - x1, y2 - y1);

			// Distance to an infinite line, defined by two points:
			// (For a more in-depth explanation, see wikipedia: https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line )
			double x0 = p.x;
			double y0 = p.y;
			final double distanceToLine = Math.abs((y2 - y1)*x0 - (x2-x1)*y0 + x2*y1 - y2*x1)/segmentLength;

			final double distanceToStart = MathUtil.hypot(x1-x0, y1-y0);
			final double distanceToEnd = MathUtil.hypot(x2-x0, y2-y0);
			final boolean withinSegment = (distanceToStart < segmentLength && distanceToEnd < segmentLength);

			if ( distanceToLine < threshold && withinSegment){
				return i;
			}

		}
		
		return -1;
	}
	
	public Point2D.Double convertPoint(final double x, final double y) {
		Point2D.Double p = new Point2D.Double(x, y);
		try {
			projection.inverseTransform(p, p);
		} catch (NoninvertibleTransformException e) {
			assert (false) : "Should not occur";
			return new Point2D.Double(0, 0);
		}

		p.x = MathUtil.clamp(p.x, -FreeformFinSet.SNAP_LARGER_THAN, FreeformFinSet.SNAP_LARGER_THAN);
		p.y = MathUtil.clamp(p.y, -FreeformFinSet.SNAP_LARGER_THAN, FreeformFinSet.SNAP_LARGER_THAN);

		return p;
	}

	public Point getSubjectOrigin() {
		if (modID != finset.getRocket().getAerodynamicModID()) {
			modID = finset.getRocket().getAerodynamicModID();
			updateTransform();
		}
		return new Point(originLocation_px.x, originLocation_px.y);
	}

	@Override
	protected void updateSubjectDimensions(){
		// update subject (i.e. Fin) bounds
		finBounds_m = new BoundingBox().update(finset.getFinPoints());
		subjectBounds_m = finBounds_m.toRectangle();

		// update to bound the parent body:
		SymmetricComponent parent = (SymmetricComponent)this.finset.getParent();
		final double xFinFront = finset.getAxialOffset(AxialMethod.TOP);
		final double xParent = -xFinFront;
		final double yParent = -parent.getRadius(xParent); // from fin-front to parent centerline
		final double rParentMax = Math.max(parent.getForeRadius(), parent.getAftRadius());
		final double rParentMin = Math.min(parent.getForeRadius(), parent.getAftRadius());

		mountBoundsMax_m = new BoundingBox()
				.update(new Coordinate(xParent, yParent, 0))
				.update(new Coordinate(xParent + parent.getLength(), yParent + rParentMax));
		mountBoundsMin_m = new BoundingBox()
				.update(new Coordinate(xParent, yParent, 0))
				.update(new Coordinate(xParent + parent.getLength(), yParent + rParentMin));

		final BoundingBox combinedBounds = new BoundingBox().update(finBounds_m).update(mountBoundsMax_m);

		contentBounds_m = combinedBounds.toRectangle();
	}

	@Override
	protected void updateCanvasOrigin() {
		final int finBottomPy = (int)(finBounds_m.max.y*scale);
		final int mountHeight = (int)(mountBoundsMin_m.span().y*scale);
		// this is non-intuitive: it's an offset _from_ the origin(0,0) _to_ the lower-left of the content --
		// because the canvas is drawn from that lower-left corner of the content, and the fin-front
		// is fixed-- by definition-- to the origin.
		final int finFrontPx = -(int)(contentBounds_m.getX()*scale);  // pixels from left-border to fin-front
		final int contentHeight = (int)(contentBounds_m.getHeight()*scale);

		originLocation_px.x = borderThickness_px.width + finFrontPx;

		if( visibleBounds_px.height > (contentHeight + 2*borderThickness_px.height)){
			originLocation_px.y = getHeight() - mountHeight - borderThickness_px.height;
		}else {
			originLocation_px.y = borderThickness_px.height + finBottomPy;
		}
	}

	public void resetSelectedIndex() {
		this.selectedIndex = -1;
	}

	public void setSelectedIndex(final int newIndex) {
		this.selectedIndex = newIndex;
	}

	public void setHighlightIndex(final int newIndex) {
		this.highlightIndex = newIndex;
	}

}
