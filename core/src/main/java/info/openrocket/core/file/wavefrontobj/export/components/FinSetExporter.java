package info.openrocket.core.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import de.javagl.obj.FloatTuple;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.DefaultObjFace;
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
                          FinSet component, String groupName, ObjUtils.LevelOfDetail LOD, boolean exportAllInstances,
                          WarningSet warnings) {
        super(obj, config, transformer, component, groupName, LOD, exportAllInstances, warnings);
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
        for (InstanceContext context : getInstanceContexts()) {
            generateMesh(floatPoints, floatTabPoints, thickness, hasTabs, context);
        }
    }

    private void generateMesh(FloatPoints floatPoints, FloatPoints floatTabPoints, float thickness,
                              boolean hasTabs, InstanceContext context) {
        // Generate the mesh
        final int startIdx = obj.getNumVertices();
        final int normalsStartIdx = obj.getNumNormals();

        final boolean isZeroThickness = Float.compare(thickness, 0f) == 0;

        // Generate the instance mesh
        if (isZeroThickness) {
            addZeroThicknessPolygonMesh(floatPoints);
        } else if (floatPoints.getXCoords().length >= 3) {
            PolygonExporter.addPolygonMesh(obj, transformer, null,
                    floatPoints.getXCoords(), floatPoints.getYCoords(), thickness);
        }

        // Generate the fin tabs
        if (hasTabs) {
            if (isZeroThickness) {
                addZeroThicknessPolygonMesh(floatTabPoints);
            } else if (floatTabPoints.getXCoords().length >= 3) {
                PolygonExporter.addPolygonMesh(obj, transformer, null,
                        floatTabPoints.getXCoords(), floatTabPoints.getYCoords(), thickness);
            }
        }

        if (obj.getNumVertices() == startIdx) {
            return;    // No geometry generated
        }

        int endIdx = obj.getNumVertices() - 1;                  // Clamp in case no vertices were added
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

    private void addZeroThicknessPolygonMesh(FloatPoints floatPoints) {
        final float[] pointLocationsX = floatPoints.getXCoords();
        final float[] pointLocationsY = floatPoints.getYCoords();

        if (pointLocationsX.length < 3) {
            return;    // Degenerate polygon, nothing to export
        }

        final int startIdx = obj.getNumVertices();
        final int normalsStartIdx = obj.getNumNormals();
        final int texCoordsStartIdx = obj.getNumTexCoords();

        obj.addNormal(transformer.convertLocWithoutOriginOffs(0, 0, -1));
        obj.addNormal(transformer.convertLocWithoutOriginOffs(0, 0, 1));

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < pointLocationsX.length; i++) {
            final float x = pointLocationsX[i];
            final float y = pointLocationsY[i];

            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }

            obj.addVertex(transformer.convertLoc(x, y, 0));
        }

        final float width = maxX - minX;
        final float height = maxY - minY;

        for (int i = 0; i < pointLocationsX.length; i++) {
            float u = width > 0 ? (pointLocationsX[i] - minX) / width : 0;
            u = 1.0f - u;
            float v = height > 0 ? (pointLocationsY[i] - minY) / height : 0;
            v = 1.0f - v;
            obj.addTexCoord(u, v);
        }

        int[] vertexIndices = new int[pointLocationsX.length];
        int[] texCoordsIndices = new int[pointLocationsX.length];
        int[] normalIndices = new int[pointLocationsX.length];
        for (int i = 0; i < pointLocationsX.length; i++) {
            vertexIndices[i] = pointLocationsX.length - 1 - i;
            texCoordsIndices[i] = pointLocationsX.length - 1 - i;
            normalIndices[i] = normalsStartIdx;
        }
        ObjUtils.offsetIndex(vertexIndices, startIdx);
        ObjUtils.offsetIndex(texCoordsIndices, texCoordsStartIdx);
        obj.addFace(new DefaultObjFace(vertexIndices, texCoordsIndices, normalIndices));

        vertexIndices = new int[pointLocationsX.length];
        texCoordsIndices = new int[pointLocationsX.length];
        normalIndices = new int[pointLocationsX.length];
        for (int i = 0; i < pointLocationsX.length; i++) {
            vertexIndices[i] = i;
            texCoordsIndices[i] = i;
            normalIndices[i] = normalsStartIdx + 1;
        }
        ObjUtils.offsetIndex(vertexIndices, startIdx);
        ObjUtils.offsetIndex(texCoordsIndices, texCoordsStartIdx);
        obj.addFace(new DefaultObjFace(vertexIndices, texCoordsIndices, normalIndices));
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
