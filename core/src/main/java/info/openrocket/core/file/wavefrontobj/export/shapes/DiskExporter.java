package info.openrocket.core.file.wavefrontobj.export.shapes;

import com.sun.istack.NotNull;
import de.javagl.obj.FloatTuple;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.DefaultObjFace;
import info.openrocket.core.file.wavefrontobj.ObjUtils;

import java.util.List;

public class DiskExporter {
    /**
     * Adds a disk mesh to the obj by using existing outer and inner vertices
     * @param obj The obj to add the mesh to
     * @param transformer The coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     * @param groupName The name of the group to add the mesh to
     * @param outerVertices The indices of the outer vertices
     * @param innerVertices The indices of the inner vertices, or null if the disk is solid
     * @param isClockwise Whether the vertices are in clockwise order (true) or counter-clockwise order (false)
     * @param isTopFace Whether the disk is a top face (true) or bottom face (false)
     * @param uMin The minimum u texture coordinate
     * @param uMax The maximum u texture coordinate
     * @param vMin The minimum v texture coordinate
     * @param vMax The maximum v texture coordinate
     */
    public static void closeDiskMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                     @NotNull List<Integer> outerVertices, List<Integer> innerVertices,
                                     boolean isClockwise, boolean isTopFace,
                                     float uMin, float uMax, float vMin, float vMax) {
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
        obj.addNormal(transformer.convertLocWithoutOriginOffs(isTopFace ? -1 : 1, 0, 0));        // TODO: hm, what if the object is rotated? If the disk is not drawn in the y direction?
        final int normalIndex = obj.getNumNormals() - 1;

        final int texCoordsStartIdx = obj.getNumTexCoords();
        final int numSides = outerVertices.size();

        // Create the texture coordinates
        //// Outer vertices
        for (int i = 0; i <= numSides; i++) {
            final float u = uMin + ((float) i) / numSides * (uMax - uMin);
            obj.addTexCoord(u, vMin);
        }
        //// Inner vertices
        for (int i = 0; i <= numSides; i++) {
            final float u = uMin + ((float) i) / numSides * (uMax - uMin);
            obj.addTexCoord(u, vMax);
        }

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

            // Add the triangle faces
            for (int i = 0; i < outerVertices.size(); i++) {
                int nextIdx = (i + 1) % outerVertices.size();

                // Vertices
                int[] vertexIndices = new int[] {
                        centerVertexIdx,
                        outerVertices.get(nextIdx),
                        outerVertices.get(i),
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, isTopFace != isClockwise);

                // Normals
                int[] normalIndices = new int[] { normalIndex, normalIndex, normalIndex };

                // Texture coordinates
                int[] texCoordsIndices = new int[] {
                        numSides+1 + i,
                        i+1,
                        i,
                };
                texCoordsIndices = ObjUtils.reverseIndexWinding(texCoordsIndices, isTopFace != isClockwise);
                ObjUtils.offsetIndex(texCoordsIndices, texCoordsStartIdx);

                DefaultObjFace face = new DefaultObjFace(vertexIndices, texCoordsIndices, normalIndices);
                obj.addFace(face);
            }
        } else {
            // Add the quad faces
            for (int i = 0; i < outerVertices.size(); i++) {
                int nextIdx = (i + 1) % outerVertices.size();

                // Vertices
                int[] vertexIndices = new int[] {
                        outerVertices.get(i),           // Bottom-left of quad
                        innerVertices.get(i),           // Top-left of quad
                        innerVertices.get(nextIdx),     // Top-right of quad
                        outerVertices.get(nextIdx),     // Bottom-right of quad
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, isTopFace != isClockwise);

                // Normals
                int[] normalIndices = new int[] { normalIndex, normalIndex, normalIndex, normalIndex };

                // Texture coordinates
                int[] texCoordsIndices = new int[] {
                        i,
                        numSides+1 + i,
                        numSides+1 + i+1,
                        i+1,
                };
                texCoordsIndices = ObjUtils.reverseIndexWinding(texCoordsIndices, isTopFace != isClockwise);
                ObjUtils.offsetIndex(texCoordsIndices, texCoordsStartIdx);


                DefaultObjFace face = new DefaultObjFace(vertexIndices, texCoordsIndices, normalIndices);
                obj.addFace(face);
            }
        }
    }

    public static void closeDiskMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                     @NotNull List<Integer> outerVertices, List<Integer> innerVertices,
                                     boolean isClockwise, boolean isTopFace) {
        // By default, OpenRocket doesn't really render textures on disks (often edges of tubes), so we don't either
        closeDiskMesh(obj, transformer, groupName, outerVertices, innerVertices, isClockwise, isTopFace, 0, 0, 0, 0);
    }

    public static void closeDiskMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                     @NotNull List<Integer> outerVertices, boolean isClockwise, boolean isTopFace,
                                     float uMin, float uMax, float vMin, float vMax) {
        closeDiskMesh(obj, transformer, groupName, outerVertices, null, isClockwise, isTopFace, uMin, uMax, vMin, vMax);
    }


    /**
     * Adds a (closed) disk mesh to the obj by using existing outer and inner vertices
     * @param obj The obj to add the mesh to
     * @param transformer The coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     * @param groupName The name of the group to add the mesh to
     * @param outerVertices The indices of the outer vertices
     * @param isClockwise Whether the vertices are in clockwise order (true) or counter-clockwise order (false)
     * @param isTopFace Whether the disk is a top face (true) or bottom face (false)
     */
    public static void closeDiskMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                     @NotNull List<Integer> outerVertices, boolean isClockwise, boolean isTopFace) {
        // By default, OpenRocket doesn't really render textures on disks (often edges of tubes), so we don't either
        closeDiskMesh(obj, transformer, groupName, outerVertices, isClockwise, isTopFace, 0, 0, 0, 0);
    }

}
