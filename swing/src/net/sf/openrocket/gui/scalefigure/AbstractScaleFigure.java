package net.sf.openrocket.gui.scalefigure;

import java.awt.Color;
import java.awt.Dimension;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.StateChangeListener;


@SuppressWarnings("serial")
public abstract class AbstractScaleFigure extends JPanel implements ScaleFigure /*, Scrollable */ {
	
	// Number of pixels to leave at edges when fitting figure
	private static final int DEFAULT_BORDER_PIXELS_WIDTH = 30;
	private static final int DEFAULT_BORDER_PIXELS_HEIGHT = 20;
	
	
	protected final double base_scale;
	
	protected double scale = 1.0;
	protected double zoom = 1.0;
	
	protected int borderPixelsWidth = DEFAULT_BORDER_PIXELS_WIDTH;
	protected int borderPixelsHeight = DEFAULT_BORDER_PIXELS_HEIGHT;
	
	protected final List<EventListener> listeners = new LinkedList<EventListener>();
	
	
	public AbstractScaleFigure() {
		// this result in a dots-per-meter scale factor 
		this.base_scale = GUIUtil.getDPI() * INCHES_PER_METER;
		this.zoom = 1.0;
		this.scale = base_scale * zoom;
		
		setBackground(Color.WHITE);
		setOpaque(true);
	}
	
	
	
	public abstract void updateFigure();
	
	public abstract double getFigureWidth();
	
	public abstract double getFigureHeight();
	
	@Override
	public double getZoom() {
		return zoom;
	}
	
	@Override
	public double getAbsoluteScale() {
		return scale;
	}
	
	@Override
	public void setZoom(double zoom) {
		if (Double.isInfinite(zoom) || Double.isNaN(zoom)){
			return;}
		
		zoom = MathUtil.clamp( zoom, MINIMUM_ZOOM, MAXIMUM_ZOOM);
		
		if (Math.abs(this.zoom - zoom) < 0.01){
			return;}
		
		this.zoom = zoom;
		this.scale = base_scale * zoom;
		updateFigure();
	}
	
	@Override 
	public void zoomToSize( Dimension bounds ){
		double zh = 1, zv = 1;
		int w = bounds.width - 2 * borderPixelsWidth - 20;
		int h = bounds.height - 2 * borderPixelsHeight - 20;
		
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
		return new Dimension(borderPixelsWidth, borderPixelsHeight);
	}
	
	@Override
	public void setBorderPixels(int width, int height) {
		this.borderPixelsWidth = width;
		this.borderPixelsHeight = height;
	}
	
	
	@Override
	public void addChangeListener(StateChangeListener listener) {
		listeners.add(0, listener);
	}
	
	@Override
	public void removeChangeListener(StateChangeListener listener) {
		listeners.remove(listener);
	}
	
	private EventObject changeEvent = null;
	
	protected void fireChangeEvent() {
		if (changeEvent == null)
			changeEvent = new EventObject(this);
		// Copy the list before iterating to prevent concurrent modification exceptions.
		EventListener[] list = listeners.toArray(new EventListener[0]);
		for (EventListener l : list) {
			if (l instanceof StateChangeListener) {
				((StateChangeListener) l).stateChanged(changeEvent);
			}
		}
	}
	

	
	// ======  ====== 'Scrollable' interface methods ====== ====== 

	
//	// this is anti-climactic.  is it useful? does it drive any behavior we couldn't get before? 
//	@Override
//	public Dimension getPreferredScrollableViewportSize() {
//		return getPreferredSize();
//	}
//
//	@Override
//	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
//		return 100;
//	}
//
//
//	@Override
//	public boolean getScrollableTracksViewportHeight() {
//		return false;
//	}
//
//
//	@Override
//	public boolean getScrollableTracksViewportWidth() {
//		return false;
//	}
//
//
//	@Override
//	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
//		return 10;
//	}
	

}
