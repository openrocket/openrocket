package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.RadiusRingComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;

public class RadiusRingComponentExporter extends RocketComponentExporter {
    public RadiusRingComponentExporter(DefaultObj obj, RadiusRingComponent component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, component, groupName, LOD);
    }

    @Override
    public void addToObj() {
        final RadiusRingComponent radiusRing = (RadiusRingComponent) component;

        float outerRadius = (float) radiusRing.getOuterRadius();
        float innerRadius = (float) radiusRing.getInnerRadius();
        float thickness = (float) radiusRing.getThickness();
        final double rocketLength = radiusRing.getRocket().getLength();
        final Coordinate[] locations = radiusRing.getComponentLocations();

        // Generate the mesh
        for (Coordinate location : locations) {
            generateMesh(outerRadius, innerRadius, thickness, rocketLength, location);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float thickness, double rocketLength, Coordinate location) {
        int startIdx = obj.getNumVertices();

        if (Float.compare(innerRadius, 0) == 0) {
            CylinderExporter.addCylinderMesh(obj, groupName, outerRadius, thickness, true, LOD);
        } else {
            if (Float.compare(innerRadius, outerRadius) == 0) {
                CylinderExporter.addCylinderMesh(obj, groupName, outerRadius, thickness, false, LOD);
            } else {
                TubeExporter.addTubeMesh(obj, groupName, outerRadius, innerRadius, thickness, LOD);
            }
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);    // Clamp in case no vertices were added

        // Translate the mesh
        final float x = (float) location.y;
        final float y = (float) (rocketLength - thickness - location.x);
        final float z = (float) location.z;
        ObjUtils.translateVertices(obj, startIdx, endIdx, x, y, z);
    }
}
