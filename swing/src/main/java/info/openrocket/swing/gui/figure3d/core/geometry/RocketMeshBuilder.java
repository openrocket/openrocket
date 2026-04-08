package info.openrocket.swing.gui.figure3d.core.geometry;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.Coaxial;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.MassObject;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.CoordinateIF;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.basic.AxesGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.components.CoaxialGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.components.FinSetGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.components.MassObjectGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.components.MotorGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.components.RailButtonGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.components.TransitionGenerator;
import info.openrocket.swing.gui.figure3d.core.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.flame.FlameSettings;
import info.openrocket.swing.gui.figure3d.core.particles.smoke.SmokeEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.smoke.SmokeSettings;
import info.openrocket.swing.gui.figure3d.core.particles.spark.SparkEmitter;
import info.openrocket.swing.gui.figure3d.core.particles.spark.SparkSettings;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory;
import info.openrocket.swing.gui.figure3d.scene.core.Scene;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.core.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.scene.properties.VisualEffectsSettings;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Factory class responsible for building complete 3D mesh representations of rockets and their components.
 * 
 * <p>This class provides the main entry point for converting OpenRocket's internal rocket data model
 * into renderable 3D geometry. It handles the complex process of traversing rocket component hierarchies,
 * generating appropriate meshes for each component type, positioning them correctly in 3D space, and
 * managing coordinate system transformations between OpenRocket's conventions and the rendering engine.</p>
 * 
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Converting rocket component data to 3D meshes using appropriate generators</li>
 *   <li>Handling coordinate system transformations (OpenRocket LHS to Engine RHS)</li>
 *   <li>Managing component positioning, scaling, and rotation</li>
 *   <li>Creating motor meshes and particle effects for propulsion visualization</li>
 *   <li>Generating coordinate system axes for reference</li>
 *   <li>Supporting various rendering configurations and visual effects</li>
 * </ul>
 * 
 * <p>The class uses a world scaling factor to bring rocket dimensions (typically in meters) 
 * to appropriate rendering units, and handles the conversion between OpenRocket's left-handed 
 * coordinate system (+X longitudinal, +Y into screen, +Z radially up) and the engine's 
 * right-handed system (+X radially right, +Y longitudinal up, -Z into screen).</p>
 */
public abstract class RocketMeshBuilder {
	/**
	 * Defines the global scaling factor for the scene.
	 * All rocket component dimensions and positions (in meters) will be multiplied
	 * by this value to bring them to a convenient size for rendering.
	 * For example, a scale of 20 means a 1-meter-long tube will be 20 units long in the scene.
	 */
	/**
	 * @deprecated Use RenderingConstants.WORLD_SCALE instead
	 */
	@Deprecated
	public static final float WORLD_SCALE = RenderingConstants.WORLD_SCALE;

	/**
	 * Populates the given scene with meshes generated from a Rocket data model.
	 * 
	 * <p>This is the main entry point for converting a complete rocket design into 3D geometry.
	 * It processes all visible components in the rocket hierarchy, generates appropriate meshes
	 * for each component type, and adds them to the scene with correct positioning and scaling.</p>
	 * 
	 * @param scene The scene to add the generated mesh objects to
	 * @param rocket The rocket data model containing all component definitions
	 * @param config The rendering configuration specifying visual options and quality settings
	 */
	public static void buildRocketMesh(SceneView scene, Rocket rocket, RenderingConfiguration config) {
		FlightConfiguration flightConfig = rocket.getSelectedConfiguration();
		buildComponents(scene, flightConfig.getId(), flightConfig.getActiveInstances().entrySet(), config,
				RenderingConstants.WORLD_SCALE);
		buildComponents(scene, flightConfig.getId(), flightConfig.getExtraRenderInstances().entrySet(), config,
				RenderingConstants.WORLD_SCALE);

		createOriginAxes(scene, config, true, true);
	}

	private static void buildComponents(SceneView scene, FlightConfigurationId fcid,
										Set<Entry<RocketComponent, ArrayList<InstanceContext>>> instanceEntries,
										RenderingConfiguration config, float worldScale) {
		for (Entry<RocketComponent, ArrayList<InstanceContext>> entry : instanceEntries) {
			RocketComponent component = entry.getKey();
			if (!component.isVisible()) {
				continue;
			}

			try {
				buildComponent(scene, fcid, component, entry.getValue(), config, worldScale);
			} catch (RuntimeException e) {
				throw new BugException("Failed to build 3D mesh for component '" + component.getName()
						+ "' (" + component.getClass().getSimpleName() + ")", e);
			}
		}
	}

	/**
	 * Build and place all instances of a RocketComponent in the scene.
	 *
	 * @param scene The scene to add the component meshes to.
	 * @param component The RocketComponent to build.
	 * @param config The render configuration
	 * @param worldScale The scale factor to apply to the component's dimensions and positions.
	 */
	private static void buildComponent(SceneView scene, FlightConfigurationId fcid, RocketComponent component,
									   List<InstanceContext> instanceContexts, RenderingConfiguration config,
									   float worldScale) {
		if (instanceContexts == null || instanceContexts.isEmpty()) {
			return;
		}

		Mesh mesh = createComponentMesh(component, config);
		if (mesh == null) {
			return;
		}

		Appearance3D appearance = AppearanceFactory.createFrom(component);
		double angleOffsetX = component instanceof RailButton ? component.getAngleOffset() : 0.0;

		for (InstanceContext context : instanceContexts) {
			CoordinateIF instanceLocation = context.getLocation();
			CoordinateIF instanceAngle = new Coordinate(
					context.transform.getXrotation() + angleOffsetX,
					context.transform.getYrotation(),
					context.transform.getZrotation());

			// Create the SceneObject for the primary component
			SceneObject obj = new SceneObject(component, mesh, new Vector3f(0, 0, 0), appearance);
			configureComponentTransform(obj, instanceLocation, instanceAngle, component, worldScale);
			scene.addObject(obj);

			// Add motor
			if (component instanceof MotorMount) {
				buildMotor(scene, fcid, (RocketComponent & MotorMount) component, instanceLocation, instanceAngle,
						worldScale, config);
			}
		}
	}

	/**
	 * Creates and adds a SceneObject for the motor within a MotorMount.
	 *
	 * @param scene The scene to add the motor object to.
	 * @param mount The parent MotorMount component.
	 * @param mountLocation The world-space location of the parent mount.
	 * @param mountAngle The world-space angle of the parent mount.
	 * @param worldScale The global world scale factor.
	 */
	private static <T extends RocketComponent & MotorMount> void buildMotor(
			SceneView scene, FlightConfigurationId fcid, T mount, CoordinateIF mountLocation, CoordinateIF mountAngle,
			float worldScale, RenderingConfiguration config) {
		MotorConfiguration motorCfg = mount.getMotorConfig(fcid);
		if (motorCfg == null) return;

		Motor motor = motorCfg.getMotor();
		if (motor == null) return;

		// 1. Create the motor mesh
		Mesh motorMesh = MotorGenerator.create(motor, config);

		// 2. Create the appearance
		Appearance3D motorAppearance = AppearanceFactory.createFrom(motor);

		// 3. Create the SceneObject, associating it with the parent mount for selection grouping
		SceneObject motorObj = new SceneObject(mount, motorMesh, new Vector3f(0, 0, 0), motorAppearance);

		// 4. Calculate the motor's absolute world position based on JOGL logic.
		// The motor's position is relative to the mount's front end.
		double motorFrontRelToMountFront = mount.getLength() + mount.getMotorOverhang() - motor.getLength();
		// The motor mesh is centered, so we need to find the center position.
		double motorCenterRelToMountFront = motorFrontRelToMountFront + motor.getLength() / 2.0;
		// The mount location already represents this instance's front position in rocket coordinates.
		CoordinateIF motorCenterAbsolute = mountLocation.add(motorCenterRelToMountFront, 0, 0);


		// 5. Apply the transform to the motor object
		Matrix4f modelMatrix = motorObj.getModelMatrix();
		Vector3f positionInEngineCS = new Vector3f(
				(float) motorCenterAbsolute.getX(),
				(float) motorCenterAbsolute.getY(),
				(float) motorCenterAbsolute.getZ() * -1.0f
		).mul(worldScale);

		Matrix4f rotationMatrix = new Matrix4f()
				.rotateX(-(float) mountAngle.getX())
				.rotateY(-(float) mountAngle.getY())
				.rotateZ(-(float) mountAngle.getZ());

		modelMatrix.identity()
				.translate(positionInEngineCS)
				.mul(rotationMatrix)
				.scale(worldScale);

		scene.addObject(motorObj);

		// 6. Create a particle emitter for the motor exhaust
		String motorComponentId = generateMotorComponentId(fcid, mount);
		addParticles(scene, worldScale, positionInEngineCS, motor, rotationMatrix, config, motorComponentId);
	}

	private static void addParticles(SceneView scene, float worldScale, Vector3f positionInEngineCS, Motor motor,
									 Matrix4f rotationMatrix, RenderingConfiguration config, String motorComponentId) {
		VisualEffectsSettings settings = config.getVisualEffects();

		// Skip particle creation if particle effects are disabled globally
		if (!settings.areParticleEffectsEnabled()) {
			return;
		}
		
		// Skip particle creation if disabled for this specific motor
		if (!settings.areMotorParticlesEnabled(motorComponentId)) {
			return;
		}
		Vector3f motorCenter = new Vector3f(positionInEngineCS.add((float) motor.getLength(), 0f, 0f));
		Vector3f exhaustDirection = new Vector3f(1, 0, 0);
		rotationMatrix.transformDirection(exhaustDirection);

		float motorLength = (float) motor.getLength() * worldScale;
		Vector3f emitterPosition = new Vector3f(motorCenter).add(new Vector3f(exhaustDirection).mul(motorLength / 2.0f));

		// Particle static capture time in seconds, or null for dynamic particles
		Float time = settings.areStaticParticles() ? settings.getParticleTime() : null;

		// Add spark particles if enabled
		if (settings.areSparkParticlesEnabled()) {
			ParticleEmitter sparkEmitter = new SparkEmitter(emitterPosition, new Vector3f(exhaustDirection),
					SparkSettings.intense(config, settings.getExhaustScale(),
							settings.getSparkConcentration(), settings.getSparkWeight()));
			if (time != null) {
				sparkEmitter.captureStaticParticles(time);
			}
			scene.addParticleEmitter(sparkEmitter);
		}

		// Add smoke particles if enabled
		if (settings.areSmokeParticlesEnabled()) {
			// Position smoke emitter slightly behind flame for more realistic interaction
			Vector3f smokePosition = new Vector3f(emitterPosition).sub(new Vector3f(exhaustDirection).mul(0.2f));
			ParticleEmitter smokeEmitter = new SmokeEmitter(smokePosition, new Vector3f(exhaustDirection),
					SmokeSettings.medium(config, settings.getSmokeColor(),
							settings.getSmokeOpacity(), settings.getExhaustScale()));
			if (time != null) {
				smokeEmitter.captureStaticParticles(time);
			}
			scene.addParticleEmitter(smokeEmitter);
		}

		// Add flame particles if enabled
		if (settings.areFlameParticlesEnabled()) {
			ParticleEmitter flameEmitter = new FlameEmitter(emitterPosition, new Vector3f(exhaustDirection),
					FlameSettings.normal(config, settings.getFlameColor(),
							settings.getExhaustScale(), settings.getFlameAspectRatio()));
			if (time != null) {
				flameEmitter.captureStaticParticles(time);
			}
			scene.addParticleEmitter(flameEmitter);
		}
	}

	/**
	 * Calculates and applies the world-space transformation for a SceneObject
	 * based on its component data and coordinate system conventions.
	 */
	private static void configureComponentTransform(SceneObject obj, CoordinateIF loc, CoordinateIF ang, RocketComponent component, float worldScale) {
		Matrix4f modelMatrix = obj.getModelMatrix();

		// Coordinate System and Handedness Conversion
		// OpenRocket (LHS): +X=longitudinal, +Y=into screen, +Z=up(radial)
		// Engine (RHS):     +X=right(radial), +Y=up(longitudinal), -Z=into screen
		double offsetX = (component instanceof Coaxial || component instanceof Transition || component instanceof MassObject) ? component.getLength() / 2.0 : 0;

		Vector3f positionInEngineCS = new Vector3f(
				(float)(loc.getX() + offsetX),
				(float)(loc.getY()),
				(float)(loc.getZ()) * -1.0f // Flip Z to convert from LHS to RHS
		).mul(worldScale);

		Matrix4f rotationMatrix = new Matrix4f()
				.rotateX(-(float)ang.getX())
				.rotateY(-(float)ang.getY())
				.rotateZ(-(float)ang.getZ());

		modelMatrix.identity()
				.translate(positionInEngineCS)
				.mul(rotationMatrix)
				.scale(worldScale);
	}

	private static Mesh createComponentMesh(RocketComponent component, RenderingConfiguration config) {
		if (component instanceof ComponentAssembly) {
			return null; // Assemblies are containers, not directly rendered.
		} else if (component instanceof Transition) {
			return TransitionGenerator.create((Transition) component, config);
		} else if (component instanceof Coaxial) {
			return CoaxialGenerator.create((RocketComponent & Coaxial) component, config);
		} else if (component instanceof FinSet) {
			return FinSetGenerator.create((FinSet) component, (SymmetricComponent) component.getParent(), config);
		} else if (component instanceof RailButton) {
			return RailButtonGenerator.create((RailButton) component, config);
		} else if (component instanceof MassObject) {
			return MassObjectGenerator.create((MassObject) component, config);
		}
		throw new IllegalArgumentException("Unsupported RocketComponent type: " + component.getClass().getSimpleName());
	}


	// HELPER METHODS

	/**
	 * Creates a visualizer for the OpenRocket coordinate system at the world origin.
	 * - Red arrow points along the rocket's longitudinal axis (+X in OR, +Y in Engine).
	 * - Green arrow points radially "right" (+Y in OR, +X in Engine).
	 * - Blue arrow points radially "up" (+Z in OR, -Z in Engine).
	 * @param scene The scene to add the axis objects to.
	 * @param config The rendering configuration.
	 * @param useORCoordinateSystem If true, uses OpenRocket's coordinate system; otherwise,uses the engine's coordinate system.
	 * @param onTop If true, the axes will be rendered on top of all other objects.
	 */
	public static void createOriginAxes(SceneView scene, RenderingConfiguration config, boolean useORCoordinateSystem, boolean onTop) {
		// Check if origin axes should be visible
		if (!config.getVisualEffects().isOriginAxesVisible()) {
			return;
		}
		float shaftLength = 0.8f;
		float shaftRadius = 0.01f;
		float headLength = 0.2f;
		float headRadius = 0.04f;
		float scale = 2.0f; // Make the visualizer larger

		// Create a single arrow mesh that will be reused for all three axes.
		// It's generated pointing along the engine's +X axis by default.
		Mesh arrowMesh = AxesGenerator.createArrowMesh(shaftLength, shaftRadius, headLength, headRadius);

		// --- OR X-Axis (Longitudinal) -> Engine +X (Right) ---
		Appearance3D xAxisAppearance = new Appearance3D(new Vector3f(1.0f, 0.3f, 0.3f));
		xAxisAppearance.setUnlit(true);
		SceneObject xAxisObject = new SceneObject(arrowMesh, new Vector3f(0, 0, 0), xAxisAppearance);
		xAxisObject.getModelMatrix().scale(scale); // No rotation needed
		xAxisObject.setSelectable(false);
		xAxisObject.setRenderOnTop(onTop);
		scene.addObject(xAxisObject);

		// --- OR Y-Axis (Radial Up) -> Engine +Y (Up) ---
		Appearance3D yAxisAppearance = new Appearance3D(new Vector3f(0.3f, 1.0f, 0.3f));
		yAxisAppearance.setUnlit(true);
		SceneObject yAxisObject = new SceneObject(arrowMesh, new Vector3f(0, 0, 0), yAxisAppearance);
		yAxisObject.getModelMatrix().rotate((float) Math.toRadians(90), 0, 0, 1).scale(scale);
		yAxisObject.setSelectable(false);
		yAxisObject.setRenderOnTop(onTop);
		scene.addObject(yAxisObject);

		// --- OR Z-Axis (Radial Right) -> Engine -Z (Into Screen) ---
		Appearance3D zAxisAppearance = new Appearance3D(new Vector3f(0.3f, 0.3f, 1.0f));
		zAxisAppearance.setUnlit(true);
		SceneObject zAxisObject = new SceneObject(arrowMesh, new Vector3f(0, 0, 0), zAxisAppearance);
		if (useORCoordinateSystem) {
			zAxisObject.getModelMatrix().rotate((float) Math.toRadians(90), 0, 1, 0).scale(scale);
		} else {
			// In the engine's coordinate system, we need to flip the Z axis.
			zAxisObject.getModelMatrix().rotate((float) Math.toRadians(-90), 1, 0, 0).scale(scale);
		}
		zAxisObject.setSelectable(false);
		zAxisObject.setRenderOnTop(onTop);
		scene.addObject(zAxisObject);
	}
	
	/**
	 * Rebuilds all particle emitters in the scene based on current scene properties.
	 * This should be called when particle settings change.
	 */
	public static void rebuildParticles(Scene scene, Rocket rocket, RenderingConfiguration config) {
		// Clear existing particle emitters
		scene.getParticleEmitters().clear();

		FlightConfiguration flightConfig = rocket.getSelectedConfiguration();
		for (Entry<RocketComponent, ArrayList<InstanceContext>> entry : flightConfig.getActiveInstances().entrySet()) {
			RocketComponent component = entry.getKey();
			if (!component.isVisible() || !(component instanceof MotorMount)) {
				continue;
			}

			@SuppressWarnings("unchecked")
			var mount = (RocketComponent & MotorMount) component;
			rebuildMotorParticles(scene, flightConfig.getId(), mount, entry.getValue(), config,
					RenderingConstants.WORLD_SCALE);
		}
	}
	
	private static <T extends RocketComponent & MotorMount> void rebuildMotorParticles(
			Scene scene, FlightConfigurationId fcid, T mount, List<InstanceContext> instanceContexts,
			RenderingConfiguration config, float worldScale) {
		MotorConfiguration motorCfg = mount.getMotorConfig(fcid);
		if (motorCfg == null) return;
		
		Motor motor = motorCfg.getMotor();
		if (motor == null) return;
		if (instanceContexts == null || instanceContexts.isEmpty()) {
			return;
		}
		
		for (InstanceContext context : instanceContexts) {
			CoordinateIF instanceLocation = context.getLocation();
			CoordinateIF instanceAngle = new Coordinate(
					context.transform.getXrotation(),
					context.transform.getYrotation(),
					context.transform.getZrotation());

			// Calculate motor position (same logic as in buildMotor)
			double motorFrontRelToMountFront = mount.getLength() + mount.getMotorOverhang() - motor.getLength();
			double motorCenterRelToMountFront = motorFrontRelToMountFront + motor.getLength() / 2.0;
			CoordinateIF motorCenterAbsolute = instanceLocation.add(motorCenterRelToMountFront, 0, 0);
			
			Vector3f positionInEngineCS = new Vector3f(
					(float) motorCenterAbsolute.getX(),
					(float) motorCenterAbsolute.getY(),
					(float) motorCenterAbsolute.getZ() * -1.0f
			).mul(worldScale);
			
			Matrix4f rotationMatrix = new Matrix4f()
					.rotateX(-(float) instanceAngle.getX())
					.rotateY(-(float) instanceAngle.getY())
					.rotateZ(-(float) instanceAngle.getZ());

			// TODO: is there a better way to do this indexing?
			String motorComponentId = generateMotorComponentId(fcid, mount);
			addParticles(scene, worldScale, positionInEngineCS, motor, rotationMatrix, config, motorComponentId);
		}
	}
	
	/**
	 * Generates a unique identifier for a motor component.
	 * This can be used to control particles for individual motors.
	 */
	private static String generateMotorComponentId(FlightConfigurationId fcid, RocketComponent mount) {
		return fcid.key.toString() + "-" + mount.getID().toString();
	}
	
	/**
	 * Rebuilds origin axes based on current scene properties.
	 * This should be called when axis visibility changes.
	 */
	public static void rebuildOriginAxes(Scene scene, RenderingConfiguration config, boolean useORCoordinateSystem, boolean onTop) {
		// Remove existing axis objects
		scene.getObjects().removeIf(obj -> !obj.isSelectable() && obj.isRenderOnTop() == onTop);
		
		// Recreate axes if they should be visible
		createOriginAxes(scene, config, useORCoordinateSystem, onTop);
	}
	
	/**
	 * Gets all motor component IDs from a rocket.
	 * Useful for setting up per-motor particle controls.
	 */
	public static List<String> getMotorComponentIds(FlightConfigurationId fcid, Rocket rocket) {
		List<String> motorIds = new ArrayList<>();
		for (RocketComponent component : rocket) {
			if (component instanceof MotorMount) {
				motorIds.add(generateMotorComponentId(fcid, component));
			}
		}
		return motorIds;
	}
}
