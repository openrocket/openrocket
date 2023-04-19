package net.sf.openrocket.database;

import java.util.List;

import net.sf.openrocket.preset.ComponentPreset;

public interface ComponentPresetDao {

	/**
	 * return a list all components
	 * @return	list of all components
	 */
	public List<ComponentPreset> listAll();
	
	/**
	 * insert a component preset into a database
	 * @param preset	the component to be inserted into the database
	 */
	public void insert( ComponentPreset preset );

	/**
	 * return all components preset matching the given type
	 * @param type	the searched type
	 * @return		the list of components matching the type
	 */
	public List<ComponentPreset> listForType( ComponentPreset.Type type );

	/**
	 * Return a list of component presets based on the type.
	 * All components returned will be of Type type.
	 * 
	 * @param type  
	 * @param favorite if true, only return the favorites.  otherwise return all matching.
	 * @return
	 */
	public List<ComponentPreset> listForType( ComponentPreset.Type type, boolean favorite );
	
	/**
	 * Returns a list of components presets of multiple types
	 * @param type	the types to be searched for
	 * @return
	 */
	public List<ComponentPreset> listForTypes( ComponentPreset.Type ... type );
	
	/**
	 * Returns a list of components preset of each type in the list
	 * @param types the list of types to be searched for
	 * @return
	 */
	public List<ComponentPreset> listForTypes( List<ComponentPreset.Type> types );

	/**
	 * set or reset a component preset as favorite
	 * @param preset	the preset to be set as favorite
	 * @param type		the type of the preset
	 * @param favorite	true to set, false to reset as favorite
	 */
	public void setFavorite( ComponentPreset preset, ComponentPreset.Type type, boolean favorite );
	
	/**
	 * returns a list of components preset based on manufacturer and part number
	 * @param manufacturer	the manufacturer to be searched for
	 * @param partNo		the part number of the component
	 * @return	the resulting list of the search
	 */
	public List<ComponentPreset> find( String manufacturer, String partNo );
	
}