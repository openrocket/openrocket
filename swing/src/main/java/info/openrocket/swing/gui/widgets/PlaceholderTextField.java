package info.openrocket.swing.gui.widgets;

import javax.swing.JTextField;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A text field that can display a placeholder when empty.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class PlaceholderTextField extends JTextField {
	private String placeholder;

	public PlaceholderTextField() { }

	public PlaceholderTextField(final int pColumns) {
		super(pColumns);
	}

	public PlaceholderTextField(final String pText) {
		super(pText);
	}

	public PlaceholderTextField(final String pText, final int pColumns) {
		super(pText, pColumns);
	}

	public String getPlaceholder() {
		return placeholder;
	}


	@Override
	protected void paintComponent(Graphics pG) {
		super.paintComponent(pG);

		if (placeholder == null || placeholder.isEmpty() || !getText().isEmpty()) {
			return;
		}

		final Graphics2D g = (Graphics2D) pG;
		g.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getDisabledTextColor());
		g.drawString(placeholder, getInsets().left, pG.getFontMetrics()
				.getMaxAscent() + getInsets().top);
	}

	public void setPlaceholder(final String s) {
		placeholder = s;
	}
}
