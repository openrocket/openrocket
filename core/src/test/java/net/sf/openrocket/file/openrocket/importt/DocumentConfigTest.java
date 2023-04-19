// thzero
package net.sf.openrocket.file.openrocket.importt;

import static org.junit.Assert.assertEquals;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
// thzero - begin
import net.sf.openrocket.util.BaseTestCase;
// thzero - end

import org.junit.Test;

public class DocumentConfigTest extends BaseTestCase {
	
	@Test
	public void testFileVersionDivisor() {
		assertEquals(OpenRocketSaver.FILE_VERSION_DIVISOR, DocumentConfig.FILE_VERSION_DIVISOR);
	}
	
}
