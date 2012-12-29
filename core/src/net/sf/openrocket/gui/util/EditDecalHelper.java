package net.sf.openrocket.gui.util;

import java.awt.Desktop;
import java.awt.Window;
import java.io.File;
import java.io.IOException;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.dialogs.EditDecalDialog;
import net.sf.openrocket.startup.Application;

public class EditDecalHelper {

	// FIXME - need to have a specific set of localizable exceptions come out of this instead of generic IOException;
	// perhaps - unable to create file,
	// unable to open system editor
	// unable to fork process

	private static final SwingPreferences prefs = ((SwingPreferences)Application.getPreferences());
	
	public static void editDecal( Window parent, OpenRocketDocument document, String decalId ) throws IOException {

		// Create Temp File.
		int dotlocation = decalId.lastIndexOf('.');
		String extension = "tmp";
		if ( dotlocation > 0 && dotlocation < decalId.length() ) {
			extension = decalId.substring(dotlocation);
		}
		File tmpFile = File.createTempFile("OR_graphics", extension);
		
		document.getDecalRegistry().exportDecal(decalId, tmpFile);
		
		//First Check preferences
		if ( prefs.isDecalEditorPreferenceSet() ) {
			
			// FIXME - need this one or all dialog.
			
			if ( prefs.isDecalEditorPreferenceSystem() ) {
				launchSystemEditor( tmpFile );
			} else {
				String commandTemplate = prefs.getDecalEditorCommandLine();
				launchCommandEditor(commandTemplate, tmpFile);
			}
			return;
		}
		
		// Preference not set, launch dialog
		EditDecalDialog dialog = new EditDecalDialog(parent);
		dialog.setVisible(true);
		
		if( dialog.isCancel() ) {
			// FIXME - delete tmpfile?
			return;
		}
		
		boolean saveToPrefs = dialog.isSavePreferences();
		
		if ( dialog.isUseSystemEditor() ) {
			if ( saveToPrefs ) {
				prefs.setDecalEditorPreference(true, null);
			}
			launchSystemEditor( tmpFile );
		} else {
			String commandLine = dialog.getCommandLine();
			if( saveToPrefs ) {
				prefs.setDecalEditorPreference(false, commandLine);
			}
			launchCommandEditor( commandLine, tmpFile );
		}
		
	}
	
	private static void launchSystemEditor( File tmpFile ) throws IOException {
		
		Desktop.getDesktop().edit(tmpFile);
		
	}
	
	private static void launchCommandEditor( String commandTemplate, File tmpFile ) throws IOException {
		
		String filename = tmpFile.getAbsolutePath();
		
		String command;
		if( commandTemplate.contains("%%")) {
			command = commandTemplate.replace("%%", filename);
		} else {
			command = commandTemplate + " " + filename;
		}
		
		Runtime.getRuntime().exec(command);
		
	}
	
}
