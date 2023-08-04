package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;

public class BodyTubeExporter extends RocketComponentExporter {
    public BodyTubeExporter(DefaultObj obj, BodyTube component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, component, groupName, LOD);
    }

    @Override
    public void addToObj() {
        final BodyTube bodyTube = (BodyTube) component;

        final float outerRadius = (float) bodyTube.getOuterRadius();
        final float innerRadius = (float) bodyTube.getInnerRadius();
        final float length = (float) bodyTube.getLength();
        final boolean isFilled = bodyTube.isFilled();
        final double rocketLength = bodyTube.getRocket().getLength();
        final Coordinate[] locations = bodyTube.getComponentLocations();

        // Generate the mesh
        for (Coordinate location : locations) {
            generateMesh(outerRadius, innerRadius, length, isFilled, rocketLength, location);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float length, boolean isFilled,
                              double rocketLength, Coordinate location) {
        int startIdx = obj.getNumVertices();

        if (isFilled || Float.compare(innerRadius, 0) == 0) {
            CylinderExporter.addCylinderMesh(obj, groupName, outerRadius, length, true, LOD);
        } else {
            if (Float.compare(innerRadius, outerRadius) == 0) {
                CylinderExporter.addCylinderMesh(obj, groupName, outerRadius, length, false, LOD);
            } else {
                TubeExporter.addTubeMesh(obj, groupName, outerRadius, innerRadius, length, LOD);
            }
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);    // Clamp in case no vertices were added

        // Translate the mesh
        final float x = (float) location.y;
        final float y = (float) (rocketLength - length - location.x);
        final float z = (float) location.z;
        ObjUtils.translateVertices(obj, startIdx, endIdx, x, y, z);
    }
}
