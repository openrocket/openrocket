package info.openrocket.swing.gui.figure3d.rendering;

import java.util.Locale;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes how much GPU memory the render chain may reasonably spend on a
 * context.
 *
 * <p>The offscreen scene target, the post-processing targets and the shadow map
 * are all sized from the physical framebuffer, and the only limits applied to
 * them are the driver's own maxima ({@code GL_MAX_SAMPLES},
 * {@code GL_MAX_TEXTURE_SIZE}). Those maxima describe what the driver will
 * accept, not what the device can comfortably hold: at HIGH quality a 2880x1800
 * framebuffer asks for roughly 400 MB, which is fine on a discrete card and
 * hostile on an integrated GPU that carves its memory out of system RAM.
 *
 * <p>OpenGL has no portable memory query, so the budget is resolved in three
 * steps: the AMD and NVIDIA vendor extensions when present, then the renderer
 * string, and finally an assumption that an unrecognised device is capable.
 * Nothing here fails a context - an unknown device keeps today's behaviour.
 */
public final class GpuMemoryProfile {

	private static final Logger log = LoggerFactory.getLogger(GpuMemoryProfile.class);

	/** Forces the profile regardless of what the device reports. */
	public static final String FORCE_PROFILE_PROPERTY = "openrocket.figure3d.forceConstrainedGpu";

	/** {@code GL_VBO_FREE_MEMORY_ATI} - free memory in KB, from GL_ATI_meminfo. */
	private static final int VBO_FREE_MEMORY_ATI = 0x87FB;
	/** {@code GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX} - dedicated memory in KB. */
	private static final int GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX = 0x9047;

	/**
	 * Devices whose total dedicated memory is below this are treated as
	 * constrained. A full HIGH quality chain at a common laptop resolution needs
	 * roughly 400 MB, so a device that cannot report at least twice that has no
	 * headroom for the reallocation churn a window resize produces.
	 */
	private static final int CONSTRAINED_TOTAL_MEGABYTES = 1024;

	/**
	 * Threshold for extensions that report <em>free</em> rather than total memory.
	 * Free memory moves with whatever else is on the GPU, and the profile is
	 * decided once per context, so this is deliberately far below
	 * {@link #CONSTRAINED_TOTAL_MEGABYTES}: it should catch a genuinely small
	 * device, not a capable one that happened to be busy at context creation.
	 */
	private static final int CONSTRAINED_FREE_MEGABYTES = 384;

	/** Renderer-string markers for GPUs that share system memory. */
	private static final String[] INTEGRATED_RENDERER_MARKERS = {
			"intel",
			"iris",
			"uhd graphics",
			"hd graphics",
			"radeon(tm) graphics",
			"radeon vega",
			"llvmpipe",
			"softpipe",
			"swiftshader",
			"microsoft basic render"
	};

	/**
	 * The most recently detected profile. GL strings can only be read from a
	 * thread with a current context, so the profile is cached here for callers
	 * that run elsewhere - notably the bug report dialog on the EDT.
	 */
	private static volatile GpuMemoryProfile lastDetected;

	private final boolean constrained;
	private final String description;
	private final String vendor;
	private final String renderer;
	private final String glVersion;

	private GpuMemoryProfile(boolean constrained, String description,
			String vendor, String renderer, String glVersion) {
		this.constrained = constrained;
		this.description = description;
		this.vendor = vendor;
		this.renderer = renderer;
		this.glVersion = glVersion;
	}

	/**
	 * Detects the profile for the context that is current on the calling thread.
	 * Must be called from the render thread with a live GL context.
	 */
	public static GpuMemoryProfile detect() {
		String vendor = describeGlString(GL11.GL_VENDOR);
		String renderer = describeGlString(GL11.GL_RENDERER);
		String glVersion = describeGlString(GL11.GL_VERSION);

		GpuMemoryProfile profile = resolve(vendor, renderer, glVersion);
		lastDetected = profile;
		return profile;
	}

	private static GpuMemoryProfile resolve(String vendor, String renderer, String glVersion) {
		// Lets the reduced path be exercised on any GPU, both for tests and so a
		// user reporting a crash can try it without needing a custom build.
		String override = System.getProperty(FORCE_PROFILE_PROPERTY);
		if (override != null) {
			boolean forced = Boolean.parseBoolean(override.trim());
			return new GpuMemoryProfile(forced, "forced by " + FORCE_PROFILE_PROPERTY + "=" + override.trim(),
					vendor, renderer, glVersion);
		}

		GpuMemoryProfile reported = detectFromVendorExtensions(vendor, renderer, glVersion);
		if (reported != null) {
			return reported;
		}

		String normalized = renderer.toLowerCase(Locale.ROOT);
		for (String marker : INTEGRATED_RENDERER_MARKERS) {
			if (normalized.contains(marker)) {
				return new GpuMemoryProfile(true, "renderer matched '" + marker + "'",
						vendor, renderer, glVersion);
			}
		}

		return new GpuMemoryProfile(false, "assumed capable", vendor, renderer, glVersion);
	}

	/**
	 * @return a profile derived from a vendor extension, or {@code null} when
	 *         neither extension is available or the query returns nothing usable
	 */
	private static GpuMemoryProfile detectFromVendorExtensions(String vendor, String renderer, String glVersion) {
		GLCapabilities capabilities = GL.getCapabilities();
		if (capabilities == null) {
			return null;
		}

		// Both extensions report kilobytes, but not the same quantity: ATI_meminfo
		// returns free memory (four values, the first being the total free pool)
		// while NVX reports the total dedicated pool. They get separate thresholds.
		try {
			if (capabilities.GL_NVX_gpu_memory_info) {
				int totalMegabytes = GL11.glGetInteger(GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX) / 1024;
				if (totalMegabytes > 0) {
					return new GpuMemoryProfile(totalMegabytes < CONSTRAINED_TOTAL_MEGABYTES,
							"NVX reports " + totalMegabytes + " MB dedicated", vendor, renderer, glVersion);
				}
			}
			if (capabilities.GL_ATI_meminfo) {
				int[] values = new int[4];
				GL11.glGetIntegerv(VBO_FREE_MEMORY_ATI, values);
				int freeMegabytes = values[0] / 1024;
				if (freeMegabytes > 0) {
					return new GpuMemoryProfile(freeMegabytes < CONSTRAINED_FREE_MEGABYTES,
							"ATI_meminfo reports " + freeMegabytes + " MB free", vendor, renderer, glVersion);
				}
			}
		} catch (RuntimeException e) {
			// A driver that advertises an extension but rejects the query must not
			// take the context down; fall through to the renderer-string heuristic.
			log.debug("GPU memory query failed, falling back to renderer string", e);
		} finally {
			// The queries above run only when advertised, but a driver that
			// advertises and then rejects would leave an error flagged for the
			// renderer's own GLErrors.check() to trip over.
			GL11.glGetError();
		}
		return null;
	}

	/**
	 * @return {@code true} when the render chain should trade quality for a
	 *         smaller memory footprint
	 */
	public boolean isConstrained() {
		return constrained;
	}

	/**
	 * @return how the profile was decided, for diagnostics
	 */
	public String getDescription() {
		return description;
	}

	public String getVendor() {
		return vendor;
	}

	public String getRenderer() {
		return renderer;
	}

	public String getGlVersion() {
		return glVersion;
	}

	/**
	 * @return the profile detected for the most recently created 3D context, or
	 *         {@code null} when no 3D view has been opened this session
	 */
	public static GpuMemoryProfile getLastDetected() {
		return lastDetected;
	}

	/**
	 * Describes the GPU for a bug report. Safe to call from any thread and
	 * without a GL context; reports that no 3D view was opened rather than
	 * failing.
	 */
	public static String describeForBugReport() {
		GpuMemoryProfile profile = lastDetected;
		if (profile == null) {
			return "not initialized (no 3D view opened this session)";
		}
		return profile.vendor + " / " + profile.renderer
				+ " / OpenGL " + profile.glVersion
				+ " / memory profile: " + (profile.constrained ? "constrained" : "full")
				+ " (" + profile.description + ")";
	}

	private static String describeGlString(int name) {
		String value = GL11.glGetString(name);
		return value == null || value.isBlank() ? "unknown" : value;
	}

	@Override
	public String toString() {
		return "GpuMemoryProfile[constrained=" + constrained + ", " + description
				+ ", renderer=" + renderer + "]";
	}
}
