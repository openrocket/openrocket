package info.openrocket.core.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.DefaultObjFace;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.RocketComponentUtils;

public class MassObjectExporter extends RocketComponentExporter<MassObject> {
    public MassObjectExporter(@NotNull DefaultObj obj, FlightConfiguration config, @NotNull CoordTransform transformer,
                              MassObject component, String groupName, ObjUtils.LevelOfDetail LOD, WarningSet warnings) {
        super(obj, config, transformer, component, groupName, LOD, warnings);
    }

    @Override
    public void addToObj() {
        obj.setActiveGroupNames(groupName);

        final int numSides = LOD.getValue() / 2;
        final int numStacks = LOD.getValue() / 2;

        // Generate the mesh
        for (InstanceContext context : config.getActiveInstances().getInstanceContexts(component)) {
            generateMesh(numSides, numStacks, context);
        }
    }

    private void generateMesh(int numSides, int numStacks, InstanceContext context) {
        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        int startIdx = obj.getNumVertices();
        int texCoordsStartIdx = obj.getNumTexCoords();
        int normalsStartIdx = obj.getNumNormals();
        double dx = component.getLength() / numStacks;
        double da = 2.0f * Math.PI / numSides;

        // Generate vertices, normals and UVs
        for (int j = 0; j <= numStacks; j++) {
            double x = j * dx;

            if (j == 0 || j == numStacks) {
                // Add a center vertex
                obj.addVertex(transformer.convertLoc(x, 0, 0));
                obj.addNormal(transformer.convertLocWithoutOriginOffs(j == 0 ? -1 : 1, 0, 0));
                for (int i = 0; i <= numSides; i++) {
                    final float u = (float) i / numSides;
                    final float v = j == 0 ? 1 : 0;
                    obj.addTexCoord(u, v);
                }
            } else {
                // Add a vertex for each side
                for (int i = 0; i < numSides; i++) {
                    double angle = i * da;
                    double r = RocketComponentUtils.getMassObjectRadius(component, x);
                    double y = r * Math.cos(angle);
                    double z = r * Math.sin(angle);

                    // Vertex
                    obj.addVertex(transformer.convertLoc(x, y, z));

                    // Normal
                    if (Double.compare(r, component.getRadius()) == 0) {        // If in cylindrical section, use cylinder normals
                        obj.addNormal(transformer.convertLocWithoutOriginOffs(0, y, z));
                    } else {
                        final double xCenter;
                        if (j <= numStacks/2) {
                            xCenter = RocketComponentUtils.getMassObjectArcHeight(component);
                        } else {
                            xCenter = component.getLength() - RocketComponentUtils.getMassObjectArcHeight(component);
                        }
                        obj.addNormal(transformer.convertLocWithoutOriginOffs(x - xCenter, y, z));     // For smooth shading
                    }

                    // Texture coordinate
                    final float u = (float) i / numSides;
                    final float v = (float) (numStacks-j) / numStacks;
                    obj.addTexCoord(u, v);
                }

                // Add final UV coordinate to close the texture
                final float u = 1f;
                final float v = (float) (numStacks-j) / numStacks;
                obj.addTexCoord(u, v);
            }
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);        // Clamp in case no vertices were added

        // Create top tip faces
        for (int i = 0; i < numSides; i++) {
            int nextIdx = (i + 1) % numSides;

            int[] vertexIndices = new int[] {
                    0,              // Center vertex
                    1 + i,
                    1 + nextIdx,
            };
            int[] normalIndices = vertexIndices.clone();   // For a smooth surface, the vertex and normal indices are the same
            int[] texCoordIndices = new int[] {
                    i,
                    numSides+1 + i,
                    numSides+1 + i+1,
            };

            ObjUtils.offsetIndex(normalIndices, normalsStartIdx);
            ObjUtils.offsetIndex(texCoordIndices, texCoordsStartIdx);
            ObjUtils.offsetIndex(vertexIndices, startIdx);      // Only do this after normals are added, since the vertex indices are used for normals

            DefaultObjFace face = new DefaultObjFace(vertexIndices, texCoordIndices, normalIndices);
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
                int[] texCoordIndices = new int[] {
                        j + j * numSides + i,
                        j+1 + (j + 1) * numSides + i,
                        j+1 + (j + 1) * numSides + i+1,
                        j + j * numSides + i+1
                };

                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);
                ObjUtils.offsetIndex(texCoordIndices, texCoordsStartIdx + numSides+1);
                ObjUtils.offsetIndex(vertexIndices, startIdx);      // Only do this after normals are added, since the vertex indices are used for normals

                DefaultObjFace face = new DefaultObjFace(vertexIndices, texCoordIndices, normalIndices);
                obj.addFace(face);
            }
        }

        // Create bottom tip faces
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
            int[] texCoordIndices = new int[] {
                    numSides+1 + i,
                    i+1,
                    i,
            };

            ObjUtils.offsetIndex(texCoordIndices, texCoordsStartIdx + ((numStacks-1) * (numSides + 1)) );
            // Don't offset vertices or normals! We reference from the last index

            DefaultObjFace face = new DefaultObjFace(vertexIndices, texCoordIndices, normalIndices);
            obj.addFace(face);
        }

        // Translate the mesh to the position in the rocket
        //      We will create an offset location that has the same effect as the axial rotation of the mass object
        final Coordinate location = context.getLocation();
        Coordinate offsetLocation = getOffsetLocation(location);
        ObjUtils.translateVerticesFromComponentLocation(obj, transformer, startIdx, endIdx, offsetLocation);
    }

    private Coordinate getOffsetLocation(Coordinate location) {
        // ! This is all still referenced to the OpenRocket coordinate system, not the OBJ one
        final double radialPosition = component.getRadialPosition();
        final double radialDirection = component.getRadialDirection();
        final double x = location.x;
        final double y = location.y + radialPosition * Math.cos(radialDirection);
        final double z = location.z + radialPosition * Math.sin(radialDirection);
        return new Coordinate(x, y, z);
    }
}
