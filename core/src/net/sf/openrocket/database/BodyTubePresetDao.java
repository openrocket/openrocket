package net.sf.openrocket.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.rocketcomponent.BodyTube;
import au.com.bytecode.opencsv.CSVReader;

public class BodyTubePresetDao {

	private final List<ComponentPreset> templates = new ArrayList<ComponentPreset>();

	// Package scope constructor to control creation pattern.
	BodyTubePresetDao() {}
	
	void initialize() throws IOException {
		
		InputStream is = BodyTubePresetDao.class.getResourceAsStream("/datafiles/bodytubepresets.csv");
		InputStreamReader r = new InputStreamReader(is);
		// Create the CSV reader.  Use comma separator and double-quote escaping.  Skip first line.
		CSVReader reader = new CSVReader(r,',','"',1);
		String[] line;
		while( (line = reader.readNext()) != null ) {
			String manu = line[0];
			String prod = line[1];
			// inner diameter in centimeters
			String idString = line[2];
			double innerRadius = Double.parseDouble(idString) /100.0/2.0;
			// outer diameter in centimeters
			String odString = line[3];
			double outerRadius = Double.parseDouble(odString) /100.0/2.0;
			// length in centimeters
			String maxLength = line[4];
			double length = Double.parseDouble(maxLength) /100.0;
			BodyTube bt = new BodyTube(length, outerRadius, outerRadius - innerRadius );
			ComponentPreset preset = new ComponentPreset( manu, prod, "", bt );
			templates.add(preset);
		}
		
	}
	
	public List<ComponentPreset> listAll() {
		return templates;
	}
	
}
