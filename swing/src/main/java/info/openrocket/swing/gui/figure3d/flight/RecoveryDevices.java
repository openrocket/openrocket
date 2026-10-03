package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.geometry.basic.SphereGenerator;
import info.openrocket.swing.gui.figure3d.geometry.basic.TrajectoryTrailGenerator;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Deployed recovery devices, shown above their stage from deployment until touchdown so the
 * slowed descent makes sense: a canopy with shroud lines for a parachute, a ribbon for a
 * streamer, sized from the component and in its paint color. Each opens with a slight
 * overshoot, then sways (or flutters) gently while staying upright however the stage tumbles.
 * Everything shown is a function of the playback time. All methods run on the render thread.
 */
final class RecoveryDevices {
	private static final int PARACHUTE_PANEL_COUNT = 8;
	private static final float PARACHUTE_CANOPY_FLATTENING = 0.42f;
	static final double INFLATION_SECONDS = 0.6;
	private static final float PACKED_SCALE = 0.15f;
	private static final float SWAY_DEGREES = 6.0f;
	private static final double SWAY_PERIOD_SECONDS = 2.4;
	private static final double STREAMER_FLUTTER_PERIOD_SECONDS = 0.9;
	private static final Vector3f DEFAULT_CANOPY_COLOR = new Vector3f(0.92f, 0.18f, 0.12f);
	private static final Vector3f CANOPY_TRIM_TINT = new Vector3f(1.0f, 0.95f, 0.85f);
	private static final Vector3f LINE_COLOR = new Vector3f(0.90f, 0.86f, 0.70f);

	/** One deployed device's scene objects and where and when it shows. */
	private record Deployment(boolean streamer, List<SceneObject> canopy, List<SceneObject> lines,
			PoseProvider provider, Vector3f attachment, double deployTime, double endTime, float lineLength,
			float swayPhase) {
		private Deployment {
			attachment = new Vector3f(attachment);
		}
	}

	/** The rendered size of a device: canopy radius and line length, or streamer width and length. */
	record RecoverySize(boolean streamer, float width, float length) {
	}

	record ParachuteGeometry(List<Mesh> canopyPanels, List<Mesh> suspensionLines, float lineLength) {
	}

	private final List<Deployment> deployments = new ArrayList<>();

	/** Adds a hidden device for each deployment event. */
	void build(SceneView scene, FlightReplayData replayData, ReplayPoses poses, float rocketLength) {
		for (FlightEvent event : replayData.getAllEvents()) {
			if (event.getType() != FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
				continue;
			}
			RocketComponent device = event.getSource();
			RecoverySize size = recoverySize(device, rocketLength);
			Vector3f color = recoveryColor(device);
			// Deterministic per device, so two canopies do not swing in lockstep.
			float swayPhase = (float) (2.0 * Math.PI * ((deployments.size() * 0.37) % 1.0));

			List<SceneObject> canopy = new ArrayList<>();
			List<SceneObject> lines = new ArrayList<>();
			if (size.streamer()) {
				canopy.add(addHidden(scene, createStreamerGeometry(size.width(), size.length()), fabric(color)));
			} else {
				ParachuteGeometry geometry = createParachuteGeometry(size.width(), size.length());
				Vector3f trim = new Vector3f(color).lerp(CANOPY_TRIM_TINT, 0.65f);
				for (int i = 0; i < geometry.canopyPanels().size(); i++) {
					canopy.add(addHidden(scene, geometry.canopyPanels().get(i), fabric(i % 2 == 0 ? color : trim)));
				}
				for (Mesh lineMesh : geometry.suspensionLines()) {
					Appearance3D lineAppearance = new Appearance3D(new Vector3f(LINE_COLOR));
					lineAppearance.setUnlit(true);
					lines.add(addHidden(scene, lineMesh, lineAppearance));
				}
			}
			deployments.add(new Deployment(size.streamer(), canopy, lines, poses.providerFor(device),
					findComponentAnchor(scene, device), event.getTime(),
					replayData.getGroundHitTime(event, replayData.getEndTime()),
					size.streamer() ? 0.0f : size.length(), swayPhase));
		}
	}

	int count() {
		return deployments.size();
	}

	/** Poses each device deployed at the given time above its attachment, and hides the rest. */
	void update(double time) {
		Matrix4f swing = new Matrix4f();
		for (Deployment deployment : deployments) {
			boolean deployed = time >= deployment.deployTime() && time <= deployment.endTime();
			deployment.canopy().forEach(object -> object.setVisible(deployed));
			deployment.lines().forEach(object -> object.setVisible(deployed));
			if (!deployed) {
				continue;
			}
			Vector3f anchor = ReplayPoses.pointOnBody(deployment.provider(), deployment.attachment(), time);
			double age = time - deployment.deployTime();
			float opening = recoveryOpening(age);
			double swayCycle = 2.0 * Math.PI * age / SWAY_PERIOD_SECONDS + deployment.swayPhase();
			float sway = (float) Math.toRadians(SWAY_DEGREES);
			// Swing about the attachment point, in two directions at slightly different rates.
			swing.translation(anchor)
					.rotateX(sway * (float) Math.sin(swayCycle))
					.rotateZ(0.6f * sway * (float) Math.sin(1.3 * swayCycle + 1.0));
			if (deployment.streamer()) {
				float flutter = (float) (2.0 * Math.PI * age / STREAMER_FLUTTER_PERIOD_SECONDS + deployment.swayPhase());
				for (SceneObject ribbon : deployment.canopy()) {
					ribbon.getModelMatrix().set(swing).rotateY(flutter).scale(1.0f, opening, 1.0f);
				}
				continue;
			}
			// The canopy geometry is built around +Z; stand it up above the lines.
			for (SceneObject panel : deployment.canopy()) {
				panel.getModelMatrix().set(swing)
						.translate(0.0f, deployment.lineLength(), 0.0f)
						.rotateX((float) (-Math.PI / 2.0))
						.scale(opening, opening, PARACHUTE_CANOPY_FLATTENING * opening);
			}
			for (SceneObject line : deployment.lines()) {
				line.getModelMatrix().set(swing)
						.translate(0.0f, deployment.lineLength(), 0.0f)
						.rotateX((float) (-Math.PI / 2.0))
						.scale(opening, opening, 1.0f);
			}
		}
	}

	/**
	 * Sizes a recovery device from its component: a parachute's diameter and shroud line
	 * length, or a streamer's strip width and length. Kept within sensible multiples of the
	 * rocket so an unusual design never hides the rocket or vanishes.
	 */
	static RecoverySize recoverySize(RocketComponent device, float rocketLength) {
		float scale = RenderingConstants.WORLD_SCALE;
		if (device instanceof Streamer streamer) {
			return new RecoverySize(true,
					clamp((float) streamer.getStripWidth() * scale, 0.02f * rocketLength, 0.5f * rocketLength),
					clamp((float) streamer.getStripLength() * scale, 0.5f * rocketLength, 8.0f * rocketLength));
		}
		if (device instanceof Parachute parachute) {
			return new RecoverySize(false,
					clamp((float) parachute.getDiameter() * 0.5f * scale, 0.2f * rocketLength, 4.0f * rocketLength),
					clamp((float) parachute.getLineLength() * scale, 0.3f * rocketLength, 4.0f * rocketLength));
		}
		return new RecoverySize(false, 0.6f * rocketLength, 0.9f * rocketLength);
	}

	/**
	 * How far a device has opened after deployment: packed small, then filling out with a
	 * slight overshoot before settling at full size.
	 */
	static float recoveryOpening(double age) {
		double progress = Math.max(0.0, Math.min(1.0, age / INFLATION_SECONDS));
		double overshoot = 1.4;
		double shifted = progress - 1.0;
		double eased = 1.0 + (overshoot + 1.0) * shifted * shifted * shifted + overshoot * shifted * shifted;
		return (float) (PACKED_SCALE + (1.0 - PACKED_SCALE) * eased);
	}

	/** Returns the rendered component origin so the harness starts where the packed device sits. */
	static Vector3f findComponentAnchor(SceneView scene, RocketComponent component) {
		if (scene == null || component == null) {
			return new Vector3f();
		}
		for (SceneObject object : scene.getObjects()) {
			if (object.getRocketComponent() == component) {
				return object.getModelMatrix().transformPosition(new Vector3f());
			}
		}
		return new Vector3f();
	}

	/** Builds an open, shallow canopy with radial suspension lines converging on the stage. */
	static ParachuteGeometry createParachuteGeometry(float rocketLength) {
		return createParachuteGeometry(rocketLength * 0.6f, rocketLength * 0.9f);
	}

	static ParachuteGeometry createParachuteGeometry(float radius, float lineLength) {
		List<Mesh> panels = new ArrayList<>(PARACHUTE_PANEL_COUNT);
		List<Mesh> lines = new ArrayList<>(PARACHUTE_PANEL_COUNT);
		for (int i = 0; i < PARACHUTE_PANEL_COUNT; i++) {
			float startAngle = (float) (2.0 * Math.PI * i / PARACHUTE_PANEL_COUNT);
			float endAngle = (float) (2.0 * Math.PI * (i + 1) / PARACHUTE_PANEL_COUNT);
			Mesh panel = SphereGenerator.create(radius, 3, 6, 0.0f, (float) (Math.PI / 2.0), startAngle, endAngle);
			panels.add(doubleSided(panel));

			float angle = (startAngle + endAngle) * 0.5f;
			Vector3f rim = new Vector3f(radius * (float) Math.cos(angle), radius * (float) Math.sin(angle), 0.0f);
			Vector3f harness = new Vector3f(0.0f, 0.0f, -lineLength);
			lines.add(TrajectoryTrailGenerator.create(List.of(rim, harness), radius * 0.008f, 5));
		}
		return new ParachuteGeometry(List.copyOf(panels), List.copyOf(lines), lineLength);
	}

	/**
	 * A streamer ribbon rising from its attachment at the origin along +Y, with a gentle
	 * S-curve so it reads as cloth; visible from both sides.
	 */
	static Mesh createStreamerGeometry(float width, float length) {
		int segments = 16;
		List<Vertex> vertices = new ArrayList<>((segments + 1) * 2);
		IntList indices = new IntList(segments * 12);
		for (int i = 0; i <= segments; i++) {
			float along = (float) i / segments;
			float wave = 0.35f * width * (float) Math.sin(3.0 * Math.PI * along) * along;
			float y = along * length;
			vertices.add(ribbonVertex(-width * 0.5f, y, wave));
			vertices.add(ribbonVertex(width * 0.5f, y, wave));
			if (i > 0) {
				int a = 2 * (i - 1);
				int b = a + 1;
				int c = a + 2;
				int d = a + 3;
				indices.addTriangle(a, b, d);
				indices.addTriangle(a, d, c);
				indices.addTriangle(a, d, b);
				indices.addTriangle(a, c, d);
			}
		}
		return new Mesh(vertices, indices);
	}

	private static Vertex ribbonVertex(float x, float y, float z) {
		return new Vertex(new Vector3f(x, y, z), new Vector3f(0, 0, 1), new Vector2f(),
				RenderingConstants.SURFACE_ID_OUTSIDE);
	}

	private static Mesh doubleSided(Mesh mesh) {
		IntList source = mesh.getIndices();
		IntList indices = new IntList(source.size() * 2);
		indices.addAll(source);
		for (int i = 0; i + 2 < source.size(); i += 3) {
			indices.addTriangle(source.get(i), source.get(i + 2), source.get(i + 1));
		}
		return new Mesh(mesh.getVertices(), indices);
	}

	/** The device's painted color, or a classic red canopy. */
	private static Vector3f recoveryColor(RocketComponent device) {
		Appearance appearance = device != null ? device.getAppearance() : null;
		ORColor paint = appearance != null ? appearance.getPaint() : null;
		if (paint == null) {
			return new Vector3f(DEFAULT_CANOPY_COLOR);
		}
		return new Vector3f(paint.getRed() / 255.0f, paint.getGreen() / 255.0f, paint.getBlue() / 255.0f);
	}

	private static Appearance3D fabric(Vector3f color) {
		Appearance3D appearance = new Appearance3D(new Vector3f(color));
		appearance.setShine(0.08f);
		return appearance;
	}

	private static SceneObject addHidden(SceneView scene, Mesh mesh, Appearance3D appearance) {
		SceneObject object = new SceneObject(mesh, new Vector3f(), appearance);
		object.setSelectable(false);
		object.setVisible(false);
		scene.addObject(object);
		return object;
	}

	private static float clamp(float value, float min, float max) {
		return Float.isFinite(value) ? Math.max(min, Math.min(max, value)) : min;
	}
}
