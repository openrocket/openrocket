package info.openrocket.core.file.rasaero.importt;

import info.openrocket.core.file.rasaero.RASAeroCommonConstants;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.position.AxialMethod;

/**
 * Adds a launch lug to a body tube.
 * <p>
 * RASAero will always add two launch lugs to the body tube, one launch lug aft
 * of the body tube and one fore of
 * the body tube, with always one inch separation between the body tube's edges.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class LaunchLugHandler {
    public static void addLaunchLug(BodyTube parent, double diameter, double length) {
        diameter = diameter / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;
        length = length / RASAeroCommonConstants.OPENROCKET_TO_RASAERO_LENGTH;

        LaunchLug lug = generateLaunchLugFromRASAeroRailGuide(diameter, length, parent.getLength());
        parent.addChild(lug);

        // Position the launch lug
        lug.setAxialMethod(AxialMethod.TOP);
        lug.setAxialOffset(0.0254); // 1 inch separation

        // Calculate the angle offset of the launch lugs
        // Don't ask me how I got this formula... I don't know what the hell RASAero is
        // doing here.
        // Just experimentally measured the right offsets, it seems to work okay.
        double r = diameter / 2;
        double R = parent.getOuterRadius();
        double rot = -Math.acos((0.6616 * R - r) / (R + r));
        lug.setAngleOffset(rot);
    }

    /**
     * Generate an OpenRocket launch lug from the RASAero parameters.
     * 
     * @param diameter     outer diameter of the launch lug
     * @param length       length of the launch lug
     * @param parentLength length of the parent body tube
     * @return an OpenRocket LaunchLug with the correct parameters
     */
    private static LaunchLug generateLaunchLugFromRASAeroRailGuide(double diameter, double length,
            double parentLength) {
        LaunchLug lug = new LaunchLug();

        lug.setOuterRadius(diameter / 2);
        lug.setLength(length);

        // Add the second launch instance
        lug.setInstanceCount(2);
        /*
         * 1 inch separation on the front side of the first launch lug, relative to the
         * front of the lug
         * 1 inch separation on the back side of the second launch lug, relative to the
         * back of the lug
         * => separation = parentLength - 2 * 1 inch - length
         * = parentLength - 0.0508 - length // 0.0508 meters = 2 inches
         */
        double separation = parentLength - 0.0508 - length;
        lug.setInstanceSeparation(separation);

        ColorHandler.applyRASAeroColor(lug, null); // Use default RASAero color

        return lug;
    }
}
