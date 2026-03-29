package info.openrocket.swing.gui.util;

import java.awt.Desktop;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import info.openrocket.core.appearance.AppearanceBuilder;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.arch.SystemInfo.Platform;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.InsideColorComponent;
import info.openrocket.core.rocketcomponent.InsideColorComponentHandler;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.DecalNotFoundException;

import info.openrocket.swing.gui.dialogs.DecalNotFoundDialog;
import info.openrocket.swing.gui.dialogs.EditDecalDialog;
import info.openrocket.swing.gui.watcher.FileWatcher;
import info.openrocket.swing.gui.watcher.WatchEvent;
import info.openrocket.swing.gui.watcher.WatchService;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditDecalHelper {

	private static final Logger log = LoggerFactory.getLogger(EditDecalHelper.class);
	private static final long CUSTOM_EDITOR_EXIT_WAIT_SECONDS = 2;
	
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
			if (insideApp) {
				decal = makeDecalUniqueInside(doc, component, decal);
			} else {
				decal = makeDecalUnique(doc, component, decal);
			}
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
			decal.setDecalFile(tmpFile);
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
			launchWithSystemDesktop(tmpFile);
		} else {
			runCustomEditorCommand(commandTemplate, tmpFile.getAbsolutePath());
			
		}
	}

	private void runCustomEditorCommand(String commandTemplate, String filename) throws EditDecalHelperException {
		List<String> commandTokens = buildCustomEditorCommand(commandTemplate, filename);
		if (commandTokens.isEmpty()) {
			throw new EditDecalHelperException(trans.get("EditDecalHelper.launchCustomEditorException"),
					trans.get("EditDecalHelper.editPreferencesHelp"),
					new IllegalArgumentException("Command template is empty"));
		}
		log.debug("Launching custom graphics editor with command tokens {}", commandTokens);

		Process process = null;
		try {
			process = new ProcessBuilder(commandTokens).start();
			drainAndClose(process);

			boolean finished = process.waitFor(CUSTOM_EDITOR_EXIT_WAIT_SECONDS, TimeUnit.SECONDS);
			if (finished && process.exitValue() != 0) {
				String message = formatCustomEditorFailure(String.join(" ", commandTokens));
				throw new EditDecalHelperException(message, trans.get("EditDecalHelper.editPreferencesHelp"),
						new IllegalStateException("Editor exited with code " + process.exitValue()));
			}
		} catch (IOException ioex) {
			String message = formatCustomEditorFailure(String.join(" ", commandTokens));
			throw new EditDecalHelperException(message, trans.get("EditDecalHelper.editPreferencesHelp"), ioex);
		} catch (InterruptedException iex) {
			Thread.currentThread().interrupt();
			String message = formatCustomEditorFailure(String.join(" ", commandTokens));
			throw new EditDecalHelperException(message, trans.get("EditDecalHelper.editPreferencesHelp"), iex);
		} finally {
			if (process != null) {
				process.destroy();
			}
		}
	}

	private void drainAndClose(Process process) {
		try {
			process.getInputStream().close();
		} catch (IOException ignored) {
		}
		try {
			process.getErrorStream().close();
		} catch (IOException ignored) {
		}
		try {
			process.getOutputStream().close();
		} catch (IOException ignored) {
		}
	}

	private String formatCustomEditorFailure(String command) {
		return MessageFormat.format(trans.get("EditDecalHelper.launchCustomEditorException"), command);
	}

	private List<String> buildCustomEditorCommand(String commandTemplate, String filename) {
		String template = commandTemplate == null ? "" : commandTemplate.trim();
		List<String> parsed = splitCommand(template);
		if (parsed.isEmpty() && !template.contains("%%") && !template.isEmpty()) {
			parsed = new ArrayList<>();
			parsed.add(template);
		}

		List<String> resolved = new ArrayList<>();
		boolean placeholderReplaced = false;
		for (String token : parsed) {
			if (token.contains("%%")) {
				resolved.add(token.replace("%%", filename));
				placeholderReplaced = true;
			} else {
				resolved.add(token);
			}
		}
		if (!placeholderReplaced && filename != null && !filename.isEmpty()) {
			resolved.add(filename);
		}

		if (SystemInfo.getPlatform() == Platform.MAC_OS && !startsWithOpenApplication(resolved)) {
			List<String> macCommand = new ArrayList<>();
			macCommand.add("open");
			macCommand.add("-a");
			macCommand.addAll(resolved);
			return macCommand;
		}

		return resolved;
	}

	private List<String> splitCommand(String commandTemplate) {
		if (commandTemplate == null || commandTemplate.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> tokens = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inDouble = false;
		boolean inSingle = false;
		boolean escaping = false;
		for (int i = 0; i < commandTemplate.length(); i++) {
			char c = commandTemplate.charAt(i);
			if (escaping) {
				current.append(c);
				escaping = false;
				continue;
			}
			if (c == '\\' && !inSingle) {
				escaping = true;
				continue;
			}
			if (c == '"' && !inSingle) {
				inDouble = !inDouble;
				continue;
			}
			if (c == '\'' && !inDouble) {
				inSingle = !inSingle;
				continue;
			}
			if (Character.isWhitespace(c) && !inDouble && !inSingle) {
				if (current.length() > 0) {
					tokens.add(current.toString());
					current.setLength(0);
				}
				continue;
			}
			current.append(c);
		}
		if (current.length() > 0) {
			tokens.add(current.toString());
		}
		return tokens;
	}

	private boolean startsWithOpenApplication(List<String> commandTokens) {
		return commandTokens.size() >= 2 &&
				"open".equals(commandTokens.get(0)) &&
				"-a".equals(commandTokens.get(1));
	}

	private void launchWithSystemDesktop(File tmpFile) throws EditDecalHelperException {
		if (!Desktop.isDesktopSupported()) {
			throw new EditDecalHelperException(trans.get("EditDecalHelper.launchSystemEditorException"),
					trans.get("EditDecalHelper.editPreferencesHelp"), new UnsupportedOperationException("Desktop API not supported"));
		}

		Exception lastFailure = null;
		Desktop desktop = Desktop.getDesktop();

		if (desktop.isSupported(Desktop.Action.EDIT)) {
			try {
				desktop.edit(tmpFile);
				return;
			} catch (Exception ex) {
				lastFailure = ex;
				log.error("Desktop#edit failed when launching graphics editor, attempting Desktop#open fallback", ex);
			}
		}

		if (desktop.isSupported(Desktop.Action.OPEN)) {
			try {
				desktop.open(tmpFile);
				return;
			} catch (Exception ex) {
				lastFailure = ex;
			}
		}

		throw new EditDecalHelperException(trans.get("EditDecalHelper.launchSystemEditorException"),
				trans.get("EditDecalHelper.editPreferencesHelp"), lastFailure);
	}
}
