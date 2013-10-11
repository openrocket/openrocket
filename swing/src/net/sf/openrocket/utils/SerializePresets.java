package net.sf.openrocket.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.xml.OpenRocketComponentLoader;
import net.sf.openrocket.util.Pair;

public class SerializePresets extends BasicApplication {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		SerializePresets app = new SerializePresets();
		app.initializeApplication();
		
		Locale.setDefault(Locale.ENGLISH);
		
		ComponentPresetDatabase componentPresetDao = new ComponentPresetDatabase();
		
		FileIterator iterator = DirectoryIterator.findDirectory("resources-src/datafiles/presets", new SimpleFileFilter("", false, "orc"));
		
		if (iterator == null) {
			throw new RuntimeException("Can't find resources-src/presets directory");
		}
		while (iterator.hasNext()) {
			Pair<String, InputStream> f = iterator.next();
			String fileName = f.getU();
			InputStream is = f.getV();
			
			OpenRocketComponentLoader loader = new OpenRocketComponentLoader();
			Collection<ComponentPreset> presets = loader.load(is, fileName);
			
			componentPresetDao.addAll(presets);
			
		}
		
		List<ComponentPreset> list = componentPresetDao.listAll();
		
		System.out.println("Total number of presets = " + list.size());
		
		File outFile = new File("resources/datafiles/presets", "system.ser");
		
		FileOutputStream ofs = new FileOutputStream(outFile);
		ObjectOutputStream oos = new ObjectOutputStream(ofs);
		
		oos.writeObject(list);
		
		ofs.flush();
		ofs.close();
	}
}
