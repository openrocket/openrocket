package info.openrocket.core.preset.loader;

import info.openrocket.core.database.Databases;
import info.openrocket.core.material.Material;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.preset.TypedPropertyMap;
import info.openrocket.core.util.StringUtils;

public class LineMaterialColumnParser extends BaseColumnParser {

	private final MaterialHolder materialMap;

	private final TypedKey<Material> param;

	public LineMaterialColumnParser(MaterialHolder materialMap, String columnName, TypedKey<Material> param) {
		super(columnName);
		this.param = param;
		this.materialMap = materialMap;
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {

		if (StringUtils.isEmpty(columnData)) {
			return;
		}

		Material.Line myMaterial = (Material.Line) Databases.findMaterial(Material.Type.LINE, columnData, 0.0);

		Material.Line m = materialMap.getLineMaterial(myMaterial);
		props.put(param, m != null ? m : myMaterial);

	}

}
