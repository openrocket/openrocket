package net.sf.openrocket.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestScriptingUtil {
	
	/*
	 * Note:  This class assumes that the JRE supports JavaScript scripting.
	 */
	
	@Test
	public void testGetLanguage() {
		assertEquals(null, ScriptingUtil.getLanguage(null));
		assertEquals(null, ScriptingUtil.getLanguage(""));
		assertEquals(null, ScriptingUtil.getLanguage("foobar"));
		assertEquals("JavaScript", ScriptingUtil.getLanguage("JavaScript"));
		assertEquals("JavaScript", ScriptingUtil.getLanguage("javascript"));
		assertEquals("JavaScript", ScriptingUtil.getLanguage("ECMAScript"));
		assertEquals("JavaScript", ScriptingUtil.getLanguage("js"));
	}
	
	
	@Test
	public void testGetLanguages() {
		assertTrue(ScriptingUtil.getLanguages().size() >= 1);
		assertTrue(ScriptingUtil.getLanguages().contains("JavaScript"));
	}
	
}
