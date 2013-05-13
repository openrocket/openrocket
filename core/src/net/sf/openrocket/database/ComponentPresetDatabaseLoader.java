package net.sf.openrocket.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.List;

import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.xml.OpenRocketComponentLoader;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentPresetDatabaseLoader extends AsynchronousDatabaseLoader {
	
	private final static Logger log = LoggerFactory.getLogger(ComponentPresetDatabaseLoader.class);
	
	private static final String SYSTEM_PRESET_DIR = "datafiles/presets";
	private int fileCount = 0;
	private int presetCount = 0;
	
	private final ComponentPresetDatabase componentPresetDao = new ComponentPresetDatabase();
	
	public ComponentPresetDatabaseLoader() {
		super(0);
	}
	
	/**
	 * Returns the loaded database.  If the database has not fully loaded,
	 * this blocks until it is.
	 * 
	 * @return	the motor database
	 */
	public ComponentPresetDatabase getDatabase() {
		blockUntilLoaded();
		return componentPresetDao;
	}
	
	@Override
	protected void loadDatabase() {
		long startTime = System.currentTimeMillis();
		
		log.info("Loading component presets from " + SYSTEM_PRESET_DIR);
		
		FileIterator iterator = DirectoryIterator.findDirectory(SYSTEM_PRESET_DIR, new SimpleFileFilter("", false, "ser"));
		
		if (iterator != null) {
			while (iterator.hasNext()) {
				Pair<String, InputStream> f = iterator.next();
				try {
					ObjectInputStream ois = new ObjectInputStream(f.getV());
					List<ComponentPreset> list = (List<ComponentPreset>) ois.readObject();
					componentPresetDao.addAll(list);
					fileCount++;
					presetCount += list.size();
				} catch (Exception ex) {
					throw new BugException(ex);
				}
			}
		}
		
		SimpleFileFilter orcFilter = new SimpleFileFilter("", false, "orc");
		try {
			iterator = new DirectoryIterator(
					((SwingPreferences) Application.getPreferences()).getDefaultUserComponentDirectory(),
					orcFilter,
					true);
		} catch (IOException ioex) {
			iterator = null;
			log.debug("Error opening UserComponentDirectory", ioex);
		}
		if (iterator != null) {
			while (iterator.hasNext()) {
				Pair<String, InputStream> f = iterator.next();
				Collection<ComponentPreset> presets = loadFile(f.getU(), f.getV());
				componentPresetDao.addAll(presets);
				fileCount++;
				presetCount += presets.size();
			}
		}
		
		long end = System.currentTimeMillis();
		log.debug("Time to load presets: " + (end - startTime) + "ms " + presetCount + " loaded from " + fileCount + " files");
		
	}
	
	private Collection<ComponentPreset> loadFile(String fileName, InputStream stream) {
		log.debug("loading from file: " + fileName);
		OpenRocketComponentLoader loader = new OpenRocketComponentLoader();
		Collection<ComponentPreset> presets = loader.load(stream, fileName);
		return presets;
		
	}
}
