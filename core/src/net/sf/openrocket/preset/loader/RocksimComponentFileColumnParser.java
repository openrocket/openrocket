package net.sf.openrocket.preset.loader;

import net.sf.openrocket.preset.TypedPropertyMap;

public interface RocksimComponentFileColumnParser {

	/**
	 * Examine the array of column headers and configure parsing for this type. 
	 * 
	 * @param headers
	 */
	public void configure( String[] headers );
	
	/**
	 * Examine the data array, parse the appropriate data and push into props.
	 * 
	 * @param data
	 * @param props
	 */
	public void parse( String[] data, TypedPropertyMap props );
	
}
