package net.sf.openrocket.gui.components;

import java.awt.Color;

import javax.swing.Icon;

/**
 * An Icon that displays a specific color, suitable for drawing into a button.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ColorIcon implements Icon {
	private final Color color;
	
	public ColorIcon(Color c) {
		this.color = c;
	}
	
	@Override
	public int getIconHeight() {
		return 15;
	}
	
	@Override
	public int getIconWidth() {
		return 25;
	}
	
	@Override
	public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
		g.setColor(color);
		g.fill3DRect(x, y, getIconWidth(), getIconHeight(), false);
	}
	
}
