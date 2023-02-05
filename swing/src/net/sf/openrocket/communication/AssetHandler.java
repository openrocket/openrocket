package net.sf.openrocket.communication;

import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BuildProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
            for (String ext : mapExtensionToPlatform.keySet()) {
                if (url.endsWith(ext)) {
                    output.put(mapExtensionToPlatform.get(ext)[0], url);    // First Platform element is enough
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
        for (UpdatePlatform[] platforms : mapPlatformToURL.keySet()) {
            for (UpdatePlatform p : platforms) {
                if (p == platform) {
                    return String.format(mapPlatformToURL.get(platforms), version);
                }
            }
        }
        return null;
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
