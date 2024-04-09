package info.openrocket.core.file.wavefrontobj;

public class DefaultObjEdge {
	/**
	 * The vertex index of the start of this edge
	 */
	private final int startVertexIndex;
	/**
	 * The vertex index of the end of this edge
	 */
	private final int endVertexIndex;

	/**
	 * The normal index of the start of this edge
	 */
	private final int startNormalIndex;
	/**
	 * The normal index of the end of this edge
	 */
	private final int endNormalIndex;

	/**
	 * The texture coordinate index of the start of this edge
	 */
	private final int startTexCoordIndex;
	/**
	 * The texture coordinate index of the end of this edge
	 */
	private final int endTexCoordIndex;


	public DefaultObjEdge(int startVertexIndex, int endVertexIndex, int startTexCoordIndex, int endTexCoordIndex, int startNormalIndex, int endNormalIndex) {
		this.startVertexIndex = startVertexIndex;
		this.endVertexIndex = endVertexIndex;
		this.startTexCoordIndex = startTexCoordIndex;
		this.endTexCoordIndex = endTexCoordIndex;
		this.startNormalIndex = startNormalIndex;
		this.endNormalIndex = endNormalIndex;
	}

	public static DefaultObjEdge[] createEdges(DefaultObjFace faces) {
		DefaultObjEdge[] edges = new DefaultObjEdge[faces.getNumVertices()-1];
		for (int i = 0; i < faces.getNumVertices()-1; i++) {
			int startVertexIndex = faces.getVertexIndex(i);
			int endVertexIndex = faces.getVertexIndex((i + 1));

			int startNormalIndex = -1;
			int endNormalIndex = -1;
			if (faces.containsNormalIndices()) {
				startNormalIndex = faces.getNormalIndex(i);
				endNormalIndex = faces.getNormalIndex((i + 1));
			}

			int startTexCoordIndex = -1;
			int endTexCoordIndex = -1;
			if (faces.containsTexCoordIndices()) {
				startTexCoordIndex = faces.getTexCoordIndex(i);
				endTexCoordIndex = faces.getTexCoordIndex((i + 1));
			}

			edges[i] = new DefaultObjEdge(startVertexIndex, endVertexIndex, startTexCoordIndex, endTexCoordIndex, startNormalIndex, endNormalIndex);
		}
		return edges;
	}
}
