package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.position.AxialMethod;

/**
 * Adds a rail button to a body tube.
 * Rail guides in RASAero are rail buttons in OpenRocket.
 * <p>
 * RASAero will always add two rail guides to the body tube, one rail guide aft
 * of the body tube with one inch separation
 * from the front side of the rail guide, and one rail guide fore of the body
 * tube, with one inch separation from the
 * center of the rail guide (yes, it's strange that it's not from the bottom of
 * the rail guide...).
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class RailGuideHandler {
    /**
     * Add two rail guides to the body tube.
     * 
     * @param parent   the body tube to add the rail guides to
     * @param diameter outer diameter of the rail guide
     * @param height   total height of the rail guide, plus the screw height
     */
    public static void addRailGuide(BodyTube parent, double diameter, double height) {
        diameter = diameter / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        height = height / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;

        RailButton button = generateRailButtonFromRASAeroRailGuide(diameter, height, parent.getLength());
        parent.addChild(button);

        // Position the first rail guide with 1 inch separation with the front of the
        // parent body tube, relative to the front of the rail guide
        button.setAxialMethod(AxialMethod.TOP);
        button.setAxialOffset(0.0254 + diameter / 2);

        // Calculate the angle offset of the rail guides
        // Don't ask me how I got this formula... I don't know what the hell RASAero is
        // doing here.
        // Just experimentally measured the right offsets, it seems to work okay.
        double H = button.getTotalHeight();
        double D = button.getOuterDiameter();
        double R = parent.getOuterRadius();
        double rot = -Math.acos((D - 1.3232 * R) / (D - 2 * H - 2 * R)); // This is not an exact solution for all cases,
                                                                         // but it is for the most common case
        if (Double.isNaN(rot)) { // Just to be safe :)
            rot = 0;
        }
        button.setAngleOffset(rot);

    }

    /**
     * In RASAero, you only specify the diameter and length of the rail guide. The
     * other parameters are calculated
     * from the diameter and height. The diameter is the outer diameter of the rail
     * guide. The height is the total height
     * of the rail guide, plus the screw height.
     * 
     * @param diameter     outer diameter of the rail guide
     * @param height       total height of the rail guide, plus the screw height
     * @param parentLength length of the parent body tube
     * @return an OpenRocket RailButton with the correct parameters
     */
    private static RailButton generateRailButtonFromRASAeroRailGuide(double diameter, double height,
            double parentLength) {
        RailButton button = new RailButton();

        button.setOuterDiameter(diameter);
        button.setInnerDiameter(diameter / 2);

        double screwHeight = diameter / 4; // Arbitrary value; RASAero doesn't specify this
        double buttonHeight = height - screwHeight; // Arbitrary value; RASAero doesn't specify this
        double innerHeight = (height - screwHeight) / 2; // Arbitrary value; RASAero doesn't specify this
        double baseHeight = (buttonHeight - innerHeight) / 2;

        button.setTotalHeight(buttonHeight);
        button.setBaseHeight(baseHeight);
        button.setFlangeHeight(baseHeight);
        button.setScrewHeight(screwHeight);

        // Add the second rail guide instance
        button.setInstanceCount(2);
        /*
         * 1 inch separation on the front side of the first rail guide, relative to the
         * front of the rail guide
         * 1 inch separation on the back side of the second rail guide, relative to the
         * center of the rail guide (strange RASAero...)
         * => separation = parentLength - 2 * 1 inch - diameter/2
         * = parentLength - 0.0508 - diameter/2 // 0.0508 meters = 2 inches
         */
        double separation = parentLength - 0.0508 - diameter / 2;
        button.setInstanceSeparation(separation);

        button.setName("Rail Guide");
        ColorHandler.applyRASAeroColor(button, null); // Use default RASAero color

        return button;
    }
}
