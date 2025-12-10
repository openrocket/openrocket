package info.openrocket.swing.gui.figure3d.core.geometry.components;

import info.openrocket.core.motor.Motor;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a 3D mesh for a rocket motor, including a detailed nozzle.
 * The mesh is constructed along the +X axis, centered at the origin.
 */
public class MotorGenerator {

	/**
	 * Creates a mesh for a motor.
	 *
	 * @param motor    The motor data object.
	 * @return A Mesh object representing the motor.
	 */
	public static Mesh create(Motor motor, RenderingConfiguration config) {
		List<Vertex> vertices = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();

		float length = (float) motor.getLength();
		float radius = (float) motor.getDiameter() / 2.0f;
		float halfLength = length / 2.0f;

		final int segments = switch (config.getQuality().getQuality()) {
			case LOW -> RenderingConstants.LOW_COMPLEX_SEGMENT_COUNT;
			case MEDIUM -> RenderingConstants.MEDIUM_COMPLEX_SEGMENT_COUNT;
			case HIGH -> RenderingConstants.HIGH_COMPLEX_SEGMENT_COUNT;
		};

		// --- 1. Main Cylinder Body ---
		// This part generates the outer tube of the motor.
		int bodyStartIndex = 0;
		for (int i = 0; i <= segments; i++) {
			float u = (float) i / segments;
			float theta = u * 2.0f * (float) Math.PI;
			float cosTheta = (float) Math.cos(theta);
			float sinTheta = (float) Math.sin(theta);

			Vector3f normal = new Vector3f(0, cosTheta, sinTheta);
			vertices.add(new Vertex(new Vector3f(-halfLength, radius * cosTheta, radius * sinTheta), normal, new Vector2f(u, 0.125f), 0));
			vertices.add(new Vertex(new Vector3f(halfLength, radius * cosTheta, radius * sinTheta), normal, new Vector2f(u, 0.875f), 0));
		}

		for (int i = 0; i < segments; i++) {
			int p1 = bodyStartIndex + i * 2;
			int p2 = p1 + 1;
			int p3 = p1 + 3;
			int p4 = p1 + 2;
			indices.add(p1); indices.add(p4); indices.add(p2);
			indices.add(p2); indices.add(p4); indices.add(p3);
		}

		// --- 2. Front Cap ---
		// This creates a flat disk at the front of the motor.
		// The texture strip from v=0.875 to v=1.0 is mapped radially
		int frontCapStartIndex = vertices.size();
		Vector3f frontNormal = new Vector3f(-1, 0, 0);

		// Create vertices in radial rings from center to edge
		int radialDivisions = 5; // Number of concentric rings
		for (int r = 0; r <= radialDivisions; r++) {
			float radialFactor = (float) r / radialDivisions;
			float currentRadius = radius * radialFactor;

			// Map v coordinate from center (v=1.0) to edge (v=0.875)
			float v = 1.0f - (0.125f * radialFactor);

			if (r == 0) {
				// Center vertex
				vertices.add(new Vertex(new Vector3f(-halfLength, 0, 0), frontNormal, new Vector2f(0.5f, v), 0));
			} else {
				// Ring vertices
				for (int i = 0; i <= segments; i++) {
					float u = (float) i / segments;
					float theta = u * 2.0f * (float) Math.PI;
					float cosTheta = (float) Math.cos(theta);
					float sinTheta = (float) Math.sin(theta);

					vertices.add(new Vertex(new Vector3f(-halfLength, currentRadius * cosTheta, currentRadius * sinTheta),
							frontNormal, new Vector2f(u, v), 0));
				}
			}
		}

		// Create triangles for front cap
		// Connect center to first ring
		for (int i = 0; i < segments; i++) {
			indices.add(frontCapStartIndex); // center
			indices.add(frontCapStartIndex + 1 + i + 1);
			indices.add(frontCapStartIndex + 1 + i);
		}

		// Connect rings
		for (int r = 1; r < radialDivisions; r++) {
			int ringStart = frontCapStartIndex + 1 + (r - 1) * (segments + 1);
			int nextRingStart = ringStart + segments + 1;

			for (int i = 0; i < segments; i++) {
				int p1 = ringStart + i;
				int p2 = p1 + 1;
				int p3 = nextRingStart + i + 1;
				int p4 = nextRingStart + i;

				indices.add(p1); indices.add(p2); indices.add(p4);
				indices.add(p2); indices.add(p3); indices.add(p4);
			}
		}

		// --- 3. Nozzle ---
		// This part is the most complex, creating the ring and cone at the back.
		float nozzleInnerRadius = radius * 0.8f;
		float nozzleDepth = length * 0.05f;

		// Nozzle Ring (flat part)
		// Maps texture strip from v=0.1 to v=0.125 radially
		int nozzleRingStartIndex = vertices.size();
		Vector3f nozzleRingNormal = new Vector3f(1, 0, 0);

		// Create ring in radial segments
		int ringDivisions = 3; // Number of divisions between inner and outer radius
		for (int r = 0; r <= ringDivisions; r++) {
			float radialFactor = (float) r / ringDivisions;
			float currentRadius = nozzleInnerRadius + (radius - nozzleInnerRadius) * radialFactor;

			// Map v coordinate from inner (v=0.1) to outer (v=0.125)
			float v = 0.1f + 0.025f * radialFactor;

			for (int i = 0; i <= segments; i++) {
				float u = (float) i / segments;
				float theta = u * 2.0f * (float) Math.PI;
				float cosTheta = (float) Math.cos(theta);
				float sinTheta = (float) Math.sin(theta);

				vertices.add(new Vertex(new Vector3f(halfLength, currentRadius * cosTheta, currentRadius * sinTheta),
						nozzleRingNormal, new Vector2f(u, v), 0));
			}
		}

		// Create triangles for nozzle ring
		for (int r = 0; r < ringDivisions; r++) {
			int ringStart = nozzleRingStartIndex + r * (segments + 1);
			int nextRingStart = ringStart + segments + 1;

			for (int i = 0; i < segments; i++) {
				int p1 = ringStart + i;
				int p2 = p1 + 1;
				int p3 = nextRingStart + i + 1;
				int p4 = nextRingStart + i;

				indices.add(p1); indices.add(p4); indices.add(p2);
				indices.add(p2); indices.add(p4); indices.add(p3);
			}
		}

		// Nozzle Cone
		// Maps texture strip from v=0.0 to v=0.1 radially
		int nozzleConeStartIndex = vertices.size();
		Vector3f apex = new Vector3f(halfLength - nozzleDepth, 0, 0);

		// Create cone in radial segments from apex to base
		int coneDivisions = 4;
		for (int r = 0; r <= coneDivisions; r++) {
			float radialFactor = (float) r / coneDivisions;
			float currentRadius = nozzleInnerRadius * radialFactor;

			// Map v coordinate from apex (v=0.0) to base (v=0.1)
			float v = 0.1f * radialFactor;

			// Interpolate X position from apex to base
			float x = halfLength - nozzleDepth * (1.0f - radialFactor);

			if (r == 0) {
				// Apex vertex
				vertices.add(new Vertex(apex, new Vector3f(1, 0, 0), new Vector2f(0.5f, v), 0));
			} else {
				// Ring vertices
				for (int i = 0; i <= segments; i++) {
					float u = (float) i / segments;
					float theta = u * 2.0f * (float) Math.PI;
					float cosTheta = (float) Math.cos(theta);
					float sinTheta = (float) Math.sin(theta);

					Vector3f p = new Vector3f(x, currentRadius * cosTheta, currentRadius * sinTheta);

					// Calculate normal for the cone surface
					Vector3f toApex = new Vector3f(apex).sub(p);
					Vector3f radialDir = new Vector3f(0, cosTheta, sinTheta);
					Vector3f normal = new Vector3f(radialDir).cross(toApex).cross(radialDir).normalize();

					vertices.add(new Vertex(p, normal, new Vector2f(u, v), 0));
				}
			}
		}

		// Create triangles for cone
		// Connect apex to first ring
		for (int i = 0; i < segments; i++) {
			indices.add(nozzleConeStartIndex); // apex
			indices.add(nozzleConeStartIndex + 1 + i);
			indices.add(nozzleConeStartIndex + 1 + i + 1);
		}

		// Connect rings
		for (int r = 1; r < coneDivisions; r++) {
			int ringStart = nozzleConeStartIndex + 1 + (r - 1) * (segments + 1);
			int nextRingStart = ringStart + segments + 1;

			for (int i = 0; i < segments; i++) {
				int p1 = ringStart + i;
				int p2 = p1 + 1;
				int p3 = nextRingStart + i + 1;
				int p4 = nextRingStart + i;

				indices.add(p1); indices.add(p4); indices.add(p2);
				indices.add(p2); indices.add(p4); indices.add(p3);
			}
		}

		return new Mesh(vertices, indices);
	}
}