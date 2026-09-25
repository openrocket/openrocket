package info.openrocket.swing.gui.figure3d.rendering;

import java.util.Locale;

import org.lwjgl.opengl.awt.GLData;

/**
 * Keeps a context-free snapshot of the most recently negotiated OpenGL pixel
 * format and context attributes.
 *
 * <p>{@link GLData} is populated by lwjgl3-awt while the native context is
 * created. The values are copied into an immutable string immediately so
 * diagnostics can be collected later from the EDT, after the canvas has been
 * disposed, and without making an OpenGL context current.</p>
 */
public final class GLContextDiagnostics {

	private static volatile String lastDescription;

	private GLContextDiagnostics() {
	}

	/**
	 * Records the requested and effective attributes for a newly created context.
	 *
	 * @return the recorded description, suitable for logging
	 */
	public static String record(GLData requested, GLData effective) {
		String description = describe(requested, effective);
		lastDescription = description;
		return description;
	}

	/**
	 * Returns the latest snapshot without requiring a current OpenGL context.
	 */
	public static String describeForBugReport() {
		String description = lastDescription;
		if (description == null) {
			return "not initialized (no 3D view opened this session)";
		}
		return description;
	}

	static String describe(GLData requested, GLData effective) {
		return "requested [" + describe(requested) + "]; effective [" + describe(effective) + "]";
	}

	private static String describe(GLData data) {
		if (data == null) {
			return "not reported";
		}

		StringBuilder description = new StringBuilder();
		description.append(describeApi(data.api));
		if (data.majorVersion > 0) {
			description.append(' ').append(data.majorVersion).append('.').append(data.minorVersion);
		} else {
			description.append(" version unspecified");
		}
		description.append(", version-policy=").append(describeEnum(data.versionPolicy));
		description.append(", profile=").append(describeEnum(data.profile));
		description.append(", RGBA=").append(data.redSize).append('/').append(data.greenSize)
				.append('/').append(data.blueSize).append('/').append(data.alphaSize);
		description.append(", depth/stencil=").append(data.depthSize).append('/').append(data.stencilSize);
		description.append(", double-buffer=").append(data.doubleBuffer);
		description.append(", stereo=").append(data.stereo);
		description.append(", sample-buffers=").append(data.sampleBuffers);
		description.append(", samples=").append(data.samples);
		description.append(", sRGB=").append(data.sRGB);
		description.append(", float-pixels=").append(data.pixelFormatFloat);
		description.append(", swap-interval=")
				.append(data.swapInterval == null ? "unspecified" : data.swapInterval);
		description.append(", debug=").append(data.debug);
		description.append(", forward-compatible=").append(data.forwardCompatible);
		description.append(", robustness=").append(data.robustness);
		description.append(", lose-on-reset=").append(data.loseContextOnReset);
		description.append(", reset-isolation=").append(data.contextResetIsolation);
		description.append(", release=").append(describeEnum(data.contextReleaseBehavior));
		return description.toString();
	}

	private static String describeApi(GLData.API api) {
		if (api == GLData.API.GL) {
			return "OpenGL";
		}
		if (api == GLData.API.GLES) {
			return "OpenGL ES";
		}
		return "API unspecified";
	}

	private static String describeEnum(Enum<?> value) {
		return value == null ? "unspecified" : value.name().toLowerCase(Locale.ROOT);
	}
}
