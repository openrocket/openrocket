package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.ComponentPresetFactory;
import info.openrocket.core.preset.InvalidComponentPresetException;
import info.openrocket.core.preset.TypedPropertyMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseComponentLoader extends RockSimComponentFileLoader {

	List<ComponentPreset> presets;

	public BaseComponentLoader(MaterialHolder materials, File theBasePathToLoadFrom) {
		super(theBasePathToLoadFrom);
		presets = new ArrayList<>();

		fileColumns.add(new ManufacturerColumnParser());
		fileColumns.add(new StringColumnParser("Part No.", ComponentPreset.PARTNO));
		fileColumns.add(new StringColumnParser("Desc.", ComponentPreset.DESCRIPTION));
		fileColumns.add(new MaterialColumnParser(materials));
		fileColumns.add(new MassColumnParser("Mass", "Mass units"));

	}

	protected abstract ComponentPreset.Type getComponentPresetType();

	public List<ComponentPreset> getPresets() {
		return presets;
	}

	@Override
	protected void postProcess(TypedPropertyMap props) {
		try {
			// Some Rocksim files don't contain description, so set it to the part no when
			// not available.
			if (!props.containsKey(ComponentPreset.DESCRIPTION)) {
				props.put(ComponentPreset.DESCRIPTION, props.get(ComponentPreset.PARTNO));
			}
			props.put(ComponentPreset.TYPE, getComponentPresetType());
			ComponentPreset preset = ComponentPresetFactory.create(props);
			presets.add(preset);
		} catch (InvalidComponentPresetException ex) {
			System.err.println(ex.getMessage());
			System.err.println(props);
		}
	}

}