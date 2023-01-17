package net.sf.openrocket.gui.scalefigure;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.Map.Entry;

import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.gui.figureelements.FigureElement;
import net.sf.openrocket.gui.rocketfigure.RocketComponentShape;
import net.sf.openrocket.gui.util.ColorConversion;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.InstanceContext;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BoundingBox;
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
@SuppressWarnings("serial")
public class RocketFigure extends AbstractScaleFigure {

    private final static Logger log = LoggerFactory.getLogger(FinPointFigure.class);
	protected final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
	
	private static final String ROCKET_FIGURE_PACKAGE = "net.sf.openrocket.gui.rocketfigure";
	private static final String ROCKET_FIGURE_SUFFIX = "Shapes";

	public static final int VIEW_TOP = 0;
	public static final int VIEW_SIDE = 1;
	public static final int VIEW_BACK = 2;
	
	// Width for drawing normal and selected components
	public static final double NORMAL_WIDTH = 1.0;
	public static final double SELECTED_WIDTH = 2.0;
	

	final private Rocket rocket;
	
	private RocketComponent[] selection = new RocketComponent[0];
	
	private RocketPanel.VIEW_TYPE currentViewType = RocketPanel.VIEW_TYPE.SideView;
	
	private double rotation;
	private Transformation axialRotation;

	private boolean drawCarets = true;
    
	/**
	 * The shapes to be drawn are stored in this Priority Queue, where the first shape to be drawn is the one with
	 * the highest priority, namely being the one where the corresponding RocketComponent has the highest displayOrder
	 * (declared in RocketComponent, can be overridden in separate components).
	 */
	private final PriorityQueue<RocketComponentShape> figureShapes_side = new PriorityQueue<>(
			Comparator.comparingInt(o -> -o.component.getDisplayOrder_side()));
	private final PriorityQueue<RocketComponentShape> figureShapes_back = new PriorityQueue<>(
			Comparator.comparingInt(o -> -o.component.getDisplayOrder_back()));
	
	
	private final ArrayList<FigureElement> relativeExtra = new ArrayList<FigureElement>();
	private final ArrayList<FigureElement> absoluteExtra = new ArrayList<FigureElement>();
	
	
	/**
	 * Creates a new rocket figure.
	 */
	public RocketFigure(Rocket _rkt) {
		super();
		this.rocket = _rkt;
		
		this.rotation = 0.0;
		this.axialRotation = Transformation.rotate_x(0.0);

		updateFigure();
	}

	public Point getAutoZoomPoint(){
		return new Point( Math.max(0, originLocation_px.x - borderThickness_px.width),
						  Math.max(0, - borderThickness_px.height));
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
        fireChangeEvent();
	}
	
	
	public double getRotation() {
		return rotation;
	}
	
	public void setRotation(double rot) {
		if (MathUtil.equals(rotation, rot))
			return;
		this.rotation = rot;
		this.axialRotation = Transformation.rotate_x(rotation);
		updateFigure();
        fireChangeEvent();
	}

	private Transformation getFigureRotation() {
		if (currentViewType == RocketPanel.VIEW_TYPE.TopView) {
			return this.axialRotation.applyTransformation(Transformation.rotate_x(-Math.PI / 2));
		} else {
			return this.axialRotation;
		}
	}
	
	
	public RocketPanel.VIEW_TYPE getCurrentViewType() {
		return currentViewType;
	}
	
	public void setType(final RocketPanel.VIEW_TYPE type) {
		if (type != RocketPanel.VIEW_TYPE.BackView && type != RocketPanel.VIEW_TYPE.SideView && type != RocketPanel.VIEW_TYPE.TopView) {
			throw new IllegalArgumentException("Illegal type: " + type);
		}
		if (this.currentViewType == type)
			return;
		this.currentViewType = type;
		updateFigure();
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

		PriorityQueue<RocketComponentShape> figureShapes;
		if (currentViewType == RocketPanel.VIEW_TYPE.SideView || currentViewType == RocketPanel.VIEW_TYPE.TopView)
			figureShapes = figureShapes_side;
		else if (currentViewType == RocketPanel.VIEW_TYPE.BackView)
			figureShapes = figureShapes_back;
		else {
			log.warn("Unknown view type for paintComponent");
			return;
		}
		
		updateSubjectDimensions();
		updateCanvasOrigin();
        updateCanvasSize();
        updateTransform();
        
        updateShapes(figureShapes);

		g2.transform(projection);
		
		// Set rendering hints appropriately
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// Draw all shapes
		PriorityQueue<RocketComponentShape> figureShapesCopy = new PriorityQueue<>(figureShapes);
		while (!figureShapesCopy.isEmpty()) {
			RocketComponentShape rcs = figureShapesCopy.poll();
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
				dashes[j] *= 1.0 / scale;
			}
			
			if (selected) {
				g2.setStroke(new BasicStroke((float) (SELECTED_WIDTH / scale),
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashes, 0));
				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_PURE);
			} else {
				g2.setStroke(new BasicStroke((float) (NORMAL_WIDTH / scale),
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, dashes, 0));
				g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
						RenderingHints.VALUE_STROKE_NORMALIZE);
			}
			g2.draw(rcs.shape);
		}
		
		g2.setStroke(new BasicStroke((float) (NORMAL_WIDTH / scale),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);
	
		// Draw motors
		Color fillColor = ((SwingPreferences)Application.getPreferences()).getMotorFillColor();
		Color borderColor = ((SwingPreferences)Application.getPreferences()).getMotorBorderColor();

		FlightConfiguration config = rocket.getSelectedConfiguration();
		for (MotorConfiguration curInstance : config.getActiveMotors()) {
			MotorMount mount = curInstance.getMount();
			Motor motor = curInstance.getMotor();
			double motorLength = motor.getLength();
			double motorRadius = motor.getDiameter() / 2;
			RocketComponent mountComponent = ((RocketComponent) mount);

			// <component>.getLocation() will return all the parent instances of this owning component,  AND all of it's own instances as well.
			// so, just draw a motor once for each Coordinate returned... 
			Coordinate[] mountLocations = mount.getLocations();

			double mountLength = mountComponent.getLength();
//			System.err.println("Drawing Motor: "+motor.getDesignation()+" (x"+mountLocations.length+")");
			for (Coordinate curMountLocation : mountLocations) {
				Coordinate curMotorLocation = curMountLocation.add(mountLength - motorLength + mount.getMotorOverhang(), 0, 0);
//		        System.err.println(String.format("        mount instance:   %s  =>  %s", curMountLocation.toString(), curMotorLocation.toString() )); 

				// rotate by figure's axial rotation:
				curMotorLocation = getFigureRotation().transform(curMotorLocation);

				{
					Shape s;
					if (currentViewType == RocketPanel.VIEW_TYPE.SideView || currentViewType == RocketPanel.VIEW_TYPE.TopView) {
						s = new Rectangle2D.Double(curMotorLocation.x,
								(curMotorLocation.y - motorRadius),
								motorLength,
								2 * motorRadius);
					} else {
						s = new Ellipse2D.Double((curMotorLocation.z - motorRadius),
								(curMotorLocation.y - motorRadius),
								2 * motorRadius,
								2 * motorRadius);
					}
					g2.setColor(fillColor);
					g2.fill(s);
					g2.setColor(borderColor);
					g2.draw(s);
				}
			}
		}
		

		// Draw relative extras
		if (drawCarets) {
			for (FigureElement e : relativeExtra) {
				e.paint(g2, scale);
			}
		}

		
		// Draw absolute extras
		g2.setTransform(baseTransform);
		Rectangle rect = this.getVisibleRect();
		
		for (FigureElement e : absoluteExtra) {
			e.paint(g2, 1.0, rect);
		}
		
	}
	
	public RocketComponent[] getComponentsByPoint(double x, double y) {
		// Calculate point in shapes' coordinates
		Point2D.Double p = new Point2D.Double(x, y);
		try {
			projection.inverseTransform(p, p);
		} catch (NoninvertibleTransformException e) {
			return new RocketComponent[0];
		}
		
		LinkedHashSet<RocketComponent> l = new LinkedHashSet<RocketComponent>();

		PriorityQueue<RocketComponentShape> figureShapes;
		if (currentViewType == RocketPanel.VIEW_TYPE.SideView || currentViewType == RocketPanel.VIEW_TYPE.TopView)
			figureShapes = figureShapes_side;
		else if (currentViewType == RocketPanel.VIEW_TYPE.BackView)
			figureShapes = figureShapes_back;
		else {
			log.warn("Unknown view type for getComponentsByPoint");
			return null;
		}

		PriorityQueue<RocketComponentShape> figureShapesCopy = new PriorityQueue<>(figureShapes);
		while (!figureShapesCopy.isEmpty()) {
			RocketComponentShape rcs = figureShapesCopy.poll();
			if (rcs.shape.contains(p))
				l.add(rcs.component);
		}
		return l.toArray(new RocketComponent[0]);
	}
	
	private void updateShapes(PriorityQueue<RocketComponentShape> allShapes) {
		// source input
		final FlightConfiguration config = rocket.getSelectedConfiguration();

		// allShapes is an output buffer -- it stores all the generated shapes
		allShapes.clear();

		addShapesFromInstanceEntries(allShapes, config.getActiveInstances().entrySet());
		addShapesFromInstanceEntries(allShapes, config.getExtraRenderInstances().entrySet());
	}

	private void addShapesFromInstanceEntries(PriorityQueue<RocketComponentShape> allShapes, Set<Entry<RocketComponent, ArrayList<InstanceContext>>> entries) {
		for (Entry<RocketComponent, ArrayList<InstanceContext>> entry : entries) {
			final RocketComponent comp = entry.getKey();

			// Only draw pod sets and boosters when they are selected
			if (preferences.isShowMarkers() && (comp instanceof PodSet || comp instanceof ParallelStage)) {
				boolean selected = false;

				// Check if component is in the selection
				for (RocketComponent component : selection) {
					if (comp == component) {
						selected = true;
						break;
					}
				}
				if (!selected) continue;
			}

			final ArrayList<InstanceContext> contextList = entry.getValue();

			for (InstanceContext context : contextList) {
				final Transformation currentTransform = getFigureRotation().applyTransformation(context.transform);
				allShapes = addThisShape(allShapes, this.currentViewType, comp, currentTransform);
			}
		}
	}

	/**
	 * Gets the shapes required to draw the component.
	 *
	 * @param allShapes output buffer for the shapes to add to
	 * @param viewType the view type to draw the component in
	 * @param component component to draw and add to <allShapes>
	 * @param transformation transformation to apply to the component before drawing it
	 * @param color color to draw the component in
	 *
	 * @return the <code>ArrayList</code> containing all the shapes to draw.
	 */
	private static PriorityQueue<RocketComponentShape> addThisShape(
			PriorityQueue<RocketComponentShape> allShapes,  // this is the output parameter
			final RocketPanel.VIEW_TYPE viewType, 
			final RocketComponent component, 
			final Transformation transformation,
			final net.sf.openrocket.util.Color color) {
		Reflection.Method m;
		
		if ((component instanceof Rocket) || (component instanceof AxialStage && !(component instanceof ParallelStage))){
			// no-op; no shapes here
			return allShapes;
		}
		
		// Find the appropriate method
		switch (viewType) {
		case SideView:
		case TopView:
			m = Reflection.findMethod(ROCKET_FIGURE_PACKAGE, component, ROCKET_FIGURE_SUFFIX, "getShapesSide",
					RocketComponent.class, Transformation.class);
				break;
		
		case BackView:
			m = Reflection.findMethod(ROCKET_FIGURE_PACKAGE, component, ROCKET_FIGURE_SUFFIX, "getShapesBack",
					RocketComponent.class, Transformation.class);
			break;
		
		default:
			throw new BugException("Unknown figure type = " + viewType);
		}
		
		if (m == null) {
			Application.getExceptionHandler().handleErrorCondition("ERROR: Rocket figure paint method not found for "
					+ component);
			return allShapes;
		}
		
	
		RocketComponentShape[] returnValue =  (RocketComponentShape[]) m.invokeStatic(component, transformation);

		if (color != null) {
			for (RocketComponentShape rcs : returnValue) {
				if (rcs.getColor() == net.sf.openrocket.util.Color.INVISIBLE) continue;	// don't change the color of invisible (often selection) components
				rcs.setColor(color);
			}
		}

		allShapes.addAll(Arrays.asList(returnValue));
		return allShapes;
	}

	/**
	 * Gets the shapes required to draw the component.
	 *
	 * @param allShapes output buffer for the shapes to add to
	 * @param viewType the view type to draw the component in
	 * @param component component to draw and add to <allShapes>
	 * @param transformation transformation to apply to the component before drawing it
	 *
	 * @return the <code>ArrayList</code> containing all the shapes to draw.
	 */
	private static PriorityQueue<RocketComponentShape> addThisShape(
			PriorityQueue<RocketComponentShape> allShapes,  // this is the output parameter
			final RocketPanel.VIEW_TYPE viewType,
			final RocketComponent component,
			final Transformation transformation) {
		return addThisShape(allShapes, viewType, component, transformation, null);
	}
	

	/**
	 * Gets the bounds of the drawn subject in Model-Space
	 *
	 *  i.e. the maximum extents in the selected dimensions.
	 * The bounds are stored in the variables minX, maxX and maxR.
	 *
	 * @return
	 */
	@Override
	protected void updateSubjectDimensions() {
		// calculate bounds, and store in class variables
		
		final BoundingBox bounds = rocket.getSelectedConfiguration().getBoundingBox();
		
		final double maxR = Math.max( Math.hypot(bounds.min.y, bounds.min.z),
									  Math.hypot(bounds.max.y, bounds.max.z));

		switch (currentViewType) {
			case SideView:
			case TopView:
				subjectBounds_m = new Rectangle2D.Double(bounds.min.x, -maxR, bounds.span().x, 2 * maxR);
				break;
			case BackView:
				subjectBounds_m = new Rectangle2D.Double(-maxR, -maxR, 2 * maxR, 2 * maxR);
				break;
			default:
				throw new BugException("Illegal figure type = " + currentViewType);
		}
		// for a rocket, these are the same
		contentBounds_m = subjectBounds_m;
	}

	/**
	 * Calculates the necessary size of the figure and set the PreferredSize 
	 * property accordingly.
	 */
	@Override
	protected void updateCanvasOrigin() {
		final int subjectWidth = (int)(subjectBounds_m.getWidth()*scale);
		final int subjectHeight = (int)(subjectBounds_m.getHeight()*scale);
		final int mid_x = (Math.max(getWidth(), subjectWidth) / 2);
		
		if (currentViewType == RocketPanel.VIEW_TYPE.BackView){
			final int newOriginX = mid_x;
			final int newOriginY = borderThickness_px.height + getHeight() / 2;
			originLocation_px = new Point(newOriginX, newOriginY);
		} else if (currentViewType == RocketPanel.VIEW_TYPE.SideView || currentViewType == RocketPanel.VIEW_TYPE.TopView) {
			final int newOriginX = mid_x - (subjectWidth / 2) - (int)(subjectBounds_m.getMinX() * scale);
			final int newOriginY = Math.max(getHeight(), subjectHeight + 2*borderThickness_px.height )/ 2;
			originLocation_px = new Point(newOriginX, newOriginY);
		}
	}

	public boolean isDrawCarets() {
		return drawCarets;
	}

	public void setDrawCarets(boolean drawCarets) {
		this.drawCarets = drawCarets;
	}

}
