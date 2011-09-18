package net.sf.openrocket.gui.components.compass;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.util.MathUtil;

/**
 * Component that allows selecting a compass direction on a CompassSelector.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class CompassSelector extends CompassPointer {
	
	private final DoubleModel model;
	
	public CompassSelector(DoubleModel model) {
		super(model);
		this.model = model;
		
		MouseAdapter mouse = new MouseAdapter() {
			private boolean dragging = false;
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (!isWithinCircle(e))
					return;
				if (e.getButton() != MouseEvent.BUTTON1)
					return;
				dragging = true;
				clicked(e);
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1)
					return;
				dragging = false;
			}
			
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (!dragging)
					return;
				clicked(e);
			}
		};
		this.addMouseListener(mouse);
		this.addMouseMotionListener(mouse);
		
	}
	
	private boolean isWithinCircle(MouseEvent e) {
		if (mid < 0 || width < 0) {
			return false;
		}
		
		int x = e.getX() - mid;
		int y = e.getY() - mid;
		
		double distance = Math.hypot(x, y);
		return distance < width / 2;
	}
	
	private void clicked(MouseEvent e) {
		
		if (mid < 0 || width < 0) {
			return;
		}
		
		int x = e.getX() - mid;
		int y = e.getY() - mid;
		
		double distance = Math.hypot(x, y);
		
		double theta = Math.atan2(y, x);
		theta = MathUtil.reduce360(theta + Math.PI / 2);
		
		// Round the value appropriately
		theta = Math.toDegrees(theta);
		
		if (distance > 50) {
			theta = Math.round(theta);
		} else if (distance > 10) {
			theta = 5 * Math.round(theta / 5);
		} else {
			// Do nothing if too close to center
			return;
		}
		theta = Math.toRadians(theta);
		
		model.setValue(theta);
	}
	
}
