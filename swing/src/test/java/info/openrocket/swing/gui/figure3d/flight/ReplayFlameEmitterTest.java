package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameSettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplayFlameEmitterTest {
	@Test
	void particlesStreamOutwardEvenWhenTheRocketIsStationary() {
		ReplayFlameEmitter emitter = emitter(new Vector3f(), new Quaternionf(), 17);
		emitter.setReplayTime(0.5001, true);
		List<Particle> before = snapshot(emitter);
		emitter.setReplayTime(0.5006, true);
		Particle old = before.get(before.size() - 1);
		Particle advanced = emitter.getParticles().stream().filter(p -> p.color.equals(old.color)
				&& p.size == old.size).findFirst().orElseThrow();
		assertTrue(advanced.position.x > old.position.x,
				"An emitted particle must move away from the nozzle as it ages");
		emitter.setReplayTime(0.57, true);
		assertNotEquals(before.get(before.size() - 1).color,
				emitter.getParticles().get(emitter.getParticles().size() - 1).color);
	}

	@Test
	void pauseSeekAndVisibilityChangesReconstructExactlyTheSameFlame() {
		ReplayFlameEmitter emitter = emitter(new Vector3f(), new Quaternionf(), 17);
		emitter.setReplayTime(0.5, true);
		List<Particle> expected = snapshot(emitter);
		emitter.update(20.0f);
		assertParticlesEqual(expected, emitter.getParticles());
		for (double time : new double[] { 0.9, 2.3, 0.1, 1.4 }) emitter.setReplayTime(time, true);
		emitter.setReplayTime(0.5, false);
		assertTrue(emitter.getParticles().isEmpty());
		emitter.setReplayTime(0.5, true);
		assertParticlesEqual(expected, emitter.getParticles());
	}

	@Test
	void ignitionBuildsThePlumeAndBurnoutLetsExistingParticlesFadeBeforeReignition() {
		ReplayFlameEmitter emitter = emitter(new Vector3f(), new Quaternionf(), 17);
		emitter.setReplayTime(-0.1, true);
		assertTrue(emitter.getParticles().isEmpty());
		emitter.setReplayTime(0.0301, true);
		int ignitionCount = emitter.getParticles().size();
		emitter.setReplayTime(0.999, true);
		int fullCount = emitter.getParticles().size();
		assertTrue(ignitionCount > 0 && ignitionCount < fullCount);
		assertTrue(fullCount <= 300);
		emitter.setReplayTime(1.09, true);
		assertFalse(emitter.getParticles().isEmpty());
		assertTrue(emitter.getParticles().size() < fullCount);
		assertTrue(emitter.getParticles().stream().allMatch(p -> p.getLife() <= 0.91f));
		emitter.setReplayTime(2.0, true);
		assertTrue(emitter.getParticles().isEmpty());
		emitter.setReplayTime(2.0301, true);
		assertEquals(ignitionCount, emitter.getParticles().size());
	}

	@Test
	void replayUsesTheExistingFlameParticlePropertiesAndPhysics() {
		FlameSettings settings = FlameSettings.normal(new RenderingConfiguration());
		PoseProvider provider = stationaryProvider(new Vector3f(), new Quaternionf());
		ReplayFlameEmitter replay = new ReplayFlameEmitter(settings, provider,
				List.of(new FlightReplayData.BurnInterval(0.0, 1.0)), new Vector3f(1, 0, 0), new Vector3f(1, 0, 0), 17);
		class ReferenceEmitter extends FlameEmitter {
			ReferenceEmitter(FlameSettings config) { super(new Vector3f(1, 0, 0), new Vector3f(1, 0, 0), config); }
			Particle emit(long index, double birthTime, float age) {
				random.setSeed(ReplayFlameEmitter.emissionSeed(17, 0, index));
				currentTime = (float) birthTime;
				createParticle();
				Particle particle = particles.remove(0);
				return particle.update(age, settings.gravity) ? particle : null;
			}
		}
		ReferenceEmitter reference = new ReferenceEmitter(settings);
		List<Particle> expected = new ArrayList<>();
		double time = 0.5;
		double rate = settings.getQualityAdjustedCreationRate();
		for (int i = 1; i <= (int) (time * rate); i++) {
			Particle particle = reference.emit(i, i / rate, (float) (time - i / rate));
			if (particle != null) expected.add(particle);
		}
		replay.setReplayTime(time, true);
		// The replay only rolls each particle about the exhaust axis (+X through the nozzle at
		// x=1), so everything that roll leaves unchanged must match the pad emitter exactly.
		List<Particle> actual = replay.getParticles();
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			Particle e = expected.get(i);
			Particle a = actual.get(i);
			assertEquals(e.color, a.color);
			assertEquals(e.orientation, a.orientation);
			assertEquals(e.size, a.size);
			assertEquals(e.getLife(), a.getLife());
			assertEquals(e.position.x, a.position.x, 1e-5f);
			assertEquals(radial(e.position), radial(a.position), 1e-5f);
			assertEquals(e.velocity.x, a.velocity.x, 1e-5f);
			assertEquals(radial(e.velocity), radial(a.velocity), 1e-5f);
		}
	}

	@Test
	void plumeIsSymmetricAboutTheExhaustAxis() {
		ReplayFlameEmitter emitter = emitter(new Vector3f(), new Quaternionf(), 17);
		emitter.setReplayTime(0.999, true);
		Vector3f lateralSum = new Vector3f();
		float radialSum = 0.0f;
		for (Particle particle : emitter.getParticles()) {
			lateralSum.add(0.0f, particle.position.y, particle.position.z);
			radialSum += radial(particle.position);
		}
		// The pad emitter pushes every particle toward +Y; unrolled, the mean offset would be
		// roughly half the mean radius.
		assertTrue(lateralSum.length() < 0.15f * radialSum,
				"Replay flame must not lean to one side of the rocket: " + lateralSum.length() / radialSum);
	}

	@Test
	void rocketScaledSettingsKeepThePadPlumeProportions() {
		RenderingConfiguration config = new RenderingConfiguration();
		FlameSettings pad = FlameSettings.normal(config, null, 1.0f, 3.0f);
		for (float scale : new float[] { 0.1f, 1.0f, 12.0f }) {
			FlameSettings scaled = ReplayFlameEmitter.rocketScaledSettings(config, scale);
			assertEquals(pad.spread / pad.velocity, scaled.spread / scaled.velocity, 1e-6f);
			assertEquals(pad.maxSize * scale, scaled.maxSize, 1e-5f);
		}
	}

	private static float radial(Vector3f vector) {
		return (float) Math.hypot(vector.y, vector.z);
	}

	@Test
	void plumeFollowsNozzleTransformAndClusteredMotorsHaveDifferentTurbulence() {
		ReplayFlameEmitter local = emitter(new Vector3f(), new Quaternionf(), 17);
		Vector3f base = new Vector3f(15, 22, -7);
		Quaternionf rotation = new Quaternionf().rotateZ((float) (Math.PI / 2));
		ReplayFlameEmitter transformed = emitter(base, rotation, 17);
		ReplayFlameEmitter secondMotor = emitter(new Vector3f(), new Quaternionf(), 48);
		for (ReplayFlameEmitter emitter : List.of(local, transformed, secondMotor)) emitter.setReplayTime(0.5, true);
		for (int i = 0; i < local.getParticles().size(); i++) {
			Vector3f expected = rotation.transform(new Vector3f(local.getParticles().get(i).position)).add(base);
			assertEquals(0.0f, expected.distance(transformed.getParticles().get(i).position), 1e-5f);
		}
		assertNotEquals(local.getParticles().get(30).position, secondMotor.getParticles().get(30).position);
	}

	@Test
	void plumeGrowsAndBrightensWithThrust() {
		FlameSettings settings = FlameSettings.normal(new RenderingConfiguration());
		PoseProvider provider = stationaryProvider(new Vector3f(), new Quaternionf());
		List<FlightReplayData.BurnInterval> burn = List.of(new FlightReplayData.BurnInterval(0.0, 1.0));
		Vector3f nozzle = new Vector3f(1, 0, 0);
		ReplayFlameEmitter spike = new ReplayFlameEmitter(settings, provider, burn, nozzle, nozzle, 17, time -> 1.6);
		ReplayFlameEmitter average = new ReplayFlameEmitter(settings, provider, burn, nozzle, nozzle, 17, time -> 1.0);
		ReplayFlameEmitter tailOff = new ReplayFlameEmitter(settings, provider, burn, nozzle, nozzle, 17, time -> 0.1);
		for (ReplayFlameEmitter emitter : List.of(spike, average, tailOff)) emitter.setReplayTime(0.5, true);

		assertTrue(meanReach(spike, nozzle) > meanReach(average, nozzle));
		assertTrue(meanReach(average, nozzle) > meanReach(tailOff, nozzle));
		assertEquals(1.0f, average.getParticles().get(0).getOpacity(), 1e-6f, "Average thrust keeps the pad plume");
		assertTrue(tailOff.getParticles().get(0).getOpacity() < 0.6f, "The tail-off fades");
		assertTrue(spike.getParticles().get(0).size > average.getParticles().get(0).size);
	}

	private static float meanReach(ReplayFlameEmitter emitter, Vector3f nozzle) {
		return (float) emitter.getParticles().stream().mapToDouble(p -> p.position.distance(nozzle)).average().orElse(0);
	}

	private static ReplayFlameEmitter emitter(Vector3f base, Quaternionf rotation, long seed) {
		return new ReplayFlameEmitter(FlameSettings.normal(new RenderingConfiguration()), stationaryProvider(base, rotation),
				List.of(new FlightReplayData.BurnInterval(0.0, 1.0), new FlightReplayData.BurnInterval(2.0, 3.0)),
				new Vector3f(1, 0, 0), new Vector3f(1, 0, 0), seed);
	}

	@Test
	void emittedParticlesInheritTheRocketsVelocity() {
		Vector3f velocity = new Vector3f(100, 200, -30);
		PoseProvider moving = new PoseProvider() {
			@Override public Vector3f getPosition(double time) { return new Vector3f(velocity).mul((float) time); }
			@Override public Quaternionf getOrientation(double time) { return new Quaternionf(); }
			@Override public Vector3f getLinearVelocity(double time) { return new Vector3f(velocity); }
			@Override public double getStartTime() { return 0; }
			@Override public double getEndTime() { return 5; }
		};
		ReplayFlameEmitter stationary = emitter(new Vector3f(), new Quaternionf(), 17);
		ReplayFlameEmitter translated = new ReplayFlameEmitter(FlameSettings.normal(new RenderingConfiguration()),
				moving, List.of(new FlightReplayData.BurnInterval(0, 1)), new Vector3f(1, 0, 0), new Vector3f(1, 0, 0), 17);
		stationary.setReplayTime(0.5, true);
		translated.setReplayTime(0.5, true);
		assertEquals(stationary.getParticles().size(), translated.getParticles().size());
		for (int i = 0; i < stationary.getParticles().size(); i++) {
			Vector3f expected = new Vector3f(stationary.getParticles().get(i).position).fma(0.5f, velocity);
			assertEquals(0, expected.distance(translated.getParticles().get(i).position), 2e-5f);
		}
	}

	private static PoseProvider stationaryProvider(Vector3f base, Quaternionf rotation) {
		return new PoseProvider() {
			@Override public Vector3f getPosition(double time) { return new Vector3f(base); }
			@Override public Quaternionf getOrientation(double time) { return new Quaternionf(rotation); }
			@Override public double getStartTime() { return 0.0; }
			@Override public double getEndTime() { return 5.0; }
		};
	}

	private static List<Particle> snapshot(ReplayFlameEmitter emitter) {
		return emitter.getParticles().stream().map(Particle::clone).toList();
	}

	private static void assertParticlesEqual(List<Particle> expected, List<Particle> actual) {
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).position, actual.get(i).position);
			assertEquals(expected.get(i).color, actual.get(i).color);
			assertEquals(expected.get(i).velocity, actual.get(i).velocity);
			assertEquals(expected.get(i).orientation, actual.get(i).orientation);
			assertEquals(expected.get(i).size, actual.get(i).size);
			assertEquals(expected.get(i).getLife(), actual.get(i).getLife());
		}
	}
}
