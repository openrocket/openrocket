package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;

public class IntegerColumnParser extends BaseColumnParser {

	private TypedKey<Integer> propKey;
	
	public IntegerColumnParser(String columnHeader, TypedKey<Integer> propKey) {
		super(columnHeader);
		this.propKey = propKey;
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {
		int value = Integer.valueOf(columnData);
		props.put(propKey, value);
	}

}
