package net.sf.openrocket.file.preset;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import au.com.bytecode.opencsv.CSVReader;

public class PresetCSVReader {
	
	private InputStream is;
	private ColumnDefinition[] columns;
	
	public PresetCSVReader(InputStream is) {
		this.is = is;
	}
	
	public List<ComponentPreset> parse() throws IOException {
		
		List<ComponentPreset> templates = new ArrayList<ComponentPreset>();
		
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
			if ("Manufacturer".equals(h)) {
				columns[i] = new ColumnDefinition.Manufactuer();
			} else if ("PartNo".equals(h)) {
				columns[i] = new ColumnDefinition.PartNumber();
			} else if ("Type".equals(h)) {
				columns[i] = new ColumnDefinition.Type();
			} else {
				TypedKey key = ComponentPreset.keyMap.get(h);
				if (key == null) {
					throw new RuntimeException("Invalid parameter key " + h + " in file");
				}
				columns[i] = new ColumnDefinition.Parameter(key);
			}
		}
		
		String[] line;
		while ((line = reader.readNext()) != null) {
			ComponentPreset preset = new ComponentPreset();
			for (int i = 0; i < headers.length; i++) {
				if (i > line.length) {
					break;
				}
				String value = line[i];
				columns[i].setProperty(preset, value);
			}
			templates.add(preset);
		}
		
		return templates;
	}
	
}
