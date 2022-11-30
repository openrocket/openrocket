package net.sf.openrocket.communication;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WelcomeInfoTest extends BaseTestCase {

    /**
     * Note: this unit test will fail if you don't run it using 'ant unittest', because otherwise
     * it can't load the ReleaseNotes.md file.
     */
    @Test
    public void testWelcomeInfo() throws Exception {
        // Test the welcome info for the current build version
        String welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo();
        assertNotNull(welcomeInfo);
        assertTrue(welcomeInfo.length() > 0);

        // Test the release info for a bogus release version
        welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo("bogus release");
        assertNull(welcomeInfo);
    }
}
