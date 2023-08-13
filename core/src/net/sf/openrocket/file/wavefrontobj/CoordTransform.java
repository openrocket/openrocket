package net.sf.openrocket.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import net.sf.openrocket.util.Coordinate;

/**
 * Interface for classes that can convert location and rotation coordinates from the OpenRocket coordinate system
 * to a custom OBJ coordinate system.
 * OpenRocket uses a left-handed coordinate system with the y-axis pointing up, the z-axis pointing away from the viewer,
 * and the x-axis pointing to the right (in the side view). Its origin is also at the tip of the rocket.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public interface CoordTransform {
    /**
     * Converts the location coordinates from OpenRocket to OBJ coordinates.
     * The returned coordinates will also be shifted towards the origin of the OBJ coordinate system.
     * E.g. if x = 0, but the origin of the OBJ coordinate system is at x = 10, then the returned x-coordinate will be 10.
     *      (that is in the case that the OpenRocket x-axis is the same as the OBJ x-axis, that no conversion is needed, but that's just for the easy example)
     * @param x OpenRocket x-coordinate
     * @param y OpenRocket y-coordinate
     * @param z OpenRocket z-coordinate
     * @return the location coordinates in OBJ coordinates
     */
    FloatTuple convertLoc(double x, double y, double z);

    /**
     * Converts the location coordinates from OpenRocket to OBJ coordinates.
     * The returned coordinates will also be shifted towards the origin of the OBJ coordinate system.
     * E.g. if x = 0, but the origin of the OBJ coordinate system is at x = 10, then the returned x-coordinate will be 10.
     *      (that is in the case that the OpenRocket x-axis is the same as the OBJ x-axis, that no conversion is needed, but that's just for the easy example)
     * @param coordinate OpenRocket coordinate
     * @return the location coordinates in OBJ coordinates
     */
    FloatTuple convertLoc(Coordinate coordinate);

    /**
     * Converts the location coordinates from OpenRocket to OBJ coordinates, without the offset of the origin.
     * This is useful for when you want the relative position of a vertex in the OpenRocket coordinate system to be the
     * same as the relative position of the converted vertex to the OBJ coordinate system.
     * </p>
     * E.g. if you have a vertex (0, 1, 2) in OpenRocket, then the converted vertex could be (2, 1, 0) in OBJ coordinates,
     * even you would have an x-axis origin offset of e.g. 10.
     * </p>
     * This is different from {@link #convertLoc(double, double, double)}, where the returned coordinates would
     * be (2, 1, 10), due to the origin x offset.
     * @param x OpenRocket x-coordinate
     * @param y OpenRocket y-coordinate
     * @param z OpenRocket z-coordinate
     * @return the location coordinates in OBJ coordinates, with the origin set to the OpenRocket origin (the tip of the rocket)
     */
    FloatTuple convertLocWithoutOriginOffs(double x, double y, double z);

    /**
     * Converts the rotation coordinates from OpenRocket to OBJ coordinates.
     * @param x OpenRocket rotation in radians around the x-axis
     * @param y OpenRocket rotation in radians around the y-axis
     * @param z OpenRocket rotation in radians around the z-axis
     * @return the rotation coordinates in OBJ coordinates
     */
    FloatTuple convertRot(double x, double y, double z);

    /**
     * Returns the equivalent axis for the x-axis in the OpenRocket coordinate system (axial axis).
     * ! The direction is relative to the OpenRocket x-axis ! For instance, if the OBJ coordinate system uses the z-axis
     * that runs in the opposite direction of the OR x-axis (z-axis runs from the bottom of the rocket to the tip), you
     * must return Axis.Z_MIN.
     * @return the equivalent axis for the x-axis in the OpenRocket coordinate system (axial axis)
     */
    Axis getAxialAxis();
}
