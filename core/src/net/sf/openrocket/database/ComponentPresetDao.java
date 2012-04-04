package net.sf.openrocket.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.openrocket.file.preset.PresetCSVReader;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

public class ComponentPresetDao {

	// List of all ComponentPresets
	private final List<ComponentPreset> templates = new ArrayList<ComponentPreset>();

	// Package scope constructor to control creation pattern.
	public ComponentPresetDao() {}

	public void initialize() throws IOException {

		Set<String> favorites = Application.getPreferences().getComponentFavorites();
		
		InputStream is = ComponentPresetDao.class.getResourceAsStream("/datafiles/bodytubepresets.csv");

		PresetCSVReader parser = new PresetCSVReader(is);
		List<TypedPropertyMap> list = parser.parse();
		for( TypedPropertyMap o : list ) {
			try {
				ComponentPreset preset = ComponentPreset.create(o);
				if ( favorites.contains(preset.preferenceKey())) {
					preset.setFavorite(true);
				}
				this.insert(preset);
			} catch ( InvalidComponentPresetException ex ) {
				throw new BugException( ex );
			}
		}

		
	}

	public List<ComponentPreset> listAll() {
		return templates;
	}

	public void insert( ComponentPreset preset ) {
		templates.add(preset);
	}

	public List<ComponentPreset> listForType( ComponentPreset.Type type ) {
		if ( type == null ) {
			return Collections.<ComponentPreset>emptyList();
		}
		
		List<ComponentPreset> result = new ArrayList<ComponentPreset>(templates.size()/6);

		for( ComponentPreset preset : templates ) {
			if ( preset.get(ComponentPreset.TYPE).equals(type) ) {
				result.add(preset);
			}
		}
		return result;

	}

	/**
	 * Return a list of component presets based on the type.
	 * All components returned will be of Type type.
	 * 
	 * @param type  
	 * @param favorite if true, only return the favorites.  otherwise return all matching.
	 * @return
	 */
	public List<ComponentPreset> listForType( ComponentPreset.Type type, boolean favorite ) {

		if ( !favorite ) {
			return listForType(type);
		}

		List<ComponentPreset> result = new ArrayList<ComponentPreset>(templates.size()/6);

		for( ComponentPreset preset : templates ) {
			if ( preset.isFavorite() && preset.get(ComponentPreset.TYPE).equals(type) ) {
				result.add(preset);
			}
		}
		return result;


	}

	public List<ComponentPreset> listForTypes( ComponentPreset.Type ... type ) {

		if( type == null || type.length == 0 ) {
			return Collections.<ComponentPreset>emptyList();
		}

		if (type.length == 1 ) {
			return listForType(type[0]);
		}

		List<ComponentPreset> result = new ArrayList<ComponentPreset>(templates.size()/6);

		for( ComponentPreset preset : templates ) {
			ComponentPreset.Type presetType = preset.get(ComponentPreset.TYPE);
			typeLoop: for( int i=0; i<type.length; i++ ) {
				if ( !presetType.equals(type) ) {
					result.add(preset);
					break typeLoop; // from inner loop.
				}
			}

		}
		return result;
	}

	public void setFavorite( ComponentPreset preset, boolean favorite ) {
		preset.setFavorite(favorite);
		Application.getPreferences().setComponentFavorite( preset, favorite );
	}
	
}