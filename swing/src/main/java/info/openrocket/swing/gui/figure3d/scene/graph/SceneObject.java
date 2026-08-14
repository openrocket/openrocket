package info.openrocket.swing.gui.figure3d.scene.graph;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.figure3d.animation.PoseProvider;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.input.DragListener;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.rendering.Renderable;
import info.openrocket.swing.gui.figure3d.rendering.GLRenderableMesh;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.UUID;

/**
 * Mesh instance with its transform, appearance, interaction state, and optional
 * component associations.
 */
public class SceneObject {

	private final UUID id;
	private final RocketComponent rocketComponent;		// Selection and rocket-wide behavior
	private final RocketComponent appearanceSourceComponent;

	private final Mesh mesh; 							// The raw geometry data
	private final Renderable renderableMesh; 		// The GPU-ready version of the mesh
	private final Matrix4f modelMatrix = new Matrix4f();	// The model matrix that transforms the object in the scene
	private Appearance3D appearance;					// The appearance of this object, including materials and textures

	// --- Animation support ---
	private PoseProvider poseProvider = null;
	private Matrix4f baseModelSnapshot = null;   // captured once, the first time we animate
	private final Matrix4f dynamicTransform = new Matrix4f(); // T*R per-frame

	private boolean isSelected = false;					// Whether this object is currently selected
	private boolean isSelectable = true;				// Whether this object can be selected by the user
	private boolean renderOnTop = false;				// Whether this object should always render on top of others
	private boolean originAxis = false;
	private boolean cleaned = false;

	private DragListener onDragListener = null;		// A listener that defines what happens when this object is dragged.

	/**
	 * Constructs a new SceneObject associated with an OpenRocket component.
	 * This constructor creates the full object representation including both
	 * raw geometry and GPU-optimized rendering data.
	 *
	 * @param component the associated component for selection and rocket-wide behavior (may be null)
	 * @param mesh the raw geometric mesh data
	 * @param position the initial 3D position of the object
	 * @param appearance the visual appearance including materials and textures
	 */
	public SceneObject(RocketComponent component, Mesh mesh, Vector3f position, Appearance3D appearance) {
		this(component, component, mesh, position, appearance);
	}

	private SceneObject(RocketComponent component, RocketComponent appearanceSourceComponent,
			Mesh mesh, Vector3f position, Appearance3D appearance) {
		this(component, appearanceSourceComponent, mesh, new GLRenderableMesh(mesh), position, appearance);
	}

	private SceneObject(RocketComponent component, RocketComponent appearanceSourceComponent,
			Mesh mesh, Renderable renderableMesh, Vector3f position, Appearance3D appearance) {
		this.id = UUID.randomUUID();
		this.rocketComponent = component;
		this.appearanceSourceComponent = appearanceSourceComponent;
		this.mesh = mesh;
		this.renderableMesh = Objects.requireNonNull(renderableMesh, "renderableMesh");
		this.modelMatrix.translate(position);
		this.appearance = appearance;
	}

	/** Creates a component object using a caller-owned renderable lease. */
	public static SceneObject withRenderable(RocketComponent component, Mesh mesh, Renderable renderableMesh,
			Vector3f position, Appearance3D appearance) {
		return new SceneObject(component, component, mesh, renderableMesh, position, appearance);
	}

	/**
	 * Creates an object that remains associated with a component for selection and
	 * rocket-wide transforms, but whose appearance comes from another model object.
	 * Motor meshes use this form: they are grouped with their mount for selection,
	 * while retaining the motor label and material during mount appearance edits.
	 */
	public static SceneObject withIndependentAppearance(RocketComponent selectionComponent,
			Mesh mesh, Vector3f position, Appearance3D appearance) {
		return new SceneObject(selectionComponent, null, mesh, position, appearance);
	}

	/** Creates an independently styled object using a caller-owned renderable lease. */
	public static SceneObject withIndependentAppearance(RocketComponent selectionComponent,
			Mesh mesh, Renderable renderableMesh, Vector3f position, Appearance3D appearance) {
		return new SceneObject(selectionComponent, null, mesh, renderableMesh, position, appearance);
	}

	/**
	 * Constructs a new SceneObject without an associated OpenRocket component.
	 * This constructor is used for objects that are not directly part of the rocket
	 * model, such as light visualizers, environment objects, or UI elements.
	 *
	 * @param mesh the raw geometric mesh data
	 * @param position the initial 3D position of the object
	 * @param appearance the visual appearance including materials and textures
	 */
	public SceneObject(Mesh mesh, Vector3f position, Appearance3D appearance) {
		this(null, mesh, position, appearance);
	}

	/**
	 * Gets the unique identifier for this scene object.
	 *
	 * @return the UUID assigned to this object
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Gets the component used to group this object for selection and rocket-wide behavior.
	 *
	 * @return the associated component, or null if this is not rocket-derived geometry
	 */
	public RocketComponent getRocketComponent() {
		return rocketComponent;
	}

	/**
	 * Returns the component whose live appearance this object follows.
	 *
	 * @return the appearance source, or {@code null} for independently styled objects
	 */
	public RocketComponent getAppearanceSourceComponent() {
		return appearanceSourceComponent;
	}

	/**
	 * Checks if this object is currently selected by the user.
	 *
	 * @return true if the object is selected, false otherwise
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Sets the selection state of this object.
	 * Selected objects typically have different visual appearance (highlighting, outlines, etc.)
	 *
	 * @param selected true to select the object, false to deselect it
	 */
	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	/**
	 * Checks if this object can be selected by user interaction.
	 *
	 * @return true if the object can be selected, false if it's non-interactive
	 */
	public boolean isSelectable() {
		return isSelectable;
	}

	public boolean isRenderOnTop() {
		return renderOnTop;
	}

	public void setSelectable(boolean selectable) {
		isSelectable = selectable;
	}

	public void setAppearance(Appearance3D appearance) {
		this.appearance = appearance;
	}

	/**
	 * Applies a rotation to the object's model matrix.
	 * @param angle The angle of rotation in radians.
	 * @param axis The axis of rotation.
	 */
	public void rotate(float angle, Vector3f axis) {
		this.modelMatrix.rotate(angle, axis);
	}

	/**
	 * Force-sets the position of this object by recreating its model matrix.
	 */
	public void setPosition(Vector3f position) {
		this.modelMatrix.setTranslation(position);
	}

	/**
	 * Checks if this object can be dragged by user interaction.
	 *
	 * @return true if the object has drag behavior configured, false otherwise
	 */
	public boolean isDraggable() {
		return this.onDragListener != null;
	}

	/**
	 * Makes this object draggable with a default behavior of updating its position.
	 *
	 * @param draggable True to make the object draggable, false otherwise.
	 */
	public void setDraggable(boolean draggable) {
		if (draggable) {
			// Assign a default drag listener that moves the object.
			this.onDragListener = this::setPosition;
		} else {
			this.onDragListener = null;
		}
	}

	public void setRenderOnTop(boolean renderOnTop) {
		this.renderOnTop = renderOnTop;
	}

	public boolean isOriginAxis() {
		return originAxis;
	}

	public void setOriginAxis(boolean originAxis) {
		this.originAxis = originAxis;
	}

	public void setOnDragListener(DragListener listener) {
		this.onDragListener = listener;
	}

	public void executeOnDrag(Vector3f newPosition) {
		if (onDragListener != null) {
			onDragListener.onDrag(newPosition);
		}
	}

	/**
	 * Returns the raw mesh data, used for physics and raycasting.
	 * @return The Mesh object.
	 */
	public Mesh getMesh() {
		return mesh;
	}

	/**
	 * Returns the renderable mesh, used by the renderer.
	 * @return the GPU-backed renderable or shared-resource lease
	 */
	public Renderable getRenderableMesh() {
		return renderableMesh;
	}

	public Matrix4f getModelMatrix() {
		return modelMatrix;
	}

	public Appearance3D getAppearance() {
		return appearance;
	}

	// ---------- Animation hooks ----------
	public void setPoseProvider(PoseProvider provider) {
		this.poseProvider = provider;
	}

	public PoseProvider getPoseProvider() {
		return poseProvider;
	}

	public boolean hasPoseProvider() {
		return poseProvider != null;
	}

	public void clearPoseProvider() {
		this.poseProvider = null;
		if (baseModelSnapshot != null) {
				// Restore original static transform
						this.modelMatrix.set(baseModelSnapshot);
			}
	}

	/**
	 * Applies the pose at time t by composing dynamic transform * base static transform.
	 * Lazily snapshots the current model matrix as the "base" the first time it runs.
	 */
	public void applyPoseAtTime(double t) {
		if (poseProvider == null) return;
		if (baseModelSnapshot == null) {
				baseModelSnapshot = new Matrix4f(this.modelMatrix);
			}
		Vector3f p = poseProvider.getPosition(t);
		Quaternionf q = poseProvider.getOrientation(t);
		dynamicTransform.identity().translate(p).rotate(q);
		this.modelMatrix.set(dynamicTransform).mul(baseModelSnapshot);
	}

	/**
	 * Cleans up all resources associated with this scene object.
	 * This method releases GPU memory used by meshes and textures,
	 * and should be called when the object is no longer needed.
	 */
	public void cleanup() {
		if (cleaned) {
			return;
		}
		cleaned = true;
		try {
			renderableMesh.cleanup();
		} finally {
			appearance.cleanup();
		}
	}
}
