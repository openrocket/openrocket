package net.sf.openrocket.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjGroup;
import net.sf.openrocket.util.MathUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.tri.Tri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class TriangulationHelper {
	public static DefaultObj simpleTriangulate(DefaultObj obj) {
		return de.javagl.obj.ObjUtils.triangulate(obj, new DefaultObj());
	}

	public static DefaultObj constrainedDelaunayTriangulate(DefaultObj obj, DefaultObjFace face) {
		// Create a new OBJ that will contain the triangulated faces, and copy all the vertices and MTL file names from the original OBJ
		DefaultObj newObj = obj.clone(true);

		// Generate the new triangulated faces
		List<ObjFace> newFaces = generateCDTFaces(obj, face);

		// Add the triangulated faces
		for (ObjFace newFace : newFaces) {
			newObj.addFace(newFace);
		}

		// Remove the old face
		obj.removeFace(face);
		for (ObjGroup group : obj.getGroups()) {
			DefaultObjGroup g = (DefaultObjGroup) group;
			if (g.containsFace(face)) {
				g.removeFace(face);
				g.addFaces(newFaces);
			}
		}

		return newObj;
	}

	public static DefaultObj constrainedDelaunayTriangulate(DefaultObj input) {
		// Create a new OBJ that will contain the triangulated faces, and copy all the vertices and MTL file names from the original OBJ
		DefaultObj output = input.clone(false);

		for (ObjFace face : input.getFaces()) {
			ObjUtils.activateGroups(input, face, output);
			if (face.getNumVertices() == 3) {
				output.addFace(face);
				continue;
			}

			// Generate the new triangulated faces
			List<ObjFace> newFaces = generateCDTFaces(input, (DefaultObjFace) face);

			// Add the triangulated faces
			for (ObjFace newFace : newFaces) {
				output.addFace(newFace);
			}
		}

		return output;
	}

	public static List<ObjFace> generateCDTFaces(DefaultObj obj, DefaultObjFace face) {
		PolygonWithOriginalIndices polygonWithIndices = createProjectedPolygon(obj, face);
		Polygon polygon = polygonWithIndices.polygon();
		Map<Coordinate3D, Integer> vertexIndexMap = polygonWithIndices.vertexIndexMap();

		ConstrainedDelaunayTriangulator triangulator = new ConstrainedDelaunayTriangulator(polygon);
		List<Tri> triangles = triangulator.getTriangles();

		List<ObjFace> newFaces = new ArrayList<>();
		for (Tri tri : triangles) {
			int[] vertexIndices = new int[3];
			for (int i = 0; i < 3; i++) {
				Coordinate coord = tri.getCoordinate(i);
				vertexIndices[i] = getNearbyValue(vertexIndexMap, coord);
			}
			if (vertexIndices[0] != -1 && vertexIndices[1] != -1 && vertexIndices[2] != -1) {
				//DefaultObjFace newFace = ObjUtils.createFaceWithNewIndices(face, vertexIndices);
				DefaultObjFace newFace = new DefaultObjFace(vertexIndices, null, null);
				newFaces.add(newFace);
			}
		}

		return newFaces;
	}

	/**
	 * Projects 3D coordinates of a polygon onto a 2D plane for further processing with JTS.
	 * The projection minimizes distortion by aligning the polygon's normal with the Z-axis.
	 *
	 * @param obj The input OBJ containing 3D polygon data.
	 * @param face The specific face of the OBJ to be projected and triangulated.
	 * @return A polygon in 2D space suitable for use with JTS.
	 */
	private static PolygonWithOriginalIndices createProjectedPolygon(DefaultObj obj, DefaultObjFace face) {
		// Calculate the normal of the polygon to determine its orientation in 3D space
		Coordinate normal = calculateNormal(
				vertexToCoordinate(obj.getVertex(face.getVertexIndices()[0])),
				vertexToCoordinate(obj.getVertex(face.getVertexIndices()[1])),
				vertexToCoordinate(obj.getVertex(face.getVertexIndices()[2])));

		// Create a list for storing the projected 2D coordinates
		List<Coordinate> projectedCoords = new ArrayList<>();
		Map<Coordinate3D, Integer> vertexIndexMap = new HashMap<>();

		// Project each vertex onto the 2D plane
		for (int vertexIndex : face.getVertexIndices()) {
			FloatTuple vertex = obj.getVertex(vertexIndex);
			Coordinate3D originalCoord = new Coordinate3D(vertexToCoordinate(vertex));
			Coordinate projectedCoord = projectVertexOntoXYPlane(originalCoord, normal);
			projectedCoord = new Coordinate(projectedCoord.x, projectedCoord.y);
			projectedCoords.add(projectedCoord);

			vertexIndexMap.put(new Coordinate3D(projectedCoord), vertexIndex);
		}

		// Ensure polygon closure by repeating the first coordinate at the end if necessary
		if (!projectedCoords.isEmpty() && !projectedCoords.get(0).equals3D(projectedCoords.get(projectedCoords.size() - 1))) {
			projectedCoords.add(projectedCoords.get(0));
		}

		GeometryFactory factory = new GeometryFactory();
		Polygon polygon = factory.createPolygon(projectedCoords.toArray(new Coordinate[0]));

		return new PolygonWithOriginalIndices(polygon, vertexIndexMap);
	}

	/**
	 * Projects a vertex onto a plane by rotating it so the face normal aligns with the Z-axis.
	 *
	 * @param vertex The 3D vertex to project.
	 * @param normal The normal vector of the polygon's face.
	 * @return The projected 2D coordinate of the vertex.
	 */
	private static Coordinate projectVertexOntoXYPlane(Coordinate3D vertex, Coordinate normal) {
		// If the normal is a zero vector, the polygon is degenerate and cannot be projected
		if (MathUtil.equals(normal.x, 0) && MathUtil.equals(normal.y, 0) && MathUtil.equals(normal.z, 0)) {
			throw new IllegalArgumentException("Cannot project a degenerate polygon onto a 2D plane");
		}
		// If the normal is parallel to the Z-axis, the polygon is already 2D
		if (MathUtil.equals(normal.x, 0) && MathUtil.equals(normal.y, 0)) {
			return new Coordinate(vertex.coordinate().x, vertex.coordinate().y);
		}

		Coordinate u = crossProduct(normal, new Coordinate(0, 0, 1));
		Coordinate w = crossProduct(normal, u);

		double x2D = dotProduct(vertex.coordinate(), u);
		double y2D = dotProduct(vertex.coordinate(), w);

		return new Coordinate(x2D, y2D);
	}

	/**
	 * Computes the cross product of two vectors.
	 *
	 * @param v1 The first vector.
	 * @param v2 The second vector.
	 * @return The cross product.
	 */
	private static Coordinate crossProduct(Coordinate v1, Coordinate v2) {
		return new Coordinate(
				v1.y * v2.z - v1.z * v2.y,
				v1.z * v2.x - v1.x * v2.z,
				v1.x * v2.y - v1.y * v2.x
		);
	}

	private static Coordinate vertexToCoordinate(FloatTuple vertex) {
		return new Coordinate(vertex.getX(), vertex.getY(), vertex.getZ());
	}


	private static int getNearbyValue(Map<Coordinate3D, Integer> vertexIndexMap, Coordinate coord) {
		for (Map.Entry<Coordinate3D, Integer> entry : vertexIndexMap.entrySet()) {
			Coordinate key = entry.getKey().coordinate();
			if (key.equals3D(coord)) {
				return entry.getValue();
			}
		}
		return -1;  // Or any default value.
	}


	private static double dotProduct(Coordinate v1, Coordinate v2) {
		return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
	}


	private static Coordinate subtract(Coordinate v1, Coordinate v2) {
		return new Coordinate(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
	}

	private static Coordinate normalize(Coordinate vector) {
		double magnitude = magnitude(vector);
		if (magnitude == 0) {
			// Handle potential divide by zero if the vector is a zero vector
			return new Coordinate(0, 0, 0);
		}

		return new Coordinate(vector.x / magnitude, vector.y / magnitude, vector.z / magnitude);
	}

	private static double magnitude(Coordinate vector) {
		return Math.sqrt(dotProduct(vector, vector));
	}

	public static Coordinate calculateNormal(Coordinate p1, Coordinate p2, Coordinate p3) {
		Coordinate u = subtract(p2, p1);
		Coordinate v = subtract(p3, p1);

		return normalize(crossProduct(u, v));
	}

	private record PolygonWithOriginalIndices(Polygon polygon, Map<Coordinate3D, Integer> vertexIndexMap) { }

	// Helper class to wrap Coordinate and override equals and hashCode to account for all 3 dimensions
	private record Coordinate3D(Coordinate coordinate) {
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Coordinate3D that = (Coordinate3D) o;
			return coordinate.equals3D(that.coordinate);
		}

		@Override
		public int hashCode() {
			return Objects.hash(coordinate.x, coordinate.y, coordinate.z);
		}
	}
}
