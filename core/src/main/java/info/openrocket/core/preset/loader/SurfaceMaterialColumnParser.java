package info.openrocket.core.preset.loader;

import info.openrocket.core.database.Databases;
import info.openrocket.core.material.Material;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.util.StringUtils;

public class SurfaceMaterialColumnParser extends BaseColumnParser {

	private final MaterialHolder materialMap;

	private final TypedKey<Material> param;

	public SurfaceMaterialColumnParser(MaterialHolder materialMap, String columnName, TypedKey<Material> param) {
		super(columnName);
		this.param = param;
		this.materialMap = materialMap;
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {

		if (StringUtils.isEmpty(columnData)) {
			return;
		}

		Material.Surface myMaterial = (Material.Surface) Databases.findMaterial(Material.Type.SURFACE, columnData, 0.0);
		Material.Surface m = materialMap.getSurfaceMaterial(myMaterial, null);
		props.put(param, m != null ? m : myMaterial);

	}

}
