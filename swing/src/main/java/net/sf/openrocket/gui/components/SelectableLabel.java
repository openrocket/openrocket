package net.sf.openrocket.gui.components;

import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTextFieldUI;

public class SelectableLabel extends JTextField {

	public SelectableLabel() {
		this("");
	}

	public SelectableLabel(String text) {
		super(text);
		
		// Set basic UI since GTK l&f doesn't support null border
		this.setUI(new BasicTextFieldUI());
		
		this.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		
		this.setEditable(false);
		this.setBorder(null);
		this.setOpaque(true);
		if (UIManager.getColor("Label.foreground") != null)
			this.setForeground(UIManager.getColor("Label.foreground"));
		if (UIManager.getColor("Label.background") != null)
			this.setBackground(UIManager.getColor("Label.background"));
		if (UIManager.getFont("Label.font") != null)
			this.setFont(UIManager.getFont("Label.font"));
		
	}
	
	// The default preferred size is slightly too short, causing it to scroll
	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.width += 5;
		return dim;
	}

}
