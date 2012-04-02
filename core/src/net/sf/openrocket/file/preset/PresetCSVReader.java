package net.sf.openrocket.file.preset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;
import au.com.bytecode.opencsv.CSVReader;

public class PresetCSVReader {

	private InputStream is;
	private ColumnDefinition<?>[] columns;

	public PresetCSVReader(InputStream is) {
		this.is = is;
	}

	public List<TypedPropertyMap> parse() throws IOException {

		List<TypedPropertyMap> templates = new ArrayList<TypedPropertyMap>();

		InputStreamReader r = new InputStreamReader(is);

		// Create the CSV reader.  Use comma separator and double-quote escaping.
		CSVReader reader = new CSVReader(r, ',', '"');

		String[] headers = reader.readNext();
		if (headers == null || headers.length == 0) {
			return templates;
		}

		columns = new ColumnDefinition[headers.length];
		for (int i = 0; i < headers.length; i++) {
			String h = headers[i];
			TypedKey key = ComponentPreset.keyMap.get(h);
			if (key == null) {
				throw new RuntimeException("Invalid parameter key " + h + " in file");
			}
			columns[i] = new ColumnDefinition(key);
		}

		String[] line;
		while ((line = reader.readNext()) != null) {
			TypedPropertyMap preset = new TypedPropertyMap();
			for (int i = 0; i < headers.length; i++) {
				if (i > line.length) {
					break;
				}
				String value = line[i];
				if ( value == null ) {
					continue;
				}
				value = value.trim();
				if ( value.length() == 0 ) {
					continue;
				}
				columns[i].setProperty(preset, value);
			}
			templates.add(preset);
		}

		return templates;
	}

}
