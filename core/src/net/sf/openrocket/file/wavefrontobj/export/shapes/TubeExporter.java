package net.sf.openrocket.file.wavefrontobj.export.shapes;

import com.sun.istack.NotNull;
import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;

import java.util.List;

import static net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter.generateRingVertices;

public class TubeExporter {
    /**
     * Add a tube mesh to the obj.
     * It is drawn in the origin of the OBJ coordinate system. The longitudinal axis is positive, converted OpenRocket x-axis.
     * @param obj The obj to add the mesh to
     * @param groupName The name of the group to add the mesh to, or null if no group should be added (use the active group)
     * @param aftOuterRadius The outer radius of the aft (bottom) of the tube
     * @param foreOuterRadius The outer radius of the fore (top) of the tube
     * @param aftInnerRadius The inner radius of the aft (bottom) of the tube
     * @param foreInnerRadius The inner radius of the fore (top) of the tube
     * @param length The length of the tube
     * @param numSides The number of sides of the tube
     * @param bottomOuterVertices A list to add the indices of the bottom outer vertices to, or null if the indices are not needed
     * @param topOuterVertices A list to add the indices of the top outer vertices to, or null if the indices are not needed
     * @param bottomInnerVertices A list to add the indices of the bottom inner vertices to, or null if the indices are not needed
     * @param topInnerVertices A list to add the indices of the top inner vertices to, or null if the indices are not needed
     */
    public static void addTubeMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                   float aftOuterRadius, float foreOuterRadius,
                                   float aftInnerRadius, float foreInnerRadius, float length, int numSides,
                                   List<Integer> bottomOuterVertices, List<Integer> topOuterVertices,
                                   List<Integer> bottomInnerVertices, List<Integer> topInnerVertices) {
        if (aftInnerRadius > aftOuterRadius || foreInnerRadius > foreOuterRadius) {
            throw new IllegalArgumentException("Inner radius must be less than outer radius");
        }

        // Set the new group
        if (groupName != null) {
            obj.setActiveGroupNames(groupName);
        }

        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        int startIdx = obj.getNumVertices();
        int normalsStartIdx = obj.getNumNormals();

        obj.addNormal(transformer.convertLocWithoutOriginOffs(-1, 0, 0));       // Top faces normal
        obj.addNormal(transformer.convertLocWithoutOriginOffs(1, 0, 0));        // Bottom faces normal

        // Generate top outside vertices
        generateRingVertices(obj, transformer, startIdx, numSides, 0, foreOuterRadius, true, topOuterVertices);

        // Generate top inside vertices
        generateRingVertices(obj, transformer, startIdx + numSides, numSides, 0, foreInnerRadius, false, topInnerVertices);

        // Generate bottom outside vertices
        generateRingVertices(obj, transformer, startIdx + 2*numSides, numSides, length, aftOuterRadius, true, bottomOuterVertices);

        // Generate bottom inside vertices
        generateRingVertices(obj, transformer, startIdx + 3*numSides, numSides, length, aftInnerRadius, false, bottomInnerVertices);

        // Create top faces
        for (int i = 0; i < numSides; i++) {
            int[] vertexIndices = new int[] {
                    i,                                  // Bottom-left of quad outside vertex
                    ((i + 1) % numSides),               // Bottom-right of quad outside vertex
                    numSides + ((i + 1) % numSides),    // Top-right of quad inside vertex
                    numSides + i,                       // Top-left of quad inside vertex
            };
            ObjUtils.offsetIndex(vertexIndices, startIdx);
            int[] normalIndices = new int[] {0, 0, 0, 0};
            ObjUtils.offsetIndex(normalIndices, normalsStartIdx);

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }

        // Create bottom faces
        for (int i = 0; i < numSides; i++) {
            int[] vertexIndices = new int[] {
                    2*numSides + i,                         // Bottom-left of quad outside vertex
                    3*numSides + i,                         // Top-left of quad inside vertex
                    3*numSides + ((i + 1) % numSides),      // Top-right of quad inside vertex
                    2*numSides + ((i + 1) % numSides),      // Bottom-right of quad outside vertex
            };
            ObjUtils.offsetIndex(vertexIndices, startIdx);

            int[] normalIndices = new int[] {1, 1, 1, 1};
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
            ObjUtils.offsetIndex(vertexIndices, startIdx);

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
            ObjUtils.offsetIndex(vertexIndices, startIdx);

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

    public static void addTubeMesh(DefaultObj obj, CoordTransform transformer, String groupName,
                                   float outerRadius, float innerRadius, float length, int numSides) {
        addTubeMesh(obj, transformer, groupName, outerRadius, outerRadius, innerRadius, innerRadius, length, numSides,
                null, null, null, null);
    }

    public static void addTubeMesh(DefaultObj obj, CoordTransform transformer, String groupName,
                                   float outerRadius, float innerRadius, float length, ObjUtils.LevelOfDetail LOD) {
        addTubeMesh(obj, transformer, groupName, outerRadius, outerRadius, innerRadius, innerRadius, length, LOD.getNrOfSides(outerRadius),
                null, null, null, null);
    }
}
