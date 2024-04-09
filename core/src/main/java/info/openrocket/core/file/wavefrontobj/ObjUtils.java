package info.openrocket.core.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.FloatTuples;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;
import de.javagl.obj.ReadableObj;
import de.javagl.obj.WritableObj;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Coordinate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods for working with {@link Obj} objects.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class ObjUtils {
    private static final Translator trans = Application.getTranslator();

    /**
     * Level of detail to use for the export.
     */
    public enum LevelOfDetail {
        LOW_QUALITY(25, trans.get("LevelOfDetail.LOW_QUALITY"), "LOW"),
        NORMAL_QUALITY(60, trans.get("LevelOfDetail.NORMAL_QUALITY"), "NORMAL"),
        HIGH_QUALITY(100, trans.get("LevelOfDetail.HIGH_QUALITY"), "HIGH");

        private final int value;
        private final String label;
        private final String exportLabel;

        LevelOfDetail(int value, String label, String exportLabel) {
            this.value = value;
            this.label = label;
            this.exportLabel = exportLabel;
        }

        public int getValue() {
            return value;
        }

        public String getExportLabel() {
            return exportLabel;
        }

        public static LevelOfDetail fromExportLabel(String exportLabel) {
            for (LevelOfDetail lod : LevelOfDetail.values()) {
                if (lod.getExportLabel().equals(exportLabel)) {
                    return lod;
                }
            }
            return LevelOfDetail.NORMAL_QUALITY;
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

        @Override
        public String toString() {
            return label;
        }
    }

    public enum TriangulationMethod {
        SIMPLE(trans.get("TriangulationMethod.SIMPLE"), trans.get("TriangulationMethod.SIMPLE.ttip"), "SIMPLE"),
        DELAUNAY(trans.get("TriangulationMethod.DELAUNAY"), trans.get("TriangulationMethod.DELAUNAY.ttip"), "DELAUNAY");

        private final String label;
        private final String tooltip;
        private final String exportLabel;

        TriangulationMethod(String label, String tooltip, String exportLabel) {
            this.label = label;
            this.tooltip = tooltip;
            this.exportLabel = exportLabel;
        }

        @Override
        public String toString() {
            return label;
        }

        public String getTooltip() {
            return tooltip;
        }

        public String getExportLabel() {
            return exportLabel;
        }

        public static TriangulationMethod fromExportLabel(String exportLabel) {
            for (TriangulationMethod tm : TriangulationMethod.values()) {
                if (tm.getExportLabel().equals(exportLabel)) {
                    return tm;
                }
            }
            return TriangulationMethod.DELAUNAY;
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
     * Translates the vertices in the obj file so that the component is at the specified translation.
     * @param obj The obj file to translate
     * @param startIdx The index of the first vertex to translate
     * @param endIdx The index of the last vertex to translate (inclusive)
     * @param translation The translation coordinates to translate the component with (in OpenRocket coordinate system)
     */
    public static void translateVerticesFromComponentLocation(DefaultObj obj, CoordTransform transformer,
                                                              int startIdx, int endIdx, Coordinate translation) {
        FloatTuple translatedLoc = transformer.convertLocWithoutOriginOffs(translation);
        ObjUtils.translateVertices(obj, startIdx, endIdx, translatedLoc.getX(), translatedLoc.getY(), translatedLoc.getZ());
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

    /**
     * Scale the vertices of an object around a certain origin.
     * <b>NOTE: this uses the Wavefront OBJ coordinate system</b>
     * @param obj The object to scale the vertices of
     * @param startIdx The starting vertex index to scale
     * @param endIdx The ending vertex index to scale (inclusive)
     * @param scaleX The scaling factor in the x direction
     * @param scaleY The scaling factor in the y direction
     * @param scaleZ The scaling factor in the z direction
     * @param origX The x coordinate of the origin of the scaling
     * @param origY The y coordinate of the origin of the scaling
     * @param origZ The z coordinate of the origin of the scaling
     */
    public static void scaleVertices(DefaultObj obj, int startIdx, int endIdx,
                                     float scaleX, float scaleY, float scaleZ,
                                     float origX, float origY, float origZ) {
        verifyIndexRange(obj, startIdx, endIdx);

        if (Float.compare(scaleX, 1) == 0 && Float.compare(scaleY, 1) == 0 && Float.compare(scaleZ, 1) == 0) {
            return;
        }

        for (int i = startIdx; i <= endIdx; i++) {
            FloatTuple vertex = obj.getVertex(i);

            // Translate vertex to origin
            final float x = vertex.getX() - origX;
            final float y = vertex.getY() - origY;
            final float z = vertex.getZ() - origZ;

            // Apply scaling
            float scaledX = x * scaleX;
            float scaledY = y * scaleY;
            float scaledZ = z * scaleZ;

            // Translate vertex back to its original position
            scaledX += origX;
            scaledY += origY;
            scaledZ += origZ;

            FloatTuple scaledVertex = new DefaultFloatTuple(scaledX, scaledY, scaledZ);
            obj.setVertex(i, scaledVertex);
        }
    }

    /**
     * Scale the vertices of an object around the origin
     * <b>NOTE: this uses the Wavefront OBJ coordinate system</b>
     * @param obj The object to scale the vertices of
     * @param scaling The uniform scaling factor
     */
    public static void scaleVertices(DefaultObj obj, float scaling) {
        scaleVertices(obj, 0, obj.getNumVertices() - 1,
                scaling, scaling, scaling,
                0, 0, 0);
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
     * Calculates the normal vector of a triangle defined by three vertices.
     *
     * @param v1 The first vertex of the triangle.
     * @param v2 The second vertex of the triangle.
     * @param v3 The third vertex of the triangle.
     * @return The normal vector of the triangle.
     */
    public static FloatTuple calculateNormalVector(FloatTuple v1, FloatTuple v2, FloatTuple v3) {
        FloatTuple u = subtractVectors(v2, v1);
        FloatTuple v = subtractVectors(v3, v1);

        return normalizeVector(crossProduct(u, v));
    }

    /**
     * Calculates the normal vector for a given face of the object.
     *
     * @param obj  The object.
     * @param face The face of the object for which to calculate the normal vector.
     * @return The calculated normal vector.
     */
    public static FloatTuple calculateNormalVector(DefaultObj obj, DefaultObjFace face) {
        FloatTuple[] vertices = getVertices(obj, face);
        return calculateNormalNewell(vertices);
    }

    /**
     * Calculates the normal of a polygon using the Newell's method.
     *
     * @param vertices a list of vertices representing the polygon
     * @return the normalized normal vector of the polygon
     */
    private static FloatTuple calculateNormalNewell(FloatTuple[] vertices) {
        float x = 0f;
        float y = 0f;
        float z = 0f;
        for (int i = 0; i < vertices.length; i++) {
            FloatTuple current = vertices[i];
            FloatTuple next = vertices[(i + 1) % vertices.length];

            x += (current.getY() - next.getY()) * (current.getZ() + next.getZ());
            y += (current.getZ() - next.getZ()) * (current.getX() + next.getX());
            z += (current.getX() - next.getX()) * (current.getY() + next.getY());
        }
        return normalizeVector(new DefaultFloatTuple(x, y, z));
    }

    /**
     * Subtracts two vectors.
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return a new FloatTuple representing the subtraction of v2 from v1
     */
    public static FloatTuple subtractVectors(FloatTuple v1, FloatTuple v2) {
        return new DefaultFloatTuple(v1.getX() - v2.getX(), v1.getY() - v2.getY(), v1.getZ() - v2.getZ());
    }

    /**
     * Calculates the cross product of two vectors.
     *
     * @param v1 the first vector
     * @param v2 the second vector
     * @return the cross product of the given vectors
     */
    public static FloatTuple crossProduct(FloatTuple v1, FloatTuple v2) {
        return new DefaultFloatTuple(
                v1.getY() * v2.getZ() - v1.getZ() * v2.getY(),
                v1.getZ() * v2.getX() - v1.getX() * v2.getZ(),
                v1.getX() * v2.getY() - v1.getY() * v2.getX()
        );
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

    /**
     * Creates a {@link FloatTuple} from the given UVW values, treating
     * optional values as described in the MTL specification: If
     * the <code>u</code> component is <code>null</code>, then
     * <code>null</code> is returned. If the <code>v</code> or
     * <code>w</code> component is null, then the given default value
     * will be used.
     *
     * @param u            The u-component
     * @param v            The v-component
     * @param w            The w-component
     * @param defaultValue The default value for v and w
     * @return The {@link FloatTuple}
     */
    public static FloatTuple createUvwTuple(
            Float u, Float v, Float w, float defaultValue) {
        if (u == null) {
            return null;
        }
        float fu = u;
        float fv = (v == null ? defaultValue : v);
        float fw = (w == null ? defaultValue : w);
        return FloatTuples.create(fu, fv, fw);
    }

    /**
     * Creates a {@link FloatTuple} from the given RGB values, treating
     * optional values as described in the MTL specification: If
     * the <code>r</code> component is <code>null</code>, then
     * <code>null</code> is returned. If the <code>g</code> or
     * <code>b</code> component is null, then the <code>r</code>
     * component will be used instead.
     *
     * @param r The r-component
     * @param g The g-component
     * @param b The b-component
     * @return The {@link FloatTuple}
     */
    public static FloatTuple createRgbTuple(Float r, Float g, Float b) {
        if (r == null) {
            return null;
        }
        float fr = r;
        float fg = r;
        float fb = r;
        if (g != null) {
            fg = g;
        }
        if (b != null) {
            fb = b;
        }
        return FloatTuples.create(fr, fg, fb);
    }

    /**
     * Returns an array of FloatTuples representing the vertices of the object
     *
     * @param obj The DefaultObj object from which to retrieve the vertices
     * @param vertexIndices An array of vertex indices specifying which vertices to retrieve
     * @return An array of FloatTuples representing the vertices
     */
    public static FloatTuple[] getVertices(DefaultObj obj, int[] vertexIndices) {
        FloatTuple[] vertices = new FloatTuple[vertexIndices.length];
        for (int i = 0; i < vertexIndices.length; i++) {
            vertices[i] = obj.getVertex(vertexIndices[i]);
        }
        return vertices;
    }

    public static FloatTuple[] getVertices(DefaultObj obj, DefaultObjFace face) {
        return getVertices(obj, face.getVertexIndices());
    }

    public static DefaultObjFace createFaceWithNewIndices(ObjFace face, int... n) {
        int[] v = new int[n.length];
        int[] vt = null;
        int[] vn = null;

        for (int i = 0; i < n.length; i++) {
            v[i] = face.getVertexIndex(n[i]);
        }

        if (face.containsTexCoordIndices()) {
            vt = new int[n.length];

            for (int i = 0; i < n.length; i++) {
                vt[i] = face.getTexCoordIndex(n[i]);
            }
        }

        if (face.containsNormalIndices()) {
            vn = new int[n.length];

            for (int i = 0; i < n.length; i++) {
                vn[i] = face.getNormalIndex(n[i]);
            }
        }

        return new DefaultObjFace(v, vt, vn);
    }

    /**
     * Copy all vertices, texture coordinates and normals from the input to the output
     * @param input The input object
     * @param output The output object
     */
    public static void copyAllVertices(ReadableObj input, WritableObj output) {
        for (int i = 0; i < input.getNumVertices(); i++) {
            output.addVertex(input.getVertex(i));
        }

        for (int i = 0; i < input.getNumTexCoords(); i++) {
            output.addTexCoord(input.getTexCoord(i));
        }

        for (int i = 0; i < input.getNumNormals(); i++) {
            output.addNormal(input.getNormal(i));
        }
    }

    /**
     * Copy all faces and groups from the input to the output
     * @param source The source object
     * @param target The target object
     */
    public static void copyAllFacesAndGroups(DefaultObj source, DefaultObj target) {
        // Store the copied faces so we don't end up adding multiple copies of the same face
        Map<DefaultObjFace, DefaultObjFace> srcToTarFaceMap = new HashMap<>();

        // Copy the groups (and their faces)
        for (int i = 0; i < source.getNumGroups(); i++) {
            DefaultObjGroup srcGroup = (DefaultObjGroup) source.getGroup(i);
            DefaultObjGroup tarGroup = new DefaultObjGroup(srcGroup.getName());
            for (int j = 0; j < srcGroup.getNumFaces(); j++) {
                DefaultObjFace srcFace = (DefaultObjFace) srcGroup.getFace(j);
                DefaultObjFace storedFace = srcToTarFaceMap.get(srcFace);

                DefaultObjFace tarFace = storedFace != null ? storedFace : new DefaultObjFace(srcFace);
                tarGroup.addFace(tarFace);
                srcToTarFaceMap.put(srcFace, tarFace);
            }
            target.addGroup(tarGroup);
        }

        // Copy the faces
        for (int i = 0; i < source.getNumFaces(); i++) {
            DefaultObjFace srcFace = (DefaultObjFace) source.getFace(i);
            DefaultObjFace tarFace = srcToTarFaceMap.get(srcFace);
            tarFace = tarFace != null ? tarFace : new DefaultObjFace(srcFace);
            target.addFace(tarFace);
        }
    }

    /**
     * Activates the groups and materials specified by the given face in the input object,
     * and sets the active groups and material in the output object accordingly.
     *
     * @param input The input object from which to activate the groups and materials
     * @param face The face containing the groups and materials to activate
     * @param output The output object in which to set the active groups and materials
     */
    public static void activateGroups(ReadableObj input, ObjFace face, WritableObj output) {
        Set<String> activatedGroupNames = input.getActivatedGroupNames(face);
        if (activatedGroupNames != null) {
            output.setActiveGroupNames(activatedGroupNames);
        }

        String activatedMaterialGroupName = input.getActivatedMaterialGroupName(face);
        if (activatedMaterialGroupName != null) {
            output.setActiveMaterialGroupName(activatedMaterialGroupName);
        }

    }
}
