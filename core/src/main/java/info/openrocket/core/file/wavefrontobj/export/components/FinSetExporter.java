package info.openrocket.core.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import de.javagl.obj.FloatTuple;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.file.wavefrontobj.export.shapes.PolygonExporter;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class FinSetExporter extends RocketComponentExporter<FinSet> {
    public FinSetExporter(@NotNull DefaultObj obj, FlightConfiguration config, @NotNull CoordTransform transformer,
                          FinSet component, String groupName, ObjUtils.LevelOfDetail LOD, WarningSet warnings) {
        super(obj, config, transformer, component, groupName, LOD, warnings);
    }

    @Override
    public void addToObj() {
        obj.setActiveGroupNames(groupName);

        final Coordinate[] points = component.getFinPointsWithRoot();
        final Coordinate[] tabPoints = component.getTabPointsWithRoot();
        final Coordinate[] tabPointsReversed = new Coordinate[tabPoints.length];        // We need clockwise points for the PolygonExporter
        for (int i = 0; i < tabPoints.length; i++) {
            tabPointsReversed[i] = tabPoints[tabPoints.length - i - 1];
        }
        final FloatPoints floatPoints = getPointsAsFloat(points);
        final FloatPoints floatTabPoints = getPointsAsFloat(tabPointsReversed);
        final float thickness = (float) component.getThickness();
        boolean hasTabs = component.getTabLength() > 0 && component.getTabHeight() > 0;

        if (Float.compare(thickness, 0) == 0) {
            warnings.add(Warning.OBJ_ZERO_THICKNESS, component);
        }

        // Generate the fin meshes
        for (InstanceContext context : config.getActiveInstances().getInstanceContexts(component)) {
            generateMesh(floatPoints, floatTabPoints, thickness, hasTabs, context);
        }
    }

    private void generateMesh(FloatPoints floatPoints, FloatPoints floatTabPoints, float thickness,
                              boolean hasTabs, InstanceContext context) {
        // Generate the mesh
        final int startIdx = obj.getNumVertices();
        final int normalsStartIdx = obj.getNumNormals();

        // Generate the instance mesh
        PolygonExporter.addPolygonMesh(obj, transformer, null,
                floatPoints.getXCoords(), floatPoints.getYCoords(), thickness);

        // Generate the fin tabs
        if (hasTabs) {
            PolygonExporter.addPolygonMesh(obj, transformer, null,
                    floatTabPoints.getXCoords(), floatTabPoints.getYCoords(), thickness);
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);                  // Clamp in case no vertices were added
        int normalsEndIdx = Math.max(obj.getNumNormals() - 1, normalsStartIdx);     // Clamp in case no normals were added

        // First rotate for the cant angle
        /*
        Note: I first thought you had to do the cant rotation with the fin center as origin, but you just have to
        rotate around the fin start. The offset due to the cant rotation around the fin start is already taken care of by
        the component location.
         */
        FloatTuple rot = transformer.convertRot(0, component.getCantAngle(), 0);
        FloatTuple orig = transformer.convertLoc(0, 0, 0);
        ObjUtils.rotateVertices(obj, startIdx, endIdx, normalsStartIdx, normalsEndIdx,
                rot.getX(), rot.getY(), rot.getZ(),
                orig.getX(), orig.getY(), orig.getZ());

        // Then do the component rotation (axial rotation)
        final double rotX = context.transform.getXrotation();
        final double rotY = context.transform.getYrotation();
        final double rotZ = context.transform.getZrotation();
        rot = transformer.convertRot(rotX, rotY, rotZ);
        ObjUtils.rotateVertices(obj, startIdx, endIdx, normalsStartIdx, normalsEndIdx,
                rot.getX(), rot.getY(), rot.getZ(),
                orig.getX(), orig.getY(), orig.getZ());

        // Translate the mesh to the position in the rocket
        final Coordinate location = context.getLocation();
        ObjUtils.translateVerticesFromComponentLocation(obj, transformer, startIdx, endIdx, location);
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
