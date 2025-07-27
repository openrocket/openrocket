package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.preset.TypedPropertyMap;

public class IntegerColumnParser extends BaseColumnParser {

	private final TypedKey<Integer> propKey;

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
