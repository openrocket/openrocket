package net.sf.openrocket.preset.xml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import net.sf.openrocket.file.Loader;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;

public class OpenRocketComponentLoader  implements Loader<ComponentPreset> {

	private static final LogHelper log = Application.getLogger();

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
			throw new BugException("Unable to parse file: "+ filename, e);
		} catch (InvalidComponentPresetException e) {
			throw new BugException("Unable to parse file: "+ filename, e);
		}

	}

}
