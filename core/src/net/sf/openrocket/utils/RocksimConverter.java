package net.sf.openrocket.utils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.file.DatabaseMotorFinder;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.file.RocketSaver;
import net.sf.openrocket.file.openrocket.OpenRocketSaver;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.logging.LogLevel;
import net.sf.openrocket.startup.Application;

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
		
		GeneralRocketLoader loader = new GeneralRocketLoader();
		RocketSaver saver = new OpenRocketSaver();
		
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
				opts.setCompressionEnabled(true);
				opts.setSimulationTimeSkip(StorageOptions.SIMULATION_DATA_NONE);
				opts.setExplicitlySet(true);
				
				OpenRocketDocument document = loader.load(input, new DatabaseMotorFinder());
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
		Application.setBaseTranslator(new ResourceBundleTranslator("l10n.messages"));
		
		Application.setLogOutputLevel(LogLevel.WARN);
		Application.setPreferences(new SwingPreferences());
	}
}
