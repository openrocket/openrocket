package info.openrocket.swing.gui.components;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Font;
import java.awt.Insets;
import java.awt.font.FontRenderContext;

import javax.swing.JLabel;
import javax.swing.UIManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StyledLabelTest extends BaseTestCase {

	@BeforeEach
	void useApplicationFont() {
		GUIUtil.loadCustomFonts();
		UIManager.put("Label.font", GUIUtil.createUIFont(GUIUtil.UI_FONT_STYLE_REGULAR, 14, 0));
	}

	/**
	 * Italic glyphs lean past their advance width, so a label sized to its text must leave room for the
	 * slant of the last glyph, or it is cut off at the label's edge (issue #3388).
	 */
	@ParameterizedTest
	@ValueSource(strings = { "Motor DB version: 20260920070751", "Italic text ending in f" })
	void italicTextFitsInPreferredWidth(String text) {
		StyledLabel label = new StyledLabel(-2.0f, StyledLabel.Style.ITALIC);
		label.setText(text);

		Font font = label.getFont();
		FontRenderContext frc = label.getFontMetrics(font).getFontRenderContext();
		double textEnd = font.createGlyphVector(frc, text).getOutline().getBounds2D().getMaxX();
		Insets insets = label.getInsets();

		int width = label.getPreferredSize().width;
		assertTrue(width >= insets.left + textEnd + insets.right,
				"The text's glyphs end at x=" + textEnd + ", past the preferred width " + width);
	}

	@ParameterizedTest
	@ValueSource(strings = { "Motor DB version: 20260920070751", "Plain text ending in f" })
	void plainTextKeepsLabelWidth(String text) {
		StyledLabel label = new StyledLabel(-2.0f, StyledLabel.Style.PLAIN);
		label.setText(text);
		JLabel reference = new JLabel(text);
		reference.setFont(label.getFont());

		assertEquals(reference.getPreferredSize().width, label.getPreferredSize().width);
	}
}
