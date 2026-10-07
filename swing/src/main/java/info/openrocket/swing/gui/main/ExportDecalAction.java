package info.openrocket.swing.gui.main;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.DecalNotFoundException;

import info.openrocket.swing.gui.dialogs.DecalNotFoundDialog;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.OverwritePrompter;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.SaveFileChooser;

public abstract class ExportDecalAction {
	
	private final static Translator trans = Application.getTranslator();
	
	static OverwritePrompter.Prompt overwritePrompt = OverwritePrompter.defaultPrompt();

	public static void export(Window parent, OpenRocketDocument doc) {
		final SaveFileChooser chooser = new SaveFileChooser();
		final List<DecalImage> selectedDecals;
		
		Collection<DecalImage> exportableDecals = doc.getDecalList();
		if (exportableDecals.isEmpty()) {
			JOptionPane.showMessageDialog(parent, trans.get("ExportDecalDialog.selection.required"));
			return;
		}
		
		ExportDecalSelectionDialog selectionDialog = new ExportDecalSelectionDialog(parent, exportableDecals);
		selectionDialog.setLocationRelativeTo(parent);
		selectionDialog.setVisible(true);
		
		if (!selectionDialog.isApproved()) {
			return;
		}
		
		List<DecalImage> decals = selectionDialog.getSelectedDecals();
		if (decals.isEmpty()) {
			return;
		}
		selectedDecals = new ArrayList<>(decals);

		int returnVal = openChooserDialog(parent, chooser, selectedDecals);

		// Cancelled file chooser
		if (returnVal != SaveFileChooser.APPROVE_OPTION) {
			return;
		}

		// Approved file chooser
		handleApproval(parent, chooser, selectedDecals);
	}
	
	static int openChooserDialog(Window parent, SaveFileChooser chooser, List<DecalImage> selectedDecals) {
		File dir = Application.getPreferences().getDefaultDirectory();
		List<String> names = new ArrayList<>();
		for (DecalImage decal : selectedDecals) {
			names.add(new File(decal.getName()).getName());
		}
		SaveFileChooser.SelectionMode mode = chooser.configureForTargets(names, dir);
		if (mode == SaveFileChooser.SelectionMode.SINGLE_FILE) {
			return chooser.showSaveDialog(parent);
		}
		return chooser.showOpenDialog(parent);
	}
	
	static boolean handleApproval(Window parent, SaveFileChooser chooser, List<DecalImage> selectedDecals) {
		File selectedFile = chooser.getSelectedFile();
		if (selectedDecals.size() == 1) {
			DecalImage decal = selectedDecals.get(0);
			File file = selectedFile;
			if (file == null) {
				file = new File(chooser.getCurrentDirectory(), new File(decal.getName()).getName());
			}
			Application.getPreferences().setDefaultDirectory(chooser.getCurrentDirectory());
			if (!FileHelper.confirmWrite(file, selectedFile, parent)) {
				return false;
			}
			return export(parent, decal, file);
		} else {
			OverwritePrompter prompter = new OverwritePrompter(overwritePrompt);
			File targetDirectory = selectedFile;
			if (targetDirectory == null) {
				targetDirectory = chooser.getCurrentDirectory();
			}
			if (ensureDirectory(parent, targetDirectory)) {
				Application.getPreferences().setDefaultDirectory(targetDirectory);
				return exportMultiple(parent, selectedDecals, targetDirectory, prompter);
			}
		}

		return false;
	}
	
	static boolean ensureDirectory(Window parent, File directory) {
		if (directory == null) {
			JOptionPane.showMessageDialog(parent, trans.get("ExportDecalDialog.directory.required"));
			return false;
		}
		if (!directory.exists() && !directory.mkdirs()) {
			JOptionPane.showMessageDialog(parent, trans.get("ExportDecalDialog.directory.required"));
			return false;
		}
		if (!directory.isDirectory()) {
			JOptionPane.showMessageDialog(parent, trans.get("ExportDecalDialog.directory.required"));
			return false;
		}
		return true;
	}
	
	static boolean export(Window parent, DecalImage decal, File selectedFile) {
		if (selectedFile == null) {
			return false;
		}
		try {
			decal.exportImage(selectedFile);
			return true;
		} catch (DecalNotFoundException e) {
			DecalNotFoundDialog.showDialog(parent, e);
		} catch (IOException iex) {
			String message = MessageFormat.format(trans.get("ExportDecalDialog.exception"), selectedFile.getAbsoluteFile());
			JOptionPane.showMessageDialog(parent, message, "", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	static boolean exportMultiple(Window parent, List<DecalImage> decals, File targetDirectory,
			OverwritePrompter prompter) {
		boolean exported = false;
		for (DecalImage decal : decals) {
			File sourceName = new File(decal.getName());
			File exportTarget = new File(targetDirectory, sourceName.getName());
			if (!prompter.canOverwrite(parent, exportTarget)) {
				return false;
			}
			if (!export(parent, decal, exportTarget)) {
				return false;
			}
			exported = true;
		}
		return exported;
	}
	
	static void setOverwritePrompt(OverwritePrompter.Prompt prompt) {
		overwritePrompt = prompt != null ? prompt : OverwritePrompter.defaultPrompt();
	}
}
