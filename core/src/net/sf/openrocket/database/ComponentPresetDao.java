package net.sf.openrocket.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.file.preset.PresetCSVReader;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.util.BugException;

public class ComponentPresetDao {

	private final List<ComponentPreset> templates = new ArrayList<ComponentPreset>();

	// Package scope constructor to control creation pattern.
	public ComponentPresetDao() {}
	
	public void initialize() throws IOException {
		
		InputStream is = ComponentPresetDao.class.getResourceAsStream("/datafiles/bodytubepresets.csv");
		
		PresetCSVReader parser = new PresetCSVReader(is);
		List<TypedPropertyMap> list = parser.parse();
		for( TypedPropertyMap o : list ) {
			try {
				ComponentPreset preset = ComponentPreset.create(o);
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
	
}
