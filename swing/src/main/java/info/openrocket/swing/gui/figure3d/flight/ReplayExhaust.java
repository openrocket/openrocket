package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.simulation.FlightEvent;
import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.flight.FlightReplayData.BurnInterval;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.smoke.SmokeEmitter;
import info.openrocket.swing.gui.figure3d.particles.smoke.SmokeSettings;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.orchestration.Scene3DOrchestrator.MotorExhaustMount;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The replay's exhaust: a smoke column along each burn, white puffs where ejection charges fire
 * and stages separate, and a flame plume per motor. Everything shown is a pure function of the
 * playback time, so any scrub shows exactly what continuous playback would have: smoke puff
 * positions and birth times are laid along the flown path up front and drift with the recorded
 * wind, and the flames are reconstructed per frame (see {@link ReplayFlameEmitter}). The real
 * volumetric-smoke and flame renderers draw them through emitters whose wall-clock simulation is
 * a no-op. All methods run on the render thread.
 */
final class ReplayExhaust {
	private static final Vector3f SMOKE_COLOR = new Vector3f(0.80f, 0.80f, 0.83f);
	private static final Vector3f BURST_COLOR = new Vector3f(0.96f, 0.96f, 0.97f);
	// Puffs render small when fresh and expand to full size over this many seconds, so in the
	// follow view fresh smoke does not engulf the rocket.
	static final double SMOKE_GROWTH_SECONDS = 5.0;
	static final double SMOKE_LIFETIME_SECONDS = 10.0;
	// Keep the renderer at the start of its built-in fade after growth; replay smoke uses
	// explicit per-particle opacity so size and transparency can evolve independently.
	private static final float SMOKE_FADE_START_RATIO = 0.8f;
	// A slow buoyant rise of the hanging smoke, in puff sizes per second.
	private static final float SMOKE_RISE_RATE = 0.02f;
	private static final int SMOKE_PATH_SAMPLES = 256;
	private static final int MAX_PUFFS_PER_BURN = 800;
	private static final int SMOKE_PARTICLES_PER_PUFF = 2;
	private static final int EJECTION_BURST_PUFFS = 12;
	private static final int SEPARATION_BURST_PUFFS = 6;

	/**
	 * One smoke particle: revealed at its birth time at a fixed world position, then carried by
	 * the wind recorded at that moment (engine units per second).
	 */
	record SmokePuff(Vector3f position, double birthTime, float size, Vector3f color, Vector3f drift) {
		SmokePuff(Vector3f position, double birthTime, float size, Vector3f color) {
			this(position, birthTime, size, color, new Vector3f());
		}
	}

	/** One evenly spaced exhaust position and its interpolated flight time. */
	record SmokeStation(Vector3f position, double time) {
	}

	private final List<SmokePuff> smokePuffs = new ArrayList<>();
	private final List<ReplayFlameEmitter> flames = new ArrayList<>();
	private SmokeEmitter smoke;

	/**
	 * Lays out the smoke and adds the emitters to the scene.
	 *
	 * @param mounts       each motor's nozzle position and exhaust direction, rocket-local
	 * @param trailRadius  the trajectory trail's radius, which sizes smoke at flight scale
	 * @param rocketLength the rocket's length, which caps the smoke and sizes the flames
	 */
	void build(SceneView scene, RenderingConfiguration config, List<MotorExhaustMount> mounts, ReplayPoses poses,
			FlightReplayData replayData, float trailRadius, float rocketLength) {
		// The smoke renderer draws a particle at up to 4x its size. Cap the trail-scaled size
		// against the rocket so exhaust also reads naturally in the follow view.
		float puffSize = replaySmokeSize(trailRadius, rocketLength);
		smoke = new SmokeEmitter(new Vector3f(), new Vector3f(0.0f, 1.0f, 0.0f), SmokeSettings.medium(config)) {
			@Override
			public void update(float deltaTime) {
				// The replay fills the particles as a function of playback time.
			}
		};
		scene.addParticleEmitter(smoke);

		for (Map.Entry<AxialStage, List<BurnInterval>> entry : replayData.getBurnIntervalsByStage().entrySet()) {
			if (entry.getValue().isEmpty()) {
				continue;
			}
			PoseProvider provider = poses.providerForStage(entry.getKey());
			ReplayPoses.TrackedBody body = poses.bodyFor(provider);
			for (MotorExhaustMount mount : mounts) {
				if (ReplayPoses.stageFor(mount.mountComponent()) != entry.getKey()) {
					continue;
				}
				for (BurnInterval burn : entry.getValue()) {
					addSmokeColumn(provider, body.wind(), mount.nozzlePosition(), burn, puffSize);
				}
				ReplayFlameEmitter flame = new ReplayFlameEmitter(config, provider, entry.getValue(),
						mount.nozzlePosition(), mount.exhaustDirection(), rocketLength, 31L * flames.size() + 17,
						ThrustProfile.fromBranch(body.branch(), entry.getValue()));
				scene.addParticleEmitter(flame);
				flames.add(flame);
			}
		}
		addEventBursts(replayData, poses, puffSize);
	}

	int puffCount() {
		return smokePuffs.size();
	}

	int flameCount() {
		return flames.size();
	}

	/** Shows the exhaust as it is at the given playback time, or clears it when hidden. */
	void update(double time, boolean visible) {
		if (smoke != null) {
			if (visible) {
				updateSmokeParticles(smoke.getParticles(), smokePuffs, time);
			} else {
				smoke.getParticles().clear();
			}
		}
		for (ReplayFlameEmitter flame : flames) {
			flame.setReplayTime(time, visible);
		}
	}

	/**
	 * Lays smoke puffs along the path flown during one burn, a small cluster per fixed distance
	 * travelled (so the column is spatially uniform however fast the rocket moves), with
	 * deterministic jitter so it reads as a smoke column rather than beads.
	 */
	private void addSmokeColumn(PoseProvider provider, WindField wind, Vector3f nozzleLocal, BurnInterval burn,
			float puffSize) {
		Random jitter = new Random(Double.hashCode(burn.start()) * 31L + smokePuffs.size());
		float spread = puffSize * 0.4f;
		for (SmokeStation station : sampleSmokeStations(provider, nozzleLocal, burn.start(), burn.end(),
				puffSize * 1.2f, MAX_PUFFS_PER_BURN)) {
			for (int j = 0; j < SMOKE_PARTICLES_PER_PUFF; j++) {
				float size = puffSize * (0.7f + 0.6f * jitter.nextFloat());
				Vector3f center = new Vector3f(station.position()).add(
						(jitter.nextFloat() - 0.5f) * spread,
						(jitter.nextFloat() - 0.5f) * spread,
						(jitter.nextFloat() - 0.5f) * spread);
				smokePuffs.add(new SmokePuff(center, station.time(), size, SMOKE_COLOR, wind.velocityAt(station.time())));
			}
		}
	}

	/**
	 * Adds a burst of white puffs where an ejection charge fires and a smaller one where a stage
	 * separates, so the events read visually along the flight.
	 */
	private void addEventBursts(FlightReplayData replayData, ReplayPoses poses, float puffSize) {
		for (FlightEvent event : replayData.getAllEvents()) {
			int puffs = switch (event.getType()) {
				case EJECTION_CHARGE -> EJECTION_BURST_PUFFS;
				case STAGE_SEPARATION -> SEPARATION_BURST_PUFFS;
				default -> 0;
			};
			double time = event.getTime();
			if (puffs == 0 || time < replayData.getStartTime() || time > replayData.getEndTime()) {
				continue;
			}
			ReplayPoses.TrackedBody body = poses.bodyFor(poses.providerFor(event.getSource()));
			Vector3f center = body.centerAt(time);
			Vector3f drift = body.wind().velocityAt(time);
			Random jitter = new Random(Double.hashCode(time) * 127L + puffs);
			float scatter = puffSize * 1.5f;
			for (int i = 0; i < puffs; i++) {
				Vector3f position = new Vector3f(center).add(
						(jitter.nextFloat() - 0.5f) * 2.0f * scatter,
						(jitter.nextFloat() - 0.5f) * 2.0f * scatter,
						(jitter.nextFloat() - 0.5f) * 2.0f * scatter);
				float size = puffSize * (0.8f + 0.6f * jitter.nextFloat());
				smokePuffs.add(new SmokePuff(position, time, size, BURST_COLOR, drift));
			}
		}
	}

	static float replaySmokeSize(float pathRadius, float rocketLength) {
		// A high apogee must not turn fresh exhaust into a cloud larger than the rocket.
		return Math.max(0.01f, Math.min(pathRadius * 0.5f, rocketLength * 0.08f));
	}

	/**
	 * Converts the time-sampled nozzle path into exact spatial intervals. A fast rocket may
	 * cross several intervals between adjacent samples, so every crossing is interpolated
	 * instead of emitting only one puff for the whole sampled segment.
	 */
	static List<SmokeStation> sampleSmokeStations(PoseProvider provider, Vector3f nozzleLocal,
			double burnStart, double burnEnd, float spacing, int maximumStations) {
		if (!Float.isFinite(spacing) || spacing <= 0.0f || maximumStations <= 0
				|| !Double.isFinite(burnStart) || !Double.isFinite(burnEnd) || burnEnd < burnStart) {
			return List.of();
		}

		// Fit the budget to the entire burn instead of silently cutting the plume off midway.
		List<Vector3f> path = new ArrayList<>(SMOKE_PATH_SAMPLES + 1);
		double length = 0.0;
		for (int i = 0; i <= SMOKE_PATH_SAMPLES; i++) {
			Vector3f position = ReplayPoses.pointOnBody(provider, nozzleLocal,
					burnStart + (burnEnd - burnStart) * i / SMOKE_PATH_SAMPLES);
			if (!path.isEmpty()) {
				length += position.distance(path.get(path.size() - 1));
			}
			path.add(position);
		}
		double interval = maximumStations > 1 ? Math.max(spacing, length / (maximumStations - 1)) : spacing;
		List<SmokeStation> stations = new ArrayList<>(maximumStations);
		double previousTime = burnStart;
		Vector3f previousPosition = path.get(0);
		stations.add(new SmokeStation(new Vector3f(previousPosition), previousTime));
		double travelled = 0.0;
		double nextStationDistance = interval;

		for (int i = 1; i <= SMOKE_PATH_SAMPLES && stations.size() < maximumStations; i++) {
			double time = burnStart + (burnEnd - burnStart) * i / SMOKE_PATH_SAMPLES;
			Vector3f position = path.get(i);
			double segmentLength = position.distance(previousPosition);
			double segmentEndDistance = travelled + segmentLength;

			while (nextStationDistance <= segmentEndDistance + 1e-6 && stations.size() < maximumStations) {
				double fraction = segmentLength > 0.0
						? Math.min(1.0, (nextStationDistance - travelled) / segmentLength) : 0.0;
				Vector3f stationPosition = new Vector3f(previousPosition).lerp(position, (float) fraction);
				double stationTime = previousTime + (time - previousTime) * fraction;
				stations.add(new SmokeStation(stationPosition, stationTime));
				nextStationDistance += interval;
			}

			travelled = segmentEndDistance;
			previousPosition = position;
			previousTime = time;
		}
		return List.copyOf(stations);
	}

	/** Fills the particle list with the puffs alive at the given time, reusing particles. */
	static void updateSmokeParticles(List<Particle> particles, List<SmokePuff> puffs, double time) {
		int count = 0;
		for (SmokePuff puff : puffs) {
			double age = time - puff.birthTime();
			if (age < 0.0 || age >= SMOKE_LIFETIME_SECONDS) {
				continue;
			}
			Particle particle = count < particles.size() ? particles.get(count) : appendBlank(particles);
			particle.position.set(puff.position())
					.fma((float) age, puff.drift())
					.add(0.0f, (float) age * SMOKE_RISE_RATE * puff.size(), 0.0f);
			particle.color.set(puff.color());
			particle.size = puff.size();
			particle.setLifetime(1.0f - smokeAgeRatio(age), 1.0f);
			particle.setOpacity(smokeOpacity(age));
			count++;
		}
		while (particles.size() > count) {
			particles.remove(particles.size() - 1);
		}
	}

	/** The renderer's size-over-life position for a puff of the given age: grows, then holds. */
	static float smokeAgeRatio(double age) {
		return SMOKE_FADE_START_RATIO * (float) Math.max(0.0, Math.min(1.0, age / SMOKE_GROWTH_SECONDS));
	}

	/** Fades a puff in quickly and out over its lifetime. */
	static float smokeOpacity(double age) {
		double fadeIn = Math.max(0.0, Math.min(1.0, age / 0.15));
		double remaining = Math.max(0.0, Math.min(1.0, 1.0 - age / SMOKE_LIFETIME_SECONDS));
		return (float) (fadeIn * fadeIn * (3.0 - 2.0 * fadeIn) * remaining * remaining * (3.0 - 2.0 * remaining));
	}

	private static Particle appendBlank(List<Particle> particles) {
		Particle particle = new Particle(new Vector3f(), new Vector3f(), new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 1.0f);
		particles.add(particle);
		return particle;
	}
}
