package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.util.Coordinate;

public class LaunchLugExporter extends RocketComponentExporter {
    public LaunchLugExporter(DefaultObj obj, LaunchLug component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, component, groupName, LOD);
    }

    @Override
    public void addToObj() {
        final LaunchLug lug = (LaunchLug) component;

        final Coordinate[] locations = lug.getComponentLocations();
        final float outerRadius = (float) lug.getOuterRadius();
        final float innerRadius = (float) lug.getInnerRadius();
        final float length = (float) lug.getLength();

        // Generate the mesh
        for (Coordinate location : locations) {
            generateMesh(lug, outerRadius, innerRadius, length, location);
        }
    }

    private void generateMesh(LaunchLug lug, float outerRadius, float innerRadius, float length, Coordinate location) {
        int startIdx = obj.getNumVertices();

        // Generate the instance mesh
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
        ObjUtils.translateVerticesFromComponentLocation(obj, lug, startIdx, endIdx, location, -length);
    }
}
