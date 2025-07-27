package info.openrocket.swing.communication;

import info.openrocket.core.communication.UpdateInfoRetriever;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.core.startup.Application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles assets extracted from a GitHub release page.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class AssetHandler {
    private static final Map<String, UpdatePlatform[]> mapExtensionToPlatform = new HashMap<>(); // Map file extensions to operating platform
    private static final Map<UpdatePlatform[], String> mapPlatformToURL= new HashMap<>();        // Map operating platform to a URL to download the release for that OS
    private static final Map<UpdatePlatform, String> mapPlatformToName = new HashMap<>();        // Map operating platform to a name

    public enum UpdatePlatform {
        WINDOWS,
        MAC_OS,
        LINUX,
        UNIX,
        JAR
    }

    static {
        mapExtensionToPlatform.put(".dmg", new UpdatePlatform[] {UpdatePlatform.MAC_OS});
        mapExtensionToPlatform.put(".exe", new UpdatePlatform[] {UpdatePlatform.WINDOWS});
        mapExtensionToPlatform.put(".AppImage", new UpdatePlatform[] {UpdatePlatform.LINUX, UpdatePlatform.UNIX});
        mapExtensionToPlatform.put(".sh", new UpdatePlatform[] {UpdatePlatform.LINUX, UpdatePlatform.UNIX});
        mapExtensionToPlatform.put(".jar", new UpdatePlatform[] {UpdatePlatform.JAR});

        String baseURL = "https://openrocket.info/downloads.html?vers=%s#content-";
        mapPlatformToURL.put(new UpdatePlatform[] {UpdatePlatform.MAC_OS}, baseURL + "macOS");
        mapPlatformToURL.put(new UpdatePlatform[] {UpdatePlatform.WINDOWS}, baseURL + "Windows");
        mapPlatformToURL.put(new UpdatePlatform[] {UpdatePlatform.LINUX, UpdatePlatform.UNIX}, baseURL + "Linux");
        mapPlatformToURL.put(new UpdatePlatform[] {UpdatePlatform.JAR}, baseURL + "JAR");

        mapPlatformToName.put(UpdatePlatform.MAC_OS, "macOS");
        mapPlatformToName.put(UpdatePlatform.WINDOWS, "Windows");
        mapPlatformToName.put(UpdatePlatform.LINUX, "Linux");
        mapPlatformToName.put(UpdatePlatform.UNIX, "Linux");
        mapPlatformToName.put(UpdatePlatform.JAR, "JAR");
    }

    /**
     * Maps a list of asset URLs to their respective operating platform name.
     * E.g. "https://github.com/openrocket/openrocket/releases/download/release-15.03/OpenRocket-15.03.dmg" is mapped a
     * map element with "macOS" as key and the url as value.
     * @param urls list of asset URLs from the GitHub release object
     * @return map with as key the operating platform name and as value the corresponding asset URL
     */
    public static Map<UpdatePlatform, String> mapURLToPlatform(List<String> urls) {
        Map<UpdatePlatform, String> output = new HashMap<>();
        if (urls == null) return null;

        for (String url : urls) {
            for (Map.Entry<String, UpdatePlatform[]> entry : mapExtensionToPlatform.entrySet()) {
                if (url.endsWith(entry.getKey())) {
                    output.put(entry.getValue()[0], url);    // First Platform element is enough
                }
            }
        }
        return output;
    }

    /**
     * Returns the URL to download the installer for the given platform.
     * @param platform platform to get the installer URL for
     * @param version version of the installer to download
     * @return URL to download the installer for the given platform
     */
    public static String getInstallerURLForPlatform(UpdatePlatform platform, String version) {
        // If it is not an official release, use the GitHub download link
        if (UpdateInfoRetriever.UpdateInfoFetcher.isOfficialRelease(version)) {
            return getWebsiteDownloadURL(platform, version);
        } else {
            return getGitHubDownloadURL(version);
        }


    }

    /**
     * Returns the URL to download the installer for the given version from the OpenRocket website.
     * @param platform platform to get the installer URL for; or null to get to the general download page
     * @param version version of the installer to download
     * @return URL to download the installer for the given version
     */
    private static String getWebsiteDownloadURL(UpdatePlatform platform, String version) {
        // If the platform is null, return the general download URL
        if (platform == null) {
            return String.format("https://openrocket.info/downloads.html?vers=%s", version);
        }

        for (Map.Entry<UpdatePlatform[], String> entry : mapPlatformToURL.entrySet()) {
            for (UpdatePlatform p : entry.getKey()) {
                if (p == platform) {
                    return String.format(entry.getValue(), version);
                }
            }
        }
        return null;
    }

    /**
     * Returns the URL to download the installer for the given version from the GitHub releases page.
     * @param version version of the installer to download
     * @return URL to download the installer for the given version
     */
    private static String getGitHubDownloadURL(String version) {
        return String.format("https://github.com/openrocket/openrocket/releases/tag/release-%s", version);
    }

    /**
     * Returns the operating platform based on the operating system that the user is running on, or the value
     * stored in preferences.
     * @return operating platform
     */
    public static UpdatePlatform getUpdatePlatform() {
        return ((SwingPreferences) Application.getPreferences()).getUpdatePlatform();
    }

    /**
     * Get the name of a platform (e.g. for Platform.MAC_OS, return "macOS")
     * @param platform platform to get the name from
     * @return name of the platform
     */
    public static String getPlatformName(UpdatePlatform platform) {
        return mapPlatformToName.get(platform);
    }
}
