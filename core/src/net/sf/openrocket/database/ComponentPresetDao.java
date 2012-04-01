package net.sf.openrocket.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.file.preset.PresetCSVReader;
import net.sf.openrocket.preset.ComponentPreset;

public class ComponentPresetDao {

	private final List<ComponentPreset> templates = new ArrayList<ComponentPreset>();

	// Package scope constructor to control creation pattern.
	ComponentPresetDao() {}
	
	void initialize() throws IOException {
		
		InputStream is = ComponentPresetDao.class.getResourceAsStream("/datafiles/bodytubepresets.csv");
		
		PresetCSVReader parser = new PresetCSVReader(is);
		List<ComponentPreset> list = parser.parse();
		for( ComponentPreset preset : list ) {
			templates.add(preset);
		}
	}
	
	public List<ComponentPreset> listAll() {
		return templates;
	}
	
}
