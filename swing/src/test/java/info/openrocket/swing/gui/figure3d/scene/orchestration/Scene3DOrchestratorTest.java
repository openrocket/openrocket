package info.openrocket.swing.gui.figure3d.scene.orchestration;

import info.openrocket.core.motor.Motor;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.figure3d.geometry.RocketSceneSnapshot;
import org.joml.Vector3f;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Scene3DOrchestratorTest {
	@Test
	void derivesNozzlePositionAndDirectionFromRenderedMotorGeometry() {
		RocketComponent mount = mock(RocketComponent.class);
		Motor motor = mock(Motor.class);
		when(motor.getLength()).thenReturn(0.2);
		RocketSceneSnapshot.ParticleEmitterPlan plan = new RocketSceneSnapshot.ParticleEmitterPlan(
				mount,
				new org.joml.Vector3f(2.0f, 3.0f, 4.0f),
				new Matrix4f().rotateZ((float) (Math.PI / 2.0)),
				motor,
				10.0f);

		Scene3DOrchestrator.MotorExhaustMount result =
				Scene3DOrchestrator.createMotorExhaustMounts(List.of(plan)).get(0);

		assertSame(mount, result.mountComponent());
		assertEquals(0.0f, result.exhaustDirection().x, 1e-5f);
		assertEquals(1.0f, result.exhaustDirection().y, 1e-5f);
		assertEquals(2.0f, result.nozzlePosition().x, 1e-5f);
		assertEquals(4.0f, result.nozzlePosition().y, 1e-5f);
		assertEquals(4.0f, result.nozzlePosition().z, 1e-5f);
	}
}
