package net.sf.openrocket.gui;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.miginfocom.swing.MigLayout;

public class DescriptionArea extends JScrollPane {

	private ResizeLabel text;
	private MigLayout layout;
	private JPanel panel;
	
	public DescriptionArea(int rows) {
		this(rows, -2);
	}
	
	public DescriptionArea(int rows, float size) {
		super(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		layout = new MigLayout("ins 0 2px, fill");
		panel = new JPanel(layout);
		
		text = new ResizeLabel(" ",size);
		text.validate();
		Dimension dim = text.getPreferredSize();
		dim.height = (dim.height+2)*rows + 2;
		this.setPreferredSize(dim);
		
		panel.add(text, "growx");
		
		this.setViewportView(panel);
		this.revalidate();
	}
	
	public void setText(String txt) {
		if (!txt.startsWith("<html>"))
			txt = "<html>" + txt;
		text.setText(txt);
	}
	
	
	@Override
	public void validate() {
		
		Rectangle dim = this.getViewportBorderBounds();
		layout.setComponentConstraints(text, "width "+ dim.width + ", growx");
		super.validate();
		text.validate();

	}
	
}
