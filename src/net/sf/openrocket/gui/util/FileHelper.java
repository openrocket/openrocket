package net.sf.openrocket.gui.util;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.openrocket.l10n.L10N;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;

/**
 * Helper methods related to user-initiated file manipulation.
 * <p>
 * These methods log the necessary information to the debug log.
* 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public final class FileHelper {
	private static final LogHelper log = Application.getLogger();
	private static final Translator trans = Application.getTranslator();
	

	// TODO: HIGH: Rename translation keys
	
	/** File filter for any rocket designs (*.ork, *.rkt) */
	public static final FileFilter ALL_DESIGNS_FILTER =
			new SimpleFileFilter(trans.get("BasicFrame.SimpleFileFilter1"),
					".ork", ".ork.gz", ".rkt", ".rkt.gz");
	
	/** File filter for OpenRocket designs (*.ork) */
	public static final FileFilter OPENROCKET_DESIGN_FILTER =
			new SimpleFileFilter(trans.get("BasicFrame.SimpleFileFilter2"), ".ork", ".ork.gz");
	
	/** File filter for RockSim designs (*.rkt) */
	public static final FileFilter ROCKSIM_DESIGN_FILTER =
			new SimpleFileFilter(trans.get("BasicFrame.SimpleFileFilter3"), ".rkt", ".rkt.gz");
	
	/** File filter for PDF files (*.pdf) */
	public static final FileFilter PDF_FILTER =
			new SimpleFileFilter(trans.get("filetypes.pdf"), ".pdf");
	
	/** File filter for CSV files (*.csv) */
	public static final FileFilter CSV_FILE_FILTER =
			new SimpleFileFilter(trans.get("SimExpPan.desc"), ".csv");
	
	



	private FileHelper() {
		// Prevent instantiation
	}
	
	/**
	 * Ensure that the provided file has a file extension.  If the file does not have
	 * any extension, append the provided extension to it.
	 * 
	 * @param original		the original file
	 * @param extension		the extension to append if none exists (without preceding dot)
	 * @return				the resulting filen
	 */
	public static File ensureExtension(File original, String extension) {
		
		if (original.getName().indexOf('.') < 0) {
			log.debug(1, "File name does not contain extension, adding '" + extension + "'");
			String name = original.getAbsolutePath();
			name = name + "." + extension;
			return new File(name);
		}
		
		return original;
	}
	
	
	/**
	 * Confirm that it is allowed to write to a file.  If the file exists,
	 * a confirmation dialog will be presented to the user to ensure overwriting is ok.
	 * 
	 * @param file		the file that is going to be written.
	 * @param parent	the parent component for the dialog.
	 * @return			<code>true</code> to write, <code>false</code> to abort.
	 */
	public static boolean confirmWrite(File file, Component parent) {
		if (file.exists()) {
			log.info(1, "File " + file + " exists, confirming overwrite from user");
			int result = JOptionPane.showConfirmDialog(parent,
					L10N.replace(trans.get("error.fileExists.desc"), "{filename}", file.getName()),
					trans.get("error.fileExists.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result != JOptionPane.YES_OPTION) {
				log.user(1, "User decided not to overwrite the file");
				return false;
			}
			log.user(1, "User decided to overwrite the file");
		}
		return true;
	}
	
	
	/**
	 * Display an error message to the user that writing a file failed.
	 * 
	 * @param e			the I/O exception that caused the error.
	 * @param parent	the parent component for the dialog.
	 */
	public static void errorWriting(IOException e, Component parent) {
		
		log.warn(1, "Error writing to file", e);
		JOptionPane.showMessageDialog(parent,
				new Object[] {
						trans.get("error.writing.desc"),
						e.getLocalizedMessage()
				}, trans.get("error.writing.title"), JOptionPane.ERROR_MESSAGE);
		
	}
	
}
