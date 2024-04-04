package info.openrocket.core.file.wavefrontobj;

import com.sun.istack.NotNull;
import de.javagl.obj.FloatTuple;
import info.openrocket.core.util.Coordinate;

/**
 * Interface for classes that can convert location and rotation coordinates from the OpenRocket coordinate system
 * to a custom OBJ coordinate system.
 * OpenRocket uses a left-handed coordinate system with the forward (y-)axis pointing up, the depth (z-)axis pointing away from the viewer,
 * and the longitudinal (x-)axis pointing to the right (in the side view). Its origin is also at the tip of the rocket.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class CoordTransform {
    // OpenRocket coordinate system axes mapping
    protected final Axis xAxis;
    protected final Axis yAxis;
    protected final Axis zAxis;

    // Origin offsets
    protected final double origXOffs;
    protected final double origYOffs;
    protected final double origZOffs;

    /**
     * Create a new coordinate system converter.
     * @param xAxis the OpenRocket axis that corresponds to the transformed x-axis
     * @param yAxis the OpenRocket axis that corresponds to the transformed y-axis
     * @param zAxis the OpenRocket axis that corresponds to the transformed z-axis
     *              Example: you want the transformed coordinate system to have the z-axis as the longitudinal axis (= OpenRocket x-axis)
     *              and want its direction to be from the bottom of the rocket to the tip of the rocket (opposite direction
     *              of the OpenRocket x-axis), then you must pass Axis.X_MIN as the xAxis parameter.
     *              You must also add an offset to the origin of the transformed coordinate system, so that it starts
     *              at the bottom of the rocket => set origZOffs to the length of the rocket.
     * @param origXOffs the x-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     * @param origYOffs the y-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     * @param origZOffs the z-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     */
    public CoordTransform(@NotNull Axis xAxis, @NotNull Axis yAxis, @NotNull Axis zAxis,
                          double origXOffs, double origYOffs, double origZOffs) {
        if (xAxis == null || yAxis == null || zAxis == null) {
            throw new IllegalArgumentException("Axes cannot be null");
        }

        if (xAxis.isSameAxis(yAxis) || xAxis.isSameAxis(zAxis) || yAxis.isSameAxis(zAxis)) {
            throw new IllegalArgumentException("Axes must be different");
        }

        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.zAxis = zAxis;

        this.origXOffs = origXOffs;
        this.origYOffs = origYOffs;
        this.origZOffs = origZOffs;
    }

    /**
     * Create a new coordinate system converter.
     * @param axialAxis the OBJ axis that corresponds to the OpenRocket axial (x-)axis
     * @param forwardAxis the OBJ axis that corresponds to the OpenRocket forward (y-)axis
     * @param origXOffs the x-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     * @param origYOffs the y-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     * @param origZOffs the z-offset of the origin of the OBJ coordinate system, <b>in the OpenRocket coordinate system</b>
     */
    public static CoordTransform generateUsingAxialAndForwardAxes(Axis axialAxis, Axis forwardAxis,
                                                                  double origXOffs, double origYOffs, double origZOffs) {
        if (axialAxis == null || forwardAxis == null) {
            throw new IllegalArgumentException("Axes cannot be null");
        }

        if (axialAxis.isSameAxis(forwardAxis)) {
            throw new IllegalArgumentException("Axes must be different");
        }

        Axis depthAxis = Axis.getThirdAxis(axialAxis, forwardAxis);

        Axis xAxis = null;
        Axis yAxis = null;
        Axis zAxis = null;

        switch (axialAxis) {
            // X-axis points from the tip to the bottom, so invert it
            case X -> xAxis = Axis.X_MIN;
            case X_MIN -> xAxis = Axis.X;
            case Y -> yAxis = Axis.X_MIN;
            case Y_MIN -> yAxis = Axis.X;
            case Z -> zAxis = Axis.X_MIN;
            case Z_MIN -> zAxis = Axis.X;
        }
        switch (forwardAxis) {
            case X -> xAxis = Axis.Y;
            case X_MIN -> xAxis = Axis.Y_MIN;
            case Y -> yAxis = Axis.Y;
            case Y_MIN -> yAxis = Axis.Y_MIN;
            case Z -> zAxis = Axis.Y;
            case Z_MIN -> zAxis = Axis.Y_MIN;
        }


        switch (depthAxis) {
            // OpenRocket uses left-handed coordinate system, so invert the axis
            case X -> xAxis = Axis.Z_MIN;
            case X_MIN -> xAxis = Axis.Z;
            case Y -> yAxis = Axis.Z_MIN;
            case Y_MIN -> yAxis = Axis.Z;
            case Z -> zAxis = Axis.Z_MIN;
            case Z_MIN -> zAxis = Axis.Z;
        }

        if (xAxis == null || yAxis == null || zAxis == null) {
            throw new IllegalStateException("Axes should not be null");
        }

        return new CoordTransform(xAxis, yAxis, zAxis, origXOffs, origYOffs, origZOffs);
    }


    private FloatTuple convertLoc(double x, double y, double z,
                                 double origXOffs, double origYOffs, double origZOffs) {
        final double xTrans = getTransformedCoordinate(xAxis, x, y, z);
        final double yTrans = getTransformedCoordinate(yAxis, x, y, z);
        final double zTrans = getTransformedCoordinate(zAxis, x, y, z);

        final double origXTrans = getTransformedOriginOffset(xAxis, origXOffs, origYOffs, origZOffs);
        final double origYTrans = getTransformedOriginOffset(yAxis, origXOffs, origYOffs, origZOffs);
        final double origZTrans = getTransformedOriginOffset(zAxis, origXOffs, origYOffs, origZOffs);

        return new DefaultFloatTuple(
                (float) (xTrans + origXTrans),
                (float) (yTrans + origYTrans),
                (float) (zTrans + origZTrans)
        );
    }

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
    public FloatTuple convertLoc(double x, double y, double z) {
        return convertLoc(x, y, z, origXOffs, origYOffs, origZOffs);
    }

    /**
     * Converts the location coordinates from OpenRocket to OBJ coordinates.
     * The returned coordinates will also be shifted towards the origin of the OBJ coordinate system.
     * E.g. if x = 0, but the origin of the OBJ coordinate system is at x = 10, then the returned x-coordinate will be 10.
     *      (that is in the case that the OpenRocket x-axis is the same as the OBJ x-axis, that no conversion is needed, but that's just for the easy example)
     * @param coordinate OpenRocket coordinate
     * @return the location coordinates in OBJ coordinates
     */
    public FloatTuple convertLoc(Coordinate coordinate) {
        return convertLoc(coordinate.x, coordinate.y, coordinate.z);
    }

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
    public FloatTuple convertLocWithoutOriginOffs(double x, double y, double z) {
        return convertLoc(x, y, z, 0, 0, 0);
    }

    public FloatTuple convertLocWithoutOriginOffs(Coordinate coordinate) {
        return convertLoc(coordinate.x, coordinate.y, coordinate.z, 0, 0, 0);
    }

    /**
     * Converts the rotation coordinates from OpenRocket to OBJ coordinates.
     * @param xRot OpenRocket rotation in radians around the x-axis
     * @param yRot OpenRocket rotation in radians around the y-axis
     * @param zRot OpenRocket rotation in radians around the z-axis
     * @return the rotation coordinates in OBJ coordinates
     */
    public FloatTuple convertRot(double xRot, double yRot, double zRot) {
        final double xRotTrans = getTransformedRotation(xAxis, xRot, yRot, zRot);
        final double yRotTrans = getTransformedRotation(yAxis, xRot, yRot, zRot);
        final double zRotTrans = getTransformedRotation(zAxis, xRot, yRot, zRot);

        return new DefaultFloatTuple((float) xRotTrans, (float) yRotTrans, (float) zRotTrans);
    }

    /**
     * Returns the transformed coordinate for the given axis.
     * @param axis the OpenRocket axis to transform
     * @param x the x-coordinate in the OpenRocket coordinate system
     * @param y the y-coordinate in the OpenRocket coordinate system
     * @param z the z-coordinate in the OpenRocket coordinate system
     * @return the coordinate in the transformed OBJ coordinate system
     */
    private static double getTransformedCoordinate(Axis axis, double x, double y, double z) {
        return switch (axis) {
            case X -> x;
            case X_MIN -> -x;
            case Y -> y;
            case Y_MIN -> -y;
            case Z -> z;
            case Z_MIN -> -z;
            default -> throw new IllegalStateException("Unknown axis");
        };
    }

    /**
     * Returns the offset of the origin of the OBJ coordinate system for the given axis.
     * @param axis the axis to get the offset for
     * @return the offset of the origin of the OBJ coordinate system for the given axis
     */
    private static double getTransformedOriginOffset(Axis axis, double origXOffs, double origYOffs, double origZOffs) {
        return switch (axis) {
            case X, X_MIN -> origXOffs;
            case Y, Y_MIN -> origYOffs;
            case Z, Z_MIN -> origZOffs;
            default -> throw new IllegalStateException("Unknown axis");
        };
    }

    /**
     * Returns the transformed rotation for the given axis.
     * @param axis the OpenRocket axis to transform
     * @param rotX the rotation in radians around the OpenRocket x-axis
     * @param rotY the rotation in radians around the OpenRocket y-axis
     * @param rotZ the rotation in radians around the OpenRocket z-axis
     * @return the rotation in radians around the transformed OBJ axis
     */
    private static double getTransformedRotation(Axis axis, double rotX, double rotY, double rotZ) {
        // OpenRocket uses left-handed coordinate system, we'll use right-handed
        return switch (axis) {
            case X -> -rotX;
            case X_MIN -> rotX;
            case Y -> -rotY;
            case Y_MIN -> rotY;
            case Z -> -rotZ;
            case Z_MIN -> rotZ;
            default -> throw new IllegalStateException("Unknown axis");
        };
    }

    public Axis getXAxis() {
        return xAxis;
    }

    public Axis getYAxis() {
        return yAxis;
    }

    public Axis getZAxis() {
        return zAxis;
    }

    /**
     * Returns the equivalent axis for the x-axis in the OpenRocket coordinate system (axial axis).
     * ! The direction is relative to the OpenRocket x-axis ! For instance, if the OBJ coordinate system uses the z-axis
     * that runs in the opposite direction of the OR x-axis (z-axis runs from the bottom of the rocket to the tip), you
     * must return Axis.Z_MIN.
     * @return the equivalent axis for the x-axis in the OpenRocket coordinate system (axial axis)
     */
    public Axis getAxialAxis() {
        switch (xAxis) {
            case X -> {
                return Axis.X;
            }
            case X_MIN -> {
                return Axis.X_MIN;
            }
        }
        switch (yAxis) {
            case X -> {
                return Axis.Y;
            }
            case X_MIN -> {
                return Axis.Y_MIN;
            }
        }
        switch (zAxis) {
            case X -> {
                return Axis.Z;
            }
            case X_MIN -> {
                return Axis.Z_MIN;
            }
        }

        throw new IllegalStateException("No axial axis found");
    }

    public Axis getForwardAxis() {
        switch (xAxis) {
            case Y -> {
                return Axis.X;
            }
            case Y_MIN -> {
                return Axis.X_MIN;
            }
        }
        switch (yAxis) {
            case Y -> {
                return Axis.Y;
            }
            case Y_MIN -> {
                return Axis.Y_MIN;
            }
        }
        switch (zAxis) {
            case Y -> {
                return Axis.Z;
            }
            case Y_MIN -> {
                return Axis.Z_MIN;
            }
        }

        throw new IllegalStateException("No forward axis found");
    }

    public double getOrigXOffs() {
        return origXOffs;
    }

    public double getOrigYOffs() {
        return origYOffs;
    }

    public double getOrigZOffs() {
        return origZOffs;
    }
}
