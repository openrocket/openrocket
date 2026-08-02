package info.openrocket.swing.gui.figure3d.math;

import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.scene.core.Camera;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

/**
 * Default implementation of the Raycaster using Möller-Trumbore triangle tests.
 */
public class DefaultRaycaster implements Raycaster {

	private final Vector3f rayOrigin = new Vector3f();
	private final Vector3f rayDirection = new Vector3f();

	@Override
	public void update(float mouseX, float mouseY, int viewportWidth, int viewportHeight, Camera camera) {
		// 1. Normalized Device Coordinates (NDC)
		float ndcX = (2.0f * mouseX) / viewportWidth - 1.0f;
		float ndcY = 1.0f - (2.0f * mouseY) / viewportHeight;
		float ndcZ = -1.0f; // The near plane

		// 2. Homogeneous Clip Coordinates
		Vector4f clipCoords = new Vector4f(ndcX, ndcY, ndcZ, 1.0f);

		// 3. Eye (Camera) Coordinates
		Matrix4f invertedProjection = new Matrix4f();
		camera.getProjectionMatrix().invert(invertedProjection);
		Vector4f eyeCoords = invertedProjection.transform(clipCoords);
		eyeCoords.z = -1.0f;
		eyeCoords.w = 0.0f;

		// 4. World Coordinates
		Matrix4f invertedView = new Matrix4f();
		camera.getViewMatrix().invert(invertedView);
		Vector4f worldCoords = invertedView.transform(eyeCoords);

		rayDirection.set(worldCoords.x, worldCoords.y, worldCoords.z).normalize();
		rayOrigin.set(camera.getPosition());
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
			Vector3f v0 = new Vector3f(vertices.get(indices.get(i)).position);
			Vector3f v1 = new Vector3f(vertices.get(indices.get(i + 1)).position);
			Vector3f v2 = new Vector3f(vertices.get(indices.get(i + 2)).position);

			modelMatrix.transformPosition(v0);
			modelMatrix.transformPosition(v1);
			modelMatrix.transformPosition(v2);

			final float EPSILON = 0.0000001f;
			Vector3f edge1 = new Vector3f(v1).sub(v0);
			Vector3f edge2 = new Vector3f(v2).sub(v0);
			Vector3f h = new Vector3f(rayDirection).cross(edge2);
			float a = edge1.dot(h);

			if (a > -EPSILON && a < EPSILON) continue;

			float f = 1.0f / a;
			Vector3f s = new Vector3f(rayOrigin).sub(v0);
			float u = f * s.dot(h);
			if (u < 0.0f || u > 1.0f) continue;

			Vector3f q = s.cross(edge1);
			float v = f * rayDirection.dot(q);
			if (v < 0.0f || u + v > 1.0f) continue;

			float t = f * edge2.dot(q);
			if (t > EPSILON) {
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
