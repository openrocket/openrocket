package net.sf.openrocket.gui.scalefigure;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.Point;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;

@SuppressWarnings("serial")
public abstract class AbstractScaleFigure extends JPanel {
    
    private final static Logger log = LoggerFactory.getLogger(AbstractScaleFigure.class);
    
    public static final double INCHES_PER_METER = 39.3701;
    public static final double METERS_PER_INCH = 0.0254;
    
    public static final double MINIMUM_ZOOM =    0.01; // ==      1 %
    public static final double MAXIMUM_ZOOM = 1000.00; // == 10,000 %
    
	// Number of pixels to leave at edges when fitting figure
	private static final int DEFAULT_BORDER_PIXELS_WIDTH = 30;
	private static final int DEFAULT_BORDER_PIXELS_HEIGHT = 20;
	protected static final Dimension borderThickness_px = new Dimension(DEFAULT_BORDER_PIXELS_WIDTH, DEFAULT_BORDER_PIXELS_HEIGHT);

	// constant factor that scales screen real-estate to rocket-space
	protected final double baseScale;
	protected double userScale = 1.0;
	protected double scale = -1;

	// pixel offset from the the subject's origin to the canvas's upper-left-corner.
	protected Point originLocation_px = new Point(0,0);
	
	// size of the visible region
	protected Dimension visibleBounds_px = new Dimension(0,0);
	
	// ======= whatever this figure is drawing, in real-space coordinates:  meters
	//     all drawable content
	protected Rectangle2D contentBounds_m = new Rectangle2D.Double(0,0,0,0);
	//     the content we should focus on (this is the auto-zoom subject)
	protected Rectangle2D subjectBounds_m = new Rectangle2D.Double(0,0,0,0);

	// combines the translation and scale in one place:
	// which frames does this transform between ?
	protected AffineTransform projection = null;

	protected final List<EventListener> listeners = new LinkedList<EventListener>();
	
	
	public AbstractScaleFigure() {
		// produces a pixels-per-meter scale factor
		//
		// dots     dots     inch
		// ----  = ------ * -----
		// meter    inch    meter
		//
		this.baseScale = GUIUtil.getDPI() * INCHES_PER_METER;
		this.userScale = 1.0;
		this.scale = baseScale * userScale;

		this.setPreferredSize(new Dimension(100,100));
		setSize(100,100);

		setBackground(Color.WHITE);
		setOpaque(true);
	}
	
	public double getUserScale(){
		return userScale;
	}
	
	public double getAbsoluteScale() {
		return scale;
	}

	public Point getSubjectOrigin() {
		return originLocation_px;
	}

	/**
	 * Calculate a point for auto-zooming from a scale-to-fit request.
	 *
	 * The return point is intended for a $ScaleScrollPane call to "viewport.scrollRectToVisible(...)"
	 *
	 * @return the offset, in pixels, from the (top left) corner of the figure's canvas
	 */
	public abstract Point getAutoZoomPoint();

	/**
	 * Set the scale level of the figure.  A scale value of 1.0 is equivalent to 100 % scale.
	 * Smaller scales display the subject smaller.
	 *
	 *  If the figure would be smaller than the 'visibleBounds', then the figure is grown to match,
	 *  and the figures internal contents are centered according to the figure's origin.
	 *
	 * @param newScaleRequest the scale level
	 * @param visibleBounds the visible bounds upon the Figure
	 */
	public void scaleTo(final double newScaleRequest, final Dimension visibleBounds) {
		if (MathUtil.equals(this.userScale, newScaleRequest, 0.01) &&
			(visibleBounds_px.width == visibleBounds.width) &&
			(visibleBounds_px.height == visibleBounds.height) )
		{
			return;}
		if (Double.isInfinite(newScaleRequest) || Double.isNaN(newScaleRequest) || 0 > newScaleRequest) {
			return;}

		this.userScale = MathUtil.clamp( newScaleRequest, MINIMUM_ZOOM, MAXIMUM_ZOOM);
		this.scale = baseScale * userScale;

		this.visibleBounds_px = visibleBounds;

		this.fireChangeEvent();
	}
	
	/**
     * Set the scale level to display newBounds
     * 
     * @param visibleBounds the visible bounds to scale this figure to.  
     */
	public void scaleTo(Dimension visibleBounds) {
		if( 0 >= visibleBounds.getWidth() || 0 >= visibleBounds.getHeight())
			return;

		updateSubjectDimensions();
		updateCanvasSize();
		updateCanvasOrigin();

		final double width_scale = (visibleBounds.width - 2 * borderThickness_px.width) / (subjectBounds_m.getWidth() * baseScale);
		final double height_scale = (visibleBounds.height - 2 * borderThickness_px.height) / (subjectBounds_m.getHeight() * baseScale);
		final double newScale = Math.min(height_scale, width_scale);

		scaleTo(newScale, visibleBounds);
	}
	
    /**
     * Return the pixel coordinates of the subject's origin.
     * 
     * @return      the pixel coordinates of the figure origin.
     */
    protected abstract void updateSubjectDimensions();
		
    protected abstract void updateCanvasOrigin();
    
    /**
     *  update preferred figure Size
     
     */
    protected void updateCanvasSize() {
         final int desiredWidth = Math.max((int)this.visibleBounds_px.getWidth(),
                                          (int)(contentBounds_m.getWidth()*scale) + 2*borderThickness_px.width);
        final int desiredHeight = Math.max((int)this.visibleBounds_px.getHeight(),
                                          (int)(contentBounds_m.getHeight()*scale) + 2*borderThickness_px.height);

        Dimension preferredFigureSize_px = new Dimension(desiredWidth, desiredHeight);

		if( (getWidth() != preferredFigureSize_px.width) || (getHeight() != preferredFigureSize_px.height)) {
			setPreferredSize(preferredFigureSize_px);
			setMinimumSize(preferredFigureSize_px);
		}
	}

    protected void updateTransform(){
        // Calculate and store the transformation used
        // (inverse is used in detecting clicks on objects)
        projection = new AffineTransform();
        projection.translate(this.originLocation_px.x, originLocation_px.y);
        // Mirror position Y-axis upwards
        projection.scale(scale, -scale);
    }
    
    /**
     * Updates the figure shapes and figure size.
     */
    public void updateFigure() {
        log.trace(String.format("____ Updating %s to: %g user scale, %g overall scale", this.getClass().getSimpleName(), this.userScale, this.scale));
        
        updateSubjectDimensions();
        updateCanvasSize();
        updateCanvasOrigin();
        updateTransform();
        
        revalidate();
        repaint();
    }

	public void addChangeListener(StateChangeListener listener) {
		listeners.add(0, listener);
	}
	
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
	protected void fireChangeEvent() {
	    final EventObject changeEvent = new EventObject(this);
	    
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(changeEvent);
			}
		}
	}

}
