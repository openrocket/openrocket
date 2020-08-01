package net.sf.openrocket.gui.scalefigure;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.unit.Tick;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;



/**
 * A scroll pane that holds a {@link AbstractScaleFigure} and includes rulers that show
 * natural units.  The figure can be moved by dragging on the figure.
 * <p>
 * This class implements both <code>MouseListener</code> and 
 * <code>MouseMotionListener</code>.  If subclasses require extra functionality
 * (e.g. checking for clicks) then these methods may be overridden, and only unhandled
 * events passed to this class.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
@SuppressWarnings("serial")
public class ScaleScrollPane extends JScrollPane
		implements MouseListener, MouseMotionListener {
	
	public static final int RULER_SIZE = 20;
	public static final int MINOR_TICKS = 3;
	public static final int MAJOR_TICKS = 30;
	
	public static final String USER_SCALE_PROPERTY = "UserScale";
	
	private final JComponent component;
	private final AbstractScaleFigure figure;
	
	private DoubleModel rulerUnit;
	private Ruler horizontalRuler;
	private Ruler verticalRuler;

	// is the subject *currently* being fitting
	private boolean fit = false;

	// magic number.  I don't know why this number works, but this nudges the figures to zoom correctly.
    // n.b. it is slightly large than the ruler.width + scrollbar.width
	final Dimension viewportMarginPx = new Dimension( 40, 40);

	/**
	 * Create a scale scroll pane.
	 * 
	 * @param component		the component to contain (must implement ScaleFigure)
	 */
	public ScaleScrollPane(final JComponent component) {
		super(component);
		
		if (!(component instanceof AbstractScaleFigure)) {
			throw new IllegalArgumentException("component must implement ScaleFigure");
		}
		
		this.component = component;
		this.figure = (AbstractScaleFigure) component;
		
		rulerUnit = new DoubleModel(0.0, UnitGroup.UNITS_LENGTH);
		rulerUnit.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				ScaleScrollPane.this.component.repaint();
			}
		});
		horizontalRuler = new Ruler(Ruler.HORIZONTAL);
		verticalRuler = new Ruler(Ruler.VERTICAL);
		this.setColumnHeaderView(horizontalRuler);
		this.setRowHeaderView(verticalRuler);
		
		UnitSelector selector = new UnitSelector(rulerUnit);
		selector.setFont(new Font("SansSerif", Font.PLAIN, 8));
		this.setCorner(JScrollPane.UPPER_LEFT_CORNER, selector);
		
		this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		getHorizontalScrollBar().setUnitIncrement(50);
		//getHorizontalScrollBar().setBlockIncrement(viewport.getWidth());  // the default value is good

		setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		getVerticalScrollBar().setUnitIncrement(50);
		//getVerticalScrollBar().setBlockIncrement(viewport.getHeight());  // the default value is good

		viewport.addMouseListener(this);
		viewport.addMouseMotionListener(this);

		figure.addChangeListener( e -> {
			horizontalRuler.updateSize();
			verticalRuler.updateSize();
			if(fit) {
				final java.awt.Dimension calculatedViewSize = new java.awt.Dimension(getWidth() - viewportMarginPx.width, getHeight() - viewportMarginPx.height);
				figure.scaleTo(calculatedViewSize);
			}
		});
		
		viewport.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if(fit) {
					final Dimension calculatedViewSize = new Dimension(getWidth() - viewportMarginPx.width, getHeight() - viewportMarginPx.height);
					figure.scaleTo(calculatedViewSize);
				}
				figure.updateFigure();

				horizontalRuler.updateSize();
				verticalRuler.updateSize();
			}
		});
		
	}
	
	public AbstractScaleFigure getFigure() {
		return figure;
	}
	
	/**
	 * Return whether the figure is currently automatically fitted within the component bounds.
	 */
	public boolean isFitting() {
		return fit;
	}
	
	/**
	 * Set whether the figure is automatically fitted within the component bounds.
	 */
	public void setFitting(final boolean shouldFit) {
		this.fit = shouldFit;
		if (shouldFit) {
			validate();

			Dimension view = viewport.getExtentSize();
			figure.scaleTo(view);

			final Point zoomPoint = figure.getAutoZoomPoint();
			final Rectangle zoomRectangle = new Rectangle(zoomPoint.x, zoomPoint.y, (int)(view.getWidth()), (int)(view.getHeight()));
			figure.scrollRectToVisible(zoomRectangle);

			revalidate();
		}
	}
	
	public double getUserScale() {
		return figure.getUserScale();
	}
	
	public void setScaling(final double newScale) {
        // match if closer than 1%:
        if( MathUtil.equals(newScale, figure.getUserScale(), 0.01)){ 
            return;
        }

        // if explicitly setting a zoom level, turn off fitting
		this.fit = false;
		Dimension view = viewport.getExtentSize();
		figure.scaleTo(newScale, view);

		revalidate();
	}
	
	
	public Unit getCurrentUnit() {
		return rulerUnit.getCurrentUnit();
	}
    	
	public String toViewportString(){
	    Rectangle view = this.getViewport().getViewRect();
	    return ("Viewport::("+view.getWidth()+","+view.getHeight()+")"
	            +"@("+view.getX()+", "+view.getY()+")");
    }
     
	@Override
    public void revalidate() {
	    if( null != component ) {
    	    component.revalidate();
            figure.updateFigure();
	    }
	    
	    if( null != horizontalRuler ){
	        horizontalRuler.revalidate();
	        horizontalRuler.repaint();
	    }
	    if( null != verticalRuler ){
	        verticalRuler.revalidate();
	        verticalRuler.repaint();
        }
     
	    super.revalidate();
	}

	
	////////////////  Mouse handlers  ////////////////
	private int dragStartX = 0;
	private int dragStartY = 0;
	private Rectangle dragRectangle = null;
	
	@Override
	public void mousePressed(MouseEvent e) {
		dragStartX = e.getX();
		dragStartY = e.getY();
		dragRectangle = viewport.getViewRect();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		dragRectangle = null;
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (dragRectangle == null) {
			return;
		}
		
		dragRectangle.setLocation(dragStartX - e.getX(), dragStartY - e.getY());
		
		dragStartX = e.getX();
		dragStartY = e.getY();

		viewport.scrollRectToVisible(dragRectangle);
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
	}
	
	
	
	////////////////  The view port rulers  ////////////////
	
	
	private class Ruler extends JComponent implements ChangeListener {
		public static final int HORIZONTAL = 0;
		public static final int VERTICAL = 1;
		
		private final int orientation;
		
		public Ruler(int orientation) {
			this.orientation = orientation;
			rulerUnit.addChangeListener(this);
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			updateSize();
			repaint();
		}
		
		private void updateSize() {
			if (orientation == HORIZONTAL) {
			    setMinimumSize(new Dimension(component.getWidth() + 10, RULER_SIZE));
			    setPreferredSize(new Dimension(component.getWidth() + 10, RULER_SIZE));
			} else {
			    setMinimumSize(new Dimension(RULER_SIZE, component.getHeight() + 10));
			    setPreferredSize(new Dimension(RULER_SIZE, component.getHeight() + 10));
			}
		}
		
        private double fromPx(final int px) {
            final Point origin = figure.getSubjectOrigin();
            double realValue = Double.NaN;
            if (orientation == HORIZONTAL) {
                realValue = px - origin.x;
            } else {
                realValue = origin.y - px;
            }
            return realValue / figure.getAbsoluteScale();
		}
		
		private int toPx(final double value) {
			final Point origin = figure.getSubjectOrigin();
			final int px = (int) (value * figure.getAbsoluteScale() + 0.5);
			if (orientation == HORIZONTAL) {
				return (px + origin.x);
			} else {
				return (origin.y - px);
			}
		}
		
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			
			updateSize();
			
			// this function doesn't reliably update all the time, so we'll draw everything for the entire canvas, 
			// and let the JVM drawing algorithms figure out what should be drawn.
			Rectangle area = viewport.getViewRect();

			// Fill area with background color
			g2.setColor(getBackground());
			g2.fillRect(area.x, area.y, area.width, area.height + 100);
		
			int startpx, endpx;
			if (orientation == HORIZONTAL) {
				startpx = area.x;
				endpx = area.x + area.width;
			} else {
				startpx = area.y;
				endpx = area.y + area.height;
			}
			
			final double start = fromPx(startpx);
			final double end = fromPx(endpx);
			
			final double minor = MINOR_TICKS / figure.getAbsoluteScale();
			final double major = MAJOR_TICKS / figure.getAbsoluteScale();
  
			Unit unit = rulerUnit.getCurrentUnit();
            Tick[] ticks = null;
            if( VERTICAL == orientation ){
                // the parameters are *intended* to be backwards: because 'getTicks(...)' can only 
                // create increasing arrays (where the start < end)
                ticks = unit.getTicks(end, start, minor, major);
            }else if(HORIZONTAL == orientation ){
                // normal parameter order
                ticks = unit.getTicks(start, end, minor, major);
            }
			
			// Set color & hints
			g2.setColor(Color.BLACK);
			g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
					RenderingHints.VALUE_STROKE_NORMALIZE);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
					RenderingHints.VALUE_RENDER_QUALITY);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			for (Tick t : ticks) {
				int position = toPx(t.value);
				drawTick(g2, position, t);
			}
		}
		
		private void drawTick(Graphics g, int position, Tick t) {
			int length;
			String str = null;
			if (t.major) {
				length = RULER_SIZE / 2;
			} else {
				if (t.notable)
					length = RULER_SIZE / 3;
				else
					length = RULER_SIZE / 6;
			}
			
			// Set font
			if (t.major) {
				str = rulerUnit.getCurrentUnit().toString(t.value);
				if (t.notable)
					g.setFont(new Font("SansSerif", Font.BOLD, 9));
				else
					g.setFont(new Font("SansSerif", Font.PLAIN, 9));
			}
			
			// Draw tick & text
			if (orientation == HORIZONTAL) {
				g.drawLine(position, RULER_SIZE - length, position, RULER_SIZE);
				if (str != null)
					g.drawString(str, position, RULER_SIZE - length - 1);
			} else {
				g.drawLine(RULER_SIZE - length, position, RULER_SIZE, position);
				if (str != null)
					g.drawString(str, 1, position - 1);
			}
		}
	}
}
