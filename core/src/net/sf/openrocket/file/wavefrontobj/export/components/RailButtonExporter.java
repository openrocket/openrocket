package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.DiskExporter;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class RailButtonExporter extends RocketComponentExporter {
    /**
     * Wavefront OBJ exporter for a rail button.
     *
     * @param obj       The OBJ to export to
     * @param component The component to export
     * @param groupName The name of the group to export to
     * @param LOD       Level of detail to use for the export (e.g. '80')
     */
    public RailButtonExporter(DefaultObj obj, RailButton component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, component, groupName, LOD);
    }

    @Override
    public void addToObj() {
        final RailButton railButton = (RailButton) component;

        obj.setActiveGroupNames(groupName);

        final float outerRadius = (float) railButton.getOuterDiameter() / 2;
        final float innerRadius = (float) railButton.getInnerDiameter() / 2;
        final float baseHeight = (float) railButton.getBaseHeight();
        final float innerHeight = (float) railButton.getInnerHeight();
        final float flangeHeight = (float) railButton.getFlangeHeight();
        final float screwHeight = (float) railButton.getScrewHeight();
        final Coordinate[] locations = railButton.getComponentLocations();
        final double[] angles = railButton.getComponentAngles();

        // Generate the mesh
        for (int i = 0; i < locations.length; i++) {
            generateMesh(outerRadius, innerRadius, baseHeight, innerHeight, flangeHeight, screwHeight,
                    locations[i], angles[i]);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float baseHeight, float innerHeight, float flangeHeight,
                              float screwHeight, Coordinate location, double angle) {
        final int startIdx = obj.getNumVertices();
        final int normalStartIdx = obj.getNumNormals();

        // Generate base cylinder
        List<Integer> baseCylinderBottomVertices = new ArrayList<>();
        List<Integer> baseCylinderTopVertices = new ArrayList<>();
        CylinderExporter.addCylinderMesh(obj, null, outerRadius, baseHeight, false, LOD,
                baseCylinderBottomVertices, baseCylinderTopVertices);

        // Generate inner cylinder
        int tmpStartIdx = obj.getNumVertices();
        List<Integer> innerCylinderBottomVertices = new ArrayList<>();
        List<Integer> innerCylinderTopVertices = new ArrayList<>();
        CylinderExporter.addCylinderMesh(obj, null, innerRadius, innerHeight, false, LOD,
                innerCylinderBottomVertices, innerCylinderTopVertices);
        int tmpEndIdx = Math.max(obj.getNumVertices() - 1, tmpStartIdx);
        ObjUtils.translateVertices(obj, tmpStartIdx, tmpEndIdx, 0, baseHeight, 0);

        // Generate flange cylinder
        tmpStartIdx = obj.getNumVertices();
        List<Integer> flangeCylinderBottomVertices = new ArrayList<>();
        List<Integer> flangeCylinderTopVertices = new ArrayList<>();
        CylinderExporter.addCylinderMesh(obj, null, outerRadius, flangeHeight, false, LOD,
                flangeCylinderBottomVertices, flangeCylinderTopVertices);
        tmpEndIdx = Math.max(obj.getNumVertices() - 1, tmpStartIdx);
        ObjUtils.translateVertices(obj, tmpStartIdx, tmpEndIdx, 0, baseHeight + innerHeight, 0);

        // Generate base disk
        DiskExporter.closeDiskMesh(obj, null, baseCylinderBottomVertices, true);        // Not a top face, but otherwise the culling isn't right...

        // Generate base inner disk
        DiskExporter.closeDiskMesh(obj, null, baseCylinderTopVertices, innerCylinderBottomVertices, true);

        // Generate flange inner disk
        DiskExporter.closeDiskMesh(obj, null, innerCylinderTopVertices, flangeCylinderBottomVertices, true);        // Not a top face, but otherwise the culling isn't right...

        // Generate flange disk/screw
        if (Float.compare(screwHeight, 0) == 0) {
            DiskExporter.closeDiskMesh(obj, null, flangeCylinderTopVertices, false);        // Not a bottom face, but otherwise the culling isn't right...
        } else {
            addScrew(obj, baseHeight, innerHeight, flangeHeight, outerRadius, screwHeight, LOD, flangeCylinderTopVertices);
        }


        final int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);
        final int normalEndIdx = Math.max(obj.getNumNormals() - 1, normalStartIdx);

        // Rotate the mesh (also PI/2!)
        final float rX = 0;
        final float rY = (float) angle;
        final float rZ = (float) - Math.PI / 2;
        ObjUtils.rotateVertices(obj, startIdx, endIdx, normalStartIdx, normalEndIdx,
                rX, rY, rZ, 0, 0, 0);

        ObjUtils.translateVertices(obj, startIdx, endIdx, 1, 0, 0);

        // Translate the mesh to the position in the rocket
        ObjUtils.translateVerticesFromComponentLocation(obj, component, startIdx, endIdx, location, 0);
    }

    private void addScrew(DefaultObj obj, float baseHeight, float innerHeight, float flangeHeight, float outerRadius,
                          float screwHeight, ObjUtils.LevelOfDetail LOD, List<Integer> flangeCylinderTopVertices) {
        final int nrOfStacks = LOD.getValue() / 10;
        final int nrOfSlices = flangeCylinderTopVertices.size();
        final int startIdx = obj.getNumVertices();
        final int normalStartIdx = obj.getNumNormals();
        final float buttonHeight = baseHeight + innerHeight + flangeHeight;

        final float centerX = 0;
        final float centerY = buttonHeight;
        final float centerZ = 0;

        // Generate the mesh vertices (no tip)
        for (int i = 1; i <= nrOfStacks-1; i++) {
            for (int j = 0; j < nrOfSlices; j++) {
                final float theta = (float) (Math.PI / 2) - (i / (float) nrOfStacks) * (float) (Math.PI / 2); // Adjusted to start from π/2 and decrease
                final float phi = (float) j / nrOfSlices * (float) (2.0 * Math.PI);

                final float x = outerRadius * (float) Math.sin(theta) * (float) Math.cos(phi);
                final float y = buttonHeight + (screwHeight * (float) Math.cos(theta)); // Now the y coordinate increases as theta decreases
                final float z = outerRadius * (float) Math.sin(theta) * (float) Math.sin(phi);

                obj.addVertex(x, y, z);

                // Calculate the optimal normal
                float nx = x - centerX;
                float ny = y - centerY;
                float nz = z - centerZ;
                obj.addNormal(nx, ny, nz);
            }
        }

        // Generate the tip vertex
        obj.addVertex(0, buttonHeight+screwHeight, 0);
        obj.addNormal(0, 1, 0);

        // Generate the faces between the flange cylinder and the quad faces
        for (int i = 0; i < nrOfSlices; i++) {
            int nextIdx = (i + 1) % nrOfSlices;
            int[] vertexIndices = new int[] {
                    flangeCylinderTopVertices.get(i),           // Bottom-left of quad
                    startIdx + i,                               // Top-left of quad
                    startIdx + nextIdx,                         // Top-right of quad
                    flangeCylinderTopVertices.get(nextIdx),     // Bottom-right of quad
            };
            int[] normalIndices = new int[] {
                    flangeCylinderTopVertices.get(i),           // Bottom-left of quad
                    normalStartIdx + i,                         // Top-left of quad
                    normalStartIdx + nextIdx,                   // Top-right of quad
                    flangeCylinderTopVertices.get(nextIdx),     // Bottom-right of quad
            };

            // No need to offset! We already directly reference the indices

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }

        // Generate the quad mesh faces (no tip)
        for (int i = 0; i <= nrOfStacks-3; i++) {       // We do -3 instead of -2 because we offset the i entirely by starting at 0 instead of 1 (so we don't have to offset the indices)
            for (int j = 0; j < nrOfSlices; j++) {
                int nextIdx = (j + 1) % nrOfSlices;
                int[] vertexIndices = new int[] {
                        i * nrOfSlices + j,             // Bottom-left of quad outside vertex
                        (i + 1) * nrOfSlices + j,       // Top-left of quad outside vertex
                        (i + 1) * nrOfSlices + nextIdx, // Top-right of quad inside vertex
                        i * nrOfSlices + nextIdx        // Bottom-right of quad inside vertex
                };
                int[] normalIndices = vertexIndices.clone();

                ObjUtils.offsetIndex(normalIndices, normalStartIdx);
                ObjUtils.offsetIndex(vertexIndices, startIdx);

                DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);
            }
        }

        // Generate the tip faces
        final int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);
        final int normalEndIdx = Math.max(obj.getNumNormals() - 1, normalStartIdx);
        for (int i = 0; i < nrOfSlices; i++) {
            int nextIdx = (i + 1) % nrOfSlices;
            int[] vertexIndices = new int[] {
                    endIdx,                         // Tip vertex
                    endIdx - nrOfSlices + nextIdx,
                    endIdx - nrOfSlices + i,
            };
            int[] normalIndices = new int[] {
                    normalEndIdx,                   // Tip normal
                    normalEndIdx - nrOfSlices + nextIdx,
                    normalEndIdx - nrOfSlices + i,
            };

            // Don't offset! We already reference from the end index

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }
    }
}
