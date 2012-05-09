package net.sf.openrocket.database;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import net.sf.openrocket.file.Loader;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.xml.OpenRocketComponentSaver;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

public class ComponentPresetDatabase extends Database<ComponentPreset> implements ComponentPresetDao {

	private static final LogHelper log = Application.getLogger();

	public static class ComponentPresetLoader implements Loader<ComponentPreset> {

		@Override
		public Collection<ComponentPreset> load(InputStream stream,	String filename) {

			log.debug("Loading presets from file " + filename);

			Set<String> favorites = Application.getPreferences().getComponentFavorites();

			try {
				List<ComponentPreset> presets;
				presets = new OpenRocketComponentSaver().unmarshalFromOpenRocketComponent( new InputStreamReader (stream));
				for( ComponentPreset preset : presets ) {
					if ( favorites.contains(preset.preferenceKey())) {
						preset.setFavorite(true);
					}
				}				
				log.debug("ComponentPreset file " + filename + " contained " + presets.size() + " presets");
				return presets;
			} catch (JAXBException e) {
				throw new BugException("Unable to parser file: "+ filename, e);
			} catch (InvalidComponentPresetException e) {
				throw new BugException("Unable to parser file: "+ filename, e);
			}

		}

	}

	public ComponentPresetDatabase() {
		super(new ComponentPresetLoader());
	}

	@Override
	public List<ComponentPreset> listAll() {
		return list;
	}

	@Override
	public void insert( ComponentPreset preset ) {
		list.add(preset);
	}

	@Override
	public List<ComponentPreset> listForType( ComponentPreset.Type type ) {
		if ( type == null ) {
			return Collections.<ComponentPreset>emptyList();
		}

		List<ComponentPreset> result = new ArrayList<ComponentPreset>(list.size()/6);

		for( ComponentPreset preset : list ) {
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
	@Override
	public List<ComponentPreset> listForType( ComponentPreset.Type type, boolean favorite ) {

		if ( !favorite ) {
			return listForType(type);
		}

		List<ComponentPreset> result = new ArrayList<ComponentPreset>(list.size()/6);

		for( ComponentPreset preset : list ) {
			if ( preset.isFavorite() && preset.get(ComponentPreset.TYPE).equals(type) ) {
				result.add(preset);
			}
		}
		return result;
	}

	@Override
	public List<ComponentPreset> listForTypes( ComponentPreset.Type ... type ) {

		if( type == null || type.length == 0 ) {
			return Collections.<ComponentPreset>emptyList();
		}

		if (type.length == 1 ) {
			return listForType(type[0]);
		}

		List<ComponentPreset> result = new ArrayList<ComponentPreset>(list.size()/6);

		for( ComponentPreset preset : list ) {
			ComponentPreset.Type presetType = preset.get(ComponentPreset.TYPE);
			typeLoop: for( int i=0; i<type.length; i++ ) {
				if ( presetType.equals(type[i]) ) {
					result.add(preset);
					break typeLoop; // from inner loop.
				}
			}

		}
		return result;
	}

	@Override
	public List<ComponentPreset> listForTypes( List<ComponentPreset.Type> types ) {
		return listForTypes( (ComponentPreset.Type[]) types.toArray() );
	}

	@Override
	public List<ComponentPreset> find(String manufacturer, String partNo) {
		List<ComponentPreset> presets = new ArrayList<ComponentPreset>();
		for( ComponentPreset preset : list ) {
			if ( preset.getManufacturer().getSimpleName().equals(manufacturer) && preset.getPartNo().equals(partNo) ) {
				presets.add(preset);
			}
		}
		return presets;
	}

	@Override
	public void setFavorite( ComponentPreset preset, boolean favorite ) {
		preset.setFavorite(favorite);
		Application.getPreferences().setComponentFavorite( preset, favorite );
		this.fireAddEvent(preset);
	}

}
