package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.geometry.basic.SphereGenerator;
import info.openrocket.swing.gui.figure3d.geometry.basic.TubeGenerator;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a 3D mesh for a rail button, composed of various cylindrical sections
 * and an optional hemispherical screw head. The mesh is built along the +Y axis.
 *
 * <p>The button is stacked bottom upwards: a base cylinder at the outer radius, a
 * narrower inner cylinder, then a flange back out at the outer radius, and
 * optionally a screw head on top. Each step up in radius leaves a flat annulus
 * that has to be filled in, since the cylinders themselves are open ended.</p>
 */
public class RailButtonGenerator {

	/**
	 * Creates a combined Mesh object for a rail button based on its dimensions.
	 * The rail button is constructed along the positive Y-axis.
	 *
	 * @param railButton The RailButton data object containing dimensions.
	 * @param config The render configuration.
	 * @return A Mesh representing the complete rail button.
	 */
	public static Mesh create(RailButton railButton, RenderingConfiguration config) {
		List<Vertex> vertices = new ArrayList<>();
		IntList indices = new IntList();

		final int segments = switch (config.getQuality().getQuality()) {
			case LOW -> RenderingConstants.LOW_SEGMENT_COUNT;
			case MEDIUM -> RenderingConstants.MEDIUM_SEGMENT_COUNT;
			case HIGH -> RenderingConstants.HIGH_SEGMENT_COUNT;
		};

		float outerRadius = (float) (railButton.getOuterDiameter() / 2.0);
		float innerRadius = (float) (railButton.getInnerDiameter() / 2.0);
		boolean hasBase = railButton.getBaseHeight() > 0;
		boolean hasInner = railButton.getInnerHeight() > 0;
		boolean hasFlange = railButton.getFlangeHeight() > 0;
		boolean stepped = outerRadius > innerRadius;

		// Y offset of the top of whatever has been stacked so far.
		float currentYOffset = 0.0f;

		if (hasBase) {
			addBottomCap(vertices, indices, outerRadius, segments);
			// Left unfilled because the cap above closes it.
			currentYOffset = addCylinder(vertices, indices, outerRadius,
					(float) railButton.getBaseHeight(), segments, false, currentYOffset);
		}

		if (hasBase && hasInner && stepped) {
			addAnnulus(vertices, indices, outerRadius, innerRadius, currentYOffset, segments, false);
		}

		float innerHeight = (float) railButton.getInnerHeight();
		currentYOffset = addCylinder(vertices, indices, innerRadius, innerHeight, segments, true, currentYOffset);

		if (hasFlange && hasInner && stepped) {
			addAnnulus(vertices, indices, outerRadius, innerRadius, currentYOffset, segments, true);
		}

		if (hasFlange) {
			currentYOffset = addCylinder(vertices, indices, outerRadius,
					(float) railButton.getFlangeHeight(), segments, true, currentYOffset);
		}

		if (railButton.getScrewHeight() > 0) {
			addScrew(vertices, indices, outerRadius, (float) railButton.getScrewHeight(),
					currentYOffset, segments);
		}

		return new Mesh(vertices, indices);
	}

	/** The disk closing the bottom of the base cylinder, facing -Y. */
	private static void addBottomCap(List<Vertex> vertices, IntList indices, float outerRadius, int segments) {
		int ringVertexOffset = vertices.size();
		Vector3f normal = new Vector3f(0, -1, 0);

		vertices.add(new Vertex(new Vector3f(0, 0, 0), normal, new Vector2f(0.5f, 0.5f), 0));
		for (int i = 0; i <= segments; i++) {
			float theta = (float) (2.0 * Math.PI * i / segments);
			float cos = (float) Math.cos(theta);
			float sin = (float) Math.sin(theta);

			Vector3f pOuter = new Vector3f(outerRadius * cos, 0, outerRadius * sin);
			vertices.add(new Vertex(pOuter, normal, new Vector2f(cos * 0.5f + 0.5f, sin * 0.5f + 0.5f), 0));
		}

		for (int i = 0; i < segments; i++) {
			indices.add(ringVertexOffset);
			indices.add(ringVertexOffset + 1 + i);
			indices.add(ringVertexOffset + 1 + (i + 1));
		}
	}

	/**
	 * Stacks one cylinder of the button on top of what is already there.
	 *
	 * @param yOffset the top of the stack so far
	 * @return the new top of the stack
	 */
	private static float addCylinder(List<Vertex> vertices, IntList indices, float radius, float height,
			int segments, boolean isFilled, float yOffset) {
		Mesh mesh = TubeGenerator.create(radius, radius, 0, height, segments, isFilled, false, false);
		addMeshToCombined(vertices, indices, mesh, yOffset + height / 2.0f);
		return yOffset + height;
	}

	/**
	 * Fills the flat ring left between the inner and outer radius where the button
	 * steps in or out, facing +Y.
	 *
	 * @param reversedWinding wind the triangles the other way round
	 */
	private static void addAnnulus(List<Vertex> vertices, IntList indices, float outerRadius, float innerRadius,
			float yOffset, int segments, boolean reversedWinding) {
		int ringVertexOffset = vertices.size();
		Vector3f normal = new Vector3f(0, 1, 0);

		for (int i = 0; i <= segments; i++) {
			float theta = (float) (2.0 * Math.PI * i / segments);
			float cos = (float) Math.cos(theta);
			float sin = (float) Math.sin(theta);

			vertices.add(new Vertex(new Vector3f(outerRadius * cos, yOffset, outerRadius * sin),
					normal, new Vector2f(0, 0), 0));
			vertices.add(new Vertex(new Vector3f(innerRadius * cos, yOffset, innerRadius * sin),
					normal, new Vector2f(0, 0), 0));
		}

		for (int i = 0; i < segments; i++) {
			int v1 = ringVertexOffset + i * 2;
			int v2 = ringVertexOffset + i * 2 + 1;
			int v3 = ringVertexOffset + (i + 1) * 2;
			int v4 = ringVertexOffset + (i + 1) * 2 + 1;

			if (reversedWinding) {
				indices.add(v2); indices.add(v1); indices.add(v3);
				indices.add(v2); indices.add(v3); indices.add(v4);
			} else {
				indices.add(v1); indices.add(v2); indices.add(v3);
				indices.add(v2); indices.add(v4); indices.add(v3);
			}
		}
	}

	/**
	 * The hemispherical screw head on top of the button, plus the disk that closes
	 * its underside.
	 *
	 * <p>The sphere generator builds around the Z axis, so the hemisphere is rotated
	 * onto +Y and then squashed to the requested height, which need not match the
	 * radius.</p>
	 */
	private static void addScrew(List<Vertex> vertices, IntList indices, float outerRadius, float screwHeight,
			float yOffset, int segments) {
		Mesh hemisphereMesh = SphereGenerator.create(outerRadius, segments, segments,
				0, (float) Math.PI / 2.0f, 0, (float) Math.PI * 2.0f);

		int screwVertexOffset = vertices.size();
		for (Vertex v : hemisphereMesh.getVertices()) {
			Vector3f rotatedPos = new Vector3f(v.position.x, v.position.z, -v.position.y);
			Vector3f scaledPos = new Vector3f(rotatedPos.x, (rotatedPos.y / outerRadius) * screwHeight, rotatedPos.z);
			Vector3f finalPos = scaledPos.add(0, yOffset, 0);

			Vector3f rotatedNormal = new Vector3f(v.normal.x, v.normal.z, -v.normal.y);
			vertices.add(new Vertex(finalPos, rotatedNormal, v.texCoords, v.surfaceID));
		}
		indices.addAllOffset(hemisphereMesh.getIndices(), screwVertexOffset);

		int capVertexOffset = vertices.size();
		Vector3f normal = new Vector3f(0, -1, 0);

		vertices.add(new Vertex(new Vector3f(0, yOffset, 0), normal, new Vector2f(0.5f, 0.5f), 0));
		for (int i = 0; i <= segments; i++) {
			float theta = (float) (2.0 * Math.PI * i / segments);
			float cos = (float) Math.cos(theta);
			float sin = (float) Math.sin(theta);

			Vector3f pOuter = new Vector3f(outerRadius * cos, yOffset, outerRadius * sin);
			vertices.add(new Vertex(pOuter, normal, new Vector2f(cos * 0.5f + 0.5f, sin * 0.5f + 0.5f), 0));
		}

		for (int i = 0; i < segments; i++) {
			indices.add(capVertexOffset);
			indices.add(capVertexOffset + 1 + (i + 1));
			indices.add(capVertexOffset + 1 + i);
		}
	}

	/**
	 * Helper method to add a sub-mesh's vertices and indices to the combined lists,
	 * applying a rotation from X-axis to Y-axis and a translation.
	 * @param combinedVertices The list of all vertices.
	 * @param combinedIndices The list of all indices.
	 * @param subMesh The mesh to add (assumed to be generated along the X-axis).
	 * @param yTranslation The Y translation to apply to the sub-mesh's center.
	 */
	private static void addMeshToCombined(List<Vertex> combinedVertices, IntList combinedIndices, Mesh subMesh, float yTranslation) {
		int vertexOffset = combinedVertices.size();
		for (Vertex v : subMesh.getVertices()) {
			// Rotate +90 degrees around Z to align X-generated mesh with Y-axis
			Vector3f rotatedPos = new Vector3f(-v.position.y, v.position.x, v.position.z);
			Vector3f rotatedNormal = new Vector3f(-v.normal.y, v.normal.x, v.normal.z);

			// Translate along the Y-axis
			Vector3f newPos = rotatedPos.add(0, yTranslation, 0);
			combinedVertices.add(new Vertex(newPos, rotatedNormal, v.texCoords, v.surfaceID));
		}
		combinedIndices.addAllOffset(subMesh.getIndices(), vertexOffset);
	}
}
