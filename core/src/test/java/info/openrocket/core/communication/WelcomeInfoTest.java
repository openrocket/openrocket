package info.openrocket.core.communication;

import info.openrocket.core.util.BaseTestCase;
import info.openrocket.core.util.BuildProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class WelcomeInfoTest extends BaseTestCase {

    @Test
    public void testWelcomeInfo() throws Exception {
		// Test the welcome info for the current build version
		String version = BuildProperties.getVersion();
		if (!version.contains(UpdateInfoRetriever.snapshotTag)) {      // Ignore snapshot releases; they don't need release notes
			String welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo();
			assertNotNull(welcomeInfo, "Current release version not present in release notes");
			assertFalse(welcomeInfo.isEmpty(), "Body of release notes is empty");
		}

		// Test the release info for a bogus release version
		String welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo("bogus release");
		assertNull(welcomeInfo);
    }
}
