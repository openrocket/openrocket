package net.sf.openrocket.file.wavefrontobj.export.shapes;

import de.javagl.obj.ObjWriter;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class TubeExporter {
    /**
     * Add a tube mesh to the obj. The longitudinal axis is the y axis (in OBJ coordinate system).
     * @param obj The obj to add the mesh to
     * @param groupName The name of the group to add the mesh to, or null if no group should be added (use the active group)
     * @param bottomOuterRadius The outer radius of the bottom of the tube
     * @param topOuterRadius The outer radius of the top of the tube
     * @param bottomInnerRadius The inner radius of the bottom of the tube
     * @param topInnerRadius The inner radius of the top of the tube
     * @param height The height of the tube
     * @param numSides The number of sides of the tube
     * @param bottomOuterVertices A list to add the indices of the bottom outer vertices to, or null if the indices are not needed
     * @param topOuterVertices A list to add the indices of the top outer vertices to, or null if the indices are not needed
     * @param bottomInnerVertices A list to add the indices of the bottom inner vertices to, or null if the indices are not needed
     * @param topInnerVertices A list to add the indices of the top inner vertices to, or null if the indices are not needed
     */
    public static void addTubeMesh(DefaultObj obj, String groupName,
                                   float bottomOuterRadius, float topOuterRadius,
                                   float bottomInnerRadius, float topInnerRadius, float height, int numSides,
                                   List<Integer> bottomOuterVertices, List<Integer> topOuterVertices,
                                   List<Integer> bottomInnerVertices, List<Integer> topInnerVertices) {
        if (bottomInnerRadius > bottomOuterRadius || topInnerRadius > topOuterRadius) {
            throw new IllegalArgumentException("Inner radius must be less than outer radius");
        }

        // Set the new group
        if (groupName != null) {
            obj.setActiveGroupNames(groupName);
        }

        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        int verticesStartIdx = obj.getNumVertices();
        int normalsStartIdx = obj.getNumNormals();

        obj.addNormal(0, -1, 0);        // Bottom faces normal
        obj.addNormal(0, 1, 0);         // Top faces normal

        // Generate bottom outside vertices
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            float x = bottomOuterRadius * (float) Math.cos(angle);
            float z = bottomOuterRadius * (float) Math.sin(angle);

            obj.addVertex(x, 0, z);
            obj.addNormal(x, 0, z);     // This kind of normal ensures the object is smoothly rendered (like the 'Shade Smooth' option in Blender)

            if (bottomOuterVertices != null) {
                bottomOuterVertices.add(verticesStartIdx + i);
            }
        }

        // Generate bottom inside vertices
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            float x = bottomInnerRadius * (float) Math.cos(angle);
            float z = bottomInnerRadius * (float) Math.sin(angle);

            obj.addVertex(x, 0, z);
            obj.addNormal(x, 0, z);     // For smooth shading

            if (bottomInnerVertices != null) {
                bottomInnerVertices.add(verticesStartIdx + numSides + i);
            }
        }

        // Generate top outside vertices
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            float x = topOuterRadius * (float) Math.cos(angle);
            float z = topOuterRadius * (float) Math.sin(angle);

            // Side top vertices
            obj.addVertex(x, height, z);
            obj.addNormal(x, 0, z);

            if (topOuterVertices != null) {
                topOuterVertices.add(verticesStartIdx + 2*numSides + i);
            }
        }

        // Generate top inside vertices
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            float x = topInnerRadius * (float) Math.cos(angle);
            float z = topInnerRadius * (float) Math.sin(angle);

            // Side top vertices
            obj.addVertex(x, height, z);
            obj.addNormal(x, 0, z);

            if (topInnerVertices != null) {
                topInnerVertices.add(verticesStartIdx + 3*numSides + i);
            }
        }

        // Create bottom faces
        for (int i = 0; i < numSides; i++) {
            int[] vertexIndices = new int[]{
                    i,                                  // Bottom-left of quad outside vertex
                    ((i + 1) % numSides),               // Bottom-right of quad outside vertex
                    numSides + ((i + 1) % numSides),    // Top-right of quad inside vertex
                    numSides + i                        // Top-left of quad inside vertex
            };
            ObjUtils.offsetIndex(vertexIndices, verticesStartIdx);
            int[] normalIndices = new int[]{0, 0, 0, 0};
            ObjUtils.offsetIndex(normalIndices, normalsStartIdx);

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }

        // Create top faces
        for (int i = 0; i < numSides; i++) {
            int[] vertexIndices = new int[]{
                    2*numSides + i,                         // Bottom-left of quad outside vertex
                    3*numSides + i,                         // Top-left of quad inside vertex
                    3*numSides + ((i + 1) % numSides),      // Top-right of quad inside vertex
                    2*numSides + ((i + 1) % numSides)       // Bottom-right of quad outside vertex
            };
            ObjUtils.offsetIndex(vertexIndices, verticesStartIdx);

            int[] normalIndices = new int[]{1, 1, 1, 1};
            ObjUtils.offsetIndex(normalIndices, normalsStartIdx);

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }

        // Create outside side faces
        for (int i = 0; i < numSides; i++) {
            final int nextIdx = (i + 1) % numSides;
            int[] vertexIndices = new int[]{
                    i,                                  // Bottom-left of quad outside vertex
                    2*numSides + i,                     // Top-left of quad outside vertex
                    2*numSides + nextIdx,               // Top-right of quad outside vertex
                    nextIdx,                            // Bottom-right of quad outside vertex
            };
            ObjUtils.offsetIndex(vertexIndices, verticesStartIdx);

            int[] normalIndices = new int[]{
                    i,                                  // Bottom-left of quad outside vertex
                    2*numSides + i,                     // Top-left of quad outside vertex
                    2*numSides + nextIdx,               // Top-right of quad outside vertex
                    nextIdx,                            // Bottom-right of quad outside vertex
            };
            ObjUtils.offsetIndex(normalIndices, normalsStartIdx + 2);       // Extra 2 offset for bottom and top normals

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }

        // Create inside side faces
        for (int i = 0; i < numSides; i++) {
            final int nextIdx = (i + 1) % numSides;
            int[] vertexIndices = new int[]{
                    numSides + i,                           // Bottom-left of quad inside vertex
                    numSides + nextIdx,                     // Bottom-right of quad inside vertex
                    3*numSides + nextIdx,                   // Top-right of quad inside vertex
                    3*numSides + i,                         // Top-left of quad inside vertex
            };
            ObjUtils.offsetIndex(vertexIndices, verticesStartIdx);

            int[] normalIndices = new int[]{
                    numSides + i,                           // Bottom-left of quad inside vertex
                    numSides + nextIdx,                     // Bottom-right of quad inside vertex
                    3*numSides + nextIdx,                   // Top-right of quad inside vertex
                    3*numSides + i,                         // Top-left of quad inside vertex
            };
            ObjUtils.offsetIndex(normalIndices, normalsStartIdx + 2);       // Extra 2 offset for bottom and top normals

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }
    }

    public static void addTubeMesh(DefaultObj obj, String groupName,
                                   float bottomOuterRadius, float topOuterRadius,
                                   float bottomInnerRadius, float topInnerRadius, float height, ObjUtils.LevelOfDetail LOD,
                                   List<Integer> bottomOuterVertices, List<Integer> topOuterVertices,
                                   List<Integer> bottomInnerVertices, List<Integer> topInnerVertices) {
        addTubeMesh(obj, groupName, bottomOuterRadius, topOuterRadius, bottomInnerRadius, topInnerRadius, height,
                LOD.getNrOfSides(Math.max(bottomOuterRadius, topOuterRadius)),
                bottomOuterVertices, topOuterVertices, bottomInnerVertices, topInnerVertices);
    }

    public static void addTubeMesh(DefaultObj obj, String groupName,
                                   float bottomOuterRadius, float topOuterRadius,
                                   float bottomInnerRadius, float topInnerRadius, float height, ObjUtils.LevelOfDetail LOD) {
        addTubeMesh(obj, groupName, bottomOuterRadius, topOuterRadius, bottomInnerRadius, topInnerRadius, height,
                LOD.getNrOfSides(Math.max(bottomOuterRadius, topOuterRadius)),
                null, null, null, null);
    }

    public static void addTubeMesh(DefaultObj obj, String groupName, float outerRadius, float innerRadius, float height, int numSides) {
        addTubeMesh(obj, groupName, outerRadius, outerRadius, innerRadius, innerRadius, height, numSides,
                null, null, null, null);
    }

    public static void addTubeMesh(DefaultObj obj, String groupName, float bottomOuterRadius, float topOuterRadius,
                                         float bottomInnerRadius, float topInnerRadius, float height) {
        addTubeMesh(obj, groupName, bottomOuterRadius, topOuterRadius, bottomInnerRadius, topInnerRadius, height, ObjUtils.LevelOfDetail.NORMAL);
    }

    public static void addTubeMesh(DefaultObj obj, String groupName, float outerRadius, float innerRadius, float height,
                                   ObjUtils.LevelOfDetail LOD,
                                   List<Integer> bottomOuterVertices, List<Integer> topOuterVertices,
                                   List<Integer> bottomInnerVertices, List<Integer> topInnerVertices) {
        addTubeMesh(obj, groupName, outerRadius, outerRadius, innerRadius, innerRadius, height, LOD.getNrOfSides(outerRadius),
                bottomOuterVertices, topOuterVertices, bottomInnerVertices, topInnerVertices);
    }

    public static void addTubeMesh(DefaultObj obj, String groupName, float outerRadius, float innerRadius, float height,
                                   int nrOfSlices,
                                   List<Integer> bottomOuterVertices, List<Integer> topOuterVertices,
                                   List<Integer> bottomInnerVertices, List<Integer> topInnerVertices) {
        addTubeMesh(obj, groupName, outerRadius, outerRadius, innerRadius, innerRadius, height, nrOfSlices,
                bottomOuterVertices, topOuterVertices, bottomInnerVertices, topInnerVertices);
    }

    public static void addTubeMesh(DefaultObj obj, String groupName, float outerRadius, float innerRadius, float height,
                                   ObjUtils.LevelOfDetail LOD) {
        addTubeMesh(obj, groupName, outerRadius, outerRadius, innerRadius, innerRadius, height, LOD.getNrOfSides(outerRadius),
                null, null, null, null);
    }

    public static void addTubeMesh(DefaultObj obj, String groupName, float outerRadius, float innerRadius, float height) {
        addTubeMesh(obj, groupName, outerRadius, outerRadius, innerRadius, innerRadius, height);
    }

    public static void main(String[] args) throws Exception {
        DefaultObj obj = new DefaultObj();
        //addTubeMesh(obj, "tube", 0.1f, 0.085f, 0.3f);
        addTubeMesh(obj, "tube", 0.14f, 0.06f, 0.13f, 0.05f, 0.3f);
        try (OutputStream objOutputStream = new FileOutputStream("/Users/SiboVanGool/Downloads/tube.obj")) {
            ObjWriter.write(obj, objOutputStream);
        }
    }
}
