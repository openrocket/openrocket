package info.openrocket.swing.gui.components;

import java.awt.Graphics;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import info.openrocket.swing.gui.util.Icons;

public class StarCheckBox extends JCheckBox {
	
	
	
	@Override
	public void paint(Graphics g) {
		JLabel l = new JLabel(Icons.FAVORITE);
		l.paint(g);
	}
	
}
