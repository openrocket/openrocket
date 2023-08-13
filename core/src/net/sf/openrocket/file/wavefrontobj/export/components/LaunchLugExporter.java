package net.sf.openrocket.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.util.Coordinate;

public class LaunchLugExporter extends RocketComponentExporter<LaunchLug> {
    public LaunchLugExporter(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, LaunchLug component,
                             String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, transformer, component, groupName, LOD);
    }

    @Override
    public void addToObj() {
        final Coordinate[] locations = component.getComponentLocations();
        final float outerRadius = (float) component.getOuterRadius();
        final float innerRadius = (float) component.getInnerRadius();
        final float length = (float) component.getLength();

        // Generate the mesh
        for (Coordinate location : locations) {
            generateMesh(outerRadius, innerRadius, length, location);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float length, Coordinate location) {
        int startIdx = obj.getNumVertices();

        // Generate the instance mesh
        if (Float.compare(innerRadius, 0) == 0) {
            CylinderExporter.addCylinderMesh(obj, transformer, groupName, outerRadius, length, true, LOD);
        } else {
            if (Float.compare(innerRadius, outerRadius) == 0) {
                CylinderExporter.addCylinderMesh(obj, transformer, groupName, outerRadius, length, false, LOD);
            } else {
                TubeExporter.addTubeMesh(obj, transformer, groupName, outerRadius, innerRadius, length, LOD);
            }
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);    // Clamp in case no vertices were added

        // Translate the mesh to the position in the rocket
        ObjUtils.translateVerticesFromComponentLocation(obj, transformer, startIdx, endIdx, location);
    }
}
