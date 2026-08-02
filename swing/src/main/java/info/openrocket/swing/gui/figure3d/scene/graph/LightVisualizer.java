package info.openrocket.swing.gui.figure3d.scene.graph;

import info.openrocket.core.util.BoundingBox;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.basic.SphereGenerator;
import info.openrocket.swing.gui.figure3d.geometry.basic.TubeGenerator;
import info.openrocket.swing.gui.figure3d.geometry.RocketMeshBuilder;
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
	private static final Vector3f OUTLINE_COLOR = new Vector3f(0.08f, 0.08f, 0.08f);
	private static final float POINT_RADIUS = 0.4f;
	private static final float POINT_OUTLINE_RADIUS = 0.46f;
	private static final float SUN_RADIUS = 0.5f;
	private static final float SUN_OUTLINE_RADIUS = 0.535f;
	private static final float SUN_GLYPH_CORE_RADIUS = 0.09f;
	private static final int SUN_SPOKE_COUNT = 8;
	private static final float SUN_SPOKE_RADIUS = 0.012f;
	private static final float SUN_SPOKE_LENGTH = 0.09f;
	private static final float SUN_SPOKE_GAP = 0.085f;
	private static final float RAY_RADIUS = 0.012f;
	private static final float RAY_OUTLINE_RADIUS = 0.021f;
	/**
	 * How far from the rocket the directional light's "sun" marker sits. Dragging it
	 * only changes the direction, so this stays fixed; the create and update paths
	 * have to agree on it or the marker jumps the first time the light changes.
	 */
	private static final float SUN_DISTANCE = 10.0f;

	/**
	 * Creates one or more SceneObjects to visually represent a light source in the 3D scene.
	 * The generated visual representations allow users to see and interact with lights,
	 * making it easier to understand and adjust the lighting setup for optimal rocket visualization.
	 * 
	 * @param light the light source to create visual representations for
	 * @return a list of SceneObjects that make up the visual marker (may include multiple objects for directional lights)
	 */
	public List<SceneObject> createVisualsForLight(Light light, Scene scene) {
		List<SceneObject> visuals = new ArrayList<>();

		Appearance3D visualAppearance = createVisualizerAppearance(light.getColor(), 1.0f);
		Appearance3D outlineAppearance = createVisualizerAppearance(OUTLINE_COLOR);

		if (light.getType() == Light.LightType.POINT) {
			addPointLightVisuals(visuals, light, visualAppearance, outlineAppearance);
		} else {
			addDirectionalLightVisuals(visuals, light, scene, visualAppearance, outlineAppearance);
		}
		return visuals;
	}

	/** A small draggable sphere at the light's position, over an outline sphere. */
	private void addPointLightVisuals(List<SceneObject> visuals, Light light,
			Appearance3D visualAppearance, Appearance3D outlineAppearance) {
		Mesh outlineMesh = SphereGenerator.create(POINT_OUTLINE_RADIUS,
				RenderingConstants.HIGH_SEGMENT_COUNT, RenderingConstants.HIGH_SEGMENT_COUNT);
		SceneObject pointOutline = new SceneObject(outlineMesh, light.getPosition(), outlineAppearance);
		configureVisualizerObject(pointOutline, false);
		visuals.add(pointOutline);

		Mesh sphereMesh = SphereGenerator.create(POINT_RADIUS,
				RenderingConstants.HIGH_SEGMENT_COUNT, RenderingConstants.HIGH_SEGMENT_COUNT);
		SceneObject pointVisual = new SceneObject(sphereMesh, light.getPosition(), visualAppearance);
		configureVisualizerObject(pointVisual, true);

		pointVisual.setOnDragListener((newPosition) -> {
			light.setPosition(newPosition.x, newPosition.y, newPosition.z);
			pointOutline.setPosition(newPosition);
			pointVisual.setPosition(newPosition);
		});
		visuals.add(pointVisual);
	}

	/**
	 * A draggable "sun" sphere with a spoke glyph, held a fixed distance from the
	 * rocket, and a ray drawn back to it showing which way the light points.
	 */
	private void addDirectionalLightVisuals(List<SceneObject> visuals, Light light, Scene scene,
			Appearance3D visualAppearance, Appearance3D outlineAppearance) {
		Camera camera = scene.getCamera();
		Appearance3D rayAppearance = createVisualizerAppearance(light.getColor(), 1.0f);

		float sunDistance = SUN_DISTANCE;
		Vector3f lightDir = light.getDirection();
		Vector3f lightAnchor = getLightAnchor(scene);
		Vector3f sunPosition = new Vector3f(lightDir).negate().mul(sunDistance).add(lightAnchor);

		Mesh sunOutlineMesh = SphereGenerator.create(SUN_OUTLINE_RADIUS,
				RenderingConstants.HIGH_SEGMENT_COUNT, RenderingConstants.HIGH_SEGMENT_COUNT);
		SceneObject sunOutline = new SceneObject(sunOutlineMesh, sunPosition, outlineAppearance);
		configureVisualizerObject(sunOutline, false);
		visuals.add(sunOutline);

		Mesh sunMesh = SphereGenerator.create(SUN_RADIUS,
				RenderingConstants.HIGH_SEGMENT_COUNT, RenderingConstants.HIGH_SEGMENT_COUNT);
		SceneObject sunVisual = new SceneObject(sunMesh, sunPosition, visualAppearance);
		configureVisualizerObject(sunVisual, true);

		SceneObject rayOutline = addRay(visuals, RAY_OUTLINE_RADIUS, sunDistance, outlineAppearance);
		SceneObject rayVisual = addRay(visuals, RAY_RADIUS, sunDistance, rayAppearance);
		updateRayTransform(rayOutline, lightAnchor, sunPosition);
		updateRayTransform(rayVisual, lightAnchor, sunPosition);

		Mesh glyphCoreMesh = SphereGenerator.create(SUN_GLYPH_CORE_RADIUS,
				RenderingConstants.LOW_SEGMENT_COUNT, RenderingConstants.LOW_SEGMENT_COUNT);
		SceneObject glyphCore = new SceneObject(glyphCoreMesh, sunPosition, outlineAppearance);
		configureVisualizerObject(glyphCore, false);
		visuals.add(sunVisual);
		visuals.add(glyphCore);

		List<SceneObject> sunSpokes = addSunSpokes(visuals, outlineAppearance);
		updateSunSpokes(sunSpokes, sunPosition, camera);

		sunVisual.setOnDragListener((newPosition) -> {
			// The sun stays a fixed distance from the rocket; dragging only turns it.
			Vector3f dragAnchor = getLightAnchor(scene);
			Vector3f newSunPos = new Vector3f(newPosition).sub(dragAnchor);
			if (newSunPos.lengthSquared() < 1.0e-6f) {
				newSunPos.set(lightDir).negate();
			}
			newSunPos.normalize().mul(sunDistance).add(dragAnchor);

			Vector3f newDirection = new Vector3f(dragAnchor).sub(newSunPos).normalize();
			light.setDirection(newDirection.x, newDirection.y, newDirection.z);
			sunOutline.setPosition(newSunPos);
			sunVisual.setPosition(newSunPos);
			glyphCore.setPosition(newSunPos);

			updateSunSpokes(sunSpokes, newSunPos, camera);
			updateRayTransform(rayOutline, dragAnchor, newSunPos);
			updateRayTransform(rayVisual, dragAnchor, newSunPos);
		});
	}

	/** Adds one of the two concentric tubes making up the ray back to the sun. */
	private SceneObject addRay(List<SceneObject> visuals, float radius, float sunDistance,
			Appearance3D appearance) {
		Mesh mesh = TubeGenerator.create(radius, radius, 0, sunDistance,
				RenderingConstants.LOW_SEGMENT_COUNT, true);
		SceneObject ray = new SceneObject(mesh, new Vector3f(0, 0, 0), appearance);
		configureVisualizerObject(ray, false);
		visuals.add(ray);
		return ray;
	}

	/** Adds the spokes of the sun glyph; {@link #updateSunSpokes} then places them. */
	private List<SceneObject> addSunSpokes(List<SceneObject> visuals, Appearance3D outlineAppearance) {
		Mesh spokeMesh = TubeGenerator.create(SUN_SPOKE_RADIUS, SUN_SPOKE_RADIUS, 0, SUN_SPOKE_LENGTH,
				RenderingConstants.LOW_SEGMENT_COUNT, true);
		List<SceneObject> sunSpokes = new ArrayList<>();
		for (int i = 0; i < SUN_SPOKE_COUNT; i++) {
			SceneObject spoke = new SceneObject(spokeMesh, new Vector3f(0, 0, 0), outlineAppearance);
			configureVisualizerObject(spoke, false);
			sunSpokes.add(spoke);
			visuals.add(spoke);
		}
		return sunSpokes;
	}

	public void updateVisualsForLight(Light light, List<SceneObject> visuals, Scene scene) {
		if (visuals == null || visuals.isEmpty()) {
			return;
		}
		Camera camera = scene.getCamera();

		if (light.getType() == Light.LightType.POINT) {
			SceneObject pointOutline = visuals.get(0);
			SceneObject pointVisual = visuals.get(1);
			pointOutline.setPosition(light.getPosition());
			pointVisual.setPosition(light.getPosition());
			return;
		}

		if (visuals.size() < 3 + 2 + SUN_SPOKE_COUNT) {
			return;
		}

		float sunDistance = SUN_DISTANCE;
		Vector3f lightAnchor = getLightAnchor(scene);
		Vector3f sunPosition = new Vector3f(light.getDirection()).negate().mul(sunDistance).add(lightAnchor);
		SceneObject sunOutline = visuals.get(0);
		SceneObject rayOutline = visuals.get(1);
		SceneObject rayVisual = visuals.get(2);
		SceneObject sunVisual = visuals.get(3);
		SceneObject glyphCore = visuals.get(4);
		List<SceneObject> sunSpokes = visuals.subList(5, 5 + SUN_SPOKE_COUNT);

		updateSunSpokes(sunSpokes, sunPosition, camera);
		sunOutline.setPosition(sunPosition);
		sunVisual.setPosition(sunPosition);
		glyphCore.setPosition(sunPosition);
		updateRayTransform(rayVisual, lightAnchor, sunPosition);
		updateRayTransform(rayOutline, lightAnchor, sunPosition);
	}

	private static Appearance3D createVisualizerAppearance(Vector3f color) {
		return createVisualizerAppearance(color, 1.0f);
	}

	private static Appearance3D createVisualizerAppearance(Vector3f color, float opacity) {
		Appearance3D appearance = new Appearance3D(color);
		appearance.setUnlit(true);
		appearance.setOpacity(opacity);
		return appearance;
	}

	private static void configureVisualizerObject(SceneObject object, boolean selectable) {
		object.setSelectable(selectable);
		object.setRenderOnTop(true);
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
	private void updateRayTransform(SceneObject ray, Vector3f anchorPosition, Vector3f sunPosition) {
		Vector3f targetDir = new Vector3f(sunPosition).sub(anchorPosition).normalize();
		Vector3f rayCenter = new Vector3f(anchorPosition).add(sunPosition).mul(0.5f);

		updateSegmentTransform(ray, rayCenter, targetDir);
	}

	private Vector3f getLightAnchor(Scene scene) {
		BoundingBox bounds = scene.getRocket().getBoundingBox();
		if (bounds == null || bounds.isEmpty()) {
			return new Vector3f();
		}
		Vector3f localCenter = new Vector3f(
				(float) ((bounds.min.getX() + bounds.max.getX()) * 0.5 * RocketMeshBuilder.WORLD_SCALE),
				(float) ((bounds.min.getY() + bounds.max.getY()) * 0.5 * RocketMeshBuilder.WORLD_SCALE),
				(float) ((bounds.min.getZ() + bounds.max.getZ()) * 0.5 * RocketMeshBuilder.WORLD_SCALE)
		);
		return scene.transformRocketPoint(localCenter, new Vector3f());
	}

	private void updateSunSpokes(List<SceneObject> sunSpokes, Vector3f sunPosition, Camera camera) {
		Vector3f planeNormal = new Vector3f(camera.getPosition()).sub(sunPosition);
		if (planeNormal.lengthSquared() < 1.0e-6f) {
			planeNormal.set(0.0f, 0.0f, 1.0f);
		} else {
			planeNormal.normalize();
		}
		Vector3f basisU = new Vector3f(planeNormal).cross(0.0f, 1.0f, 0.0f);
		if (basisU.lengthSquared() < 1.0e-6f) {
			basisU.set(planeNormal).cross(1.0f, 0.0f, 0.0f);
		}
		basisU.normalize();
		Vector3f basisV = new Vector3f(planeNormal).cross(basisU).normalize();

		float baseDistance = SUN_GLYPH_CORE_RADIUS + SUN_SPOKE_GAP + SUN_SPOKE_LENGTH * 0.5f;
		for (int i = 0; i < sunSpokes.size(); i++) {
			float angle = (float) (Math.PI * 2.0 * i / sunSpokes.size());
			Vector3f spokeDir = new Vector3f(basisU).mul((float) Math.cos(angle))
					.add(new Vector3f(basisV).mul((float) Math.sin(angle)))
					.normalize();
			Vector3f spokeCenter = new Vector3f(sunPosition).fma(baseDistance, spokeDir);
			updateSegmentTransform(sunSpokes.get(i), spokeCenter, spokeDir);
		}
	}

	private void updateSegmentTransform(SceneObject segment, Vector3f segmentCenter, Vector3f targetDir) {
		Matrix4f modelMatrix = segment.getModelMatrix();

		// The TubeGenerator creates a cylinder along the +X axis. We need a rotation that aims it along the targetDir.
		// JOML's rotationTowards method rotates from the +Z axis. So, we first apply a corrective
		// rotation to align our object's default +X axis with the world's +Z axis.
		Matrix4f correction = new Matrix4f().rotateY((float) Math.toRadians(-90));
		Matrix4f mainRotation = new Matrix4f().rotationTowards(targetDir, new Vector3f(0, 1, 0));
		Matrix4f finalRotation = mainRotation.mul(correction);

		// Build the final transform: first translate to the center, then apply the rotation.
		// This ensures the cylinder pivots correctly around its center point.
		modelMatrix.identity()
				.translate(segmentCenter)
				.mul(finalRotation);
	}
}
