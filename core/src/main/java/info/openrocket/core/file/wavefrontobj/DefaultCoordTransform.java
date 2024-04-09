package info.openrocket.core.file.wavefrontobj;

/**
 * Default OpenRocket coordinate system to OBJ coordinate system converter.
 * OpenRocket uses a left-handed coordinate system with the y-axis pointing up, the z-axis pointing away from the viewer,
 * and the x-axis pointing to the right (in the side view). Its origin is also at the tip of the rocket.
 * This uses a right-handed coordinate system with if viewed from the side view in OpenRocket, the OBJ axes are:
 *      - z-axis starting at the bottom of the rocket and pointing to the left (to the rocket tip)
 *      - side view is in the z-x plane, with the x-axis pointing up
 *      - y-axis is pointing away from the viewer
 */
public class DefaultCoordTransform extends CoordTransform {
    public DefaultCoordTransform(double rocketLength) {
        super(Axis.Y, Axis.Z, Axis.X_MIN, rocketLength, 0, 0);
    }
}
