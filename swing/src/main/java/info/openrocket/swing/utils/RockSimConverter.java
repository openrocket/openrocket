package info.openrocket.swing.utils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.StorageOptions;
import info.openrocket.core.file.GeneralRocketLoader;
import info.openrocket.core.file.GeneralRocketSaver;
import info.openrocket.core.file.RocketLoadException;
import info.openrocket.swing.gui.dialogs.DecalNotFoundDialog;
import info.openrocket.core.util.DecalNotFoundException;

/**
 * Utility that loads Rocksim file formats and saves them in ORK format.
 * File is saved with the .rkt extension replaced with .ork.
 * 
 * Usage:
 *   java -cp OpenRocket.jar info.openrocket.swing.utils.RockSimConverter <files...>
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RockSimConverter {
	
	
	public static void main(String[] args) {
		
		setup();
		
		GeneralRocketSaver saver = new GeneralRocketSaver();
		
		for (String inputFile : args) {
			System.out.println("Converting " + inputFile + "...");
			
			if (!inputFile.matches(".*\\.[rR][kK][tT]$")) {
				System.err.println("ERROR: File '" + inputFile + "' does not end in .rkt, skipping.");
				continue;
			}
			
			String outputFile = inputFile.replaceAll("\\.[rR][kK][tT]$", ".ork");
			
			File input = new File(inputFile);
			File output = new File(outputFile);
			
			if (!input.isFile()) {
				System.err.println("ERROR: File '" + inputFile + "' does not exist, skipping.");
				continue;
			}
			if (output.exists()) {
				System.err.println("ERROR: File '" + outputFile + "' already exists, skipping.");
				continue;
			}
			
			try {
				StorageOptions opts = new StorageOptions();
				opts.setFileType(StorageOptions.FileType.OPENROCKET);
				opts.setSaveSimulationData(false);
				opts.setExplicitlySet(true);
				
				GeneralRocketLoader loader = new GeneralRocketLoader(input);
				OpenRocketDocument document = loader.load();
				saver.save(output, document, opts);
				
			} catch (RocketLoadException e) {
				System.err.println("ERROR: Error loading '" + inputFile + "': " + e.getMessage());
			} catch (IOException e) {
				System.err.println("ERROR: Error saving '" + outputFile + "': " + e.getMessage());
			} catch (DecalNotFoundException decex) {
				DecalNotFoundDialog.showDialog(null, decex);
			}
			
		}
		
	}
	
	private static void setup() {
		Locale.setDefault(Locale.US);
		
		BasicApplication app = new BasicApplication();
		app.initializeApplication();
		//?? Application.setPreferences(new SwingPreferences());
	}
}
