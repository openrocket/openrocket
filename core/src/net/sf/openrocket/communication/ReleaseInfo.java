package net.sf.openrocket.communication;

import net.sf.openrocket.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;

/**
 * Class containing info about a GitHub release. All the info is stored in a JSON objects, retrieved using the GitHub
 * releases API.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class ReleaseInfo {
    // GitHub release JSON object containing all the information about a certain release
    // You can examine an example object here: https://api.github.com/repos/openrocket/openrocket/releases/latest
    private final JsonObject obj;

    private static final Logger log = LoggerFactory.getLogger(ReleaseInfo.class);

    public ReleaseInfo(JsonObject obj) {
        this.obj = obj;
    }

    /**
     * Get the release tag from the GitHub release JSON object.
     * @return release tag (e.g. "15.0.3")
     */
    public String getReleaseTag() {
        if (this.obj == null) return null;

        String tag = this.obj.get("tag_name").toString();        // Release label is encapsulated in the 'tag_name'-tag
        tag = tag.replaceAll("^\"+|\"+$", "");    // Remove double quotations in the beginning and end

        // Remove the 'release-' preamble of the name tag (example name tag: 'release-15.03')
        String preamble = "release-";
        if (tag.startsWith(preamble)) {
            tag = tag.substring(preamble.length());
        } else {
            log.debug("Invalid release tag format for tag: " + tag);
        }
        return tag;
    }

    /**
     * Get the release notes from the GitHub release JSON object.
     * @return release notes (this is the text that explains a certain GitHub release)
     */
    public String getReleaseNotes() {
        if (this.obj == null) return null;
        return this.obj.get("body").toString();
    }

    /**
     * Get the release URL from the GitHub release JSON object.
     * @return release URL (e.g. 'https://github.com/openrocket/openrocket/releases/tag/release-15.03')
     */
    public String getReleaseURL() {
        if (this.obj == null) return null;
        return this.obj.get("html_url").toString();
    }

    /**
     * Get the download URLs of the assets from the GitHub release JSON object.
     * @return list of asset download URLs (e.g. 'https://github.com/openrocket/openrocket/releases/download/release-15.03/OpenRocket-15.03-installer.exe')
     */
    public List<String> getAssetURLs() {
        if (this.obj == null) return null;
        List<String> assetURLs = new ArrayList<>();

        JsonArray assets = this.obj.getJsonArray("assets");
        for (int i = 0; i < assets.size(); i++) {
            String url = assets.getJsonObject(i).getString("browser_download_url");
            assetURLs.add(url);
        }

        return assetURLs;
    }

    @Override
    public String toString() {
        return String.format("releaseTag = %s ; releaseNotes = %s ; releaseURL = %s", getReleaseTag(), getReleaseNotes(),
                getReleaseURL());
    }
}
