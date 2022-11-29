package net.sf.openrocket.communication;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.SimpleSAX;
import net.sf.openrocket.util.BuildProperties;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class WelcomeInfoRetriever {

    /**
     * Retrieves the release notes of the current build version from the ReleaseNotes.md file.
     * @return the release notes of the current build version
     * @throws IOException if the file could not be read
     */
    public static String retrieveWelcomeInfo() throws IOException {
        InputStream in = new FileInputStream("ReleaseNotes.md");
        InputSource source = new InputSource(new InputStreamReader(in));
        ReleaseNotesHandler handler = new ReleaseNotesHandler(BuildProperties.getVersion());
        WarningSet warnings = new WarningSet();

        try {
            SimpleSAX.readXML(source, handler, warnings);
            return handler.getReleaseNotes();
        } catch (SAXException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
