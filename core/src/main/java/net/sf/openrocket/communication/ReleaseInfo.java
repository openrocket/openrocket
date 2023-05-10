package net.sf.openrocket.communication;

import com.sun.istack.NotNull;
import net.sf.openrocket.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.List;
import java.util.Objects;

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

    public ReleaseInfo(@NotNull JsonObject obj) {
        this.obj = Objects.requireNonNull(obj, "JsonObject cannot be null");
    }

    /**
     * Get the release name from the GitHub release JSON object.
     * @return release name (e.g. "15.0.3")
     */
    public String getReleaseName() {
        String name = this.obj.get("tag_name").toString();           // Release label is encapsulated in the 'tag_name'-tag
        name = name.replaceAll("^\"+|\"+$", "");    // Remove double quotations in the beginning and end

        // Remove the 'release-' preamble of the name (example name: 'release-15.03')
        String preamble = "release-";
        if (name.startsWith(preamble)) {
            name = name.substring(preamble.length());
        } else {
            log.debug("Invalid release tag format for release: " + name);
        }
        return name;
    }

    /**
     * Get the release notes from the GitHub release JSON object.
     * @return release notes (this is the text that explains a certain GitHub release)
     */
    public String getReleaseNotes() {
        String releaseNotes = this.obj.get("body").toString();
        releaseNotes = releaseNotes.replaceAll("^\"+|\"+$", "");    // Remove double quotations in the beginning and end
        return releaseNotes;
    }

    /**
     * Get the release URL from the GitHub release JSON object.
     * @return release URL (e.g. 'https://github.com/openrocket/openrocket/releases/tag/release-15.03')
     */
    public String getReleaseURL() {
        String releaseURL = this.obj.get("html_url").toString();
        releaseURL = releaseURL.replaceAll("^\"+|\"+$", "");    // Remove double quotations in the beginning and end
        return releaseURL;
    }

    /**
     * Get the download URLs of the assets from the GitHub release JSON object.
     * @return list of asset download URLs (e.g. 'https://github.com/openrocket/openrocket/releases/download/release-15.03/OpenRocket-15.03-installer.exe')
     */
    public List<String> getAssetURLs() {
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
        return String.format("releaseTag = %s ; releaseNotes = %s ; releaseURL = %s", getReleaseName(), getReleaseNotes(),
                getReleaseURL());
    }
}
