package net.sf.openrocket.communication;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.SimpleSAX;
import net.sf.openrocket.util.BuildProperties;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class WelcomeInfoRetriever {
    /**
     * Retrieves the release notes of the current build version from the ReleaseNotes.md file.
     * @param version the build version to search the release notes for (e.g. "22.02")
     * @return the release notes of the current build version
     * @throws IOException if the file could not be read
     */
    public static String retrieveWelcomeInfo(String version) throws IOException {
        ClassLoader cl = WelcomeInfoRetriever.class.getClassLoader();
        InputStream in = cl.getResourceAsStream("ReleaseNotes.md");
        if (in == null) {
            // Try to load the file from the file system (only really useful when running the unit tests directly from your IDE)
            File f = new File("../ReleaseNotes.md");
            in = f.toURI().toURL().openStream();
            if (in == null) {
                throw new FileNotFoundException("ReleaseNotes.md not found");
            }
        }
        InputSource source = new InputSource(new InputStreamReader(in));
        ReleaseNotesHandler handler = new ReleaseNotesHandler(version);
        WarningSet warnings = new WarningSet();

        try {
            SimpleSAX.readXML(source, handler, warnings);
            return handler.getReleaseNotes();
        } catch (SAXException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the release notes of the current build version from the ReleaseNotes.md file.
     * @return the release notes of the current build version
     * @throws IOException if the file could not be read
     */
    public static String retrieveWelcomeInfo() throws IOException {
        return retrieveWelcomeInfo(BuildProperties.getVersion());
    }
}
