package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.util.BaseTestCase;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecoveryDevicesTest extends BaseTestCase {
	private static final float SCALE = RenderingConstants.WORLD_SCALE;
	private static final float ROCKET_LENGTH = 1.0f * SCALE;

	@Test
	void parachuteIsSizedFromItsDiameterAndLines() {
		Parachute parachute = new Parachute();
		parachute.setDiameter(0.6);
		parachute.setLineLength(0.5);

		RecoveryDevices.RecoverySize size = RecoveryDevices.recoverySize(parachute, ROCKET_LENGTH);

		assertFalse(size.streamer());
		assertEquals(0.3f * SCALE, size.width(), 1e-4f, "Canopy radius is half the diameter");
		assertEquals(0.5f * SCALE, size.length(), 1e-4f);
	}

	@Test
	void streamerIsSizedFromItsStrip() {
		Streamer streamer = new Streamer();
		streamer.setStripLength(1.5);
		streamer.setStripWidth(0.05);

		RecoveryDevices.RecoverySize size = RecoveryDevices.recoverySize(streamer, ROCKET_LENGTH);

		assertTrue(size.streamer());
		assertEquals(0.05f * SCALE, size.width(), 1e-4f);
		assertEquals(1.5f * SCALE, size.length(), 1e-4f);
	}

	@Test
	void extremeOrUnknownDevicesStayInProportionToTheRocket() {
		Parachute tiny = new Parachute();
		tiny.setDiameter(0.001);
		assertEquals(0.2f * ROCKET_LENGTH, RecoveryDevices.recoverySize(tiny, ROCKET_LENGTH).width(), 1e-4f);

		RecoveryDevices.RecoverySize other = RecoveryDevices.recoverySize(new BodyTube(), ROCKET_LENGTH);
		assertFalse(other.streamer());
		assertEquals(0.6f * ROCKET_LENGTH, other.width(), 1e-4f);
	}

	@Test
	void deploymentOpensFromPackedWithAnOvershootThenSettles() {
		assertEquals(0.15f, RecoveryDevices.recoveryOpening(0.0), 1e-6f);
		float peak = 0.0f;
		for (double age = 0.0; age <= RecoveryDevices.INFLATION_SECONDS; age += 0.01) {
			peak = Math.max(peak, RecoveryDevices.recoveryOpening(age));
		}
		assertTrue(peak > 1.02f && peak < 1.2f, "A brief, modest overshoot: " + peak);
		assertEquals(1.0f, RecoveryDevices.recoveryOpening(RecoveryDevices.INFLATION_SECONDS), 1e-6f);
		assertEquals(1.0f, RecoveryDevices.recoveryOpening(30.0), 1e-6f);
	}

	@Test
	void streamerRibbonRisesFromItsAttachment() {
		Mesh ribbon = RecoveryDevices.createStreamerGeometry(2.0f, 30.0f);

		Vector3f min = ribbon.getBoundsMin(new Vector3f());
		Vector3f max = ribbon.getBoundsMax(new Vector3f());
		assertEquals(0.0f, min.y, 1e-5f);
		assertEquals(30.0f, max.y, 1e-4f);
		assertEquals(2.0f, max.x - min.x, 1e-4f);
	}

	@Test
	void parachuteAnchorUsesTheRenderedRecoveryDevicePosition() {
		SceneView scene = mock(SceneView.class);
		SceneObject recoveryDeviceObject = mock(SceneObject.class);
		info.openrocket.core.rocketcomponent.RocketComponent recoveryDevice =
				mock(info.openrocket.core.rocketcomponent.RocketComponent.class);
		when(recoveryDeviceObject.getRocketComponent()).thenReturn(recoveryDevice);
		when(recoveryDeviceObject.getModelMatrix()).thenReturn(
				new Matrix4f().translation(12.0f, 3.0f, -2.0f));
		when(scene.getObjects()).thenReturn(List.of(recoveryDeviceObject));

		Vector3f anchor = RecoveryDevices.findComponentAnchor(scene, recoveryDevice);

		assertEquals(new Vector3f(12.0f, 3.0f, -2.0f), anchor);
	}

	@Test
	void parachuteGeometryIsAnOpenDomeWithSuspensionLines() {
		RecoveryDevices.ParachuteGeometry geometry = RecoveryDevices.createParachuteGeometry(10.0f);

		assertEquals(8, geometry.canopyPanels().size());
		assertEquals(8, geometry.suspensionLines().size());
		assertEquals(9.0f, geometry.lineLength(), 1e-6f);

		for (Mesh panel : geometry.canopyPanels()) {
			Vector3f min = panel.getBoundsMin(new Vector3f());
			Vector3f max = panel.getBoundsMax(new Vector3f());
			assertTrue(min.z >= -1e-5f, "The canopy must not contain a lower hemisphere");
			assertTrue(max.z > 0.0f, "Each fabric panel must rise into a dome");
		}
		for (Mesh line : geometry.suspensionLines()) {
			assertEquals(-9.0f, line.getBoundsMin(new Vector3f()).z, 0.1f);
			assertTrue(line.getBoundsMax(new Vector3f()).z > -0.1f);
		}
	}
}
