package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.flight.FlightReplayData.BurnInterval;
import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameSettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

/** Adapts the existing flame emitter to absolute replay time and moving motor nozzles. */
final class ReplayFlameEmitter extends FlameEmitter {
	private final PoseProvider provider;
	private final List<BurnInterval> burnWindows;
	private final long seed;
	private final float particleTimeScale;
	private final Vector3f exhaustAxis;
	private final Quaternionf roll = new Quaternionf();
	private final DoubleUnaryOperator relativeThrust;

	ReplayFlameEmitter(RenderingConfiguration config, PoseProvider provider, List<BurnInterval> burnWindows,
			Vector3f nozzle, Vector3f direction, float rocketLength, long seed, ThrustProfile thrust) {
		// The pad/photo emitter's long lifetimes would leave burning particles far behind an
		// accelerating rocket. Run the same particle lifecycle faster, preserving its plume length.
		this(rocketScaledSettings(config, rocketLength * 0.03f), provider, burnWindows, nozzle, direction, seed, 24.0f,
				thrust::relativeThrustAt);
	}

	/**
	 * The stock settings scale velocity and size with the exhaust scale but keep an absolute
	 * spread tuned for scale 1, which makes a small rocket's plume wide and a large one's a
	 * needle. Scale the spread too, so every rocket gets the pad plume's proportions.
	 */
	static FlameSettings rocketScaledSettings(RenderingConfiguration config, float exhaustScale) {
		FlameSettings settings = FlameSettings.normal(config, null, exhaustScale, 3.0f);
		return settings.withSpread(settings.spread * exhaustScale);
	}

	ReplayFlameEmitter(FlameSettings settings, PoseProvider provider, List<BurnInterval> burnWindows,
			Vector3f nozzle, Vector3f direction, long seed) {
		this(settings, provider, burnWindows, nozzle, direction, seed, 1.0f, time -> 1.0);
	}

	ReplayFlameEmitter(FlameSettings settings, PoseProvider provider, List<BurnInterval> burnWindows,
			Vector3f nozzle, Vector3f direction, long seed, DoubleUnaryOperator relativeThrust) {
		this(settings, provider, burnWindows, nozzle, direction, seed, 1.0f, relativeThrust);
	}

	private ReplayFlameEmitter(FlameSettings settings, PoseProvider provider, List<BurnInterval> burnWindows,
			Vector3f nozzle, Vector3f direction, long seed, float particleTimeScale,
			DoubleUnaryOperator relativeThrust) {
		super(nozzle, direction, settings);
		this.provider = provider;
		this.burnWindows = List.copyOf(burnWindows);
		this.exhaustAxis = new Vector3f(direction).normalize();
		this.seed = seed;
		this.particleTimeScale = particleTimeScale;
		this.relativeThrust = relativeThrust;
	}

	@Override
	public void update(float deltaTime) {
		// The replay supplies absolute time; the scene must not integrate wall-clock deltas too.
	}

	void setReplayTime(double time, boolean visible) {
		particles.clear();
		double rate = settings.getQualityAdjustedCreationRate() * particleTimeScale;
		double maximumAge = settings.maxLife / particleTimeScale;
		if (!visible || !Double.isFinite(time) || rate <= 0 || settings.maxLife <= 0) return;

		for (int windowIndex = 0; windowIndex < burnWindows.size(); windowIndex++) {
			BurnInterval window = burnWindows.get(windowIndex);
			if (time < window.start() || time >= window.end() + maximumAge || window.end() <= window.start()) continue;
			// Only the last maximum lifetime can affect this frame, even after a long seek.
			long first = Math.max(1, (long) Math.floor((time - maximumAge - window.start()) * rate) + 1);
			long last = (long) Math.floor((Math.min(time, Math.nextDown(window.end())) - window.start()) * rate);
			for (long index = first; index <= last; index++) {
				double birthTime = window.start() + index / rate;
				float age = (float) (time - birthTime);
				if (age < 0) continue;

				// Reuse FlameEmitter's noise, colors, size, lifetime, velocity and spin.
				// Seeding each emission makes it reconstructible without replaying the whole burn.
				random.setSeed(emissionSeed(seed, windowIndex, index));
				currentTime = (float) (birthTime * particleTimeScale);
				super.createParticle();
				Particle particle = particles.get(particles.size() - 1);
				if (!particle.update(age * particleTimeScale, settings.gravity)) {
					particles.remove(particles.size() - 1);
					continue;
				}

				rollAroundExhaustAxis(particle, emissionSeed(~seed, windowIndex, index));
				applyThrust(particle, relativeThrust.applyAsDouble(birthTime));

				Quaternionf orientation = provider.getOrientation(birthTime);
				orientation.transform(particle.position);
				particle.position.add(provider.getPosition(birthTime));
				orientation.transform(particle.velocity);
				particle.velocity.mul(particleTimeScale);
				Vector3f inheritedVelocity = provider.getLinearVelocity(birthTime);
				if (inheritedVelocity != null) {
					particle.position.fma(age, inheritedVelocity);
					particle.velocity.add(inheritedVelocity);
				}
			}
		}
	}

	/**
	 * FlameEmitter biases particles toward its local +Y so pad flames rise; in the replay that
	 * axis is fixed to the rocket and points sideways, skewing the plume. A deterministic roll
	 * about the exhaust axis keeps each particle's spread but makes the plume symmetric.
	 */
	private void rollAroundExhaustAxis(Particle particle, long rollSeed) {
		float angle = (float) ((rollSeed >>> 11) * 0x1.0p-53 * 2.0 * Math.PI);
		roll.fromAxisAngleRad(exhaustAxis, angle);
		particle.position.sub(emitterPosition);
		roll.transform(particle.position).add(emitterPosition);
		roll.transform(particle.velocity);
	}

	/**
	 * Scales a particle by the thrust when it left the nozzle, relative to the burn's average:
	 * a longer, wider, brighter plume through the ignition spike, a short fading one in the
	 * tail-off, and the unchanged pad plume at average thrust.
	 */
	private void applyThrust(Particle particle, double relative) {
		float intensity = (float) Math.max(0.25, Math.min(1.6, Double.isFinite(relative) ? relative : 1.0));
		float length = 0.4f + 0.6f * intensity;
		particle.position.sub(emitterPosition).mul(length).add(emitterPosition);
		particle.velocity.mul(length);
		particle.size *= 0.6f + 0.4f * intensity;
		particle.setOpacity(0.4f + 0.6f * intensity);
	}

	static long emissionSeed(long motorSeed, int windowIndex, long particleIndex) {
		long value = particleIndex + motorSeed * 31 + windowIndex * 104729L;
		value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
		value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
		return value ^ (value >>> 31);
	}
}
