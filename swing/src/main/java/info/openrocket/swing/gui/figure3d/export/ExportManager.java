package info.openrocket.swing.gui.figure3d.export;

import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages different types of exporters and provides a unified interface for exporting.
 * This class demonstrates the Strategy pattern and makes it easy to swap out different
 * export implementations or add new formats.
 */
public class ExportManager {
	
	private final Map<String, MeshExporter> meshExporters = new HashMap<>();
	private final Map<String, ImageExporter> screenshotExporters = new HashMap<>();
	
	public ExportManager() {
		// Register default exporters
		registerMeshExporter(new ObjExporter());
		registerScreenshotExporter(new PngExporter());
	}
	
	/**
	 * Registers a mesh exporter for its file extension.
	 */
	public void registerMeshExporter(MeshExporter exporter) {
		meshExporters.put(exporter.getFileExtension().toLowerCase(), exporter);
	}
	
	/**
	 * Registers a screenshot exporter for its file extension.
	 */
	public void registerScreenshotExporter(ImageExporter exporter) {
		screenshotExporters.put(exporter.getFileExtension().toLowerCase(), exporter);
	}
	
	/**
	 * Exports a mesh using the appropriate exporter based on file extension.
	 * 
	 * @param mesh The mesh to export
	 * @param filePath The file path (extension determines the format)
	 * @throws IOException If the export fails
	 * @throws UnsupportedOperationException If no exporter is available for the format
	 */
	public void exportMesh(Mesh mesh, String filePath) throws IOException {
		String extension = getFileExtension(filePath).toLowerCase();
		MeshExporter exporter = meshExporters.get(extension);
		
		if (exporter == null) {
			throw new UnsupportedOperationException(
				"No mesh exporter available for format: " + extension + 
				". Available formats: " + String.join(", ", meshExporters.keySet())
			);
		}
		
		exporter.export(mesh, filePath);
	}
	
	/**
	 * Exports a screenshot using the appropriate exporter based on file extension.
	 * 
	 * @param framebufferId Framebuffer id that should be read (0 for default framebuffer)
	 * @param width The width of the viewport to capture
	 * @param height The height of the viewport to capture
	 * @param filePath The file path (extension determines the format)
	 * @throws IOException If the export fails
	 * @throws UnsupportedOperationException If no exporter is available for the format
	 */
	public void exportScreenshot(int framebufferId, int width, int height, String filePath) throws IOException {
		String extension = getFileExtension(filePath).toLowerCase();
		ImageExporter exporter = screenshotExporters.get(extension);
		
		if (exporter == null) {
			throw new UnsupportedOperationException(
				"No screenshot exporter available for format: " + extension +
				". Available formats: " + String.join(", ", screenshotExporters.keySet())
			);
		}
		
		exporter.export(framebufferId, width, height, filePath);
	}
	
	/**
	 * Gets all available mesh export formats.
	 */
	public List<String> getAvailableMeshFormats() {
		return new ArrayList<>(meshExporters.keySet());
	}
	
	/**
	 * Gets all available screenshot export formats.
	 */
	public List<String> getAvailableScreenshotFormats() {
		return new ArrayList<>(screenshotExporters.keySet());
	}
	
	/**
	 * Gets information about a specific mesh exporter.
	 */
	public MeshExporter getMeshExporter(String extension) {
		return meshExporters.get(extension.toLowerCase());
	}
	
	/**
	 * Gets information about a specific screenshot exporter.
	 */
	public ImageExporter getScreenshotExporter(String extension) {
		return screenshotExporters.get(extension.toLowerCase());
	}
	
	/**
	 * Checks if a mesh format is supported.
	 */
	public boolean isMeshFormatSupported(String extension) {
		return meshExporters.containsKey(extension.toLowerCase());
	}
	
	/**
	 * Checks if a screenshot format is supported.
	 */
	public boolean isScreenshotFormatSupported(String extension) {
		return screenshotExporters.containsKey(extension.toLowerCase());
	}
	
	/**
	 * Extracts the file extension from a file path.
	 */
	private String getFileExtension(String filePath) {
		int lastDotIndex = filePath.lastIndexOf('.');
		if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
			throw new IllegalArgumentException("File path must have a valid extension: " + filePath);
		}
		return filePath.substring(lastDotIndex + 1);
	}
	
	/**
	 * Gets a summary of all registered exporters for debugging/information purposes.
	 */
	public String getExporterSummary() {
		StringBuilder sb = new StringBuilder();
		sb.append("Export Manager Summary:\n");
		
		sb.append("\nMesh Exporters:\n");
		for (MeshExporter exporter : meshExporters.values()) {
			sb.append(String.format("  - %s (.%s) - Normals: %s, UVs: %s\n",
				exporter.getDescription(),
				exporter.getFileExtension(),
				exporter.supportsNormals() ? "Yes" : "No",
				exporter.supportsTextureCoordinates() ? "Yes" : "No"
			));
		}
		
		sb.append("\nScreenshot Exporters:\n");
		for (ImageExporter exporter : screenshotExporters.values()) {
			sb.append(String.format("  - %s (.%s) - Transparency: %s\n",
				exporter.getDescription(),
				exporter.getFileExtension(),
				exporter.supportsTransparency() ? "Yes" : "No"
			));
		}
		
		return sb.toString();
	}
}
