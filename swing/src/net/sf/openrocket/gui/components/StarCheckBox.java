package net.sf.openrocket.gui.components;

import java.awt.Graphics;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.sf.openrocket.gui.util.Icons;

@SuppressWarnings("serial")
public class StarCheckBox extends JCheckBox {
	
	@Override
	public void paint(Graphics g) {
		JLabel l = new JLabel(Icons.FAVORITE);
		l.paint(g);
	}
	
}
