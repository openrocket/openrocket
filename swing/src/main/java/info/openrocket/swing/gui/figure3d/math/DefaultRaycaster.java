package info.openrocket.swing.gui.figure3d.math;

import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

/**
 * Default implementation of the Raycaster using Möller-Trumbore triangle tests.
 */
public class DefaultRaycaster implements Raycaster {
	private static final float INTERSECTION_EPSILON = 0.0000001f;

	private final Vector3f rayOrigin = new Vector3f();
	private final Vector3f rayDirection = new Vector3f();
	private final Matrix4f invertedProjection = new Matrix4f();
	private final Matrix4f invertedView = new Matrix4f();
	private final Vector4f clipCoords = new Vector4f();
	private final Vector4f eyeCoords = new Vector4f();
	private final Vector4f worldCoords = new Vector4f();
	private final Vector3f vertex0 = new Vector3f();
	private final Vector3f vertex1 = new Vector3f();
	private final Vector3f vertex2 = new Vector3f();
	private final Vector3f edge1 = new Vector3f();
	private final Vector3f edge2 = new Vector3f();
	private final Vector3f cross = new Vector3f();
	private final Vector3f originOffset = new Vector3f();
	private final Vector3f barycentricCross = new Vector3f();

	@Override
	public void update(float mouseX, float mouseY, int viewportWidth, int viewportHeight, Camera camera) {
		// 1. Normalized Device Coordinates (NDC)
		float ndcX = (2.0f * mouseX) / viewportWidth - 1.0f;
		float ndcY = 1.0f - (2.0f * mouseY) / viewportHeight;
		float ndcZ = -1.0f; // The near plane

		// 2. Homogeneous Clip Coordinates
		clipCoords.set(ndcX, ndcY, ndcZ, 1.0f);

		// 3. Eye (Camera) Coordinates
		camera.getProjectionMatrix().invert(invertedProjection);
		eyeCoords.set(clipCoords);
		invertedProjection.transform(eyeCoords);
		eyeCoords.z = -1.0f;
		eyeCoords.w = 0.0f;

		// 4. World Coordinates
		camera.getViewMatrix().invert(invertedView);
		worldCoords.set(eyeCoords);
		invertedView.transform(worldCoords);

		rayDirection.set(worldCoords.x, worldCoords.y, worldCoords.z).normalize();
		camera.getPosition(rayOrigin);
	}

	@Override
	public SceneObject getIntersectedObject(List<SceneObject> objects) {
		SceneObject closestObject = null;
		float closestDistance = Float.MAX_VALUE;

		for (SceneObject object : objects) {
			if (!object.isSelectable()) {
				continue;
			}

			float distance = intersects(object.getMesh(), object.getModelMatrix());
			if (distance >= 0 && distance < closestDistance) {
				closestDistance = distance;
				closestObject = object;
			}
		}

		return closestObject;
	}

	private float intersects(Mesh mesh, Matrix4f modelMatrix) {
		float closestDistance = -1.0f;

		List<Vertex> vertices = mesh.getVertices();
		IntList indices = mesh.getIndices();

		for (int i = 0; i < indices.size(); i += 3) {
			vertex0.set(vertices.get(indices.get(i)).position);
			vertex1.set(vertices.get(indices.get(i + 1)).position);
			vertex2.set(vertices.get(indices.get(i + 2)).position);

			modelMatrix.transformPosition(vertex0);
			modelMatrix.transformPosition(vertex1);
			modelMatrix.transformPosition(vertex2);

			edge1.set(vertex1).sub(vertex0);
			edge2.set(vertex2).sub(vertex0);
			cross.set(rayDirection).cross(edge2);
			float a = edge1.dot(cross);

			if (a > -INTERSECTION_EPSILON && a < INTERSECTION_EPSILON) continue;

			float f = 1.0f / a;
			originOffset.set(rayOrigin).sub(vertex0);
			float u = f * originOffset.dot(cross);
			if (u < 0.0f || u > 1.0f) continue;

			barycentricCross.set(originOffset).cross(edge1);
			float v = f * rayDirection.dot(barycentricCross);
			if (v < 0.0f || u + v > 1.0f) continue;

			float t = f * edge2.dot(barycentricCross);
			if (t > INTERSECTION_EPSILON) {
				if (closestDistance < 0 || t < closestDistance) {
					closestDistance = t;
				}
			}
		}
		return closestDistance;
	}

	@Override
	public Vector3f getRayOrigin() {
		return rayOrigin;
	}

	@Override
	public Vector3f getRayDirection() {
		return rayDirection;
	}
}
