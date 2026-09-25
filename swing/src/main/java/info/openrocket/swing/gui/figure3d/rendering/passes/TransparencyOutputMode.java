package info.openrocket.swing.gui.figure3d.rendering.passes;

/** Fragment-shader output selected for a geometry draw. */
enum TransparencyOutputMode {
	SCENE_COLOR(0),
	ACCUMULATION(1),
	REVEALAGE(2),
	OPAQUE_TEXTURE_FRAGMENTS(3);

	private final int shaderValue;

	TransparencyOutputMode(int shaderValue) {
		this.shaderValue = shaderValue;
	}

	int getShaderValue() {
		return shaderValue;
	}
}
