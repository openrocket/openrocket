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
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_DOUBLEBUFFER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

/**
 * Main OpenGL renderer for a figure3d scene.
 *
 * Owns the scene shader, render passes, particle renderers, and offscreen targets
 * used to draw a single view.
 */
public class RealisticRenderer implements Renderer {

	private static final Logger log = LoggerFactory.getLogger(RealisticRenderer.class);
	private static final int DEFAULT_SCENE_MSAA_SAMPLES = 4;

	private final ShaderProgram mainShader;
	private final Vector4f selectionColor = ColorUtils.srgbToLinear(new org.joml.Vector4f(1.0f, 0.2f, 0.1f, 1.0f));

	// Performance optimizations
    private final TextureBinder textureStateManager = new TextureStateManager();
	private final RenderStats renderStats = new RenderStats();

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
    private final ShaderProgram screenQuadShader;
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

		/**
		 * Creates a new uniform location cache for the given shader.
		 * 
		 * @param shader The shader to resolve uniform locations for
		 */
		ShaderUniforms(ShaderProgram shader) {
			this.projection = shader.getUniformLocation("projection");
			this.view = shader.getUniformLocation("view");
			this.model = shader.getUniformLocation("model");
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
		}
	}

	private final List<RenderPass> geometryPasses = new ArrayList<>();
    private final List<RenderPass> postProcessingPasses = new ArrayList<>();
    private MotionBlurPass motionBlurPass;

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
	 * @throws Exception If shader compilation or OpenGL resource creation fails
	 */
	public RealisticRenderer(RenderingConfiguration config, Rocket rocket, int initialWidth, int initialHeight) throws Exception {
		this.config = config;
		this.screenWidth = initialWidth;
		this.screenHeight = initialHeight;

		// Main shader for scene objects
		mainShader = new Shader("/shaders/vertex.glsl", "/shaders/fragment.glsl");
		mainShaderUniforms = new ShaderUniforms(mainShader);

		this.particleRenderer = new ParticleRenderer();
		this.volumetricSmokeRenderer = new VolumetricSmokeRenderer();
		this.flameRenderer = new FlameRenderer();
		this.screenQuadShader = new Shader("/shaders/post/screen_quad_vertex.glsl", "/shaders/post/screen_quad_fragment.glsl");

		// Create screen quad for post-processing
		float[] quadVertices = {
				// positions   // texCoords
				-1.0f,  1.0f,  0.0f, 1.0f,
				-1.0f, -1.0f,  0.0f, 0.0f,
				1.0f, -1.0f,  1.0f, 0.0f,

				-1.0f,  1.0f,  0.0f, 1.0f,
				1.0f, -1.0f,  1.0f, 0.0f,
				1.0f,  1.0f,  1.0f, 1.0f
		};

		screenQuadVAO = GL33.glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, screenQuadVAO, "Renderer screenQuadVAO");
		screenQuadVBO = GL33.glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, screenQuadVBO, "Renderer screenQuadVBO");
		GL33.glBindVertexArray(screenQuadVAO);
		GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, screenQuadVBO);
		GL33.glBufferData(GL33.GL_ARRAY_BUFFER, quadVertices, GL33.GL_STATIC_DRAW);
		GL33.glEnableVertexAttribArray(0);
		GL33.glVertexAttribPointer(0, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 0);
		GL33.glEnableVertexAttribArray(1);
		GL33.glVertexAttribPointer(1, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
		GL33.glBindVertexArray(0);

		// Initialize framebuffer
		this.renderTarget = new OffscreenRenderTarget(initialWidth, initialHeight, getRequestedSceneSampleCount());
		this.resolvedTextureId = renderTarget.getColorTextureId();
        this.shadowPass = new ShadowPass(initialWidth, initialHeight);
        this.shadowPass.setQuality(config.getQuality().getQuality());
        this.shadowPass.setEnabled(config.getQuality().isShadowsEnabled());

		// Initialize Render Passes
        geometryPasses.add(new BackgroundPass(textureStateManager));
        geometryPasses.add(new GeometryPass(mainShader, config, textureStateManager, mainShaderUniforms, renderStats));
        this.caretsPass = new CaretsPass(rocket, config);
        this.cameraPointOfInterestPass = new CameraPointOfInterestPass(config);

        // Initialize Post-Processing Passes
        this.ambientOcclusionPass = new AmbientOcclusionPass(screenQuadVAO, textureStateManager,
                config.getQuality(), initialWidth, initialHeight);
        this.motionBlurPass = new MotionBlurPass(screenQuadVAO, initialWidth, initialHeight);
        this.motionBlurPass.setBlurFactor(config.getVisualEffects().getMotionBlurFactor());
        this.outlinePass = new OutlinePass(mainShader, mainShaderUniforms, textureStateManager,
                screenQuadVAO, selectionColor, initialWidth, initialHeight, screenQuadShader);
        this.fxaaPass = new FXAAPass(screenQuadVAO, initialWidth, initialHeight);
        
        // Apply initial settings
        updatePostProcessingChain();

		// Add a configuration listener
		config.addListener(this::onScenePropertiesChanged);
	}

	@Override
    public void render(SceneView scene, WindowManager windowManager, boolean renderBackground) {
		renderStats.reset();
		long startTime = System.nanoTime();

		// Some renderers and presentation paths bind textures directly instead of going
		// through TextureStateManager. Reset the cache at the start of each frame so the
		// first geometry/background/material bind always reflects the actual GL state.
		textureStateManager.reset();

		Camera camera = scene.getCamera();
		camera.update();

		final Matrix4f viewMatrix = camera.getViewMatrix();
		final Matrix4f projectionMatrix = camera.getProjectionMatrix();

		glEnable(GL_DEPTH_TEST);
		if (config.getQuality().isBackfaceCullingEnabled() && !config.getDisplay().shouldDisableCulling()) {
			glEnable(GL_CULL_FACE);
			glCullFace(GL_BACK);
		}

		// Update dynamic flame lighting
		updateFlameLighting(scene);
		shadowPass.setEnabled(config.getQuality().isShadowsEnabled());
		shadowPass.setQuality(config.getQuality().getQuality());
		shadowPass.render(scene, windowManager, viewMatrix, projectionMatrix);
		glViewport(0, 0, screenWidth, screenHeight);
		
		// Prepare the main shader with uniforms that are constant for the frame
		prepareShaderGlobals(scene, camera, viewMatrix, projectionMatrix);

		// 1. Render the geometry passes to the main FBO
        renderTarget.bind();
        glViewport(0, 0, screenWidth, screenHeight);
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

        for (RenderPass pass : geometryPasses) {
            if (!renderBackground && pass instanceof BackgroundPass) {
                continue;
            }
            pass.render(scene, windowManager, viewMatrix, projectionMatrix);
        }

        particleRenderer.render(scene, camera);
        volumetricSmokeRenderer.render(scene, camera);
        flameRenderer.render(scene, camera);
        if (config.getVisualEffects().areCaretsVisible()) {
            caretsPass.render(scene, windowManager, viewMatrix, projectionMatrix);
        }
        cameraPointOfInterestPass.render(scene, windowManager, viewMatrix, projectionMatrix);
        renderTarget.unbind();

		// Particle renderers bind textures directly, so invalidate the cache before any
		// post-processing pass that relies on TextureStateManager.
		textureStateManager.reset();

        // 2. Run the post-processing chain
        int currentTexture = renderTarget.getColorTextureId();

        if (config.getQuality().isAmbientOcclusionEnabled()) {
            ambientOcclusionPass.setInputTexture(currentTexture);
            ambientOcclusionPass.setDepthTexture(renderTarget.getDepthTextureId());
            ambientOcclusionPass.render(scene, windowManager, viewMatrix, projectionMatrix);
            currentTexture = ambientOcclusionPass.getOutputTexture();
        }
        
        // Apply motion blur if enabled (only affects the rocket, not the background)
        if (config.getVisualEffects().isMotionBlurEnabled()) {
			motionBlurPass.setBlurFactor(config.getVisualEffects().getMotionBlurFactor());
            motionBlurPass.setInputTexture(currentTexture);
            motionBlurPass.setDepthTexture(renderTarget.getDepthTextureId());
            computeAndSetBlurDirection(scene, viewMatrix, projectionMatrix);
            motionBlurPass.render(scene, windowManager, viewMatrix, projectionMatrix);
            currentTexture = motionBlurPass.getOutputTexture();
        }
        
        for (RenderPass pass : postProcessingPasses) {
            if (pass instanceof ScreenTexturePass screenPass) {
				screenPass.setInputTexture(currentTexture);
                pass.render(scene, windowManager, viewMatrix, projectionMatrix);
                currentTexture = screenPass.getOutputTexture();
            } else {
                // This pass doesn't process the screen texture, just run it
                pass.render(scene, windowManager, viewMatrix, projectionMatrix);
            }
        }

        resolveFinalTexture(currentTexture);

		renderStats.frameTimeNanos = System.nanoTime() - startTime;
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
		Vector3f rocketAxis = new Vector3f(rocketModel.m00(), rocketModel.m01(), rocketModel.m02()).normalize();

		// Pick a reference point (center of the rocket in world space)
		Vector4f origin = new Vector4f(rocketModel.m30(), rocketModel.m31(), rocketModel.m32(), 1.0f);
		Vector4f tip = new Vector4f(
				origin.x + rocketAxis.x,
				origin.y + rocketAxis.y,
				origin.z + rocketAxis.z,
				1.0f
		);

		// Transform both points to clip space
		Matrix4f vp = new Matrix4f(projectionMatrix).mul(viewMatrix);
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
		mainShader.setUniform(mainShaderUniforms.projection, projectionMatrix);
		mainShader.setUniform(mainShaderUniforms.view, viewMatrix);
		Vector3f cameraPos = camera.getPosition();
		GL33.glUniform3f(mainShaderUniforms.viewPos, cameraPos.x, cameraPos.y, cameraPos.z);

		mainShader.setUniform(mainShaderUniforms.lightSpaceMatrix, shadowPass.getLightSpaceMatrix());
		textureStateManager.bindTexture(2, GL33.GL_TEXTURE_2D, shadowPass.getDepthMapTexture());
		GL33.glUniform1i(mainShaderUniforms.shadowMap, 2);
		if (shadowPass.hasShadowMap()) {
			GL33.glUniform1i(mainShaderUniforms.shadowsEnabled, 1);
			GL33.glUniform1i(mainShaderUniforms.shadowLightIndex, shadowPass.getShadowCastingLightIndex());
			GL33.glUniform1f(mainShaderUniforms.shadowStrength, shadowPass.getShadowStrength());
		} else {
			GL33.glUniform1i(mainShaderUniforms.shadowsEnabled, 0);
			GL33.glUniform1i(mainShaderUniforms.shadowLightIndex, -1);
			GL33.glUniform1f(mainShaderUniforms.shadowStrength, 0.0f);
		}

		// --- Lighting ---
		List<Light> lights = scene.getLightController().getLights();
		int numActiveLights = Math.min(lights.size(), 10);
		GL33.glUniform1i(mainShaderUniforms.numLights, numActiveLights);

		for (int i = 0; i < numActiveLights; i++) {
			Light light = lights.get(i);
			String lightUni = "lights[" + i + "].";
			GL33.glUniform1i(mainShader.getUniformLocation(lightUni + "type"), light.getType().ordinal());
			GL33.glUniform3f(mainShader.getUniformLocation(lightUni + "position"), light.getPosition().x, light.getPosition().y, light.getPosition().z);
			GL33.glUniform3f(mainShader.getUniformLocation(lightUni + "direction"), light.getDirection().x, light.getDirection().y, light.getDirection().z);
			GL33.glUniform3f(mainShader.getUniformLocation(lightUni + "color"), light.getColor().x, light.getColor().y, light.getColor().z);
		}

		// Fog
		if (scene.isFogEnabled()) {
			GL33.glUniform1i(mainShaderUniforms.fogEnabled, 1);
			final Vector3f fogColor;
			if (scene.getBackground() instanceof SolidColorBackground) {
				Vector4f bgColor = ((SolidColorBackground) scene.getBackground()).getColor();
				fogColor = new Vector3f(bgColor.x, bgColor.y, bgColor.z);
			} else if (scene.getBackground() instanceof GradientBackground) {
				fogColor = ((GradientBackground) scene.getBackground()).getBottomColor();
			} else {
				fogColor = new Vector3f(0.5f, 0.6f, 0.7f);
			}
			GL33.glUniform3f(mainShaderUniforms.fogColor, fogColor.x, fogColor.y, fogColor.z);
			GL33.glUniform1f(mainShaderUniforms.fogDensity, scene.getFogDensity());
		} else {
			GL33.glUniform1i(mainShaderUniforms.fogEnabled, 0);
		}

		GL33.glUniform4f(mainShaderUniforms.selectionColor, selectionColor.x, selectionColor.y,
				selectionColor.z, selectionColor.w);

		GL33.glUniform1f(mainShaderUniforms.ambientLightFactor, config.getVisualEffects().getAmbientLightFactor());
		GL33.glUniform1i(mainShaderUniforms.enableRoughnessBump, config.getQuality().isRoughnessBumpEnabled() ? 1 : 0);
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
		GL33.glActiveTexture(GL33.GL_TEXTURE0);
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture);
		GL33.glUniform1i(screenQuadShader.getUniformLocation("screenTexture"), 0);

		GL33.glBindVertexArray(screenQuadVAO);
		GL33.glDrawArrays(GL_TRIANGLES, 0, 6);
		GL33.glBindVertexArray(0);

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

		int presentBuffer = GL33.glGetInteger(GL_DOUBLEBUFFER) != 0 ? GL_BACK : GL_FRONT;

		GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
		GL33.glDrawBuffer(presentBuffer);
		GL33.glDepthMask(true);
		GL33.glColorMask(true, true, true, true);
		glViewport(0, 0, screenWidth, screenHeight);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_SCISSOR_TEST);
		glDisable(GL_STENCIL_TEST);
		GL33.glDisable(GL33.GL_BLEND);
		GL33.glDisable(GL_CULL_FACE);
		GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, GL33.GL_FILL);
		// Disable automatic sRGB conversion — apply it manually in the shader instead,
		// so behaviour is consistent regardless of whether the default framebuffer is
		// sRGB-capable (it is silently ignored on many Linux/GLX drivers).
		glDisable(GL_FRAMEBUFFER_SRGB);

		screenQuadShader.use();
		GL33.glActiveTexture(GL33.GL_TEXTURE0);
		GL33.glBindTexture(GL33.GL_TEXTURE_2D, resolvedTextureId);
		GL33.glUniform1i(screenQuadShader.getUniformLocation("screenTexture"), 0);
		GL33.glUniform1i(screenQuadShader.getUniformLocation("applyGammaCorrection"), 1);

		GL33.glBindVertexArray(screenQuadVAO);
		GL33.glDrawArrays(GL_TRIANGLES, 0, 6);
		GL33.glBindVertexArray(0);

		// Reset uniform and restore sRGB state for subsequent intermediate passes
		GL33.glUniform1i(screenQuadShader.getUniformLocation("applyGammaCorrection"), 0);
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
			GL33.glDeleteBuffers(screenQuadVBO);
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

	private int getRequestedSceneSampleCount() {
		if (!config.getQuality().isFXAAEnabled()) {
			return 0;
		}

		String override = System.getProperty("openrocket.figure3d.msaaSamples");
		if (override != null) {
			try {
				return Math.max(0, Integer.parseInt(override.trim()));
			} catch (NumberFormatException ignored) {
				// fall through
			}
		}

		return DEFAULT_SCENE_MSAA_SAMPLES;
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
