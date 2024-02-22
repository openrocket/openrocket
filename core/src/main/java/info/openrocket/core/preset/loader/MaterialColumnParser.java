package info.openrocket.core.preset.loader;

import info.openrocket.core.database.Databases;
import info.openrocket.core.material.Material;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.util.StringUtils;

public class MaterialColumnParser extends BaseColumnParser {

	private final MaterialHolder materialMap;

	private final TypedKey<Material> param;

	public MaterialColumnParser(MaterialHolder materialMap, String columnName, TypedKey<Material> param) {
		super(columnName);
		this.param = param;
		this.materialMap = materialMap;
	}

	public MaterialColumnParser(MaterialHolder materialMap) {
		this(materialMap, "Material", ComponentPreset.MATERIAL);
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {

		if (StringUtils.isEmpty(columnData)) {
			return;
		}

		Material.Bulk tmpMaterial = (Material.Bulk) Databases.findMaterial(Material.Type.BULK, columnData, 0.0);
		Material.Bulk m = materialMap.getBulkMaterial(tmpMaterial);
		props.put(param, m != null ? m : tmpMaterial);

	}

}
