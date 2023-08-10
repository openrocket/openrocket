package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;
import net.sf.openrocket.util.Coordinate;

public class ThicknessRingComponentExporter extends RocketComponentExporter<ThicknessRingComponent> {
    public ThicknessRingComponentExporter(DefaultObj obj, ThicknessRingComponent component, String groupName,
                                          ObjUtils.LevelOfDetail LOD, CoordTransform transformer) {
        super(obj, component, groupName, LOD, transformer);
    }

    @Override
    public void addToObj() {
        final float outerRadius = (float) component.getOuterRadius();
        final float innerRadius = (float) component.getInnerRadius();
        final float length = (float) component.getLength();
        final Coordinate[] locations = component.getComponentLocations();

        // Generate the mesh
        for (Coordinate location : locations) {
            generateMesh(outerRadius, innerRadius, length, location);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float length, Coordinate location) {
        int startIdx = obj.getNumVertices();

        if (Float.compare(innerRadius, 0) == 0) {
            CylinderExporter.addCylinderMesh(obj, groupName, outerRadius, length, true, LOD);
        } else {
            if (Float.compare(innerRadius, outerRadius) == 0) {
                CylinderExporter.addCylinderMesh(obj, groupName, outerRadius, length, false, LOD);
            } else {
                TubeExporter.addTubeMesh(obj, groupName, outerRadius, innerRadius, length, LOD);
            }
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);    // Clamp in case no vertices were added

        // Translate the mesh to the position in the rocket
        ObjUtils.translateVerticesFromComponentLocation(obj, component, startIdx, endIdx, location, -length);
    }
}
