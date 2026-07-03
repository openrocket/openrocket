package info.openrocket.swing.gui.figure3d.core.geometry.components;

import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.IntList;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.core.geometry.basic.SphereGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.basic.TubeGenerator;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a 3D mesh for a rail button, composed of various cylindrical sections
 * and an optional hemispherical screw head. The mesh is built along the +Y axis.
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
		List<Vertex> combinedVertices = new ArrayList<>();
		IntList combinedIndices = new IntList();

		final int segments = switch (config.getQuality().getQuality()) {
			case LOW -> RenderingConstants.LOW_SEGMENT_COUNT;
			case MEDIUM -> RenderingConstants.MEDIUM_SEGMENT_COUNT;
			case HIGH -> RenderingConstants.HIGH_SEGMENT_COUNT;
		};

		float outerRadius = (float) (railButton.getOuterDiameter() / 2.0);
		float innerRadius = (float) (railButton.getInnerDiameter() / 2.0);

		// Keep track of the current Y-offset (along the longitudinal axis of the rail button)
		float currentYOffset = 0.0f;

		// --- Add bottom cap for the base cylinder ---
		if (railButton.getBaseHeight() > 0) {
			int ringVertexOffset = 0;
			Vector3f normal = new Vector3f(0, -1, 0); // Normal pointing down along Y

			// Center vertex
			combinedVertices.add(new Vertex(new Vector3f(0, 0, 0), normal, new Vector2f(0.5f, 0.5f), 0));

			for (int i = 0; i <= segments; i++) {
				float theta = (float) (2.0 * Math.PI * i / segments);
				float cos = (float) Math.cos(theta);
				float sin = (float) Math.sin(theta);

				Vector3f pOuter = new Vector3f(outerRadius * cos, 0, outerRadius * sin);
				combinedVertices.add(new Vertex(pOuter, normal, new Vector2f(cos * 0.5f + 0.5f, sin * 0.5f + 0.5f), 0));
			}

			for (int i = 0; i < segments; i++) {
				int v1 = ringVertexOffset; // center
				int v2 = ringVertexOffset + 1 + i;
				int v3 = ringVertexOffset + 1 + (i + 1);

				combinedIndices.add(v1);
				combinedIndices.add(v2);
				combinedIndices.add(v3);
			}
		}

		// --- 1. Base Cylinder ---
		if (railButton.getBaseHeight() > 0) {
			float baseHeight = (float) railButton.getBaseHeight();
			Mesh baseMesh = TubeGenerator.create(outerRadius, outerRadius, 0, baseHeight, segments, false, false, false);
			addMeshToCombined(combinedVertices, combinedIndices, baseMesh, currentYOffset + baseHeight / 2.0f);
			currentYOffset += baseHeight;
		}

		// --- Add connecting ring from base to inner ---
		if (railButton.getBaseHeight() > 0 && railButton.getInnerHeight() > 0 && outerRadius > innerRadius) {
			int ringVertexOffset = combinedVertices.size();
			Vector3f normal = new Vector3f(0, 1, 0); // Normal pointing up along Y

			for (int i = 0; i <= segments; i++) {
				float theta = (float) (2.0 * Math.PI * i / segments);
				float cos = (float) Math.cos(theta);
				float sin = (float) Math.sin(theta);

				Vector3f pOuter = new Vector3f(outerRadius * cos, currentYOffset, outerRadius * sin);
				combinedVertices.add(new Vertex(pOuter, normal, new Vector2f(0,0), 0));

				Vector3f pInner = new Vector3f(innerRadius * cos, currentYOffset, innerRadius * sin);
				combinedVertices.add(new Vertex(pInner, normal, new Vector2f(0,0), 0));
			}

			for (int i = 0; i < segments; i++) {
				int v1 = ringVertexOffset + i * 2;
				int v2 = ringVertexOffset + i * 2 + 1;
				int v3 = ringVertexOffset + (i + 1) * 2;
				int v4 = ringVertexOffset + (i + 1) * 2 + 1;

				// CCW winding for +Y normal
				combinedIndices.add(v1);
				combinedIndices.add(v2);
				combinedIndices.add(v3);

				combinedIndices.add(v2);
				combinedIndices.add(v4);
				combinedIndices.add(v3);
			}
		}

		// --- 2. Inner Cylinder ---
		float innerHeight = (float) railButton.getInnerHeight();
		Mesh innerMesh = TubeGenerator.create(innerRadius, innerRadius, 0, innerHeight, segments, true, false, false);
		addMeshToCombined(combinedVertices, combinedIndices, innerMesh, currentYOffset + innerHeight / 2.0f);
		currentYOffset += innerHeight;

		// --- Add connecting ring from inner to flange ---
		if (railButton.getFlangeHeight() > 0 && railButton.getInnerHeight() > 0 && outerRadius > innerRadius) {
			int ringVertexOffset = combinedVertices.size();
			Vector3f normal = new Vector3f(0, 1, 0); // Normal pointing up along Y

			for (int i = 0; i <= segments; i++) {
				float theta = (float) (2.0 * Math.PI * i / segments);
				float cos = (float) Math.cos(theta);
				float sin = (float) Math.sin(theta);

				Vector3f pOuter = new Vector3f(outerRadius * cos, currentYOffset, outerRadius * sin);
				combinedVertices.add(new Vertex(pOuter, normal, new Vector2f(0,0), 0));

				Vector3f pInner = new Vector3f(innerRadius * cos, currentYOffset, innerRadius * sin);
				combinedVertices.add(new Vertex(pInner, normal, new Vector2f(0,0), 0));
			}

			for (int i = 0; i < segments; i++) {
				int v1 = ringVertexOffset + i * 2;
				int v2 = ringVertexOffset + i * 2 + 1;
				int v3 = ringVertexOffset + (i + 1) * 2;
				int v4 = ringVertexOffset + (i + 1) * 2 + 1;

				// CCW winding for +Y normal
				combinedIndices.add(v2);
				combinedIndices.add(v1);
				combinedIndices.add(v3);

				combinedIndices.add(v2);
				combinedIndices.add(v3);
				combinedIndices.add(v4);
			}
		}

		// --- 3. Flange Cylinder ---
		if (railButton.getFlangeHeight() > 0) {
			float flangeHeight = (float) railButton.getFlangeHeight();
			Mesh flangeMesh = TubeGenerator.create(outerRadius, outerRadius, 0, flangeHeight, segments, true, false, false);
			addMeshToCombined(combinedVertices, combinedIndices, flangeMesh, currentYOffset + flangeHeight / 2.0f);
			currentYOffset += flangeHeight;
		}

		// --- 4. Screw (Hemisphere) ---
		if (railButton.getScrewHeight() > 0) {
			float screwHeight = (float) railButton.getScrewHeight();
			Mesh hemisphereMesh = SphereGenerator.create(outerRadius, segments, segments,
					0, (float)Math.PI / 2.0f, 0, (float)Math.PI * 2.0f);

			int screwVertexOffset = combinedVertices.size();
			for (Vertex v : hemisphereMesh.getVertices()) {
				//Rotate sphere from Z-axis to +Y-axis (-90 deg around X) and scale
				Vector3f rotatedPos = new Vector3f(v.position.x, v.position.z, -v.position.y);
				Vector3f scaledPos = new Vector3f(rotatedPos.x, (rotatedPos.y / outerRadius) * screwHeight, rotatedPos.z);
				// Translate to final position
				Vector3f finalPos = scaledPos.add(0, currentYOffset, 0);

				// Rotate the normal accordingly
				Vector3f rotatedNormal = new Vector3f(v.normal.x, v.normal.z, -v.normal.y);
				combinedVertices.add(new Vertex(finalPos, rotatedNormal, v.texCoords, v.surfaceID));
			}
			combinedIndices.addAllOffset(hemisphereMesh.getIndices(), screwVertexOffset);

			// Create a closing disk for the base of the hemisphere
			int capVertexOffset = combinedVertices.size();
			Vector3f normal = new Vector3f(0, -1, 0); // Normal pointing down towards the button body

			// Center vertex
			combinedVertices.add(new Vertex(new Vector3f(0, currentYOffset, 0), normal, new Vector2f(0.5f, 0.5f), 0));

			for (int i = 0; i <= segments; i++) {
				float theta = (float) (2.0 * Math.PI * i / segments);
				float cos = (float) Math.cos(theta);
				float sin = (float) Math.sin(theta);

				Vector3f pOuter = new Vector3f(outerRadius * cos, currentYOffset, outerRadius * sin);
				combinedVertices.add(new Vertex(pOuter, normal, new Vector2f(cos * 0.5f + 0.5f, sin * 0.5f + 0.5f), 0));
			}

			for (int i = 0; i < segments; i++) {
				int v1 = capVertexOffset; // center
				int v2 = capVertexOffset + 1 + i;
				int v3 = capVertexOffset + 1 + (i + 1);

				combinedIndices.add(v1);
				combinedIndices.add(v3);
				combinedIndices.add(v2);
			}
		}



		return new Mesh(combinedVertices, combinedIndices);
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
