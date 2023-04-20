package net.sf.openrocket.gui.main;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.document.StorageOptions.FileType;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.gui.widgets.SaveFileChooser;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class DesignFileSaveAsFileChooser extends SaveFileChooser {

	private final FileType type;
	private final OpenRocketDocument document;

	private static final Translator trans = Application.getTranslator();

	public static DesignFileSaveAsFileChooser build(OpenRocketDocument document, FileType type ) {
		return new DesignFileSaveAsFileChooser(document,type);
	}

	private DesignFileSaveAsFileChooser(OpenRocketDocument document, FileType type ) {
		this.document = document;
		this.type = type;

		this.setAcceptAllFileFilterUsed(false);

		File defaultFilename = document.getFileNoExtension();
		
		switch( type ) {
			default:
			case OPENROCKET:
				defaultFilename = FileHelper.forceExtension(defaultFilename,"ork");
				this.setDialogTitle(trans.get("saveAs.openrocket.title"));
				StorageOptionChooser storageChooser = new StorageOptionChooser(document, document.getDefaultStorageOptions());
				this.setAccessory(storageChooser);
				this.addChoosableFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
				this.setFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
				break;
			case ROCKSIM:
				defaultFilename = FileHelper.forceExtension(defaultFilename,"rkt");
				this.setDialogTitle(trans.get("saveAs.rocksim.title"));
				this.addChoosableFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);
				this.setFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);
				break;
			case RASAERO:
				defaultFilename = FileHelper.forceExtension(defaultFilename,"CDX1");
				this.setDialogTitle(trans.get("saveAs.rasaero.title"));
				this.addChoosableFileFilter(FileHelper.RASAERO_DESIGN_FILTER);
				this.setFileFilter(FileHelper.RASAERO_DESIGN_FILTER);
				break;
		}
		
		final RememberFilenamePropertyListener listener = new RememberFilenamePropertyListener();
		this.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, listener);
		this.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, listener);
		this.addPropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, listener);
		
		this.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

		if (defaultFilename != null) {
			this.setSelectedFile(defaultFilename);
		}
	}
}

class RememberFilenamePropertyListener implements PropertyChangeListener {
	private String oldFileName = null;

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();

		if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propertyName)) {
			handleSelectedFileChanged(event);
		} else if (JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(propertyName)) {
			handleFileFilterChanged(event);
		}
	}

	private void handleSelectedFileChanged(PropertyChangeEvent event) {
		if (event.getOldValue() != null) {
			oldFileName = ((File) event.getOldValue()).getName();
		}
	}

	private void handleFileFilterChanged(PropertyChangeEvent event) {
		JFileChooser chooser = (JFileChooser) event.getSource();
		FileFilter currentFilter = chooser.getFileFilter();

		if (currentFilter instanceof SimpleFileFilter) {
			SimpleFileFilter filter = (SimpleFileFilter) currentFilter;
			String desiredExtension = filter.getExtensions()[0];

			if (oldFileName == null) {
				return;
			}

			String currentFileName = oldFileName;

			if (!filter.accept(new File(currentFileName))) {
				currentFileName = removeExtension(currentFileName);
				chooser.setSelectedFile(new File(currentFileName + desiredExtension));
			}
		}
	}

	private String removeExtension(String fileName) {
		String[] splitResults = fileName.split("\\.");
		if (splitResults.length > 0) {
			return splitResults[0];
		}
		return fileName;
	}
}


