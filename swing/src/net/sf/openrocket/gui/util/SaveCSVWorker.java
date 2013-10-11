package net.sf.openrocket.gui.util;

import java.awt.Window;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.file.CSVExport;
import net.sf.openrocket.gui.dialogs.SwingWorkerDialog;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.util.BugException;


public class SaveCSVWorker extends SwingWorker<Void, Void> {
	
	private static final int BYTES_PER_FIELD_PER_POINT = 7;

	private final File file;
	private final Simulation simulation;
	private final FlightDataBranch branch;
	private final FlightDataType[] fields;
	private final Unit[] units;
	private final String fieldSeparator;
	private final String commentStarter;
	private final boolean simulationComments;
	private final boolean fieldComments;
	private final boolean eventComments;
	
	
	public SaveCSVWorker(File file, Simulation simulation, FlightDataBranch branch,
			FlightDataType[] fields, Unit[] units, String fieldSeparator, String commentStarter,
			boolean simulationComments, boolean fieldComments, boolean eventComments) {
		this.file = file;
		this.simulation = simulation;
		this.branch = branch;
		this.fields = fields;
		this.units = units;
		this.fieldSeparator = fieldSeparator;
		this.commentStarter = commentStarter;
		this.simulationComments = simulationComments;
		this.fieldComments = fieldComments;
		this.eventComments = eventComments;
	}


	@Override
	protected Void doInBackground() throws Exception {
		
		int estimate = BYTES_PER_FIELD_PER_POINT * fields.length * branch.getLength();
		estimate = Math.max(estimate, 1000);
		
		// Create the ProgressOutputStream that provides progress estimates
		@SuppressWarnings("resource")
		ProgressOutputStream os = new ProgressOutputStream(
				new BufferedOutputStream(new FileOutputStream(file)), 
				estimate, this) {
			
			@Override
			protected void setProgress(int progress) {
				SaveCSVWorker.this.setProgress(progress);
			}
			
		};
		
		try {
			CSVExport.exportCSV(os, simulation, branch, fields, units, fieldSeparator, 
					commentStarter, simulationComments, fieldComments, eventComments);
		} finally {
			try {
				os.close();
			} catch (Exception e) {
				Application.getExceptionHandler().handleErrorCondition("Error closing file", e);
			}
		}
		return null;
	}
	
	
	
	/**
	 * Exports a CSV file using a progress dialog if necessary.
	 *
	 * @return	<code>true</code> if the save was successful, <code>false</code> otherwise.
	 */
	public static boolean export(File file, Simulation simulation, FlightDataBranch branch,
			FlightDataType[] fields, Unit[] units, String fieldSeparator, String commentStarter,
			boolean simulationComments, boolean fieldComments, boolean eventComments,
			Window parent) {
		

		SaveCSVWorker worker = new SaveCSVWorker(file, simulation, branch, fields, units,
				fieldSeparator, commentStarter, simulationComments, fieldComments, 
				eventComments);
		
	    if (!SwingWorkerDialog.runWorker(parent, "Exporting flight data", 
	    		"Writing " + file.getName() + "...", worker)) {
	    	
	    	// User cancelled the save
	    	file.delete();
	    	return false;
	    }
	    
	    try {
			worker.get();
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			
			if (cause instanceof IOException) {
		    	JOptionPane.showMessageDialog(parent, new String[] { 
		    			"An I/O error occurred while saving:",
		    			e.getMessage() }, "Saving failed", JOptionPane.ERROR_MESSAGE);
		    	return false;
			} else {
				throw new BugException("Unknown error when saving file", e);
			}
			
		} catch (InterruptedException e) {
			throw new BugException("EDT was interrupted", e);
		}
		
		return true;
	}
}
