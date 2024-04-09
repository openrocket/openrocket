package info.openrocket.swing.gui.main;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.StorageOptions.FileType;
import info.openrocket.core.file.wavefrontobj.export.OBJExportOptions;
import info.openrocket.core.gui.util.SimpleFileFilter;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;
import info.openrocket.core.util.FileUtils;

import info.openrocket.swing.file.wavefrontobj.OBJOptionChooser;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.SaveFileChooser;

public class DesignFileSaveAsFileChooser extends SaveFileChooser {

	private final FileType type;
	private final OpenRocketDocument document;

	private static final Translator trans = Application.getTranslator();
	private static final Preferences prefs = Application.getPreferences();

	public static DesignFileSaveAsFileChooser build(OpenRocketDocument document, FileType type) {
		return new DesignFileSaveAsFileChooser(document, type, null);
	}

	public static DesignFileSaveAsFileChooser build(OpenRocketDocument document, FileType type, List<RocketComponent> selectedComponents) {
		return new DesignFileSaveAsFileChooser(document, type, selectedComponents);
	}

	private DesignFileSaveAsFileChooser(OpenRocketDocument document, FileType type, List<RocketComponent> selectedComponents) {
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
			case WAVEFRONT_OBJ:
				defaultFilename = FileHelper.forceExtension(defaultFilename,"obj");
				this.setDialogTitle(trans.get("saveAs.wavefront.title"));
				OBJExportOptions initialOptions = prefs.loadOBJExportOptions(document.getRocket());
				OBJOptionChooser objChooser = new OBJOptionChooser(this, initialOptions, selectedComponents, document.getRocket());
				this.setAccessory(objChooser);
				this.addChoosableFileFilter(FileHelper.WAVEFRONT_OBJ_FILTER);
				this.setFileFilter(FileHelper.WAVEFRONT_OBJ_FILTER);

				// TODO: update this dynamically instead of hard-coded values
				//   The macOS file chooser has an issue where it does not update its size when the accessory is added.
				if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS && UITheme.isLightTheme(GUIUtil.getUITheme())) {
					Dimension currentSize = this.getPreferredSize();
					Dimension newSize = new Dimension((int) (1.35 * currentSize.width), (int) (1.5 * currentSize.height));
					this.setPreferredSize(newSize);
				}

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
				currentFileName = FileUtils.removeExtension(currentFileName);
				chooser.setSelectedFile(new File(currentFileName + desiredExtension));
			}
		}
	}
}


