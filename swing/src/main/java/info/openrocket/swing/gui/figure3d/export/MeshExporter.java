package info.openrocket.swing.gui.figure3d.export;

import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;

import java.io.IOException;

/**
 * Interface for exporting 3D mesh data to various file formats.
 * Implementations should handle the specifics of each format.
 */
public interface MeshExporter {
	
	/**
	 * Exports the given mesh to the specified file path.
	 * 
	 * @param mesh The mesh to export
	 * @param filePath The path where the file should be saved
	 * @throws IOException If the export operation fails
	 */
	void export(Mesh mesh, String filePath) throws IOException;
	
	/**
	 * Gets the file extension for this exporter (without the dot).
	 * 
	 * @return The file extension (e.g., "obj", "ply", "stl")
	 */
	String getFileExtension();
	
	/**
	 * Gets a human-readable description of this export format.
	 * 
	 * @return A description of the format (e.g., "Wavefront OBJ")
	 */
	String getDescription();
	
	/**
	 * Indicates whether this exporter supports texture coordinates.
	 * 
	 * @return true if texture coordinates are supported and exported
	 */
	boolean supportsTextureCoordinates();
	
	/**
	 * Indicates whether this exporter supports vertex normals.
	 * 
	 * @return true if vertex normals are supported and exported
	 */
	boolean supportsNormals();
}