package info.openrocket.swing.gui.components;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * BasicSliderUI for dark theme UI.
 */
public class DarkBasicSliderUI extends BasicSliderUI {
	private static final Color trackColor = new Color(159, 159, 159);
	private static final Color thumbColor = new Color(82, 82, 82);
	private static final Color thumbBorderColor = new Color(166, 166, 166);

	public DarkBasicSliderUI(JSlider b) {
		super(b);
	}

	@Override
	public void paintTrack(Graphics g) {
		g.setColor(trackColor);
		super.paintTrack(g);
	}

	@Override
	public void paintThumb(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		Rectangle thumbBounds = thumbRect;
		int w = thumbBounds.width;
		int h = thumbBounds.height;

		int borderInset = 2;  // Adjust this value to change the border thickness

		// Draw the border
		g2d.setColor(thumbBorderColor);
		g2d.fillRect(thumbBounds.x, thumbBounds.y, w, h);

		// Draw the thumb fill
		g2d.setColor(thumbColor);
		g2d.fillRect(
				thumbBounds.x + borderInset - 1,
				thumbBounds.y + borderInset - 1,
				w - 2 * borderInset + 1,
				h - 2 * borderInset + 1
		);
	}

}