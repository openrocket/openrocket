package info.openrocket.core.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.file.wavefrontobj.export.shapes.CylinderExporter;
import info.openrocket.core.file.wavefrontobj.export.shapes.DiskExporter;
import info.openrocket.core.file.wavefrontobj.export.shapes.TubeExporter;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.RingComponent;
import info.openrocket.core.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class RingComponentExporter extends RocketComponentExporter<RingComponent> {
    public RingComponentExporter(@NotNull DefaultObj obj, FlightConfiguration config, @NotNull CoordTransform transformer,
                                 RingComponent component, String groupName, ObjUtils.LevelOfDetail LOD, boolean exportAllInstances,
                                 WarningSet warnings) {
        super(obj, config, transformer, component, groupName, LOD, exportAllInstances, warnings);
    }

    @Override
    public void addToObj() {
        obj.setActiveGroupNames(groupName);

        final float outerRadius = (float) component.getOuterRadius();
        final float innerRadius = (float) component.getInnerRadius();
        final float length = (float) component.getLength();

        if (Double.compare(component.getThickness(), 0) == 0) {
            warnings.add(Warning.OBJ_ZERO_THICKNESS, component);
        }

        // Generate the mesh
        for (InstanceContext context : getInstanceContexts()) {
            generateMesh(outerRadius, innerRadius, length, context);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float length, InstanceContext context) {
        // Generate the mesh
        int startIdx = obj.getNumVertices();
        final boolean zeroLength = Float.compare(length, 0f) == 0;
        final boolean zeroThickness = Float.compare(outerRadius, innerRadius) == 0;

        if (zeroLength && zeroThickness) {
            return;    // Nothing to export
        }

        if (zeroLength) {
            addRingDiskMesh(outerRadius, innerRadius);
        } else if (zeroThickness) {
            addZeroThicknessTubeMesh(outerRadius, length);
        } else {
            TubeExporter.addTubeMesh(obj, transformer, null, outerRadius, innerRadius, length, LOD);
        }

        if (obj.getNumVertices() == startIdx) {
            return;    // No geometry generated
        }
        int endIdx = obj.getNumVertices() - 1;

        // Translate the mesh to the position in the rocket
        final Coordinate location = context.getLocation();
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

    private void addRingDiskMesh(float outerRadius, float innerRadius) {
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
