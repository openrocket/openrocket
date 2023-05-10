package net.sf.openrocket.file.openrocket.savers;

import net.sf.openrocket.util.Color;

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
        List<String> elements = new ArrayList<String>();

        if (photoSettings == null || photoSettings.size() == 0) return elements;

        elements.add("<roll>" + photoSettings.get("roll") + "</roll>");
        elements.add("<yaw>" + photoSettings.get("yaw") + "</yaw>");
        elements.add("<pitch>" + photoSettings.get("pitch") + "</pitch>");
        elements.add("<advance>" + photoSettings.get("advance") + "</advance>");

        elements.add("<viewAlt>" + photoSettings.get("viewAlt") + "</viewAlt>");
        elements.add("<viewAz>" + photoSettings.get("viewAz") + "</viewAz>");
        elements.add("<viewDistance>" + photoSettings.get("viewDistance") + "</viewDistance>");
        elements.add("<fov>" + photoSettings.get("fov") + "</fov>");

        elements.add("<lightAlt>" + photoSettings.get("lightAlt") + "</lightAlt>");
        elements.add("<lightAz>" + photoSettings.get("lightAz") + "</lightAz>");
        emitColor("sunlight", elements, photoSettings.get("sunlight"));
        elements.add("<ambiance>" + photoSettings.get("ambiance") + "</ambiance>");

        emitColor("skyColor", elements, photoSettings.get("skyColor"));

        elements.add("<motionBlurred>" + photoSettings.get("motionBlurred") + "</motionBlurred>");
        elements.add("<flame>" + photoSettings.get("flame") + "</flame>");
        emitColor("flameColor", elements, photoSettings.get("flameColor"));
        elements.add("<smoke>" + photoSettings.get("smoke") + "</smoke>");
        emitColor("smokeColor", elements, photoSettings.get("smokeColor"));
        elements.add("<sparks>" + photoSettings.get("sparks") + "</sparks>");
        elements.add("<exhaustScale>" + photoSettings.get("exhaustScale") + "</exhaustScale>");
        elements.add("<flameAspectRatio>" + photoSettings.get("flameAspectRatio") + "</flameAspectRatio>");

        elements.add("<sparkConcentration>" + photoSettings.get("sparkConcentration") + "</sparkConcentration>");
        elements.add("<sparkWeight>" + photoSettings.get("sparkWeight") + "</sparkWeight>");

        elements.add("<sky>" + photoSettings.get("sky") + "</sky>");

        return elements;
    }

    private static Color getColor(String content) {
        if (content == null) return null;
        String[] values = content.split(" ");
        if (values.length < 4) return null;

        int red = Integer.parseInt(values[0]);
        int green = Integer.parseInt(values[1]);
        int blue = Integer.parseInt(values[2]);
        int alpha = Integer.parseInt(values[3]);
        return new Color(red, green, blue, alpha);
    }

    private static void emitColor(String elementName, List<String> elements, String content) {
        Color color = getColor(content);
        if (color != null) {
            elements.add("<" + elementName + " red=\"" + color.getRed() + "\" green=\"" + color.getGreen()
                    + "\" blue=\"" + color.getBlue() + "\" alpha=\"" + color.getAlpha() + "\"/>");
        }
        else
            elements.add(String.format("<%s></%s>", elementName));
    }
}
