package info.openrocket.core.file.wavefrontobj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.ObjFace;
import info.openrocket.core.util.MathUtil;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;
import org.locationtech.jts.triangulate.tri.Tri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class TriangulationHelper {
	private static final Logger log = LoggerFactory.getLogger(TriangulationHelper.class);

	public static DefaultObj simpleTriangulate(DefaultObj obj) {
		return de.javagl.obj.ObjUtils.triangulate(obj, new DefaultObj());
	}

	/**
	 * Triangulates an OBJ object using constrained Delaunay triangulation.
	 *
	 * @param input The object to triangulate.
	 * @return A new object with the triangulated faces.
	 */
	public static DefaultObj constrainedDelaunayTriangulate(DefaultObj input) {
		// Create a new OBJ that will contain the triangulated faces, and copy all the vertices and MTL file names from the original OBJ
		DefaultObj output = input.clone(false);

		for (ObjFace face : input.getFaces()) {
			ObjUtils.activateGroups(input, face, output);
			if (face.getNumVertices() == 3) {
				output.addFace(face);
				continue;
			} else if (face.getNumVertices() < 3) {
				log.debug("Face has less than 3 vertices, skipping");
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

	/**
	 * Generates constrained Delaunay triangulation faces based on a given object and face.
	 *
	 * @param obj  The input object containing 3D polygon data.
	 * @param face The specific face of the object to be triangulated.
	 * @return A list of generated object faces representing the triangulated faces.
	 */
	public static List<ObjFace> generateCDTFaces(DefaultObj obj, DefaultObjFace face) {
		// Retrieve the vertex mapping to normal indices and texture coordinate indices
		Map<Integer, Integer> vertexToNormalMap = mapVertexIndicesToNormalIndices(face);
		Map<Integer, Integer> vertexToTexCoordMap = mapVertexIndicesToTexCoordIndices(face);

		// Calculate the face normal
		Coordinate normal = vertexToCoordinate(ObjUtils.calculateNormalVector(obj, face));

		// Project the 3D face to a 2D polygon with only X and Y coordinates.
		// This is necessary because the JTS library only works with 2D polygons for triangulation.
		PolygonWithOriginalIndices polygonWithIndices = createProjectedPolygon(obj, face, normal);
		Polygon polygon = polygonWithIndices.polygon();
		Map<Coordinate3D, Integer> vertexIndexMap = polygonWithIndices.vertexIndexMap();

		// Triangulate the polygon
		ConstrainedDelaunayTriangulator triangulator = new ConstrainedDelaunayTriangulator(polygon);
		List<Tri> triangles = triangulator.getTriangles();

		// Create the new faces to add to the OBJ
		List<ObjFace> newFaces = new ArrayList<>();
		for (Tri tri : triangles) {
			// Map the 2D triangle vertices back to the original vertex indices
			int[] vertexIndices = new int[3];
			for (int i = 0; i < 3; i++) {
				Coordinate coord = tri.getCoordinate(i);
				vertexIndices[i] = getVertexIndexFromCoord(vertexIndexMap, coord);
			}

			// Calculate the normal of the triangle, and verify that it has the same orientation as the original face
			// If it does not, invert the vertex order to ensure the normal points in the same direction
			// This is necessary for correct face culling.
			Coordinate triangleNormal = calculateNormal(
					vertexToCoordinate(obj.getVertex(vertexIndices[0])),
					vertexToCoordinate(obj.getVertex(vertexIndices[1])),
					vertexToCoordinate(obj.getVertex(vertexIndices[2])));
			if (normalsHaveDifferentDirection(triangleNormal, normal)) {
				int temp = vertexIndices[0];
				vertexIndices[0] = vertexIndices[2];
				vertexIndices[2] = temp;
			}

			// Add the new face to the list
			if (vertexIndices[0] != -1 && vertexIndices[1] != -1 && vertexIndices[2] != -1) {
				// Map the vertex indices to normal and texture coordinate indices
				int[] normalIndices = vertexToNormalMap != null ?
						new int[] {vertexToNormalMap.get(vertexIndices[0]), vertexToNormalMap.get(vertexIndices[1]), vertexToNormalMap.get(vertexIndices[2])}
						: null;
				int[] texCoordIndices = vertexToTexCoordMap != null ?
						new int[] {vertexToTexCoordMap.get(vertexIndices[0]), vertexToTexCoordMap.get(vertexIndices[1]), vertexToTexCoordMap.get(vertexIndices[2])}
						: null;

				// Create and add the new face
				DefaultObjFace newFace = new DefaultObjFace(vertexIndices, texCoordIndices, normalIndices);
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
	 * @param normal the normal of the face to determine its orientation in 3D space
	 * @return A polygon in 2D space suitable for use with JTS.
	 */
	private static PolygonWithOriginalIndices createProjectedPolygon(DefaultObj obj, DefaultObjFace face, Coordinate normal) {
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

		// Create the polygon
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
	 * Maps the vertex indices of a face to the normal indices of the same face.
	 *
	 * @param face The face for which to map the vertex indices to normal indices.
	 * @return A map that maps the vertex indices to the normal indices, or null if the face does not contain normal indices.
	 */
	private static Map<Integer, Integer> mapVertexIndicesToNormalIndices(DefaultObjFace face) {
		int[] normalIndices = face.getNormalIndices();
		if (normalIndices == null) {
			return null;
		}

		Map<Integer, Integer> vertexToNormalMap = new HashMap<>();
		int[] vertexIndices = face.getVertexIndices();
		for (int i = 0; i < vertexIndices.length; i++) {
			vertexToNormalMap.put(vertexIndices[i], normalIndices[i]);
		}
		return vertexToNormalMap;
	}

	/**
	 * Maps vertex indices to texture coordinate indices.
	 *
	 * @param face The face object containing the vertex and texture coordinate indices.
	 * @return A map that maps vertex indices to texture coordinate indices, or null if the face does
	 * 			not contain texture coordinate indices.
	 */
	private static Map<Integer, Integer> mapVertexIndicesToTexCoordIndices(DefaultObjFace face) {
		int[] texCoordIndices = face.getTexCoordIndices();
		if (texCoordIndices == null) {
			return null;
		}

		Map<Integer, Integer> vertexToTexCoordMap = new HashMap<>();
		int[] vertexIndices = face.getVertexIndices();
		for (int i = 0; i < vertexIndices.length; i++) {
			vertexToTexCoordMap.put(vertexIndices[i], texCoordIndices[i]);
		}
		return vertexToTexCoordMap;
	}

	/**
	 * Converts a FloatTuple vertex to a Coordinate object.
	 *
	 * @param vertex The vertex to convert.
	 * @return The converted Coordinate object.
	 */
	private static Coordinate vertexToCoordinate(FloatTuple vertex) {
		return new Coordinate(vertex.getX(), vertex.getY(), vertex.getZ());
	}


	/**
	 * Retrieves the vertex index from the given coordinate in the vertex index map.
	 *
	 * @param vertexIndexMap  The map containing the vertex coordinates and their corresponding indices.
	 * @param coord           The coordinate to retrieve the vertex index for.
	 * @return The vertex index if the coordinate is found in the map, or -1 if not found.
	 */
	private static int getVertexIndexFromCoord(Map<Coordinate3D, Integer> vertexIndexMap, Coordinate coord) {
		for (Map.Entry<Coordinate3D, Integer> entry : vertexIndexMap.entrySet()) {
			Coordinate key = entry.getKey().coordinate();
			if (key.equals3D(coord)) {
				return entry.getValue();
			}
		}
		return -1;
	}

	/**
	 * Determines whether two normals have different directions.
	 *
	 * @param normal1 The first normal.
	 * @param normal2 The second normal.
	 * @return true if the two normals have different directions, false otherwise.
	 */
	private static boolean normalsHaveDifferentDirection(Coordinate normal1, Coordinate normal2) {
		return dotProduct(normal1, normal2) < 0;
	}

	public static Coordinate calculateNormal(Coordinate p1, Coordinate p2, Coordinate p3) {
		Coordinate u = subtract(p2, p1);
		Coordinate v = subtract(p3, p1);

		return normalize(crossProduct(u, v));
	}

	// ==================================== Basic Vector Math ====================================

	private static Coordinate crossProduct(Coordinate v1, Coordinate v2) {
		return new Coordinate(
				v1.y * v2.z - v1.z * v2.y,
				v1.z * v2.x - v1.x * v2.z,
				v1.x * v2.y - v1.y * v2.x
		);
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
			return new Coordinate(0, 0, 0);
		}

		return new Coordinate(vector.x / magnitude, vector.y / magnitude, vector.z / magnitude);
	}

	private static double magnitude(Coordinate vector) {
		return Math.sqrt(dotProduct(vector, vector));
	}

	// ==================================== Helper classes ====================================

	// Helper class to wrap a Polygon and its original vertex indices
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
