package net.sf.openrocket.gui.components;

import java.awt.Dimension;

import javax.swing.JLabel;

/**
 * A JLabel that limits the minimum and maximum height of the label to the
 * initial preferred height of the label.  This is required in labels that use HTML
 * since these often cause the panels to expand too much in height.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class HtmlLabel extends JLabel {

	public HtmlLabel() {
		super();
		limitSize();
	}

	public HtmlLabel(String text) {
		super(text);
		limitSize();
	}

	public HtmlLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		limitSize();
	}
	
	
	private void limitSize() {
		Dimension dim = this.getPreferredSize();
		this.setMinimumSize(new Dimension(0, dim.height));
		this.setMaximumSize(new Dimension(Integer.MAX_VALUE, dim.height));
	}

}
