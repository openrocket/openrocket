package net.sf.openrocket.file.openrocket.importt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class DocumentConfigTest extends BaseTestCase {
	
	@Test
	public void testFileVersionDivisor() {
		assertEquals(OpenRocketSaver.FILE_VERSION_DIVISOR, DocumentConfig.FILE_VERSION_DIVISOR);
	}
	
}
