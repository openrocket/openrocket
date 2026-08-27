package info.openrocket.swing.gui.figure3d.utils;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT;
import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SEVERITY_HIGH;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SEVERITY_LOW;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SEVERITY_MEDIUM;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SEVERITY_NOTIFICATION;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SOURCE_API;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SOURCE_OTHER;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;
import static org.lwjgl.opengl.GL43.glEnable;

/**
 * Installs OpenGL debug output with sane defaults.
 *
 * <p>Enable by setting the system property {@code -Dopenrocket.gl.debug=true}
 * or environment variable {@code OPENROCKET_GL_DEBUG=true} before starting
 * the application. The callback is installed per GL context to avoid
 * spamming duplicate messages when multiple windows are active.</p>
 */
public final class GLDebug {

	private static final Logger log = LoggerFactory.getLogger(GLDebug.class);
	private static final Set<GLCapabilities> installedContexts =
			Collections.newSetFromMap(new IdentityHashMap<>());
	private static final Map<GLCapabilities, GLDebugMessageCallback> callbacks =
			new IdentityHashMap<>();

	private GLDebug() {
	}

	/**
	 * Enables debug output on the current GL context if requested and supported.
	 *
	 * @param contextLabel short label to identify which window/canvas enabled debugging
	 */
	public static void enableIfRequested(String contextLabel) {
		if (!isDebugRequested()) {
			return;
		}
		GLCapabilities caps = GL.getCapabilities();
		if (caps == null) {
			log.warn("GL capabilities not available; cannot enable debug output for {}", contextLabel);
			return;
		}
		if (!caps.GL_KHR_debug && !caps.GL_ARB_debug_output && !caps.OpenGL43) {
			log.warn("OpenGL debug output not available on this context; KHR_debug/ARB_debug_output missing");
			return;
		}

		synchronized (installedContexts) {
			if (installedContexts.contains(caps)) {
				return; // already enabled for this context
			}
			GLDebugMessageCallback callback = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
				String msg = GLDebugMessageCallback.getMessage(length, message);
				String src = sourceToString(source);
				switch (severity) {
					case GL_DEBUG_SEVERITY_HIGH -> log.error("[GL][{}][{}] {}", contextLabel, src, msg);
					case GL_DEBUG_SEVERITY_MEDIUM -> log.warn("[GL][{}][{}] {}", contextLabel, src, msg);
					case GL_DEBUG_SEVERITY_LOW -> log.info("[GL][{}][{}] {}", contextLabel, src, msg);
					case GL_DEBUG_SEVERITY_NOTIFICATION -> log.debug("[GL][{}][{}] {}", contextLabel, src, msg);
					default -> log.debug("[GL][{}][{}] {}", contextLabel, src, msg);
				}
			});

			glEnable(GL_DEBUG_OUTPUT);
			glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
			glDebugMessageCallback(callback, 0);

			installedContexts.add(caps);
			callbacks.put(caps, callback);
			log.info("OpenGL debug output enabled for {}", contextLabel);
		}
	}

	public static boolean isDebugRequested() {
		String env = System.getenv("OPENROCKET_GL_DEBUG");
		boolean envEnabled = env != null && env.equalsIgnoreCase("true");
		return envEnabled || Boolean.getBoolean("openrocket.gl.debug");
	}

	/** Releases the callback associated with the current context before it is destroyed. */
	public static void disableCurrentContext() {
		GLCapabilities caps;
		try {
			caps = GL.getCapabilities();
		} catch (IllegalStateException e) {
			return;
		}

		GLDebugMessageCallback callback;
		synchronized (installedContexts) {
			installedContexts.remove(caps);
			callback = callbacks.remove(caps);
		}
		if (callback != null) {
			try {
				glDebugMessageCallback(null, 0L);
			} finally {
				callback.free();
			}
		}
	}

	private static String sourceToString(int source) {
		return switch (source) {
			case GL_DEBUG_SOURCE_API -> "API";
			case GL_DEBUG_SOURCE_OTHER -> "OTHER";
			default -> "SRC(" + source + ")";
		};
	}

	/**
	 * Logs currently installed debug callbacks. Useful for troubleshooting.
	 */
	public static void logInstalledContexts() {
		synchronized (installedContexts) {
			if (installedContexts.isEmpty()) {
				log.debug("No GL debug callbacks installed");
			} else {
				log.debug("GL debug callbacks active for {} context(s)", installedContexts.size());
			}
		}
	}
}
