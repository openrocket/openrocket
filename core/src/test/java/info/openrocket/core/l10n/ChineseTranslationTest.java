package info.openrocket.core.l10n;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ChineseTranslationTest {

	private final Translator translator = new ResourceBundleTranslator("l10n.messages", Locale.SIMPLIFIED_CHINESE);

	@AfterAll
	static void clearBundleCache() {
		ResourceBundle.clearCache();
	}

	@ParameterizedTest
	@ValueSource(strings = { "RASAeroExport.error9", "RASAeroExport.error25" })
	void formatsStageErrorsWithTheirOriginalArgumentOrder(String key) {
		String message = String.format(Locale.ROOT, translator.get(key), 3, "\u6d4b\u8bd5\u7ea7\u6bb5");

		assertTrue(message.contains("\u6d4b\u8bd5\u7ea7\u6bb5"));
		assertTrue(message.contains("3"));
	}

	@Test
	void keepsTransitionAndStageNamesInTheirRespectiveRoles() {
		String message = String.format(Locale.ROOT, translator.get("RASAeroExport.error14"),
				"Transition", "Booster", "BodyTube");

		assertTrue(message.contains("\u7b2c 'Booster' \u7ea7"));
		assertTrue(message.contains("\u8fc7\u6e21\u6bb5 'Transition'"));
		assertTrue(message.contains("'BodyTube'"));
	}

	@Test
	void formatsTransitionErrorWithBothRadii() {
		String message = String.format(Locale.ROOT, translator.get("RASAeroExport.error28"),
				"\u6d4b\u8bd5\u8fc7\u6e21\u6bb5", 0.02, 0.01);

		assertTrue(message.contains("\u6d4b\u8bd5\u8fc7\u6e21\u6bb5"));
		assertTrue(message.contains("0.020000"));
		assertTrue(message.contains("0.010000"));
	}
}
