package net.sf.openrocket.file.wavefrontobj.export.components;

import com.sun.istack.NotNull;
import de.javagl.obj.FloatTuple;
import net.sf.openrocket.file.wavefrontobj.CoordTransform;
import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.DiskExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.InstanceContext;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class TransitionExporter extends RocketComponentExporter<Transition> {
    private static final double RADIUS_EPSILON = 1e-4;
    private final int nrOfSides;

    public TransitionExporter(@NotNull DefaultObj obj, FlightConfiguration config, @NotNull CoordTransform transformer,
                              Transition component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, config, transformer, component, groupName, LOD);
        this.nrOfSides = LOD.getNrOfSides(Math.max(component.getForeRadius(), component.getAftRadius()));
    }

    @Override
    public void addToObj() {
        obj.setActiveGroupNames(groupName);

        // Generate the mesh
        for (InstanceContext context : config.getActiveInstances().getInstanceContexts(component)) {
            generateMesh(context);
        }
    }

    private void generateMesh(InstanceContext context) {
        int startIdx = obj.getNumVertices();

        final boolean hasForeShoulder = Double.compare(component.getForeShoulderRadius(), 0) > 0
                && Double.compare(component.getForeShoulderLength(), 0) > 0
                && component.getForeRadius() > 0;
        final boolean hasAftShoulder = Double.compare(component.getAftShoulderRadius(), 0) > 0
                && Double.compare(component.getAftShoulderLength(), 0) > 0
                && component.getAftRadius() > 0;

        final List<Integer> outsideForeRingVertices = new ArrayList<>();
        final List<Integer> outsideAftRingVertices = new ArrayList<>();
        final List<Integer> insideForeRingVertices = new ArrayList<>();
        final List<Integer> insideAftRingVertices = new ArrayList<>();

        // Check if geometry is a simple cylinder
        if (Double.compare(component.getAftRadius(), component.getForeRadius()) == 0 ||
                component.getShapeType() == Transition.Shape.CONICAL ||
                (component.getShapeType() == Transition.Shape.OGIVE && component.getShapeParameter() == 0) ||
                (component.getShapeType() == Transition.Shape.POWER && component.getShapeParameter() == 1) ||
                (component.getShapeType() == Transition.Shape.PARABOLIC && component.getShapeParameter() == 0)) {

            float outerAft = (float) component.getAftRadius();
            float innerAft = (float) (component.getAftRadius() - component.getThickness());
            float outerFore = (float) component.getForeRadius();
            float innerFore = (float) (component.getForeRadius() - component.getThickness());

            TubeExporter.addTubeMesh(obj, transformer, null, outerAft, outerFore, innerAft, innerFore,
                    (float) component.getLength(), this.nrOfSides,
                    outsideAftRingVertices, outsideForeRingVertices, insideAftRingVertices, insideForeRingVertices);
        }
        // Otherwise, use complex geometry
        else {
            int numStacks = component.getShapeType() == Transition.Shape.CONICAL ? 4 : this.nrOfSides / 2;

            // Draw outside
            addTransitionMesh(this.nrOfSides, numStacks, 0, true,
                    outsideForeRingVertices, outsideAftRingVertices, hasForeShoulder, hasAftShoulder);

            // Draw inside
            if (!component.isFilled()) {
                addTransitionMesh(this.nrOfSides, numStacks, -component.getThickness(), false,
                        insideForeRingVertices, insideAftRingVertices, hasForeShoulder, hasAftShoulder);
            }

            // Draw bottom and top face
            if (!hasForeShoulder)  {
                closeFace(insideForeRingVertices, outsideForeRingVertices, true);
            }
            if (!hasAftShoulder) {
                closeFace(insideAftRingVertices, outsideAftRingVertices, false);
            }

        }

        // Add shoulders
        addShoulders(this.nrOfSides, outsideForeRingVertices, outsideAftRingVertices,
                insideForeRingVertices, insideAftRingVertices, hasForeShoulder, hasAftShoulder);

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);    // Clamp in case no vertices were added

        // Translate the mesh to the position in the rocket
        final Coordinate location = context.getLocation();
        ObjUtils.translateVerticesFromComponentLocation(obj, transformer, startIdx, endIdx, location);
    }

    /**
     * Add a transition mesh to the obj.
     * @param numSlices the number of slices to use (= number of vertices in the circumferential direction)
     * @param numStacks the number of stacks to use (= number of vertices in the longitudinal direction)
     * @param offsetRadius offset radius from the transition radius
     * @param isOutside true if the mesh is on the outside of the rocket, false if it is on the inside
     * @param foreRingVertices list of vertices of the fore ring
     * @param aftRingVertices list of vertices of the aft ring
     */
    private void addTransitionMesh(int numSlices, int numStacks, double offsetRadius, boolean isOutside,
                                          List<Integer> foreRingVertices, List<Integer> aftRingVertices,
                                          boolean hasForeShoulder, boolean hasAftShoulder) {
        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        final int startIdx = obj.getNumVertices();
        final int normalsStartIdx = obj.getNumNormals();

        final double dxBase = component.getLength() / numStacks;         // Base step size in the longitudinal direction
        final double actualLength = estimateActualLength(offsetRadius, dxBase);

        // Generate vertices and normals
        float x = 0;                                        // Distance from the fore end
        double r;                                           // Current radius at location x
        boolean isForeTip = false;                          // True if the fore end of the transition is a tip (radius = 0)
        boolean isAftTip = false;                           // True if the aft end of the transition is a tip (radius = 0)
        int actualNumStacks = 0;                            // Number of stacks added, deviates from set point due to reduced step size near the tip (does not include aft/fore tip rings)
        while (x <= (float) component.getLength()) {
            // When closer to the smallest section of the transition, decrease the step size
            double t = Math.min(1, x / actualLength);
            if (component.getForeRadius() > component.getAftRadius()) {
                t = 1 - t;
            }
            float dx = t < 0.2 ? (float) (dxBase / (5.0 - 20*t)) : (float) dxBase;
            float xNext = x + dx;
            xNext = Math.min(xNext, (float) component.getLength());

            // Calculate the radius at this height
            r = Math.max(0, component.getRadius(x) + offsetRadius);
            double rNext = Math.max(0, component.getRadius(xNext) + offsetRadius);


            /*
            This is where things get a bit complex. Since transitions can have a fore and aft tip, we need to handle
            this separately from the rest of the mesh. Normal stack rings will eventually be drawn using quads, but
            transition tips will use an aft/fore tip vertex and then use triangles to connect the tip to the stack rings.
            Additionally, we need to store the fore ring and aft ring vertices to eventually close the outside and inside
            mesh. For this, we need to set isAftRing and isForeRing.

            The following rules apply:
            1. If actualNumStacks == 0, r == 0 and rNext == 0, skip this ring (you're at the fore end, before the fore tip)
            2. If actualNumStacks == 0, r == 0 and rNext > 0, add a single vertex at the center (= fore tip)
            3. If r > 0 and rNext == 0, set isForeRing = true & add a single vertex at the center at the next x position (= aft tip)
            4. If actualNumStacks > 0, r == 0 and rNext == 0, break the loop (you're at the aft tip)
            5. If r > 0, add normal vertices
             */
            if (actualNumStacks == 0) {
                if (Double.compare(r, RADIUS_EPSILON) <= 0) {
                    if (Double.compare(rNext, 0) == 0) {
                        // Case 1: Skip this ring (you're at the fore end, before the fore tip)
                        x = xNext;
                        continue;
                    } else {
                        // Case 2: Add a single vertex at the center (= fore tip)
                        float epsilon = dx / 20;
                        float xTip = getTipLocation(x, xNext, offsetRadius, epsilon);
                        obj.addVertex(transformer.convertLoc(xTip, 0, 0));
                        obj.addNormal(transformer.convertLocWithoutOriginOffs(isOutside ? -1 : 1, 0, 0));
                        isForeTip = true;

                        // The fore tip is the first fore "ring"
                        foreRingVertices.add(obj.getNumVertices() - 1);
                    }
                }
            } else if (actualNumStacks > 0 && Double.compare(r, 0) == 0 && Double.compare(rNext, 0) == 0) {
                // Case 4: Break the loop (you're at the aft tip)
                break;
            }
            if (Double.compare(r, RADIUS_EPSILON) > 0) {        // Don't just compare to 0, otherwise you could still have really small rings that could be better replaced by a tip
                // For the inside transition shape if we're in the shoulder base region, we need to skip adding rings,
                // because this is where the shoulder base will be
                if (!isOutside) {
                    // Get the location where the fore/aft shoulder would end (due to its thickness)
                    final double xForeShoulder = component.getAftShoulderThickness();
                    final double xAftShoulder = component.getLength() - component.getForeShoulderThickness();
                    if (hasForeShoulder && x < xForeShoulder) {
                        // If the current ring is before the fore shoulder ring and the next ring is after, clamp the
                        // next ring to the fore shoulder ring
                        if (xNext > xForeShoulder) {
                            xNext = (float) xForeShoulder;
                        } else {
                            // Skip the ring
                            x = xNext;
                            continue;
                        }
                    } else if (hasAftShoulder && x < xAftShoulder) {
                        // The aft shoulder point is between this and the next ring, so clamp the x value to the shoulder height
                        // and add the ring
                        if (xNext > xAftShoulder) {
                            xNext = (float) xAftShoulder;
                        }
                        // Skip the ring
                        else {
                            x = xNext;
                            continue;
                        }
                    }
                }

                if (Double.compare(rNext, RADIUS_EPSILON) <= 0 && Double.compare(x, component.getLength()) != 0) {
                    // Case 3: Add a single vertex at the center at the next x position (= aft tip)
                    float epsilon = dx / 20;
                    float xTip = getTipLocation(x, xNext, offsetRadius, epsilon);
                    obj.addVertex(transformer.convertLoc(xTip, 0, 0));
                    obj.addNormal(transformer.convertLocWithoutOriginOffs(isOutside ? 1 : -1, 0, 0));
                    isAftTip = true;

                    // The aft tip is the aft "ring"
                    aftRingVertices.add(obj.getNumVertices() - 1);

                    break;
                } else {
                    // Check on which ring we are
                    boolean isForeRing = actualNumStacks == 0 && foreRingVertices.isEmpty();
                    boolean isAftRing = Double.compare(x, (float) component.getLength()) == 0;

                    // Case 5: Add normal vertices
                    addQuadVertices(numSlices, foreRingVertices, aftRingVertices, r, rNext, x, isForeRing, isAftRing, isOutside);
                    actualNumStacks++;
                }
            }

            // If we're at the fore end, stop
            if (Float.compare(x, (float) component.getLength()) == 0) {
                break;
            }

            x = xNext;
        }

        // Create aft/fore tip faces
        if (isAftTip || isForeTip) {
            addTipFaces(numSlices, isOutside, isForeTip, startIdx, normalsStartIdx);
        }

        // Create regular faces
        int corrVStartIdx = isForeTip ? startIdx + 1 : startIdx;
        int corrNStartIdx = isForeTip ? normalsStartIdx + 1 : normalsStartIdx;
        addQuadFaces(numSlices, actualNumStacks, corrVStartIdx, corrNStartIdx, isOutside);
    }

    private void addTipFaces(int numSlices, boolean isOutside, boolean isForeTip, int startIdx, int normalsStartIdx) {
        final int lastIdx = obj.getNumVertices() - 1;
        for (int i = 0; i < numSlices; i++) {
            int nextIdx = (i + 1) % numSlices;
            int[] vertexIndices;
            int[] normalIndices;
            // Fore tip
            if (isForeTip) {
                vertexIndices = new int[] {
                        0,                      // Fore tip vertex
                        1 + i,
                        1 + nextIdx
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);

                normalIndices = vertexIndices.clone();   // No need to reverse, already done by vertices

                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);
                ObjUtils.offsetIndex(vertexIndices, startIdx);    // Do this last, otherwise the normal indices will be wrong
            }
            // Aft tip
            else {
                vertexIndices = new int[] {
                        lastIdx,                // Aft tip vertex
                        lastIdx - numSlices + nextIdx,
                        lastIdx - numSlices + i,
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);

                int lastNormalIdx = obj.getNumNormals() - 1;
                normalIndices = new int[] {
                        lastNormalIdx,                // Aft tip vertex
                        lastNormalIdx - numSlices + nextIdx,
                        lastNormalIdx - numSlices + i,
                };
                normalIndices = ObjUtils.reverseIndexWinding(normalIndices, !isOutside);

                // No need to offset the indices, because we reference the last vertex (this caused a lot of debugging frustration hmfmwl)
            }

            DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
            obj.addFace(face);
        }
    }

    private void addQuadVertices(int numSlices, List<Integer> foreRingVertices, List<Integer> aftRingVertices,
                                        double r, double rNext, float x, boolean isForeRing, boolean isAftRing, boolean isOutside) {
        for (int i = 0; i < numSlices; i++) {
            double angle = 2 * Math.PI * i / numSlices;
            float y = (float) (r * Math.cos(angle));
            float z = (float) (r * Math.sin(angle));

            obj.addVertex(transformer.convertLoc(x, y, z));

            // Add the ring vertices to the lists
            if (isForeRing) {
                foreRingVertices.add(obj.getNumVertices()-1);
            }
            if (isAftRing) {
                aftRingVertices.add(obj.getNumVertices()-1);
            }

            // Calculate the normal
            final float nx = isOutside ? (float) (r - rNext) : (float) (rNext -r);
            final float ny = isOutside ? y : -y;
            final float nz = isOutside ? z : -z;
            obj.addNormal(transformer.convertLocWithoutOriginOffs(nx, ny, nz));
        }
    }

    private void addQuadFaces(int numSlices, int numStacks, int startIdx, int normalsStartIdx, boolean isOutside) {
        for (int i = 0; i < numStacks - 1; i++) {
            for (int j = 0; j < numSlices; j++) {
                final int nextIdx = (j + 1) % numSlices;
                int[] vertexIndices = new int[] {
                        i * numSlices + j,              // Bottom-left of quad
                        (i + 1) * numSlices + j,        // Top-left of quad
                        (i + 1) * numSlices + nextIdx,  // Top-right of quad
                        i * numSlices + nextIdx,        // Bottom-right of quad
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);

                int[] normalIndices = vertexIndices.clone();     // No reversing needed, already done by vertices

                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);
                ObjUtils.offsetIndex(vertexIndices, startIdx);      // Do this last, otherwise the normal indices will be wrong

                DefaultObjFace face = new DefaultObjFace(vertexIndices, null, normalIndices);
                obj.addFace(face);
            }
        }
    }

    private void addShoulders(int nrOfSides, List<Integer> outsideForeRingVertices, List<Integer> outsideAftRingVertices,
                              List<Integer> insideForeRingVertices, List<Integer> insideAftRingVertices,
                              boolean hasForeShoulder, boolean hasAftShoulder) {
        final float foreShoulderRadius = (float) component.getForeShoulderRadius();
        final float aftShoulderRadius = (float) component.getAftShoulderRadius();
        final float foreShoulderLength = (float) component.getForeShoulderLength();
        final float aftShoulderLength = (float) component.getAftShoulderLength();
        final float foreShoulderThickness = (float) component.getForeShoulderThickness();
        final float aftShoulderThickness = (float) component.getAftShoulderThickness();
        final boolean foreShoulderCapped = component.isForeShoulderCapped();
        final boolean aftShoulderCapped = component.isAftShoulderCapped();

        if (hasForeShoulder) {
            addShoulder(foreShoulderRadius, foreShoulderLength, foreShoulderThickness, foreShoulderCapped,
                    true, nrOfSides, outsideForeRingVertices, insideForeRingVertices);
        }
        if (hasAftShoulder) {
            addShoulder(aftShoulderRadius, aftShoulderLength, aftShoulderThickness, aftShoulderCapped,
                    false, nrOfSides, outsideAftRingVertices, insideAftRingVertices);
        }
    }

    private void addShoulder(float shoulderRadius, float shoulderLength, float shoulderThickness, boolean isCapped,
                             boolean isForeSide, int nrOfSides, List<Integer> outerRingVertices, List<Integer> innerRingVertices) {
        final float innerCylinderRadius = isCapped ? 0 : shoulderRadius - shoulderThickness;
        final List<Integer> outerCylinderBottomVertices = new ArrayList<>();
        final List<Integer> outerCylinderTopVertices = new ArrayList<>();
        final List<Integer> innerCylinderBottomVertices = isCapped ? null : new ArrayList<>();
        final List<Integer> innerCylinderTopVertices = isCapped ? null : new ArrayList<>();
        int startIdx;
        int endIdx;

        /*
            Cross-section of a transition with aft shoulder:

                    UNCAPPED                                     CAPPED

            |  |               2|  |                    |  |               2|  |
            |  |                |  |1                   |  |                |  |1
            |  |                |  |                    |  |                |  |
            |  |______    ____6_|  |                    |  |______________6_|  |
            |_______  |7 |  _______|                    |_______        _______|
                    | |  | |   5                                |      |   5
                   3|_|  |_|                                   3|______|
                     4                                           4

            1: transition inside
            2: transition outside
            3: shoulder outer open cylinder
            4: shoulder top disk
            5: transition outer disk
            6: transition inner disk
            7: shoulder inner open cylinder (only if uncapped)
            */

        // Generate outer cylinder (no. 3)
        startIdx = obj.getNumVertices();
        CylinderExporter.addCylinderMesh(obj, transformer, null, shoulderRadius, shoulderLength,
                false, nrOfSides, outerCylinderBottomVertices, outerCylinderTopVertices);
        endIdx = Math.max(obj.getNumVertices() - 1, startIdx);

        // Translate the outer cylinder to the correct position
        float dx = isForeSide ? -shoulderLength : (float) component.getLength();
        FloatTuple offsetLoc = transformer.convertLocWithoutOriginOffs(dx, 0, 0);
        ObjUtils.translateVertices(obj, startIdx, endIdx, offsetLoc.getX(), offsetLoc.getY(), offsetLoc.getZ());

        // Generate inner cylinder (no. 7)
        if (!isCapped) {
            startIdx = obj.getNumVertices();
            CylinderExporter.addCylinderMesh(obj, transformer, null, innerCylinderRadius, shoulderLength + shoulderThickness,
                    false, false, nrOfSides, innerCylinderBottomVertices, innerCylinderTopVertices);
            endIdx = Math.max(obj.getNumVertices() - 1, startIdx);

            // Translate the outer cylinder to the correct position
            dx = isForeSide ? -shoulderLength : (float) component.getLength() - shoulderThickness;
            offsetLoc = transformer.convertLocWithoutOriginOffs(dx, 0, 0);
            ObjUtils.translateVertices(obj, startIdx, endIdx, offsetLoc.getX(), offsetLoc.getY(), offsetLoc.getZ());
        }

        // Generate shoulder top disk (no. 4)
        if (isForeSide) {
            DiskExporter.closeDiskMesh(obj, transformer, null, outerCylinderTopVertices, innerCylinderTopVertices, false, true);
        } else {
            DiskExporter.closeDiskMesh(obj, transformer, null, outerCylinderBottomVertices, innerCylinderBottomVertices, false, false);
        }

        // Generate transition outer disk (no. 5)
        if (isForeSide) {
            DiskExporter.closeDiskMesh(obj, transformer, null, outerRingVertices, outerCylinderBottomVertices, false, true);
        } else {
            DiskExporter.closeDiskMesh(obj, transformer, null, outerRingVertices, outerCylinderTopVertices, false, false);
        }

        // Generate transition inner disk (no. 6)
        if (isForeSide) {
            DiskExporter.closeDiskMesh(obj, transformer, null, innerRingVertices, innerCylinderBottomVertices, false, false);
        } else {
            DiskExporter.closeDiskMesh(obj, transformer, null, innerRingVertices, innerCylinderTopVertices, false, true);
        }
    }

    private void closeFace(List<Integer> outerVertices, List<Integer> innerVertices, boolean isTopFace) {
        boolean filledCap = component.isFilled() || innerVertices.size() <= 1;
        DiskExporter.closeDiskMesh(obj, transformer, null, outerVertices, filledCap ? null : innerVertices, true, isTopFace);
    }

    /**
     * Due to the offsetRadius, the length of the transition to be drawn can be smaller than the actual length of the transition,
     * because the offsetRadius causes the mesh to "shrink". This method estimates the length of the transition to be drawn.
     * @param offsetRadius the offset radius to the radius
     * @param dxBase the base of the dx
     * @return the estimated length of the transition to be drawn
     */
    private float estimateActualLength(double offsetRadius, double dxBase) {
        if (Double.compare(offsetRadius, 0) >= 0) {
            return (float) component.getLength();
        }

        double x = 0;
        final float increment = (float) dxBase / 4;
        float actualLength = 0;

        while (x < component.getLength()) {
            final double r = component.getRadius(x) + offsetRadius;

            if (Double.compare(r, 0) > 0) {
                actualLength += increment;
            }

            x += increment;
        }

        return actualLength;
    }

    /**
     * Locate the best location for the tip of a transition.
     * @param xStart the start position to look for
     * @param xEnd the end position to look for
     * @param offsetRadius the offset radius to the radius
     * @param epsilon the increment to parse the next y location
     * @return the best location for the tip
     */
    private float getTipLocation(float xStart, float xEnd, double offsetRadius, float epsilon) {
        if (Float.compare(xStart, xEnd) == 0 || Float.compare(epsilon, 0) == 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        boolean isStartSmaller = component.getRadius(xStart) < component.getRadius(xEnd);

        if (isStartSmaller) {
            for (float x = xEnd; x >= xStart; x -= epsilon) {
                double r = Math.max(0, component.getRadius(x) + offsetRadius);
                if (Double.compare(r, 0) == 0) {
                    return x;
                }
            }

            return xStart;
        } else {
            for (float x = xStart; x <= xEnd; x += epsilon) {
                double r = Math.max(0, component.getRadius(x) + offsetRadius);
                if (Double.compare(r, 0) == 0) {
                    return x;
                }
            }

            return xEnd;
        }
    }
}
