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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinSetGeneratorTest extends BaseTestCase {

	private static final float EPSILON = 1.0e-5f;

	@Test
	void edgeFacesAreExcludedFromDecals() {
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

		Mesh mesh = FinSetGenerator.create(finSet, parent, RenderingConfiguration.builder().build());
		assertNotNull(mesh);
		assertFalse(mesh.getVertices().isEmpty());

		int edgeVertexCount = 0;
		for (Vertex vertex : mesh.getVertices()) {
			if (vertex.surfaceID == RenderingConstants.SURFACE_ID_EDGE) {
				edgeVertexCount++;
				// The edge band runs along the fin perimeter, so its normals lie in the fin plane
				assertEquals(0.0f, vertex.normal.z, EPSILON,
						"Edge-band vertices should have in-plane normals");
			} else if (Math.abs(vertex.normal.z) > 0.99f) {
				// Front/back faces must keep the default surface ID so they still receive decals
				assertEquals(0, vertex.surfaceID, "Fin faces should keep the decal-receiving surface ID");
			}
		}
		assertTrue(edgeVertexCount > 0, "Fin mesh should mark its edge band with the edge surface ID");
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

	/**
	 * The caps sit flush against the fin's edge band, which is kept out of the decal mask.
	 * Tagging them any other way lets a decal paint the fillet ends while the fin edge
	 * touching them stays clean.
	 */
	@Test
	void filletCapsAreExcludedFromDecals() {
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

		Mesh mesh = FinSetGenerator.create(finSet, parent, RenderingConfiguration.builder().build());
		assertNotNull(mesh);
		assertFalse(mesh.getVertices().isEmpty());
		assertFalse(mesh.getIndices().isEmpty());
		assertEquals(0, mesh.getIndices().size() % 3, "Fin mesh should contain complete triangles");
		for (int index : mesh.getIndices()) {
			assertTrue(index >= 0 && index < mesh.getVertices().size(), "Triangle index should refer to a vertex");
		}
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
}
