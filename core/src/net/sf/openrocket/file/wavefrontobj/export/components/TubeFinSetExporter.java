package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
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

        // Translate the mesh to the position in the rocket
        //      We will create an offset location that has the same effect as the axial rotation of the launch lug
        Coordinate offsetLocation = getOffsetLocation(outerRadius, location, angle);
        ObjUtils.translateVerticesFromComponentLocation(obj, component, startIdx, endIdx, offsetLocation, -length);
    }

    private static Coordinate getOffsetLocation(float outerRadius, Coordinate location, double angle) {
        // ! This is all still referenced to the OpenRocket coordinate system, not the OBJ one
        final float dy = outerRadius * (float) Math.cos(angle);
        final float dz = outerRadius * (float) Math.sin(angle);
        final double y = location.y + dy;
        final double z = location.z + dz;
        return new Coordinate(location.x, y, z);
    }
}
