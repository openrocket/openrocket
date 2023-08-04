package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.PolygonExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.util.Coordinate;

public class TubeFinSetExporter extends RocketComponentExporter {
    public TubeFinSetExporter(DefaultObj obj, TubeFinSet component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, component, groupName, LOD);
    }

    @Override
    public void addToObj() {
        final TubeFinSet tubeFinSet = (TubeFinSet) component;

        obj.setActiveGroupNames(groupName);

        final float outerRadius = (float) tubeFinSet.getOuterRadius();
        final float innerRadius = (float) tubeFinSet.getInnerRadius();
        final float length = (float) tubeFinSet.getLength();
        final Coordinate[] locations = tubeFinSet.getComponentLocations();
        final double[] angles = tubeFinSet.getInstanceAngles();
        final double rocketLength = tubeFinSet.getRocket().getLength();

        if (locations.length != angles.length) {
            throw new IllegalArgumentException("Number of locations and angles must match");
        }

        // Generate the fin meshes
        for (int i = 0; i < locations.length; i++) {
            generateMesh(outerRadius, innerRadius, length, rocketLength, locations[i], angles[i]);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float length, double rocketLength, Coordinate location, double angle) {
        // Create the fin meshes
        final int startIdx = obj.getNumVertices();

        // Generate the instance mesh
        TubeExporter.addTubeMesh(obj, null, outerRadius, innerRadius, length, LOD.getValue());

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);                  // Clamp in case no vertices were added

        // Translate the mesh
        final float dx = outerRadius * (float) Math.cos(angle);
        final float dz = outerRadius * (float) Math.sin(angle);
        final float x = (float) location.y + dx;
        final float y = (float) (rocketLength - length - location.x);
        final float z = (float) location.z + dz;
        ObjUtils.translateVertices(obj, startIdx, endIdx, x, y, z);
    }
}
