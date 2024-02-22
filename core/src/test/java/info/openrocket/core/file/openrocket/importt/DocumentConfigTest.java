package info.openrocket.core.file.openrocket.importt;

import static org.junit.Assert.assertEquals;
import info.openrocket.core.file.openrocket.OpenRocketSaver;
import info.openrocket.core.util.BaseTestCase;

import org.junit.Test;

public class DocumentConfigTest extends BaseTestCase {

	@Test
	public void testFileVersionDivisor() {
		assertEquals(OpenRocketSaver.FILE_VERSION_DIVISOR, DocumentConfig.FILE_VERSION_DIVISOR);
	}

}
