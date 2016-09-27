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
import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.gui.figureelements.FigureElement;
import net.sf.openrocket.gui.rocketfigure.RocketComponentShape;
import net.sf.openrocket.gui.util.ColorConversion;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.simulation.BasicEventSimulationEngine;
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
	private static final long serialVersionUID = 45884403769043138L;

	private static final Logger log = LoggerFactory.getLogger(BasicEventSimulationEngine.class);
	
	private static final String ROCKET_FIGURE_PACKAGE = "net.sf.openrocket.gui.rocketfigure";
	private static final String ROCKET_FIGURE_SUFFIX = "Shapes";
	
	public static final int VIEW_SIDE=0;
	public static final int VIEW_BACK=1;
	
	// Width for drawing normal and selected components
	public static final double NORMAL_WIDTH = 1.0;
	public static final double SELECTED_WIDTH = 2.0;
	

	private Rocket rocket;
	
	private RocketComponent[] selection = new RocketComponent[0];
	private double figureWidth = 0, figureHeight = 0;
	protected int figureWidthPx = 0, figureHeightPx = 0;
	
	private RocketPanel.VIEW_TYPE currentViewType = RocketPanel.VIEW_TYPE.SideView;
	
	private double rotation;
	private Transformation transformation;
	
	private double translateX, translateY;
	


	/*
	 * figureComponents contains the corresponding RocketComponents of the figureShapes
	 */
	private final ArrayList<RocketComponentShape> figureShapes = new ArrayList<RocketComponentShape>();

	
	private double minX = 0, maxX = 0, maxR = 0;
	// Figure width and height in SI-units and pixels
	
	private AffineTransform g2transformation = null;
	
	private final ArrayList<FigureElement> relativeExtra = new ArrayList<FigureElement>();
	private final ArrayList<FigureElement> absoluteExtra = new ArrayList<FigureElement>();
	
	
	/**
	 * Creates a new rocket figure.
	 */
	public RocketFigure(Rocket _rkt) {
		super();
		this.rocket = _rkt;
		
		this.rotation = 0.0;
		this.transformation = Transformation.rotate_x(0.0);
		
		updateFigure();
	}
	
	public FlightConfiguration getConfiguration() {
		return this.rocket.getSelectedConfiguration();
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
	
	
	public RocketPanel.VIEW_TYPE  getType() {
		return currentViewType;
	}
	
	public void setType(final RocketPanel.VIEW_TYPE type) {
		if (type != RocketPanel.VIEW_TYPE.BackView && type != RocketPanel.VIEW_TYPE.SideView) {
			throw new IllegalArgumentException("Illegal type: " + type);
		}
		if (this.currentViewType == type)
			return;
		this.currentViewType = type;
		updateFigure();
	}
		
	
	/**
	 * Updates the figure shapes and figure size.
	 */
	@Override
	public void updateFigure() {
		figureShapes.clear();
		
		calculateSize();
		FlightConfiguration config = rocket.getSelectedConfiguration();
		getShapes( figureShapes, config);
		
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
			if (currentViewType == RocketPanel.VIEW_TYPE.BackView){
				tx = getWidth() / 2;
			}else{
				tx = (getWidth() - figureWidthPx) / 2 - minX * scale;
			}
		} else {
			
			// Figure does not fit in viewport
			if (currentViewType == RocketPanel.VIEW_TYPE.BackView){
				tx = borderPixelsWidth + figureWidthPx / 2;
			}else{
				tx = borderPixelsWidth - minX * scale;
			}
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
		
		int shapeCount = figureShapes.size();
		// Draw all shapes
		for (int i = 0; i < shapeCount; i++) {
			RocketComponentShape rcs = figureShapes.get(i);
			RocketComponent c = rcs.getComponent(); 
			boolean selected = false;
			
			// Check if component is in the selection
			for (int j = 0; j < selection.length; j++) {
				if (c == selection[j]) {
					selected = true;
					break;
				}
			}
			
			// Set component color and line style
			net.sf.openrocket.util.Color color = rcs.color;
			if (color == null) {
				color = Application.getPreferences().getDefaultColor(c.getClass());
			}
			g2.setColor(ColorConversion.toAwtColor(color));
			
			LineStyle style = rcs.lineStyle;
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
			g2.draw(rcs.shape);
		}
		
		g2.setStroke(new BasicStroke((float) (NORMAL_WIDTH * EXTRA_SCALE / scale),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
	
		// Draw motors
		Color fillColor = ((SwingPreferences)Application.getPreferences()).getMotorFillColor();
		Color borderColor = ((SwingPreferences)Application.getPreferences()).getMotorBorderColor();

		FlightConfiguration config = rocket.getSelectedConfiguration();
		for( MotorConfiguration curInstance : config.getActiveMotors()){
			MotorMount mount = curInstance.getMount();
			Motor motor = curInstance.getMotor();
			double motorLength = motor.getLength();
			double motorRadius = motor.getDiameter() / 2;
			RocketComponent mountComponent = ((RocketComponent) mount);
			
			// <component>.getLocation() will return all the parent instances of this owning component,  AND all of it's own instances as well.
			// so, just draw a motor once for each Coordinate returned... 
			Coordinate[] mountLocations = mountComponent.getLocations();
			
			double mountLength = mountComponent.getLength();
			//System.err.println("Drawing motor from here. Motor: "+motor.getDesignation()+" of length: "+motor.getLength());
			for ( Coordinate curMountLocation : mountLocations ){
				Coordinate curMotorLocation = curMountLocation.add( mountLength - motorLength + mount.getMotorOverhang(), 0, 0);
				
				Coordinate coord = curMotorLocation;
				{
					Shape s;
					if (currentViewType == RocketPanel.VIEW_TYPE.SideView) {
						s = new Rectangle2D.Double(EXTRA_SCALE * coord.x,
								EXTRA_SCALE * (coord.y - motorRadius), EXTRA_SCALE * motorLength,
								EXTRA_SCALE * 2 * motorRadius);
					} else {
						s = new Ellipse2D.Double(EXTRA_SCALE * (coord.z - motorRadius),
								EXTRA_SCALE * (coord.y - motorRadius), EXTRA_SCALE * 2 * motorRadius,
								EXTRA_SCALE * 2 * motorRadius);
					}
					g2.setColor(fillColor);
					g2.fill(s);
					g2.setColor(borderColor);
					g2.draw(s);
				}
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
			RocketComponentShape rcs = this.figureShapes.get(i);
			if (rcs.shape.contains(p))
				l.add(rcs.component);
		}
		return l.toArray(new RocketComponent[0]);
	}
	
	// facade for the recursive function below
	private void getShapes(ArrayList<RocketComponentShape> allShapes, FlightConfiguration configuration){
		for( AxialStage stage : configuration.getActiveStages()){
			getShapeTree( allShapes, stage, Coordinate.ZERO);
		}
	}
	
    // NOTE:  Recursive function
    private void getShapeTree(
    		ArrayList<RocketComponentShape> allShapes,  // output parameter 
    		final RocketComponent comp, 
    		final Coordinate parentLocation){
   
    	RocketPanel.VIEW_TYPE viewType = this.currentViewType; 
    	Transformation viewTransform = this.transformation;
    	Coordinate[] locs = comp.getLocations();
    	
        // generate shapes
    	for( Coordinate curLocation : locs){
    		allShapes = addThisShape( allShapes, viewType, comp, curLocation, viewTransform);
    	}

		// recurse into component's children
    	for( RocketComponent child: comp.getChildren() ){
    		if( child instanceof AxialStage ){
    			// recursing into BoosterSet here would double count its tree
    			continue;
    		}
    		
    		for( Coordinate curLocation : locs){
    			getShapeTree( allShapes, child, curLocation);
    		}
    	}
        
	    return;
	}

	/**
	 * Gets the shapes required to draw the component.
	 * 
	 * @param component
	 * @param params
	 * @return the <code>ArrayList</code> containing all the shapes to draw.
	 */
	private static ArrayList<RocketComponentShape> addThisShape(
			ArrayList<RocketComponentShape> allShapes,  // this is the output parameter
			final RocketPanel.VIEW_TYPE viewType, 
			final RocketComponent component, 
			final Coordinate instanceOffset, 
			final Transformation transformation) {
		Reflection.Method m;
		
		if(( component instanceof Rocket)||( component instanceof ComponentAssembly )){
			// no-op; no shapes here, either.
			return allShapes;
		}
		
		// Find the appropriate method
		switch (viewType) {
		case SideView:
			m = Reflection.findMethod(ROCKET_FIGURE_PACKAGE, component, ROCKET_FIGURE_SUFFIX, "getShapesSide",
					RocketComponent.class, Transformation.class, Coordinate.class);
			break;
		
		case BackView:
			m = Reflection.findMethod(ROCKET_FIGURE_PACKAGE, component, ROCKET_FIGURE_SUFFIX, "getShapesBack",
					RocketComponent.class, Transformation.class, Coordinate.class);
			break;
		
		default:
			throw new BugException("Unknown figure type = " + viewType);
		}
		
		if (m == null) {
			Application.getExceptionHandler().handleErrorCondition("ERROR: Rocket figure paint method not found for "
					+ component);
			return allShapes;
		}
		
	
		RocketComponentShape[] returnValue =  (RocketComponentShape[]) m.invokeStatic(component, transformation, instanceOffset);
		for ( RocketComponentShape curShape : returnValue ){
			allShapes.add( curShape );
		}
		return allShapes;
	}
	
	

	/**
	 * Gets the bounds of the figure, i.e. the maximum extents in the selected dimensions.
	 * The bounds are stored in the variables minX, maxX and maxR.
	 */
	private void calculateFigureBounds() {
		Collection<Coordinate> bounds = rocket.getSelectedConfiguration().getBounds();
		
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
	
//	public double getBestZoom(Rectangle2D bounds) {
//		double zh = 1, zv = 1;
//		if (bounds.getWidth() > 0.0001)
//			zh = (getWidth() - 2 * borderPixelsWidth) / bounds.getWidth();
//		if (bounds.getHeight() > 0.0001)
//			zv = (getHeight() - 2 * borderPixelsHeight) / bounds.getHeight();
//		return Math.min(zh, zv);
//	}
//	
	

	/**
	 * Calculates the necessary size of the figure and set the PreferredSize 
	 * property accordingly.
	 */
	private void calculateSize() {
		Rectangle2D dimensions = this.getDimensions();
		
		figureHeight = dimensions.getHeight(); 
		figureWidth = dimensions.getWidth();
			
		figureWidthPx = (int) (figureWidth * scale);
		figureHeightPx = (int) (figureHeight * scale);
		
		Dimension dpx = new Dimension(
				figureWidthPx + 2 * borderPixelsWidth,
				figureHeightPx + 2 * borderPixelsHeight);
		
		if (!dpx.equals(getPreferredSize()) || !dpx.equals(getMinimumSize())) {
			setPreferredSize(dpx);
			setMinimumSize(dpx);
			revalidate();
		}
	}
	
	public Rectangle2D getDimensions() {
		calculateFigureBounds();
		
		switch (currentViewType) {
		case SideView:
			return new Rectangle2D.Double(minX, -maxR, maxX - minX, 2 * maxR);
			
		case BackView:
			return new Rectangle2D.Double(-maxR, -maxR, 2 * maxR, 2 * maxR);
			
		default:
			throw new BugException("Illegal figure type = " + currentViewType);
		}
	}
	
}
