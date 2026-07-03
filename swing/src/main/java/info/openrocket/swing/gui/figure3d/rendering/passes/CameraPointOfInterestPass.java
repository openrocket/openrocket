package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.core.geometry.components.CameraPointOfInterestGenerator;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.gui.theme.UITheme;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * Renders a fixed-size marker at the camera center-of-interest.
 */
public class CameraPointOfInterestPass implements RenderPass {

	private static final float FIXED_SCREEN_SCALE = 25.0f;
	private int viewportHeight = 1;

	private final GLShader shader;
	private final Renderable markerMesh;
	private final RenderingConfiguration config;
	private final Vector3f markerColor = new Vector3f(0.2f, 0.8f, 1.0f);
	private final Runnable uiThemeListener;

	public CameraPointOfInterestPass(RenderingConfiguration config) {
		this.config = config;
		try {
			shader = new GLShader("/shaders/billboard_vertex.glsl", "/shaders/billboard_fragment.glsl");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.markerMesh = new GLRenderableMesh(CameraPointOfInterestGenerator.create());
		updateColorFromTheme();
		this.uiThemeListener = this::updateColorFromTheme;
		UITheme.Theme.addUIThemeChangeListener(uiThemeListener);
	}

	@Override
	public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		if (!config.getVisualEffects().isCameraPointOfInterestVisible()) {
			return;
		}

		Camera camera = scene.getCamera();
		if (camera == null) {
			return;
		}

		glDisable(GL_DEPTH_TEST);
		shader.use();
		shader.setUniformMatrix4f("projectionMatrix", projectionMatrix);
		shader.setUniformMatrix4f("viewMatrix", viewMatrix);
		shader.setUniformVector3f("center", camera.getCenterOfInterest());
		shader.setUniformVector3f("color", markerColor);
		shader.setUniformFloat("scaleWithView", 0.0f);
		shader.setUniformFloat("fixedScaleFactor", FIXED_SCREEN_SCALE);
		shader.setUniformFloat("viewportHeight", (float) viewportHeight);
		markerMesh.render();
		glEnable(GL_DEPTH_TEST);
	}

	@Override
	public void resize(int width, int height) {
		this.viewportHeight = Math.max(1, height);
	}

	@Override
	public void cleanup() {
		shader.cleanup();
		markerMesh.cleanup();
		UITheme.Theme.removeUIThemeChangeListener(uiThemeListener);
	}

	private void updateColorFromTheme() {
		Color color = UITheme.getColor(UITheme.Keys.INFO);
		if (color != null) {
			markerColor.set(ColorUtils.srgbToLinear(new Vector3f(
					color.getRed() / 255.0f,
					color.getGreen() / 255.0f,
					color.getBlue() / 255.0f)));
		}
	}
}
