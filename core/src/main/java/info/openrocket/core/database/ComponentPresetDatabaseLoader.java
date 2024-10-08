package info.openrocket.core.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import info.openrocket.core.file.iterator.DirectoryIterator;
import info.openrocket.core.file.iterator.FileIterator;
import info.openrocket.core.gui.util.SimpleFileFilter;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.xml.OpenRocketComponentLoader;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.Pair;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Loader that gets all component preset from the database in subdirectories of datafiles/components
 *
 */
public class ComponentPresetDatabaseLoader extends AsynchronousDatabaseLoader {
	
	private final static Logger log = LoggerFactory.getLogger(ComponentPresetDatabaseLoader.class);
	
	private static final String SYSTEM_PRESET_DIR = "datafiles/components";
	private int fileCount = 0;
	private int presetCount = 0;

	/** the database is immutable*/
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
		loadPresetComponents();
		loadUserComponents();
		long end = System.currentTimeMillis();
		log.debug("Time to load presets: " + (end - startTime) + "ms " + presetCount + " loaded from " + fileCount + " files");
		
	}

	/**
	 * loads the user defined component presets into the database
	 * uses the directory defined in the preferences
	 */
	private void loadUserComponents() {
		log.info("Starting reading user-defined component presets");
		SimpleFileFilter orcFilter = new SimpleFileFilter("", false, "orc");
		int initialCount = presetCount;
		for (File file : (Application.getPreferences()).getUserComponentPresetFiles()) {
			if (file.isFile()) {
				try {
					InputStream stream = new FileInputStream(file);
					loadFile(file.getName(), stream);
				} catch (IOException e) {
					log.warn("Error opening file " + file, e);
				}
			} else if (file.isDirectory()) {
				loadDirectory(orcFilter, file);
			} else {
				log.warn("User-defined motor file " + file + " is neither file nor directory");
			}
		}
		log.info("Ending reading user-defined component presets, presetCount=" + (presetCount-initialCount));
	}

	/**
	 * loads the default preset components into the database
	 * uses the file directory from "datafiles/components"
	 */
	private void loadPresetComponents() {
		log.info("Loading component presets from " + SYSTEM_PRESET_DIR);
		FileIterator iterator = DirectoryIterator.findDirectory(SYSTEM_PRESET_DIR, new SimpleFileFilter("", false, "orc"));
		
		if (iterator == null)
			return;

		while (iterator.hasNext()) {
			Pair<File, InputStream> f = iterator.next();
			Collection<ComponentPreset> presets = loadFile(f.getU().getName(), f.getV());
			componentPresetDao.addAll(presets);
			fileCount++;
			presetCount += presets.size();
		}
	}
	
	/**
	 * load components from a custom component file
	 * uses an OpenRocketComponentLoader for the job
	 * 
	 * @param fileName	name of the file to be 
	 * @param stream	the input stream to the file
	 * @return	a collection of components preset from the file
	 */
	private Collection<ComponentPreset> loadFile(String fileName, InputStream stream) {
		log.debug("loading from file: " + fileName);
		OpenRocketComponentLoader loader = new OpenRocketComponentLoader();
		Collection<ComponentPreset> presets = loader.load(stream, fileName);
		return presets;
	}

	/**
	 * loads an entire directory of component presets
	 *
	 * @param fileFilter	the supported extensions of files
	 * @param file			the directory file object
	 */
	private void loadDirectory(SimpleFileFilter fileFilter, File file) {
		FileIterator iterator;
		try {
			iterator = new DirectoryIterator(file, fileFilter, true);
		} catch (IOException ioex) {
			log.debug("Error opening UserComponentDirectory", ioex);
			return;
		}
		while (iterator.hasNext()) {
			Pair<File, InputStream> f = iterator.next();
			Collection<ComponentPreset> presets = loadFile(f.getU().getName(), f.getV());
			componentPresetDao.addAll(presets);
			fileCount++;
			presetCount += presets.size();
		}
	}
}
