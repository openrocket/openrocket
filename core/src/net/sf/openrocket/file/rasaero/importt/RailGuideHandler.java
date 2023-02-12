package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;

/**
 * Adds a rail button to a body tube.
 * Rail guides in RASAero are rail buttons in OpenRocket.
 * <p>
 * RASAero will always add two rail guides to the body tube, one rail guide aft of the body tube with one inch separation
 * from the front side of the rail guide, and one rail guide fore of the body tube, with one inch separation from the
 * center of the rail guide (yes, it's strange that it's not from the bottom of the rail guide...).
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class RailGuideHandler {
    /**
     * Add two rail guides to the body tube.
     * @param parent the body tube to add the rail guides to
     * @param diameter outer diameter of the rail guide
     * @param height total height of the rail guide, plus the screw height
     */
    public static void addRailGuide(BodyTube parent, double diameter, double height) {
        diameter = diameter / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH;
        height = height / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH;

        // TODO: use instances instead of 2 separate components
        RailButton buttonFore = generateRailButtonFromRASAeroRailGuide(diameter, height);
        RailButton buttonAft = generateRailButtonFromRASAeroRailGuide(diameter, height);

        parent.addChild(buttonFore);
        parent.addChild(buttonAft);

        buttonFore.setAxialMethod(AxialMethod.TOP);
        buttonAft.setAxialMethod(AxialMethod.BOTTOM);
        buttonFore.setAxialOffset(0.0254);     // 1 inch separation
        buttonAft.setAxialOffset(-0.0254 + buttonAft.getOuterDiameter()/2);     // 1 inch separation

        // Calculate the angle offset of the rail guides
        // Don't ask me how I got this formula... I don't know what the hell RASAero is doing here.
        // Just experimentally measured the right offsets, it seems to work okay.
        double H = buttonFore.getTotalHeight();
        double D = buttonFore.getOuterDiameter();
        double R = parent.getOuterRadius();
        double rot = -Math.acos((D - 1.3232*R)/(D - 2*H - 2*R));        // This is not an exact solution for all cases, but it is for the most common case
        if (Double.isNaN(rot)) {        // Just to be safe :)
            rot = 0;
        }
        buttonFore.setAngleOffset(rot);
        buttonAft.setAngleOffset(rot);

    }

    /**
     * In RASAero, you only specify the diameter and length of the rail guide. The other parameters are calculated
     * from the diameter and height. The diameter is the outer diameter of the rail guide. The height is the total height
     * of the rail guide, plus the screw height.
     * @param diameter outer diameter of the rail guide
     * @param height total height of the rail guide, plus the screw height
     * @return an OpenRocket RailButton with the correct parameters
     */
    private static RailButton generateRailButtonFromRASAeroRailGuide(double diameter, double height) {
        RailButton button = new RailButton();

        button.setOuterDiameter(diameter);
        button.setInnerDiameter(diameter/2);

        double screwHeight = diameter / 4;                      // Arbitrary value; RASAero doesn't specify this
        double buttonHeight = height - screwHeight;             // Arbitrary value; RASAero doesn't specify this
        double innerHeight = (height - screwHeight)/2;          // Arbitrary value; RASAero doesn't specify this
        double baseHeight = (buttonHeight - innerHeight)/2;

        button.setTotalHeight(buttonHeight);
        button.setBaseHeight(baseHeight);
        button.setFlangeHeight(baseHeight);
        button.setScrewHeight(screwHeight);

        button.setName("Rail Guide");
        ColorHandler.applyRASAeroColor(button, null);   // Use default RASAero color

        return button;
    }
}
