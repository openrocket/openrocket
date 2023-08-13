package net.sf.openrocket.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import net.sf.openrocket.util.Coordinate;

/**
 * Default OpenRocket coordinate system to OBJ coordinate system converter.
 * OpenRocket uses a left-handed coordinate system with the y-axis pointing up, the z-axis pointing away from the viewer,
 * and the x-axis pointing to the right (in the side view). Its origin is also at the tip of the rocket.
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

    /**
     * Converts the location coordinates from OpenRocket to OBJ coordinates.
     * @param x OpenRocket x-coordinate (! careful, when translating the component location, some components need an extra
     *          offset here, such as the component length !)
     * @param y OpenRocket y-coordinate
     * @param z OpenRocket z-coordinate
     * @return the location coordinates in OBJ coordinates
     */
    @Override
    public FloatTuple convertLoc(double x, double y, double z) {
        return convertLocToOBJCoord(x, y, z, this.rocketLength, 0, 0);
    }

    @Override
    public FloatTuple convertLoc(Coordinate coordinate) {
        return convertLocToOBJCoord(coordinate.x, coordinate.y, coordinate.z, this.rocketLength, 0, 0);
    }

    /**
     * Converts the location coordinates from OpenRocket to OBJ coordinates, with the offset of the origin.
     * @param x OpenRocket x-coordinate
     * @param y OpenRocket y-coordinate
     * @param z OpenRocket z-coordinate
     * @param origXOffs the x-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     * @param origYOffs the y-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     * @param origZOffs the z-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     * @return the location coordinates in OBJ coordinates
     */
    private FloatTuple convertLocToOBJCoord(double x, double y, double z,
                                            double origXOffs, double origYOffs, double origZOffs) {
        return new DefaultFloatTuple((float) (y + origYOffs), (float) (z + origZOffs), (float) (origXOffs - x));
    }

    @Override
    public FloatTuple convertLocWithoutOriginOffs(double x, double y, double z) {
        return convertLocToOBJCoord(x, y, z, 0, 0, 0);
    }

    @Override
    public FloatTuple convertRot(double x, double y, double z) {
        // OpenRocket uses left-handed rotations, we need right-handed
        return new DefaultFloatTuple((float) -y, (float) -z, (float) x);
    }

    @Override
    public Axis getAxialAxis() {
        return Axis.Z_MIN;
    }
}
