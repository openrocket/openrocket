package info.openrocket.swing.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GUIUtilFontTest {

	private static final String CHINESE = "\u706b\u7bad\u4eff\u771f\u8bbe\u8ba1\u53d1\u52a8\u673a\u63a8\u529b";

	@ParameterizedTest
	@ValueSource(strings = { "inter-light", "inter-regular", "inter-medium", "inter-bold" })
	void chineseFontsContainGlyphsAndRetainSizeAndSpacing(String style) {
		assumeTrue(new Font(Font.DIALOG, Font.PLAIN, 16).canDisplayUpTo(CHINESE) == -1,
				"The platform must have a Chinese font installed");
		Locale previous = Locale.getDefault();
		try {
			Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
			Font font = GUIUtil.createUIFont(style, 16, 0.02f);

			assertEquals(-1, font.canDisplayUpTo(CHINESE));
			assertEquals(16f, font.getSize2D());
			assertEquals(0.02f, font.getAttributes().get(TextAttribute.TRACKING));
		} finally {
			Locale.setDefault(previous);
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "inter-light", "inter-regular", "inter-medium", "inter-bold" })
	void englishKeepsTheBundledFont(String style) {
		Locale previous = Locale.getDefault();
		try {
			Locale.setDefault(Locale.US);
			assertEquals(GUIUtil.UI_FONT_FAMILY, GUIUtil.createUIFont(style, 16, 0).getFamily(Locale.ROOT));
		} finally {
			Locale.setDefault(previous);
		}
	}
}
