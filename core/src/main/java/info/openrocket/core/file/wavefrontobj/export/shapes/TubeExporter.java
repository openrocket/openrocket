package info.openrocket.core.file.wavefrontobj.export.shapes;

import com.sun.istack.NotNull;
import info.openrocket.core.file.wavefrontobj.CoordTransform;
import info.openrocket.core.file.wavefrontobj.DefaultObj;
import info.openrocket.core.file.wavefrontobj.ObjUtils;

import java.util.ArrayList;
import java.util.List;

public class TubeExporter {
    /**
     * Add a tube mesh to the obj.
     * It is drawn in the origin of the OBJ coordinate system. The longitudinal axis is positive, converted OpenRocket x-axis.
     * @param obj The obj to add the mesh to
     * @param groupName The name of the group to add the mesh to, or null if no group should be added (use the active group)
     * @param foreOuterRadius The outer radius of the fore (top) of the tube
     * @param aftOuterRadius The outer radius of the aft (bottom) of the tube
     * @param foreInnerRadius The inner radius of the fore (top) of the tube
     *                        Set to 0, together with aftInnerRadius, to create a solid tube
     * @param aftInnerRadius The inner radius of the aft (bottom) of the tube
     *                       Set to 0, together with foreInnerRadius, to create a solid tube
     * @param length The length of the tube
     * @param numSides The number of sides of the tube
     * @param foreOuterVertices A list to add the indices of the fore (top) outer vertices to, or null if the indices are not needed
     * @param aftOuterVertices A list to add the indices of the aft (bottom) outer vertices to, or null if the indices are not needed
     * @param foreInnerVertices A list to add the indices of the fore (top) inner vertices to, or null if the indices are not needed
     * @param aftInnerVertices A list to add the indices of the aft (bottom) inner vertices to, or null if the indices are not needed
     */
    public static void addTubeMesh(@NotNull DefaultObj obj, @NotNull CoordTransform transformer, String groupName,
                                   float foreOuterRadius, float aftOuterRadius,
                                   float foreInnerRadius, float aftInnerRadius, float length, int numSides,
                                   List<Integer> foreOuterVertices, List<Integer> aftOuterVertices,
                                   List<Integer> foreInnerVertices, List<Integer> aftInnerVertices) {
        if (Float.compare(aftInnerRadius, aftOuterRadius) > 0 || Float.compare(foreInnerRadius, foreOuterRadius) > 0) {
            throw new IllegalArgumentException("Inner radius must be less than outer radius");
        }

        // Set the new group
        if (groupName != null) {
            obj.setActiveGroupNames(groupName);
        }

        // We need to store the vertices to create the closing disks, so we'll create lists if they do not exist
        if (foreOuterVertices == null) {
            foreOuterVertices = new ArrayList<>();
        }
        if (aftOuterVertices == null) {
            aftOuterVertices = new ArrayList<>();
        }
        if (foreInnerVertices == null) {
            foreInnerVertices = new ArrayList<>();
        }
        if (aftInnerVertices == null) {
            aftInnerVertices = new ArrayList<>();
        }

        // Generate inside mesh
        boolean hasForeThickness = Float.compare(foreOuterRadius, foreInnerRadius) > 0 && Float.compare(foreInnerRadius, 0) > 0;
        boolean hasAftThickness = Float.compare(aftOuterRadius, aftInnerRadius) > 0  && Float.compare(aftInnerRadius, 0) > 0;
        // A matching inner and outer radius means we want an uncapped, zero-thickness surface
        boolean zeroThickness = Float.compare(foreOuterRadius, foreInnerRadius) == 0
                && Float.compare(aftOuterRadius, aftInnerRadius) == 0;
        boolean zeroLength = Float.compare(length, 0) == 0;

        if (zeroLength) {
            if (zeroThickness) {
                return;
            }

            // Lengthless tube collapses to a flat ring representation drawn at the origin plane
            float ringOuterRadius = (foreOuterRadius + aftOuterRadius) / 2f;
            float ringInnerRadius = (foreInnerRadius + aftInnerRadius) / 2f;
            addFlatRingMesh(obj, transformer, ringOuterRadius, ringInnerRadius, numSides,
                    foreOuterVertices, aftOuterVertices, foreInnerVertices, aftInnerVertices);
            return;
        }

        boolean hasOuterSurface = Float.compare(foreOuterRadius, 0f) > 0 || Float.compare(aftOuterRadius, 0f) > 0;
        if (!hasOuterSurface) {
            return;
        }
        if (hasForeThickness || hasAftThickness) {
            CylinderExporter.addCylinderMesh(obj, transformer, null, foreInnerRadius, aftInnerRadius, length, numSides,
                    false, false, foreInnerVertices, aftInnerVertices);
        }

        // Generate outside mesh
        CylinderExporter.addCylinderMesh(obj, transformer, null, foreOuterRadius, aftOuterRadius, length, numSides,
                !zeroThickness && !hasForeThickness && !hasAftThickness, true, foreOuterVertices, aftOuterVertices);

        // Close the mesh
        if (hasForeThickness) {
            DiskExporter.closeDiskMesh(obj, transformer, null, foreOuterVertices, foreInnerVertices, false, true);
        }
        if (hasAftThickness) {
            DiskExporter.closeDiskMesh(obj, transformer, null, aftOuterVertices, aftInnerVertices, false, false);
        }
    }

    public static void addTubeMesh(DefaultObj obj, CoordTransform transformer, String groupName,
                                   float outerRadius, float innerRadius, float length, int numSides) {
        addTubeMesh(obj, transformer, groupName, outerRadius, outerRadius, innerRadius, innerRadius, length, numSides,
                null, null, null, null);
    }

    public static void addTubeMesh(DefaultObj obj, CoordTransform transformer, String groupName,
                                   float outerRadius, float innerRadius, float length, ObjUtils.LevelOfDetail LOD) {
        addTubeMesh(obj, transformer, groupName, outerRadius, outerRadius, innerRadius, innerRadius, length, LOD.getNrOfSides(outerRadius),
                null, null, null, null);
    }

    private static void addFlatRingMesh(DefaultObj obj, CoordTransform transformer, float outerRadius, float innerRadius,
                                        int numSides,
                                        List<Integer> foreOuterVertices, List<Integer> aftOuterVertices,
                                        List<Integer> foreInnerVertices, List<Integer> aftInnerVertices) {
        if (Float.compare(outerRadius, 0f) <= 0) {
            return;
        }

        List<Integer> ringOuter = new ArrayList<>(numSides);
        createRingVertices(obj, transformer, outerRadius, numSides, ringOuter);

        List<Integer> ringInner = null;
        if (Float.compare(innerRadius, 0f) > 0 && Float.compare(innerRadius, outerRadius) < 0) {
            ringInner = new ArrayList<>(numSides);
            createRingVertices(obj, transformer, innerRadius, numSides, ringInner);
        }

        DiskExporter.closeDiskMesh(obj, transformer, null, ringOuter, ringInner, false, true);
        DiskExporter.closeDiskMesh(obj, transformer, null, ringOuter, ringInner, false, false);

        if (foreOuterVertices != null) {
            foreOuterVertices.addAll(ringOuter);
        }
        if (aftOuterVertices != null) {
            aftOuterVertices.addAll(ringOuter);
        }

        if (ringInner != null) {
            if (foreInnerVertices != null) {
                foreInnerVertices.addAll(ringInner);
            }
            if (aftInnerVertices != null) {
                aftInnerVertices.addAll(ringInner);
            }
        }
    }

    private static void createRingVertices(DefaultObj obj, CoordTransform transformer, float radius, int numSides,
                                           List<Integer> target) {
        for (int i = 0; i < numSides; i++) {
            double angle = 2 * Math.PI * i / numSides;
            float y = radius * (float) Math.cos(angle);
            float z = radius * (float) Math.sin(angle);
            obj.addVertex(transformer.convertLoc(0, y, z));
            target.add(obj.getNumVertices() - 1);
        }
    }
}
