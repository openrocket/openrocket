package info.openrocket.swing.gui.figure3d.rendering;

/**
 * Required uniform locations for the main scene shader.
 */
public final class MainShaderUniforms {

	private static final int MAX_LIGHTS = 10;

	public final int projection;
	public final int view;
	public final int model;
	public final int normalMatrix;
	public final int viewPos;
	public final int lightSpaceMatrix;
	public final int numLights;
	public final int fogColor;
	public final int fogEnabled;
	public final int fogDensity;
	public final int isUnlit;
	public final int ambientLightFactor;
	public final int objectColor;
	public final int materialSpecular;
	public final int renderStyle;
	public final int shine;
	public final int roughnessScale;
	public final int roughnessStrength;
	public final int opacity;
	public final int textureOpacityAffectsAlpha;
	public final int shadowMap;
	public final int shadowsEnabled;
	public final int shadowLightIndex;
	public final int shadowStrength;
	public final int textureTransformMatrix;
	public final int textureSampler;
	public final int hasTexture;
	public final int forceWhite;
	public final int enableRoughnessBump;
	public final int hideInnerSurfaces;
	public final int xrayMode;
	public final int transparencyOutputMode;
	public final LightUniforms[] lights = new LightUniforms[MAX_LIGHTS];

	MainShaderUniforms(GLShader shader) {
		this.projection = shader.requireUniformLocation("projection");
		this.view = shader.requireUniformLocation("view");
		this.model = shader.requireUniformLocation("model");
		this.normalMatrix = shader.requireUniformLocation("normalMatrix");
		this.viewPos = shader.requireUniformLocation("viewPos");
		this.lightSpaceMatrix = shader.requireUniformLocation("lightSpaceMatrix");
		this.numLights = shader.requireUniformLocation("numLights");
		this.fogEnabled = shader.requireUniformLocation("fogEnabled");
		this.fogColor = shader.requireUniformLocation("fogColor");
		this.fogDensity = shader.requireUniformLocation("fogDensity");
		this.isUnlit = shader.requireUniformLocation("isUnlit");
		this.ambientLightFactor = shader.requireUniformLocation("ambientLightFactor");
		this.objectColor = shader.requireUniformLocation("objectColor");
		this.materialSpecular = shader.requireUniformLocation("materialSpecular");
		this.renderStyle = shader.requireUniformLocation("renderStyle");
		this.shine = shader.requireUniformLocation("shine");
		this.roughnessScale = shader.requireUniformLocation("roughnessScale");
		this.roughnessStrength = shader.requireUniformLocation("roughnessStrength");
		this.opacity = shader.requireUniformLocation("opacity");
		this.textureOpacityAffectsAlpha = shader.requireUniformLocation("textureOpacityAffectsAlpha");
		this.shadowMap = shader.requireUniformLocation("shadowMap");
		this.shadowsEnabled = shader.requireUniformLocation("shadowsEnabled");
		this.shadowLightIndex = shader.requireUniformLocation("shadowLightIndex");
		this.shadowStrength = shader.requireUniformLocation("shadowStrength");
		this.textureTransformMatrix = shader.requireUniformLocation("textureTransformMatrix");
		this.textureSampler = shader.requireUniformLocation("textureSampler");
		this.hasTexture = shader.requireUniformLocation("hasTexture");
		this.forceWhite = shader.requireUniformLocation("forceWhite");
		this.enableRoughnessBump = shader.requireUniformLocation("enableRoughnessBump");
		this.hideInnerSurfaces = shader.requireUniformLocation("hideInnerSurfaces");
		this.xrayMode = shader.requireUniformLocation("xrayMode");
		this.transparencyOutputMode = shader.requireUniformLocation("transparencyOutputMode");
		for (int i = 0; i < lights.length; i++) {
			lights[i] = new LightUniforms(shader, i);
		}
	}

	public static final class LightUniforms {
		public final int type;
		public final int position;
		public final int direction;
		public final int color;

		private LightUniforms(GLShader shader, int index) {
			String prefix = "lights[" + index + "].";
			this.type = shader.requireUniformLocation(prefix + "type");
			this.position = shader.requireUniformLocation(prefix + "position");
			this.direction = shader.requireUniformLocation(prefix + "direction");
			this.color = shader.requireUniformLocation(prefix + "color");
		}
	}
}
