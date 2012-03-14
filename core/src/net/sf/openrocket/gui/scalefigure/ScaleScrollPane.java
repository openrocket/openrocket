package net.sf.openrocket.gui.scalefigure;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.unit.Tick;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.StateChangeListener;



/**
 * A scroll pane that holds a {@link ScaleFigure} and includes rulers that show
 * natural units.  The figure can be moved by dragging on the figure.
 * <p>
 * This class implements both <code>MouseListener</code> and 
 * <code>MouseMotionListener</code>.  If subclasses require extra functionality
 * (e.g. checking for clicks) then these methods may be overridden, and only unhandled
 * events passed to this class.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ScaleScrollPane extends JScrollPane
		implements MouseListener, MouseMotionListener {
	
	public static final int RULER_SIZE = 20;
	public static final int MINOR_TICKS = 3;
	public static final int MAJOR_TICKS = 30;
	
	
	private JComponent component;
	private ScaleFigure figure;
	
	private DoubleModel rulerUnit;
	private Ruler horizontalRuler;
	private Ruler verticalRuler;
	
	private final boolean allowFit;
	
	private boolean fit = false;
	
	
	/**
	 * Create a scale scroll pane that allows fitting.
	 * 
	 * @param component		the component to contain (must implement ScaleFigure)
	 */
	public ScaleScrollPane(JComponent component) {
		this(component, true);
	}
	
	/**
	 * Create a scale scroll pane.
	 * 
	 * @param component		the component to contain (must implement ScaleFigure)
	 * @param allowFit		whether automatic fitting of the figure is allowed
	 */
	public ScaleScrollPane(JComponent component, boolean allowFit) {
		super(component);
		
		if (!(component instanceof ScaleFigure)) {
			throw new IllegalArgumentException("component must implement ScaleFigure");
		}
		
		this.component = component;
		this.figure = (ScaleFigure) component;
		this.allowFit = allowFit;
		
		
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
		this.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
		this.setCorner(JScrollPane.LOWER_LEFT_CORNER, new JPanel());
		this.setCorner(JScrollPane.LOWER_RIGHT_CORNER, new JPanel());
		
		this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		
		
		viewport.addMouseListener(this);
		viewport.addMouseMotionListener(this);
		
		figure.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				horizontalRuler.updateSize();
				verticalRuler.updateSize();
				if (fit) {
					setFitting(true);
				}
			}
		});
		
		viewport.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (fit) {
					setFitting(true);
				}
			}
		});
		
	}
	
	public ScaleFigure getFigure() {
		return figure;
	}
	
	
	/**
	 * Return whether automatic fitting of the figure is allowed.
	 */
	public boolean isFittingAllowed() {
		return allowFit;
	}
	
	/**
	 * Return whether the figure is currently automatically fitted within the component bounds.
	 */
	public boolean isFitting() {
		return fit;
	}
	
	/**
	 * Set whether the figure is automatically fitted within the component bounds.
	 * 
	 * @throws BugException		if automatic fitting is disallowed and <code>fit</code> is <code>true</code>
	 */
	public void setFitting(boolean fit) {
		if (fit && !allowFit) {
			throw new BugException("Attempting to fit figure not allowing fit.");
		}
		this.fit = fit;
		if (fit) {
			setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
			validate();
			Dimension view = viewport.getExtentSize();
			figure.setScaling(view);
		} else {
			setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		}
	}
	
	
	
	public double getScaling() {
		return figure.getScaling();
	}
	
	public double getScale() {
		return figure.getAbsoluteScale();
	}
	
	public void setScaling(double scale) {
		if (fit) {
			setFitting(false);
		}
		figure.setScaling(scale);
		horizontalRuler.repaint();
		verticalRuler.repaint();
	}
	
	
	public Unit getCurrentUnit() {
		return rulerUnit.getCurrentUnit();
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
	
	
	private class Ruler extends JComponent {
		public static final int HORIZONTAL = 0;
		public static final int VERTICAL = 1;
		
		private final int orientation;
		
		public Ruler(int orientation) {
			this.orientation = orientation;
			updateSize();
			
			rulerUnit.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					Ruler.this.repaint();
				}
			});
		}
		
		
		public void updateSize() {
			Dimension d = component.getPreferredSize();
			if (orientation == HORIZONTAL) {
				setPreferredSize(new Dimension(d.width + 10, RULER_SIZE));
			} else {
				setPreferredSize(new Dimension(RULER_SIZE, d.height + 10));
			}
			revalidate();
			repaint();
		}
		
		private double fromPx(int px) {
			Dimension origin = figure.getOrigin();
			if (orientation == HORIZONTAL) {
				px -= origin.width;
			} else {
				//				px = -(px - origin.height);
				px -= origin.height;
			}
			return px / figure.getAbsoluteScale();
		}
		
		private int toPx(double l) {
			Dimension origin = figure.getOrigin();
			int px = (int) (l * figure.getAbsoluteScale() + 0.5);
			if (orientation == HORIZONTAL) {
				px += origin.width;
			} else {
				px = px + origin.height;
				//				px += origin.height;
			}
			return px;
		}
		
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			
			Rectangle area = g2.getClipBounds();
			
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
			
			Unit unit = rulerUnit.getCurrentUnit();
			double start, end, minor, major;
			start = fromPx(startpx);
			end = fromPx(endpx);
			minor = MINOR_TICKS / figure.getAbsoluteScale();
			major = MAJOR_TICKS / figure.getAbsoluteScale();
			
			Tick[] ticks = unit.getTicks(start, end, minor, major);
			
			
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
