package net.sf.openrocket.file.wavefrontobj;

import de.javagl.obj.FloatTuple;

/**
 * Default OpenRocket coordinate system to OBJ coordinate system converter.
 * This uses a right-handed coordinate system with if viewed from the side view in OpenRocket, the OBJ axes are:
 *      - z-axis starting at the bottom of the rocket and pointing to the left (to the rocket tip)
 *      - side view is in the z-x plane, with the x-axis pointing up
 *      - y-axis is pointing away from the viewer
 */
public class DefaultCoordTransform implements CoordTransform {
    private final double rocketLength;

    public DefaultCoordTransform(double rocketLength) {
        this.rocketLength = rocketLength;
    }

    @Override
    public FloatTuple convertToOBJCoord(double x, double y, double z) {
        return new DefaultFloatTuple((float) y, (float) z, (float) (this.rocketLength - x));
    }
}
