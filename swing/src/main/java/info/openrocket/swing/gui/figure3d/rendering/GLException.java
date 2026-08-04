package info.openrocket.swing.gui.figure3d.rendering;

import java.util.List;
import java.util.stream.Collectors;

/** Reports OpenGL error flags left by a checked operation. */
public class GLException extends RuntimeException {
	private final List<Integer> errorCodes;

	public GLException(String operation, List<Integer> errorCodes) {
		super(buildMessage(operation, errorCodes));
		this.errorCodes = List.copyOf(errorCodes);
	}

	private static String buildMessage(String operation, List<Integer> errorCodes) {
		String joinedErrors = errorCodes.stream()
				.map(GLErrors::errorString)
				.collect(Collectors.joining(", "));
		return "OpenGL error during '" + operation + "': " + joinedErrors;
	}

	public List<Integer> getErrorCodes() {
		return errorCodes;
	}
}
