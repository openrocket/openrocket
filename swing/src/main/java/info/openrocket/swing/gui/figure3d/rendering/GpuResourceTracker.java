package info.openrocket.swing.gui.figure3d.rendering;

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

	private static final Map<ResourceType, Map<Integer, ResourceRecord>> LIVE_RESOURCES = new EnumMap<>(ResourceType.class);

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
		if (id <= 0 || type == null) {
			return;
		}
		String safeLabel = label == null ? "" : label;
		LIVE_RESOURCES.get(type).put(id, new ResourceRecord(id, safeLabel, System.nanoTime()));
	}

	/**
	 * Marks a resource as released.
	 */
	public static void release(ResourceType type, int id) {
		if (id <= 0 || type == null) {
			return;
		}
		LIVE_RESOURCES.get(type).remove(id);
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
		for (Map.Entry<ResourceType, Map<Integer, ResourceRecord>> entry : LIVE_RESOURCES.entrySet()) {
			for (ResourceRecord record : entry.getValue().values()) {
				leaks.add(entry.getKey() + "#" + record.id + " (" + record.label + ")");
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
	 * Clears all tracking data. Useful for tests.
	 */
	public static void reset() {
		for (Map<Integer, ResourceRecord> map : LIVE_RESOURCES.values()) {
			map.clear();
		}
	}
}
