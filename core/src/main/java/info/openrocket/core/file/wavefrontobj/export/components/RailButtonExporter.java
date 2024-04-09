package info.openrocket.core.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import de.javagl.obj.FloatTuple;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.DefaultObjFace;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.file.wavefrontobj.export.shapes.CylinderExporter;
import info.openrocket.core.file.wavefrontobj.export.shapes.DiskExporter;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class RailButtonExporter extends RocketComponentExporter<RailButton> {
    /**
     * Wavefront OBJ exporter for a rail button.
     *
     * @param obj       The OBJ to export to
     * @param component The component to export
     * @param groupName The name of the group to export to
     * @param LOD       Level of detail to use for the export (e.g. '80')
     */
    public RailButtonExporter(@NotNull DefaultObj obj, FlightConfiguration config, @NotNull CoordTransform transformer,
                              RailButton component, String groupName, ObjUtils.LevelOfDetail LOD, WarningSet warnings) {
        super(obj, config, transformer, component, groupName, LOD, warnings);
    }

    @Override
    public void addToObj() {
        obj.setActiveGroupNames(groupName);

        final float outerRadius = (float) component.getOuterDiameter() / 2;
        final float innerRadius = (float) component.getInnerDiameter() / 2;
        final float baseHeight = (float) component.getBaseHeight();
        final float innerHeight = (float) component.getInnerHeight();
        final float flangeHeight = (float) component.getFlangeHeight();
        final float screwHeight = (float) component.getScrewHeight();

        // Generate the mesh
        for (InstanceContext context : config.getActiveInstances().getInstanceContexts(component)) {
            generateMesh(outerRadius, innerRadius, baseHeight, innerHeight, flangeHeight, screwHeight, context);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float baseHeight, float innerHeight, float flangeHeight,
                              float screwHeight, InstanceContext context) {
        final int startIdx = obj.getNumVertices();
        final int normalStartIdx = obj.getNumNormals();
        final int nrOfSides = LOD.getNrOfSides(outerRadius);

        // Generate base cylinder
        List<Integer> baseCylinderForeVertices = new ArrayList<>();
        List<Integer> baseCylinderAftVertices = new ArrayList<>();
        CylinderExporter.addCylinderMesh(obj, transformer, null, outerRadius, baseHeight, false, nrOfSides,
                baseCylinderForeVertices, baseCylinderAftVertices);

        // Generate inner cylinder
        int tmpStartIdx = obj.getNumVertices();
        List<Integer> innerCylinderForeVertices = new ArrayList<>();
        List<Integer> innerCylinderAftVertices = new ArrayList<>();
        CylinderExporter.addCylinderMesh(obj, transformer, null, innerRadius, innerHeight, false, nrOfSides,
                innerCylinderForeVertices, innerCylinderAftVertices);
        int tmpEndIdx = Math.max(obj.getNumVertices() - 1, tmpStartIdx);
        FloatTuple locOffs = transformer.convertLocWithoutOriginOffs(baseHeight, 0, 0);
        ObjUtils.translateVertices(obj, tmpStartIdx, tmpEndIdx, locOffs.getX(), locOffs.getY(), locOffs.getZ());

        // Generate flange cylinder
        tmpStartIdx = obj.getNumVertices();
        List<Integer> flangeCylinderForeVertices = new ArrayList<>();
        List<Integer> flangeCylinderAftVertices = new ArrayList<>();
        List<Integer> flangeCylinderAftNormals = new ArrayList<>();
        CylinderExporter.addCylinderMesh(obj, transformer, null, outerRadius, outerRadius, flangeHeight, nrOfSides,
                false, true, flangeCylinderForeVertices, flangeCylinderAftVertices, null, flangeCylinderAftNormals);
        tmpEndIdx = Math.max(obj.getNumVertices() - 1, tmpStartIdx);
        locOffs = transformer.convertLocWithoutOriginOffs(baseHeight + innerHeight, 0, 0);
        ObjUtils.translateVertices(obj, tmpStartIdx, tmpEndIdx, locOffs.getX(), locOffs.getY(), locOffs.getZ());

        // Generate base disk
        DiskExporter.closeDiskMesh(obj, transformer, null, baseCylinderForeVertices, false, true);

        // Generate base inner disk
        DiskExporter.closeDiskMesh(obj, transformer, null, baseCylinderAftVertices, innerCylinderForeVertices, false, false);

        // Generate flange inner disk
        DiskExporter.closeDiskMesh(obj, transformer, null, innerCylinderAftVertices, flangeCylinderForeVertices, true, true);

        // Generate flange disk/screw
        if (Float.compare(screwHeight, 0) == 0) {
            DiskExporter.closeDiskMesh(obj, transformer, null, flangeCylinderAftVertices, false, false);
        } else {
            addScrew(obj, baseHeight, innerHeight, flangeHeight, outerRadius, screwHeight, LOD,
                    flangeCylinderAftVertices, flangeCylinderAftNormals);
        }


        final int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);
        final int normalEndIdx = Math.max(obj.getNumNormals() - 1, normalStartIdx);

        // First orient the rail button correctly (upwards, not in the longitudinal direction)
        double rX = 0;
        double rY = 0;
        double rZ = Math.PI / 2;
        FloatTuple rot = transformer.convertRot(rX, rY, rZ);
        FloatTuple orig = transformer.convertLoc(0, 0, 0);
        ObjUtils.rotateVertices(obj, startIdx, endIdx, normalStartIdx, normalEndIdx,
                rot.getX(), rot.getY(), rot.getZ(),
                orig.getX(), orig.getY(), orig.getZ());

        // Then do the component rotation (axial rotation)
        final double rotX = context.transform.getXrotation() + component.getAngleOffset();
        final double rotY = context.transform.getYrotation();
        final double rotZ = context.transform.getZrotation();
        rot = transformer.convertRot(rotX, rotY, rotZ);
        ObjUtils.rotateVertices(obj, startIdx, endIdx, normalStartIdx, normalEndIdx,
                rot.getX(), rot.getY(), rot.getZ(),
                orig.getX(), orig.getY(), orig.getZ());

        // Translate the mesh to the position in the rocket
        final Coordinate location = context.getLocation();
        ObjUtils.translateVerticesFromComponentLocation(obj, transformer, startIdx, endIdx, location);
    }

    private void addScrew(DefaultObj obj, float baseHeight, float innerHeight, float flangeHeight, float outerRadius,
                          float screwHeight, ObjUtils.LevelOfDetail LOD,
                          List<Integer> flangeCylinderAftVertices, List<Integer> flangeCylinderAftNormals) {
        final int nrOfStacks = LOD.getValue() / 10;
        final int nrOfSlices = flangeCylinderAftVertices.size();
        final int startIdx = obj.getNumVertices();
        final int normalStartIdx = obj.getNumNormals();
        final float buttonHeight = baseHeight + innerHeight + flangeHeight;

        final float centerX = buttonHeight;

        if (flangeCylinderAftNormals.size() != nrOfSlices) {
            throw new IllegalArgumentException("The number of normals must be equal to the number of slices");
        }

        // Generate the mesh vertices (no tip)
        for (int i = 1; i <= nrOfStacks-1; i++) {
            for (int j = 0; j < nrOfSlices; j++) {
                final float theta = (float) (Math.PI / 2) - (i / (float) nrOfStacks) * (float) (Math.PI / 2); // Adjusted to start from pi/2 and decrease
                final float phi = (float) j / nrOfSlices * (float) (2.0 * Math.PI);

                final float x = centerX + (screwHeight * (float) Math.cos(theta));
                final float y = outerRadius * (float) Math.sin(theta) * (float) Math.cos(phi);
                final float z = outerRadius * (float) Math.sin(theta) * (float) Math.sin(phi);

                obj.addVertex(transformer.convertLoc(x, y, z));

                // Calculate the optimal normal
                float nx = x - centerX;
                obj.addNormal(transformer.convertLocWithoutOriginOffs(nx, y, z));
            }
        }

        // Generate the tip vertex
        obj.addVertex(transformer.convertLoc(buttonHeight + screwHeight, 0, 0));
        obj.addNormal(transformer.convertLocWithoutOriginOffs(1, 0, 0));

        // Generate the faces between the flange cylinder and the quad faces
        for (int i = 0; i < nrOfSlices; i++) {
            int nextIdx = (i + 1) % nrOfSlices;
            int[] vertexIndices = new int[] {
                    flangeCylinderAftVertices.get(i),           // Bottom-left of quad
                    startIdx + i,                               // Top-left of quad
                    startIdx + nextIdx,                         // Top-right of quad
                    flangeCylinderAftVertices.get(nextIdx),     // Bottom-right of quad
            };
            int[] normalIndices = new int[] {
                    flangeCylinderAftNormals.get(i),           // Bottom-left of quad
                    normalStartIdx + i,                         // Top-left of quad
                    normalStartIdx + nextIdx,                   // Top-right of quad
                    flangeCylinderAftNormals.get(nextIdx),     // Bottom-right of quad
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
