package net.sf.openrocket.gui.main;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.document.StorageOptions.FileType;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class SaveAsFileChooser extends JFileChooser {

	private final FileType type;
	private final OpenRocketDocument document;
	private final StorageOptionChooser storageChooser;

	private static final Translator trans = Application.getTranslator();

	public static SaveAsFileChooser build( OpenRocketDocument document, FileType type ) {
		return new SaveAsFileChooser(document,type);
	}

	private SaveAsFileChooser( OpenRocketDocument document, FileType type ) {
		this.document = document;
		this.type = type;

		this.setAcceptAllFileFilterUsed(true);

		File defaultFilename = document.getFile();
		
		switch( type ) {
		default:
		case OPENROCKET:
			defaultFilename = FileHelper.forceExtension(defaultFilename,"ork");
			this.setDialogTitle(trans.get("saveAs.openrocket.title"));
			storageChooser = new StorageOptionChooser(document, document.getDefaultStorageOptions());
			this.setAccessory(storageChooser);
			this.addChoosableFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
			this.setFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
			break;
		case ROCKSIM:
			defaultFilename = FileHelper.forceExtension(defaultFilename,"rkt");
			this.setDialogTitle(trans.get("saveAs.rocksim.title"));
			storageChooser = null;
			this.addChoosableFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);
			this.setFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);
			break;
		}
		
		final RememberFilenamePropertyListener listner = new RememberFilenamePropertyListener();
		this.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, listner);
		this.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, listner);
		this.addPropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, listner);
		
		this.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

		if (defaultFilename != null) {
			this.setSelectedFile(defaultFilename);
		}
	}
	
	public void storeOptions(StorageOptions opts) {
		if ( storageChooser != null ) {
			storageChooser.storeOptions(opts);
		}
	}
	
}

class RememberFilenamePropertyListener implements PropertyChangeListener {
	private String oldFileName=null;

	@Override
	public void propertyChange(PropertyChangeEvent event){
		if( JFileChooser.SELECTED_FILE_CHANGED_PROPERTY == event.getPropertyName()){
			if(null != event.getOldValue()){
				this.oldFileName = ((File)event.getOldValue()).getName();
			}
			return;
		}else if(JFileChooser.FILE_FILTER_CHANGED_PROPERTY == event.getPropertyName()){
			JFileChooser chooser = (JFileChooser)event.getSource();	
			SimpleFileFilter filter = (SimpleFileFilter)(chooser.getFileFilter());
			String desiredExtension = filter.getExtensions()[0];

			if( null == this.oldFileName){
				return;
			}
			String thisFileName = this.oldFileName;

			if ( filter.accept( new File(thisFileName))){
				// nop 
				return;
			}else{
				String[] splitResults = thisFileName.split("\\.");
				if(0 < splitResults.length){
					thisFileName = splitResults[0];
				}
				chooser.setSelectedFile(new File( thisFileName+desiredExtension));
				return;
			}
		}
	}

}


