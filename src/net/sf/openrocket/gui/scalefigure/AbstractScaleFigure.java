package net.sf.openrocket.gui.scalefigure;

import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.util.Prefs;


public abstract class AbstractScaleFigure extends JPanel implements ScaleFigure {

	// Number of pixels to leave at edges when fitting figure
	public static final int BORDER_PIXELS_WIDTH=30;
	public static final int BORDER_PIXELS_HEIGHT=20;
	
	
	protected final double dpi;

	protected double scale = 1.0;
	protected double scaling = 1.0;
	
	protected final List<ChangeListener> listeners = new LinkedList<ChangeListener>();
	

	public AbstractScaleFigure() {
		this.dpi = Prefs.getDPI();
		this.scaling = 1.0;
		this.scale = dpi/0.0254*scaling;
		
		setBackground(Color.WHITE);
		setOpaque(true);
	}
	
	
	
	public abstract void updateFigure();
	public abstract double getFigureWidth();
	public abstract double getFigureHeight();
	

	@Override
	public double getScaling() {
		return scaling;
	}

	@Override
	public double getAbsoluteScale() {
		return scale;
	}
	
	@Override
	public void setScaling(double scaling) {
		if (Double.isInfinite(scaling) || Double.isNaN(scaling))
			scaling = 1.0;
		if (scaling < 0.001)
			scaling = 0.001;
		if (scaling > 1000)
			scaling = 1000;
		if (Math.abs(this.scaling - scaling) < 0.01)
			return;
		this.scaling = scaling;
		this.scale = dpi/0.0254*scaling;
		updateFigure();
	}
	
	@Override
	public void setScaling(Dimension bounds) {
		double zh = 1, zv = 1;
		int w = bounds.width - 2*BORDER_PIXELS_WIDTH -20;
		int h = bounds.height - 2*BORDER_PIXELS_HEIGHT -20;
		
		if (w < 10)
			w = 10;
		if (h < 10)
			h = 10;
		
		zh = ((double)w) / getFigureWidth();
		zv = ((double)h) / getFigureHeight();
			
		double s = Math.min(zh, zv)/dpi*0.0254 - 0.001;
		
		setScaling(s);
	}

	

	@Override
	public void addChangeListener(ChangeListener listener) {
		listeners.add(0,listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}
	
	private ChangeEvent changeEvent = null;
	protected void fireChangeEvent() {
		ChangeListener[] list = listeners.toArray(new ChangeListener[0]);
		for (ChangeListener l: list) {
			if (changeEvent == null)
				changeEvent = new ChangeEvent(this);
			l.stateChanged(changeEvent);
		}
	}

}
