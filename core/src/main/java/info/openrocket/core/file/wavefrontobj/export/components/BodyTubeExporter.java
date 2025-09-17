package info.openrocket.core.file.wavefrontobj.export.components;

import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.file.wavefrontobj.export.shapes.CylinderExporter;
import info.openrocket.core.file.wavefrontobj.export.shapes.DiskExporter;
import info.openrocket.core.file.wavefrontobj.export.shapes.TubeExporter;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class BodyTubeExporter extends RocketComponentExporter<BodyTube> {
    public BodyTubeExporter(DefaultObj obj, FlightConfiguration config, CoordTransform transformer, BodyTube component,
                            String groupName, ObjUtils.LevelOfDetail LOD, boolean exportAllInstances, WarningSet warnings) {
        super(obj, config, transformer, component, groupName, LOD, exportAllInstances, warnings);
    }

    @Override
    public void addToObj() {
        obj.setActiveGroupNames(groupName);

        final float outerRadius = (float) component.getOuterRadius();
        float innerRadius = (float) component.getInnerRadius();
        final float length = (float) component.getLength();
        final boolean isFilled = component.isFilled();
        final float thickness = (float) component.getThickness();

        if (isFilled) {
            innerRadius = 0f;
        }

        if (Double.compare(component.getThickness(), 0) == 0) {
            warnings.add(Warning.OBJ_ZERO_THICKNESS, component);
        }

        // Generate the mesh
        for (InstanceContext context : getInstanceContexts()) {
            generateMesh(outerRadius, innerRadius, length, thickness, isFilled, context);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float length, float thickness,
                              boolean isFilled, InstanceContext context) {
        // Generate the mesh
        int startIdx = obj.getNumVertices();
        final boolean zeroLength = Float.compare(length, 0f) == 0;
        final boolean zeroThickness = Float.compare(thickness, 0f) == 0;

        if (zeroLength && zeroThickness) {
            return;
        }

        if (zeroLength) {
            addBodyTubeDiskMesh(outerRadius, innerRadius);
        } else if (zeroThickness) {
            addZeroThicknessTubeMesh(outerRadius, length);
        } else {
            TubeExporter.addTubeMesh(obj, transformer, null, outerRadius, isFilled ? 0 : innerRadius, length, LOD);
        }

        if (obj.getNumVertices() == startIdx) {
            return;
        }

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);    // Clamp in case no vertices were added

        // Translate the mesh to the position in the rocket
        Coordinate location = context.getLocation();
        ObjUtils.translateVerticesFromComponentLocation(obj, transformer, startIdx, endIdx, location);
    }

    private void addZeroThicknessTubeMesh(float radius, float length) {
        if (Float.compare(radius, 0f) <= 0 || Float.compare(length, 0f) == 0) {
            return;
        }

        final int numSides = LOD.getNrOfSides(radius);
        CylinderExporter.addCylinderMesh(obj, transformer, null, radius, radius, length, numSides,
                false, true, null, null);
    }

    private void addBodyTubeDiskMesh(float outerRadius, float innerRadius) {
        if (Float.compare(outerRadius, 0f) <= 0) {
            return;
        }

        final int numSides = LOD.getNrOfSides(outerRadius);
        final List<Integer> outerVertices = new ArrayList<>(numSides);
        final List<Integer> innerVertices = Float.compare(innerRadius, 0f) > 0 ? new ArrayList<>(numSides) : null;

        for (int i = 0; i < numSides; i++) {
            final double angle = 2 * Math.PI * i / numSides;
            final float y = outerRadius * (float) Math.cos(angle);
            final float z = outerRadius * (float) Math.sin(angle);
            obj.addVertex(transformer.convertLoc(0, y, z));
            outerVertices.add(obj.getNumVertices() - 1);
        }

        if (innerVertices != null) {
            for (int i = 0; i < numSides; i++) {
                final double angle = 2 * Math.PI * i / numSides;
                final float y = innerRadius * (float) Math.cos(angle);
                final float z = innerRadius * (float) Math.sin(angle);
                obj.addVertex(transformer.convertLoc(0, y, z));
                innerVertices.add(obj.getNumVertices() - 1);
            }
        }

        DiskExporter.closeDiskMesh(obj, transformer, null, outerVertices, innerVertices, false, true);
        DiskExporter.closeDiskMesh(obj, transformer, null, outerVertices, innerVertices, false, false);
    }
}
