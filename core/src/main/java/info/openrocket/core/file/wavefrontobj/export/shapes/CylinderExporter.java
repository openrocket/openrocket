package info.openrocket.core.file.wavefrontobj.export.shapes;

import com.sun.istack.NotNull;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.DefaultObjFace;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.util.MathUtil;

import java.util.List;

public class CylinderExporter {
    /**
     * Adds a cylinder mesh to the given obj
     * @param obj The obj to add the mesh to
     * @param transformer The coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     * @param groupName The name of the group to add the mesh to, or null if no group should be added (use the active group)
     * @param foreRadius The fore (top) radius of the cylinder
     * @param aftRadius The aft (bottom) radius of the cylinder
     * @param length The length of the cylinder
     * @param numSides The number of sides of the cylinder
     * @param solid Whether the cylinder should be solid (true) or hollow (false)
     *                 NOTE: Culling is not really thought of for the hollow cylinder; this mode is really meant to be
     *                       combined with other objects
     * @param isOutside Whether the cylinder is an outside face (true) or inside face (false)
     * @param uMin The minimum u texture coordinate
     * @param uMax The maximum u texture coordinate
     * @param vMin The minimum v texture coordinate
     * @param vMax The maximum v texture coordinate
     * @param foreRingVertices A list to add the fore (top) ring vertex indices to
     * @param aftRingVertices A list to add the aft (bottom) ring vertex indices to
     * @param foreRingNormals A list to add the fore (top) ring normal indices to
     * @param aftRingNormals A list to add the aft (bottom) ring normal indices to
     */
    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float foreRadius, float aftRadius, float length, int numSides, boolean solid, boolean isOutside,
                                       float uMin, float uMax, float vMin, float vMax,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices,
                                       List<Integer> foreRingNormals, List<Integer> aftRingNormals) {
        // Set the new group
        if (groupName != null) {
            obj.setActiveGroupNames(groupName);
        }

        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        int startIdx = obj.getNumVertices();
        int texCoordsStartIdx = obj.getNumTexCoords();
        int normalsStartIdx = obj.getNumNormals();

        if (solid) {
            // Top center vertex
            obj.addVertex(transformer.convertLoc(0, 0, 0));
            obj.addNormal(transformer.convertLocWithoutOriginOffs(isOutside ? -1 : 1, 0, 0));

            // Bottom center vertex
            obj.addVertex(transformer.convertLoc(length, 0, 0));
            obj.addNormal(transformer.convertLocWithoutOriginOffs(isOutside ? 1 : -1, 0, 0));
        }

        // Generate side top vertices
        generateRingVertices(obj, transformer, numSides, 0, length, length, foreRadius, aftRadius, isOutside,
                uMin, uMax, vMin, vMax, foreRingVertices, foreRingNormals);

        // Generate side bottom vertices
        generateRingVertices(obj, transformer, numSides, length, 0, length, aftRadius, foreRadius, isOutside,
                uMin, uMax, vMin, vMax, aftRingVertices, aftRingNormals);

        // Create faces for the bottom and top
        if (solid) {
            for (int i = 0; i < numSides; i++) {
                int nextIdx = (i + 1) % numSides;

                // Bottom face
                //// Vertices
                int[] vertexIndices = new int[] {
                        0,              // Bottom center vertex
                        2 + i,
                        2 + nextIdx
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);
                ObjUtils.offsetIndex(vertexIndices, startIdx);

                //// Normals
                int[] normalIndices = new int[]{0, 0, 0};
                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);

                //// Texture coordinates
                // TODO?

                DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);

                // Top face
                //// Vertices
                vertexIndices = new int[] {
                        1,                  // Top center vertex
                        2 + numSides + ((i + 1) % numSides),
                        2 + numSides + i
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);
                ObjUtils.offsetIndex(vertexIndices, startIdx);

                //// Normals
                normalIndices = new int[] {1, 1, 1};
                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);

                //// Texture coordinates
                // TODO

                face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);
            }
        }

        // Create faces for the sides
        for (int i = 0; i < numSides; i++) {
            final int nextIdx = (i + 1) % numSides;
            final int offset = solid ? 2 : 0;             // Offset by 2 to skip the bottom and top center vertices

            //// Vertices
            int[] vertexIndices = new int[]{
                    i,                                  // Bottom-left of quad
                    numSides + i,                       // Top-left of quad
                    numSides + nextIdx,                 // Top-right of quad
                    nextIdx,                            // Bottom-right of quad
            };
            vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);

            //// Normals
            int[] normalIndices = vertexIndices.clone();        // No need to reverse winding, already done by vertices

            //// Texture coordinates
            int[] texCoordIndices = new int[]{
                    i,                                  // Bottom-left of quad
                    numSides+1 + i,                     // Top-left of quad
                    numSides+1 + i+1,                   // Top-right of quad (don't use nextIdx, we don't want roll-over)
                    i+1,                                // Bottom-right of quad (don't use nextIdx, we don't want roll-over)
            };
            texCoordIndices = ObjUtils.reverseIndexWinding(texCoordIndices, !isOutside);

            ObjUtils.offsetIndex(normalIndices, normalsStartIdx + offset);
            ObjUtils.offsetIndex(texCoordIndices, texCoordsStartIdx);
            ObjUtils.offsetIndex(vertexIndices, startIdx + offset);     // ! Only add offset here, otherwise you mess up the indices for the normals

            DefaultObjFace face = new DefaultObjFace(vertexIndices, texCoordIndices, normalIndices);
            obj.addFace(face);
        }
    }
    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float foreRadius, float aftRadius, float length, int numSides, boolean solid, boolean isOutside,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices,
                                       List<Integer> foreRingNormals, List<Integer> aftRingNormals) {
        addCylinderMesh(obj, transformer, groupName, foreRadius, aftRadius, length, numSides, solid, isOutside,
                0, 1, 0, 1, foreRingVertices, aftRingVertices, foreRingNormals, aftRingNormals);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float foreRadius, float aftRadius, float length, int numSides, boolean solid, boolean isOutside,
                                       float uMin, float uMax, float vMin, float vMax,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices) {
        addCylinderMesh(obj, transformer, groupName, foreRadius, aftRadius, length, numSides, solid, isOutside,
                uMin, uMax, vMin, vMax, foreRingVertices, aftRingVertices, null, null);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float foreRadius, float aftRadius, float length, int numSides, boolean solid, boolean isOutside,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices) {
        addCylinderMesh(obj, transformer, groupName, foreRadius, aftRadius, length, numSides, solid, isOutside,
                foreRingVertices, aftRingVertices, null, null);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float length, int numSides, boolean solid, boolean isOutside,
                                       float uMin, float uMax, float vMin, float vMax,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, radius, length, numSides, solid, isOutside,
                uMin, uMax, vMin, vMax, foreRingVertices, aftRingVertices);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float length, int numSides, boolean solid, boolean isOutside,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, radius, length, numSides, solid, isOutside, foreRingVertices, aftRingVertices);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float length, int numSides, boolean solid,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, length, numSides, solid, true, foreRingVertices, aftRingVertices);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float length, boolean solid, boolean isOutside, int nrOfSlices,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, length, nrOfSlices, solid, isOutside, foreRingVertices, aftRingVertices);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float length, boolean solid, int nrOfSlices,
                                       List<Integer> foreRingVertices, List<Integer> aftRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, length, nrOfSlices, solid, foreRingVertices, aftRingVertices);
    }

    public static void generateRingVertices(DefaultObj obj, CoordTransform transformer,
                                            int numSides, float x, float nextX, float xMax, float radius, float nextRadius,
                                            boolean isOutside, float uMin, float uMax, float vMin, float vMax,
                                            List<Integer> vertexList, List<Integer> normalList) {
        int startIdx = obj.getNumVertices();
        int normalsStartIdx = obj.getNumNormals();

        for (int i = 0; i < numSides; i++) {
            final double angle = 2 * Math.PI * i / numSides;
            final float y = radius * (float) Math.cos(angle);
            final float z = radius * (float) Math.sin(angle);

            // Vertex
            obj.addVertex(transformer.convertLoc(x, y, z));

            // Normal
            //// We need special nx normal when the radius changes
            float nx;
            if (Float.compare(radius, nextRadius) != 0) {
                final double slopeAngle = Math.atan(Math.abs(nextX - x) / (nextRadius - radius));
                nx = (float) Math.cos(Math.PI - slopeAngle);
            } else {
                nx = 0;
            }
            float ny = (float) Math.cos(angle);
            float nz = (float) Math.sin(angle);

            nx = isOutside ? nx : -nx;
            ny = isOutside ? ny : -ny;
            nz = isOutside ? nz : -nz;

            obj.addNormal(transformer.convertLocWithoutOriginOffs(nx, ny, nz));     // This kind of normal ensures the object is smoothly rendered (like the 'Shade Smooth' option in Blender)

            if (vertexList != null) {
                vertexList.add(startIdx + i);
            }
            if (normalList != null) {
                normalList.add(normalsStartIdx + i);
            }

            // Texture coordinates

            float u = ((float) i) / numSides;
            u = (float) MathUtil.map(u, 0, 1, uMin, uMax);
            float v = isOutside ? (xMax - x) / xMax : x / xMax;       // For some reason, the texture is vertically flipped in OR for inside cylinders. Don't really like it, but it is what it is
            v = (float) MathUtil.map(v, 0, 1, vMin, vMax);
            obj.addTexCoord(u, v);
        }

        // Need to add a last texture coordinate for the end of the texture
        float v = isOutside ? (xMax - x) / xMax : x / xMax;
        v = (float) MathUtil.map(v, 0, 1, vMin, vMax);
        obj.addTexCoord(uMax, v);
    }
}
