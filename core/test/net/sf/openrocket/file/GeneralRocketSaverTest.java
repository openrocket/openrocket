package net.sf.openrocket.file;

import java.text.MessageFormat;

import org.junit.Test;

import static org.junit.Assert.*;

public class GeneralRocketSaverTest {

	@Test
	public void testBuildFilenameTemplate() {
		
		assertEquals("abc ({0}).jpg", GeneralRocketSaver.buildFilenameTemplate("abc.jpg"));
		
		assertEquals("a ({0}).jpg", GeneralRocketSaver.buildFilenameTemplate("a.jpg"));
		
		assertEquals("abc ({0})", GeneralRocketSaver.buildFilenameTemplate("abc"));
		
		assertEquals("abc ({0}).", GeneralRocketSaver.buildFilenameTemplate("abc."));
		
		assertEquals(".abc ({0})", GeneralRocketSaver.buildFilenameTemplate(".abc"));
		
		// This one is hideous, but hey so was the original name.
		// If anybody wants to change the algorithm when there are multiple leading dots
		// go ahead and change these test cases too.
		assertEquals(". ({0}).abc", GeneralRocketSaver.buildFilenameTemplate("..abc"));
		
		assertEquals( "abc (15).jpg", MessageFormat.format(GeneralRocketSaver.buildFilenameTemplate("abc.jpg"), 15));
	}
}
