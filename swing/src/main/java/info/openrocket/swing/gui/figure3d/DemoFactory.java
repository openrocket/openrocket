package info.openrocket.swing.gui.figure3d;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.defaults.DefaultAppearance;
import info.openrocket.core.appearance.defaults.FileDecalImage;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.motor.Manufacturer;
import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.motor.ThrustCurveMotor;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.core.geography.TerrainGenerator;
import info.openrocket.swing.gui.figure3d.input.KeyBindings;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.GradientBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.HDRIBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SkyboxBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.scene.core.Scene;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;

/**
 * A factory for creating demo content, separating it from the core engine logic.
 */
public class DemoFactory {

	private static final Logger log = LoggerFactory.getLogger(DemoFactory.class);
	private static final String RESOURCE_PREFIX = "swing/src/main/resources/";

	public final static FlightConfigurationId FCID = new FlightConfigurationId("d010716e-ce0e-469d-ae46-190f3653ebbf");

	private static String resolveResourcePath(String resourcePath) {
		File file = new File(resourcePath);
		if (file.exists()) {
			return file.getAbsolutePath();
		}

		String normalized = resourcePath.replace("\\", "/");
		if (normalized.startsWith(RESOURCE_PREFIX)) {
			normalized = normalized.substring(RESOURCE_PREFIX.length());
		}
		if (!normalized.startsWith("/")) {
			normalized = "/" + normalized;
		}

		try (InputStream stream = DemoFactory.class.getResourceAsStream(normalized)) {
			if (stream == null) {
				throw new IllegalArgumentException("Resource not found on classpath: " + resourcePath);
			}
			Path tempFile = Files.createTempFile("or-figure3d-", "-" + Paths.get(normalized).getFileName().toString());
			Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
			tempFile.toFile().deleteOnExit();
			return tempFile.toAbsolutePath().toString();
		} catch (IOException e) {
			throw new RuntimeException("Failed to materialize resource " + resourcePath, e);
		}
	}

	private static File resolveResourceFile(String resourcePath) {
		return new File(resolveResourcePath(resourcePath));
	}

	public static Rocket createTestRocket() {
		// --- ROCKET ---
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		Rocket rocket = document.getRocket();
		rocket.createFlightConfiguration(FCID);
		rocket.setSelectedConfiguration(FCID);
		AxialStage stage = rocket.getStage(0);

		// --- NOSE CONE ---
		NoseCone noseCone = new NoseCone();
		noseCone.setShapeType(Transition.Shape.OGIVE);
		noseCone.setShapeParameter(1.0);
		noseCone.setLength(0.15);
		noseCone.setBaseRadius(0.025);
		noseCone.setThickness(0.002);
		noseCone.setShoulderLength(0.02);
		noseCone.setShoulderRadius(0.01);
		noseCone.setShoulderThickness(0.002);

		stage.addChild(noseCone);

		// --- BODY TUBE ---
		BodyTube bodyTube = new BodyTube();
		bodyTube.setLength(0.2);
		bodyTube.setOuterRadius(0.025);
		bodyTube.setThickness(0.002);

		stage.addChild(bodyTube);

		bodyTube.setMotorMount(true);

		// --- MOTOR CONFIGURATION ---
		FlightConfigurationId fcid = rocket.getSelectedConfiguration().getId();
		MotorConfiguration motorConfig = new MotorConfiguration(bodyTube, fcid);
		Motor mtr = generateMotor_A8_18mm();
		motorConfig.setMotor(mtr);
		motorConfig.setEjectionDelay(0.0);
		bodyTube.setMotorConfig(motorConfig, fcid);

		// --- RAIL BUTTON ---
		RailButton railButton = new RailButton(0.01, 0.01);
		railButton.setAngleOffset(Math.PI / 2);
		railButton.setScrewHeight(0.003);

		bodyTube.addChild(railButton);

		// --- PARACHUTE ---
		Parachute parachute = new Parachute();
		parachute.setDiameter(0.045);
		parachute.setLength(0.05);

		bodyTube.addChild(parachute);
		parachute.setAxialOffset(0);

		// --- TRANSITION ---
		Transition transition = new Transition();
		transition.setLength(0.1);
		transition.setForeRadius(0.025);
		transition.setAftRadius(0.01);
		transition.setShapeType(Transition.Shape.OGIVE);
		transition.setForeShoulderRadius(0.01);
		//transition.setForeShoulderLength(0.01);
		transition.setForeShoulderThickness(0.002);
		transition.setForeShoulderCapped(true);
		transition.setAftShoulderRadius(0.005);
		transition.setAftShoulderLength(0.01);
		transition.setAftShoulderThickness(0.001);

		stage.addChild(transition);

		// --- FINSET OBJECT 1 ---
		FinSet finSet1 = createFinSet(bodyTube, 1);
		finSet1.setFilletRadius(0.02);
		finSet1.setThickness(0.01);
		finSet1.setAngleOffset(Math.PI / 4);

		// --- FINSET OBJECT 2 ---
		FinSet finSet2 = createFinSet(transition, 2);
		finSet2.setFilletRadius(0.005);
		finSet2.setTabHeight(0.0075);
		finSet2.setTabLength(0.05);
		finSet2.setThickness(0.01);

		// --- 2. Define Appearances ---
		Appearance noseConeAppearance = new Appearance(new ORColor(210, 50, 50), 0.8);
		Appearance bodyTubeAppearance = new Appearance(new ORColor(240, 240, 240), 0.5);
		Appearance transitionAppearance = new Appearance(new ORColor(240, 200, 240, 50), 0.5);
		Appearance finSet2Appearance = DefaultAppearance.getDefaultAppearance(finSet2);
		finSet2Appearance = new Appearance(finSet2Appearance.getPaint(), finSet2Appearance.getShine(),
				new Decal(
						new Coordinate(0, 0),
						new Coordinate(0, 0),
						new Coordinate(1, 1),
						0,
						new FileDecalImage(resolveResourceFile("/textures/test_texture.png")), Decal.EdgeMode.REPEAT));

		noseCone.setAppearance(noseConeAppearance);
		bodyTube.setAppearance(bodyTubeAppearance);
		transition.setAppearance(transitionAppearance);
		finSet1.setAppearance(noseConeAppearance);
		finSet2.setAppearance(finSet2Appearance);

		noseCone.setFinish(ExternalComponent.Finish.POLISHED);
		bodyTube.setFinish(ExternalComponent.Finish.NORMAL);
		finSet1.setFinish(ExternalComponent.Finish.ROUGH);
		finSet2.setFinish(ExternalComponent.Finish.NORMAL);

		return rocket;
	}

	/**
	 * Creates a FinSet based on the specified option.
	 * @param parent The parent to which the FinSet will be attached.
	 * @param option The option to determine which type of FinSet to create:
	 *               0 - Default TrapezoidFin Set,
	 *               1 - EllipticalFin Set,
	 *               2 - FreeformFin Set.
	 * @return
	 */
	private static FinSet createFinSet(RocketComponent parent, int option) {
		FinSet finSet;

		switch (option) {
			case 0:
				// Option 0: Default Trapezoid Fin Set
				TrapezoidFinSet f = new TrapezoidFinSet();
				f.setHeight(0.05);
				f.setRootChord(0.08);
				f.setSweep(0.05);
				finSet = f;
				break;
			case 1:
				// Option 1: Elliptical Fin Set
				EllipticalFinSet e = new EllipticalFinSet();
				e.setLength(0.08);
				e.setHeight(0.05);
				finSet = e;
				break;
			case 2:
				// Option 2: Freeform Fin Set
				FreeformFinSet ff = new FreeformFinSet();
				ff.setPoints(new Coordinate[]{
						new Coordinate(0, 0),
						new Coordinate(0.02, 0.1),
						new Coordinate(0.03, 0.1),
						new Coordinate(0.03, 0.05),
						new Coordinate(0.05, 0.1),
						new Coordinate(0.08, 0.1),
						new Coordinate(0.06, 0),
				});
				finSet = ff;
				break;
			default:
				throw new IllegalArgumentException("Invalid fin set option: " + option);
		}

		finSet.setFinCount(3);
		finSet.setThickness(0.005);
		finSet.setAxialMethod(AxialMethod.BOTTOM);
		finSet.setAxialOffset(0);

		parent.addChild(finSet); // Attach fins to the parent
		return finSet;
	}

	public static void setupTerrain(Scene scene) {
		scene.setFogEnabled(true);
		scene.setFogDensity(0.01f);

		// Option 1: Create terrain from GPS coordinates
		/*try {
			TerrainGenerator terrainGenerator = new TerrainGenerator();
			// GPS Coordinates for Leuven, Belgium
			double latitude = 50.8792;
			double longitude = 4.7009;
			int zoom = 16; // A good zoom level for city detail
			float size = 40.0f; // The size of the ground plane in the scene
			scene.addObject(terrainGenerator.createGpsTerrain(latitude, longitude, zoom, size));
		} catch (Exception e) {
			log.error("Could not create terrain from GPS: {}", e.getMessage(), e);
		}*/

		// Option 2: Load from a local file
		try {
			TerrainGenerator terrainGenerator = new TerrainGenerator();
			// Make sure you have an image file at this path in your resources.
			String customFloorImagePath = resolveResourcePath("/textures/grass.jpg");
			float size = 200.0f;
			scene.addObject(terrainGenerator.createTerrainFromFile(customFloorImagePath, size, 20.0f));
		} catch (Exception e) {
			log.error("Could not create terrain from file: {}", e.getMessage(), e);
		}
	}

	/**
	 * Sets up the background for the scene based on the specified option.
	 * @param scene The scene to set the background for.
	 * @param option The background option to use:
	 *               0 - Solid color background (no gradient),
	 *               1 - Solidcolor gradient,
	 *               2 - Skybox with separate images,
	 *               3 - Skybox atlas texture,
	 *               4 - HDRI background.
	 */
	public static void setupBackground(SceneView scene, int option) {
		switch (option) {
			// Option 0: Solid color
			case 0:
				scene.setBackground(new SolidColorBackground(0.5f, 0.5f, 0.5f, 1f));		// Light grey background
				break;
			// Option 1: Set a gradient color background
			case 1:
				scene.setBackground(new GradientBackground(
						new Vector3f(0.8f, 0.9f, 1.0f), // Light blue top
						new Vector3f(0.2f, 0.3f, 0.5f)  // Dark blue bottom
				));
				break;
			// Option 2: Use a skybox with separate images for each face.
			case 2:
				try {
					String[] skyboxFaces = {
							"/textures/skybox/right.jpg",
							"/textures/skybox/left.jpg",
							"/textures/skybox/top.jpg",
							"/textures/skybox/bottom.jpg",
							"/textures/skybox/front.jpg",
							"/textures/skybox/back.jpg"
					};
					scene.setBackground(new SkyboxBackground(new Texture(skyboxFaces)));
				} catch (Exception e) {
					log.warn("Could not load skybox, falling back to gradient: {}", e.getMessage());
					scene.setBackground(new GradientBackground(
							new Vector3f(0.8f, 0.9f, 1.0f), // Light blue top
							new Vector3f(0.2f, 0.3f, 0.5f)  // Dark blue bottom
					));
				}
				break;
			// Option 3: Use a skybox atlas texture for a more efficient single texture
			case 3:
				try {
					String atlasPath = "/textures/backgrounds/skybox_atlas_horizontal.png";
					scene.setBackground(new SkyboxBackground(new Texture(atlasPath, Texture.AtlasLayout.HORIZONTAL_CROSS)));
				} catch (Exception e) {
					log.warn("Could not load skybox atlas, falling back to gradient: {}", e.getMessage());
					scene.setBackground(new GradientBackground(
							new Vector3f(0.8f, 0.9f, 1.0f), // Light blue top
							new Vector3f(0.2f, 0.3f, 0.5f)  // Dark blue bottom
					));
				}
				break;
			// Option 4: Use an HDRI background
			case 4:
				try {
					Texture hdriTexture = new Texture("/textures/backgrounds/hdri_sky.hdr", true);
					scene.setBackground(new HDRIBackground(hdriTexture));
				} catch (Exception e) {
					log.warn("Could not load HDRI background, falling back to gradient: {}", e.getMessage());
					scene.setBackground(new GradientBackground(
							new Vector3f(0.8f, 0.9f, 1.0f), // Light blue top
							new Vector3f(0.2f, 0.3f, 0.5f)  // Dark blue bottom
					));
				}
		}
	}

	/**
	 * Sets up keyboard handlers for demo-specific actions.
	 */
    public static void setupDemoKeyboardHandling(KeyBindings handler, SceneView scene, Scene3DOrchestrator scene3DOrchestrator) {
		// Change Nose Cone Length with 'G'
		handler.addSinglePressAction(GLFW_KEY_G, () -> {
			scene.getObjects().stream()
					.filter(obj -> obj.getRocketComponent() instanceof NoseCone)
					.findFirst()
					.ifPresent(obj -> ((NoseCone) obj.getRocketComponent()).setLength(Math.random() * 0.15 + 0.05));
		});

		// Change Nose Cone Color with 'C'
		handler.addSinglePressAction(GLFW_KEY_C, () -> {
			scene.getObjects().stream()
					.filter(obj -> obj.getRocketComponent() instanceof NoseCone)
					.findFirst()
					.ifPresent(obj -> {
						NoseCone nose = (NoseCone) obj.getRocketComponent();
						Appearance current = nose.getAppearance();
						ORColor randomColor = new ORColor((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
						if (current == null) {
							nose.setAppearance(new Appearance(randomColor, 0.1));
						} else {
							nose.setAppearance(new Appearance(randomColor, current.getShine(), current.getTexture()));
						}
					});
		});

		// Cycle through TextureModes with 'T'
		handler.addSinglePressAction(GLFW_KEY_T, () -> {
			scene.getSelectedObjects().stream()
					.filter(obj -> obj.getRocketComponent() instanceof BodyTube)
					.findFirst()
					.ifPresent(obj -> {
						Appearance3D appearance = obj.getAppearance();
						Appearance3D.TextureMode currentMode = appearance.getTextureMode();
						Appearance3D.TextureMode[] allModes = Appearance3D.TextureMode.values();
						int nextModeIndex = (currentMode.ordinal() + 1) % allModes.length;
						Appearance3D.TextureMode nextMode = allModes[nextModeIndex];
						appearance.setTextureMode(nextMode);
					});
		});

		// Focus on rocket with 'F'
		handler.addSinglePressAction(GLFW_KEY_F, scene3DOrchestrator::focusOnRocket);

		// Export with 'E'
		handler.addSinglePressAction(GLFW_KEY_E, () -> {
			boolean isShiftDown = handler.isKeyPressed(GLFW_KEY_LEFT_SHIFT) || handler.isKeyPressed(GLFW_KEY_RIGHT_SHIFT);
			scene3DOrchestrator.requestExport(isShiftDown); // Request transparent export if Shift is held
		});
	}

	private static Motor generateMotor_A8_18mm() {
		return new ThrustCurveMotor.Builder()
				.setManufacturer(Manufacturer.getManufacturer("Estes"))
				.setDesignation("A8")
				.setDescription(" SU Black Powder")
				.setCaseInfo("SU 18.0x70.0")
				.setMotorType(Motor.Type.SINGLE)
				.setStandardDelays(new double[] { 0, 3, 5 })
				.setDiameter(0.018)
				.setLength(0.070)
				.setTimePoints(new double[] { 0, 1, 2 })
				.setThrustPoints(new double[] { 0, 9, 0 })
				.setCGPoints(new Coordinate[] {
						new Coordinate(0.035, 0, 0, 0.0164), new Coordinate(0.035, 0, 0, 0.0145),
						new Coordinate(0.035, 0, 0, 0.0131) })
				.setDigest("digest A8 test")
				.build();
	}

}
