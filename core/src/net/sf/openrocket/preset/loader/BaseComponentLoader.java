package net.sf.openrocket.preset.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPresetFactory;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;

public abstract class BaseComponentLoader extends RocksimComponentFileLoader {

	List<ComponentPreset> presets;

	public BaseComponentLoader(MaterialHolder materials) {
		super();
		presets = new ArrayList<ComponentPreset>();

		fileColumns.add( new ManufacturerColumnParser() );
		fileColumns.add( new StringColumnParser("Part No.", ComponentPreset.PARTNO));
		fileColumns.add( new StringColumnParser("Desc.", ComponentPreset.DESCRIPTION));
		fileColumns.add(new MaterialColumnParser(materials));
		fileColumns.add(new MassColumnParser("Mass","Mass units"));

	}

	protected abstract ComponentPreset.Type getComponentPresetType();
	
	public List<ComponentPreset> getPresets() {
		return presets;
	}
	
	@Override
	protected void postProcess(TypedPropertyMap props) {
		try {
			props.put(ComponentPreset.TYPE, getComponentPresetType());
			ComponentPreset preset = ComponentPresetFactory.create(props);
			presets.add(preset);
		} catch ( InvalidComponentPresetException ex ) {
			System.err.println(ex.getMessage());
			System.err.println(props);
		}
	}

}