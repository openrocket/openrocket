package net.sf.openrocket.gui.util;

import java.awt.Desktop;
import java.awt.Window;
import java.io.File;
import java.io.IOException;

import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.dialogs.EditDecalDialog;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class EditDecalHelper {
	
	// FIXME - need to have a specific set of localizable exceptions come out of this instead of generic IOException;
	// perhaps - unable to create file,
	// unable to open system editor
	// unable to fork process
	
	private static final SwingPreferences prefs = ((SwingPreferences) Application.getPreferences());
	
	public static void editDecal(Window parent, OpenRocketDocument doc, RocketComponent component, DecalImage decal) throws IOException {
		
		boolean sysPrefSet = prefs.isDecalEditorPreferenceSet();
		int usageCount = doc.countDecalUsage(decal);
		
		//First Check preferences
		if (sysPrefSet && usageCount == 1) {
			
			launchEditor(prefs.isDecalEditorPreferenceSystem(), prefs.getDecalEditorCommandLine(), decal);
			return;
		}
		
		EditDecalDialog dialog = new EditDecalDialog(parent, !sysPrefSet, usageCount);
		dialog.setVisible(true);
		
		if (dialog.isCancel()) {
			return;
		}
		
		// Do we use the System Preference Editor or from the dialog?
		boolean useSystemEditor = false;
		String commandLine = "";
		
		if (sysPrefSet) {
			useSystemEditor = prefs.isDecalEditorPreferenceSystem();
			commandLine = prefs.getDecalEditorCommandLine();
		} else {
			useSystemEditor = dialog.isUseSystemEditor();
			commandLine = dialog.getCommandLine();
			// Do we need to save the preferences?
			if (dialog.isSavePreferences()) {
				prefs.setDecalEditorPreference(useSystemEditor, commandLine);
			}
		}
		
		if (dialog.isEditOne()) {
			decal = makeDecalUnique(doc, component, decal);
		}
		
		launchEditor(useSystemEditor, commandLine, decal);
		
	}
	
	private static DecalImage makeDecalUnique(OpenRocketDocument doc, RocketComponent component, DecalImage decal) {
		
		DecalImage newImage = doc.makeUniqueDecal(decal);
		
		AppearanceBuilder appearanceBuilder = new AppearanceBuilder(component.getAppearance());
		appearanceBuilder.setImage(newImage);
		
		component.setAppearance(appearanceBuilder.getAppearance());
		
		return newImage;
	}
	
	private static void launchEditor(boolean useSystemEditor, String commandTemplate, DecalImage decal) throws IOException {
		
		String decalId = decal.getName();
		// Create Temp File.
		int dotlocation = decalId.lastIndexOf('.');
		String extension = "tmp";
		if (dotlocation > 0 && dotlocation < decalId.length()) {
			extension = decalId.substring(dotlocation);
		}
		File tmpFile = File.createTempFile("OR_graphics", extension);
		
		decal.exportImage(tmpFile, true);
		
		
		if (useSystemEditor) {
			Desktop.getDesktop().edit(tmpFile);
		} else {
			
			String filename = tmpFile.getAbsolutePath();
			
			String command;
			if (commandTemplate.contains("%%")) {
				command = commandTemplate.replace("%%", filename);
			} else {
				command = commandTemplate + " " + filename;
			}
			
			Runtime.getRuntime().exec(command);
			
		}
	}
	
}
