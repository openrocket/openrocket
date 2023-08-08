package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.ThicknessRingComponent;
import net.sf.openrocket.util.Coordinate;

public class ThicknessRingComponentExporter extends RocketComponentExporter {
    public ThicknessRingComponentExporter(DefaultObj obj, ThicknessRingComponent component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, component, groupName, LOD);
    }

    @Override
    public void addToObj() {
        final ThicknessRingComponent thicknessRing = (ThicknessRingComponent) component;

        final float outerRadius = (float) thicknessRing.getOuterRadius();
        final float innerRadius = (float) thicknessRing.getInnerRadius();
        final float length = (float) thicknessRing.getLength();
        final Coordinate[] locations = thicknessRing.getComponentLocations();

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
