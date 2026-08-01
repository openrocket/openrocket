package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.GradientBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.rendering.passes.AmbientOcclusionPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.BackgroundPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.CameraPointOfInterestPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.CaretsPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.FXAAPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.GeometryPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.MotionBlurPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.OutlinePass;
import info.openrocket.swing.gui.figure3d.rendering.passes.RenderPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.ScreenTexturePass;
import info.openrocket.swing.gui.figure3d.rendering.passes.ShadowPass;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.Light;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DOUBLEBUFFER;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawBuffer;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Main OpenGL 3.3 renderer for a figure3d scene.
 *
 * Owns the scene shader, render passes, particle renderers, and offscreen targets
 * used to draw a single view. Like everything in this package, it talks directly
 * to OpenGL through LWJGL and must run on a thread with a current GL context.
 */
public class RealisticRenderer implements GLRenderer {

	private static final Logger log = LoggerFactory.getLogger(RealisticRenderer.class);
	private static final int DEFAULT_SCENE_MSAA_SAMPLES = 4;
	/**
	 * Multisampled colour and depth renderbuffers cost samples x 8 bytes per pixel,
	 * so 4x sampling at a scaled laptop resolution is around 166 MB on its own.
	 * Halving it keeps edge quality usable on GPUs that share system memory.
	 */
	private static final int CONSTRAINED_SCENE_MSAA_SAMPLES = 2;
	private static final int MAX_SHADER_LIGHTS = 10;

	// Shader resource paths
	private static final String MAIN_VERTEX_SHADER_PATH = "/shaders/vertex.glsl";
	private static final String MAIN_FRAGMENT_SHADER_PATH = "/shaders/fragment.glsl";
	private static final String SCREEN_QUAD_VERTEX_SHADER_PATH = "/shaders/post/screen_quad_vertex.glsl";
	private static final String SCREEN_QUAD_FRAGMENT_SHADER_PATH = "/shaders/post/screen_quad_fragment.glsl";

	// Full-screen quad used by the post-processing passes (positions + texCoords)
	private static final float[] SCREEN_QUAD_VERTICES = {
			-1.0f,  1.0f,  0.0f, 1.0f,
			-1.0f, -1.0f,  0.0f, 0.0f,
			1.0f, -1.0f,  1.0f, 0.0f,

			-1.0f,  1.0f,  0.0f, 1.0f,
			1.0f, -1.0f,  1.0f, 0.0f,
			1.0f,  1.0f,  1.0f, 1.0f
	};

	private final GLShader mainShader;
	private final Vector4f selectionColor = ColorUtils.srgbToLinear(new org.joml.Vector4f(1.0f, 0.2f, 0.1f, 1.0f));
	private final Vector3f cameraPosition = new Vector3f();
	private final Vector3f scratchFogColor = new Vector3f();
	private final Vector3f blurRocketAxis = new Vector3f();
	private final Vector4f blurOrigin = new Vector4f();
	private final Vector4f blurTip = new Vector4f();
	private final Matrix4f blurViewProjection = new Matrix4f();

	// Performance optimizations
    private final TextureBinder textureStateManager = new TextureStateManager();
	private final RenderStats renderStats = new RenderStats();

	// Viewport dimensions the renderer was created with, kept around so the
	// viewport can be restored to its initial state if ever needed.
	private final int initialWidth;
	private final int initialHeight;
	private final GpuMemoryProfile gpuMemoryProfile;
	private int screenWidth;
	private int screenHeight;
	private final RenderingConfiguration config;

	// Screen quad for post-processing
	private final int screenQuadVAO;
	private final int screenQuadVBO;

	// Cached uniform locations
	private final ShaderUniforms mainShaderUniforms;

	private final ParticleRenderer particleRenderer;
	private final VolumetricSmokeRenderer volumetricSmokeRenderer;
	private final FlameRenderer flameRenderer;
    private final GLShader screenQuadShader;
	private final int screenQuadTextureLocation;
	private final int screenQuadApplyGammaCorrectionLocation;
    private final CaretsPass caretsPass;
    private final CameraPointOfInterestPass cameraPointOfInterestPass;
    private final ShadowPass shadowPass;
    private final AmbientOcclusionPass ambientOcclusionPass;
    private final OutlinePass outlinePass;
    private final FXAAPass fxaaPass;

	private final OffscreenRenderTarget renderTarget;
	private int resolvedTextureId;

	/**
	 * Cached uniform locations for the main scene shader.
	 */
	public static class ShaderUniforms {
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
		public final int selectionColor;
		public final int isSelected;
		public final int isUnlit;
		public final int ambientLightFactor;
		public final int objectColor;
		public final int materialSpecular;
		public final int specularTintFactor;
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
		public final int hasDecal;
		public final int decalTransformMatrix;
		public final int decalSampler;
		public final int decalSurfaceMask;
		public final int forceWhite;
		public final int enableRoughnessBump;
		public final int hideInnerSurfaces;
		public final int xrayMode;
		public final LightUniforms[] lights = new LightUniforms[MAX_SHADER_LIGHTS];

		/**
		 * Creates a new uniform location cache for the given shader.
		 * 
		 * @param shader The shader to resolve uniform locations for
		 */
		ShaderUniforms(GLShader shader) {
			this.projection = shader.getUniformLocation("projection");
			this.view = shader.getUniformLocation("view");
			this.model = shader.getUniformLocation("model");
			this.normalMatrix = shader.getUniformLocation("normalMatrix");
			this.viewPos = shader.getUniformLocation("viewPos");
			this.lightSpaceMatrix = shader.getUniformLocation("lightSpaceMatrix");
			this.numLights = shader.getUniformLocation("numLights");
			this.fogEnabled = shader.getUniformLocation("fogEnabled");
			this.fogColor = shader.getUniformLocation("fogColor");
			this.fogDensity = shader.getUniformLocation("fogDensity");
			this.selectionColor = shader.getUniformLocation("selectionColor");
			this.isSelected = shader.getUniformLocation("isSelected");
			this.isUnlit = shader.getUniformLocation("isUnlit");
			this.ambientLightFactor = shader.getUniformLocation("ambientLightFactor");
			this.objectColor = shader.getUniformLocation("objectColor");
			this.materialSpecular = shader.getUniformLocation("materialSpecular");
			this.specularTintFactor = shader.getUniformLocation("specularTintFactor");
			this.renderStyle = shader.getUniformLocation("renderStyle");
			this.shine = shader.getUniformLocation("shine");
			this.roughnessScale = shader.getUniformLocation("roughnessScale");
			this.roughnessStrength = shader.getUniformLocation("roughnessStrength");
			this.opacity = shader.getUniformLocation("opacity");
			this.textureOpacityAffectsAlpha = shader.getUniformLocation("textureOpacityAffectsAlpha");
			this.shadowMap = shader.getUniformLocation("shadowMap");
			this.shadowsEnabled = shader.getUniformLocation("shadowsEnabled");
			this.shadowLightIndex = shader.getUniformLocation("shadowLightIndex");
			this.shadowStrength = shader.getUniformLocation("shadowStrength");
			this.textureTransformMatrix = shader.getUniformLocation("textureTransformMatrix");
			this.textureSampler = shader.getUniformLocation("textureSampler");
			this.hasTexture = shader.getUniformLocation("hasTexture");
			this.hasDecal = shader.getUniformLocation("hasDecal");
			this.decalTransformMatrix = shader.getUniformLocation("decalTransformMatrix");
			this.decalSampler = shader.getUniformLocation("decalSampler");
			this.decalSurfaceMask = shader.getUniformLocation("decalSurfaceMask");
			this.forceWhite = shader.getUniformLocation("forceWhite");
			this.enableRoughnessBump = shader.getUniformLocation("enableRoughnessBump");
			this.hideInnerSurfaces = shader.getUniformLocation("hideInnerSurfaces");
			this.xrayMode = shader.getUniformLocation("xrayMode");
			for (int i = 0; i < lights.length; i++) {
				lights[i] = new LightUniforms(shader, i);
			}
		}

		public static class LightUniforms {
			public final int type;
			public final int position;
			public final int direction;
			public final int color;

			LightUniforms(GLShader shader, int index) {
				String prefix = "lights[" + index + "].";
				this.type = shader.getUniformLocation(prefix + "type");
				this.position = shader.getUniformLocation(prefix + "position");
				this.direction = shader.getUniformLocation(prefix + "direction");
				this.color = shader.getUniformLocation(prefix + "color");
			}
		}
	}

	private final List<RenderPass> geometryPasses = new ArrayList<>();
    private final List<RenderPass> postProcessingPasses = new ArrayList<>();
    private MotionBlurPass motionBlurPass;
    private volatile boolean interactionMode = false;

	/**
	 * Creates a new realistic renderer with the specified configuration and viewport dimensions.
	 * 
	 * Initializes the complete rendering pipeline including shaders, framebuffers,
	 * particle renderers, and post-processing passes based on the provided configuration.
	 * 
	 * @param config The rendering configuration specifying quality settings and visual effects
	 * @param rocket The rocket model to be rendered
	 * @param initialWidth Initial viewport width in pixels
	 * @param initialHeight Initial viewport height in pixels
	 * @throws ShaderException If shader compilation fails
	 */
	public RealisticRenderer(RenderingConfiguration config, Rocket rocket, int initialWidth, int initialHeight) {
		this.config = config;
		this.initialWidth = initialWidth;
		this.initialHeight = initialHeight;
		this.screenWidth = initialWidth;
		this.screenHeight = initialHeight;

		// Detected before any target is allocated, because it decides how large
		// the scene target and the shadow map are allowed to be.
		this.gpuMemoryProfile = GpuMemoryProfile.detect();
		log.info("3D render memory profile: {}", gpuMemoryProfile);

		// Main shader for scene objects
		mainShader = new GLShader(MAIN_VERTEX_SHADER_PATH, MAIN_FRAGMENT_SHADER_PATH);
		mainShaderUniforms = new ShaderUniforms(mainShader);
		screenQuadShader = new GLShader(SCREEN_QUAD_VERTEX_SHADER_PATH, SCREEN_QUAD_FRAGMENT_SHADER_PATH);
		screenQuadTextureLocation = screenQuadShader.getUniformLocation("screenTexture");
		screenQuadApplyGammaCorrectionLocation = screenQuadShader.getUniformLocation("applyGammaCorrection");

		particleRenderer = new ParticleRenderer();
		volumetricSmokeRenderer = new VolumetricSmokeRenderer();
		flameRenderer = new FlameRenderer();

		ScreenQuad screenQuad = createScreenQuad();
		screenQuadVAO = screenQuad.vao();
		screenQuadVBO = screenQuad.vbo();

		renderTarget = new OffscreenRenderTarget(initialWidth, initialHeight, getRequestedSceneSampleCount());
		resolvedTextureId = renderTarget.getColorTextureId();

		shadowPass = createShadowPass();
		initGeometryPasses();
		caretsPass = createCaretsPass(rocket);
		cameraPointOfInterestPass = createCameraPointOfInterestPass();

		// Post-processing passes
		ambientOcclusionPass = new AmbientOcclusionPass(screenQuadVAO, textureStateManager,
				config.getQuality(), initialWidth, initialHeight);
		motionBlurPass = createMotionBlurPass();
		outlinePass = new OutlinePass(mainShader, mainShaderUniforms, textureStateManager,
				screenQuadVAO, selectionColor, initialWidth, initialHeight, screenQuadShader);
		fxaaPass = new FXAAPass(screenQuadVAO, initialWidth, initialHeight);

		// Apply initial settings
		updatePostProcessingChain();

		GLErrors.check("renderer initialization");

		// Add a configuration listener
		config.addListener(this::onScenePropertiesChanged);
	}

	private record ScreenQuad(int vao, int vbo) {}

	/**
	 * Creates and uploads the full-screen quad used by the post-processing passes,
	 * and configures its vertex attribute pointers (position + texCoord).
	 */
	private static ScreenQuad createScreenQuad() {
		int vao = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao, "GLRenderer screenQuadVAO");
		int vbo = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vbo, "GLRenderer screenQuadVBO");

		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, SCREEN_QUAD_VERTICES, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
		glBindVertexArray(0);

		return new ScreenQuad(vao, vbo);
	}

	private ShadowPass createShadowPass() {
		ShadowPass pass = new ShadowPass(initialWidth, initialHeight, gpuMemoryProfile.isConstrained());
		pass.setQuality(config.getQuality().getQuality());
		pass.setEnabled(config.getQuality().isShadowsEnabled());
		return pass;
	}

	private void initGeometryPasses() {
		geometryPasses.add(new BackgroundPass(textureStateManager));
		geometryPasses.add(new GeometryPass(mainShader, config, textureStateManager, mainShaderUniforms, renderStats));
	}

	private CaretsPass createCaretsPass(Rocket rocket) {
		CaretsPass pass = new CaretsPass(rocket, config);
		pass.resize(initialWidth, initialHeight);
		return pass;
	}

	private CameraPointOfInterestPass createCameraPointOfInterestPass() {
		CameraPointOfInterestPass pass = new CameraPointOfInterestPass(config);
		pass.resize(initialWidth, initialHeight);
		return pass;
	}

	private MotionBlurPass createMotionBlurPass() {
		MotionBlurPass pass = new MotionBlurPass(screenQuadVAO, initialWidth, initialHeight);
		pass.setBlurFactor(config.getVisualEffects().getMotionBlurFactor());
		return pass;
	}

	@Override
    public void render(SceneView scene, boolean renderBackground) {
		renderStats.reset();
		long startTime = System.nanoTime();

		// Some renderers and presentation paths bind textures directly instead of going
		// through TextureStateManager. Reset the cache at the start of each frame so the
		// first geometry/background/material bind always reflects the actual GL state.
		textureStateManager.reset();

		Camera camera = scene.getCamera();
		camera.update();

		final Matrix4f cameraViewMatrix = camera.getViewMatrix();
		final Matrix4f cameraProjectionMatrix = camera.getProjectionMatrix();

		prepareFrameGLState();

		// Update dynamic flame lighting
		updateFlameLighting(scene);
		// Rebuilding a complex shadow map for every mouse event can keep weaker
		// Windows drivers continuously saturated. The first idle frame recreates it.
		shadowPass.setEnabled(config.getQuality().isShadowsEnabled() && !shouldReduceInteractionEffects());
		shadowPass.setQuality(config.getQuality().getQuality());
		shadowPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
		glViewport(0, 0, screenWidth, screenHeight);

		// Prepare the main shader with uniforms that are constant for the frame
		prepareShaderGlobals(scene, camera, cameraViewMatrix, cameraProjectionMatrix);

		// 1. Render the geometry passes to the main FBO
		renderSceneToTarget(scene, renderBackground, camera, cameraViewMatrix, cameraProjectionMatrix);

		// Particle renderers bind textures directly, so invalidate the cache before any
		// post-processing pass that relies on TextureStateManager.
		textureStateManager.reset();

		// 2. Run the post-processing chain
		int finalTexture = runPostProcessingChain(scene, cameraViewMatrix, cameraProjectionMatrix);
		resolveFinalTexture(finalTexture);

		GLErrors.debugCheck("frame render");

		renderStats.frameTimeNanos = System.nanoTime() - startTime;
	}

	/**
	 * Sets the fixed-function GL state (depth testing, face culling) for the frame.
	 */
	private void prepareFrameGLState() {
		glEnable(GL_DEPTH_TEST);
		if (config.getQuality().isBackfaceCullingEnabled() && !config.getDisplay().shouldDisableCulling()) {
			glEnable(GL_CULL_FACE);
			glCullFace(GL_BACK);
		}
	}

	/**
	 * Renders the geometry passes, particle systems, and overlay markers into the
	 * off-screen render target.
	 */
	private void renderSceneToTarget(SceneView scene, boolean renderBackground,
									 Camera camera, Matrix4f cameraViewMatrix, Matrix4f cameraProjectionMatrix) {
		renderTarget.bind();
		glViewport(0, 0, screenWidth, screenHeight);
		clearRenderTarget(scene, renderBackground);

		for (RenderPass pass : geometryPasses) {
			if (!renderBackground && pass instanceof BackgroundPass) {
				continue;
			}
			pass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
		}

		particleRenderer.render(scene, camera);
		volumetricSmokeRenderer.render(scene, camera);
		flameRenderer.render(scene, camera);
		if (config.getVisualEffects().areCaretsVisible()) {
			caretsPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
		}
		cameraPointOfInterestPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
		renderTarget.unbind();
	}

	private void clearRenderTarget(SceneView scene, boolean renderBackground) {
		if (!renderBackground) {
			if (scene.getBackground() instanceof SolidColorBackground solidBackground) {
				Vector4f color = solidBackground.getColor();
				glClearColor(color.x, color.y, color.z, color.w);
			} else {
				glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			}
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		} else {
			glClear(GL_COLOR_BUFFER_BIT);
		}
	}

	/**
	 * Runs the post-processing passes over the rendered scene texture.
	 *
	 * @return the texture id holding the final processed frame
	 */
	private int runPostProcessingChain(SceneView scene,
									   Matrix4f cameraViewMatrix, Matrix4f cameraProjectionMatrix) {
		int currentTexture = renderTarget.getColorTextureId();
		boolean reduceInteractionEffects = shouldReduceInteractionEffects();

		if (config.getQuality().isAmbientOcclusionEnabled() && !reduceInteractionEffects) {
			ambientOcclusionPass.setInputTexture(currentTexture);
			ambientOcclusionPass.setDepthTexture(renderTarget.getDepthTextureId());
			ambientOcclusionPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
			currentTexture = ambientOcclusionPass.getOutputTexture();
		}

		// Apply motion blur if enabled (only affects the rocket, not the background)
		if (config.getVisualEffects().isMotionBlurEnabled() && !reduceInteractionEffects) {
			motionBlurPass.setBlurFactor(config.getVisualEffects().getMotionBlurFactor());
			motionBlurPass.setInputTexture(currentTexture);
			motionBlurPass.setDepthTexture(renderTarget.getDepthTextureId());
			computeAndSetBlurDirection(scene, cameraViewMatrix, cameraProjectionMatrix);
			motionBlurPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
			currentTexture = motionBlurPass.getOutputTexture();
		}

		for (RenderPass pass : postProcessingPasses) {
			// Outline pass is screen-space and full-frame; skip during interaction.
			if (reduceInteractionEffects && pass instanceof OutlinePass) {
				continue;
			}
			if (pass instanceof ScreenTexturePass screenPass) {
				screenPass.setInputTexture(currentTexture);
				pass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
				currentTexture = screenPass.getOutputTexture();
			} else {
				// This pass doesn't process the screen texture, just run it
				pass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
			}
		}

		return currentTexture;
	}

	private boolean shouldReduceInteractionEffects() {
		return interactionMode && config.getQuality().shouldReduceEffectsDuringInteraction();
	}

	/**
	 * Computes the rocket's axis direction in screen space and passes it to the motion blur pass.
	 * The rocket axis is along the local X-axis of its model matrix. We project this direction
	 * through the view-projection matrix to get a 2D screen-space blur direction.
	 */
	private void computeAndSetBlurDirection(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		// Find the first rocket component to get its model matrix
		Matrix4f rocketModel = null;
		for (SceneObject obj : scene.getObjects()) {
			if (obj.getRocketComponent() != null) {
				rocketModel = obj.getModelMatrix();
				break;
			}
		}

		if (rocketModel == null) {
			motionBlurPass.setBlurDirection(1.0f, 0.0f);
			return;
		}

		// The rocket axis is along X in model space.
		// Extract the X-axis direction from the model matrix (first column).
		Vector3f rocketAxis = blurRocketAxis.set(rocketModel.m00(), rocketModel.m01(), rocketModel.m02()).normalize();

		// Pick a reference point (center of the rocket in world space)
		Vector4f origin = blurOrigin.set(rocketModel.m30(), rocketModel.m31(), rocketModel.m32(), 1.0f);
		Vector4f tip = blurTip.set(
				origin.x + rocketAxis.x,
				origin.y + rocketAxis.y,
				origin.z + rocketAxis.z,
				1.0f
		);

		// Transform both points to clip space
		Matrix4f vp = blurViewProjection.set(projectionMatrix).mul(viewMatrix);
		vp.transform(origin);
		vp.transform(tip);

		// Perspective divide to get NDC
		if (Math.abs(origin.w) < 0.0001f || Math.abs(tip.w) < 0.0001f) {
			motionBlurPass.setBlurDirection(1.0f, 0.0f);
			return;
		}
		float ox = origin.x / origin.w;
		float oy = origin.y / origin.w;
		float tx = tip.x / tip.w;
		float ty = tip.y / tip.w;

		// Screen-space direction (NDC is -1..1, texture coords are 0..1, but direction is the same)
		motionBlurPass.setBlurDirection(tx - ox, ty - oy);
	}

	/**
	 * Sets up global shader uniforms that remain constant for the entire frame.
	 *
	 * This includes camera matrices, lighting information, fog settings,
	 * and other frame-constant values to avoid redundant uniform updates.
	 *
	 * @param scene The scene containing lights and environment settings
	 * @param camera The active camera
	 * @param viewMatrix The camera's view transformation matrix
	 * @param projectionMatrix The camera's projection matrix
	 */
	private void prepareShaderGlobals(SceneView scene, Camera camera, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		mainShader.use();

		// Camera uniforms
		mainShader.setUniformMatrix4f(mainShaderUniforms.projection, projectionMatrix);
		mainShader.setUniformMatrix4f(mainShaderUniforms.view, viewMatrix);
		Vector3f cameraPos = camera.getPosition(cameraPosition);
		glUniform3f(mainShaderUniforms.viewPos, cameraPos.x, cameraPos.y, cameraPos.z);

		mainShader.setUniformMatrix4f(mainShaderUniforms.lightSpaceMatrix, shadowPass.getLightSpaceMatrix());
		textureStateManager.bindTexture(2, GL_TEXTURE_2D, shadowPass.getDepthMapTexture());
		glUniform1i(mainShaderUniforms.shadowMap, 2);
		if (shadowPass.hasShadowMap()) {
			glUniform1i(mainShaderUniforms.shadowsEnabled, 1);
			glUniform1i(mainShaderUniforms.shadowLightIndex, shadowPass.getShadowCastingLightIndex());
			glUniform1f(mainShaderUniforms.shadowStrength, shadowPass.getShadowStrength());
		} else {
			glUniform1i(mainShaderUniforms.shadowsEnabled, 0);
			glUniform1i(mainShaderUniforms.shadowLightIndex, -1);
			glUniform1f(mainShaderUniforms.shadowStrength, 0.0f);
		}

		// --- Lighting ---
		List<Light> lights = scene.getLightController().getLights();
		int numActiveLights = Math.min(lights.size(), MAX_SHADER_LIGHTS);
		glUniform1i(mainShaderUniforms.numLights, numActiveLights);

		for (int i = 0; i < numActiveLights; i++) {
			Light light = lights.get(i);
			ShaderUniforms.LightUniforms lightUniforms = mainShaderUniforms.lights[i];
			Vector3f lightPosition = light.getPosition();
			Vector3f lightDirection = light.getDirection();
			Vector3f lightColor = light.getColor();
			glUniform1i(lightUniforms.type, light.getType().ordinal());
			glUniform3f(lightUniforms.position, lightPosition.x, lightPosition.y, lightPosition.z);
			glUniform3f(lightUniforms.direction, lightDirection.x, lightDirection.y, lightDirection.z);
			glUniform3f(lightUniforms.color, lightColor.x, lightColor.y, lightColor.z);
		}

		// Fog
		if (scene.isFogEnabled()) {
			glUniform1i(mainShaderUniforms.fogEnabled, 1);
			final Vector3f fogColor = scratchFogColor;
			if (scene.getBackground() instanceof SolidColorBackground) {
				Vector4f bgColor = ((SolidColorBackground) scene.getBackground()).getColor();
				fogColor.set(bgColor.x, bgColor.y, bgColor.z);
			} else if (scene.getBackground() instanceof GradientBackground) {
				fogColor.set(((GradientBackground) scene.getBackground()).getBottomColor());
			} else {
				fogColor.set(0.5f, 0.6f, 0.7f);
			}
			glUniform3f(mainShaderUniforms.fogColor, fogColor.x, fogColor.y, fogColor.z);
			glUniform1f(mainShaderUniforms.fogDensity, scene.getFogDensity());
		} else {
			glUniform1i(mainShaderUniforms.fogEnabled, 0);
		}

		glUniform4f(mainShaderUniforms.selectionColor, selectionColor.x, selectionColor.y,
				selectionColor.z, selectionColor.w);

		glUniform1f(mainShaderUniforms.ambientLightFactor, config.getVisualEffects().getAmbientLightFactor());
		// The roughness bump is the most expensive thing the fragment shader does, so it is
		// suspended during interaction along with the shadow map and the post-processing
		// passes — but only when the user has asked for that, since dropping it is a visible
		// change to the surface rather than a free one. The first idle frame restores it.
		boolean roughnessBump = config.getQuality().isRoughnessBumpRendered() && !shouldReduceInteractionEffects();
		glUniform1i(mainShaderUniforms.enableRoughnessBump, roughnessBump ? 1 : 0);
	}
	
	/**
	 * Updates dynamic lighting from flame particle emitters.
	 * 
	 * Manages the addition and removal of flame lights in the scene's
	 * light manager based on active flame emitters.
	 * 
	 * @param scene The scene containing particle emitters and light manager
	 */
	private void updateFlameLighting(SceneView scene) {
		// Update each flame's light and manage scene lights
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (emitter instanceof FlameEmitter flameEmitter) {
				// Remove old light if it exists
                Light oldLight = flameEmitter.getFlameLight();
                if (oldLight != null) {
                    scene.getLightController().removeLight(oldLight);
                }
				
				// Update the flame's light properties
				flameEmitter.updateFlameLight();
				
				// Add new light if it exists
                Light newLight = flameEmitter.getFlameLight();
                if (newLight != null) {
                    scene.getLightController().addLight(newLight);
                }
			}
		}
	}

	private void resolveFinalTexture(int currentTexture) {
		if (currentTexture == 0) {
			resolvedTextureId = 0;
			return;
		}

		if (currentTexture == renderTarget.getColorTextureId()) {
			resolvedTextureId = currentTexture;
			return;
		}

		renderTarget.bind();
		glViewport(0, 0, screenWidth, screenHeight);
		glDisable(GL_DEPTH_TEST);
		glClear(GL_COLOR_BUFFER_BIT);

		screenQuadShader.use();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, currentTexture);
		glUniform1i(screenQuadTextureLocation, 0);

		glBindVertexArray(screenQuadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);

		glEnable(GL_DEPTH_TEST);
		renderTarget.unbind();
		resolvedTextureId = renderTarget.getColorTextureId();
	}

	public int getResolvedTextureId() {
		return resolvedTextureId;
	}

	public int getResolvedFramebufferId() {
		return renderTarget.getFramebufferId();
	}

	public int getRenderWidth() {
		return screenWidth;
	}

	public int getRenderHeight() {
		return screenHeight;
	}

	public void presentResolvedToCurrentFramebuffer() {
		if (resolvedTextureId == 0) {
			return;
		}

		int presentBuffer = glGetInteger(GL_DOUBLEBUFFER) != 0 ? GL_BACK : GL_FRONT;

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glDrawBuffer(presentBuffer);
		glDepthMask(true);
		glColorMask(true, true, true, true);
		glViewport(0, 0, screenWidth, screenHeight);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_SCISSOR_TEST);
		glDisable(GL_STENCIL_TEST);
		glDisable(GL_BLEND);
		glDisable(GL_CULL_FACE);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		// Disable automatic sRGB conversion — apply it manually in the shader instead,
		// so behaviour is consistent regardless of whether the default framebuffer is
		// sRGB-capable (it is silently ignored on many Linux/GLX drivers).
		glDisable(GL_FRAMEBUFFER_SRGB);

		screenQuadShader.use();
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, resolvedTextureId);
		glUniform1i(screenQuadTextureLocation, 0);
		glUniform1i(screenQuadApplyGammaCorrectionLocation, 1);

		glBindVertexArray(screenQuadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);

		// Reset uniform and restore sRGB state for subsequent intermediate passes
		glUniform1i(screenQuadApplyGammaCorrectionLocation, 0);
		glEnable(GL_FRAMEBUFFER_SRGB);
		glEnable(GL_DEPTH_TEST);
	}

	/**
	 * Resizes the renderer's viewport and all associated framebuffers.
	 * 
	 * This method should be called when the window is resized to ensure
	 * all rendering targets match the new dimensions.
	 * 
	 * @param width New viewport width in pixels
	 * @param height New viewport height in pixels
	 */
	@Override
	public void resize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;

		renderTarget.resize(width, height);
		resolvedTextureId = renderTarget.getColorTextureId();
		shadowPass.resize(width, height);

		for (RenderPass pass : geometryPasses) {
			pass.resize(width, height);
		}
        ambientOcclusionPass.resize(width, height);
        outlinePass.resize(width, height);
        fxaaPass.resize(width, height);
		motionBlurPass.resize(width, height);
		caretsPass.resize(width, height);
		cameraPointOfInterestPass.resize(width, height);
	}

	@Override
	public void setCaretPositions(info.openrocket.core.util.CoordinateIF cg, info.openrocket.core.util.CoordinateIF cp) {
		caretsPass.setPositions(cg, cp);
	}

	@Override
	public void resetTextureState() {
		textureStateManager.reset();
	}

	@Override
	public void cleanup() {
		mainShader.cleanup();
		particleRenderer.cleanup();
		flameRenderer.cleanup();
		volumetricSmokeRenderer.cleanup();
        screenQuadShader.cleanup();

		caretsPass.cleanup();
		cameraPointOfInterestPass.cleanup();
		shadowPass.cleanup();

		for (RenderPass pass : geometryPasses) {
			pass.cleanup();
		}
        ambientOcclusionPass.cleanup();
        outlinePass.cleanup();
        fxaaPass.cleanup();
		motionBlurPass.cleanup();

		renderTarget.cleanup();
		if (screenQuadVAO != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, screenQuadVAO);
			glDeleteVertexArrays(screenQuadVAO);
		}
		if (screenQuadVBO != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, screenQuadVBO);
			glDeleteBuffers(screenQuadVBO);
		}

		textureStateManager.reset();
	}
	
	private void onScenePropertiesChanged(RenderingConfiguration config) {
		// Update motion blur settings
		motionBlurPass.setBlurFactor(config.getVisualEffects().getMotionBlurFactor());
		renderTarget.setSamples(getRequestedSceneSampleCount());
		resolvedTextureId = renderTarget.getColorTextureId();
		
		// Update post-processing chain based on current settings
		updatePostProcessingChain();

		shadowPass.setQuality(config.getQuality().getQuality());
		shadowPass.setEnabled(config.getQuality().isShadowsEnabled());
	}
	
	private void updatePostProcessingChain() {
        postProcessingPasses.clear();
        postProcessingPasses.add(outlinePass);
        if (config.getQuality().isFXAAEnabled()) {
            postProcessingPasses.add(fxaaPass);
        }
	}

	@Override
	public void setInteractionMode(boolean active) {
		this.interactionMode = active;
	}

	/**
	 * @return the memory profile detected for this context, for diagnostics
	 */
	public GpuMemoryProfile getGpuMemoryProfile() {
		return gpuMemoryProfile;
	}

	/**
	 * @return the multisample count the scene target actually allocated, which may
	 *         be below the requested count when the driver or the memory profile
	 *         caps it
	 */
	public int getSceneSampleCount() {
		return renderTarget.getSamples();
	}

	private int getRequestedSceneSampleCount() {
		// An explicit override is a diagnostic knob and deliberately outranks both the
		// quality level and the memory profile, so a constrained device can still be
		// asked for 8x.
		String override = System.getProperty("openrocket.figure3d.msaaSamples");
		if (override != null) {
			try {
				return Math.max(0, Integer.parseInt(override.trim()));
			} catch (NumberFormatException ignored) {
				// fall through
			}
		}

		// Scene multisampling follows the quality level rather than the FXAA setting.
		// Those used to be the same switch, which meant turning FXAA off silently also
		// turned off multisampling, and no quality level had any effect on it.
		int requested = config.getQuality().getSceneSampleCount();
		return gpuMemoryProfile.isConstrained()
				? Math.min(requested, CONSTRAINED_SCENE_MSAA_SAMPLES)
				: Math.min(requested, DEFAULT_SCENE_MSAA_SAMPLES);
	}

	/**
	 * Performance statistics for debugging and optimization.
	 * 
	 * Tracks rendering metrics such as objects rendered, texture bindings,
	 * state changes, and frame timing for performance analysis.
	 */
	public static class RenderStats {
		public int objectsRendered;
		public int textureBinds;
		public int stateChanges;
		public long frameTimeNanos;

		/**
		 * Resets all counters for a new frame.
		 */
		public void reset() {
			objectsRendered = 0;
			textureBinds = 0;
			stateChanges = 0;
		}

		/**
		 * Prints current statistics to console for debugging.
		 */
		public void print() {
			log.debug("Frame: {}ms, Rendered: {}, TexBinds: {}, States: {}",
					String.format("%.2f", frameTimeNanos / 1_000_000.0), objectsRendered, textureBinds, stateChanges);
		}
	}
}
