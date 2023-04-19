package net.sf.openrocket.file.photo;

import net.sf.openrocket.gui.figure3d.photo.PhotoSettings;
import net.sf.openrocket.util.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * This class converts the PhotoSettings to a Map that can be sent over to the core module for saving
 * into the OpenRocketDocument. The key of the map is the attribute name, the value is its content.
 * (this cumbersome solution is done because of dependency reasons
 * between files of the core and swing module; trying to just use PhotoSettings objects in the
 * core module would have caused circular dependencies)
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class PhotoStudioSetter {
    public static Map<String, String> getPhotoSettings(PhotoSettings p) {
        Map<String, String> photoSettings = new HashMap<>();

        photoSettings.put("roll", String.valueOf(p.getRoll()));
        photoSettings.put("yaw", String.valueOf(p.getYaw()));
        photoSettings.put("pitch", String.valueOf(p.getPitch()));
        photoSettings.put("advance", String.valueOf(p.getAdvance()));

        photoSettings.put("viewAlt", String.valueOf(p.getViewAlt()));
        photoSettings.put("viewAz", String.valueOf(p.getViewAz()));
        photoSettings.put("viewDistance", String.valueOf(p.getViewDistance()));
        photoSettings.put("fov", String.valueOf(p.getFov()));

        photoSettings.put("lightAlt", String.valueOf(p.getLightAlt()));
        photoSettings.put("lightAz", String.valueOf(p.getLightAz()));
        photoSettings.put("sunlight", getColor(p.getSunlight()));
        photoSettings.put("ambiance", String.valueOf(p.getAmbiance()));

        photoSettings.put("skyColor", getColor(p.getSkyColor()));

        photoSettings.put("motionBlurred", String.valueOf(p.isMotionBlurred()));
        photoSettings.put("flame", String.valueOf(p.isFlame()));
        photoSettings.put("flameColor", getColor(p.getFlameColor()));
        photoSettings.put("smoke", String.valueOf(p.isSmoke()));
        photoSettings.put("smokeColor", getColor(p.getSmokeColor()));
        photoSettings.put("sparks", String.valueOf(p.isSparks()));
        photoSettings.put("exhaustScale", String.valueOf(p.getExhaustScale()));
        photoSettings.put("flameAspectRatio", String.valueOf(p.getFlameAspectRatio()));

        photoSettings.put("sparkConcentration", String.valueOf(p.getSparkConcentration()));
        photoSettings.put("sparkWeight", String.valueOf(p.getSparkWeight()));

        if (p.getSky() != null)
            photoSettings.put("sky", p.getSky().getClass().getName());
        else
            photoSettings.put("sky", "");

        return photoSettings;
    }

    private static String getColor(Color color) {
        if (color == null) return "";
        return color.getRed() + " " + color.getGreen() + " " + color.getBlue() + " " + color.getAlpha();
    }
}
