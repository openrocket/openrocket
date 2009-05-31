package net.sf.openrocket.gui.components;

import java.awt.Font;
import javax.swing.JLabel;

/**
 * A resizeable JLabel.  The method resizeFont(float) changes the current font size by the
 * given (positive or negative) amount.  The change is relative to the current font size.
 * <p>
 * A nice small text is achievable by  <code>new ResizeLabel("My text", -2);</code>
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class ResizeLabel extends JLabel {
	
	public ResizeLabel() {
		super();
	}
	
	public ResizeLabel(String text) {
		super(text);
	}
	
	public ResizeLabel(float size) {
		super();
		resizeFont(size);
	}
	
	public ResizeLabel(String text, float size) {
		super(text);
		resizeFont(size);
	}
	
	public ResizeLabel(String text, int horizontalAlignment, float size) {
		super(text, horizontalAlignment);
		resizeFont(size);
	}
	
	
	public void resizeFont(float size) {
		Font font = this.getFont();
		font = font.deriveFont(font.getSize2D()+size);
		this.setFont(font);
	}
	
}
