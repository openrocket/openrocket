package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReplayExhaustTest {
	@Test
	void smokeStationBudgetCoversTheEntireBurn() {
		PoseProvider provider = mock(PoseProvider.class);
		when(provider.getPosition(anyDouble())).thenAnswer(invocation ->
				new Vector3f((float) (invocation.getArgument(0, Double.class) * 1_000.0), 0.0f, 0.0f));
		when(provider.getOrientation(anyDouble())).thenReturn(new Quaternionf());
		List<ReplayExhaust.SmokeStation> stations = ReplayExhaust.sampleSmokeStations(
				provider, new Vector3f(), 0.0, 10.0, 1.0f, 100);
		assertEquals(100, stations.size());
		assertEquals(0.0, stations.get(0).time());
		assertEquals(10.0, stations.get(99).time(), 1e-6);
		assertEquals(10_000.0f, stations.get(99).position().x, 1e-3f);
	}

	@Test
	void exhaustSizeStaysProportionalToTheRocketOnHighFlights() {
		assertEquals(0.8f, ReplayExhaust.replaySmokeSize(100.0f, 10.0f), 1e-6f);
		assertEquals(0.8f, ReplayExhaust.replaySmokeSize(1_000.0f, 10.0f), 1e-6f);
		assertEquals(0.0f, ReplayExhaust.smokeOpacity(0.0));
		assertTrue(ReplayExhaust.smokeOpacity(0.05) < ReplayExhaust.smokeOpacity(0.15));
	}

	@Test
	void smokeScrubbingReconstructsTheSameParticlesAfterRewinding() {
		List<Particle> particles = new ArrayList<>();
		List<ReplayExhaust.SmokePuff> puffs = List.of(new ReplayExhaust.SmokePuff(
				new Vector3f(1.0f, 2.0f, 3.0f), 1.0, 0.5f, new Vector3f(0.8f)));
		ReplayExhaust.updateSmokeParticles(particles, puffs, 3.0);
		Vector3f expectedPosition = new Vector3f(particles.get(0).position);
		float expectedOpacity = particles.get(0).getOpacity();
		ReplayExhaust.updateSmokeParticles(particles, puffs, 20.0);
		assertTrue(particles.isEmpty());
		ReplayExhaust.updateSmokeParticles(particles, puffs, 3.0);
		assertEquals(expectedPosition, particles.get(0).position);
		assertEquals(expectedOpacity, particles.get(0).getOpacity());
		ReplayExhaust.updateSmokeParticles(particles, puffs, 0.0);
		assertTrue(particles.isEmpty());
	}

	@Test
	void smokeDriftsWithTheWindRecordedAtItsRelease() {
		List<Particle> particles = new ArrayList<>();
		Vector3f wind = new Vector3f(10.0f, 0.0f, -5.0f);
		List<ReplayExhaust.SmokePuff> puffs = List.of(new ReplayExhaust.SmokePuff(
				new Vector3f(1.0f, 2.0f, 3.0f), 1.0, 0.5f, new Vector3f(0.8f), wind));

		ReplayExhaust.updateSmokeParticles(particles, puffs, 4.0);

		// Three seconds downwind; buoyancy only lifts it.
		assertEquals(31.0f, particles.get(0).position.x, 1e-4f);
		assertEquals(-12.0f, particles.get(0).position.z, 1e-4f);
		assertTrue(particles.get(0).position.y > 2.0f);
	}

	@Test
	void smokeStationsRemainContinuousWhenSeveralIntervalsAreCrossedPerPathSample() {
		PoseProvider provider = mock(PoseProvider.class);
		when(provider.getPosition(anyDouble())).thenAnswer(invocation ->
				new Vector3f((float) (invocation.getArgument(0, Double.class) * 1_000.0), 0.0f, 0.0f));
		when(provider.getOrientation(anyDouble())).thenReturn(new Quaternionf());

		List<ReplayExhaust.SmokeStation> stations = ReplayExhaust.sampleSmokeStations(
				provider, new Vector3f(), 0.0, 1.0, 3.0f, 400);

		assertEquals(334, stations.size());
		for (int i = 1; i < stations.size(); i++) {
			assertEquals(3.0f, stations.get(i - 1).position().distance(stations.get(i).position()), 1e-4f);
			assertEquals(stations.get(i).position().x / 1_000.0, stations.get(i).time(), 1e-6);
		}
	}

	@Test
	void replaySmokeGrowsFadesVisiblyAndRemovesExpiredParticles() {
		List<Particle> particles = new ArrayList<>();
		List<ReplayExhaust.SmokePuff> puffs = List.of(new ReplayExhaust.SmokePuff(
				new Vector3f(1.0f, 2.0f, 3.0f), 0.0, 4.0f, new Vector3f(0.8f)));

		ReplayExhaust.updateSmokeParticles(particles, puffs, 0.0);
		assertEquals(1, particles.size());
		assertEquals(1.0f, particles.get(0).getLife(), 1e-6f);

		ReplayExhaust.updateSmokeParticles(particles, puffs,
				ReplayExhaust.SMOKE_LIFETIME_SECONDS / 2.0);
		assertEquals(0.5f, particles.get(0).getOpacity(), 1e-6f,
				"Replay smoke must visibly fade instead of only changing its size-age ratio");

		ReplayExhaust.updateSmokeParticles(particles, puffs,
				ReplayExhaust.SMOKE_LIFETIME_SECONDS);
		assertTrue(particles.isEmpty(), "A fully transparent smoke puff must be removed");
	}
}
