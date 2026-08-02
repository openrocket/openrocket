package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.motor.Motor;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a 3D mesh for a rocket motor, including a detailed nozzle.
 * The mesh is constructed along the +X axis, centered at the origin.
 *
 * <p>The motor texture is one strip, divided along v between the parts of the motor:
 * the nozzle cone takes 0 to 0.1, the nozzle ring 0.1 to 0.125, the body 0.125 to
 * 0.875, and the front cap 0.875 to 1. The cap and the nozzle are built as
 * concentric rings rather than a triangle fan so that their bands map radially.</p>
 */
public class MotorGenerator {

	/** v where the body ends and the nozzle ring begins, at the back of the motor. */
	private static final float BODY_V_MIN = 0.125f;
	/** v where the body ends and the front cap begins. */
	private static final float BODY_V_MAX = 0.875f;
	/** v at which the nozzle ring starts, the nozzle cone taking everything below. */
	private static final float NOZZLE_RING_V_MIN = 0.1f;

	private static final int FRONT_CAP_RINGS = 5;
	private static final int NOZZLE_RING_DIVISIONS = 3;
	private static final int NOZZLE_CONE_DIVISIONS = 4;

	private static final float NOZZLE_INNER_RADIUS_FRACTION = 0.8f;
	private static final float NOZZLE_DEPTH_FRACTION = 0.05f;

	/**
	 * Creates a mesh for a motor.
	 *
	 * @param motor    The motor data object.
	 * @return A Mesh object representing the motor.
	 */
	public static Mesh create(Motor motor, RenderingConfiguration config) {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();

		float length = (float) motor.getLength();
		float radius = (float) motor.getDiameter() / 2.0f;
		float halfLength = length / 2.0f;

		final int segments = switch (config.getQuality().getQuality()) {
			case LOW -> RenderingConstants.LOW_COMPLEX_SEGMENT_COUNT;
			case MEDIUM -> RenderingConstants.MEDIUM_COMPLEX_SEGMENT_COUNT;
			case HIGH -> RenderingConstants.HIGH_COMPLEX_SEGMENT_COUNT;
		};

		float nozzleInnerRadius = radius * NOZZLE_INNER_RADIUS_FRACTION;
		float nozzleDepth = length * NOZZLE_DEPTH_FRACTION;

		addBody(vertices, indices, halfLength, radius, segments);
		addFrontCap(vertices, indices, halfLength, radius, segments);
		addNozzleRing(vertices, indices, halfLength, radius, nozzleInnerRadius, segments);
		addNozzleCone(vertices, indices, halfLength, nozzleDepth, nozzleInnerRadius, segments);

		return new Mesh(vertices, indices);
	}

	/** The outer tube of the motor. */
	private static void addBody(List<Vertex> vertices, IntList indices,
			float halfLength, float radius, int segments) {
		int bodyStartIndex = vertices.size();
		for (int i = 0; i <= segments; i++) {
			float u = (float) i / segments;
			float theta = u * 2.0f * (float) Math.PI;
			float cosTheta = (float) Math.cos(theta);
			float sinTheta = (float) Math.sin(theta);

			Vector3f normal = new Vector3f(0, cosTheta, sinTheta);
			vertices.add(new Vertex(new Vector3f(-halfLength, radius * cosTheta, radius * sinTheta),
					normal, new Vector2f(u, BODY_V_MIN), 0));
			vertices.add(new Vertex(new Vector3f(halfLength, radius * cosTheta, radius * sinTheta),
					normal, new Vector2f(u, BODY_V_MAX), 0));
		}

		// The two ends alternate in the vertex list, so a segment's quad is four
		// consecutive entries rather than one entry in each of two separate rings.
		for (int i = 0; i < segments; i++) {
			int p1 = bodyStartIndex + i * 2;
			int p2 = p1 + 1;
			int p3 = p1 + 3;
			int p4 = p1 + 2;
			indices.add(p1); indices.add(p4); indices.add(p2);
			indices.add(p2); indices.add(p4); indices.add(p3);
		}
	}

	/** The flat disk closing the front of the motor, facing -X. */
	private static void addFrontCap(List<Vertex> vertices, IntList indices,
			float halfLength, float radius, int segments) {
		int frontCapStartIndex = vertices.size();
		Vector3f frontNormal = new Vector3f(-1, 0, 0);

		for (int r = 0; r <= FRONT_CAP_RINGS; r++) {
			float radialFactor = (float) r / FRONT_CAP_RINGS;
			float currentRadius = radius * radialFactor;
			// v runs from the centre (1.0) out to the edge, where the body starts.
			float v = 1.0f - ((1.0f - BODY_V_MAX) * radialFactor);

			if (r == 0) {
				vertices.add(new Vertex(new Vector3f(-halfLength, 0, 0), frontNormal, new Vector2f(0.5f, v), 0));
			} else {
				addRing(vertices, -halfLength, currentRadius, v, frontNormal, segments);
			}
		}

		fanFromCentreReversed(indices, frontCapStartIndex, segments);
		for (int r = 1; r < FRONT_CAP_RINGS; r++) {
			int ringStart = frontCapStartIndex + 1 + (r - 1) * (segments + 1);
			stitchRingsReversed(indices, ringStart, ringStart + segments + 1, segments);
		}
	}

	/** The flat annulus around the nozzle at the back of the motor, facing +X. */
	private static void addNozzleRing(List<Vertex> vertices, IntList indices,
			float halfLength, float radius, float nozzleInnerRadius, int segments) {
		int nozzleRingStartIndex = vertices.size();
		Vector3f nozzleRingNormal = new Vector3f(1, 0, 0);

		for (int r = 0; r <= NOZZLE_RING_DIVISIONS; r++) {
			float radialFactor = (float) r / NOZZLE_RING_DIVISIONS;
			float currentRadius = nozzleInnerRadius + (radius - nozzleInnerRadius) * radialFactor;
			// v runs from the inner edge out to where the body starts.
			float v = NOZZLE_RING_V_MIN + (BODY_V_MIN - NOZZLE_RING_V_MIN) * radialFactor;

			addRing(vertices, halfLength, currentRadius, v, nozzleRingNormal, segments);
		}

		for (int r = 0; r < NOZZLE_RING_DIVISIONS; r++) {
			int ringStart = nozzleRingStartIndex + r * (segments + 1);
			stitchRings(indices, ringStart, ringStart + segments + 1, segments);
		}
	}

	/** The cone recessed into the back of the motor. */
	private static void addNozzleCone(List<Vertex> vertices, IntList indices,
			float halfLength, float nozzleDepth, float nozzleInnerRadius, int segments) {
		int nozzleConeStartIndex = vertices.size();
		Vector3f apex = new Vector3f(halfLength - nozzleDepth, 0, 0);

		for (int r = 0; r <= NOZZLE_CONE_DIVISIONS; r++) {
			float radialFactor = (float) r / NOZZLE_CONE_DIVISIONS;
			float currentRadius = nozzleInnerRadius * radialFactor;
			float v = NOZZLE_RING_V_MIN * radialFactor;
			// Walk the apex out to the mouth as the radius grows.
			float x = halfLength - nozzleDepth * (1.0f - radialFactor);

			if (r == 0) {
				vertices.add(new Vertex(apex, new Vector3f(1, 0, 0), new Vector2f(0.5f, v), 0));
			} else {
				addConeRing(vertices, apex, x, currentRadius, v, segments);
			}
		}

		fanFromCentre(indices, nozzleConeStartIndex, segments);
		for (int r = 1; r < NOZZLE_CONE_DIVISIONS; r++) {
			int ringStart = nozzleConeStartIndex + 1 + (r - 1) * (segments + 1);
			stitchRings(indices, ringStart, ringStart + segments + 1, segments);
		}
	}

	/**
	 * Adds one ring of {@code segments + 1} vertices around the X axis, all sharing
	 * the same normal and v coordinate. The ring repeats its first position at u = 1
	 * rather than closing on itself, so the texture does not wrap over the seam.
	 */
	private static void addRing(List<Vertex> vertices, float x, float ringRadius, float v,
			Vector3f normal, int segments) {
		for (int i = 0; i <= segments; i++) {
			float u = (float) i / segments;
			float theta = u * 2.0f * (float) Math.PI;
			float cosTheta = (float) Math.cos(theta);
			float sinTheta = (float) Math.sin(theta);

			vertices.add(new Vertex(new Vector3f(x, ringRadius * cosTheta, ringRadius * sinTheta),
					normal, new Vector2f(u, v), 0));
		}
	}

	/**
	 * As {@link #addRing}, but every vertex takes the normal of the cone surface
	 * rather than a shared one: the radial direction tilted towards the apex.
	 */
	private static void addConeRing(List<Vertex> vertices, Vector3f apex, float x,
			float ringRadius, float v, int segments) {
		for (int i = 0; i <= segments; i++) {
			float u = (float) i / segments;
			float theta = u * 2.0f * (float) Math.PI;
			float cosTheta = (float) Math.cos(theta);
			float sinTheta = (float) Math.sin(theta);

			Vector3f p = new Vector3f(x, ringRadius * cosTheta, ringRadius * sinTheta);
			Vector3f toApex = new Vector3f(apex).sub(p);
			Vector3f radialDir = new Vector3f(0, cosTheta, sinTheta);
			Vector3f normal = new Vector3f(radialDir).cross(toApex).cross(radialDir).normalize();

			vertices.add(new Vertex(p, normal, new Vector2f(u, v), 0));
		}
	}

	/** Joins the centre vertex at {@code centreIndex} to the ring that follows it. */
	private static void fanFromCentre(IntList indices, int centreIndex, int segments) {
		for (int i = 0; i < segments; i++) {
			indices.add(centreIndex);
			indices.add(centreIndex + 1 + i);
			indices.add(centreIndex + 1 + i + 1);
		}
	}

	/** As {@link #fanFromCentre}, wound the other way for a face pointing along -X. */
	private static void fanFromCentreReversed(IntList indices, int centreIndex, int segments) {
		for (int i = 0; i < segments; i++) {
			indices.add(centreIndex);
			indices.add(centreIndex + 1 + i + 1);
			indices.add(centreIndex + 1 + i);
		}
	}

	/** Joins two concentric rings into a band of quads, facing +X. */
	private static void stitchRings(IntList indices, int ringStart, int nextRingStart, int segments) {
		for (int i = 0; i < segments; i++) {
			int p1 = ringStart + i;
			int p2 = p1 + 1;
			int p3 = nextRingStart + i + 1;
			int p4 = nextRingStart + i;

			indices.add(p1); indices.add(p4); indices.add(p2);
			indices.add(p2); indices.add(p4); indices.add(p3);
		}
	}

	/** As {@link #stitchRings}, wound the other way for a face pointing along -X. */
	private static void stitchRingsReversed(IntList indices, int ringStart, int nextRingStart, int segments) {
		for (int i = 0; i < segments; i++) {
			int p1 = ringStart + i;
			int p2 = p1 + 1;
			int p3 = nextRingStart + i + 1;
			int p4 = nextRingStart + i;

			indices.add(p1); indices.add(p2); indices.add(p4);
			indices.add(p2); indices.add(p3); indices.add(p4);
		}
	}
}
