package info.openrocket.swing.gui.figure3d.rendering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;

/**
 * Utility for surfacing silent OpenGL errors as Java-side logs and exceptions.
 *
 * OpenGL does not report failures except through {@code glGetError()}; a bad draw
 * call or buffer setup typically just renders nothing. Call {@link #check(String)}
 * after one-time setup work (mesh upload, shader link, framebuffer creation) so
 * mistakes fail fast with a stack trace, and {@link #debugCheck(String)} in per-frame
 * code, which is a no-op unless the {@code openrocket.figure3d.glDebug} system
 * property is set.
 *
 * All methods must be called on a thread with a current OpenGL context.
 */
public final class GLErrors {
	private static final Logger log = LoggerFactory.getLogger(GLErrors.class);

	/** System property that enables per-frame {@link #debugCheck(String)} polling. */
	public static final String DEBUG_PROPERTY = "openrocket.figure3d.glDebug";

	private GLErrors() {
	}

	/**
	 * Starts a resource-creation phase at the native-canvas boundary.
	 *
	 * <p>Error flags belong to a context rather than to an individual call. A
	 * transient default-framebuffer failure during native window setup can
	 * therefore remain pending until later resource creation checks the context. At
	 * this boundary that one known error is safe to discard. Any other error is
	 * surfaced instead of being misattributed to, or hidden by, a later upload.</p>
	 *
	 * @param operation description of the operation about to begin
	 */
	public static void beginResourceCreation(String operation) {
		verifyResourceBoundary(operation, drainErrors());
	}

	static void verifyResourceBoundary(String operation, List<Integer> errors) {
		if (errors.isEmpty()) {
			return;
		}
		if (errors.stream().allMatch(error -> error == GL_INVALID_FRAMEBUFFER_OPERATION)) {
			log.debug("Ignored transient default-framebuffer error before '{}': {}",
					operation, errorStrings(errors));
			return;
		}
		log.error("OpenGL error before '{}': {}", operation, errorStrings(errors));
		throw new GLException("before " + operation, errors);
	}

	/**
	 * Drains all pending OpenGL error flags and throws if any were set.
	 *
	 * Intended for initialization and resource-creation code where an error means
	 * the resulting GL objects are unusable and continuing would only hide the bug.
	 *
	 * @param operation Description of the operation just performed (used in the log/exception)
	 * @throws GLException If one or more OpenGL error flags were set
	 */
	public static void check(String operation) {
		List<Integer> errors = drainErrors();
		if (!errors.isEmpty()) {
			log.error("OpenGL error during '{}': {}", operation, errorStrings(errors));
			throw new GLException(operation, errors);
		}
	}

	/**
	 * Drains all pending OpenGL error flags and logs them without throwing.
	 *
	 * Use in per-frame or recovery code where aborting the frame would be worse
	 * than rendering a glitched one.
	 *
	 * @param operation Description of the operation just performed
	 * @return true if any error flags were set
	 */
	public static boolean logErrors(String operation) {
		List<Integer> errors = drainErrors();
		if (!errors.isEmpty()) {
			log.warn("OpenGL error during '{}': {}", operation, errorStrings(errors));
			return true;
		}
		return false;
	}

	/**
	 * Per-frame error poll, disabled by default because {@code glGetError()} can
	 * stall the GPU pipeline. Enable with {@code -Dopenrocket.figure3d.glDebug=true}
	 * when hunting rendering bugs; errors are logged, not thrown.
	 *
	 * @param operation Description of the operation just performed
	 */
	public static void debugCheck(String operation) {
		if (isDebugEnabled()) {
			logErrors(operation);
		}
	}

	/**
	 * @return true if per-frame GL debug checking is enabled via {@value #DEBUG_PROPERTY}
	 */
	public static boolean isDebugEnabled() {
		return Boolean.getBoolean(DEBUG_PROPERTY);
	}

	/**
	 * Maps an OpenGL error code to its symbolic name.
	 *
	 * @param errorCode The value returned by {@code glGetError()}
	 * @return The GL constant name, or a hex string for unknown codes
	 */
	public static String errorString(int errorCode) {
		return switch (errorCode) {
			case GL_INVALID_ENUM -> "GL_INVALID_ENUM";
			case GL_INVALID_VALUE -> "GL_INVALID_VALUE";
			case GL_INVALID_OPERATION -> "GL_INVALID_OPERATION";
			case GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW";
			case GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW";
			case GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY";
			case GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION";
			default -> String.format("UNKNOWN_GL_ERROR(0x%04X)", errorCode);
		};
	}

	private static List<Integer> drainErrors() {
		List<Integer> errors = new ArrayList<>();
		int errorCode;
		while ((errorCode = glGetError()) != GL_NO_ERROR) {
			errors.add(errorCode);
			if (errors.size() >= 32) {
				// A context error or driver bug can make glGetError never return
				// GL_NO_ERROR; don't loop forever on it.
				break;
			}
		}
		return errors;
	}

	private static String errorStrings(List<Integer> errors) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < errors.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(errorString(errors.get(i)));
		}
		return sb.toString();
	}
}
