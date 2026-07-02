package info.openrocket.swing.gui.figure3d.rendering;

/**
 * Exception thrown when loading, compiling, or linking a shader program fails.
 *
 * Carries the shader source path(s) and the driver's info log in the message so
 * shader bugs can be traced to the exact file instead of surfacing later as a
 * generic runtime failure.
 */
public class ShaderException extends RuntimeException {
	public ShaderException(String message) {
		super(message);
	}

	public ShaderException(String message, Throwable cause) {
		super(message, cause);
	}
}
