package info.openrocket.core.preset.loader;

import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.preset.TypedPropertyMap;

public class DoubleColumnParser extends BaseColumnParser {

	private final TypedKey<Double> propKey;

	public DoubleColumnParser(String columnHeader, TypedKey<Double> propKey) {
		super(columnHeader);
		this.propKey = propKey;
	}

	@Override
	protected void doParse(String columnData, String[] data, TypedPropertyMap props) {
		double value = Double.valueOf(columnData);
		props.put(propKey, value);
	}

}
