package net.sf.openrocket.communication;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Objects;

/**
 * Class that parses ReleaseNotes.md.
 * <p>
 * Releases are stored in an HTML <div> object with as id attribute the release version.
 * E.g. <div id="15.03">...</div>
 * The content of the div is the release notes for that version.
 */
public class ReleaseNotesHandler extends AbstractElementHandler {
    private final String buildVersion;
    private String releaseNotes = null;

    /**
     * @param buildVersion the build version to search the release notes for (e.g. "22.02")
     */
    public ReleaseNotesHandler(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        if (element.equals("body")) {       // The release notes are encapsulated in a root <body> tag (required for XML parsing)
            return this;
        }
        if (element.equals("div") && Objects.equals(attributes.get("id"), this.buildVersion)) {
            return PlainTextHandler.INSTANCE;
        }
        return null;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        super.closeElement(element, attributes, content, warnings);

        if (element.equals("div")) {
            this.releaseNotes = content.trim();
        }
    }

    /**
     * @return the release notes for the build version
     */
    public String getReleaseNotes() {
        return releaseNotes;
    }
}
