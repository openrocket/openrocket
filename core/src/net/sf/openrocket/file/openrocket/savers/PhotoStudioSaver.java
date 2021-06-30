package net.sf.openrocket.file.openrocket.savers;

import net.sf.openrocket.document.PhotoSettings;
import net.sf.openrocket.util.Color;

import java.util.ArrayList;
import java.util.List;

public class PhotoStudioSaver {
    public static List<String> getElements(PhotoSettings p) {
        List<String> elements = new ArrayList<String>();

        elements.add("<photostudio>");

        elements.add("<roll>" + p.getRoll() + "</roll>");
        elements.add("<yaw>" + p.getYaw() + "</yaw>");
        elements.add("<pitch>" + p.getPitch() + "</pitch>");
        elements.add("<advance>" + p.getAdvance() + "</advance>");

        elements.add("<viewAlt>" + p.getViewAlt() + "</viewAlt>");
        elements.add("<viewAz>" + p.getViewAz() + "</viewAz>");
        elements.add("<viewDistance>" + p.getViewDistance() + "</viewDistance>");
        elements.add("<fov>" + p.getFov() + "</fov>");

        elements.add("<lightAlt>" + p.getLightAlt() + "</lightAlt>");
        elements.add("<lightAz>" + p.getLightAz() + "</lightAz>");
        emitColor("sunlight", elements, p.getSunlight());
        elements.add("<ambiance>" + p.getAmbiance() + "</ambiance>");

        emitColor("skyColor", elements, p.getSkyColor());

        elements.add("<motionBlurred>" + p.isMotionBlurred() + "</motionBlurred>");
        elements.add("<flame>" + p.isFlame() + "</flame>");
        emitColor("flameColor", elements, p.getFlameColor());
        elements.add("<smoke>" + p.isSmoke() + "</smoke>");
        emitColor("smokeColor", elements, p.getSmokeColor());
        elements.add("<smokeOpacity>" + p.getSmokeOpacity() + "</smokeOpacity>");
        elements.add("<sparks>" + p.isSparks() + "</sparks>");
        elements.add("<exhaustScale>" + p.getExhaustScale() + "</exhaustScale>");
        elements.add("<flameAspectRatio>" + p.getFlameAspectRatio() + "</flameAspectRatio>");

        elements.add("<sparkConcentration>" + p.getSparkConcentration() + "</sparkConcentration>");
        elements.add("<sparkWeight>" + p.getSparkWeight() + "</sparkWeight>");

        if (p.getSky() != null) {
            elements.add("<sky>" + p.getSky().getClass().getName() + "</sky>");
        }
        else
            elements.add("<sky></sky>");

        elements.add("</photostudio>");

        return elements;
    }

    private final static void emitColor(String elementName, List<String> elements, Color color) {
        if (color != null) {
            elements.add("<" + elementName + " red=\"" + color.getRed() + "\" green=\"" + color.getGreen()
                    + "\" blue=\"" + color.getBlue() + "\" alpha=\"" + color.getAlpha() + "\"/>");
        }
    }
}
