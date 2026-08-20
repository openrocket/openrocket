package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.rendering.DefaultMaterialBinder;
import info.openrocket.swing.gui.figure3d.rendering.FullscreenQuad;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.MainShaderUniforms;
import info.openrocket.swing.gui.figure3d.rendering.OffscreenRenderTarget;
import info.openrocket.swing.gui.figure3d.rendering.TextureBinder;
import info.openrocket.swing.gui.figure3d.rendering.TransparencyPolicy;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory.ComponentAppearanceRole;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL33.glUniform1i;

/**
 * Renders opaque geometry first, then translucent geometry with weighted blended
 * order-independent transparency, and finally opaque objects marked "render on top".
 *
 * <p>The translucent pass accumulates fragments on the GPU. It therefore remains stable
 * when objects intersect or the camera moves through a point where a CPU depth sort would
 * reverse two objects.</p>
 */
public class GeometryPass implements RenderPass {

	private final GLShader mainShader;
	private final RenderingConfiguration config;
	private final TextureBinder textureStateManager;
	private final MainShaderUniforms mainShaderUniforms;
	private final OffscreenRenderTarget renderTarget;
	private final DefaultMaterialBinder materialBinder = new DefaultMaterialBinder();
	private final WeightedBlendedTransparency transparency;

	/**
	 * Creates a new geometry pass with the specified rendering components.
	 *
	 * @param mainShader The main shader program for geometry rendering
	 * @param config Rendering configuration for quality and display settings
	 * @param textureStateManager Manager for optimized texture state changes
	 * @param mainShaderUniforms Cached uniform locations for performance
	 * @param renderTarget Main scene target whose resolved depth is shared by transparency
	 * @param fullscreenQuad Shared full-screen geometry used to composite transparency
	 */
	public GeometryPass(GLShader mainShader, RenderingConfiguration config,
						TextureBinder textureStateManager, MainShaderUniforms mainShaderUniforms,
						OffscreenRenderTarget renderTarget, FullscreenQuad fullscreenQuad) {
		this.mainShader = mainShader;
		this.config = config;
		this.textureStateManager = textureStateManager;
		this.mainShaderUniforms = mainShaderUniforms;
		this.renderTarget = renderTarget;
		this.transparency = new WeightedBlendedTransparency(fullscreenQuad, textureStateManager);
	}

	/** Draws regular opaque objects into the primary (possibly multisampled) scene target. */
	@Override
	public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		mainShader.use();
		setTransparencyOutputMode(TransparencyOutputMode.SCENE_COLOR);
		glDisable(GL_BLEND);
		glEnable(GL_DEPTH_TEST);
		glDepthMask(true);

		for (SceneObject object : scene.getObjects()) {
			if (!object.isRenderOnTop() && !TransparencyPolicy.isTransparent(object, config)) {
				renderObject(object, false);
			}
		}

		// A translucent component can still contain texture pixels whose opacity is
		// independent of the component opacity. Render those pixels with depth writes
		// (and MSAA) so WBOIT cannot average geometry through them.
		setTransparencyOutputMode(TransparencyOutputMode.OPAQUE_TEXTURE_FRAGMENTS);
		try {
			for (SceneObject object : scene.getObjects()) {
				if (!object.isRenderOnTop() && mayProduceOpaqueTextureFragments(object)) {
					renderTransparentObject(object);
				}
			}
		} finally {
			setTransparencyOutputMode(TransparencyOutputMode.SCENE_COLOR);
		}
	}

	/**
	 * Draws and composites all translucent objects into the resolved scene target.
	 * The renderer must resolve opaque multisampling before calling this method.
	 */
	public void renderTransparent(SceneView scene) {
		if (hasTransparentObjects(scene)) {
			transparency.render(renderTarget, outputMode -> renderTransparentGeometry(scene, outputMode));
		}
		renderOpaqueOnTop(scene);
	}

	private boolean hasTransparentObjects(SceneView scene) {
		for (SceneObject object : scene.getObjects()) {
			if (TransparencyPolicy.isTransparent(object, config)) {
				return true;
			}
		}
		return false;
	}

	private boolean mayProduceOpaqueTextureFragments(SceneObject object) {
		return TransparencyPolicy.mayProduceOpaqueTextureFragments(
				object.getRocketComponent(), object.getAppearance(), config);
	}

	private void renderTransparentGeometry(SceneView scene, TransparencyOutputMode outputMode) {
		mainShader.use();
		setTransparencyOutputMode(outputMode);
		glDepthMask(false);
		glEnable(GL_DEPTH_TEST);
		try {
			for (SceneObject object : scene.getObjects()) {
				if (!object.isRenderOnTop() && TransparencyPolicy.isTransparent(object, config)) {
					renderTransparentObject(object);
				}
			}

			// No current translucent scene object uses this flag, but honour its contract
			// if one is introduced: it contributes without being rejected by opaque depth.
			glDisable(GL_DEPTH_TEST);
			for (SceneObject object : scene.getObjects()) {
				if (object.isRenderOnTop() && TransparencyPolicy.isTransparent(object, config)) {
					renderTransparentObject(object);
				}
			}
		} finally {
			glEnable(GL_DEPTH_TEST);
			setTransparencyOutputMode(TransparencyOutputMode.SCENE_COLOR);
		}
	}

	private void renderOpaqueOnTop(SceneView scene) {
		mainShader.use();
		glDisable(GL_BLEND);
		glDepthMask(true);
		glDisable(GL_DEPTH_TEST);
		try {
			// Opaque texture coverage was excluded from WBOIT. Draw it last for the
			// (currently unused) combination of transparency and render-on-top.
			setTransparencyOutputMode(TransparencyOutputMode.OPAQUE_TEXTURE_FRAGMENTS);
			for (SceneObject object : scene.getObjects()) {
				if (object.isRenderOnTop() && mayProduceOpaqueTextureFragments(object)) {
					renderTransparentObject(object);
				}
			}

			setTransparencyOutputMode(TransparencyOutputMode.SCENE_COLOR);
			for (SceneObject object : scene.getObjects()) {
				if (object.isRenderOnTop() && !TransparencyPolicy.isTransparent(object, config)) {
					renderObject(object, false);
				}
			}
		} finally {
			setTransparencyOutputMode(TransparencyOutputMode.SCENE_COLOR);
			glEnable(GL_DEPTH_TEST);
		}
	}

	private void renderTransparentObject(SceneObject object) {
		if (TransparencyPolicy.isTransparentBodyComponent(object.getRocketComponent())) {
			renderTransparentBodyObject(object);
			return;
		}
		renderObject(object, false);
	}

	/**
	 * Curved shells need both front and back faces in the accumulation. Their dedicated
	 * inside-wall triangles remain hidden to avoid counting physical wall thickness twice.
	 */
	private void renderTransparentBodyObject(SceneObject object) {
		boolean cullWasEnabled = glIsEnabled(GL_CULL_FACE);
		if (cullWasEnabled) {
			glDisable(GL_CULL_FACE);
		}
		try {
			// A dedicated secondary partition intentionally contains tube interiors or a
			// fin's right face. Respect that material instead of applying the legacy
			// whole-body rule that suppresses inner-wall triangles during transparency.
			boolean forceHideInnerSurfaces = object.getAppearanceRole() != ComponentAppearanceRole.SECONDARY;
			renderObject(object, forceHideInnerSurfaces);
		} finally {
			if (cullWasEnabled) {
				glEnable(GL_CULL_FACE);
			}
		}
	}

	/** Renders one object after applying its material and display-mode overrides. */
	private void renderObject(SceneObject object, boolean forceHideInnerSurfaces) {
		materialBinder.bind(object, mainShader, mainShaderUniforms, config, textureStateManager);
		if (forceHideInnerSurfaces) {
			glUniform1i(mainShaderUniforms.hideInnerSurfaces, 1);
		}

		object.getRenderableMesh().render();
	}

	private void setTransparencyOutputMode(TransparencyOutputMode outputMode) {
		glUniform1i(mainShaderUniforms.transparencyOutputMode, outputMode.getShaderValue());
	}

	@Override
	public void resize(int width, int height) {
		transparency.invalidateAttachments();
	}

	@Override
	public void cleanup() {
		transparency.cleanup();
		materialBinder.cleanup();
	}
}
