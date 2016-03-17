package net.sf.openrocket.gui.scalefigure;

import java.awt.Dimension;

import net.sf.openrocket.util.ChangeSource;


public interface ScaleFigure extends ChangeSource {
	
	/**
	 * Extra scaling applied to the figure.  The f***ing Java JRE doesn't know 
	 * how to draw shapes when using very large scaling factors, so this must 
	 * be manually applied to every single shape used.
	 * <p>
	 * The scaling factor used is divided by this value, and every coordinate used 
	 * in the figures must be multiplied by this factor.
	 */
	public static final double EXTRA_SCALE = 1000;

	public static final double INCHES_PER_METER = 39.3701;
	public static final double METERS_PER_INCH = 0.0254;

	public static final double MINIMUM_ZOOM=0.001; // ==  00.1%
	public static final double MAXIMUM_ZOOM=1000; // 100,000 %
	
	
	
	/**
	 * Set the scale level of the figure.  A scale value of 1.0 indicates an original
	 * size when using the current DPI level.
	 * 
	 * @param newZoom the zoom level.
	 */
	public void setZoom( final double newZoom );
	
	
	/**
	 * Set the zoom level so that the figure fits into the given bounds.
	 * 
	 * @param bounds the dimension 
	 */
	@Deprecated
	public void zoomToSize( final Dimension bounds );
	
	
	// even better would be to re-zoom AND re-center.
	public void zoomToBounds( final Dimension center, final Dimension bounds);
	
	/**
	 * Return the scale level of the figure.  A scale value of 1.0 indicates an original
	 * size when using the current DPI level.
	 * 
	 * @return   the current scale level.
	 */
	public double getZoom();
	
	
	/**
	 * Return the scale of the figure on px/m.
	 * 
	 * @return   the current scale value.
	 */
	public double getAbsoluteScale();
	
	
	/**
	 * Return the pixel coordinates of the figure origin.
	 * 
	 * @return	the pixel coordinates of the figure origin.
	 */
	public Dimension getOrigin();
	
	/**
	 * Get the amount of blank space left around the figure.
	 * 
	 * @return	the amount of horizontal and vertical space left on both sides of the figure.
	 */
	public Dimension getBorderPixels();
	
	/**
	 * Set the amount of blank space left around the figure.
	 * 
	 * @param width		the amount of horizontal space left on both sides of the figure.
	 * @param height	the amount of vertical space left on both sides of the figure.
	 */
	public void setBorderPixels( final int width, final int height);
}
