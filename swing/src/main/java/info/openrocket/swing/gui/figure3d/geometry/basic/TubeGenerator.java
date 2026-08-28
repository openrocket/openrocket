package info.openrocket.swing.gui.figure3d.geometry.basic;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates lengthwise profiles along the +X axis, including optional fore and aft shoulders.
 */
public class TubeGenerator {
	private static final Logger log = LoggerFactory.getLogger(TubeGenerator.class);

	public record RadiusPoint(float position, float radius) {}

	public record Shoulder(double length, double radius, double thickness, boolean isCapped) {}

	private record IndexRange(int start, int end) {}

	private record ProfileSections(IndexRange walls, IndexRange foreCap, IndexRange aftCap) {}

	private record SectionedMesh(Mesh mesh, ProfileSections sections) {}

	/**
	 * Creates a tube mesh from an outer profile, wall thickness, length, and number of segments.
	 * @param outerProfile the outer profile of the tube, defined as a list of RadiusPoint objects.
	 * @param wallThickness the thickness of the tube walls.
	 * @param length the length of the tube.
	 * @param numSegments the number of segments to divide the tube into for radial resolution.
	 * @param isFilled whether the tube is filled (true) or hollow (false).
	 * @param foreShoulder the fore shoulder of the tube, or null if no fore shoulder is present.
	 * @param aftShoulder the aft shoulder of the tube, or null if no aft shoulder is present.
	 * @return a Mesh object representing the tube geometry.
	 */
	public static Mesh create(List<RadiusPoint> outerProfile, float wallThickness, float length, int numSegments,
							  boolean isFilled, Shoulder foreShoulder, Shoulder aftShoulder) {
		List<Vertex> vertexList = new ArrayList<>();
		IntList indexList = new IntList();

		boolean hasForeShoulder = foreShoulder != null && foreShoulder.length() > 0 && foreShoulder.radius() > 0;
		boolean hasAftShoulder = aftShoulder != null && aftShoulder.length() > 0 && aftShoulder.radius() > 0;

		// 1. Generate the main tube body with open ends where shoulders will attach.
		createFromProfile(vertexList, indexList, outerProfile, wallThickness, length, numSegments, isFilled, 0,
				!hasForeShoulder, !hasAftShoulder, foreShoulder, aftShoulder);

		List<RadiusPoint> sortedOuterProfile = outerProfile.stream()
				.sorted(Comparator.comparingDouble(RadiusPoint::position))
				.toList();
		RadiusPoint tubeForePoint = sortedOuterProfile.get(0);
		RadiusPoint tubeAftPoint = sortedOuterProfile.get(sortedOuterProfile.size() - 1);


		// 2. Generate Fore Shoulder and its specific connection plates.
		if (hasForeShoulder) {
			float tubeInnerR = Math.max(0, tubeForePoint.radius - wallThickness);
			float shoulderInnerR = (float) Math.max(0, foreShoulder.radius() - foreShoulder.thickness());
			addShoulderConnector(vertexList, indexList,
					-length / 2.0f, tubeForePoint.radius, (float) foreShoulder.radius(),
					-length / 2.0f + (float) foreShoulder.thickness(), tubeInnerR, shoulderInnerR,
					numSegments, true, isFilled);

			int shoulderVertexOffset = vertexList.size();
			SectionedMesh generatedShoulder = createSectionedTube((float) foreShoulder.radius(),
					(float) foreShoulder.thickness(), (float) foreShoulder.length(), numSegments);
			Mesh shoulderMesh = generatedShoulder.mesh();

			List<Vertex> shoulderVertices = new ArrayList<>(shoulderMesh.getVertices());
			IntList newShoulderIndices = new IntList();
			int verticesPerRing = 2 * (numSegments + 1);

			if (foreShoulder.isCapped()) {
				// CAPPED FORE SHOULDER
				// 1. Adjust inner vertices to shorten the inner shell at the free end.
				for (int i = 0; i < verticesPerRing; i++) { // Fore-end vertices
					if (i % 2 == 1) { // Inner vertices
						Vertex v = shoulderVertices.get(i);
						v.position.x += (float) foreShoulder.thickness();
					}
				}
				// Also adjust inner vertices at the connection end to match the offset connector.
				for (int i = verticesPerRing; i < 2 * verticesPerRing; i++) { // Aft-end vertices
					if (i % 2 == 1) { // Inner vertices
						Vertex v = shoulderVertices.get(i);
						v.position.x += (float) foreShoulder.thickness();
					}
				}

				// 2. Rebuild index list with walls and two new filled caps.
				copyIndexRange(newShoulderIndices, shoulderMesh.getIndices(), generatedShoulder.sections().walls());

				// 3. Generate new vertices and indices for the caps.
				int capVertexOffset = shoulderVertices.size();
				float sLength = (float) foreShoulder.length();
				float sRadius = (float) foreShoulder.radius();
				float sThick = (float) foreShoulder.thickness();
				float sInnerR = Math.max(0, sRadius - sThick);

				generateFilledCap(shoulderVertices, newShoulderIndices, sRadius, -sLength / 2.0f, numSegments, true,
						capVertexOffset);
				capVertexOffset = shoulderVertices.size();
				generateFilledCap(shoulderVertices, newShoulderIndices, sInnerR, -sLength / 2.0f + sThick, numSegments,
						false, capVertexOffset);

			} else {
				// Uncapped fore shoulder
				for (int i = verticesPerRing; i < 2 * verticesPerRing; i++) { // Aft-end vertices of the shoulder
					if (i % 2 == 1) { // Inner vertices
						Vertex v = shoulderVertices.get(i);
						v.position.x += (float) foreShoulder.thickness();
					}
				}
				copyIndexRange(newShoulderIndices, shoulderMesh.getIndices(), generatedShoulder.sections().walls());
				copyIndexRange(newShoulderIndices, shoulderMesh.getIndices(), generatedShoulder.sections().foreCap());
			}

			// Translate the modified shoulder into position.
			float shoulderTranslateX = -length / 2.0f - (float) foreShoulder.length() / 2.0f;
			for (Vertex v : shoulderVertices) {
				v.position.x += shoulderTranslateX;
				vertexList.add(v);
			}
			indexList.addAllOffset(newShoulderIndices, shoulderVertexOffset);
		}


		// 3. Generate Aft Shoulder and its specific connection plates.
		if (hasAftShoulder) {
			float tubeInnerR = Math.max(0, tubeAftPoint.radius - wallThickness);
			float shoulderInnerR = (float) Math.max(0, aftShoulder.radius() - aftShoulder.thickness());
			addShoulderConnector(vertexList, indexList,
					length / 2.0f, tubeAftPoint.radius, (float) aftShoulder.radius(),
					length / 2.0f - (float) aftShoulder.thickness(), tubeInnerR, shoulderInnerR,
					numSegments, false, isFilled);

			int shoulderVertexOffset = vertexList.size();
			SectionedMesh generatedShoulder = createSectionedTube((float) aftShoulder.radius(),
					(float) aftShoulder.thickness(), (float) aftShoulder.length(), numSegments);
			Mesh shoulderMesh = generatedShoulder.mesh();

			List<Vertex> shoulderVertices = new ArrayList<>(shoulderMesh.getVertices());
			IntList newShoulderIndices = new IntList();
			int verticesPerRing = 2 * (numSegments + 1);

			if (aftShoulder.isCapped()) {
				// CAPPED AFT SHOULDER
				// 1. Adjust inner vertices to shorten the inner shell at the free end.
				for (int i = verticesPerRing; i < 2 * verticesPerRing; i++) { // Aft-end vertices
					if (i % 2 == 1) { // Inner vertices
						Vertex v = shoulderVertices.get(i);
						v.position.x -= (float) aftShoulder.thickness();
					}
				}
				// Also adjust inner vertices at the connection end to match the offset connector.
				for (int i = 0; i < verticesPerRing; i++) { // Fore-end vertices
					if (i % 2 == 1) { // Inner vertices
						Vertex v = shoulderVertices.get(i);
						v.position.x -= (float) aftShoulder.thickness();
					}
				}

				// 2. Rebuild index list with walls and two new filled caps.
				copyIndexRange(newShoulderIndices, shoulderMesh.getIndices(), generatedShoulder.sections().walls());

				// 3. Generate new vertices and indices for the caps.
				int capVertexOffset = shoulderVertices.size();
				float sLength = (float) aftShoulder.length();
				float sRadius = (float) aftShoulder.radius();
				float sThick = (float) aftShoulder.thickness();
				float sInnerR = (float) Math.max(0, sRadius - sThick);

				generateFilledCap(shoulderVertices, newShoulderIndices, sRadius, sLength / 2.0f, numSegments, false,
						capVertexOffset);
				capVertexOffset = shoulderVertices.size();
				generateFilledCap(shoulderVertices, newShoulderIndices, sInnerR, sLength / 2.0f - sThick, numSegments,
						true, capVertexOffset);

			} else {
				// Uncapped aft shoulder
				for (int i = 0; i < verticesPerRing; i++) { // Fore-end vertices of the shoulder
					if (i % 2 == 1) { // Inner vertices
						Vertex v = shoulderVertices.get(i);
						v.position.x -= (float) aftShoulder.thickness();
					}
				}
				copyIndexRange(newShoulderIndices, shoulderMesh.getIndices(), generatedShoulder.sections().walls());
				copyIndexRange(newShoulderIndices, shoulderMesh.getIndices(), generatedShoulder.sections().aftCap());
			}


			float shoulderTranslateX = length / 2.0f + (float) aftShoulder.length() / 2.0f;
			for (Vertex v : shoulderVertices) {
				v.position.x += shoulderTranslateX;
				vertexList.add(v);
			}
			indexList.addAllOffset(newShoulderIndices, shoulderVertexOffset);
		}

		return new Mesh(vertexList, indexList);
	}

	/**
	 * Creates the connection plates between a tube and a shoulder.
	 * Generates two separate rings: one for the outer shells and one for the inner shells at a different depth.
	 */
	private static void addShoulderConnector(List<Vertex> vertexList, IntList indexList,
											 float xOuter, float tubeOuterR, float shoulderOuterR,
											 float xInner, float tubeInnerR, float shoulderInnerR,
											 int segments, boolean isFore, boolean isTubeFilled) {
		// --- Outer Connection Plate ---
		Vector3f normalOuter = new Vector3f(isFore ? -1 : 1, 0, 0);
		int edgeSurfaceID = isFore ? RenderingConstants.SURFACE_ID_FORE : RenderingConstants.SURFACE_ID_AFT;
		for (int i = 0; i < segments; i++) {
			int baseVtxIdx = vertexList.size();
			double theta1 = 2.0 * Math.PI * i / segments;
			double theta2 = 2.0 * Math.PI * (i + 1) / segments;
			float cos1 = (float) Math.cos(theta1), sin1 = (float) Math.sin(theta1);
			float cos2 = (float) Math.cos(theta2), sin2 = (float) Math.sin(theta2);

			vertexList.add(new Vertex(new Vector3f(xOuter, tubeOuterR * cos1, tubeOuterR * sin1), normalOuter,
					new Vector2f(0, 0), edgeSurfaceID));
			vertexList.add(new Vertex(new Vector3f(xOuter, shoulderOuterR * cos1, shoulderOuterR * sin1), normalOuter,
					new Vector2f(0, 0), edgeSurfaceID));
			vertexList.add(new Vertex(new Vector3f(xOuter, shoulderOuterR * cos2, shoulderOuterR * sin2), normalOuter,
					new Vector2f(0, 0), edgeSurfaceID));
			vertexList.add(new Vertex(new Vector3f(xOuter, tubeOuterR * cos2, tubeOuterR * sin2), normalOuter,
					new Vector2f(0, 0), edgeSurfaceID));
			addQuad(indexList, baseVtxIdx, baseVtxIdx + 1, baseVtxIdx + 2, baseVtxIdx + 3, isFore);
		}

		// --- Inner Connection Plate ---
		if (!isTubeFilled) {
			Vector3f normalInner = new Vector3f(isFore ? 1 : -1, 0, 0); // Faces opposite to the outer plate
			for (int i = 0; i < segments; i++) {
				int baseVtxIdx = vertexList.size();
				double theta1 = 2.0 * Math.PI * i / segments;
				double theta2 = 2.0 * Math.PI * (i + 1) / segments;
				float cos1 = (float) Math.cos(theta1), sin1 = (float) Math.sin(theta1);
				float cos2 = (float) Math.cos(theta2), sin2 = (float) Math.sin(theta2);

				vertexList.add(new Vertex(new Vector3f(xInner, tubeInnerR * cos1, tubeInnerR * sin1), normalInner,
						new Vector2f(0, 0), RenderingConstants.SURFACE_ID_INSIDE));
				vertexList.add(new Vertex(new Vector3f(xInner, shoulderInnerR * cos1, shoulderInnerR * sin1), normalInner,
						new Vector2f(0, 0), RenderingConstants.SURFACE_ID_INSIDE));
				vertexList.add(new Vertex(new Vector3f(xInner, shoulderInnerR * cos2, shoulderInnerR * sin2), normalInner,
						new Vector2f(0, 0), RenderingConstants.SURFACE_ID_INSIDE));
				vertexList.add(new Vertex(new Vector3f(xInner, tubeInnerR * cos2, tubeInnerR * sin2), normalInner,
						new Vector2f(0, 0), RenderingConstants.SURFACE_ID_INSIDE));
				addQuad(indexList, baseVtxIdx, baseVtxIdx + 1, baseVtxIdx + 2, baseVtxIdx + 3, !isFore);
			}
		}
	}

	public static Mesh create(float foreOuterRadius, float aftOuterRadius, float wallThickness, float length,
							  int numSegments, boolean isFilled) {
		List<RadiusPoint> profile = List.of(
				new RadiusPoint(0.0f, foreOuterRadius),
				new RadiusPoint(1.0f, aftOuterRadius)
		);
		return create(profile, wallThickness, length, numSegments, isFilled, null, null);
	}

	public static Mesh create(float outerRadius, float wallThickness, float length, int numSegments,
							  boolean isFilled) {
		return create(outerRadius, outerRadius, wallThickness, length, numSegments, isFilled);
	}

	public static Mesh create(float foreOuterRadius, float aftOuterRadius, float wallThickness, float length,
							  int numSegments, boolean isFilled, boolean capFore, boolean capAft) {
		List<Vertex> vertexList = new ArrayList<>();
		IntList indexList = new IntList();
		List<RadiusPoint> profile = List.of(
				new RadiusPoint(0.0f, foreOuterRadius),
				new RadiusPoint(1.0f, aftOuterRadius)
		);
		createFromProfile(vertexList, indexList, profile, wallThickness, length, numSegments, isFilled, 0,
				capFore, capAft, null, null);
		return new Mesh(vertexList, indexList);
	}

	private static SectionedMesh createSectionedTube(float outerRadius, float wallThickness, float length,
			int numSegments) {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();
		List<RadiusPoint> profile = List.of(
				new RadiusPoint(0.0f, outerRadius),
				new RadiusPoint(1.0f, outerRadius)
		);
		ProfileSections sections = createFromProfile(vertices, indices, profile, wallThickness, length,
				numSegments, false, 0, true, true, null, null);
		return new SectionedMesh(new Mesh(vertices, indices), sections);
	}

	private static void copyIndexRange(IntList destination, IntList source, IndexRange range) {
		destination.addAll(source, range.start(), range.end());
	}

	private static ProfileSections createFromProfile(
			List<Vertex> vertexList, IntList indexList, List<RadiusPoint> outerProfile,
			float wallThickness, float length, int numSegments, boolean isFilled,
			int vertexStartIndex, boolean capFore, boolean capAft,
			Shoulder foreShoulder, Shoulder aftShoulder) {

		if (outerProfile == null || outerProfile.size() < 2) {
			throw new IllegalArgumentException("Outer profile must contain at least two points.");
		}

		List<RadiusPoint> sortedOuterProfile = outerProfile.stream()
				.sorted(Comparator.comparingDouble(RadiusPoint::position))
				.collect(Collectors.toList());

		List<RadiusPoint> innerProfile = null;
		if (!isFilled) {
			// Allow over-thick hollow components to collapse to a solid centerline instead of
			// failing the entire 3D view. This occurs legitimately near pointed tips where the
			// local outer radius can be smaller than the configured wall thickness.
			innerProfile = sortedOuterProfile.stream()
					.map(p -> new RadiusPoint(p.position, Math.max(0, p.radius - wallThickness)))
					.toList();
			float maxRadius = (float) sortedOuterProfile.stream()
					.mapToDouble(RadiusPoint::radius).max().orElse(0.0);
			if (wallThickness > 0 && wallThickness > maxRadius) {
				log.warn("Clamping hollow 3D profile to a solid centerline because wall thickness {} exceeds max outer radius {} (length={}, profilePoints={})",
						wallThickness, maxRadius, length, sortedOuterProfile.size());
			}
		}

		float halfLength = length / 2.0f;

		// === PASS 1: Pre-calculate the slopes of each flat segment ===
		List<Float> outerSlopes = new ArrayList<>();
		List<Float> innerSlopes = new ArrayList<>();
		for (int i = 0; i < sortedOuterProfile.size() - 1; i++) {
			RadiusPoint start = sortedOuterProfile.get(i), end = sortedOuterProfile.get(i + 1);
			float sectionLength = (end.position - start.position) * length;
			if (sectionLength != 0) {
				outerSlopes.add((end.radius - start.radius) / sectionLength);
				if (!isFilled) {
					RadiusPoint innerStart = innerProfile.get(i), innerEnd = innerProfile.get(i + 1);
					innerSlopes.add((innerEnd.radius - innerStart.radius) / sectionLength);
				}
			} else {
				outerSlopes.add(0f);
				if (!isFilled) innerSlopes.add(0f);
			}
		}

		// === PASS 2: Generate all vertices with smoothed normals ===
		boolean hasForeShoulder = foreShoulder != null && foreShoulder.length() > 0;
		boolean hasAftShoulder = aftShoulder != null && aftShoulder.length() > 0;
		boolean foreShoulderOffsetAdded = false;

		for (int i = 0; i < sortedOuterProfile.size(); i++) {
			RadiusPoint currentPoint = sortedOuterProfile.get(i);
			float x = (currentPoint.position - 0.5f) * length;
			float v = 1 - currentPoint.position;
			float avgOuterSlope = getAvgOuterSlope(sortedOuterProfile, outerSlopes, i);
			float avgInnerSlope = isFilled ? 0 : getAvgOuterSlope(sortedOuterProfile, innerSlopes, i);

			// Offset the inner shell at shoulder transitions.
			float innerX = x;
			if (!isFilled) {
				if (hasForeShoulder && !foreShoulderOffsetAdded && i < sortedOuterProfile.size() - 1) {
					RadiusPoint nextPoint = sortedOuterProfile.get(i + 1);
					float nextX = (nextPoint.position - 0.5f) * length;
					if (nextX > -halfLength + (float) foreShoulder.thickness()) {
						innerX = (float) foreShoulder.thickness() - halfLength;
						foreShoulderOffsetAdded = true;
					}
				}
				if (hasAftShoulder && x >= (halfLength - (float) aftShoulder.thickness())) {
					innerX = halfLength - (float) aftShoulder.thickness();
				}
			}

			for (int j = 0; j <= numSegments; j++) {
				float theta = (float) (2.0 * Math.PI * j / numSegments);
				float u = 1 - ((float) j) / numSegments;
				float cos = (float) Math.cos(theta), sin = (float) Math.sin(theta);

				Vector3f nOuter = new Vector3f(avgOuterSlope, cos, sin).normalize();
				Vector3f pOuter = new Vector3f(x, currentPoint.radius * cos, currentPoint.radius * sin);
				vertexList.add(new Vertex(pOuter, nOuter, new Vector2f(u, v), RenderingConstants.SURFACE_ID_OUTSIDE));

				// Inner vertices are now always generated for a hollow tube to ensure consistent data layout.
				if (!isFilled) {
					RadiusPoint currentInnerPoint = innerProfile.get(i);
					Vector3f nInner = new Vector3f(-avgInnerSlope, -cos, -sin).normalize();
					Vector3f pInner = new Vector3f(innerX, currentInnerPoint.radius * cos, currentInnerPoint.radius * sin);
					vertexList.add(new Vertex(pInner, nInner, new Vector2f(u, v), RenderingConstants.SURFACE_ID_INSIDE));
				}
			}
		}

		// === PASS 3: Index the vertices to create faces ===
		int wallsStart = indexList.size();
		int verticesPerRing = (isFilled ? 1 : 2) * (numSegments + 1);
		for (int i = 0; i < sortedOuterProfile.size() - 1; i++) {
			for (int j = 0; j < numSegments; j++) {
				int p1_outer = vertexStartIndex + i * verticesPerRing + (j * (isFilled ? 1 : 2));
				int p2_outer = vertexStartIndex + (i + 1) * verticesPerRing + (j * (isFilled ? 1 : 2));
				int p3_outer = vertexStartIndex + (i + 1) * verticesPerRing + ((j + 1) * (isFilled ? 1 : 2));
				int p4_outer = vertexStartIndex + i * verticesPerRing + ((j + 1) * (isFilled ? 1 : 2));

				addQuadIndices(indexList, p1_outer, p4_outer, p3_outer, p2_outer);

				if (!isFilled) {
					int p1_inner = p1_outer + 1, p2_inner = p2_outer + 1;
					int p3_inner = p3_outer + 1, p4_inner = p4_outer + 1;
					addQuadIndices(indexList, p1_inner, p2_inner, p3_inner, p4_inner);
				}
			}
		}
		int wallsEnd = indexList.size();

		// === PASS 4: Generate End Caps (only for ends without shoulders) ===
		int foreCapStart = indexList.size();
		if (capFore) {
			RadiusPoint firstPoint = sortedOuterProfile.get(0);
			if (firstPoint.radius > 0) {
				float firstX = (firstPoint.position - 0.5f) * length;
				if (isFilled) {
					generateFilledCap(vertexList, indexList, firstPoint.radius, firstX, numSegments, true,
							vertexList.size());
				} else {
					generateRingCap(vertexList, indexList, firstPoint.radius, innerProfile.get(0).radius, firstX,
							numSegments, true);
				}
			}
		}
		int foreCapEnd = indexList.size();

		int aftCapStart = indexList.size();
		if (capAft) {
			RadiusPoint lastPoint = sortedOuterProfile.get(sortedOuterProfile.size() - 1);
			if (lastPoint.radius > 0) {
				float lastX = (lastPoint.position - 0.5f) * length;
				if (isFilled) {
					generateFilledCap(vertexList, indexList, lastPoint.radius, lastX, numSegments, true,
							vertexList.size());
				} else {
					generateRingCap(vertexList, indexList, lastPoint.radius,
							innerProfile.get(innerProfile.size() - 1).radius, lastX, numSegments, false);
				}
			}
		}
		int aftCapEnd = indexList.size();

		return new ProfileSections(
				new IndexRange(wallsStart, wallsEnd),
				new IndexRange(foreCapStart, foreCapEnd),
				new IndexRange(aftCapStart, aftCapEnd));
	}

	private static float getAvgOuterSlope(List<RadiusPoint> sortedOuterProfile, List<Float> outerSlopes, int i) {
		float avgOuterSlope;
		if (i == 0) avgOuterSlope = outerSlopes.get(0);
		else if (i == sortedOuterProfile.size() - 1) avgOuterSlope = outerSlopes.get(outerSlopes.size() - 1);
		else avgOuterSlope = (outerSlopes.get(i - 1) + outerSlopes.get(i)) / 2.0f;
		return avgOuterSlope;
	}

	private static void addQuadIndices(IntList indices, int p1, int p2, int p3, int p4) {
		indices.add(p1); indices.add(p2); indices.add(p3);
		indices.add(p1); indices.add(p3); indices.add(p4);
	}

	private static void addQuad(IntList indices, int p1, int p2, int p3, int p4, boolean isFore) {
		if (isFore) {
			indices.add(p1); indices.add(p2); indices.add(p3);
			indices.add(p1); indices.add(p3); indices.add(p4);
		} else {
			indices.add(p1); indices.add(p4); indices.add(p3);
			indices.add(p1); indices.add(p3); indices.add(p2);
		}
	}

	private static void generateFilledCap(List<Vertex> vertexList, IntList indexList, float radius, float x,
										  int numSegments, boolean isFore, int capOffset) {
		boolean isCCWCulling = isFore;

		Vector3f normal = new Vector3f(isCCWCulling ? -1 : 1, 0, 0);
		int surfaceID = isFore ? RenderingConstants.SURFACE_ID_FORE : RenderingConstants.SURFACE_ID_AFT;
		vertexList.add(new Vertex(new Vector3f(x, 0, 0), normal, new Vector2f(0.5f, 0.5f), surfaceID));
		for (int i = 0; i <= numSegments; i++) {
			float theta = (float) (2.0 * Math.PI * i / numSegments);
			Vector3f p = new Vector3f(x, radius * (float)Math.cos(theta), radius * (float)Math.sin(theta));
			vertexList.add(new Vertex(p, normal, getCapUV(p), surfaceID));
		}
		for (int i = 0; i < numSegments; i++) {
			if (isCCWCulling) {
				indexList.add(capOffset);
				indexList.add(capOffset + 1 + (i + 1));
				indexList.add(capOffset + 1 + i);
			} else {
				indexList.add(capOffset);
				indexList.add(capOffset + 1 + i);
				indexList.add(capOffset + 1 + (i + 1));
			}
		}
	}

	private static void generateRingCap(List<Vertex> vertexList, IntList indexList, float outerR, float innerR,
										float x, int numSegments, boolean isFore) {
		boolean isCCWCulling = isFore;

		Vector3f normal = new Vector3f(isCCWCulling ? -1 : 1, 0, 0);
		int surfaceID = isFore ? RenderingConstants.SURFACE_ID_FORE : RenderingConstants.SURFACE_ID_AFT;
		for (int i = 0; i < numSegments; i++) {
			float theta1 = (float) (2.0 * Math.PI * i / numSegments);
			float theta2 = (float) (2.0 * Math.PI * (i + 1) / numSegments);
			float cos1 = (float)Math.cos(theta1), sin1 = (float)Math.sin(theta1);
			float cos2 = (float)Math.cos(theta2), sin2 = (float)Math.sin(theta2);

			Vector3f pOuter1 = new Vector3f(x, outerR * cos1, outerR * sin1);
			Vector3f pInner1 = new Vector3f(x, innerR * cos1, innerR * sin1);
			Vector3f pOuter2 = new Vector3f(x, outerR * cos2, outerR * sin2);
			Vector3f pInner2 = new Vector3f(x, innerR * cos2, innerR * sin2);

			int baseIndex = vertexList.size();
			vertexList.add(new Vertex(pInner1, normal, getRingCapUV(theta1, innerR, outerR, innerR, isFore), surfaceID));
			vertexList.add(new Vertex(pOuter1, normal, getRingCapUV(theta1, outerR, outerR, innerR, isFore), surfaceID));
			vertexList.add(new Vertex(pOuter2, normal, getRingCapUV(theta2, outerR, outerR, innerR, isFore), surfaceID));
			vertexList.add(new Vertex(pInner2, normal, getRingCapUV(theta2, innerR, outerR, innerR, isFore), surfaceID));

			if (isCCWCulling) {
				addQuadIndices(indexList, baseIndex, baseIndex + 3, baseIndex + 2, baseIndex + 1);
			} else {
				addQuadIndices(indexList, baseIndex, baseIndex + 1, baseIndex + 2, baseIndex + 3);
			}
		}
	}

	private static Vector2f getCapUV(Vector3f p) {
		return new Vector2f(p.y * 0.5f + 0.5f, p.z * 0.5f + 0.5f);
	}

	private static Vector2f getRingCapUV(float theta, float radius, float outerR, float innerR, boolean isFore) {
		float u = 1.0f - theta / (float) (2.0 * Math.PI);
		float radialSpan = Math.max(outerR - innerR, 1.0e-6f);
		float radialOffset = (outerR - radius) / radialSpan;
		float v = isFore ? 1.0f - radialOffset : radialOffset;
		return new Vector2f(u, v);
	}
}
