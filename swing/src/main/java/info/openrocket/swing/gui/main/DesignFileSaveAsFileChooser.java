package info.openrocket.swing.gui.main;

import java.io.File;
import java.util.List;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.SaveSimulationDataMode;
import info.openrocket.core.document.StorageOptions.FileType;
import info.openrocket.core.file.wavefrontobj.export.OBJExportOptions;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.preferences.ApplicationPreferences;

import info.openrocket.swing.gui.choosers.OBJOptionChooser;
import info.openrocket.swing.gui.choosers.StorageOptionChooser;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.widgets.SaveFileChooser;

public class DesignFileSaveAsFileChooser extends SaveFileChooser {

	private final FileType type;
	private final OpenRocketDocument document;

	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

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
				// Without a fixed choice in the preferences, ask which simulated data to store
				if (prefs.getSaveSimulationDataMode() == SaveSimulationDataMode.ASK) {
					this.setOptionsPanel(new StorageOptionChooser(document, document.getDefaultStorageOptions()));
				}
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
				OBJOptionChooser objChooser = new OBJOptionChooser(initialOptions, selectedComponents, document.getRocket());
				this.setOptionsPanel(objChooser);
				this.addChoosableFileFilter(FileHelper.WAVEFRONT_OBJ_FILTER);
				this.setFileFilter(FileHelper.WAVEFRONT_OBJ_FILTER);
				break;
		}
		
		this.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());

		if (defaultFilename != null) {
			this.setSelectedFile(defaultFilename);
		}
	}
}
