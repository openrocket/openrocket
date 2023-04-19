package net.sf.openrocket.l10n;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestDebugTranslator {
	
	@Test
	public void testGetEnglish() {
		DebugTranslator trans = new DebugTranslator(null);
		assertEquals("[material:Paper (office)]", trans.get("material", "Paper (office)"));
	}
	
	
	@Test
	public void testGetBase() {
		DebugTranslator trans = new DebugTranslator(null);
		assertEquals("Paper (office)", trans.getBaseText("material", "[material:Paper (office)]"));
		assertEquals("Papier (toilet)", trans.getBaseText("material", "Papier (toilet)"));
	}
	
	
}
