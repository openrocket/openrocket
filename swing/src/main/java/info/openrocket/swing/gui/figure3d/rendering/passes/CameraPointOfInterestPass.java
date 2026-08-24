package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.swing.gui.figure3d.geometry.components.CameraPointOfInterestGenerator;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
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
	private final int projectionUniform;
	private final int viewUniform;
	private final int centerUniform;
	private final int colorUniform;
	private final int scaleWithViewUniform;
	private final int fixedScaleFactorUniform;
	private final int viewportHeightUniform;
	private final Renderable markerMesh;
	private final RenderingConfiguration config;
	private final Vector3f markerColor = new Vector3f(0.2f, 0.8f, 1.0f);
	private final Runnable uiThemeListener;

	public CameraPointOfInterestPass(RenderingConfiguration config) {
		this.config = config;
		shader = new GLShader("/shaders/billboard_vertex.glsl", "/shaders/billboard_fragment.glsl");
		projectionUniform = shader.requireUniformLocation("projectionMatrix");
		viewUniform = shader.requireUniformLocation("viewMatrix");
		centerUniform = shader.requireUniformLocation("center");
		colorUniform = shader.requireUniformLocation("color");
		scaleWithViewUniform = shader.requireUniformLocation("scaleWithView");
		fixedScaleFactorUniform = shader.requireUniformLocation("fixedScaleFactor");
		viewportHeightUniform = shader.requireUniformLocation("viewportHeight");
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
		shader.setUniformMatrix4f(projectionUniform, projectionMatrix);
		shader.setUniformMatrix4f(viewUniform, viewMatrix);
		shader.setUniformVector3f(centerUniform, camera.getCenterOfInterest());
		shader.setUniformVector3f(colorUniform, markerColor);
		shader.setUniformFloat(scaleWithViewUniform, 0.0f);
		shader.setUniformFloat(fixedScaleFactorUniform, FIXED_SCREEN_SCALE);
		shader.setUniformFloat(viewportHeightUniform, (float) viewportHeight);
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
