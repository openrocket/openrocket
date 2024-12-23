package info.openrocket.core.communication;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.file.simplesax.SimpleSAX;
import info.openrocket.core.util.BuildProperties;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class WelcomeInfoRetriever {
    private static final String RELEASE_NOTES_FILENAME = "ReleaseNotes.md";

    /**
     * Retrieves the release notes of the current build version from the
     * ReleaseNotes.md file.
     *
     * @param version the build version to search the release notes for (e.g. "22.02")
     * @return the release notes of the current build version
     * @throws IOException if the file could not be read
     */
    public static String retrieveWelcomeInfo(String version) throws IOException {
        InputStream inputStream = null;

        // First, try to load from resources (for packaged application)
        inputStream = WelcomeInfoRetriever.class.getClassLoader().getResourceAsStream(RELEASE_NOTES_FILENAME);

        // If not found in resources, try to load from project root directory
        if (inputStream == null) {
            // Get the current path
            Path currentPath = Paths.get("").toAbsolutePath();
            File releaseNotesFile = currentPath.resolve(RELEASE_NOTES_FILENAME).toFile();

            // If file not found, try one level up (for unit tests)
            if (!releaseNotesFile.exists()) {
                releaseNotesFile = currentPath.getParent().resolve(RELEASE_NOTES_FILENAME).toFile();
            }

            if (releaseNotesFile.exists()) {
                inputStream = new FileInputStream(releaseNotesFile);
            } else {
                throw new FileNotFoundException(
                        String.format("ReleaseNotes.md not found in resources or project root: %s",
                                releaseNotesFile.getAbsolutePath())
                );
            }
        }

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            InputSource source = new InputSource(reader);
            ReleaseNotesHandler handler = new ReleaseNotesHandler(version);
            WarningSet warnings = new WarningSet();

            SimpleSAX.readXML(source, handler, warnings);
            return handler.getReleaseNotes();
        } catch (SAXException e) {
            throw new IOException("Failed to parse release notes: " + e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Ignore close errors
                }
            }
        }
    }

    /**
     * Retrieves the release notes of the current build version from the
     * ReleaseNotes.md file.
     * 
     * @return the release notes of the current build version
     * @throws IOException if the file could not be read
     */
    public static String retrieveWelcomeInfo() throws IOException {
        return retrieveWelcomeInfo(BuildProperties.getVersion());
    }
}
