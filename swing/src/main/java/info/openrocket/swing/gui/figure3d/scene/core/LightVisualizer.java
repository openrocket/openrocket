package info.openrocket.swing.gui.figure3d.scene.core;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.basic.SphereGenerator;
import info.openrocket.swing.gui.figure3d.core.geometry.basic.TubeGenerator;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates visual representations of lights within the 3D scene for debugging and user interaction.
 * This component generates visual markers that help users understand the lighting setup in the
 * OpenRocket 3D visualization environment. It provides interactive elements that allow users to
 * manipulate light positions and directions through direct 3D interaction.
 * 
 * <p>The visualizer creates different visual representations depending on the light type:</p>
 * <ul>
 *   <li><b>Point lights:</b> Rendered as draggable spheres positioned at the light location</li>
 *   <li><b>Directional lights:</b> Rendered as a draggable "sun" sphere connected to the origin by a ray</li>
 * </ul>
 * 
 * <p>All light visualizers are rendered as unlit objects to ensure they remain visible
 * regardless of the current lighting conditions. They integrate with the scene's interaction
 * system to provide intuitive light manipulation capabilities.</p>
 */
public class LightVisualizer {

	/**
	 * Creates one or more SceneObjects to visually represent a light source in the 3D scene.
	 * The generated visual representations allow users to see and interact with lights,
	 * making it easier to understand and adjust the lighting setup for optimal rocket visualization.
	 * 
	 * @param light the light source to create visual representations for
	 * @return a list of SceneObjects that make up the visual marker (may include multiple objects for directional lights)
	 */
	public List<SceneObject> createVisualsForLight(Light light) {
		List<SceneObject> visuals = new ArrayList<>();

		Appearance3D visualAppearance = new Appearance3D(light.getColor());
		visualAppearance.setUnlit(true); // The marker should not be affected by light.

		if (light.getType() == Light.LightType.POINT) {
			// For a point light, create a small sphere at its position.
			Mesh sphereMesh = SphereGenerator.create(0.4f, RenderingConstants.HIGH_SEGMENT_COUNT, RenderingConstants.HIGH_SEGMENT_COUNT);
			SceneObject pointVisual = new SceneObject(sphereMesh, light.getPosition(), visualAppearance);

			// Define the drag behavior: update the light and the visualizer's position.
			pointVisual.setOnDragListener((newPosition) -> {
				light.setPosition(newPosition.x, newPosition.y, newPosition.z);
				pointVisual.setPosition(newPosition);
			});
			visuals.add(pointVisual);
		} else { // DIRECTIONAL
			// For a directional light, create a draggable "sun" sphere and a non-draggable "ray" line.
			float sunDistance = 10.0f;
			Vector3f lightDir = light.getDirection();
			Vector3f sunPosition = new Vector3f(lightDir).negate().mul(sunDistance);
			Mesh sunMesh = SphereGenerator.create(0.5f, RenderingConstants.HIGH_SEGMENT_COUNT, RenderingConstants.HIGH_SEGMENT_COUNT);
			SceneObject sunVisual = new SceneObject(sunMesh, sunPosition, visualAppearance);
			sunVisual.setSelectable(true);

			Mesh rayMesh = TubeGenerator.create(0.02f, 0.02f, 0, sunDistance, RenderingConstants.LOW_SEGMENT_COUNT, true);
			SceneObject rayVisual = new SceneObject(rayMesh, new Vector3f(0,0,0), visualAppearance);
			rayVisual.setSelectable(false); // The ray itself is not draggable.

			updateRayTransform(rayVisual, sunPosition);

			// Define the drag behavior for the sun sphere.
			// This listener will update the light, the sun, AND the ray.
			sunVisual.setOnDragListener((newPosition) -> {
				// Keep the sun a fixed distance from the origin for easier dragging
				Vector3f newSunPos = new Vector3f(newPosition).normalize().mul(sunDistance);

				Vector3f newDirection = new Vector3f(newSunPos).negate().normalize();
				light.setDirection(newDirection.x, newDirection.y, newDirection.z);
				sunVisual.setPosition(newSunPos);

				// Also update the ray's transform to follow the sun ---
				updateRayTransform(rayVisual, newSunPos);
			});
			visuals.add(sunVisual);
			visuals.add(rayVisual);
		}
		return visuals;
	}

	/**
	 * Correctly calculates and applies the position and rotation for the directional light's ray visualizer.
	 * This method handles the complex 3D transformation needed to position and orient the ray geometry
	 * so it appears to connect the origin with the sun visualizer, providing clear visual feedback
	 * about the light direction.
	 * 
	 * @param ray the SceneObject representing the directional light ray
	 * @param sunPosition the current position of the "sun" visualizer that represents the light source
	 */
	private void updateRayTransform(SceneObject ray, Vector3f sunPosition) {
		// The ray should point from the origin towards the sun.
		Vector3f targetDir = new Vector3f(sunPosition).normalize();
		// The center of the ray should be halfway between the origin and the sun.
		Vector3f rayCenter = new Vector3f(sunPosition).mul(0.5f);

		Matrix4f modelMatrix = ray.getModelMatrix();

		// The TubeGenerator creates a cylinder along the +X axis. We need a rotation that aims it along the targetDir.
		// JOML's rotationTowards method rotates from the +Z axis. So, we first apply a corrective
		// rotation to align our object's default +X axis with the world's +Z axis.
		Matrix4f correction = new Matrix4f().rotateY((float) Math.toRadians(-90));
		Matrix4f mainRotation = new Matrix4f().rotationTowards(targetDir, new Vector3f(0, 1, 0));
		Matrix4f finalRotation = mainRotation.mul(correction);

		// Build the final transform: first translate to the center, then apply the rotation.
		// This ensures the cylinder pivots correctly around its center point.
		modelMatrix.identity()
				.translate(rayCenter)
				.mul(finalRotation);
	}
}