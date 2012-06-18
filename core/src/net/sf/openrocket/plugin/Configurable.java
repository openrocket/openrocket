package net.sf.openrocket.plugin;

public interface Configurable {
	
	
	/**
	 * Return the plugin ID.  This is a text string uniquely identifying this plugin.
	 * The recommended format is similar to the fully-qualified class name of the
	 * plugin, though a shorter format starting with the developer's domain name
	 * is also possible for future compatibility.
	 * 
	 * @return	the plugin ID
	 */
	public String getPluginID();
	
	/**
	 * Test whether this plugin provides functionality corresponding to the specified
	 * plugin ID.  This provides backwards compatibility if the plugin ID should change.
	 * 
	 * @param pluginID	the plugin ID to test
	 * @return			whether this plugin provides the requested functionality
	 */
	public boolean isCompatible(String pluginID);
	
	public void loadFromXML(Object... objects);
	
	public void saveToXML(Object... objects);
	
}
