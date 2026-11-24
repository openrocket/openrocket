package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import info.openrocket.swing.util.BaseTestCase;

/**
 * Regression tests for UI font fallback coverage (issue #3482) and HiDPI text-selection metrics (issue #3477).
 */
public class GUIUtilFontTest extends BaseTestCase {

	@BeforeAll
	public static void loadFonts() {
		GUIUtil.loadCustomFonts();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			GUIUtil.UI_FONT_STYLE_LIGHT,
			GUIUtil.UI_FONT_STYLE_REGULAR,
			GUIUtil.UI_FONT_STYLE_MEDIUM,
			GUIUtil.UI_FONT_STYLE_BOLD
	})
	public void uiFontSupportsPlatformFallbackGlyphs(String style) {
		Font uiFont = GUIUtil.createUIFont(style, 14.0f, 0.0f);
		Font dialogFont = new Font(Font.DIALOG, Font.PLAIN, 14);

		assertTrue(uiFont.canDisplay('A'), "UI font must display ASCII characters");
		for (char c : new char[] { '\u4e2d', '\u6587', '\u3042', '\ud55c' }) {
			if (dialogFont.canDisplay(c)) {
				assertTrue(uiFont.canDisplay(c),
						"UI font (" + uiFont.getName() + ") must match platform Dialog fallback for U+"
								+ Integer.toHexString(c));
			}
		}
		assertFalse(uiFont.hasLayoutAttributes(),
				"Zero letter spacing should not attach layout attributes");
	}

	@Test
	public void uiFontStylesAreDistinct() {
		Font light = GUIUtil.createUIFont(GUIUtil.UI_FONT_STYLE_LIGHT, 14.0f, 0.0f);
		Font regular = GUIUtil.createUIFont(GUIUtil.UI_FONT_STYLE_REGULAR, 14.0f, 0.0f);
		Font medium = GUIUtil.createUIFont(GUIUtil.UI_FONT_STYLE_MEDIUM, 14.0f, 0.0f);
		Font bold = GUIUtil.createUIFont(GUIUtil.UI_FONT_STYLE_BOLD, 14.0f, 0.0f);

		Set<Font> distinct = new HashSet<>();
		distinct.add(light);
		distinct.add(regular);
		distinct.add(medium);
		distinct.add(bold);
		assertEquals(4, distinct.size(), "Each UI font weight must be distinct to avoid FontDesignMetrics collisions");
	}

	/**
	 * Issue #3477 is a macOS Retina problem, fixed by resolving native CoreText fonts there. On Windows and Linux the
	 * bundled Inter is a FreeType font whose hinted 1x integer advances legitimately differ from 2x fractional ones.
	 */
	@ParameterizedTest
	@EnabledOnOs(OS.MAC)
	@ValueSource(strings = {
			GUIUtil.UI_FONT_STYLE_LIGHT,
			GUIUtil.UI_FONT_STYLE_REGULAR,
			GUIUtil.UI_FONT_STYLE_MEDIUM,
			GUIUtil.UI_FONT_STYLE_BOLD
	})
	public void uiFontMetricsMatchHiDpiGlyphAdvance(String style) {
		String prefix = "---------- System ";
		Font uiFont = GUIUtil.createUIFont(style, 14.0f, 0.0f);

		// Unattached JEditorPane uses Swing's default 1x FontRenderContext during layout (as in BugReportDialog
		// before pack/show), while painting on a Retina display uses a 2x scaled Graphics2D.
		JEditorPane pane = new JEditorPane("text/html",
				"<html><p style=\"margin-top: 0\">---------- System information ----------</p></html>");
		pane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
		pane.setFont(uiFont);
		FontMetrics layoutMetrics = pane.getFontMetrics(uiFont);
		int layoutWidth = layoutMetrics.stringWidth(prefix);

		BufferedImage image = new BufferedImage(600, 100, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		try {
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
			g2.transform(AffineTransform.getScaleInstance(2.0, 2.0));
			FontRenderContext hiDpiFrc = g2.getFontRenderContext();
			double paintedWidth = uiFont.createGlyphVector(hiDpiFrc, prefix)
					.getGlyphPosition(prefix.length()).getX();

			assertTrue(Math.abs(paintedWidth - layoutWidth) <= 1.0,
					"Layout width (" + layoutWidth + ") drifted from 2x painted width (" + paintedWidth
							+ ") for " + style + " (" + uiFont.getName() + ")");
		} finally {
			g2.dispose();
		}
	}

	@Test
	public void nonZeroTrackingChangesAdvanceWidth() {
		Font base = GUIUtil.createUIFont(GUIUtil.UI_FONT_STYLE_REGULAR, 14.0f, 0.0f);
		Font tracked = GUIUtil.createUIFont(GUIUtil.UI_FONT_STYLE_REGULAR, 14.0f, 0.05f);
		FontRenderContext frc = new FontRenderContext(null, true, false);

		assertNotEquals(base, tracked);
		// Measure via TextLayout, as Swing's FontMetrics does for fonts with layout attributes; Font.getStringBounds()
		// ignores TRACKING for native macOS fonts.
		assertTrue(new TextLayout("OpenRocket", tracked, frc).getAdvance()
				> new TextLayout("OpenRocket", base, frc).getAdvance());
	}

	@Test
	public void bundledInterFontFilesHaveDistinctNames() throws Exception {
		String[] paths = {
				"/fonts/Inter/Inter-Light.ttf",
				"/fonts/Inter/Inter-Regular.ttf",
				"/fonts/Inter/Inter-Medium.ttf",
				"/fonts/Inter/Inter-Bold.ttf"
		};
		Set<String> fullNames = new HashSet<>();
		for (String path : paths) {
			try (InputStream in = Objects.requireNonNull(GUIUtil.class.getResourceAsStream(path),
					"Missing bundled font " + path)) {
				Font loaded = Font.createFont(Font.TRUETYPE_FONT, in);
				fullNames.add(loaded.getName());
			}
		}
		assertEquals(paths.length, fullNames.size(),
				"Bundled Inter faces must have distinct font names so registerFont() and FontDesignMetrics do not collide");
	}
}
