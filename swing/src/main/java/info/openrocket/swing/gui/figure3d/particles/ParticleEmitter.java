package info.openrocket.swing.gui.figure3d.particles;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Abstract base class for particle emitters that create and manage particles over time.
 * Supports both continuous emission and burst modes, as well as static particle capture.
 */
public abstract class ParticleEmitter {
	protected List<Particle> particles;
	protected List<Particle> staticParticles;
	protected Vector3f emitterPosition;
	protected Vector3f direction;
	protected float timeSinceLastCreation;
	protected final ParticleSettings settings;

	protected boolean isStaticMode;
	protected float staticCaptureTime;
	protected float currentTime;
	protected final long baseSeed;
	protected Random random;

	/**
	 * Creates a new particle emitter.
	 * @param emitterPosition the 3D position where particles are emitted
	 * @param direction the base direction for particle emission
	 * @param settings configuration for particle creation and behavior
	 */
	public ParticleEmitter(Vector3f emitterPosition, Vector3f direction, ParticleSettings settings) {
		this.particles = new ArrayList<>();
		this.staticParticles = new ArrayList<>();
		this.emitterPosition = new Vector3f(emitterPosition);
		this.direction = new Vector3f(direction);
		this.settings = settings;
		this.timeSinceLastCreation = 0;
		this.isStaticMode = false;
		this.staticCaptureTime = 0.0f;
		this.currentTime = 0.0f;
		this.baseSeed = computeBaseSeed(emitterPosition, direction);
		this.random = new Random(baseSeed);
	}

	/**
	 * Updates the emitter, managing particle lifecycle and creating new particles.
	 * @param deltaTime time elapsed since last update in seconds
	 */
	public void update(float deltaTime) {
		currentTime += deltaTime;

		if (isStaticMode) {
			// In static mode, don't update particles or create new ones
			return;
		}

		updateParticleList(deltaTime);

		// Create new particles
		emitPendingParticles(deltaTime);
	}

	/**
	 * Creates a new particle with emitter-specific properties.
	 * Implementation varies by emitter type (flame, smoke, spark, etc.).
	 */
	protected abstract void createParticle();

	public List<Particle> getParticles() {
		return isStaticMode ? staticParticles : particles;
	}

	public Vector3f getEmitterPosition() {
		return emitterPosition;
	}

	public Vector3f getDirection() {
		return direction;
	}

	public void setEmitterPosition(Vector3f position) {
		this.emitterPosition.set(position);
	}

	public void setDirection(Vector3f direction) {
		this.direction.set(direction);
	}

	protected float nextRandomFloat() {
		return random.nextFloat();
	}

	protected float nextRandomSignedFloat() {
		return random.nextFloat() - 0.5f;
	}

	protected float getNoiseTime() {
		return currentTime;
	}

	/**
	 * Captures a static snapshot of particles at a specific time for performance.
	 * @param captureTime simulation time to capture particles at
	 */
	public void captureStaticParticles(float captureTime) {
		this.staticCaptureTime = captureTime;
		simulateToTime(captureTime);
		this.isStaticMode = true;
	}

	public void setStaticMode(boolean staticMode) {
		this.isStaticMode = staticMode;
		if (!staticMode) {
			staticParticles.clear();
		}
	}

	public boolean isStaticMode() {
		return isStaticMode;
	}

	public float getStaticCaptureTime() {
		return staticCaptureTime;
	}

	/**
	 * Fast-forwards simulation to capture particles at a target time.
	 * @param targetTime the simulation time to advance to
	 */
	private void simulateToTime(float targetTime) {
		// Reset to initial state
		particles.clear();
		staticParticles.clear();
		timeSinceLastCreation = 0;
		currentTime = 0;
		random = new Random(baseSeed);

		// Simulate up to target time with small time steps
		float timeStep = 0.016f; // ~60 FPS time step

		while (currentTime < targetTime) {
			float deltaTime = Math.min(timeStep, targetTime - currentTime);

			updateParticleList(deltaTime);

			// Create new particles
			emitPendingParticles(deltaTime);

			currentTime += deltaTime;
		}

		// Copy current particles to static particles
		for (Particle p : particles) {
			staticParticles.add(p.clone());
		}
	}

	private void updateParticleList(float deltaTime) {
		for (int i = particles.size() - 1; i >= 0; i--) {
			if (!particles.get(i).update(deltaTime, settings.gravity)) {
				particles.remove(i);
			}
		}
	}

	private void emitPendingParticles(float deltaTime) {
		if (settings.burst && !particles.isEmpty()) {
			return;
		}

		timeSinceLastCreation += deltaTime;
		float qualityAdjustedCreationRate = settings.getQualityAdjustedCreationRate();
		if (qualityAdjustedCreationRate <= 0.0f) {
			return;
		}

		int requestedParticles = (int) (timeSinceLastCreation * qualityAdjustedCreationRate);
		int availableSlots = RenderingConstants.DEFAULT_MAX_PARTICLES - particles.size();
		int newParticles = Math.min(requestedParticles, Math.max(0, availableSlots));
		if (newParticles <= 0) {
			if (availableSlots <= 0) {
				timeSinceLastCreation = Math.min(timeSinceLastCreation, 1.0f / qualityAdjustedCreationRate);
			}
			return;
		}

		for (int i = 0; i < newParticles; i++) {
			createParticle();
		}
		timeSinceLastCreation -= newParticles / qualityAdjustedCreationRate;
		if (settings.burst) {
			// Stop creating particles after the burst by effectively setting creationRate to 0
			timeSinceLastCreation = Float.NEGATIVE_INFINITY;
		}
	}

	private long computeBaseSeed(Vector3f emitterPosition, Vector3f direction) {
		long seed = getClass().getName().hashCode();
		seed = 31L * seed + Float.floatToIntBits(emitterPosition.x);
		seed = 31L * seed + Float.floatToIntBits(emitterPosition.y);
		seed = 31L * seed + Float.floatToIntBits(emitterPosition.z);
		seed = 31L * seed + Float.floatToIntBits(direction.x);
		seed = 31L * seed + Float.floatToIntBits(direction.y);
		seed = 31L * seed + Float.floatToIntBits(direction.z);
		return seed;
	}
}
