package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.rendering.passes.AmbientOcclusionPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.BackgroundPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.CameraPointOfInterestPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.CaretsPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.FXAAPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.GeometryPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.MotionBlurPass;
import info.openrocket.swing.gui.figure3d.rendering.passes.OutlinePass;
import info.openrocket.swing.gui.figure3d.rendering.passes.ScreenTexturePass;
import info.openrocket.swing.gui.figure3d.rendering.passes.ShadowPass;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
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
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Main OpenGL 3.1 renderer for a figure3d scene.
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

	// Shader resource paths
	private static final String MAIN_VERTEX_SHADER_PATH = "/shaders/vertex.glsl";
	private static final String MAIN_FRAGMENT_SHADER_PATH = "/shaders/fragment.glsl";

	private final GLShader mainShader;
	private final Vector4f selectionColor = ColorUtils.srgbToLinear(new org.joml.Vector4f(1.0f, 0.2f, 0.1f, 1.0f));
	private final Vector3f blurRocketAxis = new Vector3f();
	private final Vector4f blurOrigin = new Vector4f();
	private final Vector4f blurTip = new Vector4f();
	private final Matrix4f blurViewProjection = new Matrix4f();

	// Performance optimizations
	private final TextureBinder textureStateManager = new TextureStateManager();

	private final GpuMemoryProfile gpuMemoryProfile;
	private int screenWidth;
	private int screenHeight;
	private final RenderingConfiguration config;

	private final FullscreenQuad fullscreenQuad;

	// Cached uniform locations
	private final MainShaderUniforms mainShaderUniforms;

	private final ParticleRenderer particleRenderer;
	private final VolumetricSmokeRenderer volumetricSmokeRenderer;
	private final FlameRenderer flameRenderer;
	private final CaretsPass caretsPass;
	private final CameraPointOfInterestPass cameraPointOfInterestPass;
	private final ShadowPass shadowPass;
	private final SceneLighting sceneLighting;
	private final AmbientOcclusionPass ambientOcclusionPass;
	private final OutlinePass outlinePass;
	private final FXAAPass fxaaPass;

	private final OffscreenRenderTarget renderTarget;
	private int resolvedTextureId;

	private final BackgroundPass backgroundPass;
	private final GeometryPass geometryPass;
	private final List<ScreenTexturePass> postProcessingPasses = new ArrayList<>();
	private final MotionBlurPass motionBlurPass;
	private volatile boolean interactionMode = false;

	/**
	 * @param config rendering configuration
	 * @param rocket rocket model to render
	 * @param initialWidth initial framebuffer width
	 * @param initialHeight initial framebuffer height
	 * @throws ShaderException if shader compilation fails
	 */
	public RealisticRenderer(RenderingConfiguration config, Rocket rocket, int initialWidth, int initialHeight) {
		this.config = config;
		this.screenWidth = initialWidth;
		this.screenHeight = initialHeight;

		// Detected before any target is allocated, because it decides how large
		// the scene target and the shadow map are allowed to be.
		this.gpuMemoryProfile = GpuMemoryProfile.detect();
		log.info("3D render memory profile: {}", gpuMemoryProfile);

		// Main shader for scene objects
		mainShader = new GLShader(MAIN_VERTEX_SHADER_PATH, MAIN_FRAGMENT_SHADER_PATH);
		mainShaderUniforms = new MainShaderUniforms(mainShader);
		fullscreenQuad = new FullscreenQuad();

		particleRenderer = new ParticleRenderer();
		volumetricSmokeRenderer = new VolumetricSmokeRenderer();
		flameRenderer = new FlameRenderer();

		renderTarget = new OffscreenRenderTarget(initialWidth, initialHeight, getRequestedSceneSampleCount());
		resolvedTextureId = renderTarget.getColorTextureId();

		shadowPass = createShadowPass(initialWidth, initialHeight);
		sceneLighting = new SceneLighting(mainShader, mainShaderUniforms, textureStateManager,
				shadowPass, config);
		backgroundPass = new BackgroundPass(textureStateManager);
		geometryPass = new GeometryPass(mainShader, config, textureStateManager, mainShaderUniforms,
				renderTarget, fullscreenQuad);
		caretsPass = createCaretsPass(rocket, initialWidth, initialHeight);
		cameraPointOfInterestPass = createCameraPointOfInterestPass(initialWidth, initialHeight);

		// Post-processing passes
		ambientOcclusionPass = new AmbientOcclusionPass(fullscreenQuad, textureStateManager,
				config.getQuality(), initialWidth, initialHeight);
		motionBlurPass = createMotionBlurPass(initialWidth, initialHeight);
		outlinePass = new OutlinePass(mainShader, mainShaderUniforms, textureStateManager,
				fullscreenQuad, selectionColor, initialWidth, initialHeight);
		fxaaPass = new FXAAPass(fullscreenQuad, initialWidth, initialHeight);

		// Apply initial settings
		updatePostProcessingChain();

		GLErrors.check("renderer initialization");

		// Add a configuration listener
		config.addListener(this::onScenePropertiesChanged);
	}

	private ShadowPass createShadowPass(int width, int height) {
		ShadowPass pass = new ShadowPass(width, height, gpuMemoryProfile.isConstrained());
		pass.setQuality(config.getQuality().getQuality());
		pass.setEnabled(config.getQuality().isShadowsEnabled());
		return pass;
	}

	private CaretsPass createCaretsPass(Rocket rocket, int width, int height) {
		CaretsPass pass = new CaretsPass(rocket, config);
		pass.resize(width, height);
		return pass;
	}

	private CameraPointOfInterestPass createCameraPointOfInterestPass(int width, int height) {
		CameraPointOfInterestPass pass = new CameraPointOfInterestPass(config);
		pass.resize(width, height);
		return pass;
	}

	private MotionBlurPass createMotionBlurPass(int width, int height) {
		MotionBlurPass pass = new MotionBlurPass(fullscreenQuad, width, height);
		pass.setBlurFactor(config.getVisualEffects().getMotionBlurFactor());
		return pass;
	}

	@Override
	public void render(SceneView scene, boolean renderBackground) {
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
		SceneLighting.updateFlameLights(scene);
		// Rebuilding a complex shadow map for every mouse event can keep weaker
		// Windows drivers continuously saturated. The first idle frame recreates it.
		shadowPass.setEnabled(config.getQuality().isShadowsEnabled() && !shouldReduceInteractionEffects());
		shadowPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
		glViewport(0, 0, screenWidth, screenHeight);

		// Prepare the main shader with uniforms that are constant for the frame
		sceneLighting.bindFrameUniforms(scene, camera, cameraViewMatrix, cameraProjectionMatrix,
				shouldReduceInteractionEffects());

		// 1. Render the geometry passes to the main FBO
		renderSceneToTarget(scene, renderBackground, camera, cameraViewMatrix, cameraProjectionMatrix);

		// Particle renderers bind textures directly, so invalidate the cache before any
		// post-processing pass that relies on TextureStateManager.
		textureStateManager.reset();

		// 2. Run the post-processing chain
		int finalTexture = runPostProcessingChain(scene, cameraViewMatrix, cameraProjectionMatrix);
		resolveFinalTexture(finalTexture);

		GLErrors.debugCheck("frame render");
	}

	/**
	 * Sets the fixed-function GL state (depth testing, face culling) for the frame.
	 */
	private void prepareFrameGLState() {
		glEnable(GL_DEPTH_TEST);
		if (config.getQuality().isBackfaceCullingEnabled()
				&& config.getDisplay().getMode() != DisplaySettings.RenderMode.XRAY) {
			glEnable(GL_CULL_FACE);
			glCullFace(GL_BACK);
		} else {
			glDisable(GL_CULL_FACE);
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

		if (renderBackground) {
			backgroundPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
		}
		geometryPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);

		// Resolve opaque MSAA before transparency. The order-independent attachments
		// are intentionally single-sample and share this resolved depth texture,
		// avoiding two large multisampled transparency buffers.
		renderTarget.unbind();
		renderTarget.bindResolved();
		geometryPass.renderTransparent(scene);

		particleRenderer.render(scene, camera);
		volumetricSmokeRenderer.render(scene, camera);
		flameRenderer.render(scene, camera);
		if (config.getVisualEffects().areCaretsVisible()) {
			caretsPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
		}
		cameraPointOfInterestPass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
		renderTarget.unbindResolved();
	}

	private void clearRenderTarget(SceneView scene, boolean renderBackground) {
		if (!renderBackground) {
			if (scene.getBackground() instanceof SolidColorBackground solidBackground) {
				Vector4f color = solidBackground.getColor();
				// The off-screen pipeline uses premultiplied linear RGB while blending and
				// resolving MSAA. An unassociated clear color would otherwise survive under
				// zero alpha and bleed the invisible matte into anti-aliased edges.
				glClearColor(color.x * color.w, color.y * color.w, color.z * color.w, color.w);
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

		for (ScreenTexturePass pass : postProcessingPasses) {
			// Outline pass is screen-space and full-frame; skip during interaction.
			if (reduceInteractionEffects && pass instanceof OutlinePass) {
				continue;
			}
			pass.setInputTexture(currentTexture);
			pass.render(scene, cameraViewMatrix, cameraProjectionMatrix);
			currentTexture = pass.getOutputTexture();
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

	private void resolveFinalTexture(int currentTexture) {
		if (currentTexture == 0) {
			resolvedTextureId = 0;
			return;
		}

		if (currentTexture == renderTarget.getColorTextureId()) {
			resolvedTextureId = currentTexture;
			return;
		}

		fullscreenQuad.copyTextureTo(renderTarget, currentTexture, screenWidth, screenHeight);
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
		fullscreenQuad.present(resolvedTextureId, screenWidth, screenHeight);
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

		backgroundPass.resize(width, height);
		geometryPass.resize(width, height);
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
	public void setDisplayScale(float displayScale) {
		caretsPass.setDisplayScale(displayScale);
	}

	@Override
	public void resetTextureState() {
		textureStateManager.reset();
	}

	@Override
	public void cleanup() {
		particleRenderer.cleanup();
		flameRenderer.cleanup();
		volumetricSmokeRenderer.cleanup();

		caretsPass.cleanup();
		cameraPointOfInterestPass.cleanup();
		shadowPass.cleanup();

		backgroundPass.cleanup();
		geometryPass.cleanup();
		ambientOcclusionPass.cleanup();
		outlinePass.cleanup();
		fxaaPass.cleanup();
		motionBlurPass.cleanup();
		fullscreenQuad.cleanup();
		mainShader.cleanup();

		renderTarget.cleanup();

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
		// An explicit override is a diagnostic knob and deliberately outranks the user
		// preference, quality level and memory profile, so a constrained device can
		// still be asked for 8x.
		String override = System.getProperty("openrocket.figure3d.msaaSamples");
		if (override != null) {
			try {
				return Math.max(0, Integer.parseInt(override.trim()));
			} catch (NumberFormatException ignored) {
				// fall through
			}
		}

		// Scene multisampling has its own preference. When enabled, the quality level
		// selects its sample count independently of the FXAA post-process.
		int requested = config.getQuality().getSceneSampleCount();
		return gpuMemoryProfile.isConstrained()
				? Math.min(requested, CONSTRAINED_SCENE_MSAA_SAMPLES)
				: Math.min(requested, DEFAULT_SCENE_MSAA_SAMPLES);
	}

}
