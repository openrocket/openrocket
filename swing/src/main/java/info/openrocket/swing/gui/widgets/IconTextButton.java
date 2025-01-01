package info.openrocket.swing.gui.widgets;

import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Font;

/**
 * A custom button component that displays an icon with text beneath it.
 * The icon and text are both center-aligned within the button.
 */
public class IconTextButton extends JButton {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a button with no text or icon
	 */
	public IconTextButton() {
		this(null, null, null);
	}

	/**
	 * Creates a button with the specified text and no icon
	 *
	 * @param text The text to display on the button
	 */
	public IconTextButton(String text) {
		this(text, null, null);
	}

	/**
	 * Creates a button with the specified text and icons
	 *
	 * @param text The text to display on the button
	 * @param enabledIcon The icon to display when the button is enabled
	 * @param disabledIcon The icon to display when the button is disabled
	 */
	public IconTextButton(String text, Icon enabledIcon, Icon disabledIcon) {
		super(formatText(text), enabledIcon);

		// Configure icon placement
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);

		// Configure button alignment
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);

		// Set disabled icon if provided
		if (disabledIcon != null) {
			setDisabledIcon(disabledIcon);
		}

		// Ensure components inherit enabled state
		addPropertyChangeListener("enabled", evt -> {
			boolean enabled = (Boolean) evt.getNewValue();
			for (Component child : getComponents()) {
				child.setEnabled(enabled);
			}
		});
	}

	/**
	 * Formats the button text, converting newlines to HTML line breaks if needed
	 *
	 * @param text The text to format
	 * @return Formatted text, possibly wrapped in HTML tags
	 */
	private static String formatText(String text) {
		if (text == null || !text.contains("\n")) {
			return text;
		}

		return "<html><center>" + text.replace("\n", "<br>") + "</center></html>";
	}

	/**
	 * Override to ensure custom font settings are preserved
	 */
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		// Propagate font to child components if needed
		for (Component child : getComponents()) {
			child.setFont(font);
		}
	}
}
