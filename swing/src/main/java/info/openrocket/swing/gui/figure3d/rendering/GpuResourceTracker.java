package info.openrocket.swing.gui.figure3d.rendering;

import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight tracker for GPU resources to help catch leaks and double frees.
 *
 * <p>Register resources when they are created and release them on cleanup.
 * A leak summary can be logged at shutdown to highlight dangling handles.</p>
 */
public final class GpuResourceTracker {

	private static final Logger log = LoggerFactory.getLogger(GpuResourceTracker.class);
	private static final Object NO_CONTEXT = new Object();

	public enum ResourceType {
		TEXTURE,
		BUFFER,
		FRAMEBUFFER,
		RENDERBUFFER,
		PROGRAM,
		SHADER,
		VERTEX_ARRAY
	}

	private record ResourceRecord(int id, String label, long createdAtNanos) {
	}

	private static final class ResourceKey {
		private final Object context;
		private final int id;

		private ResourceKey(Object context, int id) {
			this.context = context;
			this.id = id;
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof ResourceKey key && context == key.context && id == key.id;
		}

		@Override
		public int hashCode() {
			return 31 * System.identityHashCode(context) + id;
		}
	}

	private static final Map<ResourceType, Map<ResourceKey, ResourceRecord>> LIVE_RESOURCES =
			new EnumMap<>(ResourceType.class);

	static {
		for (ResourceType type : ResourceType.values()) {
			LIVE_RESOURCES.put(type, new ConcurrentHashMap<>());
		}
	}

	private GpuResourceTracker() {
	}

	/**
	 * Registers a GPU resource handle.
	 *
	 * @param type  resource category
	 * @param id    GL object id
	 * @param label optional label to identify the owner
	 */
	public static void register(ResourceType type, int id, String label) {
		registerForContext(currentContext(), type, id, label);
	}

	static void registerForContext(Object context, ResourceType type, int id, String label) {
		if (id <= 0 || type == null) {
			return;
		}
		String safeLabel = label == null ? "" : label;
		ResourceKey key = new ResourceKey(normalizeContext(context), id);
		ResourceRecord record = new ResourceRecord(id, safeLabel, System.nanoTime());
		ResourceRecord previous = LIVE_RESOURCES.get(type).putIfAbsent(key, record);
		if (previous != null) {
			log.warn("GPU resource registered twice in {}: {}#{} (existing='{}', new='{}')",
					contextLabel(key.context), type, id, previous.label, safeLabel);
		}
	}

	/**
	 * Marks a resource as released.
	 */
	public static void release(ResourceType type, int id) {
		releaseForContext(currentContext(), type, id);
	}

	static void releaseForContext(Object context, ResourceType type, int id) {
		if (id <= 0 || type == null) {
			return;
		}
		ResourceKey key = new ResourceKey(normalizeContext(context), id);
		if (LIVE_RESOURCES.get(type).remove(key) == null) {
			log.warn("GPU resource released without a matching registration in {}: {}#{}",
					contextLabel(key.context), type, id);
		}
	}

	/**
	 * Emits a warning for any still-live resources.
	 *
	 * @param reason short description of why the check is being performed (e.g., "shutdown")
	 */
	public static void logLiveResources(String reason) {
		logLiveResources(reason, true);
	}

	/**
	 * Emits a log for any still-live resources.
	 *
	 * @param reason short description of why the check is being performed (e.g., "shutdown")
	 * @param warn   whether to log leaks at WARN (otherwise INFO)
	 */
	public static void logLiveResources(String reason, boolean warn) {
		List<String> leaks = new ArrayList<>();
		for (Map.Entry<ResourceType, Map<ResourceKey, ResourceRecord>> entry : LIVE_RESOURCES.entrySet()) {
			for (Map.Entry<ResourceKey, ResourceRecord> resourceEntry : entry.getValue().entrySet()) {
				ResourceKey key = resourceEntry.getKey();
				ResourceRecord record = resourceEntry.getValue();
				leaks.add(entry.getKey() + "#" + record.id + "@" + contextLabel(key.context)
						+ " (" + record.label + ")");
			}
		}
		if (!leaks.isEmpty()) {
			if (warn) {
				log.warn("GPU resources still live during {}: {}", reason, String.join(", ", leaks));
			} else {
				log.info("GPU resources still live during {}: {}", reason, String.join(", ", leaks));
			}
		} else {
			log.debug("All tracked GPU resources released during {}", reason);
		}
	}

	/**
	 * Number of resources of one type that have been registered and not released.
	 *
	 * @param type resource category
	 * @return the live count, or 0 for an unknown type
	 */
	public static int liveCount(ResourceType type) {
		if (type == null) {
			return 0;
		}
		return LIVE_RESOURCES.get(type).size();
	}

	/**
	 * Total number of registered-and-not-released resources across every type. A count that
	 * climbs while the scene is unchanged is a leak: rendering a frame should not allocate.
	 *
	 * @return the total live count
	 */
	public static int liveCount() {
		int total = 0;
		for (Map<ResourceKey, ResourceRecord> map : LIVE_RESOURCES.values()) {
			total += map.size();
		}
		return total;
	}

	/**
	 * Clears all tracking data. Useful for tests.
	 */
	public static void reset() {
		for (Map<ResourceKey, ResourceRecord> map : LIVE_RESOURCES.values()) {
			map.clear();
		}
	}

	private static Object currentContext() {
		try {
			return normalizeContext(GL.getCapabilities());
		} catch (IllegalStateException e) {
			return NO_CONTEXT;
		}
	}

	private static Object normalizeContext(Object context) {
		return context != null ? context : NO_CONTEXT;
	}

	private static String contextLabel(Object context) {
		return context == NO_CONTEXT
				? "no-context"
				: "context-" + Integer.toHexString(System.identityHashCode(context));
	}
}
