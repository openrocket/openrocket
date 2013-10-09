package net.sf.openrocket.gui.components.compass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.gui.Resettable;
import net.sf.openrocket.gui.adaptors.DoubleModel;

/**
 * A component that draws a pointer onto a compass rose.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class CompassPointer extends CompassRose implements Resettable {
	
	private static final Color PRIMARY_POINTER_COLOR = new Color(1.0f, 0.2f, 0.2f);
	private static final Color SECONDARY_POINTER_COLOR = new Color(0.2f, 0.2f, 0.2f, 0.2f);
	
	private final DoubleModel model;
	private final ChangeListener listener;
	
	protected int width = -1;
	protected int mid = -1;
	
	private DoubleModel secondaryModel;
	
	private float pointerLength = 0.95f;
	private float pointerWidth = 0.1f;
	private float pointerArrowWidth = 0.2f;
	private boolean pointerArrow = true;
	
	

	public CompassPointer(DoubleModel model) {
		super();
		this.model = model;
		listener = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				CompassPointer.this.repaint();
			}
		};
		model.addChangeListener(listener);
	}
	
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		

		Dimension dimension = this.getSize();
		
		width = Math.min(dimension.width, dimension.height);
		mid = width / 2;
		width = (int) (getScaler() * width);
		

		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		

		if (secondaryModel != null) {
			drawArrow(secondaryModel.getValue(), SECONDARY_POINTER_COLOR, g2);
		}
		drawArrow(model.getValue(), PRIMARY_POINTER_COLOR, g2);
		

	}
	
	
	private void drawArrow(double angle, Color color, Graphics2D g2) {
		
		int pLength = (int) (width * pointerLength / 2);
		int pWidth = (int) (width * pointerWidth / 2);
		int pArrowWidth = (int) (width * pointerArrowWidth / 2);
		
		int[] x = new int[8];
		int[] y = new int[8];
		
		g2.setColor(color);
		

		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		
		int n = 0;
		
		// Top part
		x[n] = 0;
		y[n] = -pLength;
		n++;
		if (pointerArrow) {
			x[n] = -pArrowWidth;
			y[n] = -pLength + 2 * pArrowWidth;
			n++;
			x[n] = -pWidth;
			y[n] = -pLength + 2 * pArrowWidth;
			n++;
		}
		
		// Bottom part
		x[n] = -pWidth;
		y[n] = pLength;
		n++;
		x[n] = 0;
		y[n] = pLength - pWidth;
		n++;
		x[n] = pWidth;
		y[n] = pLength;
		n++;
		
		// Top part
		if (pointerArrow) {
			x[n] = pWidth;
			y[n] = -pLength + 2 * pArrowWidth;
			n++;
			x[n] = pArrowWidth;
			y[n] = -pLength + 2 * pArrowWidth;
			n++;
		}
		
		// Rotate and shift
		for (int i = 0; i < n; i++) {
			double x2, y2;
			x2 = cos * x[i] - sin * y[i];
			y2 = sin * x[i] + cos * y[i];
			
			x[i] = (int) (x2 + mid);
			y[i] = (int) (y2 + mid);
		}
		
		g2.fillPolygon(x, y, n);
		
		g2.setColor(color.darker());
		g2.drawPolygon(x, y, n);
		
	}
	
	
	public boolean isPointerArrow() {
		return pointerArrow;
	}
	
	
	public void setPointerArrow(boolean useArrow) {
		this.pointerArrow = useArrow;
		repaint();
	}
	
	
	public float getPointerLength() {
		return pointerLength;
	}
	
	
	public void setPointerLength(float pointerLength) {
		this.pointerLength = pointerLength;
		repaint();
	}
	
	
	public float getPointerWidth() {
		return pointerWidth;
	}
	
	
	public void setPointerWidth(float pointerWidth) {
		this.pointerWidth = pointerWidth;
		repaint();
	}
	
	
	public float getPointerArrowWidth() {
		return pointerArrowWidth;
	}
	
	
	public void setPointerArrowWidth(float pointerArrowWidth) {
		this.pointerArrowWidth = pointerArrowWidth;
		repaint();
	}
	
	

	public DoubleModel getSecondaryModel() {
		return secondaryModel;
	}
	
	
	public void setSecondaryModel(DoubleModel secondaryModel) {
		if (this.secondaryModel != null) {
			this.secondaryModel.removeChangeListener(listener);
		}
		this.secondaryModel = secondaryModel;
		if (this.secondaryModel != null) {
			this.secondaryModel.addChangeListener(listener);
		}
	}
	
	
	@Override
	public void resetModel() {
		model.removeChangeListener(listener);
		setSecondaryModel(null);
	}
	


}
