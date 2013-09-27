package net.sf.openrocket.gui.figureelements;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public interface FigureElement {

	public void paint(Graphics2D g2, double scale);
	
	public void paint(Graphics2D g2, double scale, Rectangle visible);
	
}
