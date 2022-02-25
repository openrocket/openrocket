package net.sf.openrocket.communication;

import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.startup.Application;

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
    private static final Map<String, UpdatePlatform> mapExtensionToPlatform = new HashMap<>();  // Map file extensions to operating platform
    private static final Map<UpdatePlatform, String> mapPlatformToName = new HashMap<>();       // Map operating platform to a name

    public enum UpdatePlatform {
        WINDOWS,
        MAC_OS,
        LINUX,
        JAR
    }

    static {
        mapExtensionToPlatform.put(".dmg", UpdatePlatform.MAC_OS);
        mapExtensionToPlatform.put(".exe", UpdatePlatform.WINDOWS);
        mapExtensionToPlatform.put(".AppImage", UpdatePlatform.LINUX);
        mapExtensionToPlatform.put(".sh", UpdatePlatform.LINUX);
        mapExtensionToPlatform.put(".jar", UpdatePlatform.JAR);

        mapPlatformToName.put(UpdatePlatform.MAC_OS, "Mac OS");
        mapPlatformToName.put(UpdatePlatform.WINDOWS, "Windows");
        mapPlatformToName.put(UpdatePlatform.LINUX, "Linux");
        mapPlatformToName.put(UpdatePlatform.JAR, "JAR");
    }

    /**
     * Maps a list of asset URLs to their respective operating platform name.
     * E.g. "https://github.com/openrocket/openrocket/releases/download/release-15.03/OpenRocket-15.03.dmg" is mapped a
     * map element with "Mac OS" as key and the url as value.
     * @param urls list of asset URLs
     * @return map with as key the operating platform name and as value the corresponding asset URL
     */
    public static Map<UpdatePlatform, String> mapURLToPlatform(List<String> urls) {
        Map<UpdatePlatform, String> output = new TreeMap<>();
        if (urls == null) return null;

        for (String url : urls) {
            for (String ext : mapExtensionToPlatform.keySet()) {
                if (url.endsWith(ext)) {
                    output.put(mapExtensionToPlatform.get(ext), url);
                }
            }
        }
        return output;
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
     * Get the name of a platform (e.g. for Platform.MAC_OS, return "Mac OS")
     * @param platform platform to get the name from
     * @return name of the platform
     */
    public static String getPlatformName(UpdatePlatform platform) {
        return mapPlatformToName.get(platform);
    }
}
