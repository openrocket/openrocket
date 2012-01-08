package net.sf.openrocket.gui.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * A color chooser button.  The currently selected color can be queried or set using the
 * {@link #getSelectedColor()} and {@link #setSelectedColor(Color)}, and changes listened
 * to by listening to property events with property name {@link #COLOR_KEY}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ColorChooserButton extends JButton {
	private static final LogHelper log = Application.getLogger();
	
	public static final String COLOR_KEY = "selectedColor";
	
	
	public ColorChooserButton(Color initial) {
		
		setSelectedColor(initial);
		
		// Add action listener that opens color chooser dialog
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.user("Activating color chooser");
				final JColorChooser chooser = new JColorChooser(getSelectedColor());
				chooser.setPreviewPanel(new JPanel());
				final JDialog dialog = JColorChooser.createDialog(ColorChooserButton.this, "Select color", true,
						chooser, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e2) {
								Color c = chooser.getColor();
								log.user("User selected color " + c);
								setSelectedColor(chooser.getColor());
							}
						}, null);
				log.info("Closing color chooser");
				dialog.setVisible(true);
			}
		});
		
	}
	
	
	public void addColorPropertyChangeListener(PropertyChangeListener listener) {
		this.addPropertyChangeListener(COLOR_KEY, listener);
	}
	
	public void setSelectedColor(Color c) {
		log.debug("Selecting color " + c);
		this.setIcon(new ColorIcon(c));
		this.putClientProperty(COLOR_KEY, c);
	}
	
	public Color getSelectedColor() {
		return (Color) this.getClientProperty(COLOR_KEY);
	}
	
}
