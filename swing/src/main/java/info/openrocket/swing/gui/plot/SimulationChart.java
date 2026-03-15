package info.openrocket.swing.gui.plot;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.io.Serializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import com.jogamp.newt.event.InputEvent;

/**
 * Custom implementation of JFreeChart's ChartPanel which changes the mouse handling.
 * 
 * Changed mouse drag (left click + move) to pan the image.
 * 
 * Changed mouse wheel handling.  wheel zooms.  Alt+wheel zooms only domain axis.
 * 
 * @author kruland
 *
 */
public class SimulationChart extends ChartPanel {
	
	private Point2D panLast;
	private Point startPoint;
	private double panW;
	private double panH;
	
	private enum Interaction {
		ZOOM
	};
	
	private Interaction interaction = null;
	
	private MouseWheelHandler mouseWheelHandler = null;

    private String hoveredLegendLabel = null;

    private final Set<String> selectedLegendLabels = new HashSet<>();

    private final Map<String, Stroke> baseStrokes = new HashMap<>();
	
	public SimulationChart(JFreeChart chart) {
		super(chart,
				/* properties */false,
				/* save */true,
				/* print */false,
				/* zoom */true,
				/* tooltips */true);
		this.setMouseWheelEnabled(true);
		this.setEnforceFileExtensions(true);
		this.setInitialDelay(50);
		this.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
	}
	
	@Override
	public boolean isMouseWheelEnabled() {
		return mouseWheelHandler != null;
	}
	
	@Override
	public void setMouseWheelEnabled(boolean flag) {
		if (flag && mouseWheelHandler == null) {
			this.mouseWheelHandler = new MouseWheelHandler();
			this.addMouseWheelListener(this.mouseWheelHandler);
		} else if (!flag && mouseWheelHandler != null) {
			this.removeMouseWheelListener(this.mouseWheelHandler);
			this.mouseWheelHandler = null;
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			
			Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
			
			if (screenDataArea != null && screenDataArea.contains(e.getPoint())) {
				this.panW = screenDataArea.getWidth();
				this.panH = screenDataArea.getHeight();
				this.panLast = e.getPoint();
				this.startPoint = e.getPoint();
			}
			interaction = Interaction.ZOOM;
			setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
			
		}
		else {
			interaction = null;
			super.mousePressed(e);
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (interaction == null) {
			super.mouseDragged(e);
			return;
		}
		
		if (panLast == null) {
			return;
		}
		
		double dx = e.getX() - this.panLast.getX();
		double dy = e.getY() - this.panLast.getY();
		if (dx == 0.0 && dy == 0.0) {
			return;
		}
		double wPercent = -dx / this.panW;
		double hPercent = dy / this.panH;
		boolean old = this.getChart().getPlot().isNotify();
		this.getChart().getPlot().setNotify(false);
		
		switch (interaction) {
		case ZOOM:
			Zoomable pz = (Zoomable) this.getChart().getPlot();
			
			double zoomx = 1 + 2 * wPercent;
			double zoomy = 1 + 2 * hPercent;
			
			Point2D anchor = SimulationChart.this.translateScreenToJava2D(startPoint);
			
			if (pz.getOrientation() == PlotOrientation.VERTICAL) {
				pz.zoomDomainAxes(zoomx, this.getChartRenderingInfo().getPlotInfo(), anchor, true);
				pz.zoomRangeAxes(zoomy, this.getChartRenderingInfo().getPlotInfo(), anchor, true);
			} else {
				pz.zoomRangeAxes(zoomx, this.getChartRenderingInfo().getPlotInfo(), anchor, true);
				pz.zoomDomainAxes(zoomy, this.getChartRenderingInfo().getPlotInfo(), anchor, true);
			}
			
			break;
		}
		
		
		this.panLast = e.getPoint();
		this.getChart().getPlot().setNotify(old);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (interaction == null) {
			super.mouseReleased(e);
			return;
		}
		
		if (this.panLast != null) {
			this.panLast = null;
			setCursor(Cursor.getDefaultCursor());
		}
		interaction = null;
	}

    /**
     * Called whenever the mouse moves.
     *
     * Gets an entity at the location of the mouse, if this entity is a LegendItemEntity, calls updateHighlightingSet.
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        String legendLabel = null;
        ChartEntity entity = getChartRenderingInfo().getEntityCollection().getEntity(e.getX(), e.getY());

        if (entity instanceof LegendItemEntity) {
            legendLabel = entity.getToolTipText();
        }

        if ((legendLabel == null && hoveredLegendLabel != null)||(legendLabel != null && !legendLabel.equals(hoveredLegendLabel))) {
            hoveredLegendLabel = legendLabel;
            updateHighlightingSet();
        }
    }

    /**
     * Responsible for adding and removing labels from the selectedLegendLabels set before calling updateHighlightingSet.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getButton() != MouseEvent.BUTTON1) return;
        ChartEntity entity = getChartRenderingInfo().getEntityCollection().getEntity(e.getX(), e.getY());

        if (entity instanceof LegendItemEntity) {
            String label = entity.getToolTipText();
            if (selectedLegendLabels.contains(label)) {
                selectedLegendLabels.remove(label);
            }
            else selectedLegendLabels.add(label);
            updateHighlightingSet();
        }
    }
    /**
     * Draw a border around swatches of legend items that are currently highlighted.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Set<String> labelsToHighlight = new HashSet<>(selectedLegendLabels);
        EntityCollection entities = getChartRenderingInfo().getEntityCollection();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(4.0f));
            for (int i = 0; i < entities.getEntityCount(); i++) {
                ChartEntity entity = entities.getEntity(i);
                if (entity instanceof LegendItemEntity) {
                    String label = entity.getToolTipText();
                    if (label != null && labelsToHighlight.contains(label)) {
                        Rectangle2D swatchBorder = getRectangle2D(entity);
                        g2.draw(swatchBorder);
                    }
                }
            }
        } finally {
            g2.dispose();
        }
    }

    private static Rectangle2D getRectangle2D(ChartEntity entity) {
        Rectangle2D area = entity.getArea().getBounds2D();
        double h = area.getHeight();

        double swatchSize = 0.6 * h;
        double swatch_centerX = area.getX() + 0.5 * h; //center of the swatch
        double swatchX = swatch_centerX - 0.75 * swatchSize; // X coordinate for the top left corner of the swatch
        double swatchY = area.getY() + 0.5 * (h - swatchSize);// Y coordinate for the top left corner of the swatch

        return new Rectangle2D.Double(
                swatchX,
                swatchY,
                swatchSize,
                swatchSize
        );
    }

    private void updateHighlightingSet() {
        Set<String> labelsToHighlight = new HashSet<>(selectedLegendLabels);
        if (hoveredLegendLabel != null) {
            labelsToHighlight.add(hoveredLegendLabel);
        }
        applyLegendHighlight(labelsToHighlight);
    }

    private void applyLegendHighlight(Set<String> labelsToHighlight) {
        XYPlot plot = getChart().getXYPlot();
        boolean hasHighlight = labelsToHighlight != null;

        for (int r = 0; r < plot.getRendererCount(); r++) {
            XYItemRenderer renderer = plot.getRenderer(r);
            XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset(r);

            for (int s = 0; s < dataset.getSeriesCount(); s++) {
                String key = r + ":" + s;
                int finalS = s;

                //Stores the original stroke in the baseStrokes set so it can be recovered later to remove highlighting.
                BasicStroke base = (BasicStroke) baseStrokes.computeIfAbsent(key, k -> renderer.getSeriesStroke(finalS));

                BasicStroke stroke = base;
                if (hasHighlight && labelsToHighlight.contains(dataset.getSeries(s).getDescription())) {
                    stroke = new BasicStroke(base.getLineWidth() * 3.0f, base.getEndCap(), base.getLineJoin(), base.getMiterLimit(), base.getDashArray(), base.getDashPhase());
                }

                renderer.setSeriesStroke(s, stroke);
            }
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
				Zoomable zoomable = (Zoomable) plot;
				handleZoomable(zoomable, e);
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
		private void handleZoomable(Zoomable zoomable, MouseWheelEvent e) {
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
			boolean domainOnly = (e.getModifiers() & InputEvent.CTRL_MASK) != 0;
			if (SimulationChart.this.isRangeZoomable() && !domainOnly) {
				zoomable.zoomRangeAxes(zf, pinfo, p, true);
			}
			plot.setNotify(notifyState); // this generates the change event too
		}
	}
}
