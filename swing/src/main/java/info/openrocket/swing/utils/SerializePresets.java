package info.openrocket.swing.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.file.iterator.DirectoryIterator;
import info.openrocket.core.file.iterator.FileIterator;
import info.openrocket.core.gui.util.SimpleFileFilter;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.xml.OpenRocketComponentLoader;
import info.openrocket.core.util.Pair;

public class SerializePresets extends BasicApplication {

    private static void printUsage() {
        System.err.println("SerializePresets <dir> ... ");
        System.err.println("<dir> (may be repeated) is base directory for a set of .orc preset files");
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		SerializePresets app = new SerializePresets();
		app.initializeApplication();
		
		if (args.length < 1) {
			printUsage();
            throw new IllegalArgumentException("Invalid Command Line Params");
        }
		
		Locale.setDefault(Locale.ENGLISH);
		
		ComponentPresetDatabase componentPresetDao = new ComponentPresetDatabase();

		for (int i = 0; i < args.length; i++) {

			System.err.println("Processing .orc files in directory " + args[i]);
			
			FileIterator iterator = DirectoryIterator.findDirectory(args[i], new SimpleFileFilter("", false, "orc"));
			if (iterator == null) {
				throw new RuntimeException("Can't find " + args[i] + " directory");
			}
			
			while (iterator.hasNext()) {
				Pair<File, InputStream> f = iterator.next();
				String fileName = f.getU().getName();
				InputStream is = f.getV();
				
				OpenRocketComponentLoader loader = new OpenRocketComponentLoader();
				Collection<ComponentPreset> presets = loader.load(is, fileName);
				
				componentPresetDao.addAll(presets);
				
			}

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
