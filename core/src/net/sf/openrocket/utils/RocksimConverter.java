package net.sf.openrocket.utils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.GeneralRocketSaver;
import net.sf.openrocket.file.RocketLoadException;

/**
 * Utility that loads Rocksim file formats and saves them in ORK format.
 * File is saved with the .rkt extension replaced with .ork.
 * 
 * Usage:
 *   java -cp OpenRocket.jar net.sf.openrocket.utils.RocksimConverter <files...>
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocksimConverter {
	
	
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
				opts.setSimulationTimeSkip(StorageOptions.SIMULATION_DATA_NONE);
				opts.setExplicitlySet(true);
				
				GeneralRocketLoader loader = new GeneralRocketLoader(input);
				OpenRocketDocument document = loader.load();
				saver.save(output, document, opts);
				
			} catch (RocketLoadException e) {
				System.err.println("ERROR: Error loading '" + inputFile + "': " + e.getMessage());
			} catch (IOException e) {
				System.err.println("ERROR: Error saving '" + outputFile + "': " + e.getMessage());
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
