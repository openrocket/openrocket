package net.sf.openrocket.gui.components;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A resizeable and styleable JLabel.  The method {@link #resizeFont(float)} changes the 
 * current font size by the given (positive or negative) amount.  The change is relative 
 * to the current font size.  The method {@link #setFontStyle(Style)} sets the style
 * (bold/italic) of the font.
 * <p>
 * A nice small text is achievable by  <code>new ResizeLabel("My text", -2);</code>
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */

public class StyledLabel extends JLabel {
	
	public enum Style {
		PLAIN(Font.PLAIN),
		BOLD(Font.BOLD),
		ITALIC(Font.ITALIC),
		BOLD_ITALIC(Font.BOLD | Font.ITALIC);
		
		private int style;
		Style(int fontStyle) {
			this.style = fontStyle;
		}
		public int getFontStyle() {
			return style;
		}
	}
	
	
	
	public StyledLabel() {
		this("", SwingConstants.LEADING, 0f);
	}
	
	public StyledLabel(String text) {
		this(text, SwingConstants.LEADING, 0f);
	}
	
	public StyledLabel(float size) {
		this("", SwingConstants.LEADING, size);
	}
	
	public StyledLabel(String text, float size) {
		this(text, SwingConstants.LEADING, size);
	}
	
	public StyledLabel(String text, int horizontalAlignment, float size) {
		super(text, horizontalAlignment);
		resizeFont(size);
		checkPreferredSize(size, Style.PLAIN);
	}
	
	

	public StyledLabel(Style style) {
		this("", SwingConstants.LEADING, 0f, style);
	}
	
	public StyledLabel(String text, Style style) {
		this(text, SwingConstants.LEADING, 0f, style);
	}
	
	public StyledLabel(float size, Style style) {
		this("", SwingConstants.LEADING, size, style);
	}
	
	public StyledLabel(String text, float size, Style style) {
		this(text, SwingConstants.LEADING, size, style);
	}
	
	public StyledLabel(String text, int horizontalAlignment, float size, Style style) {
		super(text, horizontalAlignment);
		resizeFont(size);
		setFontStyle(style);
		checkPreferredSize(size, style);
	}
	
	
	
	
	private void checkPreferredSize(float size, Style style) {
		String str = this.getText();
		if (str.startsWith("<html>") && str.indexOf("<br") < 0) {
			StyledLabel label = new StyledLabel("plaintext", size, style);
			label.validate();
			//System.out.println("Plain-text label: " + label.getPreferredSize());
			//System.out.println("HTML label: " + this.getPreferredSize());
		}
	}
	
	
	
	public void resizeFont(float size) {
		Font font = this.getFont();
		font = font.deriveFont(font.getSize2D()+size);
		this.setFont(font);
	}
	
	public void setFontStyle(Style style) {
		Font font = this.getFont();
		font = font.deriveFont(style.getFontStyle());
		this.setFont(font);
	}
}
