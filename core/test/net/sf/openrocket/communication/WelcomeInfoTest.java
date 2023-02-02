package net.sf.openrocket.communication;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WelcomeInfoTest extends BaseTestCase {

    @Test
    public void testWelcomeInfo() throws Exception {
        // Test the welcome info for the current build version
        String welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo();
        assertNotNull("Current release version not present in release notes", welcomeInfo);
        assertTrue("Body of release notes is empty", welcomeInfo.length() > 0);

        // Test the release info for a bogus release version
        welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo("bogus release");
        assertNull(welcomeInfo);
    }
}
