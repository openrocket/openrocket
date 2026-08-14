package info.openrocket.swing.gui.figure3d.geometry;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.motor.MotorConfiguration;
import info.openrocket.core.rocketcomponent.Coaxial;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.InstanceContext;
import info.openrocket.core.rocketcomponent.InstanceMap;
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
import info.openrocket.swing.gui.figure3d.geometry.basic.AxesGenerator;
import info.openrocket.swing.gui.figure3d.geometry.components.CoaxialGenerator;
import info.openrocket.swing.gui.figure3d.geometry.components.FinSetGenerator;
import info.openrocket.swing.gui.figure3d.geometry.components.MassObjectGenerator;
import info.openrocket.swing.gui.figure3d.geometry.components.MotorGenerator;
import info.openrocket.swing.gui.figure3d.geometry.components.RailButtonGenerator;
import info.openrocket.swing.gui.figure3d.geometry.components.TransitionGenerator;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameSettings;
import info.openrocket.swing.gui.figure3d.particles.smoke.SmokeEmitter;
import info.openrocket.swing.gui.figure3d.particles.smoke.SmokeSettings;
import info.openrocket.swing.gui.figure3d.particles.spark.SparkEmitter;
import info.openrocket.swing.gui.figure3d.particles.spark.SparkSettings;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import info.openrocket.swing.gui.figure3d.scene.properties.VisualEffectsSettings;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderableMesh;
import info.openrocket.swing.gui.figure3d.rendering.GLErrors;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import info.openrocket.swing.gui.figure3d.rendering.SharedRenderable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Converts rocket components and instances into immutable CPU-side scene snapshots,
 * then realizes those snapshots as context-owned renderables and particle emitters.
 */
public abstract class RocketMeshBuilder {
	/** GPU resources prepared before an existing rocket scene is replaced. */
	public static final class PreparedSnapshot {
		private final List<SceneObject> objects;
		private final List<ParticleEmitter> particleEmitters;
		private boolean committed;

		private PreparedSnapshot(List<SceneObject> objects, List<ParticleEmitter> particleEmitters) {
			this.objects = objects;
			this.particleEmitters = particleEmitters;
		}

		public void commitTo(SceneView scene) {
			for (SceneObject object : objects) {
				scene.addObject(object);
			}
			for (ParticleEmitter emitter : particleEmitters) {
				scene.addParticleEmitter(emitter);
			}
			committed = true;
		}

		public void cleanupIfUncommitted() {
			if (!committed) {
				objects.forEach(SceneObject::cleanup);
			}
		}
	}

	/**
	 * Captures all rocket-derived data needed to (re)build the 3D scene into an immutable
	 * snapshot. Intended to be called on the thread that owns the rocket model (the EDT for
	 * the running app), so the snapshot reflects a consistent view of the rocket even when
	 * it would otherwise be raced by edits before the GL thread renders.
	 *
	 * <p>This is pure CPU work: meshes and transforms are pre-computed but no GPU resources
	 * are allocated. Pair with {@link #prepareSnapshot} on the GL thread.</p>
	 */
	public static RocketSceneSnapshot buildSnapshot(Rocket rocket, RenderingConfiguration config) {
		FlightConfiguration flightConfig = rocket.getSelectedConfiguration();
		InstanceMap lowestMotorInstances = flightConfig.getLowestMotorInstances();
		List<RocketSceneSnapshot.ComponentInstance> componentInstances = new ArrayList<>();
		List<RocketSceneSnapshot.MotorInstance> motorInstances = new ArrayList<>();
		collectInstances(componentInstances, motorInstances, flightConfig.getId(),
				flightConfig.getActiveInstances().entrySet(), config,
				RenderingConstants.WORLD_SCALE, lowestMotorInstances);
		collectInstances(componentInstances, motorInstances, flightConfig.getId(),
				flightConfig.getExtraRenderInstances().entrySet(), config,
				RenderingConstants.WORLD_SCALE, lowestMotorInstances);
		return new RocketSceneSnapshot(componentInstances, motorInstances, flightConfig.getId());
	}

	/** Allocates all resources without mutating the destination scene. */
	public static PreparedSnapshot prepareSnapshot(RocketSceneSnapshot snapshot, RenderingConfiguration config) {
		GLErrors.beginResourceCreation("rocket scene resource creation");
		List<SceneObject> objects = new ArrayList<>();
		List<ParticleEmitter> particleEmitters = new ArrayList<>();
		try {
			prepareSnapshotObjects(objects, particleEmitters, snapshot, config);
			createOriginAxes(objects::add, config, true, true);
			return new PreparedSnapshot(objects, particleEmitters);
		} catch (RuntimeException | Error e) {
			objects.forEach(SceneObject::cleanup);
			throw e;
		}
	}

	private static void prepareSnapshotObjects(List<SceneObject> objects, List<ParticleEmitter> particleEmitters,
			RocketSceneSnapshot snapshot, RenderingConfiguration config) {
		IdentityHashMap<RocketComponent, Appearance3D> componentAppearances = new IdentityHashMap<>();
		IdentityHashMap<Mesh, SharedRenderable> renderables = new IdentityHashMap<>();
		for (RocketSceneSnapshot.ComponentInstance ci : snapshot.getComponentInstances()) {
			Appearance3D appearance = componentAppearances.computeIfAbsent(ci.component(),
					ignored -> AppearanceFactory.createFrom(ci.appearance()));
			Renderable renderable = acquireRenderable(renderables, ci.mesh());
			SceneObject obj = SceneObject.withRenderable(ci.component(), ci.mesh(), renderable,
					new Vector3f(0, 0, 0), appearance);
			obj.getModelMatrix().set(ci.modelMatrix());
			objects.add(obj);
		}

		for (RocketSceneSnapshot.MotorInstance mi : snapshot.getMotorInstances()) {
			// Match legacy behavior: motor appearances are not shared across instances.
			Appearance3D motorAppearance = AppearanceFactory.createFrom(mi.motor());
			Renderable renderable = acquireRenderable(renderables, mi.motorMesh());
			SceneObject motorObj = SceneObject.withIndependentAppearance(
					mi.mountForSelectionGrouping(), mi.motorMesh(), renderable,
					new Vector3f(0, 0, 0), motorAppearance);
			motorObj.getModelMatrix().set(mi.modelMatrix());
			objects.add(motorObj);

			if (mi.particleEmitterPlan() != null) {
				addParticles(particleEmitters::add, mi.particleEmitterPlan(), config);
			}
		}
	}

	private static Renderable acquireRenderable(IdentityHashMap<Mesh, SharedRenderable> renderables, Mesh mesh) {
		SharedRenderable shared = renderables.computeIfAbsent(mesh,
				ignored -> new SharedRenderable(new GLRenderableMesh(mesh)));
		return shared.acquire();
	}

	private static void collectInstances(List<RocketSceneSnapshot.ComponentInstance> componentInstances,
										 List<RocketSceneSnapshot.MotorInstance> motorInstances,
										 FlightConfigurationId fcid,
										 Set<Entry<RocketComponent, ArrayList<InstanceContext>>> instanceEntries,
										 RenderingConfiguration config, float worldScale,
										 InstanceMap lowestMotorInstances) {
		for (Entry<RocketComponent, ArrayList<InstanceContext>> entry : instanceEntries) {
			RocketComponent component = entry.getKey();
			if (!component.isVisible()) {
				continue;
			}

			try {
				collectComponent(componentInstances, motorInstances, fcid, component, entry.getValue(), config,
						worldScale, lowestMotorInstances);
			} catch (RuntimeException e) {
				throw new BugException("Failed to build 3D mesh for component '" + component.getName()
						+ "' (" + component.getClass().getSimpleName() + ")", e);
			}
		}
	}

	private static void collectComponent(List<RocketSceneSnapshot.ComponentInstance> componentInstances,
										 List<RocketSceneSnapshot.MotorInstance> motorInstances,
										 FlightConfigurationId fcid, RocketComponent component,
										 List<InstanceContext> instanceContexts, RenderingConfiguration config,
										 float worldScale, InstanceMap lowestMotorInstances) {
		if (instanceContexts == null || instanceContexts.isEmpty()) {
			return;
		}

		Mesh mesh = createComponentMesh(component, config);
		boolean isMotorMount = component instanceof MotorMount;
		if (mesh == null && !isMotorMount) {
			return;
		}
		Mesh motorMesh = null;
		if (isMotorMount) {
			MotorConfiguration motorConfig = ((MotorMount) component).getMotorConfig(fcid);
			if (motorConfig != null && motorConfig.getMotor() != null) {
				motorMesh = MotorGenerator.create(motorConfig.getMotor(), config);
			}
		}

		double angleOffsetX = component instanceof RailButton ? component.getAngleOffset() : 0.0;
		AppearanceFactory.ComponentAppearanceSnapshot appearance = mesh != null
				? AppearanceFactory.captureComponentAppearance(component) : null;

		for (InstanceContext context : instanceContexts) {
			CoordinateIF instanceLocation = context.getLocation();
			CoordinateIF instanceAngle = new Coordinate(
					context.transform.getXrotation() + angleOffsetX,
					context.transform.getYrotation(),
					context.transform.getZrotation());

			if (mesh != null) {
				Matrix4f modelMatrix = computeComponentModelMatrix(instanceLocation, instanceAngle, component, worldScale);
				componentInstances.add(new RocketSceneSnapshot.ComponentInstance(component, appearance, mesh, modelMatrix));
			}

			if (isMotorMount) {
				@SuppressWarnings("unchecked")
				RocketSceneSnapshot.MotorInstance motor = collectMotor(fcid,
						(RocketComponent & MotorMount) component, instanceLocation, instanceAngle,
						worldScale, motorMesh, context, lowestMotorInstances);
				if (motor != null) {
					motorInstances.add(motor);
				}
			}
		}
	}

	/**
	 * Captures all data required to build a single motor instance scene object and (optionally)
	 * its exhaust particle emitter. Pure CPU; safe to call on the EDT.
	 */
	private static <T extends RocketComponent & MotorMount> RocketSceneSnapshot.MotorInstance collectMotor(
			FlightConfigurationId fcid, T mount, CoordinateIF mountLocation, CoordinateIF mountAngle,
			float worldScale, Mesh motorMesh, InstanceContext context,
			InstanceMap lowestMotorInstances) {
		MotorConfiguration motorCfg = mount.getMotorConfig(fcid);
		if (motorCfg == null) return null;

		Motor motor = motorCfg.getMotor();
		if (motor == null || motorMesh == null) return null;

		// The motor's position is relative to the mount's front end.
		double motorFrontRelToMountFront = mount.getLength() + mount.getMotorOverhang() - motor.getLength();
		// The motor mesh is centered, so we need to find the center position.
		double motorCenterRelToMountFront = motorFrontRelToMountFront + motor.getLength() / 2.0;
		// The mount location already represents this instance's front position in rocket coordinates.
		CoordinateIF motorCenterAbsolute = mountLocation.add(motorCenterRelToMountFront, 0, 0);

		Vector3f positionInEngineCS = new Vector3f(
				(float) motorCenterAbsolute.getX(),
				(float) motorCenterAbsolute.getY(),
				(float) motorCenterAbsolute.getZ() * -1.0f
		).mul(worldScale);

		Matrix4f rotationMatrix = new Matrix4f()
				.rotateX(-(float) mountAngle.getX())
				.rotateY(-(float) mountAngle.getY())
				.rotateZ(-(float) mountAngle.getZ());

		Matrix4f modelMatrix = new Matrix4f()
				.translate(positionInEngineCS)
				.mul(rotationMatrix)
				.scale(worldScale);

		RocketSceneSnapshot.ParticleEmitterPlan emitterPlan = null;
		if (isLowestMotorInstance(lowestMotorInstances, mount, context)) {
			emitterPlan = new RocketSceneSnapshot.ParticleEmitterPlan(
					new Vector3f(positionInEngineCS), rotationMatrix, motor, worldScale);
		}

		return new RocketSceneSnapshot.MotorInstance(mount, motorMesh, motor, modelMatrix, emitterPlan);
	}

	private static void addParticles(Consumer<ParticleEmitter> emitterConsumer,
			RocketSceneSnapshot.ParticleEmitterPlan plan,
			RenderingConfiguration config) {
		Vector3f positionInEngineCS = plan.motorCenterEngineCS();
		Motor motor = plan.motor();
		Matrix4f rotationMatrix = plan.motorRotationMatrix();
		float worldScale = plan.worldScale();
		addParticles(emitterConsumer, worldScale, positionInEngineCS, motor, rotationMatrix, config);
	}

	private static void addParticles(Consumer<ParticleEmitter> emitterConsumer, float worldScale,
			Vector3f positionInEngineCS, Motor motor,
			Matrix4f rotationMatrix, RenderingConfiguration config) {
		VisualEffectsSettings settings = config.getVisualEffects();

		// Skip particle creation if particle effects are disabled globally
		if (!settings.areParticleEffectsEnabled()) {
			return;
		}

		Vector3f motorCenter = new Vector3f(positionInEngineCS).add((float) motor.getLength(), 0f, 0f);
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
			emitterConsumer.accept(sparkEmitter);
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
			emitterConsumer.accept(smokeEmitter);
		}

		// Add flame particles if enabled
		if (settings.areFlameParticlesEnabled()) {
			ParticleEmitter flameEmitter = new FlameEmitter(emitterPosition, new Vector3f(exhaustDirection),
					FlameSettings.normal(config, settings.getFlameColor(),
							settings.getExhaustScale(), settings.getFlameAspectRatio()));
			if (time != null) {
				flameEmitter.captureStaticParticles(time);
			}
			emitterConsumer.accept(flameEmitter);
		}
	}

	/**
	 * Computes the world-space model matrix for a component instance.
	 *
	 * <p>Coordinate System and Handedness Conversion:</p>
	 * <ul>
	 *   <li>OpenRocket (LHS): +X=longitudinal, +Y=into screen, +Z=up(radial)</li>
	 *   <li>Engine (RHS):     +X=right(radial), +Y=up(longitudinal), -Z=into screen</li>
	 * </ul>
	 */
	private static Matrix4f computeComponentModelMatrix(CoordinateIF loc, CoordinateIF ang,
														RocketComponent component, float worldScale) {
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

		return new Matrix4f()
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
		createOriginAxes(scene::addObject, config, useORCoordinateSystem, onTop);
	}

	private static void createOriginAxes(Consumer<SceneObject> objectConsumer, RenderingConfiguration config,
			boolean useORCoordinateSystem, boolean onTop) {
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
		SharedRenderable arrowRenderable = new SharedRenderable(new GLRenderableMesh(arrowMesh));
		SceneObject xAxisObject = SceneObject.withRenderable(null, arrowMesh, arrowRenderable.acquire(),
				new Vector3f(0, 0, 0), xAxisAppearance);
		xAxisObject.getModelMatrix().scale(scale); // No rotation needed
		xAxisObject.setSelectable(false);
		xAxisObject.setRenderOnTop(onTop);
		xAxisObject.setOriginAxis(true);
		objectConsumer.accept(xAxisObject);

		// --- OR Y-Axis (Radial Up) -> Engine +Y (Up) ---
		Appearance3D yAxisAppearance = new Appearance3D(new Vector3f(0.3f, 1.0f, 0.3f));
		yAxisAppearance.setUnlit(true);
		SceneObject yAxisObject = SceneObject.withRenderable(null, arrowMesh, arrowRenderable.acquire(),
				new Vector3f(0, 0, 0), yAxisAppearance);
		yAxisObject.getModelMatrix().rotate((float) Math.toRadians(90), 0, 0, 1).scale(scale);
		yAxisObject.setSelectable(false);
		yAxisObject.setRenderOnTop(onTop);
		yAxisObject.setOriginAxis(true);
		objectConsumer.accept(yAxisObject);

		// --- OR Z-Axis (Radial Right) -> Engine -Z (Into Screen) ---
		Appearance3D zAxisAppearance = new Appearance3D(new Vector3f(0.3f, 0.3f, 1.0f));
		zAxisAppearance.setUnlit(true);
		SceneObject zAxisObject = SceneObject.withRenderable(null, arrowMesh, arrowRenderable.acquire(),
				new Vector3f(0, 0, 0), zAxisAppearance);
		if (useORCoordinateSystem) {
			zAxisObject.getModelMatrix().rotate((float) Math.toRadians(90), 0, 1, 0).scale(scale);
		} else {
			// In the engine's coordinate system, we need to flip the Z axis.
			zAxisObject.getModelMatrix().rotate((float) Math.toRadians(-90), 1, 0, 0).scale(scale);
		}
		zAxisObject.setSelectable(false);
		zAxisObject.setRenderOnTop(onTop);
		zAxisObject.setOriginAxis(true);
		objectConsumer.accept(zAxisObject);
	}

	private static boolean isLowestMotorInstance(InstanceMap lowestMotorInstances, RocketComponent mount,
			InstanceContext context) {
		List<InstanceContext> lowestInstances = lowestMotorInstances.get(mount);
		if (lowestInstances == null || lowestInstances.isEmpty()) {
			return lowestMotorInstances.isEmpty();
		}
		for (InstanceContext lowestContext : lowestInstances) {
			if (lowestContext.instanceNumber == context.instanceNumber) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Rebuilds origin axes based on current scene properties.
	 * This should be called when axis visibility changes.
	 */
	public static void rebuildOriginAxes(Scene scene, RenderingConfiguration config, boolean useORCoordinateSystem, boolean onTop) {
		List<SceneObject> axes = scene.getObjects().stream().filter(SceneObject::isOriginAxis).toList();
		for (SceneObject axis : axes) {
			scene.removeObject(axis);
			axis.cleanup();
		}

		// Recreate axes if they should be visible
		createOriginAxes(scene, config, useORCoordinateSystem, onTop);
	}
}
