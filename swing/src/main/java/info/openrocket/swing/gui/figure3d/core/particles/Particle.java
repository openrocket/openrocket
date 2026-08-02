package info.openrocket.swing.gui.figure3d.core.particles;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Represents a single particle in a 3D particle system.
 * Supports position, velocity, color, size, lifetime, and 3D rotation.
 */
public class Particle implements Cloneable {
	public static final Vector3f GRAVITY = new Vector3f(0, -9.8f, 0);
	public Vector3f position;
	public Vector3f velocity;
	public Vector3f color;
	public float size;
	public float life;
	public Quaternionf orientation;
	public Vector3f angularVelocity;

	private float maxLife;
	private final Vector3f scratchVector = new Vector3f();
	private final Vector3f scratchAxis = new Vector3f();
	private final Quaternionf scratchRotation = new Quaternionf();

	/**
	 * Creates a particle with basic properties and default orientation.
	 * @param position initial position in 3D space
	 * @param velocity initial velocity vector
	 * @param color RGB color components
	 * @param size particle size
	 * @param life lifetime in seconds
	 */
	public Particle(Vector3f position, Vector3f velocity, Vector3f color, float size, float life) {
		this.position = new Vector3f(position);
		this.velocity = new Vector3f(velocity);
		this.color = new Vector3f(color);
		this.size = size;
		this.life = life;
		this.maxLife = life;
		this.orientation = new Quaternionf();
		this.angularVelocity = new Vector3f();
	}
	
	/**
	 * Creates a particle with full properties including 3D rotation.
	 * @param position initial position in 3D space
	 * @param velocity initial velocity vector
	 * @param color RGB color components
	 * @param size particle size
	 * @param life lifetime in seconds
	 * @param orientation initial 3D rotation quaternion
	 * @param angularVelocity rotation speed around each axis
	 */
	public Particle(Vector3f position, Vector3f velocity, Vector3f color, float size, float life, Quaternionf orientation, Vector3f angularVelocity) {
		this.position = new Vector3f(position);
		this.velocity = new Vector3f(velocity);
		this.color = new Vector3f(color);
		this.size = size;
		this.life = life;
		this.maxLife = life;
		this.orientation = new Quaternionf(orientation);
		this.angularVelocity = new Vector3f(angularVelocity);
	}
	
	/**
	 * Creates a deep copy of this particle.
	 * @return a new Particle with identical properties
	 */
	public Particle clone() {
		Particle cloned = new Particle(
			new Vector3f(this.position),
			new Vector3f(this.velocity), 
			new Vector3f(this.color),
			this.size,
			this.life,
			new Quaternionf(this.orientation),
			new Vector3f(this.angularVelocity)
		);
		cloned.maxLife = this.maxLife; // Preserve original maxLife
		return cloned;
	}

	/**
	 * Updates particle physics including position, velocity, rotation, and lifetime.
	 * @param deltaTime time elapsed since last update in seconds
	 * @param gravity gravity vector to apply to velocity
	 * @return true if particle is still alive, false if expired
	 */
	public boolean update(float deltaTime, Vector3f gravity) {
		life -= deltaTime;
		if (life > 0) {
			velocity.add(scratchVector.set(gravity).mul(deltaTime));
			position.add(scratchVector.set(velocity).mul(deltaTime));
			
			// Update 3D rotation using angular velocity
			float angularSpeedSquared = angularVelocity.lengthSquared();
			if (angularSpeedSquared > 0) {
				float angularSpeed = (float) Math.sqrt(angularSpeedSquared);
				float angle = angularSpeed * deltaTime;
				scratchAxis.set(angularVelocity).mul(1.0f / angularSpeed);
				orientation.mul(scratchRotation.identity().rotateAxis(angle, scratchAxis));
			}
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Updates particle using default gravity.
	 * @param deltaTime time elapsed since last update in seconds
	 * @return true if particle is still alive, false if expired
	 */
	public boolean update(float deltaTime) {
		return update(deltaTime, GRAVITY);
	}
	
	// Getter methods
	public Vector3f getPosition() { return position; }
	public Vector3f getColor() { return color; }
	public float getSize() { return size; }
	public float getLife() { return life; }
	public float getMaxLife() { return maxLife; }
	public Quaternionf getOrientation() { return orientation; }
}
