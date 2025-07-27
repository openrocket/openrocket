package info.openrocket.swing.gui.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.event.ChangeListener;

import info.openrocket.core.logging.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A color chooser button.  The currently selected color can be queried or set using the
 * {@link #getSelectedColor()} and {@link #setSelectedColor(Color)}, and changes listened
 * to by listening to property events with property name {@link #COLOR_KEY}.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
@SuppressWarnings("serial")
public class ColorChooserButton extends JButton {
	private static final Logger log = LoggerFactory.getLogger(ColorChooserButton.class);

	public static final String COLOR_KEY = "selectedColor";

	private JColorChooser chooser;

	/**
	 * Main constructor.
	 *
	 * @param initial the initial color, or null.
	 * @param chooser a shared JColorChooser instance, or null to use a private one.
	 */
	public ColorChooserButton(Color initial, JColorChooser chooser) {
		if (chooser == null) {
			this.chooser = new JColorChooser();
		} else {
			this.chooser = chooser;
		}
		setSelectedColor(initial);

		// Add action listener that opens color chooser dialog
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Activating color chooser");

				// Store the original color in case of cancellation
				final Color originalColor = getSelectedColor();
				ColorChooserButton.this.chooser.setColor(originalColor);

				// This listener will handle live updates.
				final ChangeListener liveUpdateListener = changeEvent -> {
					setSelectedColor(ColorChooserButton.this.chooser.getColor());
				};
				ColorChooserButton.this.chooser.getSelectionModel().addChangeListener(liveUpdateListener);

				// Create the dialog
				JDialog dialog = JColorChooser.createDialog(
						ColorChooserButton.this,
						"Select color",
						true, // modal
						ColorChooserButton.this.chooser,
						okEvent -> { // OK button listener
							// Final color is already set, just remove the listener
							ColorChooserButton.this.chooser.getSelectionModel().removeChangeListener(liveUpdateListener);
						},
						cancelEvent -> { // Cancel button listener
							// Revert to original color and remove the listener
							setSelectedColor(originalColor);
							ColorChooserButton.this.chooser.getSelectionModel().removeChangeListener(liveUpdateListener);
						}
				);

				dialog.setVisible(true);
			}
		});
	}

	public ColorChooserButton(Color initial) {
		this(initial, null);
	}

	public void addColorPropertyChangeListener(PropertyChangeListener listener) {
		this.addPropertyChangeListener(COLOR_KEY, listener);
	}

	public void setSelectedColor(Color c) {
		Color old = getSelectedColor();
		if (c != null && !c.equals(old)) {
			log.debug("Selecting color " + c);
			this.setIcon(new ColorIcon(c));
			this.putClientProperty(COLOR_KEY, c);
			firePropertyChange(COLOR_KEY, old, c);
		}
	}

	public Color getSelectedColor() {
		return (Color) this.getClientProperty(COLOR_KEY);
	}
}