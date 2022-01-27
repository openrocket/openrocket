package net.sf.openrocket.communication;

import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.arch.SystemInfo.Platform;

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
    private static final Map<String, Platform> mapExtensionToPlatform = new HashMap<>();  // Map file extensions to operating platform
    private static final Map<Platform, String> mapPlatformToName = new HashMap<>();       // Map operating platform to a name
    static {
        mapExtensionToPlatform.put(".dmg", Platform.MAC_OS);
        mapExtensionToPlatform.put(".exe", Platform.WINDOWS);
        mapExtensionToPlatform.put(".AppImage", Platform.UNIX);
        mapExtensionToPlatform.put(".jar", null);

        mapPlatformToName.put(Platform.MAC_OS, "Mac OS");
        mapPlatformToName.put(Platform.WINDOWS, "Windows");
        mapPlatformToName.put(Platform.UNIX, "Linux");
        mapPlatformToName.put(null, "JAR");
    }

    /**
     * Maps a list of asset URLs to their respective operating platform name.
     * E.g. "https://github.com/openrocket/openrocket/releases/download/release-15.03/OpenRocket-15.03.dmg" is mapped a
     * map element with "Mac OS" as key and the url as value.
     * @param urls list of asset URLs
     * @return map with as key the operating platform name and as value the corresponding asset URL
     */
    public static Map<String, String> mapURLToPlatformName(List<String> urls) {
        Map<String, String> output = new TreeMap<>();
        if (urls == null) return null;

        for (String url : urls) {
            for (String ext : mapExtensionToPlatform.keySet()) {
                if (url.endsWith(ext)) {
                    output.put(mapPlatformToName.get(mapExtensionToPlatform.get(ext)), url);
                }
            }
        }
        return output;
    }

    /**
     * Returns the operating platform name based on the operating system that the user is running on, or the value
     * stored in preferences.
     * @return operating platform name
     */
    public static String getPlatformName() {
        Platform currentPlatform = SystemInfo.getPlatform();
        // TODO: select right option based on preference
        return mapPlatformToName.get(currentPlatform);
    }
}
