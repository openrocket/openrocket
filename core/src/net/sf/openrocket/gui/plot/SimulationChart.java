package net.sf.openrocket.gui.plot;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;

import com.jogamp.newt.event.InputEvent;

public class SimulationChart extends ChartPanel {

	private Point2D panLast;
	private double panW;
	private double panH;
	
	private MouseWheelHandler mouseWheelHandler = null;

	public SimulationChart(JFreeChart chart, boolean properties, boolean save,
			boolean print, boolean zoom, boolean tooltips) {
		super(chart, properties, save, print, zoom, tooltips);
	}

	public SimulationChart(JFreeChart chart, boolean useBuffer) {
		super(chart, useBuffer);
	}

	public SimulationChart(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean copy, boolean save, boolean print, boolean zoom,
			boolean tooltips) {
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties, copy, save,
				print, zoom, tooltips);
	}

	public SimulationChart(JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean save, boolean print, boolean zoom, boolean tooltips) {
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save,
				print, zoom, tooltips);
	}

	public SimulationChart(JFreeChart chart) {
		super(chart);
	}

	@Override
	public boolean isMouseWheelEnabled() {
		return mouseWheelHandler != null;
	}

	@Override
	public void setMouseWheelEnabled(boolean flag) {
		if ( flag && mouseWheelHandler == null ) {
			this.mouseWheelHandler = new MouseWheelHandler();
			this.addMouseWheelListener(this.mouseWheelHandler);
		} else if ( !flag && mouseWheelHandler != null ) {
			this.removeMouseWheelListener(this.mouseWheelHandler);
			this.mouseWheelHandler = null;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if ( e.getButton() == MouseEvent.BUTTON1 ) {
			// if no modifiers, use pan
			Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
			
			if ( screenDataArea != null && screenDataArea.contains(e.getPoint())) {
				this.panW  = screenDataArea.getWidth();
				this.panH = screenDataArea.getHeight();
				this.panLast = e.getPoint();
				setCursor( Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR));
			}
		} else if ( e.getButton() == MouseEvent.BUTTON2 ) {
			
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if ( panLast == null ) {
			return;
		}

		double dx = e.getX() - this.panLast.getX();
		double dy = e.getY() - this.panLast.getY();
		if ( dx == 0.0 && dy == 0.0 ) {
			return ;
		}
		double wPercent = -dx / this.panW;
		double hPercent = dy / this.panH;
		boolean old = this.getChart().getPlot().isNotify();
		this.getChart().getPlot().setNotify(false);
		Pannable p = (Pannable) this.getChart().getPlot();
		if ( p.getOrientation() == PlotOrientation.VERTICAL){
			p.panDomainAxes( wPercent, this.getChartRenderingInfo().getPlotInfo(),panLast);
			p.panRangeAxes( hPercent, this.getChartRenderingInfo().getPlotInfo(),panLast);
		} else {
			p.panDomainAxes( hPercent, this.getChartRenderingInfo().getPlotInfo(),panLast);
			p.panRangeAxes( wPercent, this.getChartRenderingInfo().getPlotInfo(),panLast);
		}
		
		this.panLast = e.getPoint();
		this.getChart().getPlot().setNotify(old);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if ( this.panLast != null ) {
			this.panLast = null;
			setCursor(Cursor.getDefaultCursor());
		}
	}

	
	/**
	 * 
	 * Hacked up copy of MouseWheelHandler from JFreechart.  This version
	 * has the special ability to only zoom on the domain if the alt key is pressed.
	 * 
	 * A class that handles mouse wheel events for the {@link ChartPanel} class.
	 * Mouse wheel event support was added in JDK 1.4, so this class will be omitted
	 * from JFreeChart if you build it using JDK 1.3.
	 *
	 * @since 1.0.13
	 */
	class MouseWheelHandler implements MouseWheelListener, Serializable {

	    /** The zoom factor. */
	    double zoomFactor;

	    /**
	     * Creates a new instance for the specified chart panel.
	     *
	     * @param chartPanel  the chart panel (<code>null</code> not permitted).
	     */
	    public MouseWheelHandler() {
	        this.zoomFactor = 0.10;
	    }

	    /**
	     * Returns the current zoom factor.  The default value is 0.10 (ten
	     * percent).
	     *
	     * @return The zoom factor.
	     *
	     * @see #setZoomFactor(double)
	     */
	    public double getZoomFactor() {
	        return this.zoomFactor;
	    }

	    /**
	     * Sets the zoom factor.
	     *
	     * @param zoomFactor  the zoom factor.
	     *
	     * @see #getZoomFactor()
	     */
	    public void setZoomFactor(double zoomFactor) {
	        this.zoomFactor = zoomFactor;
	    }

	    /**
	     * Handles a mouse wheel event from the underlying chart panel.
	     *
	     * @param e  the event.
	     */
	    public void mouseWheelMoved(MouseWheelEvent e) {
	        JFreeChart chart = SimulationChart.this.getChart();
	        if (chart == null) {
	            return;
	        }
	        Plot plot = chart.getPlot();
	        if (plot instanceof Zoomable) {
	        	boolean domainOnly = ( e.getModifiers() & InputEvent.ALT_MASK ) != 0;
	            Zoomable zoomable = (Zoomable) plot;
	            handleZoomable(zoomable, e, domainOnly);
	        }
	        else if (plot instanceof PiePlot) {
	            PiePlot pp = (PiePlot) plot;
	            pp.handleMouseWheelRotation(e.getWheelRotation());
	        }
	    }

	    /**
	     * Handle the case where a plot implements the {@link Zoomable} interface.
	     *
	     * @param zoomable  the zoomable plot.
	     * @param e  the mouse wheel event.
	     */
	    private void handleZoomable(Zoomable zoomable, MouseWheelEvent e, boolean domainOnly) {
	        // don't zoom unless the mouse pointer is in the plot's data area
	        ChartRenderingInfo info = SimulationChart.this.getChartRenderingInfo();
	        PlotRenderingInfo pinfo = info.getPlotInfo();
	        Point2D p = SimulationChart.this.translateScreenToJava2D(e.getPoint());
	        if (!pinfo.getDataArea().contains(p)) {
	            return;
	        }

	        Plot plot = (Plot) zoomable;
	        // do not notify while zooming each axis
	        boolean notifyState = plot.isNotify();
	        plot.setNotify(false);
	        int clicks = e.getWheelRotation();
	        double zf = 1.0 + this.zoomFactor;
	        if (clicks < 0) {
	            zf = 1.0 / zf;
	        }
	        if (SimulationChart.this.isDomainZoomable()) {
	            zoomable.zoomDomainAxes(zf, pinfo, p, true);
	        }
	        if (SimulationChart.this.isRangeZoomable() && !domainOnly ) {
	            zoomable.zoomRangeAxes(zf, pinfo, p, true);
	        }
	        plot.setNotify(notifyState);  // this generates the change event too
	    }

	}

}
