package info.openrocket.swing.gui.figure3d.export;

import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.IntList;
import info.openrocket.swing.gui.figure3d.core.geometry.Vertex;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A utility class to export a Mesh object to the Wavefront .obj file format.
 * This version de-duplicates vertex positions and normals to create an efficient,
 * standard-compliant file, avoiding floating-point comparison issues.
 */
public class ObjExporter implements MeshExporter {

	private static final Logger log = LoggerFactory.getLogger(ObjExporter.class);

	@Override
	public void export(Mesh mesh, String filePath) throws IOException {
		// Lists to hold the unique geometric data
		List<Vector3f> positions = new ArrayList<>();
		List<Vector3f> normals = new ArrayList<>();
		List<String> faces = new ArrayList<>();

		// Maps to track the indices of unique data using a String key to avoid float precision issues
		Map<String, Integer> posIndexMap = new HashMap<>();
		Map<String, Integer> normIndexMap = new HashMap<>();

		// Process the mesh data to populate the lists and maps
		IntList meshIndices = mesh.getIndices();
		List<Vertex> meshVertices = mesh.getVertices();

		for (int i = 0; i < meshIndices.size(); i += 3) {
			Vertex v1 = meshVertices.get(meshIndices.get(i));
			Vertex v2 = meshVertices.get(meshIndices.get(i + 1));
			Vertex v3 = meshVertices.get(meshIndices.get(i + 2));

			int p1 = getOrAdd(v1.position, positions, posIndexMap);
			int n1 = getOrAdd(v1.normal, normals, normIndexMap);
			int p2 = getOrAdd(v2.position, positions, posIndexMap);
			int n2 = getOrAdd(v2.normal, normals, normIndexMap);
			int p3 = getOrAdd(v3.position, positions, posIndexMap);
			int n3 = getOrAdd(v3.normal, normals, normIndexMap);

			faces.add(String.format("f %d//%d %d//%d %d//%d", p1, n1, p2, n2, p3, n3));
		}

		// Write the processed data to the .obj file
		try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, StandardCharsets.UTF_8))) {
			writer.println("# Exported from LWJGL Engine");
			writer.printf("# Vertices: %d%n", positions.size());
			writer.printf("# Normals: %d%n", normals.size());
			writer.printf("# Faces: %d%n%n", faces.size());

			writer.println("# Vertex Positions");
			for (Vector3f pos : positions) {
				writer.printf(Locale.US, "v %.6f %.6f %.6f%n", pos.x, pos.y, pos.z);
			}
			writer.println();

			writer.println("# Vertex Normals");
			for (Vector3f norm : normals) {
				writer.printf(Locale.US, "vn %.6f %.6f %.6f%n", norm.x, norm.y, norm.z);
			}
			writer.println();

			writer.println("# Faces");
			for (String face : faces) {
				writer.println(face);
			}

			log.info("Successfully exported mesh to {}", filePath);
		} catch (IOException e) {
			log.error("Failed to export mesh to .obj file: {}", e.getMessage());
			throw e;
		}
	}

	private static String vecToString(Vector3f vec) {
		// Format with high precision to use as a reliable map key
		return String.format(Locale.US, "%.6f_%.6f_%.6f", vec.x, vec.y, vec.z);
	}

	private static int getOrAdd(Vector3f vec, List<Vector3f> list, Map<String, Integer> map) {
		String key = vecToString(vec);
		if (map.containsKey(key)) {
			return map.get(key);
		}
		list.add(vec);
		int newIndex = list.size(); // .obj indices are 1-based
		map.put(key, newIndex);
		return newIndex;
	}

	@Override
	public String getFileExtension() {
		return "obj";
	}

	@Override
	public String getDescription() {
		return "Wavefront OBJ";
	}

	@Override
	public boolean supportsTextureCoordinates() {
		return false;
	}

	@Override
	public boolean supportsNormals() {
		return true;
	}
}
