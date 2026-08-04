package info.openrocket.swing.gui.figure3d.rendering;

/** Reports a failure to load, compile, or link a shader program. */
public class ShaderException extends RuntimeException {
	public ShaderException(String message) {
		super(message);
	}

	public ShaderException(String message, Throwable cause) {
		super(message, cause);
	}
}
