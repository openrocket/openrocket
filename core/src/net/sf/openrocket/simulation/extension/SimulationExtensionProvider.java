package net.sf.openrocket.simulation.extension;

import java.util.List;

import net.sf.openrocket.plugin.Plugin;

@Plugin
public interface SimulationExtensionProvider {
	
	/**
	 * Return a list of simulation extension ID's that this provider supports.
	 * The ID is used to identify the plugin when storing files.  It should follow
	 * the conventions of Java package and class naming.
	 * 
	 * @return  a list of ID strings
	 */
	public List<String> getIds();
	
	/**
	 * Return the UI name for a simulation extension.  The first values
	 * are nested menus, with the last one the actual entry, for example
	 * ["Launch conditions", "Air-start"].
	 * 
	 * If the ID does not represent an extension that should be displayed
	 * in the UI, this method must return null.  For example, if an extension
	 * has multiple ID's, this method must return the menu name for only one
	 * of the ID's.
	 * 
	 * These can be localized, and the system may attempt to localize
	 * English-language names automatically (mainly for the menus).
	 * 
	 * @param id	the extension ID
	 * @return  	the UI name for the extension, or null for no display
	 */
	public List<String> getName(String id);
	
	/**
	 * Return a new instance of a simulation extension.  This is a new instance
	 * that should have some default configuration.
	 * 
	 * @param id	the extension ID
	 * @return		a new simulation extension instance
	 */
	public SimulationExtension getInstance(String id);
	
}
