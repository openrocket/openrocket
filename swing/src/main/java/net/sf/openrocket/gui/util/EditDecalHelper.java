package net.sf.openrocket.gui.util;

import java.awt.Desktop;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.arch.SystemInfo.Platform;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.dialogs.DecalNotFoundDialog;
import net.sf.openrocket.gui.dialogs.EditDecalDialog;
import net.sf.openrocket.gui.watcher.FileWatcher;
import net.sf.openrocket.gui.watcher.WatchEvent;
import net.sf.openrocket.gui.watcher.WatchService;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.InsideColorComponent;
import net.sf.openrocket.rocketcomponent.InsideColorComponentHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;

import com.google.inject.Inject;
import net.sf.openrocket.util.DecalNotFoundException;

public class EditDecalHelper {
	
	@Inject
	private WatchService watchService;
	
	@Inject
	private Translator trans;
	
	@Inject
	private SwingPreferences prefs;
	
	public static class EditDecalHelperException extends Exception {
		private static final long serialVersionUID = 6434514222471759358L;
		
		private String extraMessage = "";
		
		public EditDecalHelperException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public EditDecalHelperException(String message, String extraMessage, Throwable cause) {
			super(message, cause);
			this.extraMessage = extraMessage;
		}
		
		@Override
		public String getMessage() {
			if (extraMessage == null || extraMessage.isEmpty()) {
				return super.getMessage();
			}
			return super.getMessage() + "\n" + getExtraMessage();
		}
		
		public String getExtraMessage() {
			return extraMessage;
		}
		
	}
	
	/**
	 * Returns the decal which is edited.  The decal edited might be different from the one passed in
	 * if only a single copy of a decal should be edited.
	 * 
	 * @param parent
	 * @param doc
	 * @param component
	 * @param decal
	 * @param insideApp flag to check whether it is the inside appearance that is edited
	 * @return
	 * @throws EditDecalHelperException
	 */
	public DecalImage editDecal(Window parent, OpenRocketDocument doc, RocketComponent component, DecalImage decal,
								boolean insideApp) throws EditDecalHelperException {
		
		boolean sysPrefSet = prefs.isDecalEditorPreferenceSet();
		int usageCount = doc.countDecalUsage(decal);
		boolean isSnapConfined = (SystemInfo.getPlatform() == Platform.UNIX && SystemInfo.isConfined());
		
		//First Check preferences
		if (usageCount == 1 && (sysPrefSet || isSnapConfined)) {
			String commandLine = isSnapConfined ? "xdg-open %%" : prefs.getDecalEditorCommandLine();
			launchEditor(parent, prefs.isDecalEditorPreferenceSystem(), commandLine, decal);
			return decal;
		}
		
		boolean promptForEditor = (!sysPrefSet && !isSnapConfined);
		EditDecalDialog dialog = new EditDecalDialog(parent, promptForEditor, usageCount);
		dialog.setVisible(true);
		
		if (dialog.isCancel()) {
			return decal;
		}
		
		// Do we use the System Preference Editor or from the dialog?
		boolean useSystemEditor = false;
		String commandLine = "";
		
		if (isSnapConfined) {
			useSystemEditor = false;
			commandLine = "xdg-open %%";
		} else if (sysPrefSet) {
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
			if (insideApp)
				decal = makeDecalUnique(doc, component, decal);
			else
				decal = makeDecalUniqueInside(doc, component, decal);
		}
		
		launchEditor(parent, useSystemEditor, commandLine, decal);
		
		return decal;
		
	}
	
	private static DecalImage makeDecalUnique(OpenRocketDocument doc, RocketComponent component, DecalImage decal) {
		
		DecalImage newImage = doc.makeUniqueDecal(decal);
		
		AppearanceBuilder appearanceBuilder = new AppearanceBuilder(component.getAppearance());
		appearanceBuilder.setImage(newImage);
		
		component.setAppearance(appearanceBuilder.getAppearance());
		
		return newImage;
	}

	private static DecalImage makeDecalUniqueInside(OpenRocketDocument doc, RocketComponent component, DecalImage decal) {

		DecalImage newImage = doc.makeUniqueDecal(decal);

		if (component instanceof InsideColorComponent) {
			InsideColorComponentHandler handler = ((InsideColorComponent)component).getInsideColorComponentHandler();
			AppearanceBuilder appearanceBuilder = new AppearanceBuilder(handler.getInsideAppearance());
			appearanceBuilder.setImage(newImage);

			handler.setInsideAppearance(appearanceBuilder.getAppearance());
		}

		return newImage;
	}
	
	private void launchEditor(Window parent, boolean useSystemEditor, String commandTemplate, final DecalImage decal) throws EditDecalHelperException {
		
		String decalId = decal.getName();
		// Create Temp File.
		int dotlocation = decalId.lastIndexOf('.');
		String extension = "tmp";
		if (dotlocation > 0 && dotlocation < decalId.length()) {
			extension = decalId.substring(dotlocation);
		}
		File tmpFile = null;
		
		try {
			tmpFile = File.createTempFile("OR_graphics", extension);
		} catch (IOException ioex) {
			String message = MessageFormat.format(trans.get("EditDecalHelper.createFileException"), "OR_graphics"+extension);
			throw new EditDecalHelperException(message, ioex);
		}
		
		try {
			decal.exportImage(tmpFile);
			watchService.register(new FileWatcher(tmpFile) {
				
				@Override
				public void handleEvent(WatchEvent evt) {
					decal.fireChangeEvent(evt);
					//System.out.println(this.getFile() + " has changed");
					
				}
				
			});
			
		} catch (IOException ioex) {
			String message = MessageFormat.format(trans.get("EditDecalHelper.createFileException"), tmpFile.getAbsoluteFile());
			throw new EditDecalHelperException(message, ioex);
		} catch (DecalNotFoundException decex) {
			if (DecalNotFoundDialog.showDialog(parent, decex)) {
				launchEditor(parent, useSystemEditor, commandTemplate, decal);
			}
			return;
		}
		
		
		if (useSystemEditor) {
			try {
				Desktop.getDesktop().edit(tmpFile);
			} catch (Exception ioex) {
				throw new EditDecalHelperException(trans.get("EditDecalHelper.launchSystemEditorException"), trans.get("EditDecalHelper.editPreferencesHelp"), ioex);
			}
		} else {
			
			String filename = tmpFile.getAbsolutePath();
			
			String command;
			if (commandTemplate.contains("%%")) {
				command = commandTemplate.replace("%%", filename);
			} else {
				command = commandTemplate + " " + filename;
			}
			
			/* On OSX, the program needs to be opened using the command
			 * open -a to tell it which application to use to open the 
			 * program. Check to see if the command starts with it or not
			 * and pre-pend it as necessary. See issue #619.
			 */
			if (SystemInfo.getPlatform() == Platform.MAC_OS && !command.startsWith("open -a")) {
				command = "open -a " + command;
			}
			
			try {
				Runtime.getRuntime().exec(command);
			} catch (IOException ioex) {
				String message = MessageFormat.format(trans.get("EditDecalHelper.launchCustomEditorException"), command);
				throw new EditDecalHelperException(message, trans.get("EditDecalHelper.editPreferencesHelp"), ioex);
			}
			
		}
	}
}
