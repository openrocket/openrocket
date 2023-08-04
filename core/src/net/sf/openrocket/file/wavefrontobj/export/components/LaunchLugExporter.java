package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;

public class LaunchLugExporter extends RocketComponentExporter {
    public LaunchLugExporter(DefaultObj obj, LaunchLug component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, component, groupName, LOD);
    }

    @Override
    public void addToObj() {
        final LaunchLug lug = (LaunchLug) component;

        final Coordinate[] locations = lug.getComponentLocations();
        final double rocketLength = lug.getRocket().getLength();
        final float outerRadius = (float) lug.getOuterRadius();
        final float innerRadius = (float) lug.getInnerRadius();
        final float length = (float) lug.getLength();

        // Generate the mesh
        for (Coordinate location : locations) {
            generateMesh(lug, outerRadius, innerRadius, length, rocketLength, location);
        }
    }

    private void generateMesh(LaunchLug lug, float outerRadius, float innerRadius, float length, double rocketLength, Coordinate location) {
        int startIdx2 = obj.getNumVertices();

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

        int endIdx2 = Math.max(obj.getNumVertices() - 1, startIdx2);    // Clamp in case no vertices were added

        // Translate the lug instance
        final float x = (float) location.y;
        final float y = (float) (rocketLength - lug.getLength() - location.x);
        final float z = (float) location.z;
        ObjUtils.translateVertices(obj, startIdx2, endIdx2, x, y, z);
    }
}
