package info.openrocket.core.communication;

import info.openrocket.core.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WelcomeInfoTest extends BaseTestCase {

    @Test
    public void testWelcomeInfo() throws Exception {
        // Test the welcome info for the current build version
        String welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo();
        assertNotNull(welcomeInfo, "Current release version not present in release notes");
        assertTrue(welcomeInfo.length() > 0, "Body of release notes is empty");

        // Test the release info for a bogus release version
        welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo("bogus release");
        assertNull(welcomeInfo);
    }
}
