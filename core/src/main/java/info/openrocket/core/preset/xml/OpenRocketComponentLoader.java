package info.openrocket.core.preset.xml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import jakarta.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.file.Loader;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.InvalidComponentPresetException;

public class OpenRocketComponentLoader implements Loader<ComponentPreset> {

	private static final Logger log = LoggerFactory.getLogger(OpenRocketComponentLoader.class);

	@Override
	public Collection<ComponentPreset> load(InputStream stream, String filename) throws IOException {

		log.debug("Loading presets from file " + filename);

		if (!(stream instanceof BufferedInputStream)) {
			stream = new BufferedInputStream(stream);
		}

		try {
			OpenRocketComponentDTO dto = new OpenRocketComponentSaver()
					.unmarshalFromOpenRocketComponent(new InputStreamReader(stream, StandardCharsets.UTF_8));
			if (dto == null) {
				throw new IOException("Unable to parse component preset file: " + filename + 
						" (invalid format or corrupted file)");
			}
			List<ComponentPreset> presets = dto.asComponentPresets();
			log.debug("ComponentPreset file " + filename + " contained " + presets.size() + " presets");
			return presets;
		} catch (JAXBException | InvalidComponentPresetException e) {
			throw new IOException("Unable to parse component preset file: " + filename, e);
		}

	}

}
