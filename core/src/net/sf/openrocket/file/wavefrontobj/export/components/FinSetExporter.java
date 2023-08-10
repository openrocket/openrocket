package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.PolygonExporter;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class FinSetExporter extends RocketComponentExporter<FinSet> {
    public FinSetExporter(DefaultObj obj, FinSet component, String groupName,
                          ObjUtils.LevelOfDetail LOD, CoordTransform transformer) {
        super(obj, component, groupName, LOD, transformer);
    }

    @Override
    public void addToObj() {
        obj.setActiveGroupNames(groupName);

        final Coordinate[] points = component.getFinPointsWithRoot();
        final Coordinate[] tabPoints = component.getTabPoints();
        final Coordinate[] tabPointsReversed = new Coordinate[tabPoints.length];
        for (int i = 0; i < tabPoints.length; i++) {
            tabPointsReversed[i] = tabPoints[tabPoints.length - i - 1];
        }
        final FloatPoints floatPoints = getPointsAsFloat(points);
        final FloatPoints floatTabPoints = getPointsAsFloat(tabPointsReversed);
        final float thickness = (float) component.getThickness();
        boolean hasTabs = component.getTabLength() > 0 && component.getTabHeight() > 0;
        final Coordinate[] locations = component.getComponentLocations();
        final Coordinate[] angles = component.getComponentAngles();

        if (locations.length != angles.length) {
            throw new IllegalArgumentException("Number of locations and angles must match");
        }

        // Generate the fin meshes
        for (int i = 0; i < locations.length; i++) {
            generateMesh(floatPoints, floatTabPoints, thickness, hasTabs, locations[i], angles[i]);
        }
    }

    private void generateMesh(FloatPoints floatPoints, FloatPoints floatTabPoints, float thickness,
                              boolean hasTabs, Coordinate location, Coordinate angle) {
        // Generate the mesh
        final int startIdx = obj.getNumVertices();
        final int normalsStartIdx = obj.getNumNormals();

        // Generate the instance mesh
        PolygonExporter.addPolygonMesh(obj, null,
                floatPoints.getXCoords(), floatPoints.getYCoords(), thickness);

        // Generate the fin tabs
        if (hasTabs) {
            PolygonExporter.addPolygonMesh(obj, null,
                    floatTabPoints.getXCoords(), floatTabPoints.getYCoords(), thickness);
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);                  // Clamp in case no vertices were added
        int normalsEndIdx = Math.max(obj.getNumNormals() - 1, normalsStartIdx);     // Clamp in case no normals were added

        // First rotate for the cant angle
        final float cantAngle = (float) -component.getCantAngle();
        ObjUtils.rotateVertices(obj, startIdx, endIdx, normalsStartIdx, normalsEndIdx,
                cantAngle, 0, 0, 0, (float) -component.getLength(), 0);

        // Then do the axial rotation
        final float axialRot = (float) angle.x;
        ObjUtils.rotateVertices(obj, startIdx, endIdx, normalsStartIdx, normalsEndIdx,
                0, axialRot, 0, 0, 0, 0);

        // Translate the mesh to the position in the rocket
        ObjUtils.translateVerticesFromComponentLocation(obj, component, startIdx, endIdx, location, 0);
    }

    /**
     * Converts the double fin points to float fin points and removes any duplicate points (OBJ can't handle this).
     * @param points The fin points
     * @return The fin points as floats
     */
    private FloatPoints getPointsAsFloat(Coordinate[] points) {
        // We first want to remove duplicate points, so we'll keep track of indices that are correct
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            final int nextIdx = (i + 1) % points.length;
            if (!points[i].equals(points[nextIdx])) {
                indices.add(i);
            }
        }

        final int targetLength = indices.size();
        final float[] xCoords = new float[targetLength];
        final float[] yCoords = new float[targetLength];

        // Fill the arrays with the x and y values of each coordinate
        for (int i = 0; i < targetLength; i++) {
            xCoords[i] = (float) points[indices.get(i)].x;
            yCoords[i] = (float) points[indices.get(i)].y;
        }

        return new FloatPoints(xCoords, yCoords);
    }

    private record FloatPoints(float[] xCoords, float[] yCoords) {
        public float[] getXCoords() {
            return xCoords;
        }

        public float[] getYCoords() {
            return yCoords;
        }
    }
}
