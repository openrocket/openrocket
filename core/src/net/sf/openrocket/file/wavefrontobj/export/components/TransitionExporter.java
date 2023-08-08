package net.sf.openrocket.file.wavefrontobj.export.components;

import net.sf.openrocket.file.wavefrontobj.DefaultObj;
import net.sf.openrocket.file.wavefrontobj.DefaultObjFace;
import net.sf.openrocket.file.wavefrontobj.ObjUtils;
import net.sf.openrocket.file.wavefrontobj.export.shapes.CylinderExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.DiskExporter;
import net.sf.openrocket.file.wavefrontobj.export.shapes.TubeExporter;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.util.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class TransitionExporter extends RocketComponentExporter {
    private static final double RADIUS_EPSILON = 1e-4;
    private final int nrOfSides;

    /**
     * Exporter for transition components
     * @param obj Wavefront OBJ file to export to
     * @param component Component to export
     * @param groupName Name of the group to export to
     * @param LOD Level of detail to use for the export
     */
    public TransitionExporter(DefaultObj obj, Transition component, String groupName, ObjUtils.LevelOfDetail LOD) {
        super(obj, component, groupName, LOD);
        this.nrOfSides = LOD.getNrOfSides(Math.max(component.getForeRadius(), component.getAftRadius()));
    }

    @Override
    public void addToObj() {
        final Transition transition = (Transition) component;

        obj.setActiveGroupNames(groupName);

        final Coordinate[] locations = transition.getComponentLocations();

        // Generate the mesh
        for (Coordinate location : locations) {
            generateMesh(transition, location);
        }
    }

    private void generateMesh(Transition transition, Coordinate location) {
        int startIdx = obj.getNumVertices();

        final boolean hasForeShoulder = Double.compare(transition.getForeShoulderRadius(), 0) > 0
                && Double.compare(transition.getForeShoulderLength(), 0) > 0
                && transition.getForeRadius() > 0;
        final boolean hasAftShoulder = Double.compare(transition.getAftShoulderRadius(), 0) > 0
                && Double.compare(transition.getAftShoulderLength(), 0) > 0
                && transition.getAftRadius() > 0;

        final List<Integer> outsideForeRingVertices = new ArrayList<>();
        final List<Integer> outsideAftRingVertices = new ArrayList<>();
        final List<Integer> insideForeRingVertices = new ArrayList<>();
        final List<Integer> insideAftRingVertices = new ArrayList<>();

        // Check if geometry is a simple cylinder
        if (Double.compare(transition.getAftRadius(), transition.getForeRadius()) == 0 ||
                transition.getShapeType() == Transition.Shape.CONICAL ||
                (transition.getShapeType() == Transition.Shape.OGIVE && transition.getShapeParameter() == 0) ||
                (transition.getShapeType() == Transition.Shape.POWER && transition.getShapeParameter() == 1) ||
                (transition.getShapeType() == Transition.Shape.PARABOLIC && transition.getShapeParameter() == 0)) {

            float outerAft = (float) transition.getAftRadius();
            float innerAft = (float) (transition.getAftRadius() - transition.getThickness());
            float outerFore = (float) transition.getForeRadius();
            float innerFore = (float) (transition.getForeRadius() - transition.getThickness());

            TubeExporter.addTubeMesh(obj, null, outerAft, outerFore, innerAft, innerFore,
                    (float) transition.getLength(), this.nrOfSides,
                    outsideAftRingVertices, outsideForeRingVertices, insideAftRingVertices, insideForeRingVertices);
        }
        // Otherwise, use complex geometry
        else {
            int numStacks = transition.getShapeType() == Transition.Shape.CONICAL ? 4 : this.nrOfSides / 2;

            // Draw outside
            addTransitionMesh(obj, transition, this.nrOfSides, numStacks, 0, true,
                    outsideForeRingVertices, outsideAftRingVertices, hasForeShoulder, hasAftShoulder);

            // Draw inside
            if (!transition.isFilled()) {
                addTransitionMesh(obj, transition, this.nrOfSides, numStacks, -transition.getThickness(), false,
                        insideForeRingVertices, insideAftRingVertices, hasForeShoulder, hasAftShoulder);
            }

            // Draw bottom and top face
            if (!hasForeShoulder)  {
                closeFace(obj, transition, outsideForeRingVertices, insideForeRingVertices, true);
            }
            if (!hasAftShoulder) {
                closeFace(obj, transition, outsideAftRingVertices, insideAftRingVertices, false);
            }

        }

        // Add shoulders
        addShoulders(obj, transition, this.nrOfSides, outsideForeRingVertices, outsideAftRingVertices,
                insideForeRingVertices, insideAftRingVertices, hasForeShoulder, hasAftShoulder);

        int endIdx = Math.max(obj.getNumVertices() - 1, startIdx);    // Clamp in case no vertices were added

        // Translate the mesh to the position in the rocket
        ObjUtils.translateVerticesFromComponentLocation(obj, transition, startIdx, endIdx, location, -transition.getLength());
    }

    /**
     * Add a transition mesh to the obj.
     * @param obj the obj to add the mesh to
     * @param transition the transition to draw
     * @param numSlices the number of slices to use (= number of vertices in the circumferential direction)
     * @param numStacks the number of stacks to use (= number of vertices in the longitudinal direction)
     * @param offsetRadius offset radius from the transition radius
     * @param isOutside true if the mesh is on the outside of the rocket, false if it is on the inside
     * @param foreRingVertices list of vertices of the fore ring
     * @param aftRingVertices list of vertices of the aft ring
     */
    private static void addTransitionMesh(DefaultObj obj, Transition transition,
                                         int numSlices, int numStacks, double offsetRadius, boolean isOutside,
                                          List<Integer> foreRingVertices, List<Integer> aftRingVertices,
                                          boolean hasForeShoulder, boolean hasAftShoulder) {
        // Other meshes may have been added to the obj, so we need to keep track of the starting indices
        final int startIdx = obj.getNumVertices();
        final int normalsStartIdx = obj.getNumNormals();

        final double dyBase = transition.getLength() / numStacks;         // Base step size in the longitudinal direction
        final double actualLength = estimateActualLength(transition, offsetRadius, dyBase);

        // Generate vertices and normals
        float y = 0;                                        // Distance from the aft end
        double r;                                           // Current radius at height y
        boolean isForeTip = false;                          // True if the fore end of the transition is a tip (radius = 0)
        boolean isAftTip = false;                           // True if the aft end of the transition is a tip (radius = 0)
        int actualNumStacks = 0;                            // Number of stacks added, deviates from set point due to reduced step size near the tip (does not include aft/fore tip rings)
        while (y <= (float) transition.getLength()) {
            // When closer to the tip, decrease the step size
            double t = Math.min(1, y / actualLength);
            if (transition.getForeRadius() < transition.getAftRadius()) {
                t = 1 - t;
            }
            float dy = t < 0.2 ? (float) (dyBase / (5.0 - 20*t)) : (float) dyBase;
            float yNext = y + dy;
            yNext = Math.min(yNext, (float) transition.getLength());

            // Calculate the radius at this height
            r = Math.max(0, transition.getRadius(transition.getLength()-y) + offsetRadius);     // y = 0 is aft and, but this would return the fore radius, so subtract it from the length
            double rNext = Math.max(0, transition.getRadius(transition.getLength()-yNext) + offsetRadius);


            /*
            This is where things get a bit complex. Since transitions can have a fore and aft tip, we need to handle
            this separately from the rest of the mesh. Normal stack rings will eventually be drawn using quads, but
            transition tips will use an aft/fore tip vertex and then use triangles to connect the tip to the stack rings.
            Additionally, we need to store the fore ring and aft ring vertices to eventually close the outside and inside
            mesh. For this, we need to set isAftRing and isForeRing.

            The following rules apply:
            1. If actualNumStacks == 0, r == 0 and rNext == 0, skip this ring (you're at the aft end, before the aft tip)
            2. If actualNumStacks == 0, r == 0 and rNext > 0, add a single vertex at the center (= aft tip)
            3. If r > 0 and rNext == 0, set isForeRing = true & add a single vertex at the center at the next y position (= fore tip)
            4. If actualNumStacks > 0, r == 0 and rNext == 0, break the loop (you're at the fore tip)
            5. If r > 0, add normal vertices
             */
            if (actualNumStacks == 0) {
                if (Double.compare(r, 0) == 0) {
                    if (Double.compare(rNext, 0) == 0) {
                        // Case 1: Skip this ring (you're at the aft end, before the aft tip)
                        y = yNext;
                        continue;
                    } else {
                        // Case 2: Add a single vertex at the center (= aft tip)
                        float epsilon = dy / 20;
                        float yTip = getTipLocation(transition, y, yNext, offsetRadius, epsilon);
                        obj.addVertex(0, yTip, 0);
                        obj.addNormal(0, isOutside ? -1 : 1, 0);
                        isAftTip = true;

                        // The aft tip is the first aft "ring"
                        aftRingVertices.add(obj.getNumVertices() - 1);
                    }
                }
            } else if (actualNumStacks > 0 && Double.compare(r, 0) == 0 && Double.compare(rNext, 0) == 0) {
                // Case 4: Break the loop (you're at the fore tip)
                break;
            }
            if (Double.compare(r, RADIUS_EPSILON) > 0) {        // Don't just compare to 0, otherwise you could still have really small rings that could be better replaced by a tip
                // For the inside transition shape if we're in the shoulder base region, we need to skip adding rings,
                // because this is where the shoulder base will be
                float yClamped = y;
                if (!isOutside) {
                    final double yForeShoulder = transition.getLength() - transition.getForeShoulderThickness();
                    final double yAftShoulder = transition.getAftShoulderThickness();
                    if (hasForeShoulder) {
                        if (y < yForeShoulder) {
                            // If the current ring is before the fore shoulder ring and the next ring is after, clamp the
                            // next ring to the fore shoulder ring
                            if (yNext > yForeShoulder) {
                                yNext = (float) yForeShoulder;
                            }
                        }
                        // Skip the ring
                        else if (y > yForeShoulder) {
                            continue;
                        }
                    } else if (hasAftShoulder && y < yAftShoulder) {
                        // The aft shoulder point is between this and the next ring, so clamp the y value to the shoulder height
                        // and add the ring
                        if (yNext > yAftShoulder) {
                            yClamped = (float) yAftShoulder;
                        }
                        // Skip the ring
                        else {
                            continue;
                        }
                    }
                }

                if (Double.compare(rNext, RADIUS_EPSILON) <= 0 && Double.compare(yClamped, transition.getLength()) != 0) {
                    // Case 3: Add a single vertex at the center at the next y position (= fore tip)
                    float epsilon = dy / 20;
                    float yTip = getTipLocation(transition, yClamped, yNext, offsetRadius, epsilon);
                    obj.addVertex(0, yTip, 0);
                    obj.addNormal(0, isOutside ? 1 : -1, 0);
                    isForeTip = true;

                    // The fore tip is the first fore "ring"
                    foreRingVertices.add(obj.getNumVertices() - 1);

                    break;
                } else {
                    // Check on which ring we are
                    boolean isAftRing = actualNumStacks == 0 && aftRingVertices.isEmpty();
                    boolean isForeRing = Double.compare(yClamped, (float) transition.getLength()) == 0;

                    // Case 5: Add normal vertices
                    addQuadVertices(obj, numSlices, foreRingVertices, aftRingVertices, r, rNext, yClamped, isAftRing, isForeRing, isOutside);
                    actualNumStacks++;
                }
            }

            // If we're at the fore end, stop
            if (Float.compare(y, (float) transition.getLength()) == 0) {
                break;
            }

            y = yNext;
        }

        // Create aft/fore tip faces
        if (isAftTip || isForeTip) {
            addTipFaces(obj, numSlices, isOutside, isAftTip, startIdx, normalsStartIdx);
        }

        // Create regular faces
        int corrVStartIdx = isAftTip ? startIdx + 1 : startIdx;
        int corrNStartIdx = isAftTip ? normalsStartIdx + 1 : normalsStartIdx;
        addQuadFaces(obj, numSlices, actualNumStacks, corrVStartIdx, corrNStartIdx, isOutside);
    }

    private static void addTipFaces(DefaultObj obj, int numSlices, boolean isOutside, boolean isAftTip, int startIdx, int normalsStartIdx) {
        final int lastIdx = obj.getNumVertices() - 1;
        for (int i = 0; i < numSlices; i++) {
            int nextIdx = (i + 1) % numSlices;
            int[] vertexIndices;
            int[] normalIndices;
            // Aft tip
            if (isAftTip) {
                vertexIndices = new int[] {
                        0,                      // Aft tip vertex
                        1 + i,
                        1 + nextIdx
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);

                normalIndices = vertexIndices.clone();   // No need to reverse, already done by vertices

                ObjUtils.offsetIndex(normalIndices, normalsStartIdx);
                ObjUtils.offsetIndex(vertexIndices, startIdx);    // Do this last, otherwise the normal indices will be wrong
            }
            // Fore tip
            else {
                vertexIndices = new int[] {
                        lastIdx,                // Fore tip vertex
                        lastIdx - numSlices + nextIdx,
                        lastIdx - numSlices + i,
                };
                vertexIndices = ObjUtils.reverseIndexWinding(vertexIndices, !isOutside);

                int lastNormalIdx = obj.getNumNormals() - 1;
                normalIndices = new int[] {
                        lastNormalIdx,                // Fore tip vertex
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

    private static void addQuadVertices(DefaultObj obj, int numSlices, List<Integer> foreRingVertices, List<Integer> aftRingVertices,
                                        double r, double rNext, float y, boolean isAftRing, boolean isForeRing, boolean isOutside) {
        for (int i = 0; i < numSlices; i++) {
            double angle = 2 * Math.PI * i / numSlices;
            float x = (float) (r * Math.cos(angle));
            float z = (float) (r * Math.sin(angle));

            obj.addVertex(x, y, z);

            // Add the ring vertices to the lists
            if (isAftRing) {
                aftRingVertices.add(obj.getNumVertices()-1);
            }
            if (isForeRing) {
                foreRingVertices.add(obj.getNumVertices()-1);
            }

            // Calculate the normal
            final float nx = isOutside ? x : -x;
            final float ny = isOutside ? (float) (r - rNext) : (float) (rNext -r);
            final float nz = isOutside ? z : -z;
            obj.addNormal(nx, ny, nz);
        }
    }

    private static void addQuadFaces(DefaultObj obj, int numSlices, int numStacks, int startIdx, int normalsStartIdx, boolean isOutside) {
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

    private static void addShoulders(DefaultObj obj, Transition transition, int nrOfSides,
                                     List<Integer> outsideForeRingVertices, List<Integer> outsideAftRingVertices,
                                        List<Integer> insideForeRingVertices, List<Integer> insideAftRingVertices,
                                     boolean hasForeShoulder, boolean hasAftShoulder) {
        final float foreShoulderRadius = (float) transition.getForeShoulderRadius();
        final float aftShoulderRadius = (float) transition.getAftShoulderRadius();
        final float foreShoulderLength = (float) transition.getForeShoulderLength();
        final float aftShoulderLength = (float) transition.getAftShoulderLength();
        final float foreShoulderThickness = (float) transition.getForeShoulderThickness();
        final float aftShoulderThickness = (float) transition.getAftShoulderThickness();
        final boolean foreShoulderCapped = transition.isForeShoulderCapped();
        final boolean aftShoulderCapped = transition.isAftShoulderCapped();

        if (hasForeShoulder) {
            addShoulder(obj, transition, foreShoulderRadius, foreShoulderLength, foreShoulderThickness, foreShoulderCapped,
                    true, nrOfSides, outsideForeRingVertices, insideForeRingVertices);
        }
        if (hasAftShoulder) {
            addShoulder(obj, transition, aftShoulderRadius, aftShoulderLength, aftShoulderThickness, aftShoulderCapped,
                    false, nrOfSides, outsideAftRingVertices, insideAftRingVertices);
        }
    }

    private static void addShoulder(DefaultObj obj, Transition transition, float shoulderRadius, float shoulderLength,
                                    float shoulderThickness, boolean isCapped, boolean isForeSide, int nrOfSides,
                                    List<Integer> outerRingVertices, List<Integer> innerRingVertices) {
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
        CylinderExporter.addCylinderMesh(obj, null, shoulderRadius, shoulderLength,
                false, nrOfSides, outerCylinderBottomVertices, outerCylinderTopVertices);
        endIdx = Math.max(obj.getNumVertices() - 1, startIdx);

        // Translate the outer cylinder to the correct position
        float dy = isForeSide ? (float) transition.getLength() : -shoulderLength;
        ObjUtils.translateVertices(obj, startIdx, endIdx, 0, dy, 0);

        // Generate inner cylinder (no. 7)
        if (!isCapped) {
            startIdx = obj.getNumVertices();
            CylinderExporter.addCylinderMesh(obj, null, innerCylinderRadius, shoulderLength + shoulderThickness,
                    false, false, nrOfSides, innerCylinderBottomVertices, innerCylinderTopVertices);
            endIdx = Math.max(obj.getNumVertices() - 1, startIdx);

            // Translate the outer cylinder to the correct position
            dy = isForeSide ? (float) transition.getLength() - shoulderThickness : -shoulderLength;
            ObjUtils.translateVertices(obj, startIdx, endIdx, 0, dy, 0);
        }

        // Generate shoulder top disk (no. 4)
        if (isForeSide) {
            DiskExporter.closeDiskMesh(obj, null, outerCylinderTopVertices, innerCylinderTopVertices, true);
        } else {
            DiskExporter.closeDiskMesh(obj, null, outerCylinderBottomVertices, innerCylinderBottomVertices, false);
        }

        // Generate transition outer disk (no. 5)
        if (isForeSide) {
            DiskExporter.closeDiskMesh(obj, null, outerRingVertices, outerCylinderBottomVertices, true);
        } else {
            DiskExporter.closeDiskMesh(obj, null, outerRingVertices, outerCylinderTopVertices, false);
        }

        // Generate transition inner disk (no. 6)
        if (isForeSide) {
            DiskExporter.closeDiskMesh(obj, null, innerRingVertices, innerCylinderBottomVertices, false);
        } else {
            DiskExporter.closeDiskMesh(obj, null, innerRingVertices, innerCylinderTopVertices, true);
        }
    }

    private static void closeFace(DefaultObj obj, Transition transition, List<Integer> outerVertices, List<Integer> innerVertices,
                                  boolean isTopFace) {
        boolean filledCap = transition.isFilled() || innerVertices.size() <= 1;
        DiskExporter.closeDiskMesh(obj, null, outerVertices, filledCap ? null : innerVertices, isTopFace);
    }

    /**
     * Due to the offsetRadius, the length of the transition to be drawn can be smaller than the actual length of the transition,
     * because the offsetRadius causes the mesh to "shrink". This method estimates the length of the transition to be drawn.
     * @param transition the transition to estimate the length for
     * @param offsetRadius the offset radius to the radius
     * @param dyBase the base of the dy
     * @return the estimated length of the transition to be drawn
     */
    private static float estimateActualLength(Transition transition, double offsetRadius, double dyBase) {
        if (Double.compare(offsetRadius, 0) >= 0) {
            return (float) transition.getLength();
        }

        double y = 0;
        final float increment = (float) dyBase / 4;
        float actualLength = 0;

        while (y < transition.getLength()) {
            final double r = transition.getRadius(transition.getLength()-y) + offsetRadius;

            if (Double.compare(r, 0) > 0) {
                actualLength += increment;
            }

            y += increment;
        }

        return actualLength;
    }

    /**
     * Locate the best location for the tip of a transition.
     * @param transition the transition to look the tip for
     * @param yStart the start position to look for
     * @param yEnd the end position to look for
     * @param offsetRadius the offset radius to the radius
     * @param epsilon the increment to parse the next y location
     * @return the best location for the tip
     */
    private static float getTipLocation(Transition transition, float yStart, float yEnd, double offsetRadius, float epsilon) {
        if (Float.compare(yStart, yEnd) == 0 || Float.compare(epsilon, 0) == 0) {
            throw new IllegalArgumentException("Invalid parameters");
        }

        boolean isStartSmaller = transition.getRadius(transition.getLength()-yStart) < transition.getRadius(transition.getLength()-yEnd);

        if (isStartSmaller) {
            for (float y = yEnd; y >= yStart; y -= epsilon) {
                double r = Math.max(0, transition.getRadius(transition.getLength() - y) + offsetRadius);
                if (Double.compare(r, 0) == 0) {
                    return y;
                }
            }

            return yStart;
        } else {
            for (float y = yStart; y <= yEnd; y += epsilon) {
                double r = Math.max(0, transition.getRadius(transition.getLength() - y) + offsetRadius);
                if (Double.compare(r, 0) == 0) {
                    return y;
                }
            }

            return yEnd;
        }
    }
}
