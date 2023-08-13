package net.sf.openrocket.file.wavefrontobj.export.shapes;

import com.sun.istack.NotNull;
import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;

import java.util.List;

public class CylinderExporter {
    /**
     * Adds a cylinder mesh to the given obj
     * @param obj The obj to add the mesh to
     * @param transformer The coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     * @param groupName The name of the group to add the mesh to, or null if no group should be added (use the active group)
     * @param radius The radius of the cylinder
     * @param length The length of the cylinder
     * @param numSides The number of sides of the cylinder
     * @param solid Whether the cylinder should be solid (true) or hollow (false)
     *                 NOTE: Culling is not really thought of for the hollow cylinder; this mode is really meant to be
     *                       combined with other objects
     * @param isOutside Whether the cylinder is an outside face (true) or inside face (false)
     * @param bottomRingVertices A list to add the bottom ring vertex indices to
     * @param topRingVertices A list to add the top ring vertex indices to
     */
    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float length, int numSides, boolean solid, boolean isOutside,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        // Set the new group
        if (groupName != null) {
            obj.setActiveGroupNames(groupName);
        }

        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        int startIdx = obj.getNumVertices();
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
        generateRingVertices(obj, transformer, startIdx, numSides, 0, radius, isOutside, topRingVertices);

        // Generate side bottom vertices
        generateRingVertices(obj, transformer, startIdx + numSides, numSides, length, radius, isOutside, bottomRingVertices);

        // Create faces for the bottom and top
        if (solid) {
            for (int i = 0; i < numSides; i++) {
                int nextIdx = (i + 1) % numSides;
                // Bottom face
                int[] vertexIndices = new int[] {
                        0,              // Bottom center vertex
                        2 + i,
                        2 + nextIdx
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);
                ObjUtils.offsetIndex(vertexIndices, startIdx);

                int[] normalIndices = new int[]{0, 0, 0};
                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);

                DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);

                // Top face
                vertexIndices = new int[] {
                        1,                  // Top center vertex
                        2 + numSides + ((i + 1) % numSides),
                        2 + numSides + i
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);
                ObjUtils.offsetIndex(vertexIndices, startIdx);

                normalIndices = new int[] {1, 1, 1};
                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);

                face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);
            }
        }

        // Create faces for the sides
        for (int i = 0; i < numSides; i++) {
            final int nextIdx = (i + 1) % numSides;
            final int offset = solid ? 2 : 0;             // Offset by 2 to skip the bottom and top center vertices

            int[] vertexIndices = new int[]{
                    i,                                  // Bottom-left of quad
                    numSides + i,                       // Top-left of quad
                    numSides + nextIdx,                 // Top-right of quad
                    nextIdx,                            // Bottom-right of quad
            };
            vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);

            int[] normalIndices = vertexIndices.clone();        // No need to reverse winding, already done by vertices

            ObjUtils.offsetIndex(normalIndices, normalsStartIdx + offset);
            ObjUtils.offsetIndex(vertexIndices, startIdx + offset);     // ! Only add offset here, otherwise you mess up the indices for the normals

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float height, int numSides, boolean solid,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, height, numSides, solid, true, bottomRingVertices, topRingVertices);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float height, boolean solid, ObjUtils.LevelOfDetail LOD,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, height, LOD.getNrOfSides(radius), solid, bottomRingVertices, topRingVertices);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float height, boolean solid, boolean isOutside, int nrOfSlices,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, height, nrOfSlices, solid, isOutside, bottomRingVertices, topRingVertices);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float height, boolean solid, int nrOfSlices,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        addCylinderMesh(obj, transformer, groupName, radius, height, nrOfSlices, solid, bottomRingVertices, topRingVertices);
    }

    public static void addCylinderMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                       float radius, float height, boolean solid, ObjUtils.LevelOfDetail LOD) {
        addCylinderMesh(obj, transformer, groupName, radius, height, LOD.getNrOfSides(radius), solid, null, null);
    }

    public static void generateRingVertices(DefaultObj obj, CoordTransform transformer, int startIdx,
                                            int numSides, float x, float radius, boolean isOutside,
                                            List<Integer> vertexList) {
        for (int i = 0; i < numSides; i++) {
            final double angle = 2 * Math.PI * i / numSides;
            final float y = radius * (float) Math.cos(angle);
            final float z = radius * (float) Math.sin(angle);

            // Side top vertices
            obj.addVertex(transformer.convertLoc(x, y, z));
            obj.addNormal(transformer.convertLocWithoutOriginOffs(0, isOutside ? y : -y, isOutside ? z : -z));     // This kind of normal ensures the object is smoothly rendered (like the 'Shade Smooth' option in Blender)

            if (vertexList != null) {
                vertexList.add(startIdx + i);
            }
        }
    }
}
