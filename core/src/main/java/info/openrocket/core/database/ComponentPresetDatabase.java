package info.openrocket.core.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.startup.Application;

public class ComponentPresetDatabase extends Database<ComponentPreset> implements ComponentPresetDao {

	private static final Logger logger = LoggerFactory.getLogger(ComponentPresetDatabase.class);

	public ComponentPresetDatabase() {
		super();
	}

	@Override
	public List<ComponentPreset> listAll() {
		return list;
	}

	@Override
	public void insert(ComponentPreset preset) {
		list.add(preset);
	}

	@Override
	public List<ComponentPreset> listForType(ComponentPreset.Type type) {
		if (type == null) {
			return Collections.emptyList();
		}

		List<ComponentPreset> result = new ArrayList<>(list.size() / 6);

		for (ComponentPreset preset : list) {
			if (preset.get(ComponentPreset.TYPE).equals(type)) {
				result.add(preset);
			}
		}
		return result;

	}

	/**
	 * Return a list of component presets based on the type.
	 * All components returned will be of Type type.
	 * 
	 * @param type
	 * @param favorite if true, only return the favorites. otherwise return all
	 *                 matching.
	 * @return
	 */
	@Override
	public List<ComponentPreset> listForType(ComponentPreset.Type type, boolean favorite) {
		if (!favorite) {
			return listForType(type);
		}

		List<ComponentPreset> result = new ArrayList<>(list.size() / 6);

		Set<String> favorites = Application.getPreferences().getComponentFavorites(type);

		for (ComponentPreset preset : list) {
			if (preset.get(ComponentPreset.TYPE).equals(type) && favorites.contains(preset.preferenceKey())) {
				result.add(preset);
			}
		}
		return result;
	}

	@Override
	public List<ComponentPreset> listForTypes(ComponentPreset.Type... type) {
		if (type == null || type.length == 0) {
			return Collections.emptyList();
		}

		if (type.length == 1) {
			return listForType(type[0]);
		}

		List<ComponentPreset> result = new ArrayList<>(list.size() / 6);

		for (ComponentPreset preset : list) {
			ComponentPreset.Type presetType = preset.get(ComponentPreset.TYPE);
			typeLoop:
			for (ComponentPreset.Type value : type) {
				if (presetType.equals(value)) {
					result.add(preset);
					break typeLoop; // from inner loop.
				}
			}

		}
		return result;
	}

	@Override
	public List<ComponentPreset> listForTypes(List<ComponentPreset.Type> types) {
		return listForTypes(types.toArray(new ComponentPreset.Type[0]));
	}

	@Override
	public List<ComponentPreset> find(String manufacturer, String partNo) {
		List<ComponentPreset> presets = new ArrayList<>();
		for (ComponentPreset preset : list) {
			if (preset.getManufacturer().matches(manufacturer) && preset.getPartNo().equals(partNo)) {
				presets.add(preset);
			}
		}
		return presets;
	}

	@Override
	public void setFavorite(ComponentPreset preset, ComponentPreset.Type type, boolean favorite) {
		Application.getPreferences().setComponentFavorite(preset, type, favorite);
		this.fireAddEvent(preset);
	}

}
