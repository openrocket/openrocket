package net.sf.openrocket.file.openrocket.importt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.util.BaseTestCase.BaseTestCase;

import org.junit.Test;

public class DocumentConfigTest extends BaseTestCase {
	
	/**
	 * Check that unit tests exist for all supported OR file versions.
	 * 
	 * This test is here to remind future developers to update the unit tests after adding a file version.
	 * 
	 * Whenever a new file version is created, this test needs to be updated after new unit tests 
	 * are created in OpenRocketSaver.java for the new file version.  
	 */
	@Test
	public void testAllVersionsTested() {
		
		// Update this after creating new unit tests in OpenRocketSaver for a new OR file version
		String[] testedVersionsStr = { "1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7" };
		
		List<String> supportedVersions = Arrays.asList(DocumentConfig.SUPPORTED_VERSIONS);
		List<String> testedVersions = Arrays.asList(testedVersionsStr);
		
		for (String supportedVersion : supportedVersions) {
			String msg = String.format("No unit tests exist for OpenRocket file version %s", supportedVersion);
			assertTrue(msg, testedVersions.contains(supportedVersion));
		}
	}
	
	@Test
	public void testFileVersionDivisor() {
		assertEquals(OpenRocketSaver.FILE_VERSION_DIVISOR, DocumentConfig.FILE_VERSION_DIVISOR);
	}
	
}
