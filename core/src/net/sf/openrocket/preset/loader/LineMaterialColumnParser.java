package net.sf.openrocket.preset.loader;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.util.StringUtil;

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
		
		if (StringUtil.isEmpty(columnData)) {
			return;
		}
		
		Material.Line myMaterial = (Material.Line) Databases.findMaterial(Material.Type.LINE, columnData, 0.0);
		
		Material.Line m = materialMap.getLineMaterial(myMaterial);
		props.put(param, m != null ? m : myMaterial);
		
	}
	
}
