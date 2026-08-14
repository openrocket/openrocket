package info.openrocket.swing.gui.figure3d.geometry;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.figure3d.materials.AppearanceFactory;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * Immutable snapshot of all rocket-derived data needed to (re)build the 3D scene.
 *
 * <p>The snapshot is built on the EDT (where the rocket model is consistent under
 * {@code Rocket}'s mutex during a {@code fireComponentChangeEvent}) and consumed
 * later on the GL thread by {@link RocketMeshBuilder#prepareSnapshot}. This avoids
 * reading mutable rocket fields (component lengths/radii, instance maps, etc.)
 * from the GL thread, where they could race with concurrent edits and produce
 * partially-updated geometry — for example, a body tube with the new length but
 * a downstream nose cone still positioned for the old length.</p>
 */
public final class RocketSceneSnapshot {

	/**
	 * One renderable instance of a rocket component. Holds the prebuilt mesh and
	 * the fully-resolved model matrix; nothing the GL thread needs to compute
	 * touches the live rocket model.
	 */
	public record ComponentInstance(
			RocketComponent component,
			AppearanceFactory.ComponentAppearanceSnapshot appearance,
			Mesh mesh,
			Matrix4f modelMatrix
	) {}

	/**
	 * One renderable motor instance. The {@code mountForSelectionGrouping} mirrors
	 * the existing convention of associating motor scene objects with their parent
	 * mount component for selection.
	 *
	 * @param particleEmitterPlan particle emitter inputs, or {@code null} when this
	 *                            instance should not emit particles (i.e. it is not
	 *                            the lowest motor instance for its mount).
	 */
	public record MotorInstance(
			RocketComponent mountForSelectionGrouping,
			Mesh motorMesh,
			Motor motor,
			Matrix4f modelMatrix,
			ParticleEmitterPlan particleEmitterPlan
	) {}

	/**
	 * Pre-computed inputs for motor exhaust particle emitters. Whether emitters
	 * are actually created depends on {@code RenderingConfiguration} settings,
	 * which are evaluated at apply time so live UI toggles take effect.
	 */
	public record ParticleEmitterPlan(
			Vector3f motorCenterEngineCS,
			Matrix4f motorRotationMatrix,
			Motor motor,
			float worldScale
	) {}

	private final List<ComponentInstance> componentInstances;
	private final List<MotorInstance> motorInstances;
	private final FlightConfigurationId flightConfigurationId;

	public RocketSceneSnapshot(List<ComponentInstance> componentInstances, List<MotorInstance> motorInstances,
			FlightConfigurationId flightConfigurationId) {
		this.componentInstances = List.copyOf(componentInstances);
		this.motorInstances = List.copyOf(motorInstances);
		this.flightConfigurationId = flightConfigurationId;
	}

	public List<ComponentInstance> getComponentInstances() {
		return componentInstances;
	}

	public List<MotorInstance> getMotorInstances() {
		return motorInstances;
	}

	public FlightConfigurationId getFlightConfigurationId() {
		return flightConfigurationId;
	}
}
