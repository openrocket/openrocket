package info.openrocket.core.preset.loader;

import java.util.Locale;

import info.openrocket.core.preset.TypedPropertyMap;

public abstract class BaseColumnParser implements RockSimComponentFileColumnParser {

	protected String columnHeader;
	protected boolean isConfigured = false;
	protected int columnIndex;

	public BaseColumnParser(String columnHeader) {
		super();
		this.columnHeader = columnHeader.toLowerCase(Locale.US);
	}

	@Override
	public void configure(String[] headers) {
		if (headers == null) {
			return;
		}
		for (int i = 0; i < headers.length; i++) {
			if (columnHeader.equals(headers[i].toLowerCase(Locale.US))) {
				columnIndex = i;
				isConfigured = true;
				return;
			}
		}
	}

	@Override
	final public void parse(String[] data, TypedPropertyMap props) {
		if (isConfigured) {
			doParse(data[columnIndex], data, props);
		}
	}

	protected abstract void doParse(String columnData, String[] data, TypedPropertyMap props);

}
