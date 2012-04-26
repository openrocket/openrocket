package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;

public class StringColumnParser extends BaseColumnParser {

	private TypedKey<String> propKey;
	
	public StringColumnParser(String columnHeader, TypedKey<String> propKey) {
		super(columnHeader);
		this.propKey = propKey;
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {
		props.put(propKey, columnData);
	}

}
