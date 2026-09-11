package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.properties.GraphicsQualitySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.DelaunayTriangulationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates one fin and tab as a manifold mesh. JTS triangulates concave
 * planforms, and the curved root follows the parent body's surface.
 */
public class FinSetGenerator {

	private static final Logger log = LoggerFactory.getLogger(FinSetGenerator.class);
	private static final GeometryFactory geometryFactory = new GeometryFactory();
	private static final float POINT_EPSILON = 1e-9f;
	private static final double PLANFORM_INTERSECTION_EPSILON = 1e-6;
	private static final float PLANFORM_SPAN_EPSILON = 1e-6f;
	private static final float NORMAL_LENGTH_SQUARED_EPSILON = 1e-12f;
	private static final double TRIANGULATION_WELD_EPSILON = 1e-6;

	// Data structure to store curved root connection information
	private static class CurvedRootInfo {
		List<Integer> frontEdgeVertices = new ArrayList<>(); // Vertices on the front side (z=+t/2) of the root patch
		List<Integer> backEdgeVertices = new ArrayList<>();  // Vertices on the back side (z=-t/2) of the root patch
		List<Integer> p1EdgeVertexIndices = new ArrayList<>(); // Full edge for side connection (at start of segment)
		List<Integer> p2EdgeVertexIndices = new ArrayList<>(); // Full edge for side connection (at end of segment)
		int startVertexIndex;
		int endVertexIndex;
		int segmentIndex;
	}

	private enum SegmentType {
		SIDE, ROOT, DEGENERATE
	}

	// Data structure to store side edge information for connection
	private static class SideEdgeInfo {
		List<Integer> edgeVertices = new ArrayList<>();
		int segmentIndex;
		boolean isAdjacentToRoot;
	}

	public static Mesh create(FinSet finSet, SymmetricComponent parent, RenderingConfiguration config) {
		CoordinateIF[] finShape = finSet.generateContinuousFinAndTabShape();
		FinMeshData combinedMeshData = new FinMeshData();

		GraphicsQualitySettings.RenderQuality renderQuality = config.getQuality().getQuality();
		final int rootSegments = switch (renderQuality) {	// Number of segments for the curved root
			case LOW -> RenderingConstants.LOW_FIN_ROOT_SEGMENTS;
			case MEDIUM -> RenderingConstants.MEDIUM_FIN_ROOT_SEGMENTS;
			case HIGH -> RenderingConstants.HIGH_FIN_ROOT_SEGMENTS;
		};
		final int rootXSegments = switch (renderQuality) {	// Number of segments along the root's length for better curvature
			case LOW -> RenderingConstants.LOW_FIN_ROOT_X_SEGMENTS;
			case MEDIUM -> RenderingConstants.MEDIUM_FIN_ROOT_X_SEGMENTS;
			case HIGH -> RenderingConstants.HIGH_FIN_ROOT_X_SEGMENTS;
		};
		final int filletSegments = switch (renderQuality) {	// Number of segments for the fillet arc
			case LOW -> RenderingConstants.LOW_FIN_FILLET_SEGMENTS;
			case MEDIUM -> RenderingConstants.MEDIUM_FIN_FILLET_SEGMENTS;
			case HIGH -> RenderingConstants.HIGH_FIN_FILLET_SEGMENTS;
		};
		final int filletXSegments = switch (renderQuality) {	// Number of segments along the fillet's length
			case LOW -> RenderingConstants.LOW_FIN_FILLET_X_SEGMENTS;
			case MEDIUM -> RenderingConstants.MEDIUM_FIN_FILLET_X_SEGMENTS;
			case HIGH -> RenderingConstants.HIGH_FIN_FILLET_X_SEGMENTS;
		};

		List<CoordinateIF> combinedShape = new ArrayList<>();
		if (finShape != null) {
			combinedShape.addAll(Arrays.asList(finShape));
		}

		if (combinedShape.isEmpty()) {
			return new Mesh(new ArrayList<>(), new IntList());
		}

		float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
		float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
		for (CoordinateIF c : combinedShape) {
			if (c.getX() < minX) minX = (float) c.getX();
			if (c.getX() > maxX) maxX = (float) c.getX();
			if (c.getY() < minY) minY = (float) c.getY();
			if (c.getY() > maxY) maxY = (float) c.getY();
		}
		float spanX = maxX - minX;
		float spanY = maxY - minY;
		if (Math.abs(spanX) < POINT_EPSILON) spanX = 1.0f;
		if (Math.abs(spanY) < POINT_EPSILON) spanY = 1.0f;

		if (finShape.length >= 3) {
			FinMeshData finMeshData = createShapeMesh(finShape, finSet, minX, spanX, minY, spanY, false, parent,
					rootSegments, rootXSegments);
			combinedMeshData.vertices.addAll(finMeshData.vertices);
			combinedMeshData.indices.addAll(finMeshData.indices);
		}

		// Create and add the fillet mesh
		if (renderQuality != GraphicsQualitySettings.RenderQuality.LOW) {
			Mesh filletMesh = createFilletMesh(finSet, parent, filletSegments, filletXSegments,
					minX, spanX, minY, spanY);
			if (filletMesh != null && !filletMesh.getVertices().isEmpty()) {
				int offset = combinedMeshData.vertices.size();
				combinedMeshData.vertices.addAll(filletMesh.getVertices());
				combinedMeshData.indices.addAllOffset(filletMesh.getIndices(), offset);
			}
		}

		if (combinedMeshData.vertices.isEmpty()) {
			return new Mesh(new ArrayList<>(), new IntList());
		}

		return new Mesh(combinedMeshData.vertices, combinedMeshData.indices);
	}

	public static Mesh createFilletMesh(FinSet finSet, SymmetricComponent parent,
										final int filletSegments, final int filletXSegments,
										float globalMinX, float globalSpanX, float globalMinY, float globalSpanY) {
		float filletRadius = (float) finSet.getFilletRadius();
		if (filletRadius <= 0) return null;

		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();

		float finThickness = (float) finSet.getThickness();
		CoordinateIF[] rootPoints = finSet.getRootPoints();
		if (rootPoints == null || rootPoints.length < 2) return null;

		// Fin planform for clamping (in fin local X-Y)
		CoordinateIF[] shapePoints = finSet.generateContinuousFinAndTabShape();

		float axialOffset   = (float) finSet.getAxialOffset(AxialMethod.TOP);
		float rootChordStart= (float) rootPoints[0].getX();
		float rootChordEnd  = (float) rootPoints[rootPoints.length - 1].getX();
		float rootLength    = Math.abs(rootChordEnd - rootChordStart);

		int[][] grid = new int[filletXSegments + 1][2 * (filletSegments + 1)];

		float startRadius = (float) parent.getRadius(rootChordStart + axialOffset);

		for (int i = 0; i <= filletXSegments; i++) {
			float t = (float) i / filletXSegments;
			float currentX = rootChordStart + t * rootLength;
			float parentRadius = (float) parent.getRadius(currentX + axialOffset);

			// --- fillet circle geometry at this X ---
			float z_center_r = finThickness / 2f + filletRadius;

			double cosArg = (finThickness / 2.0 + filletRadius) / (filletRadius + parentRadius);
			if (cosArg > 1.0) cosArg = 1.0;
			if (cosArg < -1.0) cosArg = -1.0;
			float beta = (float) Math.acos(cosArg);

			double insideSqrt = Math.pow(filletRadius + parentRadius, 2) - Math.pow(z_center_r, 2);
			if (insideSqrt < 0) insideSqrt = 0;
			float y_center = (float) (Math.sqrt(insideSqrt) - parentRadius) - (startRadius - parentRadius);

			float z_center_l = -z_center_r;

			for (int j = 0; j <= filletSegments; j++) {
				float theta = ((float) j / filletSegments) * beta;

				float y_ring = y_center - filletRadius * (float) Math.sin(theta);
				float z_r    = z_center_r - filletRadius * (float) Math.cos(theta); // right wall
				float z_l    = z_center_l + filletRadius * (float) Math.cos(theta); // left wall

				// --- Place this ring across the fin's own extent at its height ---
				// The fillet is swept along the root chord, but every ring above the base lies
				// against the fin some way up the span, where sweep has already moved the
				// leading and trailing edges. Keeping all the rings on the root chord's X range
				// let the top of the fillet run past the edge of the fin, leaving it hanging
				// over the body with nothing to blend into. The artefact is worst where the fin
				// meets the body at an acute angle, since that is where the edge travels
				// furthest per unit of height. Spreading each ring over the planform's own span
				// at its Y instead makes the top of the fillet end exactly where the fin does.
				// The base ring sits fractionally below the root plane, following the body's
				// curvature away from the fin, so there is no planform to sample there and it
				// keeps the root chord.
				float ringX = currentX;
				if (j < filletSegments) {
					float[] finSpan = planformSpanAtY(shapePoints, y_ring);
					if (finSpan != null) {
						ringX = finSpan[0] + t * (finSpan[1] - finSpan[0]);
					}
					// else: Y outside the fin -> keep the root chord X
				}

				// Project UV from fin planform space: same axes as the fin face UVs
				float uv_u = (ringX  - globalMinX) / globalSpanX;
				float uv_v = (y_ring - globalMinY) / globalSpanY;

				// Right vertex
				Vector3f pos_r = new Vector3f(ringX, y_ring, z_r);
				Vector3f nrm_r = new Vector3f(0, y_center - y_ring, z_center_r - z_r).normalize();
				grid[i][j] = vertices.size();
				vertices.add(new Vertex(pos_r, nrm_r, new Vector2f(uv_u, uv_v),
						RenderingConstants.SURFACE_ID_RIGHT));

				// Left vertex
				Vector3f pos_l = new Vector3f(ringX, y_ring, z_l);
				Vector3f nrm_l = new Vector3f(0, y_center - y_ring, z_center_l - z_l).normalize();
				grid[i][j + filletSegments + 1] = vertices.size();
				vertices.add(new Vertex(pos_l, nrm_l, new Vector2f(uv_u, uv_v),
						RenderingConstants.SURFACE_ID_OUTSIDE));
			}
		}

		// --- build faces ---
		for (int i = 0; i < filletXSegments; i++) {
			// right band
			for (int j = 0; j < filletSegments; j++) {
				int v00 = grid[i][j];
				int v10 = grid[i + 1][j];
				int v11 = grid[i + 1][j + 1];
				int v01 = grid[i][j + 1];

				indices.add(v00); indices.add(v11); indices.add(v10);
				indices.add(v00); indices.add(v01); indices.add(v11);
			}
			// left band
			int base = filletSegments + 1;
			for (int j = 0; j < filletSegments; j++) {
				int v00 = grid[i][base + j];
				int v10 = grid[i + 1][base + j];
				int v11 = grid[i + 1][base + j + 1];
				int v01 = grid[i][base + j + 1];

				indices.add(v00); indices.add(v10); indices.add(v11);
				indices.add(v00); indices.add(v11); indices.add(v01);
			}
		}

		// End caps
		addFilletCaps(vertices, indices, grid, 0, rootPoints[0], finSet, parent, filletSegments, false,
				shapePoints, globalMinX, globalSpanX, globalMinY, globalSpanY);
		addFilletCaps(vertices, indices, grid, filletXSegments, rootPoints[rootPoints.length - 1],
				finSet, parent, filletSegments, true,
				shapePoints, globalMinX, globalSpanX, globalMinY, globalSpanY);

		return new Mesh(vertices, indices);
	}

	private static void addFilletCaps(List<Vertex> vertices, IntList indices, int[][] gridIndices,
									  int segmentIndex, CoordinateIF rootPoint, FinSet finSet,
									  SymmetricComponent parent, int arcSegments, boolean isEndCap,
									  CoordinateIF[] shapePoints,
									  float globalMinX, float globalSpanX, float globalMinY, float globalSpanY) {
		float x = (float) rootPoint.getX();
		float y = (float) rootPoint.getY(); // y-coordinate of the fin root from the 2D profile
		float axialOffset = (float) finSet.getAxialOffset(AxialMethod.TOP);
		float parentRadius = (float) parent.getRadius(x + axialOffset);

		// Each cap sits at the seam where the fillet meets one of the fin's edge
		// surfaces: the leading cap against the leading edge band, the trailing cap
		// against the trailing edge band. Aligning a cap with the band it abuts
		// (perimeter-walk U + that band's own normal) keeps the texture and shading
		// continuous across the fillet→edge boundary; left on face-space U and a ±X
		// normal, the cap shades and unwraps differently from the edge face touching
		// it, which reads as a seam along the whole join.
		Vector3f normal = isEndCap ? new Vector3f(1, 0, 0) : new Vector3f(-1, 0, 0);
		float capUOverride = Float.NaN;
		if (shapePoints != null && shapePoints.length >= 2) {
			int cornerIndex = findShapePointIndex(shapePoints, rootPoint);
			if (cornerIndex >= 0) {
				capUOverride = perimeterUAt(shapePoints, cornerIndex, globalSpanX);
				Vector3f edgeNormal = adjacentEdgeNormal(shapePoints, cornerIndex,
						(float) finSet.getThickness(), isEndCap);
				if (edgeNormal != null) {
					normal = edgeNormal;
				}
			}
		}

		// Create vertices for the fillet edge and the new curved root edge
		List<Integer> filletEdge = new ArrayList<>();
		List<Integer> rootEdge = new ArrayList<>();

		// Left side of fillet and root
		for (int j = 0; j <= arcSegments; j++) {
			// Get the original vertex from the fillet strip
			int originalVertexIndex = gridIndices[segmentIndex][arcSegments - j + arcSegments + 1];
			Vector3f filletPos = vertices.get(originalVertexIndex).position;
			float filletU = Float.isNaN(capUOverride) ? (filletPos.x - globalMinX) / globalSpanX : capUOverride;
			Vector2f filletUV = new Vector2f(filletU, (filletPos.y - globalMinY) / globalSpanY);
			filletEdge.add(vertices.size());
			vertices.add(new Vertex(filletPos, normal, filletUV, RenderingConstants.SURFACE_ID_EDGE));

			// Create the corresponding root vertex on the parent body
			float z_fillet = filletPos.z;
			// Calculate the "drop" from the fin root baseline to the curved surface of the parent
			float y_drop = (float) (Math.sqrt(Math.max(0, parentRadius * parentRadius - z_fillet * z_fillet)) - parentRadius);
			float y_new = y + y_drop;

			float rootU = Float.isNaN(capUOverride) ? (x - globalMinX) / globalSpanX : capUOverride;
			rootEdge.add(vertices.size());
			vertices.add(new Vertex(new Vector3f(x, y_new, z_fillet), normal,
					new Vector2f(rootU, (y_new - globalMinY) / globalSpanY), RenderingConstants.SURFACE_ID_EDGE));
		}

		// Right side of fillet and root
		for (int j = 0; j <= arcSegments; j++) {
			// Get the original vertex from the fillet strip
			int originalVertexIndex = gridIndices[segmentIndex][j];
			Vector3f filletPos = vertices.get(originalVertexIndex).position;
			float filletU = Float.isNaN(capUOverride) ? (filletPos.x - globalMinX) / globalSpanX : capUOverride;
			Vector2f filletUV = new Vector2f(filletU, (filletPos.y - globalMinY) / globalSpanY);
			filletEdge.add(vertices.size());
			vertices.add(new Vertex(filletPos, normal, filletUV, RenderingConstants.SURFACE_ID_EDGE));

			// Create the corresponding root vertex on the parent body
			float z_fillet = filletPos.z;
			// Calculate the "drop" from the fin root baseline to the curved surface of the parent
			float y_drop = (float) (Math.sqrt(Math.max(0, parentRadius * parentRadius - z_fillet * z_fillet)) - parentRadius);
			float y_new = y + y_drop;

			float rootU = Float.isNaN(capUOverride) ? (x - globalMinX) / globalSpanX : capUOverride;
			rootEdge.add(vertices.size());
			vertices.add(new Vertex(new Vector3f(x, y_new, z_fillet), normal,
					new Vector2f(rootU, (y_new - globalMinY) / globalSpanY), RenderingConstants.SURFACE_ID_EDGE));
		}

		// Triangulate the left cap
		for (int j = 0; j < arcSegments; j++) {
			int r_j = rootEdge.get(j);
			int f_j = filletEdge.get(j);
			int r_j1 = rootEdge.get(j + 1);
			int f_j1 = filletEdge.get(j + 1);

			if (isEndCap) {
				indices.add(r_j); indices.add(f_j); indices.add(f_j1);
				indices.add(r_j); indices.add(f_j1); indices.add(r_j1);
			} else {
				indices.add(r_j); indices.add(f_j1); indices.add(f_j);
				indices.add(r_j); indices.add(r_j1); indices.add(f_j1);
			}
		}

		// Triangulate the right cap
		for (int j = 0; j < arcSegments; j++) {
			int base = arcSegments + 1;
			int r_j = rootEdge.get(base + j);
			int f_j = filletEdge.get(base + j);
			int r_j1 = rootEdge.get(base + j + 1);
			int f_j1 = filletEdge.get(base + j + 1);

			if (isEndCap) {
				indices.add(r_j); indices.add(f_j); indices.add(f_j1);
				indices.add(r_j); indices.add(f_j1); indices.add(r_j1);
			} else {
				indices.add(r_j); indices.add(f_j1); indices.add(f_j);
				indices.add(r_j); indices.add(r_j1); indices.add(f_j1);
			}
		}
	}

	/**
	 * Checks if a given Coordinate is present in an array of root points, using a tolerance.
	 * This is to avoid issues with floating-point precision when comparing points from different sources.
	 *
	 * @param p          The point to check.
	 * @param rootPoints The array of root points to check against.
	 * @param tolerance  The tolerance for comparison.
	 * @return True if the point is found within the tolerance, false otherwise.
	 */
	private static boolean isRootPoint(CoordinateIF p, CoordinateIF[] rootPoints, float tolerance) {
		if (rootPoints == null) return false;
		for (CoordinateIF rootPoint : rootPoints) {
			if (Math.abs(p.getX() - rootPoint.getX()) < tolerance && Math.abs(p.getY() - rootPoint.getY()) < tolerance) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds how far the fin planform reaches along X at a given spanwise Y, by intersecting
	 * the perimeter with the horizontal line at that height.
	 *
	 * @param shapePoints the closed fin (and tab) outline, in fin-local X/Y
	 * @param y           the spanwise height to sample
	 * @return {@code {xMin, xMax}} at that height, or {@code null} when Y lies outside the
	 *         planform or the section is too short to place anything against
	 */
	private static float[] planformSpanAtY(CoordinateIF[] shapePoints, float y) {
		float xMin = Float.POSITIVE_INFINITY;
		float xMax = Float.NEGATIVE_INFINITY;
		boolean hadHorizontal = false;
		int hits = 0;

		for (int k = 0; k < shapePoints.length; k++) {
			CoordinateIF a = shapePoints[k];
			CoordinateIF b = shapePoints[(k + 1) % shapePoints.length];
			double ya = a.getY(), yb = b.getY();

			double ylo = Math.min(ya, yb) - PLANFORM_INTERSECTION_EPSILON;
			double yhi = Math.max(ya, yb) + PLANFORM_INTERSECTION_EPSILON;
			if (y < ylo || y > yhi) continue;

			if (Math.abs(ya - yb) < PLANFORM_INTERSECTION_EPSILON) {
				// A horizontal edge lies along the sample line, so it bounds the section itself.
				hadHorizontal = true;
				float xa = (float) a.getX(), xb = (float) b.getX();
				xMin = Math.min(xMin, Math.min(xa, xb));
				xMax = Math.max(xMax, Math.max(xa, xb));
			} else {
				double te = (y - ya) / (yb - ya);
				if (te >= -PLANFORM_INTERSECTION_EPSILON && te <= 1.0 + PLANFORM_INTERSECTION_EPSILON) {
					float xHit = (float) (a.getX() + te * (b.getX() - a.getX()));
					xMin = Math.min(xMin, xHit);
					xMax = Math.max(xMax, xHit);
					hits++;
				}
			}
		}

		boolean spanOK = (hadHorizontal || hits >= 2) && (xMax - xMin) > PLANFORM_SPAN_EPSILON
				&& Float.isFinite(xMin) && Float.isFinite(xMax);
		return spanOK ? new float[]{xMin, xMax} : null;
	}

	private static int findShapePointIndex(CoordinateIF[] shapePoints, CoordinateIF target) {
		final float tol = 1.0e-5f;
		for (int i = 0; i < shapePoints.length; i++) {
			if (Math.abs(shapePoints[i].getX() - target.getX()) < tol &&
					Math.abs(shapePoints[i].getY() - target.getY()) < tol) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Distance walked around the fin perimeter to reach a given outline point, in the same
	 * units the edge band uses for its U coordinate: the band starts each segment at the
	 * length accumulated before it, so the first point of the walk sits at U = 0.
	 */
	private static float perimeterUAt(CoordinateIF[] shapePoints, int targetIndex, float spanX) {
		float accumulated = 0f;
		for (int i = 0; i < targetIndex && i < shapePoints.length; i++) {
			int end = (i + 1) % shapePoints.length;
			float dx = (float) (shapePoints[end].getX() - shapePoints[i].getX());
			float dy = (float) (shapePoints[end].getY() - shapePoints[i].getY());
			accumulated += (float) Math.sqrt(dx * dx + dy * dy);
		}
		return accumulated / spanX;
	}

	/**
	 * Normal of the fin edge band that meets the fillet at a root corner, built the same way
	 * the band builds its own so the two shade identically where they touch.
	 *
	 * @param cornerIndex   outline index of the root corner the cap sits at
	 * @param incomingEdge  {@code true} at the trailing corner, where the adjacent edge
	 *                      segment arrives at the corner; {@code false} at the leading
	 *                      corner, where it leaves
	 */
	private static Vector3f adjacentEdgeNormal(CoordinateIF[] shapePoints, int cornerIndex,
											   float thickness, boolean incomingEdge) {
		int fromIndex = incomingEdge
				? (cornerIndex - 1 + shapePoints.length) % shapePoints.length
				: cornerIndex;
		int toIndex = incomingEdge
				? cornerIndex
				: (cornerIndex + 1) % shapePoints.length;

		Vector3f p1Front = new Vector3f(
				(float) shapePoints[fromIndex].getX(), (float) shapePoints[fromIndex].getY(), thickness / 2f);
		Vector3f p2Front = new Vector3f(
				(float) shapePoints[toIndex].getX(), (float) shapePoints[toIndex].getY(), thickness / 2f);
		Vector3f p1Back = new Vector3f(
				(float) shapePoints[fromIndex].getX(), (float) shapePoints[fromIndex].getY(), -thickness / 2f);
		Vector3f edge = new Vector3f(p2Front).sub(p1Front);
		Vector3f normal = edge.cross(new Vector3f(p1Back).sub(p1Front));
		if (normal.lengthSquared() < NORMAL_LENGTH_SQUARED_EPSILON) {
			return null;
		}
		return normal.normalize();
	}

	private static FinMeshData createShapeMesh(CoordinateIF[] shapePoints, FinSet finSet,
											   float globalMinX, float globalSpanX, float globalMinY, float globalSpanY,
											   boolean reverseEdgeWinding, SymmetricComponent parent,
											   final int rootSegments, final int rootXSegments) {
		FinMeshData meshData = new FinMeshData();
		int backFaceOffset = shapePoints.length;

		// Create vertices for the FRONT and BACK faces first
		for (CoordinateIF c : shapePoints) {
			float u = ((float) c.getX() - globalMinX) / globalSpanX;
			float v = ((float) c.getY() - globalMinY) / globalSpanY;
			meshData.vertices.add(new Vertex(
					new Vector3f((float) c.getX(), (float) c.getY(), (float) finSet.getThickness() / 2),
					new Vector3f(0, 0, 1), new Vector2f(u, v), RenderingConstants.SURFACE_ID_RIGHT));
		}
		for (CoordinateIF c : shapePoints) {
			float u = ((float) c.getX() - globalMinX) / globalSpanX;
			float v = ((float) c.getY() - globalMinY) / globalSpanY;
			meshData.vertices.add(new Vertex(
					new Vector3f((float) c.getX(), (float) c.getY(), (float) -finSet.getThickness() / 2),
					new Vector3f(0, 0, -1), new Vector2f(u, v), RenderingConstants.SURFACE_ID_OUTSIDE));
		}

		// Triangulate the main front and back faces
		IntList triangleIndices = triangulateWithJTS(shapePoints);
		for (int i = 0; i < triangleIndices.size(); i += 3) {
			meshData.addTriangle(triangleIndices.get(i), triangleIndices.get(i + 1), triangleIndices.get(i + 2));
			meshData.addTriangle(backFaceOffset + triangleIndices.get(i), backFaceOffset + triangleIndices.get(i + 2),
					backFaceOffset + triangleIndices.get(i + 1));
		}

		// --- Pass 1: Classify all perimeter segments ---

		// Get the explicit root points from the FinSet.
		CoordinateIF[] rootPoints = finSet.getRootPoints();

		SegmentType[] segmentTypes = new SegmentType[shapePoints.length];
		float[] segmentLengths = new float[shapePoints.length];

		for (int i = 0; i < shapePoints.length; i++) {
			CoordinateIF p1 = shapePoints[i];
			CoordinateIF p2 = shapePoints[(i + 1) % shapePoints.length];

			if (new Vector3f((float)p1.getX(), (float)p1.getY(), 0).equals(new Vector3f((float)p2.getX(), (float)p2.getY(), 0), POINT_EPSILON)) {
				segmentTypes[i] = SegmentType.DEGENERATE;
				segmentLengths[i] = 0;
				continue;
			}
			segmentLengths[i] = (float) Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2) + Math.pow(p2.getY() - p1.getY(), 2));

			// A segment is a root if both its points are in the explicit root point set.
			if (isRootPoint(p1, rootPoints, POINT_EPSILON) && isRootPoint(p2, rootPoints, POINT_EPSILON)) {
				segmentTypes[i] = SegmentType.ROOT;
			} else {
				segmentTypes[i] = SegmentType.SIDE;
			}
		}

		// --- Pass 2: Generate perimeter geometry and populate info maps ---
		Map<Integer, CurvedRootInfo> curvedRootInfoMap = new HashMap<>();
		Map<Integer, SideEdgeInfo> sideEdgeInfoMap = new HashMap<>();
		float accumulatedLength = 0;

		for (int i = 0; i < shapePoints.length; i++) {
			SegmentType type = segmentTypes[i];
			if (type == SegmentType.DEGENERATE) continue;

			int p1_idx = i;
			int p2_idx = (i + 1) % shapePoints.length;

			if (type == SegmentType.ROOT) {
				CurvedRootInfo rootInfo = createCurvedRootSurface(meshData, p1_idx, p2_idx, backFaceOffset, finSet, parent,
						reverseEdgeWinding, globalMinX, globalSpanX, globalMinY, globalSpanY, rootSegments, rootXSegments);
				rootInfo.segmentIndex = i;
				curvedRootInfoMap.put(i, rootInfo);
			} else { // type == SegmentType.SIDE
				Vector3f p1_front = meshData.vertices.get(p1_idx).position;
				Vector3f p2_front = meshData.vertices.get(p2_idx).position;
				Vector3f p1_back = meshData.vertices.get(p1_idx + backFaceOffset).position;
				Vector3f p2_back = meshData.vertices.get(p2_idx + backFaceOffset).position;

				int baseIndex = meshData.vertices.size();
				float thickness = (float) finSet.getThickness();

				// U-coordinate is the unwrapped distance along the perimeter, scaled by the fin's total width (spanX)
				float u0 = accumulatedLength / globalSpanX;
				float u1 = (accumulatedLength + segmentLengths[i]) / globalSpanX;
				// V-coordinate is the distance across the fin's thickness, scaled by the fin's total height (spanY)
				float v0 = 0;
				float v1 = thickness / globalSpanY;

				if (finSet instanceof EllipticalFinSet) {
					// For elliptical fins, calculate smoothed normals based on the true curve
					double a = finSet.getLength() / 2.0;
					double b = finSet.getSpan();

					CoordinateIF p1 = shapePoints[p1_idx];
					CoordinateIF p2 = shapePoints[p2_idx];

					// Get coordinates relative to the ellipse's center to calculate the gradient
					double p1_x_rel = p1.getX() - a;
					double p2_x_rel = p2.getX() - a;

					// The normal is derived from the gradient of (x/a)^2 + (y/b)^2 = 1
					Vector3f normal1 = new Vector3f((float)(p1_x_rel / (a * a)), (float)(p1.getY() / (b * b)), 0).normalize();
					Vector3f normal2 = new Vector3f((float)(p2_x_rel / (a * a)), (float)(p2.getY() / (b * b)), 0).normalize();

					// Keep the edge band distinct from the broad outside faces.
					meshData.vertices.add(new Vertex(p1_front, normal1, new Vector2f(u0, v1), RenderingConstants.SURFACE_ID_EDGE));
					meshData.vertices.add(new Vertex(p2_front, normal2, new Vector2f(u1, v1), RenderingConstants.SURFACE_ID_EDGE));
					meshData.vertices.add(new Vertex(p2_back, normal2, new Vector2f(u1, v0), RenderingConstants.SURFACE_ID_EDGE));
					meshData.vertices.add(new Vertex(p1_back, normal1, new Vector2f(u0, v0), RenderingConstants.SURFACE_ID_EDGE));
				} else {
					// For other fin types, use a single normal for a faceted look
					Vector3f edge = new Vector3f(p2_front).sub(p1_front);
					Vector3f thicknessVec = new Vector3f(p1_back).sub(p1_front);
					Vector3f normal = new Vector3f(edge).cross(thicknessVec).normalize();

					// Keep the edge band distinct from the broad outside faces.
					meshData.vertices.add(new Vertex(p1_front, normal, new Vector2f(u0, v1), RenderingConstants.SURFACE_ID_EDGE));
					meshData.vertices.add(new Vertex(p2_front, normal, new Vector2f(u1, v1), RenderingConstants.SURFACE_ID_EDGE));
					meshData.vertices.add(new Vertex(p2_back, normal, new Vector2f(u1, v0), RenderingConstants.SURFACE_ID_EDGE));
					meshData.vertices.add(new Vertex(p1_back, normal, new Vector2f(u0, v0), RenderingConstants.SURFACE_ID_EDGE));
				}

				int prevSegIdx = findNextNonDegenerateSegment(i, segmentTypes, -1);
				int nextSegIdx = findNextNonDegenerateSegment(i, segmentTypes, 1);
				boolean isAdjacent = (prevSegIdx != -1 && segmentTypes[prevSegIdx] == SegmentType.ROOT) ||
						(nextSegIdx != -1 && segmentTypes[nextSegIdx] == SegmentType.ROOT);

				SideEdgeInfo sideInfo = new SideEdgeInfo();
				sideInfo.segmentIndex = i;
				sideInfo.isAdjacentToRoot = isAdjacent;
				sideInfo.edgeVertices.add(baseIndex);
				sideInfo.edgeVertices.add(baseIndex + 1);
				sideInfo.edgeVertices.add(baseIndex + 2);
				sideInfo.edgeVertices.add(baseIndex + 3);
				sideEdgeInfoMap.put(i, sideInfo);

				if (reverseEdgeWinding) {
					meshData.addTriangle(baseIndex, baseIndex + 2, baseIndex + 1);
					meshData.addTriangle(baseIndex, baseIndex + 3, baseIndex + 2);
				} else {
					meshData.addTriangle(baseIndex, baseIndex + 1, baseIndex + 2);
					meshData.addTriangle(baseIndex, baseIndex + 2, baseIndex + 3);
				}
				accumulatedLength += segmentLengths[i];
			}
		}

		// --- Pass 3: Create connection faces between surfaces ---
		createRootConnectionFaces(meshData, curvedRootInfoMap, sideEdgeInfoMap, shapePoints, backFaceOffset,
				reverseEdgeWinding, segmentTypes, globalMinX, globalSpanX, globalMinY, globalSpanY, rootSegments, rootXSegments);

		return meshData;
	}

	private static CurvedRootInfo createCurvedRootSurface(
			FinMeshData meshData, int p1_idx, int p2_idx, int backFaceOffset, FinSet finSet, SymmetricComponent parent,
			boolean reverseEdgeWinding, float globalMinX, float globalSpanX, float globalMinY, float globalSpanY,
			final int rootSegments, final int rootXSegments) {
		Vector3f p1_base = meshData.vertices.get(p1_idx).position;
		Vector3f p2_base = meshData.vertices.get(p2_idx).position;

		float finThickness = (float) finSet.getThickness();
		float axialOffset = (float) finSet.getAxialOffset(AxialMethod.TOP);

		int[][] gridIndices = new int[rootXSegments + 1][rootSegments + 1];

		for (int j = 0; j <= rootXSegments; j++) {
			float tx = (float) j / rootXSegments;

			// Interpolate the base X and Y coordinates from the 2D fin profile
			float currentX = p1_base.x + tx * (p2_base.x - p1_base.x);
			float baseY = p1_base.y + tx * (p2_base.y - p1_base.y);

			float r = (float) parent.getRadius(currentX + axialOffset);

			for (int i = 0; i <= rootSegments; i++) {
				float tz = (float) i / rootSegments;
				float z = -finThickness / 2 + tz * finThickness;

				// Calculate the "drop" from the fin root baseline to the curved surface
				float y_drop = (r > Math.abs(z)) ? (float) (Math.sqrt(r * r - z * z) - r) : -r;

				// The final Y is the tapered baseline Y plus the curvature drop.
				float finalY = baseY + y_drop;

				Vector3f pos = new Vector3f(currentX, finalY, z);
				Vector3f normal = new Vector3f(0, y_drop + r, z).normalize();
				// Use the same scaling as the main faces for consistent texture density
				float u = (currentX - globalMinX) / globalSpanX;
				float v = (finalY - globalMinY) / globalSpanY;
				Vector2f uv = new Vector2f(u,v);

				gridIndices[j][i] = meshData.vertices.size();
				meshData.vertices.add(new Vertex(pos, normal, uv, RenderingConstants.SURFACE_ID_EDGE));
			}
		}

		// --- Create triangles for the grid surface ---
		for (int j = 0; j < rootXSegments; j++) {
			for (int i = 0; i < rootSegments; i++) {
				int v00 = gridIndices[j][i];
				int v10 = gridIndices[j + 1][i];
				int v11 = gridIndices[j + 1][i + 1];
				int v01 = gridIndices[j][i + 1];

				if (reverseEdgeWinding) {
					meshData.addTriangle(v00, v10, v11);
					meshData.addTriangle(v00, v11, v01);
				} else {
					meshData.addTriangle(v00, v11, v10);
					meshData.addTriangle(v00, v01, v11);
				}
			}
		}

		CurvedRootInfo rootInfo = new CurvedRootInfo();
		rootInfo.startVertexIndex = p1_idx;
		rootInfo.endVertexIndex = p2_idx;

		for (int i = 0; i <= rootSegments; i++) {
			rootInfo.p1EdgeVertexIndices.add(gridIndices[0][i]);
			rootInfo.p2EdgeVertexIndices.add(gridIndices[rootXSegments][i]);
		}
		for (int j = 0; j <= rootXSegments; j++) {
			rootInfo.backEdgeVertices.add(gridIndices[j][0]);
			rootInfo.frontEdgeVertices.add(gridIndices[j][rootSegments]);
		}

		return rootInfo;
	}

	private static void createRootConnectionFaces(FinMeshData meshData, Map<Integer, CurvedRootInfo> curvedRootInfoMap,
												  Map<Integer, SideEdgeInfo> sideEdgeInfoMap, CoordinateIF[] shapePoints,
												  int backFaceOffset, boolean reverseEdgeWinding, SegmentType[] segmentTypes,
												  float globalMinX, float globalSpanX, float globalMinY, float globalSpanY,
												  final int rootSegments, final int rootXSegments) {
		for (Map.Entry<Integer, CurvedRootInfo> entry : curvedRootInfoMap.entrySet()) {
			int segmentIndex = entry.getKey();
			CurvedRootInfo rootInfo = entry.getValue();

			// Connect front face (z=+t/2) to the curved root's front edge
			if (rootInfo.frontEdgeVertices.size() > 1) {
				int p1FaceIdx = rootInfo.startVertexIndex;
				int p2FaceIdx = rootInfo.endVertexIndex;
				List<Integer> curvedEdge = rootInfo.frontEdgeVertices;

				Vector3f p1FacePos = meshData.vertices.get(p1FaceIdx).position;
				Vector3f p2FacePos = meshData.vertices.get(p2FaceIdx).position;
				Vector3f c0Pos = meshData.vertices.get(curvedEdge.get(0)).position;

				Vector3f edge1 = new Vector3f(p2FacePos).sub(p1FacePos);
				Vector3f edge2 = new Vector3f(c0Pos).sub(p1FacePos);
				Vector3f normal = new Vector3f(edge1).cross(edge2).normalize();

				if (Float.isNaN(normal.x)) {
					Vector3f segmentDir = new Vector3f(p2FacePos).sub(p1FacePos);
					normal = segmentDir.cross(new Vector3f(0, 0, 1)).normalize();
				}

				int p1New = meshData.vertices.size();
				float u1 = (p1FacePos.x - globalMinX) / globalSpanX;
				float v1 = (p1FacePos.y - globalMinY) / globalSpanY;
				meshData.vertices.add(new Vertex(p1FacePos, normal, new Vector2f(u1, v1),
						RenderingConstants.SURFACE_ID_RIGHT));

				int p2New = meshData.vertices.size();
				float u2 = (p2FacePos.x - globalMinX) / globalSpanX;
				float v2 = (p2FacePos.y - globalMinY) / globalSpanY;
				meshData.vertices.add(new Vertex(p2FacePos, normal, new Vector2f(u2, v2),
						RenderingConstants.SURFACE_ID_RIGHT));


				List<Integer> curvedEdgeNew = new ArrayList<>();
				for (Integer idx : curvedEdge) {
					curvedEdgeNew.add(meshData.vertices.size());
					Vector3f pos = meshData.vertices.get(idx).position;
					float u = (pos.x - globalMinX) / globalSpanX;
					float v = (pos.y - globalMinY) / globalSpanY;
					meshData.vertices.add(new Vertex(pos, normal, new Vector2f(u, v),
							RenderingConstants.SURFACE_ID_RIGHT));
				}

				if (reverseEdgeWinding) { // CW
					meshData.addTriangle(p1New, curvedEdgeNew.get(curvedEdgeNew.size() - 1), p2New);
					for (int i = curvedEdgeNew.size() - 1; i > 0; i--) {
						meshData.addTriangle(p1New, curvedEdgeNew.get(i - 1), curvedEdgeNew.get(i));
					}
				} else { // CCW
					meshData.addTriangle(p1New, p2New, curvedEdgeNew.get(curvedEdgeNew.size() - 1));
					for (int i = curvedEdgeNew.size() - 1; i > 0; i--) {
						meshData.addTriangle(p1New, curvedEdgeNew.get(i), curvedEdgeNew.get(i - 1));
					}
				}
			}

			// Connect back face (z=-t/2) to the curved root's back edge
			if (rootInfo.backEdgeVertices.size() > 1) {
				int p1FaceIdx = rootInfo.startVertexIndex + backFaceOffset;
				int p2FaceIdx = rootInfo.endVertexIndex + backFaceOffset;
				List<Integer> curvedEdge = rootInfo.backEdgeVertices;

				Vector3f p1FacePos = meshData.vertices.get(p1FaceIdx).position;
				Vector3f p2FacePos = meshData.vertices.get(p2FaceIdx).position;
				Vector3f c0Pos = meshData.vertices.get(curvedEdge.get(0)).position;

				Vector3f edge1 = new Vector3f(p2FacePos).sub(p1FacePos);
				Vector3f edge2 = new Vector3f(c0Pos).sub(p1FacePos);
				Vector3f normal = new Vector3f(edge2).cross(edge1).normalize();

				if (Float.isNaN(normal.x)) {
					Vector3f segmentDir = new Vector3f(p2FacePos).sub(p1FacePos);
					normal = new Vector3f(0, 0, -1).cross(segmentDir).normalize();
				}

				int p1New = meshData.vertices.size();
				float u1 = (p1FacePos.x - globalMinX) / globalSpanX;
				float v1 = (p1FacePos.y - globalMinY) / globalSpanY;
				meshData.vertices.add(new Vertex(p1FacePos, normal, new Vector2f(u1, v1),
						RenderingConstants.SURFACE_ID_OUTSIDE));

				int p2New = meshData.vertices.size();
				float u2 = (p2FacePos.x - globalMinX) / globalSpanX;
				float v2 = (p2FacePos.y - globalMinY) / globalSpanY;
				meshData.vertices.add(new Vertex(p2FacePos, normal, new Vector2f(u2, v2),
						RenderingConstants.SURFACE_ID_OUTSIDE));

				List<Integer> curvedEdgeNew = new ArrayList<>();
				for (Integer idx : curvedEdge) {
					curvedEdgeNew.add(meshData.vertices.size());
					Vector3f pos = meshData.vertices.get(idx).position;
					float u = (pos.x - globalMinX) / globalSpanX;
					float v = (pos.y - globalMinY) / globalSpanY;
					meshData.vertices.add(new Vertex(pos, normal, new Vector2f(u, v),
							RenderingConstants.SURFACE_ID_OUTSIDE));
				}

				if (reverseEdgeWinding) { // CCW
					meshData.addTriangle(p1New, p2New, curvedEdgeNew.get(curvedEdgeNew.size() - 1));
					for (int i = curvedEdgeNew.size() - 1; i > 0; i--) {
						meshData.addTriangle(p1New, curvedEdgeNew.get(i), curvedEdgeNew.get(i - 1));
					}
				} else { // CW
					meshData.addTriangle(p1New, curvedEdgeNew.get(curvedEdgeNew.size() - 1), p2New);
					for (int i = curvedEdgeNew.size() - 1; i > 0; i--) {
						meshData.addTriangle(p1New, curvedEdgeNew.get(i - 1), curvedEdgeNew.get(i));
					}
				}
			}

			// --- Connect to adjacent edges ---
			// Connect to the previous and next adjacent segments.
			// Root-to-root connections are handled symmetrically by only connecting to the previous segment,
			// but root-to-side connections must be explicitly created in both directions.
			int prevSegment = findNextNonDegenerateSegment(segmentIndex, segmentTypes, -1);
			if (prevSegment != -1) {
				if (sideEdgeInfoMap.containsKey(prevSegment)) {
					// Connect to an adjacent SIDE segment
					connectRootToSideEdge(meshData, rootInfo, sideEdgeInfoMap.get(prevSegment), true, reverseEdgeWinding,
							globalMinX, globalSpanX, globalMinY, globalSpanY, rootSegments, rootXSegments);
				} else if (curvedRootInfoMap.containsKey(prevSegment)) {
					// Connect to an adjacent ROOT segment
					CurvedRootInfo prevRoot = curvedRootInfoMap.get(prevSegment);
					connectRootToRoot(meshData, rootInfo.p1EdgeVertexIndices, prevRoot.p2EdgeVertexIndices, reverseEdgeWinding, globalMinX, globalSpanX, globalMinY, globalSpanY);
				}
			}

			// Connect to the *next* segment if it's a side piece.
			int nextSegment = findNextNonDegenerateSegment(segmentIndex, segmentTypes, 1);
			if (nextSegment != -1) {
				if (sideEdgeInfoMap.containsKey(nextSegment)) {
					connectRootToSideEdge(meshData, rootInfo, sideEdgeInfoMap.get(nextSegment), false, reverseEdgeWinding,
							globalMinX, globalSpanX, globalMinY, globalSpanY, rootSegments, rootXSegments);
				}
			}
		}
	}

	private static void connectRootToRoot(FinMeshData meshData, List<Integer> currentEdgeIndices, List<Integer> prevEdgeIndices, boolean reverseEdgeWinding, float globalMinX, float globalSpanX, float globalMinY, float globalSpanY) {
		if (currentEdgeIndices.size() != prevEdgeIndices.size() || currentEdgeIndices.isEmpty()) {
			return; // Mismatched or empty edges
		}

		int segments = currentEdgeIndices.size() - 1;
		if (segments <= 0) return;

		// Create a "zipper" strip of quads to connect the two root patch edges
		for (int i = 0; i < segments; i++) {
			Vector3f p_curr_i  = meshData.vertices.get(currentEdgeIndices.get(i)).position;
			Vector3f p_curr_i1 = meshData.vertices.get(currentEdgeIndices.get(i + 1)).position;
			Vector3f p_prev_i  = meshData.vertices.get(prevEdgeIndices.get(i)).position;
			Vector3f p_prev_i1 = meshData.vertices.get(prevEdgeIndices.get(i + 1)).position;

			Vector3f v_edge = new Vector3f(p_curr_i1).sub(p_curr_i);
			Vector3f v_link = new Vector3f(p_prev_i).sub(p_curr_i);
			Vector3f normal = v_link.cross(v_edge).normalize();

			// Fallback for co-linear points that can occur at the seam
			if (Float.isNaN(normal.x) || normal.length() < POINT_EPSILON) {
				Vector3f p_curr_normal = meshData.vertices.get(currentEdgeIndices.get(i)).normal;
				Vector3f p_prev_normal = meshData.vertices.get(prevEdgeIndices.get(i)).normal;
				normal = new Vector3f(p_curr_normal).add(p_prev_normal).normalize();
			}

			float u_curr_i = (p_curr_i.x - globalMinX) / globalSpanX;
			float v_curr_i = (p_curr_i.y - globalMinY) / globalSpanY;
			float u_prev_i = (p_prev_i.x - globalMinX) / globalSpanX;
			float v_prev_i = (p_prev_i.y - globalMinY) / globalSpanY;
			float u_prev_i1 = (p_prev_i1.x - globalMinX) / globalSpanX;
			float v_prev_i1 = (p_prev_i1.y - globalMinY) / globalSpanY;
			float u_curr_i1 = (p_curr_i1.x - globalMinX) / globalSpanX;
			float v_curr_i1 = (p_curr_i1.y - globalMinY) / globalSpanY;

			int baseIdx = meshData.vertices.size();
			meshData.vertices.add(new Vertex(p_curr_i, normal, new Vector2f(u_curr_i, v_curr_i),
					RenderingConstants.SURFACE_ID_EDGE));
			meshData.vertices.add(new Vertex(p_prev_i, normal, new Vector2f(u_prev_i, v_prev_i),
					RenderingConstants.SURFACE_ID_EDGE));
			meshData.vertices.add(new Vertex(p_prev_i1, normal, new Vector2f(u_prev_i1, v_prev_i1),
					RenderingConstants.SURFACE_ID_EDGE));
			meshData.vertices.add(new Vertex(p_curr_i1, normal, new Vector2f(u_curr_i1, v_curr_i1),
					RenderingConstants.SURFACE_ID_EDGE));


			// The winding needs to be consistent to properly connect the two patches
			if (reverseEdgeWinding) {
				meshData.addTriangle(baseIdx, baseIdx + 1, baseIdx + 2);
				meshData.addTriangle(baseIdx, baseIdx + 2, baseIdx + 3);
			} else {
				meshData.addTriangle(baseIdx, baseIdx + 2, baseIdx + 1);
				meshData.addTriangle(baseIdx, baseIdx + 3, baseIdx + 2);
			}
		}
	}

	private static void connectRootToSideEdge(FinMeshData meshData, CurvedRootInfo rootInfo, SideEdgeInfo sideEdge,
											  boolean connectToStart, boolean reverseEdgeWinding,
											  float globalMinX, float globalSpanX, float globalMinY, float globalSpanY,
											  final int rootSegments, final int rootXSegments) {
		List<Integer> rootEdgeIndices = connectToStart ? rootInfo.p1EdgeVertexIndices : rootInfo.p2EdgeVertexIndices;

		if (sideEdge.edgeVertices.size() < 4 || rootEdgeIndices.size() <= 1) {
			return;
		}

		int sideFrontIdx, sideBackIdx;
		if (connectToStart) {
			sideFrontIdx = sideEdge.edgeVertices.get(1);
			sideBackIdx = sideEdge.edgeVertices.get(2);
		} else {
			sideFrontIdx = sideEdge.edgeVertices.get(0);
			sideBackIdx = sideEdge.edgeVertices.get(3);
		}
		Vector3f pSideFront = meshData.vertices.get(sideFrontIdx).position;
		Vector3f pSideBack = meshData.vertices.get(sideBackIdx).position;

		for (int i = 0; i < rootSegments; i++) {
			Vector3f pRoot_i = meshData.vertices.get(rootEdgeIndices.get(i)).position;
			Vector3f pRoot_i1 = meshData.vertices.get(rootEdgeIndices.get(i + 1)).position;

			float t0 = (float) i / rootSegments;
			float t1 = (float) (i + 1) / rootSegments;
			Vector3f pSide_i = new Vector3f(pSideBack).lerp(pSideFront, t0);
			Vector3f pSide_i1 = new Vector3f(pSideBack).lerp(pSideFront, t1);

			Vector3f v_root_edge = new Vector3f(pRoot_i1).sub(pRoot_i);
			Vector3f v_link = new Vector3f(pSide_i).sub(pRoot_i);
			Vector3f normal;

			if (connectToStart ^ reverseEdgeWinding) {
				normal = v_link.cross(v_root_edge).normalize();
			} else {
				normal = v_root_edge.cross(v_link).normalize();
			}

			float u_root_i = (pRoot_i.x - globalMinX) / globalSpanX;
			float v_root_i = (pRoot_i.y - globalMinY) / globalSpanY;
			float u_root_i1 = (pRoot_i1.x - globalMinX) / globalSpanX;
			float v_root_i1 = (pRoot_i1.y - globalMinY) / globalSpanY;
			float u_side_i1 = (pSide_i1.x - globalMinX) / globalSpanX;
			float v_side_i1 = (pSide_i1.y - globalMinY) / globalSpanY;
			float u_side_i = (pSide_i.x - globalMinX) / globalSpanX;
			float v_side_i = (pSide_i.y - globalMinY) / globalSpanY;


			int baseIdx = meshData.vertices.size();
			meshData.vertices.add(new Vertex(pRoot_i, normal, new Vector2f(u_root_i, v_root_i),
					RenderingConstants.SURFACE_ID_EDGE));
			meshData.vertices.add(new Vertex(pRoot_i1, normal, new Vector2f(u_root_i1, v_root_i1),
					RenderingConstants.SURFACE_ID_EDGE));
			meshData.vertices.add(new Vertex(pSide_i1, normal, new Vector2f(u_side_i1, v_side_i1),
					RenderingConstants.SURFACE_ID_EDGE));
			meshData.vertices.add(new Vertex(pSide_i, normal, new Vector2f(u_side_i, v_side_i),
					RenderingConstants.SURFACE_ID_EDGE));

			if (connectToStart ^ reverseEdgeWinding) {
				meshData.addTriangle(baseIdx, baseIdx + 2, baseIdx + 1);
				meshData.addTriangle(baseIdx, baseIdx + 3, baseIdx + 2);
			} else {
				meshData.addTriangle(baseIdx, baseIdx + 1, baseIdx + 2);
				meshData.addTriangle(baseIdx, baseIdx + 2, baseIdx + 3);
			}
		}
	}

	private static int findNextNonDegenerateSegment(int currentIndex, SegmentType[] segmentTypes, int direction) {
		final int length = segmentTypes.length;
		int nextIndex = (currentIndex + direction + length) % length;

		while (segmentTypes[nextIndex] == SegmentType.DEGENERATE && nextIndex != currentIndex) {
			nextIndex = (nextIndex + direction + length) % length;
		}

		return (nextIndex == currentIndex) ? -1 : nextIndex;
	}

	private static IntList triangulateWithJTS(CoordinateIF[] polygonPoints) {
		Map<Long, Integer> coordIndexMap = new HashMap<>();
		for (int i = 0; i < polygonPoints.length; i++) {
			coordIndexMap.putIfAbsent(quantizedCoordinateKey(polygonPoints[i].getX(), polygonPoints[i].getY()), i);
		}

		org.locationtech.jts.geom.Coordinate[] jtsCoords = new org.locationtech.jts.geom.Coordinate[polygonPoints.length + 1];
		for (int i = 0; i < polygonPoints.length; i++) {
			jtsCoords[i] = new org.locationtech.jts.geom.Coordinate(polygonPoints[i].getX(), polygonPoints[i].getY());
		}
		jtsCoords[polygonPoints.length] = new org.locationtech.jts.geom.Coordinate(polygonPoints[0].getX(), polygonPoints[0].getY());

		Polygon polygon = geometryFactory.createPolygon(jtsCoords);
		DelaunayTriangulationBuilder builder = new DelaunayTriangulationBuilder();
		builder.setSites(polygon);

		Geometry triangles = builder.getTriangles(geometryFactory);
		IntList indices = new IntList();
		int droppedTriangles = 0;

		for (int i = 0; i < triangles.getNumGeometries(); i++) {
			Polygon tri = (Polygon) triangles.getGeometryN(i);
			if (polygon.contains(tri.getInteriorPoint())) {
				org.locationtech.jts.geom.Coordinate[] triCoords = tri.getCoordinates();
				int[] mappedIndices = new int[3];
				boolean mapped = true;
				for (int j = 0; j < 3; j++) {
					Integer mappedIndex = coordIndexMap.get(quantizedCoordinateKey(triCoords[j].x, triCoords[j].y));
					if (mappedIndex == null) {
						mapped = false;
						break;
					}
					mappedIndices[j] = mappedIndex;
				}
				if (mapped) {
					indices.add(mappedIndices[0]);
					indices.add(mappedIndices[1]);
					indices.add(mappedIndices[2]);
				} else {
					droppedTriangles++;
				}
			}
		}
		if (droppedTriangles > 0) {
			log.warn("Dropped {} fin triangulation triangle(s) because JTS returned unmapped vertices", droppedTriangles);
		}
		return indices;
	}

	private static long quantizedCoordinateKey(double x, double y) {
		long quantizedX = Math.round(x / TRIANGULATION_WELD_EPSILON);
		long quantizedY = Math.round(y / TRIANGULATION_WELD_EPSILON);
		return (quantizedX << 32) ^ (quantizedY & 0xffffffffL);
	}

	private static class FinMeshData {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();

		void addTriangle(int i1, int i2, int i3) {
			indices.addTriangle(i1, i2, i3);
		}
	}
}
