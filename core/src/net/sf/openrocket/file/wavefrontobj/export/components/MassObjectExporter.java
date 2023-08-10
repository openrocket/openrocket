package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.RocketComponentUtils;

public class MassObjectExporter extends RocketComponentExporter<MassObject> {
    public MassObjectExporter(DefaultObj obj, MassObject component, String groupName,
                              ObjUtils.LevelOfDetail LOD, CoordTransform transformer) {
        super(obj, component, groupName, LOD, transformer);
    }

    @Override
    public void addToObj() {
        obj.setActiveGroupNames(groupName);

        final Coordinate[] locations = component.getComponentLocations();
        final int numSides = LOD.getValue() / 2;
        final int numStacks = LOD.getValue() / 2;

        // Generate the mesh
        for (Coordinate location : locations) {
            generateMesh(numSides, numStacks, location);
        }
    }

    private void generateMesh(int numSides, int numStacks, Coordinate location) {
        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        int startIdx = obj.getNumVertices();
        int normalsStartIdx = obj.getNumNormals();
        double dy = component.getLength() / numStacks;
        double da = 2.0f * Math.PI / numSides;

        // Generate vertices and normals
        for (int j = 0; j <= numStacks; j++) {
            double y = j * dy;

            if (j == 0 || j == numStacks) {
                // Add a center vertex
                obj.addVertex(0, (float) y, 0);
                obj.addNormal(0, j == 0 ? -1 : 1, 0);
            } else {
                // Add a vertex for each side
                for (int i = 0; i < numSides; i++) {
                    double angle = i * da;
                    double r = RocketComponentUtils.getMassObjectRadius(component, y);
                    float x = (float) (r * Math.cos(angle));
                    float z = (float) (r * Math.sin(angle));

                    obj.addVertex(x, (float) y, z);

                    // Add normals
                    if (Double.compare(r, component.getRadius()) == 0) {
                        obj.addNormal(x, 0, z);
                    } else {
                        final double yCenter;
                        if (j <= numStacks/2) {
                            yCenter = RocketComponentUtils.getMassObjectArcHeight(component);
                        } else {
                            yCenter = component.getLength() - RocketComponentUtils.getMassObjectArcHeight(component);
                        }
                        obj.addNormal(x, (float) (y - yCenter), z);     // For smooth shading
                    }
                }
            }
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);        // Clamp in case no vertices were added

        // Create bottom tip faces
        for (int i = 0; i < numSides; i++) {
            int nextIdx = (i + 1) % numSides;

            int[] vertexIndices = new int[] {
                    0,              // Center vertex
                    1 + i,
                    1 + nextIdx,
            };
            int[] normalIndices = vertexIndices.clone();   // For a smooth surface, the vertex and normal indices are the same

            ObjUtils.offsetIndex(normalIndices, normalsStartIdx);
            ObjUtils.offsetIndex(vertexIndices, startIdx);      // Only do this after normals are added, since the vertex indices are used for normals

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }

        // Create normal side faces
        for (int j = 0; j < numStacks-2; j++) {
            for (int i = 0; i < numSides; i++) {
                int nextIdx = (i + 1) % numSides;

                int[] vertexIndices = new int[] {
                        1 + j * numSides + i,
                        1 + (j + 1) * numSides + i,
                        1 + (j + 1) * numSides + nextIdx,
                        1 + j * numSides + nextIdx
                };
                int[] normalIndices = vertexIndices.clone();   // For a smooth surface, the vertex and normal indices are the same

                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);
                ObjUtils.offsetIndex(vertexIndices, startIdx);      // Only do this after normals are added, since the vertex indices are used for normals

                DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);
            }
        }

        // Create top tip faces
        final int normalEndIdx = obj.getNumNormals() - 1;
        for (int i = 0; i < numSides; i++) {
            int nextIdx = (i + 1) % numSides;
            int[] vertexIndices = new int[] {
                    endIdx,           // Center vertex
                    endIdx - numSides + nextIdx,
                    endIdx - numSides + i,
            };
            int[] normalIndices = new int[] {
                    normalEndIdx,           // Center vertex
                    normalEndIdx - numSides + nextIdx,
                    normalEndIdx - numSides + i,
            };

            // Don't offset! We reference from the last index

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }

        // Translate the mesh to the position in the rocket
        //      We will create an offset location that has the same effect as the axial rotation of the mass object
        Coordinate offsetLocation = getOffsetLocation(component, location);
        ObjUtils.translateVerticesFromComponentLocation(obj, component, startIdx, endIdx, offsetLocation, -component.getLength());
    }

    private static Coordinate getOffsetLocation(MassObject massObject, Coordinate location) {
        // ! This is all still referenced to the OpenRocket coordinate system, not the OBJ one
        final double radialPosition = massObject.getRadialPosition();
        final double radialDirection = massObject.getRadialDirection();
        final double y = location.y + radialPosition * Math.cos(radialDirection);
        final double z = location.z + radialPosition * Math.sin(radialDirection);
        return new Coordinate(location.x, y, z);
    }
}
