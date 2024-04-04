package info.openrocket.core.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;
import info.openrocket.core.file.wavefrontobj.export.shapes.TubeExporter;
import info.openrocket.core.logging.Warning;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.util.Coordinate;

public class TubeFinSetExporter extends RocketComponentExporter<TubeFinSet> {
    public TubeFinSetExporter(@NotNull DefaultObj obj, FlightConfiguration config, @NotNull CoordTransform transformer,
                              TubeFinSet component, String groupName, ObjUtils.LevelOfDetail LOD, WarningSet warnings) {
        super(obj, config, transformer, component, groupName, LOD, warnings);
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

        // Generate the fin meshes
        for (InstanceContext context : config.getActiveInstances().getInstanceContexts(component)) {
            generateMesh(outerRadius, innerRadius, length, context);
        }
    }

    private void generateMesh(float outerRadius, float innerRadius, float length, InstanceContext context) {
        // Create the fin meshes
        final int startIdx = obj.getNumVertices();

        // Generate the instance mesh
        TubeExporter.addTubeMesh(obj, transformer, null, outerRadius, innerRadius, length, LOD.getValue());

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);                  // Clamp in case no vertices were added

        // Translate the mesh to the position in the rocket
        //      We will create an offset location that has the same effect as the axial rotation of the launch lug
        final double rotX = context.transform.getXrotation();
        final Coordinate location = context.getLocation();
        Coordinate offsetLocation = getOffsetLocation(outerRadius, location, rotX);
        ObjUtils.translateVerticesFromComponentLocation(obj, transformer, startIdx, endIdx, offsetLocation);
    }

    private static Coordinate getOffsetLocation(float outerRadius, Coordinate location, double rotX) {
        // ! This is all still referenced to the OpenRocket coordinate system, not the OBJ one
        final float dy = outerRadius * (float) Math.cos(rotX);
        final float dz = outerRadius * (float) Math.sin(rotX);
        final double x = location.x;
        final double y = location.y + dy;
        final double z = location.z + dz;
        return new Coordinate(x, y, z);
    }
}
