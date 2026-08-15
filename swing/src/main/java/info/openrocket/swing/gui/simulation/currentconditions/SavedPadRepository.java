package info.openrocket.swing.gui.simulation.currentconditions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import info.openrocket.core.startup.Application;

/** Stores the user's named launch pads in the platform preferences store. */
final class SavedPadRepository {
	private static final String NODE_NAME = "weatherSavedPads";
	private static final String LAST_SELECTED_PAD = "lastSelectedPad";
	private final Preferences root;

	SavedPadRepository() {
		this(Application.getPreferences().getNode(NODE_NAME));
	}

	SavedPadRepository(Preferences root) {
		this.root = root;
	}

	List<SavedPad> load() {
		try {
			List<SavedPad> pads = new ArrayList<>();
			for (String id : root.childrenNames()) {
				Preferences node = root.node(id);
				String name = node.get("name", "").trim();
				double latitude = node.getDouble("latitude", Double.NaN);
				double longitude = node.getDouble("longitude", Double.NaN);
				String timezoneId = node.get("timezone", "").trim();
				if (!name.isEmpty() && Double.isFinite(latitude) && Double.isFinite(longitude)) {
					pads.add(new SavedPad(id, name, latitude, longitude,
							timezoneId.isEmpty() ? null : timezoneId));
				}
			}
			pads.sort(Comparator.comparing(SavedPad::name, String.CASE_INSENSITIVE_ORDER));
			return pads;
		} catch (BackingStoreException e) {
			return List.of();
		}
	}

	SavedPad save(String name, DeviceLocation location) {
		String normalizedName = name == null ? "" : name.trim();
		if (normalizedName.isEmpty()) {
			throw new IllegalArgumentException("Pad name is required");
		}
		SavedPad existing = load().stream()
				.filter(pad -> pad.name().equalsIgnoreCase(normalizedName))
				.findFirst().orElse(null);
		String id = existing == null ? UUID.randomUUID().toString() : existing.id();
		Preferences node = root.node(id);
		node.put("name", normalizedName);
		node.putDouble("latitude", location.latitude());
		node.putDouble("longitude", location.longitude());
		if (location.timezoneId() == null || location.timezoneId().isBlank()) {
			node.remove("timezone");
		} else {
			node.put("timezone", location.timezoneId());
		}
		return new SavedPad(id, normalizedName, location.latitude(), location.longitude(), location.timezoneId());
	}

	void delete(SavedPad pad) {
		try {
			root.node(pad.id()).removeNode();
			if (pad.id().equals(lastSelectedId())) {
				clearLastSelected();
			}
		} catch (BackingStoreException ignored) {
			// The preferences backend is best effort; a failed deletion is harmless.
		}
	}

	String lastSelectedId() {
		String id = root.get(LAST_SELECTED_PAD, "").trim();
		return id.isEmpty() ? null : id;
	}

	void setLastSelected(SavedPad pad) {
		root.put(LAST_SELECTED_PAD, pad.id());
	}

	void clearLastSelected() {
		root.remove(LAST_SELECTED_PAD);
	}

	record SavedPad(String id, String name, double latitude, double longitude, String timezoneId) {
		DeviceLocation location() {
			return new DeviceLocation(latitude, longitude, Double.NaN, Double.NaN, name, timezoneId);
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
