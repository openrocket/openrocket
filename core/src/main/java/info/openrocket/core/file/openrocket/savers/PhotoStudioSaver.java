package info.openrocket.core.file.openrocket.savers;

import info.openrocket.core.util.ORColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class takes in the photo settings map from the swing module and converts it into the xml format
 * needed to save it in the OpenRocketDocument.
 * (this cumbersome solution is done because of dependency reasons
 * between files of the core and swing module; trying to just use PhotoSettings objects in the
 * core module would have caused circular dependencies)
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class PhotoStudioSaver {
    public static List<String> getElements(Map<String, String> photoSettings) {
        List<String> elements = new ArrayList<>();

        if (photoSettings == null || photoSettings.size() == 0)
            return elements;

        emitValue("roll", elements, photoSettings.get("roll"));
        emitValue("yaw", elements, photoSettings.get("yaw"));
        emitValue("pitch", elements, photoSettings.get("pitch"));
        emitValue("advance", elements, photoSettings.get("advance"));

        emitValue("viewAlt", elements, photoSettings.get("viewAlt"));
        emitValue("viewAz", elements, photoSettings.get("viewAz"));
        emitValue("viewDistance", elements, photoSettings.get("viewDistance"));
        emitValue("fov", elements, photoSettings.get("fov"));

        emitValue("lightAlt", elements, photoSettings.get("lightAlt"));
        emitValue("lightAz", elements, photoSettings.get("lightAz"));
        emitValue("lightStrength", elements, photoSettings.get("lightStrength"));
        emitColor("sunlight", elements, photoSettings.get("sunlight"));
        emitValue("ambiance", elements, photoSettings.get("ambiance"));

        emitColor("skyColor", elements, photoSettings.get("skyColor"));
        emitValue("backgroundType", elements, photoSettings.get("backgroundType"));
        emitColor("gradientTopColor", elements, photoSettings.get("gradientTopColor"));
        emitColor("gradientBottomColor", elements, photoSettings.get("gradientBottomColor"));

        emitValue("motionBlurred", elements, photoSettings.get("motionBlurred"));
        emitValue("motionBlurAmount", elements, photoSettings.get("motionBlurAmount"));
        emitValue("flame", elements, photoSettings.get("flame"));
        emitColor("flameColor", elements, photoSettings.get("flameColor"));
        emitValue("smoke", elements, photoSettings.get("smoke"));
        emitColor("smokeColor", elements, photoSettings.get("smokeColor"));
        emitValue("sparks", elements, photoSettings.get("sparks"));
        emitValue("exhaustScale", elements, photoSettings.get("exhaustScale"));
        emitValue("flameAspectRatio", elements, photoSettings.get("flameAspectRatio"));

        emitValue("sparkConcentration", elements, photoSettings.get("sparkConcentration"));
        emitValue("sparkWeight", elements, photoSettings.get("sparkWeight"));

        emitValue("sky", elements, photoSettings.get("sky"));

        return elements;
    }

    private static ORColor getColor(String content) {
        if (content == null)
            return null;
        String[] values = content.split(" ");
        if (values.length < 4)
            return null;

        int red = Integer.parseInt(values[0]);
        int green = Integer.parseInt(values[1]);
        int blue = Integer.parseInt(values[2]);
        int alpha = Integer.parseInt(values[3]);
        return new ORColor(red, green, blue, alpha);
    }

    private static void emitColor(String elementName, List<String> elements, String content) {
        if (content == null) {
            return;
        }
        ORColor color = getColor(content);
        if (color != null) {
            elements.add("<" + elementName + " " + color.toXMLAttributes() + "/>");
        } else
            elements.add(String.format("<%s></%s>", elementName, elementName));
    }

    private static void emitValue(String elementName, List<String> elements, String content) {
        if (content != null) {
            elements.add(String.format("<%s>%s</%s>", elementName, content, elementName));
        }
    }
}
