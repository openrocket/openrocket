package info.openrocket.swing.gui.figure3d.rendering;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception thrown when an OpenGL operation left one or more error flags set.
 *
 * OpenGL itself never throws; errors are only visible by polling {@code glGetError()}.
 * This exception surfaces those silent failures on the Java side, together with the
 * name of the operation that was being checked, so rendering bugs can be traced back
 * to their source instead of only manifesting as visual glitches.
 */
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
