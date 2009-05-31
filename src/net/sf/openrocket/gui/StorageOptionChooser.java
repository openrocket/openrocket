package net.sf.openrocket.gui;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.StorageOptions;
import net.sf.openrocket.simulation.FlightData;
import net.sf.openrocket.simulation.FlightDataBranch;

public class StorageOptionChooser extends JPanel {
	
	public static final double DEFAULT_SAVE_TIME_SKIP = 0.20;

	private JRadioButton allButton;
	private JRadioButton someButton;
	private JRadioButton noneButton;
	
	private JSpinner timeSpinner;
	
	private JCheckBox compressButton;
	
	
	private boolean artificialEvent = false;
	
	public StorageOptionChooser(StorageOptions opts) {
		super(new MigLayout());

		ButtonGroup buttonGroup = new ButtonGroup();
		String tip;
		
		this.add(new JLabel("Simulated data to store:"), "spanx, wrap unrel");

		allButton = new JRadioButton("All simulated data");
		allButton.setToolTipText("<html>Store all simulated data.<br>" +
				"This can result in very large files!");
		buttonGroup.add(allButton);
		this.add(allButton, "spanx, wrap rel");
		
		
		someButton = new JRadioButton("Every");
		tip = "<html>Store plottable values approximately this far apart.<br>" +
				"Larger values result in smaller files.";
		someButton.setToolTipText(tip);
		buttonGroup.add(someButton);
		this.add(someButton, "");
		
		timeSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 5.0, 0.1));
		timeSpinner.setToolTipText(tip);
		timeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (artificialEvent)
					return;
				someButton.setSelected(true);
			}
		});
		this.add(timeSpinner, "wmin 55lp");
		
		JLabel label = new JLabel("seconds");
		label.setToolTipText(tip);
		this.add(label, "wrap rel");
		
		
		noneButton = new JRadioButton("Only primary figures");
		noneButton.setToolTipText("<html>Store only the values shown in the summary table.<br>" +
				"This results in the smallest files.");
		buttonGroup.add(noneButton);

		this.add(noneButton, "spanx, wrap 20lp");
		
		
		compressButton = new JCheckBox("Compress file");
		compressButton.setToolTipText("Using compression reduces the file size significantly.");
		this.add(compressButton, "spanx");
		
		
		this.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(0, 10, 0, 0),
				BorderFactory.createTitledBorder("Save options")));
		
		loadOptions(opts);
	}
	
	
	public void loadOptions(StorageOptions opts) {
		double t;
		
		// Data storage radio button
		t = opts.getSimulationTimeSkip();
		if (t == StorageOptions.SIMULATION_DATA_ALL) {
			allButton.setSelected(true);
			t = DEFAULT_SAVE_TIME_SKIP;
		} else if (t == StorageOptions.SIMULATION_DATA_NONE) {
			noneButton.setSelected(true);
			t = DEFAULT_SAVE_TIME_SKIP;
		} else {
			someButton.setSelected(true);
		}
		
		// Time skip spinner
		artificialEvent = true;
		timeSpinner.setValue(t);
		artificialEvent = false;
		
		// Compression checkbox
		compressButton.setSelected(opts.isCompressionEnabled());
	}
	
	
	public void storeOptions(StorageOptions opts) {
		double t;
		
		if (allButton.isSelected()) {
			t = StorageOptions.SIMULATION_DATA_ALL;
		} else if (noneButton.isSelected()) {
			t = StorageOptions.SIMULATION_DATA_NONE;
		} else {
			t = (Double)timeSpinner.getValue();
		}
		
		opts.setSimulationTimeSkip(t);
		
		opts.setCompressionEnabled(compressButton.isSelected());
		
		opts.setExplicitlySet(true);
	}
	
	
	
	/**
	 * Asks the user the storage options using a modal dialog window if the document
	 * contains simulated data and the user has not explicitly set how to store the data.
	 * 
	 * @param document	the document to check.
	 * @param parent	the parent frame for the dialog.
	 * @return			<code>true</code> to continue, <code>false</code> if the user cancelled.
	 */
	public static boolean verifyStorageOptions(OpenRocketDocument document, JFrame parent) {
		StorageOptions options = document.getDefaultStorageOptions();
		
		if (options.isExplicitlySet()) {
			// User has explicitly set the values, save as is
			return true;
		}
		
		
		boolean hasData = false;
		
		simulationLoop:
			for (Simulation s: document.getSimulations()) {
				if (s.getStatus() == Simulation.Status.NOT_SIMULATED ||
						s.getStatus() == Simulation.Status.EXTERNAL)
					continue;
				
				FlightData data = s.getSimulatedData();
				if (data == null)
					continue;
				
				for (int i=0; i < data.getBranchCount(); i++) {
					FlightDataBranch branch = data.getBranch(i);
					if (branch == null)
						continue;
					if (branch.getLength() > 0) {
						hasData = true;
						break simulationLoop;
					}
				}
			}
		

		if (!hasData) {
			// No data to store, do not ask only about compression
			return true;
		}
		
		
		StorageOptionChooser chooser = new StorageOptionChooser(options);
		
		if (JOptionPane.showConfirmDialog(parent, chooser, "Save options", 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) !=
					JOptionPane.OK_OPTION) {
			// User cancelled
			return false;
		}
		
		chooser.storeOptions(options);
		return true;
	}
	
	
}
