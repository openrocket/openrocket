package info.openrocket.swing.gui.figure3d.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.opengl.GL33.*;

/**
 * Utility class for OpenGL debugging and error handling.
 * Provides methods to check for and report OpenGL errors during rendering operations.
 *
 * <p>Debug mode can be enabled/disabled at runtime. When debug mode is disabled,
 * error checking methods return immediately without querying GL state (for performance).</p>
 */
public final class GLUtils {

	private GLUtils() {
	}

	private static final Logger log = LoggerFactory.getLogger(GLUtils.class);

	/** Debug mode flag - set to false in production for better performance */
	private static volatile boolean debugMode = false;

	/**
	 * Enables or disables debug mode.
	 * When debug mode is disabled, GL error checking is skipped for performance.
	 *
	 * @param enabled true to enable debug mode, false to disable
	 */
	public static void setDebugMode(boolean enabled) {
		debugMode = enabled;
		log.info("GL debug mode {}", enabled ? "enabled" : "disabled");
	}

	/**
	 * Returns whether debug mode is currently enabled.
	 *
	 * @return true if debug mode is enabled
	 */
	public static boolean isDebugMode() {
		return debugMode;
	}

	/**
	 * Checks for OpenGL errors and logs them with a location tag.
	 * This method is a no-op when debug mode is disabled.
	 *
	 * @param locationTag a descriptive string indicating where this check is being performed
	 */
	public static void checkGLError(String locationTag) {
		if (!debugMode) return;

		int error;
		while ((error = glGetError()) != GL_NO_ERROR) {
			log.error("OpenGL Error at [{}]: {}", locationTag, getErrorString(error));
		}
	}

	/**
	 * Checks for OpenGL errors and throws a RuntimeException if any are found.
	 * This method always checks, regardless of debug mode setting.
	 *
	 * @param locationTag a descriptive string indicating where this check is being performed
	 * @throws RuntimeException if an OpenGL error is detected
	 */
	public static void assertNoGLError(String locationTag) {
		int error = glGetError();
		if (error != GL_NO_ERROR) {
			String errorStr = getErrorString(error);
			log.error("OpenGL Error at [{}]: {}", locationTag, errorStr);
			// Clear remaining errors
			while (glGetError() != GL_NO_ERROR) {
				// drain error queue
			}
			throw new RuntimeException("OpenGL error at [" + locationTag + "]: " + errorStr);
		}
	}

	/**
	 * Checks for OpenGL errors and returns the count.
	 * Useful for testing and validation.
	 *
	 * @param locationTag a descriptive string indicating where this check is being performed
	 * @return the number of GL errors detected
	 */
	public static int countGLErrors(String locationTag) {
		int count = 0;
		int error;
		while ((error = glGetError()) != GL_NO_ERROR) {
			count++;
			if (debugMode) {
				log.warn("OpenGL Error #{} at [{}]: {}", count, locationTag, getErrorString(error));
			}
		}
		return count;
	}

	/**
	 * Clears all pending GL errors without logging.
	 * Useful before starting a critical operation to ensure a clean error state.
	 */
	public static void clearGLErrors() {
		while (glGetError() != GL_NO_ERROR) {
			// drain error queue
		}
	}

	/**
	 * Converts a GL error code to a human-readable string.
	 *
	 * @param error the GL error code
	 * @return a string representation of the error
	 */
	private static String getErrorString(int error) {
		return switch (error) {
			case GL_INVALID_ENUM -> "INVALID_ENUM";
			case GL_INVALID_VALUE -> "INVALID_VALUE";
			case GL_INVALID_OPERATION -> "INVALID_OPERATION";
			case GL_STACK_OVERFLOW -> "STACK_OVERFLOW";
			case GL_STACK_UNDERFLOW -> "STACK_UNDERFLOW";
			case GL_OUT_OF_MEMORY -> "OUT_OF_MEMORY";
			case GL_INVALID_FRAMEBUFFER_OPERATION -> "INVALID_FRAMEBUFFER_OPERATION";
			default -> "UNKNOWN_ERROR(" + error + ")";
		};
	}
}
