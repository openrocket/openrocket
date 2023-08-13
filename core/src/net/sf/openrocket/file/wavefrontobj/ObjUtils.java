package net.sf.openrocket.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;

import java.util.List;

/**
 * Utility methods for working with {@link Obj} objects.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class ObjUtils {

    /**
     * Level of detail to use for the export.
     */
    public enum LevelOfDetail {
        LOW_QUALITY(25),
        NORMAL(60),
        HIGH_QUALITY(100);

        private final int value;

        LevelOfDetail(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        /**
         * Get a dynamic estimate of the number of sides (= vertices in a perfect circle) to be used for the given radius.
         * @param radius The radius to estimate the number of sides for
         * @return The number of sides to use for the given radius
         */
        public int getNrOfSides(double radius) {
            final int MIN_SIDES = 10;
            final double refRadius = 0.05;      // Reference radius for the number of sides (the "most common radius") <-- very arbitrary, oh well.
            return Math.max(MIN_SIDES, (int) (0.75*value + (radius/refRadius * 0.25*value)));   // Adjust if needed
        }
    }


    /**
     * Offset the indices by the given offset
     * @param indices The indices to offset
     * @param offset The offset to apply
     */
    public static void offsetIndex(int[] indices, int offset) {
        if (indices == null || indices.length == 0) {
            return;
        }

        for (int i = 0; i < indices.length; i++) {
            indices[i] += offset;
        }
    }

    /**
     * Reverse the winding of the indices if needed.
     * @param indices The indices to reverse the winding of
     * @param reverse Whether to reverse the winding or not
     * @return The indices with the winding reversed if needed
     */
    public static int[] reverseIndexWinding(int[] indices, boolean reverse) {
        if (!reverse) {
            return indices;
        }

        final int[] reversedIndices = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            reversedIndices[i] = indices[indices.length - 1 - i];
        }

        return reversedIndices;
    }

    /**
     * Translate the vertices of an object.
     * <b>NOTE: this uses the Wavefront OBJ coordinate system</b>
     * @param obj The object to translate the vertices of
     * @param startIdx The starting vertex index to translate
     * @param endIdx The ending vertex index to translate (inclusive)
     * @param transX The translation in the x direction
     * @param transY The translation in the y direction
     * @param transZ The translation in the z direction
     */
    public static void translateVertices(DefaultObj obj, int startIdx, int endIdx,
                                         float transX, float transY, float transZ) {
        verifyIndexRange(obj, startIdx, endIdx);

        if (Float.compare(transX, 0) == 0 && Float.compare(transY, 0) == 0 && Float.compare(transZ, 0) == 0) {
            return;
        }

        for (int i = startIdx; i <= endIdx; i++) {
            FloatTuple vertex = obj.getVertex(i);
            final float x = vertex.getX();
            final float y = vertex.getY();
            final float z = vertex.getZ();
            FloatTuple translatedVertex = new DefaultFloatTuple(x + transX, y + transY, z + transZ);
            obj.setVertex(i, translatedVertex);
        }
    }

    /**
     * Rotate the vertices of an object.
     * <b>NOTE: this uses the Wavefront OBJ coordinate system</b>
     * @param obj The object to rotate the vertices of
     * @param verticesStartIdx The starting vertex index to rotate
     * @param verticesEndIdx The ending vertex index to rotate (inclusive)
     * @param normalsStartIdx The starting normal index to rotate
     * @param normalsEndIdx The ending normal index to rotate (inclusive)
     * @param rotX The rotation around the x axis in radians
     * @param rotY The rotation around the y axis in radians
     * @param rotZ The rotation around the z axis in radians
     * @param origX The x coordinate of the origin of the rotation
     * @param origY The y coordinate of the origin of the rotation
     * @param origZ The z coordinate of the origin of the rotation
     */
    public static void rotateVertices(DefaultObj obj, int verticesStartIdx, int verticesEndIdx,
                                      int normalsStartIdx, int normalsEndIdx,
                                      float rotX, float rotY, float rotZ,
                                      float origX, float origY, float origZ) {
        verifyIndexRange(obj, verticesStartIdx, verticesEndIdx);

        if (Float.compare(rotX, 0) == 0 && Float.compare(rotY, 0) == 0 && Float.compare(rotZ, 0) == 0) {
            return;
        }

        final float cosX = (float) Math.cos(rotX);
        final float sinX = (float) Math.sin(rotX);
        final float cosY = (float) Math.cos(rotY);
        final float sinY = (float) Math.sin(rotY);
        final float cosZ = (float) Math.cos(rotZ);
        final float sinZ = (float) Math.sin(rotZ);

        final float Axx = cosY * cosZ;
        final float Axy = -cosY * sinZ;
        final float Axz = sinY;

        final float Ayx = sinX * sinY * cosZ + cosX * sinZ;
        final float Ayy = -sinX * sinY * sinZ + cosX * cosZ;
        final float Ayz = -sinX * cosY;

        final float Azx = -cosX * sinY * cosZ + sinX * sinZ;
        final float Azy = cosX * sinY * sinZ + sinX * cosZ;
        final float Azz = cosX * cosY;

        // Rotate the vertices
        for (int i = verticesStartIdx; i <= verticesEndIdx; i++) {
            // Get the vertex information
            FloatTuple vertex = obj.getVertex(i);
            final float x = vertex.getX() - origX;
            final float y = vertex.getY() - origY;
            final float z = vertex.getZ() - origZ;

            // Apply rotation
            float rotatedX = Axx * x + Axy * y + Axz * z;
            float rotatedY = Ayx * x + Ayy * y + Ayz * z;
            float rotatedZ = Azx * x + Azy * y + Azz * z;

            // Translate the point back to its original position
            rotatedX += origX;
            rotatedY += origY;
            rotatedZ += origZ;

            FloatTuple rotatedVertex = new DefaultFloatTuple(rotatedX, rotatedY, rotatedZ);
            obj.setVertex(i, rotatedVertex);
        }

        // Rotate the normals
        for (int i = normalsStartIdx; i <= normalsEndIdx; i++) {
            // We don't need to consider the rotation origin for normals, since they are unit vectors
            FloatTuple normal = obj.getNormal(i);
            final float x = normal.getX();
            final float y = normal.getY();
            final float z = normal.getZ();

            float newX = Axx * x + Axy * y + Axz * z;
            float newY = Ayx * x + Ayy * y + Ayz * z;
            float newZ = Azx * x + Azy * y + Azz * z;

            FloatTuple rotatedNormal = new DefaultFloatTuple(newX, newY, newZ);
            rotatedNormal = normalizeVector(rotatedNormal);
            obj.setNormal(i, rotatedNormal);
        }
    }

    private static void verifyIndexRange(DefaultObj obj, int startIdx, int endIdx) {
        if (startIdx < 0 || startIdx >= obj.getNumVertices()) {
            throw new IllegalArgumentException("startIdx must be between 0 and the number of vertices");
        }
        if (endIdx < 0 || endIdx >= obj.getNumVertices()) {
            throw new IllegalArgumentException("endIdx must be between 0 and the number of vertices");
        }
        if (startIdx > endIdx) {
            throw new IllegalArgumentException("startIdx must be less than or equal to endIdx");
        }
    }

    /**
     * Normalizes a vector so that its length is 1.
     * If all components of the vector are 0, then the vector is returned unchanged.
     * @param vector The vector to normalize
     * @return The normalized vector
     */
    public static FloatTuple normalizeVector(FloatTuple vector) {
        final float x = vector.getX();
        final float y = vector.getY();
        final float z = vector.getZ();

        float length = (float)Math.sqrt(x * x + y * y + z * z);

        if (length == 0) {
            return vector;
        }

        final float newX = x / length;
        final float newY = y / length;
        final float newZ = z / length;

        return new DefaultFloatTuple(newX, newY, newZ);
    }

    /**
     * Normalizes a vector so that its length is 1.
     * @param x The x component of the vector
     * @param y The y component of the vector
     * @param z The z component of the vector
     * @return The normalized vector
     */
    public static FloatTuple normalizeVector(float x, float y, float z) {
        return normalizeVector(new DefaultFloatTuple(x, y, z));
    }

    /**
     * Calculate the average of a list of vertices.
     * @param vertices The list of vertices
     * @return The average of the vertices
     */
    public static FloatTuple averageVertices(List<FloatTuple> vertices) {
        float x = 0;
        float y = 0;
        float z = 0;
        for (FloatTuple vertex : vertices) {
            x += vertex.getX();
            y += vertex.getY();
            z += vertex.getZ();
        }
        x /= vertices.size();
        y /= vertices.size();
        z /= vertices.size();
        return new DefaultFloatTuple(x, y, z);
    }

    /**
     * Calculate the average of a list of vertices.
     * @param obj The obj file to get the vertices from
     * @param vertexIndices The indices of the vertices to average
     * @return The average of the vertices
     */
    public static FloatTuple averageVertices(DefaultObj obj, List<Integer> vertexIndices) {
        List<FloatTuple> vertices = obj.getVertices(vertexIndices);
        return averageVertices(vertices);
    }

    /**
     * Removes the positional offset of the vertices in the obj file.
     * This is useful for making sure that the object is centered at the origin. By default, every component is exported
     * in the position that it is in the rocket. This may not be useful for individual components.
     * @param obj The obj file to remove the offset from
     */
    public static void removeVertexOffset(DefaultObj obj, CoordTransform transformer) {
        final FloatTupleBounds bounds = obj.getVertexBounds();
        final FloatTuple min = bounds.getMin();
        final FloatTuple max = bounds.getMax();

        // These are all referenced in the OBJ coordinate system
        float minX = min.getX();
        float minY = min.getY();
        float minZ = min.getZ();
        float maxX = max.getX();
        float maxY = max.getY();
        float maxZ = max.getZ();

        // Adjust the min/max values based on the axial axis so that in the offset calculations, one of the two
        // cancels out. This is necessary to ensure that the rocket bottom is at the OBJ coordinate system origin.
        Axis axialAxis = transformer.getAxialAxis();
        switch (axialAxis) {
            case X -> {
                minX = maxX;
            } case X_MIN -> {
                maxX = minX;
            } case Y -> {
                minY = maxY;
            } case Y_MIN -> {
                maxY = minY;
            } case Z -> {
                minZ = maxZ;
            } case Z_MIN -> {
                maxZ = minZ;
            }
        }

        final float offsetX = (maxX + minX) / 2;
        final float offsetY = (maxY + minY) / 2;
        final float offsetZ = (maxZ + minZ) / 2;

        for (int i = 0; i < obj.getNumVertices(); i++) {
            FloatTuple vertex = obj.getVertex(i);
            final float x = vertex.getX() - offsetX;
            final float y = vertex.getY() - offsetY;
            final float z = vertex.getZ() - offsetZ;
            obj.setVertex(i, new DefaultFloatTuple(x, y, z));
        }
    }


    /**
     * Translates the vertices in the obj file so that the component is at the specified translation.
     * @param obj The obj file to translate
     * @param startIdx The index of the first vertex to translate
     * @param endIdx The index of the last vertex to translate (inclusive)
     * @param translation The translation coordinates to translate the component with (in OpenRocket coordinate system)
     */
    public static void translateVerticesFromComponentLocation(DefaultObj obj, CoordTransform transformer,
                                                              int startIdx, int endIdx, Coordinate translation) {
        FloatTuple translatedLoc = transformer.convertLoc(translation);
        ObjUtils.translateVertices(obj, startIdx, endIdx, translatedLoc.getX(), translatedLoc.getY(), translatedLoc.getZ());
    }

    /**
     * Merge a list of objs into a single obj.
     * @param objs The objs to merge
     * @return The merged obj
     */
    public static Obj merge(List<DefaultObj> objs) {
        DefaultObj merged = new DefaultObj();

        for (DefaultObj obj : objs) {
            for (int i = 0; i < obj.getNumVertices(); i++) {
                FloatTuple vertex = obj.getVertex(i);
                merged.addVertex(vertex.get(0), vertex.get(1), vertex.get(2));
            }

            for (ObjGroup group : obj.getGroups()) {
                if (!(group instanceof DefaultObjGroup)) {
                    throw new RuntimeException("Expected DefaultObjGroup");
                }
                DefaultObjGroup newGroup = new DefaultObjGroup(group.getName());
                for (ObjFace face : ((DefaultObjGroup) group).getFaces()) {
                    newGroup.addFace(face);
                }
                merged.addGroup(newGroup);
            }
        }

        return merged;
    }
}
