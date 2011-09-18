package net.sf.openrocket.gui.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * A JButton that appears flat until you roll over it.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class FlatButton extends JButton {
	
	public FlatButton() {
		super();
		initialize();
	}
	
	public FlatButton(Icon icon) {
		super(icon);
		initialize();
	}
	
	public FlatButton(String text) {
		super(text);
		initialize();
	}
	
	public FlatButton(Action a) {
		super(a);
		initialize();
	}
	
	public FlatButton(String text, Icon icon) {
		super(text, icon);
		initialize();
	}
	
	
	private void initialize() {
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				flatten();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				raise();
			}
		});
		flatten();
	}
	
	
	private void flatten() {
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
	}
	
	private void raise() {
		this.setContentAreaFilled(true);
		this.setBorderPainted(true);
	}
	
}
