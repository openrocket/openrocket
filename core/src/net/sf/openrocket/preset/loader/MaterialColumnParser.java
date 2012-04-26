package net.sf.openrocket.preset.loader;

import java.util.Collections;
import java.util.Map;

import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedPropertyMap;

public class MaterialColumnParser extends BaseColumnParser {

	private Map<String,Material> materialMap = Collections.<String,Material>emptyMap();
	
	// FIXME - BULK vs other types.
	
	public MaterialColumnParser(Map<String,Material> materialMap) {
		super("Material");
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {

		Material m = materialMap.get(columnData);
		if ( m == null ) {
			m = new Material.Bulk(columnData, 0.0, true);
		}
		props.put(ComponentPreset.MATERIAL, m);
		
	}

}
