package net.sf.openrocket.gui.print;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestPaperSize {
	
	@Test
	public void testGetDefaultForCountry() {
		assertEquals(PaperSize.LETTER, PaperSize.getDefaultForCountry("US"));
		assertEquals(PaperSize.LETTER, PaperSize.getDefaultForCountry("cA"));
		assertEquals(PaperSize.LETTER, PaperSize.getDefaultForCountry("mx"));
		
		assertEquals(PaperSize.A4, PaperSize.getDefaultForCountry("FI"));
		assertEquals(PaperSize.A4, PaperSize.getDefaultForCountry("xy"));
		
		assertNull(PaperSize.getDefaultForCountry("FIN"));
		assertNull(PaperSize.getDefaultForCountry("a"));
		assertNull(PaperSize.getDefaultForCountry("A4"));
		assertNull(PaperSize.getDefaultForCountry(null));
	}
	
	@Test
	public void testGetSizeFromString() {
		assertEquals(PaperSize.LETTER, PaperSize.getSizeFromString("Letter"));
		assertEquals(PaperSize.LEGAL, PaperSize.getSizeFromString("  legal\t"));
		assertEquals(PaperSize.A4, PaperSize.getSizeFromString("  A4\n"));
		assertEquals(PaperSize.A3, PaperSize.getSizeFromString("A3"));
		
		assertNull(PaperSize.getSizeFromString("#A4"));
		assertNull(PaperSize.getSizeFromString(""));
		assertNull(PaperSize.getSizeFromString(null));
	}
	
}
