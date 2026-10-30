package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.basic.AxesGenerator;
import info.openrocket.swing.gui.figure3d.geometry.basic.SphereGenerator;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.GradientBackground;
import info.openrocket.swing.gui.figure3d.scene.graph.Scene;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Vector3f;

/**
 * The replay's surroundings: a sky, open ground with a mown launch field that fades into the
 * horizon through scale-aware haze, and a launch pad with its rod and a compass rose whose
 * arrows match the orientation gizmo's colors. Added once per replay on the render thread.
 */
final class LaunchSiteScenery {
	private static final Vector3f SKY_COLOR = new Vector3f(0.18f, 0.48f, 0.82f);
	private static final Vector3f HORIZON_COLOR = new Vector3f(0.76f, 0.87f, 0.96f);
	// Haze and ground scale with the flight, but never below this much scenery around the pad.
	private static final float MIN_SCENE_EXTENT_METERS = 100.0f;
	private static final float HAZE_FRAMING_DISTANCES = 6.0f;
	private static final float LAUNCH_FIELD_HALF_SIZE_METERS = 120.0f;
	private static final float MOWN_STRIPE_WIDTH_METERS = 8.0f;

	private LaunchSiteScenery() {
	}

	static void addSky(SceneView scene) {
		scene.setBackground(GradientBackground.worldAligned(SKY_COLOR, HORIZON_COLOR));
	}

	/**
	 * Adds the ground and a scale-aware haze. Fog reaches full density several times farther
	 * than a camera framing the whole flight stands (the replay lens is narrow, so that is
	 * already several flight sizes away), keeping the flight clear, while the ground, which
	 * extends further still, fades into the horizon instead of ending at an edge.
	 *
	 * @param flightSize  the largest dimension of the flight's bounds
	 * @param fieldOfView the replay camera's lens, which sets how far a framing camera stands
	 */
	static void addGroundAndHaze(Scene scene, float flightSize, float fieldOfView) {
		float extent = Math.max(flightSize, MIN_SCENE_EXTENT_METERS * RenderingConstants.WORLD_SCALE);
		float hazeDistance = framingDistance(extent, fieldOfView) * HAZE_FRAMING_DISTANCES;
		// exp(-(d * density)^2) falls to about 5% at the haze distance.
		scene.setFogDensity(1.73f / hazeDistance);
		scene.setFogEnabled(true);

		for (LaunchFieldGround.Patch patch : LaunchFieldGround.create(hazeDistance * 1.5f,
				LAUNCH_FIELD_HALF_SIZE_METERS * RenderingConstants.WORLD_SCALE,
				MOWN_STRIPE_WIDTH_METERS * RenderingConstants.WORLD_SCALE)) {
			scene.addObject(scenery(patch.mesh(), patch.color()));
		}
	}

	/** Adds a pad disc with a launch rod at the origin and a compass rose around it. */
	static void addLaunchPad(SceneView scene, float rocketLength) {
		// Pad disc, half sunk into the ground.
		SceneObject pad = scenery(SphereGenerator.create(rocketLength * 1.5f, 24, 12), new Vector3f(0.32f, 0.32f, 0.34f));
		pad.getModelMatrix().scaling(1.0f, 0.08f, 1.0f);
		scene.addObject(pad);

		// Launch rod, standing beside the rocket. The arrow mesh points along +X; stand it
		// upright with its base on the pad.
		float rodLength = rocketLength * 1.4f;
		SceneObject rod = scenery(AxesGenerator.createArrowMesh(rodLength, rocketLength * 0.015f,
				rocketLength * 0.02f, rocketLength * 0.015f), new Vector3f(0.55f, 0.55f, 0.58f));
		rod.getModelMatrix().translation(0.0f, rodLength * 0.5f, rocketLength * 0.08f).rotateZ((float) (Math.PI / 2.0));
		scene.addObject(rod);

		// Compass rose: engine north is -Z, east +X, south +Z, west -X.
		record CompassArrow(float yawRadians, Vector3f color) {
		}
		CompassArrow[] arrows = {
				new CompassArrow((float) (Math.PI / 2.0), new Vector3f(0.95f, 0.27f, 0.27f)),
				new CompassArrow(0.0f, new Vector3f(0.32f, 0.82f, 0.42f)),
				new CompassArrow((float) (-Math.PI / 2.0), new Vector3f(0.40f, 0.58f, 1.0f)),
				new CompassArrow((float) Math.PI, new Vector3f(1.0f, 0.82f, 0.22f))
		};
		float arrowLength = rocketLength * 1.2f;
		float arrowDistance = rocketLength * 2.4f;
		float arrowHeadRadius = rocketLength * 0.11f;
		for (CompassArrow arrow : arrows) {
			SceneObject compassArrow = scenery(AxesGenerator.createArrowMesh(arrowLength, rocketLength * 0.05f,
					rocketLength * 0.45f, arrowHeadRadius), arrow.color());
			// Turn the +X arrow to its direction and push it out from the pad, lifted so the
			// arrowhead clears the ground instead of clipping into it.
			compassArrow.getModelMatrix()
					.rotationY(arrow.yawRadians())
					.translate(arrowDistance, arrowHeadRadius * 1.3f, 0.0f);
			scene.addObject(compassArrow);
		}
	}

	/** How far a camera stands to fit a flight of the given size through the given lens. */
	static float framingDistance(float extent, float fieldOfView) {
		float halfAngle = (float) Math.max(Math.toRadians(1.0), Math.min(Math.toRadians(80.0), fieldOfView / 2.0));
		return extent * 0.5f / (float) Math.tan(halfAngle);
	}

	private static SceneObject scenery(Mesh mesh, Vector3f color) {
		Appearance3D appearance = new Appearance3D(new Vector3f(color));
		appearance.setUnlit(true);
		SceneObject object = new SceneObject(mesh, new Vector3f(), appearance);
		object.setSelectable(false);
		return object;
	}
}
