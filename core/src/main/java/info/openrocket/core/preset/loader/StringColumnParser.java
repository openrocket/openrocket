package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.preset.TypedPropertyMap;

public class StringColumnParser extends BaseColumnParser {

	private final TypedKey<String> propKey;

	public StringColumnParser(String columnHeader, TypedKey<String> propKey) {
		super(columnHeader);
		this.propKey = propKey;
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {
		props.put(propKey, columnData);
	}

}
