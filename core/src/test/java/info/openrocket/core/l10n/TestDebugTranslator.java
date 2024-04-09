package info.openrocket.core.l10n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestDebugTranslator {

	@Test
	public void testGetEnglish() {
		DebugTranslator trans = new DebugTranslator(null);
		assertEquals(trans.get("material", "Paper (office)"), "[material:Paper (office)]");
	}

	@Test
	public void testGetBase() {
		DebugTranslator trans = new DebugTranslator(null);
		assertEquals(trans.getBaseText("material", "[material:Paper (office)]"), "Paper (office)");
		assertEquals(trans.getBaseText("material", "Papier (toilet)"), "Papier (toilet)");
	}

}
