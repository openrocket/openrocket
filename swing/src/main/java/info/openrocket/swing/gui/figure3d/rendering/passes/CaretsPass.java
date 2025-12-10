package info.openrocket.swing.gui.figure3d.rendering.passes;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.aerodynamics.BarrowmanCalculator;
import info.openrocket.core.aerodynamics.FlightConditions;
import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.masscalc.MassCalculator;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.core.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.core.geometry.components.CGCaretGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.components.CPCaretGenerator;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import info.openrocket.swing.gui.figure3d.rendering.RenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.Shader;
import info.openrocket.swing.gui.figure3d.rendering.ShaderProgram;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.utils.VectorUtils;
import info.openrocket.swing.gui.figure3d.window.WindowManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;

/**
 * Specialized render pass for aerodynamic and mass property visualization carets.
 * 
 * This pass renders visual indicators for the Center of Gravity (CG) and Center of
 * Pressure (CP) positions in rocket simulations. These carets provide critical
 * visual feedback for rocket stability analysis and design validation.
 * 
 * Key features:
 * - **Real-time Calculation**: Automatically updates CG/CP positions when rocket configuration changes
 * - **Aerodynamic Integration**: Uses Barrowman calculator for accurate CP computation
 * - **Mass Calculation**: Integrates with OpenRocket's mass calculator for precise CG location
 * - **Billboard Rendering**: Carets always face the camera for optimal visibility
 * - **Color Coding**: Blue for CG (mass-related), Red for CP (aerodynamic-related)
 * - **Scale Awareness**: Positions scaled to match world coordinate system
 * 
 * Technical implementation:
 * - Listens for rocket configuration changes to update positions dynamically
 * - Renders carets without depth testing to ensure visibility through geometry
 * - Uses specialized billboard shaders for orientation-independent display
 * - Integrates with OpenRocket's flight configuration system
 * 
 * The carets are essential tools for rocket designers to:
 * - Verify static stability margins
 * - Analyze the effect of component changes on balance
 * - Visualize aerodynamic center progression
 * - Validate design configurations before flight
 */
public class CaretsPass implements RenderPass {

    private final ShaderProgram shader;
    private final Renderable cgMesh;
    private final Renderable cpMesh;
    private final Vector3f cgColor = new Vector3f(0.0f, 0.0f, 1.0f); // Blue
    private final Vector3f cpColor = new Vector3f(1.0f, 0.0f, 0.0f); // Red
    private final AerodynamicCalculator aerodynamicCalculator;

    private final Rocket rocket;
    private Vector3f cgPosition = new Vector3f();
    private Vector3f cpPosition = new Vector3f();

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
        try {
            shader = new Shader("/shaders/billboard_vertex.glsl", "/shaders/billboard_fragment.glsl");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.aerodynamicCalculator = new BarrowmanCalculator();
        this.cgMesh = new RenderableMesh(CGCaretGenerator.create(config.getQuality().getQuality()));
        this.cpMesh = new RenderableMesh(CPCaretGenerator.create(config.getQuality().getQuality()));

        // Add a listener to update positions when the rocket changes
        this.rocket.addChangeListener(e -> updatePositions());
        // Initial calculation
        updatePositions();
    }

    /**
     * Recalculates and caches the CG and CP positions for the current rocket configuration.
     * 
     * This method is called automatically when rocket configuration changes or can be
     * called manually to refresh the calculations. Uses OpenRocket's mass and aerodynamic
     * calculators to determine accurate positions.
     */
    private void updatePositions() {
        FlightConfiguration config = rocket.getSelectedConfiguration();
        if (config == null) {
            return;
        }

        // Calculate and cache CG position
        CoordinateIF cgCoord = MassCalculator.calculateLaunch(config).getCM();
        this.cgPosition = VectorUtils.coordinateToVector3f(cgCoord).mul(RocketMeshBuilder.WORLD_SCALE);

        // Calculate and cache CP position
        FlightConditions conditions = new FlightConditions(config);
		CoordinateIF cpCoord = aerodynamicCalculator.getWorstCP(config, conditions, new WarningSet());
        this.cpPosition = VectorUtils.coordinateToVector3f(cpCoord).mul(RocketMeshBuilder.WORLD_SCALE);
    }

    @Override
    public void render(SceneView scene, WindowManager windowManager, Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        glDisable(GL_DEPTH_TEST);
        shader.use();
        shader.setUniform("projectionMatrix", projectionMatrix);
        shader.setUniform("viewMatrix", viewMatrix);

        // Render CG
        shader.setUniform("center", cgPosition);
        shader.setUniform("color", cgColor);
        cgMesh.render();

        // Render CP
        shader.setUniform("center", cpPosition);
        shader.setUniform("color", cpColor);
        cpMesh.render();

        glEnable(GL_DEPTH_TEST);
    }

    @Override
    public void resize(int width, int height) {
        // No-op
    }

    @Override
    public void cleanup() {
        shader.cleanup();
        cgMesh.cleanup();
        cpMesh.cleanup();
    }
}
