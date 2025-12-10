package info.openrocket.swing.gui.figure3d.export;

import java.io.IOException;

/**
 * Interface for exporting screenshots/framebuffer data to various image formats.
 * Implementations should handle the specifics of each format.
 */
public interface ImageExporter {
	
	/**
	 * Exports the contents of the specified framebuffer to the provided file path.
	 * 
	 * @param framebufferId The OpenGL framebuffer object to read from (0 for the default framebuffer)
	 * @param width The width of the viewport to capture
	 * @param height The height of the viewport to capture  
	 * @param filePath The path where the file should be saved
	 * @throws IOException If the export operation fails
	 */
	void export(int framebufferId, int width, int height, String filePath) throws IOException;
	
	/**
	 * Gets the file extension for this exporter (without the dot).
	 * 
	 * @return The file extension (e.g., "png", "jpg", "tga")
	 */
	String getFileExtension();
	
	/**
	 * Gets a human-readable description of this export format.
	 * 
	 * @return A description of the format (e.g., "PNG Image", "JPEG Image")
	 */
	String getDescription();
	
	/**
	 * Indicates whether this format supports transparency/alpha channel.
	 * 
	 * @return true if alpha channel is supported
	 */
	boolean supportsTransparency();
	
	/**
	 * Gets the compression quality setting for formats that support it.
	 * Only relevant for lossy formats like JPEG.
	 * 
	 * @return Quality value from 0.0 to 1.0, or -1 if not applicable
	 */
	default float getCompressionQuality() {
		return -1.0f;
	}
	
	/**
	 * Sets the compression quality for formats that support it.
	 * Only relevant for lossy formats like JPEG.
	 * 
	 * @param quality Quality value from 0.0 to 1.0
	 */
	default void setCompressionQuality(float quality) {
		// Default implementation does nothing
	}
}
