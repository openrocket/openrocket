package info.openrocket.core.database;

import java.awt.Dialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collection;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import info.openrocket.core.file.iterator.DirectoryIterator;
import info.openrocket.core.file.iterator.FileIterator;
import info.openrocket.core.gui.util.SimpleFileFilter;
import info.openrocket.core.l10n.Translator;
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
			if (file.isFile() && orcFilter.accept(file)) {
				loadUserFile(file);
			} else if (file.isDirectory()) {
				loadUserDirectory(orcFilter, file);
			} else {
				log.warn("User-defined component preset file " + file + " is neither file nor directory");
			}
		}
		log.info("Ending reading user-defined component presets, presetCount=" + (presetCount-initialCount));
	}

	/**
	 * Loads a user-defined component preset file.
	 * Shows a warning dialog if the file fails to load.
	 *
	 * @param file the file to load
	 */
	private void loadUserFile(File file) {
		try {
			InputStream stream = new FileInputStream(file);
			Collection<ComponentPreset> presets = loadFile(file.getName(), stream);
			componentPresetDao.addAll(presets);
			fileCount++;
			presetCount += presets.size();
		} catch (IOException e) {
			log.warn("Error loading user-defined component preset file " + file, e);
			showLoadingErrorDialog(file.getPath(), e.getMessage());
		}
	}

	/**
	 * Loads user-defined component presets from a directory.
	 *
	 * @param fileFilter the supported extensions of files
	 * @param file       the directory file object
	 */
	private void loadUserDirectory(SimpleFileFilter fileFilter, File file) {
		FileIterator iterator;
		try {
			iterator = new DirectoryIterator(file, fileFilter, true);
		} catch (IOException ioex) {
			log.debug("Error opening UserComponentDirectory", ioex);
			return;
		}
		while (iterator.hasNext()) {
			Pair<File, InputStream> f = iterator.next();
			try {
				Collection<ComponentPreset> presets = loadFile(f.getU().getName(), f.getV());
				componentPresetDao.addAll(presets);
				fileCount++;
				presetCount += presets.size();
			} catch (IOException e) {
				log.warn("Error loading user-defined component preset file " + f.getU(), e);
				showLoadingErrorDialog(f.getU().getPath(), e.getMessage());
			}
		}
	}

	/**
	 * Shows a warning dialog when a component preset file fails to load.
	 *
	 * @param filePath     the path of the file that failed to load
	 * @param errorMessage the error message from the exception
	 */
	private void showLoadingErrorDialog(String filePath, String errorMessage) {
		Translator trans = Application.getTranslator();
		String message = "<html><body><p style='width: 400px;'><i>" + errorMessage +
				"</i>.<br><br>" + MessageFormat.format(trans.get("ComponentDbLoaderDlg.message1"), filePath) +
				"<br>" + trans.get("ComponentDbLoaderDlg.message2") + "</p></body></html>";
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE);
				JDialog dialog = pane.createDialog(null, trans.get("ComponentDbLoaderDlg.title"));
				dialog.setModalityType(Dialog.ModalityType.MODELESS);
				dialog.setAlwaysOnTop(true);
				dialog.setVisible(true);
			}
		});
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
			try {
				Collection<ComponentPreset> presets = loadFile(f.getU().getName(), f.getV());
				componentPresetDao.addAll(presets);
				fileCount++;
				presetCount += presets.size();
			} catch (IOException e) {
				log.error("Error loading system component preset file " + f.getU(), e);
			}
		}
	}
	
	/**
	 * load components from a custom component file
	 * uses an OpenRocketComponentLoader for the job
	 * 
	 * @param fileName	name of the file to be loaded
	 * @param stream	the input stream to the file
	 * @return	a collection of component presets from the file
	 * @throws IOException if the file cannot be parsed
	 */
	private Collection<ComponentPreset> loadFile(String fileName, InputStream stream) throws IOException {
		log.debug("loading from file: " + fileName);
		OpenRocketComponentLoader loader = new OpenRocketComponentLoader();
		return loader.load(stream, fileName);
	}
}
