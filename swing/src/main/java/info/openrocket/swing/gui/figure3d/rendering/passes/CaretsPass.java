package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.masscalc.RigidBody;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.core.util.MathUtil;
import info.openrocket.core.util.StateChangeListener;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.components.CGCaretGenerator;
import info.openrocket.swing.gui.figure3d.geometry.components.CPCaretGenerator;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import info.openrocket.swing.gui.figure3d.utils.VectorUtils;
import info.openrocket.swing.gui.theme.UITheme;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicReference;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * Draws the centre of gravity and centre of pressure markers.
 *
 * <p>Both are billboards, so they face the camera whatever the view, and both are
 * drawn with depth testing off so they stay visible through the rocket. Their
 * positions come from the host where there is one, and are otherwise computed
 * here from the mass calculator and the Barrowman calculator; see
 * {@link #setPositions}.</p>
 */
public class CaretsPass implements RenderPass {

	private final GLShader shader;
	private final int projectionUniform;
	private final int viewUniform;
	private final int scaleWithViewUniform;
	private final int fixedScaleFactorUniform;
	private final int viewportHeightUniform;
	private final int centerUniform;
	private final int colorUniform;
	private final Renderable cgMesh;
	private final Renderable cpMesh;
	private volatile Vector3f cgColor = new Vector3f(0.0f, 0.0f, 1.0f);
	private volatile Vector3f cpColor = new Vector3f(1.0f, 0.0f, 0.0f);
	private final Vector3f transformedCgPosition = new Vector3f();
	private final Vector3f transformedCpPosition = new Vector3f();
	private final AerodynamicCalculator aerodynamicCalculator;
	private final RenderingConfiguration config;

	private final Rocket rocket;
	private final AtomicReference<CaretPositions> positions =
			new AtomicReference<>(new CaretPositions(null, null, false));
	/**
	 * Caret size in logical pixels, multiplied by the display scale before use.
	 *
	 * It used to be applied in framebuffer pixels, which made the markers a constant number
	 * of physical pixels: half the intended size on a HiDPI screen and full size on an
	 * ordinary one, so the same design looked noticeably larger on a non-scaled display.
	 * The value here is half the old constant, which leaves a 2x display looking as it did
	 * and brings everything else down to match it.
	 */
	private static final float FIXED_SCREEN_SCALE = 21.0f;
	private int viewportHeight = 1;
	private float displayScale = 1.0f;
	private final Runnable uiThemeListener;
	private final StateChangeListener rocketChangeListener;

	private record CaretPositions(Vector3f cg, Vector3f cp, boolean suppliedByHost) {
	}

	/**
	 * Creates a new carets pass for the given scene and configuration.
	 *
	 * Initializes the aerodynamic calculator, creates caret geometry, and sets up
	 * automatic position updates when the rocket configuration changes.
	 *
	 * @param rocket The rocket to analyze
	 * @param config Rendering configuration for quality settings
	 */
	public CaretsPass(Rocket rocket, RenderingConfiguration config) {
		this.rocket = rocket;
		this.config = config;
		shader = new GLShader("/shaders/billboard_vertex.glsl", "/shaders/billboard_fragment.glsl");
		projectionUniform = shader.requireUniformLocation("projectionMatrix");
		viewUniform = shader.requireUniformLocation("viewMatrix");
		scaleWithViewUniform = shader.requireUniformLocation("scaleWithView");
		fixedScaleFactorUniform = shader.requireUniformLocation("fixedScaleFactor");
		viewportHeightUniform = shader.requireUniformLocation("viewportHeight");
		centerUniform = shader.requireUniformLocation("center");
		colorUniform = shader.requireUniformLocation("color");
		this.aerodynamicCalculator = new BarrowmanCalculator();
		this.cgMesh = new GLRenderableMesh(CGCaretGenerator.create(config.getQuality().getQuality()));
		this.cpMesh = new GLRenderableMesh(CPCaretGenerator.create(config.getQuality().getQuality()));
		updateColorsFromTheme();
		this.uiThemeListener = this::updateColorsFromTheme;
		UITheme.Theme.addUIThemeChangeListener(uiThemeListener);

		// Add a listener to update positions when the rocket changes
		this.rocketChangeListener = e -> updatePositions();
		this.rocket.addChangeListener(rocketChangeListener);
		// Initial calculation
		updatePositions();
	}

	/**
	 * Places the carets at positions computed by the host rather than by this pass.
	 *
	 * <p>The design view already computes both, under the flight conditions the user has
	 * selected, and shows them in the 2D figure and the info overlay. Taking those values
	 * keeps all three in agreement; recomputing here produced a caret that ignored any
	 * Mach, angle-of-attack or roll override and disagreed with the numbers beside it.</p>
	 *
	 * @param cg centre of gravity, or {@code null}/NaN when there is nothing to show
	 * @param cp centre of pressure, or {@code null}/NaN when there is nothing to show
	 */
	public void setPositions(CoordinateIF cg, CoordinateIF cp) {
		Vector3f cgPosition = cg != null && !cg.isNaN() && cg.getWeight() > MassCalculator.MIN_MASS
				? VectorUtils.coordinateToVector3f(cg).mul(RenderingConstants.WORLD_SCALE)
				: null;
		Vector3f cpPosition = cp != null && !cp.isNaN() && cp.getWeight() > MathUtil.EPSILON
				? VectorUtils.coordinateToVector3f(cp).mul(RenderingConstants.WORLD_SCALE)
				: null;
		positions.set(new CaretPositions(cgPosition, cpPosition, true));
	}

	/**
	 * Recalculates and caches the CG and CP positions for the current rocket configuration,
	 * using default flight conditions.
	 *
	 * <p>Only used when no host has supplied positions — a standalone scene with no design
	 * view behind it. Where there is one, {@link #setPositions} wins, because the conditions
	 * the centre of pressure depends on live there.</p>
	 */
	private void updatePositions() {
		CaretPositions previous = positions.get();
		if (previous.suppliedByHost()) {
			return;
		}
		FlightConfiguration config = rocket.getSelectedConfiguration();
		if (config == null) {
			positions.compareAndSet(previous, new CaretPositions(null, null, false));
			return;
		}

		// Calculate and cache CG position
		RigidBody cgBody = MassCalculator.calculateLaunch(config);
		CoordinateIF cgCoord = cgBody.getCM();
		Vector3f cgPosition = cgBody.getMass() > MassCalculator.MIN_MASS
				? VectorUtils.coordinateToVector3f(cgCoord).mul(RenderingConstants.WORLD_SCALE)
				: null;

		// Calculate and cache CP position
		FlightConditions conditions = new FlightConditions(config);
		CoordinateIF cpCoord = aerodynamicCalculator.getWorstCP(config, conditions, new WarningSet());
		Vector3f cpPosition = cpCoord != null
				? VectorUtils.coordinateToVector3f(cpCoord).mul(RenderingConstants.WORLD_SCALE)
				: null;
		// Do not let an in-flight fallback calculation overwrite positions supplied by
		// the host while this calculation was running on another thread.
		positions.compareAndSet(previous, new CaretPositions(cgPosition, cpPosition, false));
	}

	@Override
	public void render(SceneView scene, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
		glDisable(GL_DEPTH_TEST);
		shader.use();
		shader.setUniformMatrix4f(projectionUniform, projectionMatrix);
		shader.setUniformMatrix4f(viewUniform, viewMatrix);
		shader.setUniformFloat(scaleWithViewUniform,
				config.getVisualEffects().isCaretScaleWithView() ? 1.0f : 0.0f);
		shader.setUniformFloat(fixedScaleFactorUniform, FIXED_SCREEN_SCALE * displayScale);
		shader.setUniformFloat(viewportHeightUniform, (float) viewportHeight);

		CaretPositions currentPositions = positions.get();
		if (currentPositions.cg() != null) {
			// Render CG
			shader.setUniformVector3f(centerUniform,
					scene.transformRocketPoint(currentPositions.cg(), transformedCgPosition));
			shader.setUniformVector3f(colorUniform, cgColor);
			cgMesh.render();
		}

		if (currentPositions.cp() != null) {
			// Render CP
			shader.setUniformVector3f(centerUniform,
					scene.transformRocketPoint(currentPositions.cp(), transformedCpPosition));
			shader.setUniformVector3f(colorUniform, cpColor);
			cpMesh.render();
		}

		glEnable(GL_DEPTH_TEST);
	}

	@Override
	public void resize(int width, int height) {
		this.viewportHeight = Math.max(1, height);
	}

	/**
	 * Sets how many framebuffer pixels the display packs into one logical pixel, so the
	 * carets keep the same apparent size whatever the screen's scaling.
	 *
	 * @param displayScale framebuffer pixels per logical pixel; 1.0 on a non-scaled display
	 */
	public void setDisplayScale(float displayScale) {
		this.displayScale = displayScale > 0.0f ? displayScale : 1.0f;
	}

	@Override
	public void cleanup() {
		shader.cleanup();
		cgMesh.cleanup();
		cpMesh.cleanup();
		rocket.removeChangeListener(rocketChangeListener);
		UITheme.Theme.removeUIThemeChangeListener(uiThemeListener);
	}

	private void updateColorsFromTheme() {
		Color cgTheme = UITheme.getColor(UITheme.Keys.CG);
		Color cpTheme = UITheme.getColor(UITheme.Keys.CP);
		if (cgTheme != null) {
			cgColor = ColorUtils.srgbToLinear(new Vector3f(
					cgTheme.getRed() / 255.0f,
					cgTheme.getGreen() / 255.0f,
					cgTheme.getBlue() / 255.0f));
		}
		if (cpTheme != null) {
			cpColor = ColorUtils.srgbToLinear(new Vector3f(
					cpTheme.getRed() / 255.0f,
					cpTheme.getGreen() / 255.0f,
					cpTheme.getBlue() / 255.0f));
		}
	}
}
