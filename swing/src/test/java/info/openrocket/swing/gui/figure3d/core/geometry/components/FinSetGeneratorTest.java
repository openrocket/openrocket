package info.openrocket.swing.gui.figure3d.core.geometry.components;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
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
		float thickness = (float) finSet.getThickness();

		Vector3f p1Front = new Vector3f((float) shapePoints[previousIndex].getX(), (float) shapePoints[previousIndex].getY(), thickness / 2f);
		Vector3f p2Front = new Vector3f((float) shapePoints[trailingIndex].getX(), (float) shapePoints[trailingIndex].getY(), thickness / 2f);
		Vector3f p1Back = new Vector3f((float) shapePoints[previousIndex].getX(), (float) shapePoints[previousIndex].getY(), -thickness / 2f);
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
