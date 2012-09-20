package net.sf.openrocket.preset.loader;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.util.StringUtil;

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

		if (StringUtil.isEmpty(columnData)) {
			return;
		}

		Material.Surface myMaterial = (Material.Surface) Databases.findMaterial(Material.Type.SURFACE, columnData, 0.0);
		Material.Surface m = materialMap.getSurfaceMaterial(myMaterial, null);
		props.put(param, m != null ? m : myMaterial);

	}

}
