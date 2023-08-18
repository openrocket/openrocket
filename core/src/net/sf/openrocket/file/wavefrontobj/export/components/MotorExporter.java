package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.DiskExporter;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.motor.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.InstanceContext;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class MotorExporter {
    protected final DefaultObj obj;
    protected final FlightConfiguration config;
    protected final RocketComponent mount;
    protected final String groupName;
    protected final ObjUtils.LevelOfDetail LOD;
    protected final CoordTransform transformer;

    /**
     * Wavefront OBJ exporter for a rocket component.
     *
     * @param obj         The OBJ to export to
     * @param config      The flight configuration to use for the export
     * @param transformer Coordinate system transformer to use to switch from the OpenRocket coordinate system to a custom OBJ coordinate system
     * @param mount       The motor mount that holds the motor to export
     * @param groupName   The name of the group to export to
     * @param LOD         Level of detail to use for the export (e.g. '80')
     */
    public MotorExporter(DefaultObj obj, FlightConfiguration config, CoordTransform transformer, RocketComponent mount,
                         String groupName, ObjUtils.LevelOfDetail LOD) {
        if (!(mount instanceof MotorMount)) {
            throw new IllegalArgumentException("Motor exporter can only be used for motor mounts");
        }
        this.obj = obj;
        this.config = config;
        this.transformer = transformer;
        this.mount = mount;
        this.groupName = groupName;
        this.LOD = LOD;
    }

    public void addToObj() {
        MotorConfiguration motoConfig = ((MotorMount) mount).getMotorConfig(config.getId());
        Motor motor = motoConfig.getMotor();

        if (motor == null) {
            return;
        }

        obj.setActiveGroupNames(motor.getMotorName());

        for (InstanceContext context : config.getActiveInstances().getInstanceContexts(mount)) {
            generateMesh(motor, context);
        }
    }

    private void generateMesh(Motor motor, InstanceContext context) {
        final double length = motor.getLength();
        final double radius = motor.getDiameter() / 2;
        final float coneLength = (float) (0.05 * length);           // Length of the indent cone at the aft end of the motor
        final int numSides = LOD.getNrOfSides(radius);
        final int startIdx = obj.getNumVertices();

        // Draw the cylinder
        List<Integer> foreRingVertices = new ArrayList<>();
        List<Integer> aftRingVertices = new ArrayList<>();
        CylinderExporter.addCylinderMesh(obj, transformer, null, (float) radius, (float) length, numSides, false, true,
                foreRingVertices, aftRingVertices);

        // Close the fore end
        DiskExporter.closeDiskMesh(obj, transformer, null, foreRingVertices, false, true);

        // Generate the aft end inner ring vertices
        List<Integer> aftInnerRingVertices = new ArrayList<>();
        final int normalsStartIdx = obj.getNumNormals();
        final float innerRadius = (float) (0.8 * radius);
        for (int i = 0; i < numSides; i++) {
            final double angle = 2.0f * Math.PI * i / numSides;
            final float x = (float) length;
            final float y = (float) (innerRadius * Math.cos(angle));
            final float z = (float) (innerRadius * Math.sin(angle));
            obj.addVertex(transformer.convertLoc(x, y, z));

            final double slopeAngle = Math.atan(coneLength / innerRadius);
            final float nx = (float) Math.cos(slopeAngle);
            final float ny = (float) -Math.cos(angle);
            final float nz = (float) -Math.sin(angle);
            obj.addNormal(transformer.convertLocWithoutOriginOffs(nx, ny, nz));

            aftInnerRingVertices.add(obj.getNumVertices() - 1);
        }

        // Close outer and inner aft ring
        DiskExporter.closeDiskMesh(obj, transformer, null, aftRingVertices, aftInnerRingVertices, false, false);

        // Add cone tip vertex
        obj.addVertex(transformer.convertLoc(length - coneLength, 0, 0));
        obj.addNormal(transformer.convertLocWithoutOriginOffs(1, 0, 0));

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);    // Clamp in case no vertices were added
        int normalsEndIdx = Math.max(obj.getNumNormals() - 1, normalsStartIdx);

        // Create the cone faces
        for (int i = 0; i < numSides; i++) {
            final int nextIdx = (i + 1) % numSides;
            final int[] vertexIndices = new int[] {
                    endIdx,
                    aftInnerRingVertices.get(nextIdx),
                    aftInnerRingVertices.get(i),
            };
            final int[] normalIndices = new int[] {
                    normalsEndIdx,
                    normalsStartIdx + nextIdx,
                    normalsStartIdx + i,
            };

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }


        // Translate the mesh to the position in the rocket
        Coordinate location = context.getLocation();
        final double xOffs = mount.getLength() + ((MotorMount) mount).getMotorOverhang() - length;
        location = location.add(xOffs, 0, 0);      // Motor starts at the aft end of the mount
        ObjUtils.translateVerticesFromComponentLocation(obj, transformer, startIdx, endIdx, location);
    }
}