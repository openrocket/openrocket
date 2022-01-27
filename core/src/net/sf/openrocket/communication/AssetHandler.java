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
    private static final Map<String, Platform> mapExtensionToOS = new HashMap<>();  // Map file extensions to operating system
    private static final Map<Platform, String> mapOSToName = new HashMap<>();       // Map operating system to a name
    static {
        mapExtensionToOS.put(".dmg", Platform.MAC_OS);
        mapExtensionToOS.put(".exe", Platform.WINDOWS);
        mapExtensionToOS.put(".AppImage", Platform.UNIX);
        mapExtensionToOS.put(".jar", null);

        mapOSToName.put(Platform.MAC_OS, "Mac OS");
        mapOSToName.put(Platform.WINDOWS, "Windows");
        mapOSToName.put(Platform.UNIX, "Linux");
        mapOSToName.put(null, "JAR");
    }

    /**
     * Maps a list of asset URLs to their respective operating system.
     * E.g. "https://github.com/openrocket/openrocket/releases/download/release-15.03/OpenRocket-15.03.dmg" is mapped to
     * "Mac OS".
     * @param urls list of asset URLs
     * @return map with as key the operating system and as value the corresponding asset URL
     */
    public static Map<String, String> mapURLToOSName(List<String> urls) {
        Map<String, String> output = new TreeMap<>();
        if (urls == null) return null;

        for (String url : urls) {
            for (String ext : mapExtensionToOS.keySet()) {
                if (url.endsWith(ext)) {
                    output.put(mapOSToName.get(mapExtensionToOS.get(ext)), url);
                }
            }
        }
        return output;
    }

    /**
     * Returns the OS name based on the operating system that the user is running on, or the value stored in preferences.
     * @return operating system name
     */
    public static String getOSName() {
        Platform currentPlatform = SystemInfo.getPlatform();
        // TODO: select right option based on preference
        return mapOSToName.get(currentPlatform);
    }
}
