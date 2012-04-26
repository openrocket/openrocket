package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;

public class DoubleColumnParser extends BaseColumnParser {

	private TypedKey<Double> propKey;
	
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
