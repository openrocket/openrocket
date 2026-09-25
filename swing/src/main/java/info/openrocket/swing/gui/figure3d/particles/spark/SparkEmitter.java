package info.openrocket.swing.gui.figure3d.particles.spark;

import info.openrocket.swing.gui.figure3d.particles.Particle;
import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.ParticleSettings;
import org.joml.Vector3f;

/**
 * Particle emitter for glowing fragments carried by the motor exhaust.
 *
 * <p>Most sparks stay inside a concentrated exhaust cone, while a small number
 * are emitted as wider, slower fragments.  Size, lifetime, and temperature are
 * varied together so the result does not resemble a uniform spray of lines.</p>
 */
public class SparkEmitter extends ParticleEmitter {
	private static final float TWO_PI = (float) (Math.PI * 2.0);
	private static final float MIN_DIRECTION_LENGTH_SQUARED = 1.0e-8f;
	private static final float WIDE_FRAGMENT_CHANCE = 0.09f;

	private final Vector3f emissionDirection = new Vector3f();
	private final Vector3f tangent = new Vector3f();
	private final Vector3f bitangent = new Vector3f();

	/**
	 * Creates a spark emitter.
	 * @param emitterPosition where sparks are emitted from
	 * @param direction base direction of spark emission
	 * @param settings particle configuration for spark behavior
	 */
	public SparkEmitter(Vector3f emitterPosition, Vector3f direction, ParticleSettings settings) {
		super(emitterPosition, direction, settings);
	}

	/**
	 * Creates a hot fragment with a predominantly axial velocity and a small
	 * radial component.  A minority of fragments receive a wider radial impulse.
	 */
	@Override
	protected void createParticle() {
		prepareEmissionBasis();

		// Most fragments are tiny.  Larger pieces retain heat for longer.
		float sizeFactor = (float) Math.pow(nextRandomFloat(), 2.2);
		float size = mix(settings.minSize, settings.maxSize, sizeFactor);
		float lifeFactor = 0.55f * sizeFactor + 0.45f * nextRandomFloat();
		float life = mix(settings.minLife, settings.maxLife, lifeFactor);

		float azimuth = nextRandomFloat() * TWO_PI;
		float radialX = (float) Math.cos(azimuth);
		float radialY = (float) Math.sin(azimuth);
		boolean wideFragment = nextRandomFloat() < WIDE_FRAGMENT_CHANCE;
		float radialDistribution = (float) Math.pow(nextRandomFloat(), 2.0);
		float radialSpeedFactor = wideFragment
				? 0.20f + 0.25f * radialDistribution
				: 0.025f + 0.24f * radialDistribution;
		float radialSpeed = settings.spread * radialSpeedFactor;
		float axialSpeed = settings.velocity * (0.70f + 0.65f * nextRandomFloat());
		if (wideFragment) {
			axialSpeed *= 0.72f + 0.18f * nextRandomFloat();
		}

		Vector3f velocity = new Vector3f(emissionDirection).mul(axialSpeed)
				.fma(radialSpeed * radialX, tangent)
				.fma(radialSpeed * radialY, bitangent);

		// Scatter origins over a very small nozzle disk instead of one exact point.
		float originAzimuth = nextRandomFloat() * TWO_PI;
		float originRadius = size * 0.75f * (float) Math.sqrt(nextRandomFloat());
		Vector3f position = new Vector3f(emitterPosition)
				.fma(originRadius * (float) Math.cos(originAzimuth), tangent)
				.fma(originRadius * (float) Math.sin(originAzimuth), bitangent);

		// Bias the initial temperature toward pale yellow, retaining some orange fragments.
		float colorFactor = 0.25f + 0.75f * (float) Math.sqrt(nextRandomFloat());

		// Linearly interpolate between min and max color
		float r = settings.minColor.x + colorFactor * (settings.maxColor.x - settings.minColor.x);
		float g = settings.minColor.y + colorFactor * (settings.maxColor.y - settings.minColor.y);
		float b = settings.minColor.z + colorFactor * (settings.maxColor.z - settings.minColor.z);
		Vector3f color = new Vector3f(r, g, b);

		particles.add(new Particle(position, velocity, color, size, life));
	}

	private void prepareEmissionBasis() {
		emissionDirection.set(direction);
		if (emissionDirection.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
			emissionDirection.set(1.0f, 0.0f, 0.0f);
		} else {
			emissionDirection.normalize();
		}

		if (Math.abs(emissionDirection.y) < 0.9f) {
			tangent.set(-emissionDirection.z, 0.0f, emissionDirection.x).normalize();
		} else {
			tangent.set(0.0f, emissionDirection.z, -emissionDirection.y).normalize();
		}
		bitangent.set(emissionDirection).cross(tangent).normalize();
	}

	private static float mix(float start, float end, float amount) {
		return start + amount * (end - start);
	}
}
