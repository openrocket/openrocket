package net.sf.openrocket.file.wavefrontobj.export.shapes;

import de.javagl.obj.FloatTuple;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;

import java.util.List;

public class DiskExporter {
    /**
     * Adds a disk mesh to the obj by using existing outer and inner vertices
     * @param obj The obj to add the mesh to
     * @param groupName The name of the group to add the mesh to
     * @param outerVertices The indices of the outer vertices
     * @param innerVertices The indices of the inner vertices, or null if the disk is solid
     * @param isTopFace Whether the disk is a top face (true) or bottom face (false)
     */
    public static void closeDiskMesh(DefaultObj obj, String groupName, List<Integer> outerVertices, List<Integer> innerVertices,
                                     boolean isTopFace) {
        if (outerVertices.isEmpty()) {
            throw new IllegalArgumentException("Outer vertices cannot be empty");
        }
        // If there is only one vertex, this is a tip of an object (0 radius)
        if (outerVertices.size() == 1) {
            return;
        }
        boolean isSolid = innerVertices == null || innerVertices.size() == 1;
        boolean useInnerVertexAsCenter = innerVertices != null && innerVertices.size() == 1;     // Whether you should use the inner vertex as the center of the solid disk
        if (!isSolid && innerVertices.size() > 1 && outerVertices.size() != innerVertices.size()) {
            throw new IllegalArgumentException("Outer and inner vertices must have the same size");
        }

        // Set the new group
        if (groupName != null) {
            obj.setActiveGroupNames(groupName);
        }

        // Flat disk, so all vertices have the same normal
        obj.addNormal(0, isTopFace ? 1 : -1, 0);        // TODO: hm, what if the object is rotated? If the disk is not drawn in the y direction?
        final int normalIndex = obj.getNumNormals() - 1;

        if (isSolid) {
            // Add the center vertex
            final int centerVertexIdx;
            if (useInnerVertexAsCenter) {
                centerVertexIdx = innerVertices.get(0);
            } else {
                final FloatTuple centerVertex = ObjUtils.averageVertices(obj, outerVertices);
                obj.addVertex(centerVertex);
                centerVertexIdx = obj.getNumVertices() - 1;
            }

            // Add the faces
            for (int i = 0; i < outerVertices.size(); i++) {
                int nextIdx = (i + 1) % outerVertices.size();
                int[] vertexIndices = new int[] {
                        centerVertexIdx,
                        outerVertices.get(nextIdx),
                        outerVertices.get(i),
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, isTopFace);

                int[] normalIndices = new int[] { normalIndex, normalIndex, normalIndex };
                DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);
            }
        } else {
            // Add the faces
            for (int i = 0; i < outerVertices.size(); i++) {
                int nextIdx = (i + 1) % outerVertices.size();
                int[] vertexIndices = new int[] {
                        outerVertices.get(i),           // Bottom-left of quad
                        outerVertices.get(nextIdx),      // Bottom-right of quad
                        innerVertices.get(nextIdx),     // Top-right of quad
                        innerVertices.get(i),           // Top-left of quad
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, isTopFace);

                int[] normalIndices = new int[] { normalIndex, normalIndex, normalIndex, normalIndex };
                DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);
            }
        }
    }

    /**
     * Adds a (closed) disk mesh to the obj by using existing outer and inner vertices
     * @param obj The obj to add the mesh to
     * @param groupName The name of the group to add the mesh to
     * @param outerVertices The indices of the outer vertices
     * @param isTopFace Whether the disk is a top face (true) or bottom face (false)
     */
    public static void closeDiskMesh(DefaultObj obj, String groupName, List<Integer> outerVertices,
                                     boolean isTopFace) {
        closeDiskMesh(obj, groupName, outerVertices, null, isTopFace);
    }

}
