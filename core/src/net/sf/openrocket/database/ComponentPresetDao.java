package net.sf.openrocket.database;

import java.util.List;

import net.sf.openrocket.preset.ComponentPreset;

public interface ComponentPresetDao {

	public List<ComponentPreset> listAll();
	
	public void insert( ComponentPreset preset );

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

	public List<ComponentPreset> listForTypes( ComponentPreset.Type ... type );
	
	public List<ComponentPreset> listForTypes( List<ComponentPreset.Type> types );

	public void setFavorite( ComponentPreset preset, ComponentPreset.Type type, boolean favorite );
	
	public List<ComponentPreset> find( String manufacturer, String partNo );
	
}