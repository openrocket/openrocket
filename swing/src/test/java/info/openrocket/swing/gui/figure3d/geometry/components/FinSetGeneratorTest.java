package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinSetGeneratorTest extends BaseTestCase {

	private static final float EPSILON = 1.0e-5f;

	@Test
	void finFacesAndEdgesUseDistinctSurfaceIds() {
		BodyTube parent = new BodyTube();
		parent.setOuterRadius(0.05);
		parent.setLength(0.5);

		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setRootChord(0.20);
		finSet.setTipChord(0.10);
		finSet.setSweep(0.04);
		finSet.setHeight(0.12);
		finSet.setThickness(0.004);
		parent.addChild(finSet);

		Mesh mesh = FinSetGenerator.create(finSet, parent, new RenderingConfiguration());
		assertNotNull(mesh);
		assertFalse(mesh.getVertices().isEmpty());

		int edgeVertexCount = 0;
		int leftFaceVertexCount = 0;
		int rightFaceVertexCount = 0;
		for (Vertex vertex : mesh.getVertices()) {
			if (vertex.surfaceID == RenderingConstants.SURFACE_ID_EDGE) {
				edgeVertexCount++;
			} else if (vertex.normal.z < -0.99f) {
				assertEquals(RenderingConstants.SURFACE_ID_OUTSIDE, vertex.surfaceID,
						"Fin left faces should use the primary surface ID");
				leftFaceVertexCount++;
			} else if (vertex.normal.z > 0.99f) {
				assertEquals(RenderingConstants.SURFACE_ID_RIGHT, vertex.surfaceID,
						"Fin right faces should use the secondary surface ID");
				rightFaceVertexCount++;
			}
		}
		assertTrue(edgeVertexCount > 0, "Fin mesh should mark its edge band with the edge surface ID");
		assertTrue(leftFaceVertexCount > 0, "Fin mesh should contain left-face vertices");
		assertTrue(rightFaceVertexCount > 0, "Fin mesh should contain right-face vertices");
	}

	@Test
	void filletTrailingCapUsesAdjacentEdgeAppearanceBasis() {
		BodyTube parent = new BodyTube();
		parent.setOuterRadius(0.05);
		parent.setLength(0.5);

		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setRootChord(0.20);
		finSet.setTipChord(0.10);
		finSet.setSweep(0.04);
		finSet.setHeight(0.12);
		finSet.setThickness(0.004);
		finSet.setFilletRadius(0.006);
		parent.addChild(finSet);

		CoordinateIF[] shapePoints = finSet.generateContinuousFinAndTabShape();
		float minX = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY;
		float minY = Float.POSITIVE_INFINITY;
		float maxY = Float.NEGATIVE_INFINITY;
		for (CoordinateIF point : shapePoints) {
			minX = Math.min(minX, (float) point.getX());
			maxX = Math.max(maxX, (float) point.getX());
			minY = Math.min(minY, (float) point.getY());
			maxY = Math.max(maxY, (float) point.getY());
		}
		float spanX = maxX - minX;
		float spanY = maxY - minY;

		int arcSegments = 6;
		int xSegments = 4;
		Mesh filletMesh = FinSetGenerator.createFilletMesh(finSet, parent, arcSegments, xSegments, minX, spanX, minY, spanY);
		assertNotNull(filletMesh);
		assertFalse(filletMesh.getVertices().isEmpty());

		int stripVertexCount = (xSegments + 1) * 2 * (arcSegments + 1);
		int capVertexCount = 4 * (arcSegments + 1);
		List<Vertex> trailingCapVertices = filletMesh.getVertices()
				.subList(stripVertexCount + capVertexCount, stripVertexCount + 2 * capVertexCount);

		CoordinateIF trailingRootPoint = finSet.getRootPoints()[finSet.getRootPoints().length - 1];
		float expectedU = expectedTrailingEdgeU(shapePoints, trailingRootPoint, spanX);
		Vector3f expectedNormal = expectedTrailingEdgeNormal(finSet, shapePoints, trailingRootPoint);

		assertTrue(expectedU > 1.0f, "Trailing edge unwrap should extend past the face-space U range");
		for (Vertex vertex : trailingCapVertices) {
			assertEquals(expectedU, vertex.texCoords.x, EPSILON, "Trailing cap should share the edge surface U basis");
			assertEquals(expectedNormal.x, vertex.normal.x, EPSILON, "Trailing cap should inherit the edge normal");
			assertEquals(expectedNormal.y, vertex.normal.y, EPSILON, "Trailing cap should inherit the edge normal");
			assertEquals(expectedNormal.z, vertex.normal.z, EPSILON, "Trailing cap should inherit the edge normal");
		}
	}

	/**
	 * The leading cap closes the fillet against the fin's leading edge band, so it has to
	 * shade and unwrap like that band rather than like a flat -X wall, or it reads as a seam
	 * against the edge face it touches.
	 */
	@Test
	void filletLeadingCapUsesAdjacentEdgeAppearanceBasis() {
		BodyTube parent = new BodyTube();
		parent.setOuterRadius(0.05);
		parent.setLength(0.5);

		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setRootChord(0.20);
		finSet.setTipChord(0.10);
		finSet.setSweep(0.04);
		finSet.setHeight(0.12);
		finSet.setThickness(0.004);
		finSet.setFilletRadius(0.006);
		parent.addChild(finSet);

		CoordinateIF[] shapePoints = finSet.generateContinuousFinAndTabShape();
		int arcSegments = 6;
		int xSegments = 4;
		Mesh filletMesh = FinSetGenerator.createFilletMesh(finSet, parent, arcSegments, xSegments, 0, 1, 0, 1);
		assertNotNull(filletMesh);

		int stripVertexCount = (xSegments + 1) * 2 * (arcSegments + 1);
		int capVertexCount = 4 * (arcSegments + 1);
		List<Vertex> leadingCapVertices = filletMesh.getVertices()
				.subList(stripVertexCount, stripVertexCount + capVertexCount);

		// The band's normal comes from the edge segment leaving the root leading corner.
		CoordinateIF leadingRootPoint = finSet.getRootPoints()[0];
		int leadingIndex = findMatchingPointIndex(shapePoints, leadingRootPoint);
		Vector3f expectedNormal = edgeSegmentNormal(finSet, shapePoints,
				leadingIndex, (leadingIndex + 1) % shapePoints.length);

		assertTrue(expectedNormal.x < 0.0f, "Leading edge normal should face forwards");
		assertTrue(Math.abs(expectedNormal.y) > EPSILON,
				"A swept leading edge should tilt the normal away from a plain -X wall");

		for (Vertex vertex : leadingCapVertices) {
			assertEquals(expectedNormal.x, vertex.normal.x, EPSILON, "Leading cap should inherit the edge normal");
			assertEquals(expectedNormal.y, vertex.normal.y, EPSILON, "Leading cap should inherit the edge normal");
			assertEquals(expectedNormal.z, vertex.normal.z, EPSILON, "Leading cap should inherit the edge normal");
		}
	}

	/** The caps sit flush against the fin's edge band and share its surface ID. */
	@Test
	void filletCapsUseEdgeSurfaceId() {
		BodyTube parent = new BodyTube();
		parent.setOuterRadius(0.05);
		parent.setLength(0.5);

		TrapezoidFinSet finSet = new TrapezoidFinSet();
		finSet.setRootChord(0.20);
		finSet.setTipChord(0.10);
		finSet.setSweep(0.04);
		finSet.setHeight(0.12);
		finSet.setThickness(0.004);
		finSet.setFilletRadius(0.006);
		parent.addChild(finSet);

		int arcSegments = 6;
		int xSegments = 4;
		Mesh filletMesh = FinSetGenerator.createFilletMesh(finSet, parent, arcSegments, xSegments, 0, 1, 0, 1);
		assertNotNull(filletMesh);

		int stripVertexCount = (xSegments + 1) * 2 * (arcSegments + 1);
		List<Vertex> capVertices = filletMesh.getVertices()
				.subList(stripVertexCount, filletMesh.getVertices().size());
		assertEquals(2 * 4 * (arcSegments + 1), capVertices.size(), "Both fillet caps should be present");

		for (Vertex vertex : capVertices) {
			assertEquals(RenderingConstants.SURFACE_ID_EDGE, vertex.surfaceID,
					"Fillet caps should carry the fin edge band's surface ID");
		}
	}

	/**
	 * The top of the fillet lies against the fin a little way up the span, where sweep has
	 * already moved the leading and trailing edges, so it has to end where the fin ends at
	 * that height rather than where the root chord ends. Backward and forward sweep are both
	 * checked: a forward-swept edge needs the fillet to reach further than the root chord,
	 * which the earlier clamp-only placement could not do at all.
	 */
	@Test
	void filletTopEndsAtTheFinEdgeAtItsOwnHeight() {
		for (double sweep : new double[]{0.04, 0.0, -0.04}) {
			double rootChord = 0.20;
			double tipChord = 0.10;
			double height = 0.12;

			BodyTube parent = new BodyTube();
			parent.setOuterRadius(0.05);
			parent.setLength(0.5);

			TrapezoidFinSet finSet = new TrapezoidFinSet();
			finSet.setRootChord(rootChord);
			finSet.setTipChord(tipChord);
			finSet.setSweep(sweep);
			finSet.setHeight(height);
			finSet.setThickness(0.004);
			finSet.setFilletRadius(0.006);
			parent.addChild(finSet);

			int arcSegments = 6;
			int xSegments = 4;
			Mesh fillet = FinSetGenerator.createFilletMesh(finSet, parent, arcSegments, xSegments, 0, 1, 0, 1);
			assertNotNull(fillet);

			// Ring 0 is the top of the fillet, where it meets the fin; ring arcSegments is its
			// base on the body. Each (i, ring) pair emits a right vertex then a left vertex.
			float topY = ringVertex(fillet, arcSegments, 0, 0).y;
			float[] topSpan = ringXSpan(fillet, arcSegments, xSegments, 0);
			float[] baseSpan = ringXSpan(fillet, arcSegments, xSegments, arcSegments);

			// Straight leading and trailing edges running from the root corners to the tip.
			double expectedLeading = sweep / height * topY;
			double expectedTrailing = rootChord + (sweep + tipChord - rootChord) / height * topY;

			assertEquals(expectedLeading, topSpan[0], EPSILON,
					"Fillet top should start at the fin's leading edge at its own height (sweep " + sweep + ")");
			assertEquals(expectedTrailing, topSpan[1], EPSILON,
					"Fillet top should end at the fin's trailing edge at its own height (sweep " + sweep + ")");

			CoordinateIF[] rootPoints = finSet.getRootPoints();
			assertEquals(rootPoints[0].getX(), baseSpan[0], EPSILON,
					"Fillet base should still start at the root chord (sweep " + sweep + ")");
			assertEquals(rootPoints[rootPoints.length - 1].getX(), baseSpan[1], EPSILON,
					"Fillet base should still end at the root chord (sweep " + sweep + ")");
		}
	}

	private static Vector3f ringVertex(Mesh fillet, int arcSegments, int xIndex, int ring) {
		return fillet.getVertices().get((xIndex * (arcSegments + 1) + ring) * 2).position;
	}

	private static float[] ringXSpan(Mesh fillet, int arcSegments, int xSegments, int ring) {
		float minX = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY;
		for (int i = 0; i <= xSegments; i++) {
			float x = ringVertex(fillet, arcSegments, i, ring).x;
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
		}
		return new float[]{minX, maxX};
	}

	@Test
	void triangulatesFinWithCoincidentPlanformPoints() {
		BodyTube parent = new BodyTube();
		parent.setOuterRadius(0.05);
		parent.setLength(0.5);

		FreeformFinSet finSet = new FreeformFinSet();
		parent.addChild(finSet);
		finSet.setThickness(0.004);
		finSet.setPoints(new CoordinateIF[] {
				Coordinate.ZERO,
				new Coordinate(0.06, 0.12),
				new Coordinate(0.0600003, 0.1200002),
				new Coordinate(0.16, 0.05),
				new Coordinate(0.22, 0.0)
		}, false);

		Mesh mesh = FinSetGenerator.create(finSet, parent, new RenderingConfiguration());
		assertNotNull(mesh);
		assertFalse(mesh.getVertices().isEmpty());
		assertFalse(mesh.getIndices().isEmpty());
		assertEquals(0, mesh.getIndices().size() % 3, "Fin mesh should contain complete triangles");
		for (int index : mesh.getIndices()) {
			assertTrue(index >= 0 && index < mesh.getVertices().size(), "Triangle index should refer to a vertex");
		}
	}

	/** Concave freeform fins must retain their indentation instead of filling the convex hull. */
	@Test
	void concaveFinFaceMatchesPlanformArea() {
		BodyTube parent = new BodyTube();
		parent.setOuterRadius(0.025);
		parent.setLength(0.2);

		FreeformFinSet finSet = new FreeformFinSet();
		parent.addChild(finSet);
		finSet.setThickness(0.001);
		finSet.setPoints(new CoordinateIF[] {
				new Coordinate(0.0, 0.0),
				new Coordinate(0.0, 0.0067818),
				new Coordinate(0.092075, 0.0067818),
				new Coordinate(0.098425, 0.02032),
				new Coordinate(0.111125, 0.02032),
				new Coordinate(0.111125, 0.0)
		}, false);

		CoordinateIF[] planform = finSet.generateContinuousFinAndTabShape();
		Mesh mesh = FinSetGenerator.create(finSet, parent, new RenderingConfiguration());
		GeometryFactory geometryFactory = new GeometryFactory();
		Polygon planformPolygon = createPolygon(geometryFactory, planform);
		Geometry planformWithFloatTolerance = planformPolygon.buffer(1.0e-7);

		double faceArea = 0.0;
		for (int i = 0; i < mesh.getIndices().size(); i += 3) {
			int first = mesh.getIndices().get(i);
			int second = mesh.getIndices().get(i + 1);
			int third = mesh.getIndices().get(i + 2);
			if (first < planform.length && second < planform.length && third < planform.length) {
				Vertex firstVertex = mesh.getVertices().get(first);
				Vertex secondVertex = mesh.getVertices().get(second);
				Vertex thirdVertex = mesh.getVertices().get(third);
				faceArea += triangleArea(firstVertex, secondVertex, thirdVertex);
				assertTrue(signedTwiceTriangleArea(firstVertex, secondVertex, thirdVertex) > 0.0,
						"Front-face triangles must use counter-clockwise winding");
				assertTrue(planformWithFloatTolerance.covers(createPolygon(geometryFactory,
						new CoordinateIF[] {
								new Coordinate(firstVertex.position.x, firstVertex.position.y),
								new Coordinate(secondVertex.position.x, secondVertex.position.y),
								new Coordinate(thirdVertex.position.x, thirdVertex.position.y)
						})), "Every front-face triangle must remain inside the concave planform");
			}
		}

		assertEquals(polygonArea(planform), faceArea, 1.0e-8,
				"Front-face triangles must cover only the concave fin planform");
	}

	private static float expectedTrailingEdgeU(CoordinateIF[] shapePoints, CoordinateIF trailingRootPoint, float spanX) {
		int trailingIndex = findMatchingPointIndex(shapePoints, trailingRootPoint);
		float accumulatedLength = 0f;
		for (int i = 0; i < shapePoints.length; i++) {
			int end = (i + 1) % shapePoints.length;
			float segmentLength = distance(shapePoints[i], shapePoints[end]);
			if (end == trailingIndex) {
				return (accumulatedLength + segmentLength) / spanX;
			}
			accumulatedLength += segmentLength;
		}
		throw new AssertionError("Trailing root point was not found on the perimeter");
	}

	private static Vector3f expectedTrailingEdgeNormal(FinSet finSet, CoordinateIF[] shapePoints, CoordinateIF trailingRootPoint) {
		int trailingIndex = findMatchingPointIndex(shapePoints, trailingRootPoint);
		int previousIndex = (trailingIndex - 1 + shapePoints.length) % shapePoints.length;
		return edgeSegmentNormal(finSet, shapePoints, previousIndex, trailingIndex);
	}

	/** Outward normal of the fin edge band spanning one perimeter segment. */
	private static Vector3f edgeSegmentNormal(FinSet finSet, CoordinateIF[] shapePoints, int fromIndex, int toIndex) {
		float thickness = (float) finSet.getThickness();
		Vector3f p1Front = new Vector3f((float) shapePoints[fromIndex].getX(), (float) shapePoints[fromIndex].getY(), thickness / 2f);
		Vector3f p2Front = new Vector3f((float) shapePoints[toIndex].getX(), (float) shapePoints[toIndex].getY(), thickness / 2f);
		Vector3f p1Back = new Vector3f((float) shapePoints[fromIndex].getX(), (float) shapePoints[fromIndex].getY(), -thickness / 2f);
		return new Vector3f(p2Front).sub(p1Front)
				.cross(new Vector3f(p1Back).sub(p1Front))
				.normalize();
	}

	private static int findMatchingPointIndex(CoordinateIF[] points, CoordinateIF target) {
		for (int i = 0; i < points.length; i++) {
			if (Math.abs(points[i].getX() - target.getX()) < EPSILON &&
					Math.abs(points[i].getY() - target.getY()) < EPSILON) {
				return i;
			}
		}
		throw new AssertionError("Point not present in perimeter");
	}

	private static float distance(CoordinateIF a, CoordinateIF b) {
		float dx = (float) (b.getX() - a.getX());
		float dy = (float) (b.getY() - a.getY());
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	private static double polygonArea(CoordinateIF[] points) {
		double twiceArea = 0.0;
		for (int i = 0; i < points.length; i++) {
			CoordinateIF current = points[i];
			CoordinateIF next = points[(i + 1) % points.length];
			twiceArea += current.getX() * next.getY() - next.getX() * current.getY();
		}
		return Math.abs(twiceArea) * 0.5;
	}

	private static Polygon createPolygon(GeometryFactory geometryFactory, CoordinateIF[] points) {
		org.locationtech.jts.geom.Coordinate[] coordinates =
				new org.locationtech.jts.geom.Coordinate[points.length + 1];
		for (int i = 0; i < points.length; i++) {
			coordinates[i] = new org.locationtech.jts.geom.Coordinate(points[i].getX(), points[i].getY());
		}
		coordinates[points.length] = coordinates[0].copy();
		return geometryFactory.createPolygon(coordinates);
	}

	private static double triangleArea(Vertex first, Vertex second, Vertex third) {
		return Math.abs(signedTwiceTriangleArea(first, second, third)) * 0.5;
	}

	private static double signedTwiceTriangleArea(Vertex first, Vertex second, Vertex third) {
		return (second.position.x - first.position.x) * (third.position.y - first.position.y)
				- (third.position.x - first.position.x) * (second.position.y - first.position.y);
	}
}
