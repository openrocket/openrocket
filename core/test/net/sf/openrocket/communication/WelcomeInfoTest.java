package net.sf.openrocket.communication;

import net.sf.openrocket.util.BaseTestCase.BaseTestCase;
import net.sf.openrocket.util.BuildProperties;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WelcomeInfoTest extends BaseTestCase {

    @Test
    public void testWelcomeInfo() throws Exception {
        // Test the welcome info for the current build version
        String version = BuildProperties.getVersion();
        if (!version.contains(UpdateInfoRetriever.snapshotTag)) {      // Ignore snapshot releases; they don't need release notes
            String welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo();
            assertNotNull("Current release version not present in release notes", welcomeInfo);
            assertFalse("Body of release notes is empty", welcomeInfo.isEmpty());
        }

        // Test the release info for a bogus release version
        String welcomeInfo = WelcomeInfoRetriever.retrieveWelcomeInfo("bogus release");
        assertNull(welcomeInfo);
    }
}
