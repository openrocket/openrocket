package net.sf.openrocket.file.wavefrontobj.export.shapes;

import de.javagl.obj.ObjWriter;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class CylinderExporter {
    /**
     * Adds a cylinder mesh to the given obj
     * @param obj The obj to add the mesh to
     * @param groupName The name of the group to add the mesh to, or null if no group should be added (use the active group)
     * @param radius The radius of the cylinder
     * @param height The height of the cylinder
     * @param numSides The number of sides of the cylinder
     * @param solid Whether the cylinder should be solid (true) or hollow (false)
     *                 NOTE: Culling is not really thought of for the hollow cylinder; this mode is really meant to be
     *                       combined with other objects
     * @param isOutside Whether the cylinder is an outside face (true) or inside face (false)
     * @param bottomRingVertices A list to add the bottom ring vertex indices to
     * @param topRingVertices A list to add the top ring vertex indices to
     */
    public static void addCylinderMesh(DefaultObj obj, String groupName,
                                       float radius, float height, int numSides, boolean solid, boolean isOutside,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        // Set the new group
        if (groupName != null) {
            obj.setActiveGroupNames(groupName);
        }

        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        int verticesStartIdx = obj.getNumVertices();
        int normalsStartIdx = obj.getNumNormals();

        if (solid) {
            // Bottom center vertex
            obj.addVertex(0, 0, 0);
            obj.addNormal(0, isOutside ? -1 : 1, 0);

            // Top center vertex
            obj.addVertex(0, height, 0);
            obj.addNormal(0, isOutside ? 1 : -1, 0);
        }

        // Generate side bottom vertices
        int tmpStartIdx = obj.getNumVertices();
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            float x = radius * (float) Math.cos(angle);
            float z = radius * (float) Math.sin(angle);

            obj.addVertex(x, 0, z);
            final float nx = isOutside ? x : -x;
            final float nz = isOutside ? z : -z;
            obj.addNormal(nx, 0, nz);     // This kind of normal ensures the object is smoothly rendered (like the 'Shade Smooth' option in Blender)

            if (bottomRingVertices != null) {
                bottomRingVertices.add(tmpStartIdx + i);
            }
        }

        // Generate side top vertices
        tmpStartIdx = obj.getNumVertices();
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            float x = radius * (float) Math.cos(angle);
            float z = radius * (float) Math.sin(angle);

            obj.addVertex(x, height, z);
            final float nx = isOutside ? x : -x;
            final float nz = isOutside ? z : -z;
            obj.addNormal(nx, height, nz);     // For smooth shading

            if (topRingVertices != null) {
                topRingVertices.add(tmpStartIdx + i);
            }
        }

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
                ObjUtils.offsetIndex(vertexIndices, verticesStartIdx);

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
                ObjUtils.offsetIndex(vertexIndices, verticesStartIdx);

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
            ObjUtils.offsetIndex(vertexIndices, verticesStartIdx + offset);     // ! Only add offset here, otherwise you mess up the indices for the normals

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }
    }

    public static void addCylinderMesh(DefaultObj obj, String groupName,
                                       float radius, float height, int numSides, boolean solid,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        addCylinderMesh(obj, groupName, radius, height, numSides, solid, true, bottomRingVertices, topRingVertices);
    }

    public static void addCylinderMesh(DefaultObj obj, String groupName, float radius, float height, boolean solid,
                                       ObjUtils.LevelOfDetail LOD,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        addCylinderMesh(obj, groupName, radius, height, LOD.getNrOfSides(radius), solid, bottomRingVertices, topRingVertices);
    }

    public static void addCylinderMesh(DefaultObj obj, String groupName, float radius, float height, boolean solid,
                                       boolean isOutside, int nrOfSlices,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        addCylinderMesh(obj, groupName, radius, height, nrOfSlices, solid, isOutside, bottomRingVertices, topRingVertices);
    }

    public static void addCylinderMesh(DefaultObj obj, String groupName, float radius, float height, boolean solid,
                                       int nrOfSlices,
                                       List<Integer> bottomRingVertices, List<Integer> topRingVertices) {
        addCylinderMesh(obj, groupName, radius, height, nrOfSlices, solid, bottomRingVertices, topRingVertices);
    }

    public static void addCylinderMesh(DefaultObj obj, String groupName, float radius, float height, boolean solid,
                                       ObjUtils.LevelOfDetail LOD) {
        addCylinderMesh(obj, groupName, radius, height, LOD.getNrOfSides(radius), solid, null, null);
    }

    public static void addCylinderMesh(DefaultObj obj, String groupName, float radius, float height, boolean solid) {
        addCylinderMesh(obj, groupName, radius, height, solid, ObjUtils.LevelOfDetail.NORMAL);
    }

    public static void main(String[] args) throws Exception {
        DefaultObj obj = new DefaultObj();
        addCylinderMesh(obj, "cylinder", 1, 2, 15, true, null, null);
        try (OutputStream objOutputStream = new FileOutputStream("/Users/SiboVanGool/Downloads/cylinder.obj")) {
            ObjWriter.write(obj, objOutputStream);
        }
    }
}
