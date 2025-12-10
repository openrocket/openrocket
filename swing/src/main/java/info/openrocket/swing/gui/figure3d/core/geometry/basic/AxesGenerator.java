package info.openrocket.swing.gui.figure3d.core.geometry.basic;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates geometry for a 3D arrow, composed of a shaft and a cone head.
 */
public class AxesGenerator {

	/**
	 * Creates a single mesh for an arrow pointing along the positive X-axis.
	 *
	 * @param shaftLength The length of the arrow's shaft.
	 * @param shaftRadius The radius of the arrow's shaft.
	 * @param headLength  The length of the conical arrowhead.
	 * @param headRadius  The radius of the base of the arrowhead.
	 * @return A combined Mesh object for the arrow.
	 */
	public static Mesh createArrowMesh(float shaftLength, float shaftRadius, float headLength, float headRadius) {
		// Generate the shaft (a cylinder)
		Mesh shaftMesh = TubeGenerator.create(shaftRadius, shaftRadius, 0.0f, shaftLength, RenderingConstants.MEDIUM_SEGMENT_COUNT, true);

		// Generate the head (a cone)
		Mesh headMesh = TubeGenerator.create(headRadius, 0.0f, 0.0f, headLength, RenderingConstants.MEDIUM_SEGMENT_COUNT, true);

		// --- Combine the two meshes into one ---

		// Add shaft vertices and indices
		List<Vertex> combinedVertices = new ArrayList<>(shaftMesh.getVertices());
		List<Integer> combinedIndices = new ArrayList<>(shaftMesh.getIndices());

		// Offset for the head's vertices
		int headVertexOffset = combinedVertices.size();

		// Add head vertices, shifting their position to the end of the shaft
		for (Vertex v : headMesh.getVertices()) {
			v.position.x += shaftLength / 2 + headLength / 2;
			combinedVertices.add(v);
		}

		// Add head indices, applying the offset
		for (Integer index : headMesh.getIndices()) {
			combinedIndices.add(index + headVertexOffset);
		}

		return new Mesh(combinedVertices, combinedIndices);
	}
}
