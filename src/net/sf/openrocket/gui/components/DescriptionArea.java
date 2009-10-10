package net.sf.openrocket.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

public class DescriptionArea extends JScrollPane {

	private final JEditorPane editorPane;
	

	public DescriptionArea(int rows) {
		this("", rows, -1);
	}
	public DescriptionArea(int rows, float size) {
		this("", rows, size);
	}
	
	public DescriptionArea(String text, int rows, float size) {
		super(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		editorPane = new JEditorPane("text/html", "");
		Font font = editorPane.getFont();
		editorPane.setFont(font.deriveFont(font.getSize2D() + size));
		editorPane.setEditable(false);
		
		// Calculate correct height
		editorPane.setText("abc");
		Dimension oneline = editorPane.getPreferredSize();
		editorPane.setText("abc<br>def");
		Dimension twolines = editorPane.getPreferredSize();
		editorPane.setText("");
		
		int lineheight = twolines.height - oneline.height;
		int extraheight = oneline.height - lineheight;
		
		Dimension dim = editorPane.getPreferredSize();
		dim.height = lineheight * rows + extraheight + 2;
		this.setPreferredSize(dim);
		
		this.setViewportView(editorPane);
		this.setText(text);
	}
	
	public void setText(String txt) {
		editorPane.setText(txt);
		editorPane.revalidate();
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				editorPane.scrollRectToVisible(new Rectangle(0,0,1,1));
			}
			
		});
		editorPane.scrollRectToVisible(new Rectangle(0,0,1,1));
	}
	
}
